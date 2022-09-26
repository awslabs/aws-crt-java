/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <aws/auth/auth.h>
#include <aws/common/allocator.h>
#include <aws/common/atomics.h>
#include <aws/common/clock.h>
#include <aws/common/common.h>
#include <aws/common/hash_table.h>
#include <aws/common/logging.h>
#include <aws/common/rw_lock.h>
#include <aws/common/string.h>
#include <aws/common/system_info.h>
#include <aws/common/thread.h>
#include <aws/event-stream/event_stream.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/io/channel.h>
#include <aws/io/io.h>
#include <aws/io/logging.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/mqtt/mqtt.h>
#include <aws/s3/s3.h>

#include <stdio.h>

#include "crt.h"
#include "java_class_ids.h"
#include "logging.h"

/* 0 = off, 1 = bytes, 2 = stack traces, see aws_mem_trace_level */
int g_memory_tracing = 0;
static struct aws_allocator *s_init_allocator(void) {
    if (g_memory_tracing) {
        struct aws_allocator *allocator = aws_default_allocator();
        allocator = aws_mem_tracer_new(allocator, NULL, (enum aws_mem_trace_level)g_memory_tracing, 8);
        return allocator;
    }
    return aws_default_allocator();
}

static struct aws_allocator *s_allocator = NULL;
struct aws_allocator *aws_jni_get_allocator() {
    if (AWS_UNLIKELY(s_allocator == NULL)) {
        s_allocator = s_init_allocator();
    }
    return s_allocator;
}

static void s_detach_jvm_from_thread(void *user_data) {
    AWS_LOGF_DEBUG(AWS_LS_COMMON_GENERAL, "s_detach_jvm_from_thread invoked");
    JavaVM *jvm = user_data;

    /* we don't need this JNIEnv, but this is an easy way to verify the JVM is still valid to use */
    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env != NULL) {
        (*jvm)->DetachCurrentThread(jvm);

        aws_jni_release_thread_env(jvm, env);
        /********** JNI ENV RELEASE **********/
    }
}

static JNIEnv *s_aws_jni_get_thread_env(JavaVM *jvm) {
#ifdef ANDROID
    JNIEnv *env = NULL;
#else
    void *env = NULL;
#endif
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        AWS_LOGF_DEBUG(AWS_LS_COMMON_GENERAL, "s_aws_jni_get_thread_env returned detached, attaching");
#ifdef ANDROID
        jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, &env, NULL);
#else
        jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)&env, NULL);
#endif
        /* Ran out of memory, don't log in this case */
        AWS_FATAL_ASSERT(result != JNI_ENOMEM);
        if (result != JNI_OK) {
            fprintf(stderr, "Unrecoverable AttachCurrentThreadAsDaemon failed, JNI error code is %d\n", (int)result);
            return NULL;
        }
        /* This should only happen in event loop threads, the JVM main thread attachment is
         * managed by the JVM, so we only need to clean up event loop thread attachments */
        AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_thread_current_at_exit(s_detach_jvm_from_thread, (void *)jvm));
    }

    return env;
}

/*
A simple system to support unpredictable JVM shutdowns.  In an ideal world, everyone would correctly use the
CrtResource ref counting and strict (aws_thread_managed_join_all) shutdown, but given the difficulty of using
them correctly, that's not a realistic expectation.  So we need to come up with a way for JVM shutdowns to
not trigger crashes from native threads that try and call back to Java (where even the JavaVM pointer cached
on the binding object is now garbage) after the JVM has shutdown (but before the process has killed all of its
threads).

Our system works as follows:

We track the set of all active JVMs (since we don't correctly support multiple JVMs yet, this is always going to be
either one or zero for now).  We protect this set with a read-write lock.  Adding (CRT init) or removing (JVM
shutdown hook) a JVM from this set will take a write lock.  Acquiring a JNIEnv from a tracked JVM, will take a read
lock, and releasing a JNIEnv will release the read lock.

Acquiring a JNIEnv succeeds if the JVM in question is in our set, and fails otherwise.  All users of a JNIEnv have
been hardened to check for null and just not call to Java in that case.

Since we don't have RAII in C, bindings must be very careful to release once, and exactly once, every JNIEnv that
they acquire.  An alternative approach would be to replace all of the JNIEnv usage with a new API that
takes the lock, calls a supplied callback (which does all the JNIEnv operations), and then releases the lock.  This
approach was tried but was so disruptive refactor-wise that I deemed it too dangerous to try and push through.  So
instead, we just have to be careful with acquire/release.

In this way, the vast majority of usage is relatively contentionless; it's just a bunch of native threads taking
read locks on a shared rw lock.  Only when the JVM shutdown hook calls into native is there read-write contention.
 */
static struct aws_rw_lock s_jvm_table_lock = AWS_RW_LOCK_INIT;
static struct aws_hash_table *s_jvms = NULL;

static void s_jvm_table_add_jvm_for_env(JNIEnv *env) {
    aws_rw_lock_wlock(&s_jvm_table_lock);

    if (s_jvms == NULL) {
        /* use default allocator so that tracing allocator doesn't flag this as a leak during tests */
        s_jvms = aws_mem_calloc(aws_default_allocator(), 1, sizeof(struct aws_hash_table));
        AWS_FATAL_ASSERT(
            AWS_OP_SUCCESS ==
            aws_hash_table_init(s_jvms, aws_default_allocator(), 1, aws_hash_ptr, aws_ptr_eq, NULL, NULL));
    }

    JavaVM *jvm = NULL;
    jint jvmresult = (*env)->GetJavaVM(env, &jvm);
    AWS_FATAL_ASSERT(jvmresult == 0 && jvm != NULL);

    int was_created = 0;
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_put(s_jvms, jvm, NULL, &was_created));
    AWS_FATAL_ASSERT(was_created == 1);

    aws_rw_lock_wunlock(&s_jvm_table_lock);
}

static void s_jvm_table_remove_jvm_for_env(JNIEnv *env) {
    aws_rw_lock_wlock(&s_jvm_table_lock);

    if (s_jvms == NULL) {
        goto done;
    }

    JavaVM *jvm = NULL;
    jint jvmresult = (*env)->GetJavaVM(env, &jvm);
    AWS_FATAL_ASSERT(jvmresult == 0 && jvm != NULL);

    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_remove(s_jvms, jvm, NULL, NULL));

    if (aws_hash_table_get_entry_count(s_jvms) == 0) {
        aws_hash_table_clean_up(s_jvms);
        aws_mem_release(aws_default_allocator(), s_jvms);
        s_jvms = NULL;
    }

done:

    aws_rw_lock_wunlock(&s_jvm_table_lock);
}

JNIEnv *aws_jni_acquire_thread_env(JavaVM *jvm) {
    /*
     * We use try-lock here in order to avoid the re-entrant deadlock case that could happen if we have a read
     * lock already, the JVM shutdown hooks causes another thread to block on taking the write lock, and then
     * we try to reacquire the read-lock recursively due to some synchronous code path.  That case can deadlock
     * but since the JVM is going away, it's safe to just fail completely from here on out.
     */
    if (aws_rw_lock_try_rlock(&s_jvm_table_lock)) {
        if (aws_last_error() != AWS_ERROR_UNSUPPORTED_OPERATION) {
            aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_DESTROYED);
        }
        return NULL;
    }

    if (s_jvms == NULL) {
        aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_DESTROYED);
        goto error;
    }

    struct aws_hash_element *element = NULL;
    int find_result = aws_hash_table_find(s_jvms, jvm, &element);
    if (find_result != AWS_OP_SUCCESS || element == NULL) {
        aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_DESTROYED);
        goto error;
    }

    JNIEnv *env = s_aws_jni_get_thread_env(jvm);
    if (env == NULL) {
        aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_DESTROYED);
        goto error;
    }

    return env;

error:

    aws_rw_lock_runlock(&s_jvm_table_lock);

    return NULL;
}

void aws_jni_release_thread_env(JavaVM *jvm, JNIEnv *env) {
    (void)jvm;
    (void)env;

    if (env != NULL) {
        aws_rw_lock_runlock(&s_jvm_table_lock);
    }
}

void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    char buf[1024];
    vsnprintf(buf, sizeof(buf), msg, args);
    va_end(args);

    int error = aws_last_error();
    char exception[1280];
    snprintf(
        exception,
        sizeof(exception),
        "%s (aws_last_error: %s(%d), %s)",
        buf,
        aws_error_name(error),
        error,
        aws_error_str(error));
    jclass runtime_exception = crt_runtime_exception_properties.crt_runtime_exception_class;
    (*env)->ThrowNew(env, runtime_exception, exception);
}

void aws_jni_throw_null_pointer_exception(JNIEnv *env, const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    char buf[1024];
    vsnprintf(buf, sizeof(buf), msg, args);
    va_end(args);
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/NullPointerException"), buf);
}

void aws_jni_throw_illegal_argument_exception(JNIEnv *env, const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    char buf[1024];
    vsnprintf(buf, sizeof(buf), msg, args);
    va_end(args);
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/IllegalArgumentException"), buf);
}

bool aws_jni_check_and_clear_exception(JNIEnv *env) {
    bool exception_pending = (*env)->ExceptionCheck(env);
    if (exception_pending) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
    return exception_pending;
}

jbyteArray aws_java_byte_array_new(JNIEnv *env, size_t size) {
    jbyteArray jArray = (*env)->NewByteArray(env, (jsize)size);
    return jArray;
}

bool aws_copy_native_array_to_java_byte_array(JNIEnv *env, jbyteArray dst, uint8_t *src, size_t amount) {
    (*env)->SetByteArrayRegion(env, dst, 0, (jsize)amount, (jbyte *)src);
    return aws_jni_check_and_clear_exception(env);
}

/**
 * Converts a Native aws_byte_cursor to a Java byte[]
 */
jbyteArray aws_jni_byte_array_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data) {
    jbyteArray jArray = aws_java_byte_array_new(env, native_data->len);
    if (jArray) {
        if (!aws_copy_native_array_to_java_byte_array(env, jArray, native_data->ptr, native_data->len)) {
            return jArray;
        }
    }
    return NULL;
}

/**
 * Get the Buffer Position (the next element to read/write)
 */
int aws_jni_byte_buffer_get_position(JNIEnv *env, jobject java_byte_buffer) {
    jint position = (*env)->CallIntMethod(env, java_byte_buffer, byte_buffer_properties.get_position);
    return (aws_jni_check_and_clear_exception(env)) ? -1 : (int)position;
}

/**
 * Set the Buffer Position (the next element to read/write)
 */
void aws_jni_byte_buffer_set_position(JNIEnv *env, jobject jByteBuf, jint position) {
    jobject val = (*env)->CallObjectMethod(env, jByteBuf, byte_buffer_properties.set_position, position);
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    (*env)->DeleteLocalRef(env, val);
}

/**
 * Set the Buffer Limit (the max allowed element to read/write)
 */
void aws_jni_byte_buffer_set_limit(JNIEnv *env, jobject jByteBuf, jint limit) {
    jobject val = (*env)->CallObjectMethod(env, jByteBuf, byte_buffer_properties.set_limit, limit);
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    (*env)->DeleteLocalRef(env, val);
}

jobject aws_jni_direct_byte_buffer_from_raw_ptr(JNIEnv *env, const void *dst, size_t capacity) {

    jobject jByteBuf = (*env)->NewDirectByteBuffer(env, (void *)dst, (jlong)capacity);
    if (jByteBuf) {
        aws_jni_byte_buffer_set_limit(env, jByteBuf, (jint)capacity);
        aws_jni_byte_buffer_set_position(env, jByteBuf, 0);
    }

    return jByteBuf;
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jstring_acquire(JNIEnv *env, jstring str) {
    if (str == NULL) {
        aws_jni_throw_null_pointer_exception(env, "string is null");
        return aws_byte_cursor_from_array(NULL, 0);
    }

    const char *bytes = (*env)->GetStringUTFChars(env, str, NULL);
    if (bytes == NULL) {
        /* GetStringUTFChars() has thrown exception */
        return aws_byte_cursor_from_array(NULL, 0);
    }

    return aws_byte_cursor_from_array(bytes, (size_t)(*env)->GetStringUTFLength(env, str));
}

void aws_jni_byte_cursor_from_jstring_release(JNIEnv *env, jstring str, struct aws_byte_cursor cur) {
    if (cur.ptr != NULL) {
        (*env)->ReleaseStringUTFChars(env, str, (const char *)cur.ptr);
    }
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray_acquire(JNIEnv *env, jbyteArray array) {
    if (array == NULL) {
        aws_jni_throw_null_pointer_exception(env, "byte[] is null");
        return aws_byte_cursor_from_array(NULL, 0);
    }

    jbyte *bytes = (*env)->GetByteArrayElements(env, array, NULL);
    if (bytes == NULL) {
        /* GetByteArrayElements() has thrown exception */
        return aws_byte_cursor_from_array(NULL, 0);
    }

    size_t len = (*env)->GetArrayLength(env, array);
    return aws_byte_cursor_from_array(bytes, len);
}

void aws_jni_byte_cursor_from_jbyteArray_release(JNIEnv *env, jbyteArray array, struct aws_byte_cursor cur) {
    if (cur.ptr != NULL) {
        (*env)->ReleaseByteArrayElements(env, array, (jbyte *)cur.ptr, JNI_ABORT);
    }
}

struct aws_byte_cursor aws_jni_byte_cursor_from_direct_byte_buffer(JNIEnv *env, jobject byte_buffer) {
    jlong payload_size = (*env)->GetDirectBufferCapacity(env, byte_buffer);
    if (payload_size == -1) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_publish: Unable to get capacity of payload ByteBuffer");
        return aws_byte_cursor_from_array(NULL, 0);
    }
    jbyte *payload_data = (*env)->GetDirectBufferAddress(env, byte_buffer);
    if (!payload_data) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_publish: Unable to get buffer from payload ByteBuffer");
        return aws_byte_cursor_from_array(NULL, 0);
    }
    return aws_byte_cursor_from_array((const uint8_t *)payload_data, (size_t)payload_size);
}

struct aws_string *aws_jni_new_string_from_jstring(JNIEnv *env, jstring str) {
    struct aws_allocator *allocator = aws_jni_get_allocator();
    const char *str_chars = (*env)->GetStringUTFChars(env, str, NULL);
    if (!str_chars) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        return NULL;
    }
    struct aws_string *result = aws_string_new_from_c_str(allocator, str_chars);
    (*env)->ReleaseStringUTFChars(env, str, str_chars);
    return result;
}

jobject aws_jni_new_crt_exception_from_error_code(JNIEnv *env, int error_code) {
    jint jni_error_code = error_code;

    jobject crt_exception = (*env)->NewObject(
        env,
        crt_runtime_exception_properties.crt_runtime_exception_class,
        crt_runtime_exception_properties.constructor_method_id,
        jni_error_code);
    AWS_FATAL_ASSERT(crt_exception);
    return crt_exception;
}

#define AWS_DEFINE_ERROR_INFO_CRT(CODE, STR) AWS_DEFINE_ERROR_INFO(CODE, STR, "aws-crt-java")

/* clang-format off */
static struct aws_error_info s_crt_errors[] = {
    AWS_DEFINE_ERROR_INFO_CRT(
        AWS_ERROR_JAVA_CRT_JVM_DESTROYED,
        "Attempt to use a JVM that has already been destroyed"),
};
/* clang-format on */

static struct aws_error_info_list s_crt_error_list = {
    .error_list = s_crt_errors,
    .count = sizeof(s_crt_errors) / sizeof(struct aws_error_info),
};

static struct aws_log_subject_info s_crt_log_subject_infos[] = {
    DEFINE_LOG_SUBJECT_INFO(
        AWS_LS_JAVA_CRT_GENERAL,
        "JavaCrtGeneral",
        "Subject for aws-crt-java logging that defies categorization."),
};

static struct aws_log_subject_info_list s_crt_log_subject_list = {
    .subject_list = s_crt_log_subject_infos,
    .count = AWS_ARRAY_SIZE(s_crt_log_subject_infos),
};

static void s_jni_atexit_strict(void) {

    aws_unregister_log_subject_info_list(&s_crt_log_subject_list);
    aws_unregister_error_info(&s_crt_error_list);

    aws_s3_library_clean_up();
    aws_event_stream_library_clean_up();
    aws_auth_library_clean_up();
    aws_http_library_clean_up();
    aws_mqtt_library_clean_up();

    if (g_memory_tracing) {
        struct aws_allocator *tracer_allocator = aws_jni_get_allocator();
        aws_mem_tracer_dump(tracer_allocator);
    }

    aws_jni_cleanup_logging();

    if (g_memory_tracing) {
        struct aws_allocator *tracer_allocator = aws_jni_get_allocator();
        aws_mem_tracer_destroy(tracer_allocator);
    }

    s_allocator = NULL;
}

#define DEFAULT_MANAGED_SHUTDOWN_WAIT_IN_SECONDS 1

static void s_jni_atexit_gentle(void) {

    /* If not doing strict shutdown, wait only a short time before shutting down */
    aws_thread_set_managed_join_timeout_ns(
        aws_timestamp_convert(DEFAULT_MANAGED_SHUTDOWN_WAIT_IN_SECONDS, AWS_TIMESTAMP_SECS, AWS_TIMESTAMP_NANOS, NULL));

    if (aws_thread_join_all_managed() == AWS_OP_SUCCESS) {
        /* a successful managed join means it should be safe to do a full, strict clean up */
        s_jni_atexit_strict();
    } else {
        /*
         * We didn't successfully join all our threads so it's not really safe to clean up the libraries.
         * Just dump memory if applicable and exit.
         */
        AWS_LOGF_WARN(
            AWS_LS_JAVA_CRT_GENERAL,
            "Not all native threads were successfully joined during gentle shutdown.  Memory may be leaked.");

        if (g_memory_tracing) {
            AWS_LOGF_DEBUG(
                AWS_LS_JAVA_CRT_GENERAL,
                "At shutdown, %u bytes remaining",
                (uint32_t)aws_mem_tracer_bytes(aws_jni_get_allocator()));
            if (g_memory_tracing > 1) {
                aws_mem_tracer_dump(aws_jni_get_allocator());
            }
        }
    }
}

#define KB_256 (256 * 1024)

/* Called as the entry point, immediately after the shared lib is loaded the first time by JNI */
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_awsCrtInit(
    JNIEnv *env,
    jclass jni_crt_class,
    jint jni_memtrace,
    jboolean jni_debug_wait,
    jboolean jni_strict_shutdown) {
    (void)jni_crt_class;

    if (jni_debug_wait) {
        bool done = false;
        while (!done) {
            ;
        }
    }

    g_memory_tracing = jni_memtrace;

    /*
     * Increase the maximum channel message size in order to improve throughput on large payloads.
     * Consider adding a system property override in the future.
     */
    g_aws_channel_max_fragment_size = KB_256;

    /* check to see if we have support for backtraces only if we need to */
    void *stack[1];
    if (g_memory_tracing > 1 && 0 == aws_backtrace(stack, 1)) {
        g_memory_tracing = 1;
    }

    /* NOT using aws_jni_get_allocator to avoid trace leak outside the test */
    struct aws_allocator *allocator = aws_default_allocator();
    aws_mqtt_library_init(allocator);
    aws_http_library_init(allocator);
    aws_auth_library_init(allocator);
    aws_event_stream_library_init(allocator);
    aws_s3_library_init(allocator);

    aws_register_error_info(&s_crt_error_list);
    aws_register_log_subject_info_list(&s_crt_log_subject_list);

    s_jvm_table_add_jvm_for_env(env);
    cache_java_class_ids(env);

    if (jni_strict_shutdown) {
        atexit(s_jni_atexit_strict);
    } else {
        atexit(s_jni_atexit_gentle);
    }
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_onJvmShutdown(JNIEnv *env, jclass jni_crt_class) {

    (void)jni_crt_class;

    s_jvm_table_remove_jvm_for_env(env);
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_CRT_awsLastError(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    return aws_last_error();
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_CRT_awsErrorString(JNIEnv *env, jclass jni_crt_class, jint error_code) {
    (void)jni_crt_class;
    const char *error_msg = aws_error_str(error_code);
    return (*env)->NewStringUTF(env, error_msg);
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_CRT_awsErrorName(JNIEnv *env, jclass jni_crt_class, jint error_code) {
    (void)jni_crt_class;
    const char *error_msg = aws_error_name(error_code);
    return (*env)->NewStringUTF(env, error_msg);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_CRT_awsNativeMemory(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    jlong allocated = 0;
    if (g_memory_tracing) {
        allocated = (jlong)aws_mem_tracer_bytes(aws_jni_get_allocator());
    }
    return allocated;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_dumpNativeMemory(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    if (g_memory_tracing > 1) {
        aws_mem_tracer_dump(aws_jni_get_allocator());
    }
}

jstring aws_jni_string_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data) {
    struct aws_string *string = aws_string_new_from_array(aws_jni_get_allocator(), native_data->ptr, native_data->len);
    AWS_FATAL_ASSERT(string != NULL);

    jstring java_string = (*env)->NewStringUTF(env, aws_string_c_str(string));
    aws_string_destroy(string);

    return java_string;
}

jstring aws_jni_string_from_string(JNIEnv *env, const struct aws_string *string) {
    AWS_FATAL_ASSERT(string != NULL);

    jstring java_string = (*env)->NewStringUTF(env, aws_string_c_str(string));

    return java_string;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CrtResource_waitForGlobalResourceDestruction(
    JNIEnv *env,
    jclass jni_crt_resource_class,
    jint timeout_in_seconds) {
    (void)env;
    (void)jni_crt_resource_class;

    aws_thread_set_managed_join_timeout_ns(
        aws_timestamp_convert(timeout_in_seconds, AWS_TIMESTAMP_SECS, AWS_TIMESTAMP_NANOS, NULL));
    aws_thread_join_all_managed();

    if (g_memory_tracing) {
        AWS_LOGF_DEBUG(
            AWS_LS_COMMON_GENERAL,
            "At shutdown, %u bytes remaining",
            (uint32_t)aws_mem_tracer_bytes(aws_jni_get_allocator()));
        if (g_memory_tracing > 1) {
            aws_mem_tracer_dump(aws_jni_get_allocator());
        }
    }
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_nativeCheckJniExceptionContract(
    JNIEnv *env,
    jclass jni_crt_class,
    jboolean clear_exception) {

    (*env)->CallStaticVoidMethod(env, jni_crt_class, crt_properties.test_jni_exception_method_id, true);

    if (clear_exception) {
        (*env)->ExceptionClear(env);
        (*env)->CallStaticVoidMethod(env, jni_crt_class, crt_properties.test_jni_exception_method_id, false);
    } else {
        (*env)->ExceptionCheck(env);
    }
}

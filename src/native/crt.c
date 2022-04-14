/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <aws/auth/auth.h>
#include <aws/common/allocator.h>
#include <aws/common/atomics.h>
#include <aws/common/clock.h>
#include <aws/common/common.h>
#include <aws/common/logging.h>
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

#include <aws/common/hash_table.h>

#include "crt.h"
#include "java_class_ids.h"
#include "logging.h"

/* 0 = off, 1 = bytes, 2 = stack traces, see aws_mem_trace_level */
int g_memory_tracing = 2;
static struct aws_allocator *s_init_allocator(void) {
    struct aws_allocator *sba_allocator = aws_small_block_allocator_new(aws_default_allocator(), true);
    if (g_memory_tracing) {
        sba_allocator = aws_mem_tracer_new(sba_allocator, NULL, (enum aws_mem_trace_level)g_memory_tracing, 8);
    }
    return sba_allocator;
}

static struct aws_allocator *s_allocator = NULL;
struct aws_allocator *aws_jni_get_allocator() {
    if (AWS_UNLIKELY(s_allocator == NULL)) {
        s_allocator = s_init_allocator();
    }
    return s_allocator;
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
    (*env)->ReleaseStringUTFChars(env, str, (const char *)cur.ptr);
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray_acquire(JNIEnv *env, jbyteArray array) {
    size_t len = (*env)->GetArrayLength(env, array);
    jbyte *bytes = (*env)->GetByteArrayElements(env, array, NULL);
    return aws_byte_cursor_from_array(bytes, len);
}

void aws_jni_byte_cursor_from_jbyteArray_release(JNIEnv *env, jbyteArray array, struct aws_byte_cursor cur) {
    (*env)->ReleaseByteArrayElements(env, array, (jbyte *)cur.ptr, JNI_ABORT);
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

void s_detach_jvm_from_thread(void *user_data) {
    AWS_LOGF_DEBUG(AWS_LS_COMMON_GENERAL, "s_detach_jvm_from_thread invoked");
    JavaVM *jvm = user_data;
    (*jvm)->DetachCurrentThread(jvm);
}

JNIEnv *aws_jni_get_thread_env(JavaVM *jvm) {
#ifdef ANDROID
    JNIEnv *env = NULL;
#else
    void *env = NULL;
#endif
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        AWS_LOGF_DEBUG(AWS_LS_COMMON_GENERAL, "aws_jni_get_thread_env returned detached, attaching");
#ifdef ANDROID
        jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, &env, NULL);
#else
        jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)&env, NULL);
#endif
        (void)result;
        AWS_FATAL_ASSERT(result == JNI_OK);
        /* This should only happen in event loop threads, the JVM main thread attachment is
         * managed by the JVM, so we only need to clean up event loop thread attachments */
        AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_thread_current_at_exit(s_detach_jvm_from_thread, (void *)jvm));
    }

    return env;
}

static void s_jni_atexit_common(void) {
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
}

static void s_jni_atexit_strict(void) {
    AWS_LOGF_DEBUG(AWS_LS_COMMON_GENERAL, "s_jni_atexit_strict invoked");
    s_jni_atexit_common();

    if (g_memory_tracing) {
        struct aws_allocator *trace_allocator = aws_jni_get_allocator();
        aws_mem_tracer_destroy(trace_allocator);
        trace_allocator = NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_small_block_allocator_destroy(allocator);
    allocator = NULL;
    s_allocator = NULL;
}

#define DEFAULT_MANAGED_SHUTDOWN_WAIT_IN_SECONDS 1

static void s_jni_atexit_gentle(void) {
    AWS_LOGF_DEBUG(AWS_LS_COMMON_GENERAL, "s_jni_atexit_gentle invoked");

    /* If not doing strict shutdown, wait only a short time before shutting down */
    aws_thread_set_managed_join_timeout_ns(
        aws_timestamp_convert(DEFAULT_MANAGED_SHUTDOWN_WAIT_IN_SECONDS, AWS_TIMESTAMP_SECS, AWS_TIMESTAMP_NANOS, NULL));

    if (aws_thread_join_all_managed() == AWS_OP_SUCCESS) {
        /* a successful managed join means it should be safe to do a full, strict clean up */
        s_jni_atexit_common();
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

static void (*jni_atexit)(void) = s_jni_atexit_gentle;
void jni_on_unload(void) {
    jni_atexit();
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

    cache_java_class_ids(env);

    if (jni_strict_shutdown) {
        jni_atexit = s_jni_atexit_strict;
    }
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
        //allocated = (jlong)aws_mem_tracer_bytes(aws_jni_get_allocator());
        if (s_allocator != NULL) {
            allocated = (jlong)aws_mem_tracer_bytes(s_allocator); // BUMP 11
        }
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
    if (string == NULL) {
        return NULL;
    }

    jstring java_string = (*env)->NewStringUTF(env, aws_string_c_str(string));
    aws_string_destroy(string);

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

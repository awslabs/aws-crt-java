/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

#include <aws/auth/auth.h>
#include <aws/common/atomics.h>
#include <aws/common/common.h>
#include <aws/common/string.h>
#include <aws/common/thread.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/io/io.h>
#include <aws/io/logging.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/mqtt/mqtt.h>

#include <stdio.h>

#include "async_callback.h"
#include "crt.h"
#include "logging.h"

/* 0 = off, 1 = bytes, 2 = stack traces, see aws_mem_trace_level */
static int s_memory_tracing = 0;
static struct aws_allocator *s_init_allocator() {
    if (s_memory_tracing) {
        struct aws_allocator *allocator = aws_default_allocator();
        allocator = aws_mem_tracer_new(allocator, NULL, (enum aws_mem_trace_level)s_memory_tracing, 8);
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

void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    char buf[1024];
    vsnprintf(buf, sizeof(buf), msg, args);
    va_end(args);

    char exception[1280];
    snprintf(exception, sizeof(exception), "%s (aws_last_error: %s)", buf, aws_error_str(aws_last_error()));
    jclass runtime_exception = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtRuntimeException");
    (*env)->ThrowNew(env, runtime_exception, exception);
}

/* methods of Java's ByteBuffer Class */
static struct {
    jclass cls;
    jmethodID get_capacity; /* The total number of bytes in the internal byte array. Stays constant. */
    jmethodID get_limit;    /* The max allowed read/write position of the Buffer. limit must be <= capacity. */
    jmethodID set_limit;
    jmethodID get_position; /* The current read/write position of the Buffer. position must be <= limit */
    jmethodID set_position;
    jmethodID get_remaining; /* Remaining number of bytes before the limit is reached. Equal to (limit - position). */
    jmethodID wrap;          /* Creates a new ByteBuffer Object from a Java byte[]. */
} s_java_byte_buffer = {0};

void s_cache_java_byte_buffer(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/nio/ByteBuffer");
    AWS_FATAL_ASSERT(cls);

    // FindClass() returns local JNI references that become eligible for GC once this native method returns to Java.
    // Call NewGlobalRef() so that this class reference doesn't get Garbage collected.
    s_java_byte_buffer.cls = (*env)->NewGlobalRef(env, cls);

    s_java_byte_buffer.get_capacity = (*env)->GetMethodID(env, cls, "capacity", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_capacity);

    s_java_byte_buffer.get_limit = (*env)->GetMethodID(env, cls, "limit", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_limit);

    s_java_byte_buffer.set_limit = (*env)->GetMethodID(env, cls, "limit", "(I)Ljava/nio/Buffer;");
    AWS_FATAL_ASSERT(s_java_byte_buffer.set_limit);

    s_java_byte_buffer.get_position = (*env)->GetMethodID(env, cls, "position", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_position);

    s_java_byte_buffer.set_position = (*env)->GetMethodID(env, cls, "position", "(I)Ljava/nio/Buffer;");
    AWS_FATAL_ASSERT(s_java_byte_buffer.set_position);

    s_java_byte_buffer.get_remaining = (*env)->GetMethodID(env, cls, "remaining", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_remaining);

    s_java_byte_buffer.wrap = (*env)->GetStaticMethodID(env, cls, "wrap", "([B)Ljava/nio/ByteBuffer;");
    AWS_FATAL_ASSERT(s_java_byte_buffer.wrap);
}

jbyteArray aws_java_byte_array_new(JNIEnv *env, size_t size) {
    jbyteArray jArray = (*env)->NewByteArray(env, (jsize)size);
    return jArray;
}

bool aws_copy_native_array_to_java_byte_array(JNIEnv *env, jbyteArray dst, uint8_t *src, size_t amount) {
    (*env)->SetByteArrayRegion(env, dst, 0, (jsize)amount, (jbyte *)src);
    return (*env)->ExceptionCheck(env);
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
    jint position = (*env)->CallIntMethod(env, java_byte_buffer, s_java_byte_buffer.get_position);
    return ((*env)->ExceptionCheck(env)) ? -1 : (int)position;
}

/**
 * Set the Buffer Position (the next element to read/write)
 */
void aws_jni_byte_buffer_set_position(JNIEnv *env, jobject jByteBuf, jint position) {
    jobject val = (*env)->CallObjectMethod(env, jByteBuf, s_java_byte_buffer.set_position, position);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    (*env)->DeleteLocalRef(env, val);
}

/**
 * Set the Buffer Limit (the max allowed element to read/write)
 */
void aws_jni_byte_buffer_set_limit(JNIEnv *env, jobject jByteBuf, jint limit) {
    jobject val = (*env)->CallObjectMethod(env, jByteBuf, s_java_byte_buffer.set_limit, limit);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
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
    return aws_byte_cursor_from_array(
        (*env)->GetStringUTFChars(env, str, NULL), (size_t)(*env)->GetStringUTFLength(env, str));
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
    struct aws_string *result = aws_string_new_from_c_str(allocator, str_chars);
    (*env)->ReleaseStringUTFChars(env, str, str_chars);
    return result;
}

void s_detach_jvm_from_thread(void *user_data) {
    JavaVM *jvm = user_data;
    (*jvm)->DetachCurrentThread(jvm);
}

JNIEnv *aws_jni_get_thread_env(JavaVM *jvm) {
    JNIEnv *env = NULL;
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)&env, NULL);
        (void)result;
        AWS_FATAL_ASSERT(result == JNI_OK);
        /* This should only happen in event loop threads, the JVM main thread attachment is
         * managed by the JVM, so we only need to clean up event loop thread attachments */
        AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_thread_current_at_exit(s_detach_jvm_from_thread, (void *)jvm));
    }

    return env;
}

#if defined(_MSC_VER)
#    pragma warning(push)
#    pragma warning(disable : 4210) /* non-standard extension used: function given file scope */
#endif
static void s_cache_jni_classes(JNIEnv *env) {
    extern void s_cache_mqtt_connection(JNIEnv *);
    extern void s_cache_message_handler(JNIEnv *);
    extern void s_cache_mqtt_exception(JNIEnv *);
    extern void s_cache_http_conn_manager(JNIEnv *);
    extern void s_cache_http_response_stream_handler(JNIEnv *);
    extern void s_cache_http_request_body_stream(JNIEnv *);
    extern void s_cache_http_header(JNIEnv *);
    extern void s_cache_http_stream(JNIEnv *);
    extern void s_cache_event_loop_group(JNIEnv *);
    extern void s_cache_credentials_ids(JNIEnv *);

    s_cache_java_byte_buffer(env);
    s_cache_mqtt_connection(env);
    s_cache_async_callback(env);
    s_cache_message_handler(env);
    s_cache_mqtt_exception(env);
    s_cache_http_conn_manager(env);
    s_cache_http_response_stream_handler(env);
    s_cache_http_request_body_stream(env);
    s_cache_http_header(env);
    s_cache_http_stream(env);
    s_cache_event_loop_group(env);
    s_cache_credentials_ids(env);
}
#if defined(_MSC_VER)
#    pragma warning(pop)
#endif

static void s_jni_atexit(void) {
    aws_auth_library_clean_up();
    aws_http_library_clean_up();
    aws_mqtt_library_clean_up();
    aws_jni_cleanup_logging();

    if (s_memory_tracing) {
        struct aws_allocator *tracer_allocator = aws_jni_get_allocator();
        aws_mem_tracer_dump(tracer_allocator);
        aws_mem_tracer_destroy(tracer_allocator);
    }
}

/* Called as the entry point, immediately after the shared lib is loaded the first time by JNI */
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_awsCrtInit(JNIEnv *env, jclass jni_crt_class, jint jni_memtrace) {
    (void)jni_crt_class;

    s_memory_tracing = jni_memtrace;
#if !defined(ALLOC_TRACE_AVAILABLE)
    s_memory_tracing = (s_memory_tracing > 1) ? 1 : s_memory_tracing;
#endif

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mqtt_library_init(allocator);
    aws_http_library_init(allocator);
    aws_auth_library_init(allocator);

    s_cache_jni_classes(env);

    atexit(s_jni_atexit);
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_CRT_awsErrorString(JNIEnv *env, jclass jni_crt_class, jint error_code) {
    (void)jni_crt_class;
    const char *error_msg = aws_error_str(error_code);
    return (*env)->NewStringUTF(env, error_msg);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_CRT_awsNativeMemory(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    jlong allocated = 0;
    if (s_memory_tracing) {
        allocated = (jlong)aws_mem_tracer_bytes(aws_jni_get_allocator());
    }
    return allocated;
}

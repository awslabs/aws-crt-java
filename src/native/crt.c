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

#include <aws/common/common.h>
#include <aws/common/string.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/io/io.h>
#include <aws/io/logging.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/mqtt/mqtt.h>

#include <stdio.h>

#include "async_callback.h"
#include "crt.h"

struct aws_allocator *aws_jni_get_allocator() {
    return aws_default_allocator();
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
    assert(cls);

    // FindClass() returns local JNI references that become eligible for GC once this native method returns to Java.
    // Call NewGlobalRef() so that this class reference doesn't get Garbage collected.
    s_java_byte_buffer.cls = (*env)->NewGlobalRef(env, cls);

    s_java_byte_buffer.get_capacity = (*env)->GetMethodID(env, cls, "capacity", "()I");
    assert(s_java_byte_buffer.get_capacity);

    s_java_byte_buffer.get_limit = (*env)->GetMethodID(env, cls, "limit", "()I");
    assert(s_java_byte_buffer.get_limit);

    s_java_byte_buffer.set_limit = (*env)->GetMethodID(env, cls, "limit", "(I)Ljava/nio/Buffer;");
    assert(s_java_byte_buffer.set_limit);

    s_java_byte_buffer.get_position = (*env)->GetMethodID(env, cls, "position", "()I");
    assert(s_java_byte_buffer.get_position);

    s_java_byte_buffer.set_position = (*env)->GetMethodID(env, cls, "position", "(I)Ljava/nio/Buffer;");
    assert(s_java_byte_buffer.set_position);

    s_java_byte_buffer.get_remaining = (*env)->GetMethodID(env, cls, "remaining", "()I");
    assert(s_java_byte_buffer.get_remaining);

    s_java_byte_buffer.wrap = (*env)->GetStaticMethodID(env, cls, "wrap", "([B)Ljava/nio/ByteBuffer;");
    assert(s_java_byte_buffer.wrap);
}

jbyteArray aws_java_byte_array_new(JNIEnv *env, size_t size) {
    jbyteArray jArray = (*env)->NewByteArray(env, (jsize)size);
    return jArray;
}

void aws_copy_java_byte_array_to_native_array(JNIEnv *env, jbyteArray src, uint8_t *dst, size_t amount) {
    (*env)->GetByteArrayRegion(env, src, 0, amount, (jbyte *)dst);
}

void aws_copy_native_array_to_java_byte_array(JNIEnv *env, jbyteArray dst, uint8_t *src, size_t amount) {
    (*env)->SetByteArrayRegion(env, dst, 0, amount, (jbyte *)src);
}

jobject aws_java_byte_array_to_java_byte_buffer(JNIEnv *env, jbyteArray jArray) {
    jobject jByteBuffer = (*env)->CallStaticObjectMethod(env, s_java_byte_buffer.cls, s_java_byte_buffer.wrap, jArray);
    return jByteBuffer;
}

/**
 * Converts a Java byte[] to a Native aws_byte_cursor
 */
struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray(JNIEnv *env, jbyteArray array) {

    jboolean isCopy;
    jbyte *data = (*env)->GetByteArrayElements(env, array, &isCopy);
    jsize len = (*env)->GetArrayLength(env, array);
    return aws_byte_cursor_from_array((const uint8_t *)data, (size_t)len);
}

/**
 * Converts a Native aws_byte_cursor to a Java byte[]
 */
jbyteArray aws_jni_byte_array_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data) {
    jbyteArray jArray = aws_java_byte_array_new(env, native_data->len);
    aws_copy_native_array_to_java_byte_array(env, jArray, native_data->ptr, native_data->len);
    return jArray;
}

/**
 * Converts a Native aws_byte_cursor to a Java ByteBuffer Object
 */
jobject aws_jni_byte_buffer_copy_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data) {
    assert(env);
    jbyteArray jArray = aws_jni_byte_array_from_cursor(env, native_data);
    jobject jByteBuffer = aws_java_byte_array_to_java_byte_buffer(env, jArray);

    return jByteBuffer;
}

/**
 * Converts a Native aws_byte_cursor to a Java DirectByteBuffer
 */
jobject aws_jni_direct_byte_buffer_from_byte_buf(JNIEnv *env, const struct aws_byte_buf *dst) {
    jobject jByteBuf = (*env)->NewDirectByteBuffer(env, (void *)dst->buffer, (jlong)dst->capacity);

    // Set the Buffer Limit (the max allowed element to read/write)
    (*env)->CallObjectMethod(env, jByteBuf, s_java_byte_buffer.set_limit, (jint)dst->capacity);

    // Set the Buffer Position (the next element to read/write)
    (*env)->CallObjectMethod(env, jByteBuf, s_java_byte_buffer.set_position, (jint)dst->len);

    return jByteBuf;
}

/**
 * Returns the read/write position of a Java ByteBuffer
 */
int aws_jni_byte_buffer_get_position(JNIEnv *env, jobject java_byte_buffer) {
    jint position = (*env)->CallIntMethod(env, java_byte_buffer, s_java_byte_buffer.get_position);
    return (int)position;
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jstring(JNIEnv *env, jstring str) {
    return aws_byte_cursor_from_array(
        (*env)->GetStringUTFChars(env, str, NULL), (size_t)(*env)->GetStringUTFLength(env, str));
}

struct aws_byte_cursor aws_jni_byte_cursor_from_direct_byte_buffer(JNIEnv *env, jobject byte_buffer) {
    jlong payload_size = (*env)->GetDirectBufferCapacity(env, byte_buffer);
    if (payload_size == -1) {
        aws_jni_throw_runtime_exception(
            env, "MqttConnection.mqtt_publish: Unable to get capacity of payload ByteBuffer");
        return aws_byte_cursor_from_array(NULL, 0);
    }
    jbyte *payload_data = (*env)->GetDirectBufferAddress(env, byte_buffer);
    if (!payload_data) {
        aws_jni_throw_runtime_exception(
            env, "MqttConnection.mqtt_publish: Unable to get buffer from payload ByteBuffer");
        return aws_byte_cursor_from_array(NULL, 0);
    }
    return aws_byte_cursor_from_array((const uint8_t *)payload_data, (size_t)payload_size);
}

struct aws_string *aws_jni_new_string_from_jstring(JNIEnv *env, jstring str) {
    struct aws_allocator *allocator = aws_jni_get_allocator();
    return aws_string_new_from_c_str(allocator, (*env)->GetStringUTFChars(env, str, NULL));
}

JNIEnv *aws_jni_get_thread_env(JavaVM *jvm) {
    JNIEnv *env = NULL;
    jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)&env, NULL);
    (void)result;
    assert(result == JNI_OK);
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
    extern void s_cache_http_connection(JNIEnv *);
    extern void s_cache_crt_http_stream_handler(JNIEnv *);
    extern void s_cache_http_header(JNIEnv *);
    extern void s_cache_http_stream(JNIEnv *);
    s_cache_java_byte_buffer(env);
    s_cache_mqtt_connection(env);
    s_cache_async_callback(env);
    s_cache_message_handler(env);
    s_cache_mqtt_exception(env);
    s_cache_http_connection(env);
    s_cache_crt_http_stream_handler(env);
    s_cache_http_header(env);
    s_cache_http_stream(env);
}
#if defined(_MSC_VER)
#    pragma warning(pop)
#endif

// static struct aws_logger s_logger;

static void s_jni_atexit(void) {
    // aws_logger_clean_up(&s_logger);
    aws_tls_clean_up_static_state();
    aws_http_library_clean_up();
}

/* Called as the entry point, immediately after the shared lib is loaded the first time by JNI */
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_awsCrtInit(JNIEnv *env, jclass jni_crt_class) {
    (void)jni_crt_class;
    aws_load_error_strings();
    aws_io_load_error_strings();
    aws_mqtt_load_error_strings();

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_tls_init_static_state(allocator);
    aws_http_library_init(allocator);

    // struct aws_logger_standard_options log_options = {.level = AWS_LL_TRACE, .file = stderr};
    // if (aws_logger_init_standard(&s_logger, allocator, &log_options)) {
    //     aws_jni_throw_runtime_exception(env, "Failed to initialize logging");
    //     return;
    // }
    // aws_logger_set(&s_logger);

    s_cache_jni_classes(env);

    atexit(s_jni_atexit);
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_CRT_awsErrorString(JNIEnv *env, jclass jni_crt_class, jint error_code) {
    (void)jni_crt_class;
    const char *error_msg = aws_error_str(error_code);
    return (*env)->NewStringUTF(env, error_msg);
}

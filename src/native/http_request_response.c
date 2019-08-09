/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

#include <crt.h>
#include <jni.h>

#include <aws/common/mutex.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/http/request_response.h>
#include <aws/io/logging.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'jni_tls_ctx_options *' */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

static struct {
    jclass header_class;
    jmethodID constructor;
    jfieldID name;
    jfieldID value;
} s_http_header;

void s_cache_http_header(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpHeader");
    AWS_FATAL_ASSERT(cls);
    s_http_header.header_class = cls;

    s_http_header.constructor = (*env)->GetMethodID(env, cls, "<init>", "()V");
    AWS_FATAL_ASSERT(s_http_header.constructor);

    s_http_header.name = (*env)->GetFieldID(env, cls, "name", "[B");
    AWS_FATAL_ASSERT(s_http_header.name);

    s_http_header.value = (*env)->GetFieldID(env, cls, "value", "[B");
    AWS_FATAL_ASSERT(s_http_header.value);

    // FindClass() returns local JNI references that become eligible for GC once this native method returns to Java.
    // Call NewGlobalRef() so that this class reference doesn't get Garbage collected.
    s_http_header.header_class = (*env)->NewGlobalRef(env, s_http_header.header_class);
}

static struct {
    jclass stream_class;
    jmethodID constructor;
    jmethodID close;
} s_http_stream_handler;

void s_cache_http_stream(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpStream");
    AWS_FATAL_ASSERT(cls);
    s_http_stream_handler.stream_class = cls;

    // FindClass() returns local JNI references that become eligible for GC once this native method returns to Java.
    // Call NewGlobalRef() so that this class reference doesn't get Garbage collected.
    s_http_stream_handler.stream_class = (*env)->NewGlobalRef(env, s_http_stream_handler.stream_class);

    s_http_stream_handler.constructor = (*env)->GetMethodID(env, cls, "<init>", "(J)V");
    AWS_FATAL_ASSERT(s_http_stream_handler.constructor);

    s_http_stream_handler.close = (*env)->GetMethodID(env, cls, "close", "()V");
    AWS_FATAL_ASSERT(s_http_stream_handler.close);
}

static jobject s_java_http_stream_from_native_new(JNIEnv *env, struct aws_http_stream *stream) {
    jlong jni_native_ptr = (jlong)stream;
    AWS_FATAL_ASSERT(jni_native_ptr);
    jobject jHttpStream =
        (*env)->NewObject(env, s_http_stream_handler.stream_class, s_http_stream_handler.constructor, jni_native_ptr);

    if ((*env)->ExceptionCheck(env) || jHttpStream == NULL) {
        // Close the Connection if the Java Callback throws an Exception
        aws_http_connection_close(aws_http_stream_get_connection(stream));
        return NULL;
    }

    return jHttpStream;
}

static void s_java_http_stream_from_native_delete(JNIEnv *env, jobject jHttpStream) {
    // Delete our reference to the HttpStream Object from the JVM.
    (*env)->DeleteGlobalRef(env, jHttpStream);
}

/*******************************************************************************
 * http_stream_callback_data - carries around data needed by the various http request
 * callbacks.
 ******************************************************************************/
struct http_stream_callback_data {
    struct aws_mutex lock;
    JavaVM *jvm;
    struct aws_byte_buf native_body_buf;
    jobject java_crt_http_callback_handler;
    jobject java_http_stream;

    /* Direct Byte Buffer that points to native_body_buf struct above*/
    jobject java_body_buf;
};

static struct http_stream_callback_data *http_stream_callback_alloc(
    JNIEnv *env,
    jint body_buf_size,
    jobject java_callback_handler) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct http_stream_callback_data *callback = aws_mem_calloc(allocator, 1, sizeof(struct http_stream_callback_data));
    if (!callback) {
        /* caller will throw when they get a null */
        return NULL;
    }

    // GetJavaVM() reference doesn't need a NewGlobalRef() call since it's global by default
    jint jvmresult = (*env)->GetJavaVM(env, &callback->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_mutex_init(&callback->lock);

    /* Pre-allocate a Native buffer and Java Direct ByteBuffer so that we don't create a new Java Object for each IO
     * operation. Otherwise, we'll create garbage faster than Java's GC can clean up. */
    callback->native_body_buf.allocator = allocator;
    callback->native_body_buf.buffer = aws_mem_calloc(allocator, 1, body_buf_size);
    callback->native_body_buf.capacity = (size_t)body_buf_size;
    callback->native_body_buf.len = 0;

    /* Create a Java DirectByteBuffer that points to native_body_buf */
    callback->java_body_buf = aws_jni_direct_byte_buffer_from_byte_buf(env, &callback->native_body_buf);

    /* Tell the JVM we have a reference to both the Java ByteBuffer and the callback handler (so they're not GC'd) */
    callback->java_body_buf = (*env)->NewGlobalRef(env, callback->java_body_buf);
    callback->java_crt_http_callback_handler = (*env)->NewGlobalRef(env, java_callback_handler);

    return callback;
}

static void http_stream_callback_release(JNIEnv *env, struct http_stream_callback_data *callback) {

    s_java_http_stream_from_native_delete(env, callback->java_http_stream);

    // Mark our Callback Java Objects as eligible for Garbage Collection
    (*env)->DeleteGlobalRef(env, callback->java_body_buf);
    (*env)->DeleteGlobalRef(env, callback->java_crt_http_callback_handler);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback->native_body_buf.buffer);
    aws_mem_release(allocator, callback);
}

/* CrtHttpStreamHandler Java Methods */
static struct {
    jmethodID onResponseHeaders;
    jmethodID onResponseHeadersDone;
    jmethodID onResponseBody;
    jmethodID onResponseComplete;
    jmethodID sendOutgoingBody;
} s_crt_http_stream_handler;

void s_cache_crt_http_stream_handler(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/CrtHttpStreamHandler");
    AWS_FATAL_ASSERT(cls);
    s_crt_http_stream_handler.onResponseHeaders = (*env)->GetMethodID(
        env,
        cls,
        "onResponseHeaders",
        "(Lsoftware/amazon/awssdk/crt/http/HttpStream;I[Lsoftware/amazon/awssdk/crt/http/HttpHeader;)V");

    AWS_FATAL_ASSERT(s_crt_http_stream_handler.onResponseHeaders);

    s_crt_http_stream_handler.onResponseHeadersDone =
        (*env)->GetMethodID(env, cls, "onResponseHeadersDone", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;Z)V");
    AWS_FATAL_ASSERT(s_crt_http_stream_handler.onResponseHeadersDone);

    s_crt_http_stream_handler.onResponseBody = (*env)->GetMethodID(
        env, cls, "onResponseBody", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;Ljava/nio/ByteBuffer;)I");
    AWS_FATAL_ASSERT(s_crt_http_stream_handler.onResponseBody);

    s_crt_http_stream_handler.onResponseComplete =
        (*env)->GetMethodID(env, cls, "onResponseComplete", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;I)V");
    AWS_FATAL_ASSERT(s_crt_http_stream_handler.onResponseComplete);

    s_crt_http_stream_handler.sendOutgoingBody = (*env)->GetMethodID(
        env, cls, "sendRequestBody", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;Ljava/nio/ByteBuffer;)Z");
    AWS_FATAL_ASSERT(s_crt_http_stream_handler.sendOutgoingBody);
}

static jobjectArray s_java_headers_array_from_native(
    struct http_stream_callback_data *callback,
    const struct aws_http_header *header_array,
    size_t num_headers) {

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    AWS_FATAL_ASSERT(s_http_header.header_class);
    AWS_FATAL_ASSERT(s_http_header.constructor);
    AWS_FATAL_ASSERT(s_http_header.name);
    AWS_FATAL_ASSERT(s_http_header.value);

    jobjectArray jArray = (*env)->NewObjectArray(env, (jsize)num_headers, s_http_header.header_class, NULL);

    for (size_t i = 0; i < num_headers; i++) {
        jobject jHeader = (*env)->NewObject(env, s_http_header.header_class, s_http_header.constructor);

        jbyteArray actual_name = aws_jni_byte_array_from_cursor(env, &(header_array[i].name));
        jbyteArray actual_value = aws_jni_byte_array_from_cursor(env, &(header_array[i].value));

        // Overwrite with actual values
        (*env)->SetObjectField(env, jHeader, s_http_header.name, actual_name);
        (*env)->SetObjectField(env, jHeader, s_http_header.value, actual_value);
        (*env)->SetObjectArrayElement(env, jArray, (jsize)i, jHeader);
    }

    return jArray;
}

static void aws_http_close_connection_with_reason(struct aws_http_stream *stream, char *reason) {

    struct aws_http_connection *conn = aws_http_stream_get_connection(stream);

    AWS_LOGF_ERROR(
        AWS_LS_HTTP_CONNECTION,
        "Aborting Http Connection. conn=%p, stream=%p, Reason: %s",
        (void *)conn,
        (void *)stream,
        reason);

    aws_http_connection_close(conn);
}

static void s_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;
    // Other threads might edit the callback struct, so ensure that we gain a lock on it
    aws_mutex_lock(&callback->lock);

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);
    jobjectArray jHeaders = s_java_headers_array_from_native(user_data, header_array, num_headers);

    int resp_status = -1;
    int err_code = aws_http_stream_get_incoming_response_status(stream, &resp_status);

    if (err_code != AWS_OP_SUCCESS) {
        // Close the connection if we can't get the response status
        aws_mutex_unlock(&callback->lock);
        aws_http_connection_close(aws_http_stream_get_connection(stream));
        return;
    }

    (*env)->CallVoidMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.onResponseHeaders,
        callback->java_http_stream,
        resp_status,
        jHeaders);

    aws_mutex_unlock(&callback->lock);

    if ((*env)->ExceptionCheck(env)) {
        // Close the Connection if the Java Callback throws an Exception
        aws_http_connection_close(aws_http_stream_get_connection(stream));
        return;
    }
}

static void s_on_incoming_header_block_done_fn(struct aws_http_stream *stream, bool has_body, void *user_data) {
    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    // Other threads might edit the callback struct, so ensure that we gain a lock on it
    aws_mutex_lock(&callback->lock);

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    jboolean jHasBody = has_body;
    (*env)->CallVoidMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.onResponseHeadersDone,
        callback->java_http_stream,
        jHasBody);

    aws_mutex_unlock(&callback->lock);

    if ((*env)->ExceptionCheck(env)) {
        // Close the Connection if the Java Callback throws an Exception
        aws_http_connection_close(aws_http_stream_get_connection(stream));
        return;
    }
}

/**
 * Copies src to dest. If dest is too small, src->ptr will be incremented to point to the next byte that wasn't copied,
 * and src->len will be decremented to the number of bytes remaining.
 */
static void aws_byte_buf_transfer_best_effort(struct aws_byte_buf *dst, struct aws_byte_cursor *src) {
    size_t dst_remaining = (dst->capacity - dst->len);
    size_t amt_to_copy = (dst_remaining < src->len) ? dst_remaining : src->len;

    memcpy(dst->buffer + dst->len, src->ptr, amt_to_copy);

    dst->len += amt_to_copy;
    src->ptr += amt_to_copy;
    src->len -= amt_to_copy;
}

static int aws_http_resp_body_publish_to_java(
    struct aws_http_stream *stream,
    struct http_stream_callback_data *callback,
    size_t *out_window_update_size) {

    // Return early if there's nothing to publish
    if (callback->native_body_buf.len == 0) {
        return AWS_OP_SUCCESS;
    }

    // Set read start position to zero
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);
    aws_jni_byte_buffer_set_position(env, callback->java_body_buf, 0);
    aws_jni_byte_buffer_set_limit(env, callback->java_body_buf, (jint)callback->native_body_buf.len);

    jint window_increment = (*env)->CallIntMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.onResponseBody,
        callback->java_http_stream,
        callback->java_body_buf);

    if ((*env)->ExceptionCheck(env)) {
        // Close the Connection if the Java Callback throws an Exception
        aws_http_close_connection_with_reason(stream, "Received Exception from onResponseBody");
        return AWS_OP_ERR;
    }

    if (window_increment < 0) {
        aws_http_close_connection_with_reason(stream, "Window Increment from onResponseBody < 0");
        return AWS_OP_ERR;
    }

    // We can check the ByteBuffer read position to verify that the user callback actually read all the data
    // they claimed to be able to read.
    size_t read_position = aws_jni_byte_buffer_get_position(env, callback->java_body_buf);
    if (read_position != callback->native_body_buf.len) {
        aws_http_close_connection_with_reason(stream, "ByteBuffer.remaining() > 0 after onResponseBody");
        return AWS_OP_ERR;
    }

    // Publish to Java succeeded, set resp body buffer position to zero
    callback->native_body_buf.len = 0;
    *out_window_update_size = window_increment;
    return AWS_OP_SUCCESS;
}

static void s_on_incoming_body_fn(
    struct aws_http_stream *stream,
    const struct aws_byte_cursor *data,
    /* NOLINTNEXTLINE(readability-non-const-parameter) */
    size_t *out_window_update_size,
    void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    // Other threads might edit the callback struct, so ensure that we gain a lock on it
    aws_mutex_lock(&callback->lock);

    struct aws_byte_cursor body_in_remaining = *data;
    size_t total_window_increment = 0;

    while (body_in_remaining.len > 0) {
        size_t curr_window_increment = 0;
        aws_byte_buf_transfer_best_effort(&callback->native_body_buf, &body_in_remaining);

        if (AWS_OP_SUCCESS != aws_http_resp_body_publish_to_java(stream, callback, &curr_window_increment)) {
            return;
        }

        total_window_increment += curr_window_increment;
    }

    aws_mutex_unlock(&callback->lock);
    *out_window_update_size = (size_t)total_window_increment;
}

static void s_on_stream_complete_fn(struct aws_http_stream *stream, int error_code, void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    // Other threads might edit the callback struct, so ensure that we gain a lock on it
    aws_mutex_lock(&callback->lock);

    size_t curr_window_increment = 0;
    int result = aws_http_resp_body_publish_to_java(stream, callback, &curr_window_increment);
    if (result != AWS_OP_SUCCESS) {
        return;
    }

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    jint jErrorCode = error_code;
    (*env)->CallVoidMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.onResponseComplete,
        callback->java_http_stream,
        jErrorCode);

    aws_mutex_unlock(&callback->lock);

    if ((*env)->ExceptionCheck(env)) {
        // Close the Connection if the Java Callback throws an Exception
        aws_http_connection_close(aws_http_stream_get_connection(stream));
        return;
    }

    http_stream_callback_release(env, callback);
}

enum aws_http_outgoing_body_state s_stream_outgoing_body_fn(
    struct aws_http_stream *stream,
    struct aws_byte_buf *dst,
    void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    // Other threads might edit the callback struct, so ensure that we gain a lock on it
    aws_mutex_lock(&callback->lock);

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    uint8_t *out = &(dst->buffer[dst->len]);
    size_t out_remaining = dst->capacity - dst->len;

    size_t buf_capacity = callback->native_body_buf.capacity;
    size_t request_size = (buf_capacity > out_remaining) ? out_remaining : buf_capacity;

    jobject jByteBuffer = callback->java_body_buf;

    aws_jni_byte_buffer_set_position(env, jByteBuffer, 0);
    aws_jni_byte_buffer_set_limit(env, jByteBuffer, request_size);

    jboolean isDone = (*env)->CallBooleanMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.sendOutgoingBody,
        callback->java_http_stream,
        jByteBuffer);

    if ((*env)->ExceptionCheck(env)) {
        // Close the Connection if the Java Callback throws an Exception
        aws_mutex_unlock(&callback->lock);
        aws_http_connection_close(aws_http_stream_get_connection(stream));
        return AWS_HTTP_OUTGOING_BODY_IN_PROGRESS;
    }

    size_t amt_written = aws_jni_byte_buffer_get_position(env, jByteBuffer);
    AWS_FATAL_ASSERT(amt_written <= out_remaining);

    memcpy(out, callback->native_body_buf.buffer, amt_written);
    dst->len += amt_written;

    aws_mutex_unlock(&callback->lock);

    if (isDone) {
        return AWS_HTTP_OUTGOING_BODY_DONE;
    }

    return AWS_HTTP_OUTGOING_BODY_IN_PROGRESS;
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_http_HttpConnection_httpConnectionMakeRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jint jni_resp_body_buf_size,
    jstring jni_method,
    jstring jni_uri,
    jobjectArray jni_headers,
    jobject jni_crt_http_callback_handler) {

    (void)jni_class;

    struct aws_http_connection *native_conn = (struct aws_http_connection *)jni_connection;

    if (!native_conn) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Invalid jni_connection");
        return (jobject)NULL;
    }

    if (!jni_crt_http_callback_handler) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Invalid jni_callback_handler");
        return (jobject)NULL;
    }

    struct http_stream_callback_data *callback_data =
        http_stream_callback_alloc(env, jni_resp_body_buf_size, jni_crt_http_callback_handler);

    if (!callback_data) {
        aws_jni_throw_runtime_exception(
            env, "HttpConnection.MakeRequest: Unable to allocate http_request_jni_async_callback");
        return (jobject)NULL;
    }

    // There's a Data Race between this thread writing to callback_data->java_http_stream and the EventLoop thread
    // reading callback_data->java_http_stream when calling the callbacks, add a lock so that both threads see a
    // consistent state.
    aws_mutex_lock(&callback_data->lock);

    struct aws_byte_cursor method = aws_jni_byte_cursor_from_jstring(env, jni_method);
    struct aws_byte_cursor uri = aws_jni_byte_cursor_from_jstring(env, jni_uri);
    jsize num_headers = (*env)->GetArrayLength(env, jni_headers);

    AWS_VARIABLE_LENGTH_ARRAY(struct aws_http_header, headers, num_headers);
    AWS_ZERO_ARRAY(headers);

    AWS_FATAL_ASSERT(s_http_header.name);
    AWS_FATAL_ASSERT(s_http_header.value);

    for (int i = 0; i < num_headers; i++) {
        jobject jHeader = (*env)->GetObjectArrayElement(env, jni_headers, i);
        jbyteArray jname = (*env)->GetObjectField(env, jHeader, s_http_header.name);
        jbyteArray jvalue = (*env)->GetObjectField(env, jHeader, s_http_header.value);

        headers[i].name = aws_jni_byte_cursor_from_jbyteArray(env, jname);
        headers[i].value = aws_jni_byte_cursor_from_jbyteArray(env, jvalue);
    }

    struct aws_http_request_options request_options = AWS_HTTP_REQUEST_OPTIONS_INIT;
    request_options.client_connection = native_conn;
    request_options.method = method;
    request_options.uri = uri;
    request_options.header_array = headers;
    request_options.num_headers = num_headers;

    // Set Callbacks
    request_options.on_response_headers = s_on_incoming_headers_fn;
    request_options.on_response_header_block_done = s_on_incoming_header_block_done_fn;
    request_options.on_response_body = s_on_incoming_body_fn;
    request_options.stream_outgoing_body = s_stream_outgoing_body_fn;
    request_options.on_complete = s_on_stream_complete_fn;
    request_options.user_data = callback_data;

    // This call schedules tasks on the Native Event loop thread to begin sending HttpRequest and receive the response.
    struct aws_http_stream *req = aws_http_stream_new_client_request(&request_options);

    if (req == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Unable to Execute Request");
        return (jobject)NULL;
    }

    jobject jHttpStream = s_java_http_stream_from_native_new(env, req);

    // Call NewGlobalRef() so that jHttpStream reference doesn't get Garbage collected can can be used from callbacks.
    jHttpStream = (*env)->NewGlobalRef(env, jHttpStream);

    callback_data->java_http_stream = jHttpStream;

    // Now that callback_data->java_http_stream has been written, the EventLoop thread may begin using this callback.
    aws_mutex_unlock(&callback_data->lock);

    return jHttpStream;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamRelease(JNIEnv *env, jclass jni_class, jlong jni_stream) {

    (void)jni_class;

    struct aws_http_stream *stream = (struct aws_http_stream *)jni_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }

    aws_http_stream_release(stream);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamGetResponseStatusCode(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_stream) {

    (void)jni_class;

    struct aws_http_stream *stream = (struct aws_http_stream *)jni_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return -1;
    }

    int status = -1;
    int err_code = aws_http_stream_get_incoming_response_status(stream, &status);

    if (err_code != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "Error Getting Response Status Code from HttpStream.");
        return -1;
    }

    return (jint)status;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamIncrementWindow(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_stream,
    jint window_update) {

    (void)jni_class;

    struct aws_http_stream *stream = (struct aws_http_stream *)jni_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }

    if (window_update < 0) {
        aws_jni_throw_runtime_exception(env, "Window Update is < 0");
        return;
    }

    aws_http_stream_update_window(stream, window_update);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

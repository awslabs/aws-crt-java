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
#include <aws/io/stream.h>

#include "crt_byte_buffer.h"

#if _MSC_VER
#    pragma warning(disable : 4204) /* non-constant aggregate initializer */
#endif

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

    s_http_stream_handler.constructor =
        (*env)->GetMethodID(env, cls, "<init>", "(Lsoftware/amazon/awssdk/crt/io/CrtByteBuffer;J)V");
    AWS_FATAL_ASSERT(s_http_stream_handler.constructor);

    s_http_stream_handler.close = (*env)->GetMethodID(env, cls, "close", "()V");
    AWS_FATAL_ASSERT(s_http_stream_handler.close);
}

static jobject s_java_http_stream_from_native_new(JNIEnv *env, jobject crtBuffer, struct aws_http_stream *stream) {
    jlong jni_native_ptr = (jlong)stream;
    AWS_ASSERT(jni_native_ptr);
    return (*env)->NewObject(
        env, s_http_stream_handler.stream_class, s_http_stream_handler.constructor, crtBuffer, jni_native_ptr);
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
    struct aws_mutex setup_lock;
    JavaVM *jvm;

    // TEMP: Until Java API changes to match "H1B" native HTTP API,
    // create aws_http_message and aws_input_stream under the hood.
    struct aws_http_message *native_request;
    struct aws_input_stream native_outgoing_body;
    bool native_outgoing_body_done;

    struct aws_byte_buf native_body_buf;
    jobject java_crt_http_callback_handler;
    jobject java_http_stream;

    /* Direct Byte Buffer that points to native_body_buf struct above*/
    jobject java_body_buf;
};

static int s_native_outgoing_body_read(struct aws_input_stream *input_stream, struct aws_byte_buf *dst);
static int s_native_outgoing_body_status(struct aws_input_stream *input_stream, struct aws_stream_status *status);

struct aws_input_stream_vtable s_native_outgoing_body_vtable = {
    .read = s_native_outgoing_body_read,
    .get_status = s_native_outgoing_body_status,
};

// If error occurs, A Java exception is thrown and NULL is returned.
static struct http_stream_callback_data *http_stream_callback_alloc(
    JNIEnv *env,
    jobject crtBuffer,
    jobject java_callback_handler) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct http_stream_callback_data *callback = aws_mem_calloc(allocator, 1, sizeof(struct http_stream_callback_data));
    if (!callback) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: failed");
        goto failed_callback_alloc;
    }

    // GetJavaVM() reference doesn't need a NewGlobalRef() call since it's global by default
    jint jvmresult = (*env)->GetJavaVM(env, &callback->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_mutex_init(&callback->setup_lock);

    callback->native_outgoing_body.vtable = &s_native_outgoing_body_vtable;
    callback->native_outgoing_body.impl = callback;

    callback->native_request = aws_http_message_new_request(allocator);
    if (!callback->native_request) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: failed");
        goto failed_request_new;
    }

    aws_http_message_set_body_stream(callback->native_request, &callback->native_outgoing_body);

    jobject java_direct_buf = aws_crt_byte_buffer_get_direct_buffer(env, crtBuffer);
    aws_jni_native_byte_buf_from_java_direct_byte_buf(env, java_direct_buf, &callback->native_body_buf);

    /* Tell the JVM we have a reference to both the Java ByteBuffer and the callback handler (so they're not GC'd) */
    callback->java_body_buf = (*env)->NewGlobalRef(env, java_direct_buf);
    if (!callback->java_body_buf) {
        /* Local ref to java_body_buf is cleaned up automatically */
        goto failed_java_body_buf_ref;
    }

    callback->java_crt_http_callback_handler = (*env)->NewGlobalRef(env, java_callback_handler);
    if (!callback->java_crt_http_callback_handler) {
        goto failed_callback_handler_ref;
    }

    return callback;

failed_callback_handler_ref:
    (*env)->DeleteGlobalRef(env, callback->java_body_buf);
failed_java_body_buf_ref:
    aws_http_message_destroy(callback->native_request);
failed_request_new:
    aws_mutex_clean_up(&callback->setup_lock);
    aws_mem_release(allocator, callback);
failed_callback_alloc:
    return NULL;
}

static void http_stream_callback_release(JNIEnv *env, struct http_stream_callback_data *callback) {

    if (callback->java_http_stream) {
        s_java_http_stream_from_native_delete(env, callback->java_http_stream);
    }

    // Mark our Callback Java Objects as eligible for Garbage Collection
    (*env)->DeleteGlobalRef(env, callback->java_body_buf);
    (*env)->DeleteGlobalRef(env, callback->java_crt_http_callback_handler);

    aws_byte_buf_clean_up(&callback->native_body_buf);
    aws_http_message_destroy(callback->native_request);
    aws_mutex_clean_up(&callback->setup_lock);
    aws_mem_release(aws_jni_get_allocator(), callback);
}

// Return whether the Java HttpStream object was successfully created.
static bool http_stream_callback_is_valid(struct http_stream_callback_data *callback) {
    // Lock to be sure that the setup attempt is finished
    aws_mutex_lock(&callback->setup_lock);
    bool is_setup = (callback->java_http_stream != NULL);
    aws_mutex_unlock(&callback->setup_lock);
    return is_setup;
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
        (*env)->GetMethodID(env, cls, "onResponseHeadersDone", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;I)V");
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

static int s_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data) {

    (void)block_type;
    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    /* All New Java Objects created through JNI have Thread-local references in the current Environment Frame, and
     * won't be eligible for GC until either DeleteLocalRef or PopLocalFrame is called.
     *
     * Capacity is multiple of 4 since we will need at minimum 4 Java Objects for a single Header:
     *   - byte[] name, byte[] val, HttpHeader, HttpHeader[]
     */
    jint frameCapacity = (jint)(num_headers * 4);
    jint result = (*env)->PushLocalFrame(env, frameCapacity);

    if (result != 0) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Failed to PushLocalFrame. Possibly OutOfMemory.", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    jobjectArray jHeaders = s_java_headers_array_from_native(user_data, header_array, num_headers);
    if (!jHeaders) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Failed to create HttpHeaders", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    int resp_status = -1;
    int err_code = aws_http_stream_get_incoming_response_status(stream, &resp_status);
    if (err_code != AWS_OP_SUCCESS) {
        return AWS_OP_ERR;
    }

    (*env)->CallVoidMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.onResponseHeaders,
        callback->java_http_stream,
        resp_status,
        jHeaders);

    /* Mark all the Java Objects created since the last call to PushLocalFrame() as eligible for GC */
    (*env)->PopLocalFrame(env, NULL);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    return AWS_OP_SUCCESS;
}

static int s_on_incoming_header_block_done_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    void *user_data) {
    (void)stream;

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    jint jni_block_type = block_type;
    (*env)->CallVoidMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.onResponseHeadersDone,
        callback->java_http_stream,
        jni_block_type);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    return AWS_OP_SUCCESS;
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

// Returns AWS_OP_SUCCESS or AWS_OP_ERR
static int s_resp_body_publish_to_java(
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
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Received Exception from onResponseBody", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    if (window_increment < 0) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Window Increment from onResponseBody < 0", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    // We can check the ByteBuffer read position to verify that the user callback actually read all the data
    // they claimed to be able to read.
    size_t read_position = aws_jni_byte_buffer_get_position(env, callback->java_body_buf);
    if (read_position != callback->native_body_buf.len) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: ByteBuffer.remaining() > 0 after onResponseBody", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    // Publish to Java succeeded, set resp body buffer position to zero
    callback->native_body_buf.len = 0;
    *out_window_update_size = window_increment;
    return AWS_OP_SUCCESS;
}

static int s_on_incoming_body_fn(struct aws_http_stream *stream, const struct aws_byte_cursor *data, void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

    struct aws_byte_cursor body_in_remaining = *data;
    size_t total_window_increment = 0;

    while (body_in_remaining.len > 0) {
        size_t curr_window_increment = 0;
        aws_byte_buf_transfer_best_effort(&callback->native_body_buf, &body_in_remaining);

        int result = s_resp_body_publish_to_java(stream, callback, &curr_window_increment);
        if (result != AWS_OP_SUCCESS) {
            return AWS_OP_ERR;
        }

        total_window_increment += curr_window_increment;
    }

    if (total_window_increment > 0) {
        aws_http_stream_update_window(stream, total_window_increment);
    }

    return AWS_OP_SUCCESS;
}

static void s_on_stream_complete_fn(struct aws_http_stream *stream, int error_code, void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    // Don't invoke Java callbacks if Java HttpStream failed to completely setup
    if (http_stream_callback_is_valid(callback)) {

        jint jErrorCode = error_code;
        (*env)->CallVoidMethod(
            env,
            callback->java_crt_http_callback_handler,
            s_crt_http_stream_handler.onResponseComplete,
            callback->java_http_stream,
            jErrorCode);

        if ((*env)->ExceptionCheck(env)) {
            // Close the Connection if the Java Callback throws an Exception
            aws_http_connection_close(aws_http_stream_get_connection(stream));
        }
    }

    http_stream_callback_release(env, callback);
}

static int s_native_outgoing_body_read(struct aws_input_stream *input_stream, struct aws_byte_buf *dst) {
    struct http_stream_callback_data *callback = input_stream->impl;

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    uint8_t *out = &(dst->buffer[dst->len]);
    size_t out_remaining = dst->capacity - dst->len;

    size_t buf_capacity = callback->native_body_buf.capacity;
    size_t request_size = (buf_capacity > out_remaining) ? out_remaining : buf_capacity;

    jobject jByteBuffer = callback->java_body_buf;

    aws_jni_byte_buffer_set_position(env, jByteBuffer, 0);
    aws_jni_byte_buffer_set_limit(env, jByteBuffer, (jint)request_size);

    jboolean isDone = (*env)->CallBooleanMethod(
        env,
        callback->java_crt_http_callback_handler,
        s_crt_http_stream_handler.sendOutgoingBody,
        callback->java_http_stream,
        jByteBuffer);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    size_t amt_written = aws_jni_byte_buffer_get_position(env, jByteBuffer);
    AWS_FATAL_ASSERT(amt_written <= out_remaining);

    memcpy(out, callback->native_body_buf.buffer, amt_written);
    dst->len += amt_written;

    if (isDone) {
        callback->native_outgoing_body_done = isDone;
    }

    return AWS_OP_SUCCESS;
}

static int s_native_outgoing_body_status(struct aws_input_stream *input_stream, struct aws_stream_status *status) {
    struct http_stream_callback_data *callback = input_stream->impl;

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

    AWS_ZERO_STRUCT(*status);
    status->is_end_of_stream = callback->native_outgoing_body_done;
    return AWS_OP_SUCCESS;
}

/* If error occurs, A Java exception is thrown and false is returned. */
static bool s_fill_out_request(
    JNIEnv *env,
    struct aws_http_message *request,
    jstring jni_method,
    jstring jni_uri,
    jobjectArray jni_headers) {

    int result = aws_http_message_set_request_method(request, aws_jni_byte_cursor_from_jstring(env, jni_method));
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Method error");
        return false;
    }

    result = aws_http_message_set_request_path(request, aws_jni_byte_cursor_from_jstring(env, jni_uri));
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Path error");
        return false;
    }

    jsize num_headers = (*env)->GetArrayLength(env, jni_headers);
    for (jsize i = 0; i < num_headers; ++i) {
        jobject jHeader = (*env)->GetObjectArrayElement(env, jni_headers, i);
        jbyteArray jname = (*env)->GetObjectField(env, jHeader, s_http_header.name);
        jbyteArray jvalue = (*env)->GetObjectField(env, jHeader, s_http_header.value);

        struct aws_http_header c_header = {
            .name = aws_jni_byte_cursor_from_jbyteArray(env, jname),
            .value = aws_jni_byte_cursor_from_jbyteArray(env, jvalue),
        };

        result = aws_http_message_add_header(request, c_header);
        if (result != AWS_OP_SUCCESS) {
            aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Header[%d] error", i);
            return false;
        }
    }

    return true;
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_http_HttpConnection_httpConnectionMakeRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jobject crtBuffer,
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
        http_stream_callback_alloc(env, crtBuffer, jni_crt_http_callback_handler);
    if (!callback_data) {
        // Exception already thrown
        return (jobject)NULL;
    }

    if (!s_fill_out_request(env, callback_data->native_request, jni_method, jni_uri, jni_headers)) {
        // Exception already thrown
        http_stream_callback_release(env, callback_data);
        return (jobject)NULL;
    }

    struct aws_http_make_request_options request_options = {
        .self_size = sizeof(request_options),
        .request = callback_data->native_request,
        .manual_window_management = true,
        // Set Callbacks
        .on_response_headers = s_on_incoming_headers_fn,
        .on_response_header_block_done = s_on_incoming_header_block_done_fn,
        .on_response_body = s_on_incoming_body_fn,
        .on_complete = s_on_stream_complete_fn,
        .user_data = callback_data,
    };

    struct aws_http_stream *native_stream = NULL;
    jobject jHttpStream = NULL;

    // There's a Data Race between this thread writing to callback_data->java_http_stream and the EventLoop thread
    // reading callback_data->java_http_stream when calling the callbacks, add a lock so that
    // callbacks will wait for setup to complete.
    aws_mutex_lock(&callback_data->setup_lock);

    // This call schedules tasks on the Native Event loop thread to begin sending HttpRequest and receive the response.
    native_stream = aws_http_connection_make_request(native_conn, &request_options);
    if (native_stream) {
        AWS_LOGF_TRACE(
            AWS_LS_HTTP_CONNECTION,
            "Opened new Stream on Connection. conn: %p, stream: %p",
            (void *)native_conn,
            (void *)native_stream);

        jHttpStream = s_java_http_stream_from_native_new(env, crtBuffer, native_stream);
        if (jHttpStream) {
            // Call NewGlobalRef() so that jHttpStream reference doesn't get Garbage collected can can be used from
            // callbacks.
            callback_data->java_http_stream = (*env)->NewGlobalRef(env, jHttpStream);
        }
    }

    // Now that callback_data->java_http_stream has been written, the EventLoop thread may begin using this callback.
    aws_mutex_unlock(&callback_data->setup_lock);

    // Check for errors that might have occurred while holding the lock.
    if (!native_stream) {
        // Failed to create native aws_http_stream. Clean up callback_data.
        AWS_LOGF_ERROR(AWS_LS_HTTP_CONNECTION, "Stream Request Failed. conn: %p", (void *)native_conn);
        aws_jni_throw_runtime_exception(env, "HttpConnection.MakeRequest: Unable to Execute Request");
        http_stream_callback_release(env, callback_data);
        return NULL;
    } else if (!jHttpStream) {
        // Failed to create java HttpStream, but did create native aws_http_stream.
        // Close connection and mark native_stream for release.
        // callback_data will clean itself up when stream completes.
        aws_http_connection_close(native_conn);
        aws_http_stream_release(native_stream);
        return NULL;
    } else if (!callback_data->java_http_stream) {
        // Failed to create global reference to HttpStream, but we did create java HttpStream.
        // Close the connection. Native stream will be marked for release when local HttpStream goes out of scope.
        // callback_data will clean itself up when stream completes. */
        aws_http_connection_close(native_conn);
        return NULL;
    }

    return callback_data->java_http_stream;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamRelease(JNIEnv *env, jclass jni_class, jlong jni_stream) {

    (void)jni_class;

    struct aws_http_stream *stream = (struct aws_http_stream *)jni_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }

    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Releasing Stream. stream: %p", (void *)stream);
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

    AWS_LOGF_TRACE(
        AWS_LS_HTTP_STREAM, "Updating Stream Window. stream: %p, update: %d", (void *)stream, (int)window_update);
    aws_http_stream_update_window(stream, window_update);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

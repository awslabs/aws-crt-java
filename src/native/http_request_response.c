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

#include <jni.h>

#include "crt.h"
#include "http_request_utils.h"
#include "java_class_ids.h"

#include <aws/common/mutex.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/http/request_response.h>
#include <aws/io/logging.h>
#include <aws/io/stream.h>

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

static jobject s_java_http_stream_from_native_new(JNIEnv *env, void *opaque) {
    jlong jni_native_ptr = (jlong)opaque;
    AWS_ASSERT(jni_native_ptr);
    return (*env)->NewObject(
        env, http_stream_properties.stream_class, http_stream_properties.constructor, jni_native_ptr);
}

static void s_java_http_stream_from_native_delete(JNIEnv *env, jobject jHttpStream) {
    /* Delete our reference to the HttpStream Object from the JVM. */
    (*env)->DeleteGlobalRef(env, jHttpStream);
}

/*******************************************************************************
 * http_stream_callback_data - carries around data needed by the various http request
 * callbacks.
 ******************************************************************************/
struct http_stream_callback_data {
    JavaVM *jvm;

    // TEMP: Until Java API changes to match "H1B" native HTTP API,
    // create aws_http_message and aws_input_stream under the hood.
    struct aws_http_message *native_request;

    jobject java_http_response_stream_handler;
    jobject java_http_stream;
    struct aws_http_stream *native_stream;
    struct aws_byte_buf headers_buf;
    int response_status;
};

static void http_stream_callback_destroy(JNIEnv *env, struct http_stream_callback_data *callback) {

    if (callback == NULL) {
        return;
    }

    if (callback->java_http_stream) {
        s_java_http_stream_from_native_delete(env, callback->java_http_stream);
    }

    if (callback->java_http_response_stream_handler != NULL) {
        (*env)->DeleteGlobalRef(env, callback->java_http_response_stream_handler);
    }

    if (callback->native_request) {
        struct aws_input_stream *input_stream = aws_http_message_get_body_stream(callback->native_request);
        if (input_stream != NULL) {
            aws_input_stream_destroy(input_stream);
        }

        aws_http_message_destroy(callback->native_request);
    }

    aws_byte_buf_clean_up(&callback->headers_buf);
    aws_mem_release(aws_jni_get_allocator(), callback);
}

// If error occurs, A Java exception is thrown and NULL is returned.
static struct http_stream_callback_data *http_stream_callback_alloc(JNIEnv *env, jobject java_callback_handler) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct http_stream_callback_data *callback = aws_mem_calloc(allocator, 1, sizeof(struct http_stream_callback_data));
    AWS_FATAL_ASSERT(callback);

    // GetJavaVM() reference doesn't need a NewGlobalRef() call since it's global by default
    jint jvmresult = (*env)->GetJavaVM(env, &callback->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback->java_http_response_stream_handler = (*env)->NewGlobalRef(env, java_callback_handler);
    AWS_FATAL_ASSERT(callback->java_http_response_stream_handler);
    AWS_FATAL_ASSERT(!aws_byte_buf_init(&callback->headers_buf, allocator, 1024));

    return callback;
}

static int s_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data) {
    (void)block_type;

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;
    int resp_status = -1;
    int err_code = aws_http_stream_get_incoming_response_status(stream, &resp_status);
    if (err_code != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Invalid Incoming Response Status", (void *)stream);
        return AWS_OP_ERR;
    }

    callback->response_status = resp_status;

    if (aws_marshal_http_headers_to_dynamic_buffer(&callback->headers_buf, header_array, num_headers)) {
        AWS_LOGF_ERROR(
            AWS_LS_HTTP_STREAM, "id=%p: Failed to allocate buffer space for incoming headers", (void *)stream);
        return AWS_OP_ERR;
    }

    return AWS_OP_SUCCESS;
}

static int s_on_incoming_header_block_done_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    void *user_data) {
    (void)stream;

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);
    jint jni_block_type = block_type;

    jobject jni_headers_buf =
        aws_jni_direct_byte_buffer_from_raw_ptr(env, callback->headers_buf.buffer, callback->headers_buf.len);

    (*env)->CallVoidMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseHeaders,
        callback->java_http_stream,
        (jint)callback->response_status,
        (jint)block_type,
        jni_headers_buf);

    if ((*env)->ExceptionCheck(env)) {
        (*env)->DeleteLocalRef(env, jni_headers_buf);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    /* instead of cleaning it up here, reset it in case another block is encountered */
    aws_byte_buf_reset(&callback->headers_buf, false);
    (*env)->DeleteLocalRef(env, jni_headers_buf);

    (*env)->CallVoidMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseHeadersDone,
        callback->java_http_stream,
        jni_block_type);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    return AWS_OP_SUCCESS;
}

static int s_on_incoming_body_fn(struct aws_http_stream *stream, const struct aws_byte_cursor *data, void *user_data) {
    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    size_t total_window_increment = 0;

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);
    jbyteArray jni_payload = (*env)->NewByteArray(env, (jsize)data->len);
    (*env)->SetByteArrayRegion(env, jni_payload, 0, (jsize)data->len, (const signed char *)data->ptr);

    jint window_increment = (*env)->CallIntMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseBody,
        callback->java_http_stream,
        jni_payload);

    (*env)->DeleteLocalRef(env, jni_payload);

    if ((*env)->ExceptionCheck(env)) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Received Exception from onResponseBody", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    if (window_increment < 0) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Window Increment from onResponseBody < 0", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    total_window_increment += window_increment;

    if (total_window_increment > 0) {
        aws_http_stream_update_window(stream, total_window_increment);
    }

    return AWS_OP_SUCCESS;
}

static void s_on_stream_complete_fn(struct aws_http_stream *stream, int error_code, void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    /* Don't invoke Java callbacks if Java HttpStream failed to completely setup */
    jint jErrorCode = error_code;
    (*env)->CallVoidMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseComplete,
        callback->java_http_stream,
        jErrorCode);

    if ((*env)->ExceptionCheck(env)) {
        /* Close the Connection if the Java Callback throws an Exception */
        aws_http_connection_close(aws_http_stream_get_connection(stream));
    }

    http_stream_callback_destroy(env, callback);
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionMakeRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jbyteArray marshalled_request,
    jobject jni_http_request_body_stream,
    jobject jni_http_response_callback_handler) {

    (void)jni_class;
    struct aws_http_connection *native_conn = (struct aws_http_connection *)jni_connection;

    if (!native_conn) {
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Invalid aws_http_connection");
        return (jobject)NULL;
    }

    if (!jni_http_response_callback_handler) {
        aws_jni_throw_runtime_exception(
            env, "HttpClientConnection.MakeRequest: Invalid jni_http_response_callback_handler");
        return (jobject)NULL;
    }

    struct http_stream_callback_data *callback_data =
        http_stream_callback_alloc(env, jni_http_response_callback_handler);
    if (!callback_data) {
        /* Exception already thrown */
        return (jobject)NULL;
    }

    callback_data->native_request =
        aws_http_request_new_from_java_http_request(env, marshalled_request, jni_http_request_body_stream);
    if (callback_data->native_request == NULL) {
        /* Exception already thrown */
        http_stream_callback_destroy(env, callback_data);
        return (jobject)NULL;
    }

    struct aws_http_make_request_options request_options = {
        .self_size = sizeof(request_options),
        .request = callback_data->native_request,
        /* Set Callbacks */
        .on_response_headers = s_on_incoming_headers_fn,
        .on_response_header_block_done = s_on_incoming_header_block_done_fn,
        .on_response_body = s_on_incoming_body_fn,
        .on_complete = s_on_stream_complete_fn,
        .user_data = callback_data,
    };

    jobject jHttpStream = NULL;

    callback_data->native_stream = aws_http_connection_make_request(native_conn, &request_options);
    if (callback_data->native_stream) {
        AWS_LOGF_TRACE(
            AWS_LS_HTTP_CONNECTION,
            "Opened new Stream on Connection. conn: %p, stream: %p",
            (void *)native_conn,
            (void *)callback_data->native_stream);

        jHttpStream = s_java_http_stream_from_native_new(env, callback_data);
    }

    /* Check for errors that might have occurred while holding the lock. */
    if (!callback_data->native_stream) {
        /* Failed to create native aws_http_stream. Clean up callback_data. */
        AWS_LOGF_ERROR(AWS_LS_HTTP_CONNECTION, "Stream Request Failed. conn: %p", (void *)native_conn);
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Unable to Execute Request");
        http_stream_callback_destroy(env, callback_data);
        return NULL;
    } else if (!jHttpStream) {
        /* Failed to create java HttpStream, but did create native aws_http_stream.
          Close connection and mark native_stream for release.
          callback_data will clean itself up when stream completes. */
        aws_http_connection_close(native_conn);
        aws_http_stream_release(callback_data->native_stream);
        return NULL;
    }

    return jHttpStream;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamActivate(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_cb_data,
    jobject j_http_stream) {
    (void)jni_class;

    struct http_stream_callback_data *cb_data = (struct http_stream_callback_data *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }

    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Activating Stream. stream: %p", (void *)stream);

    /* global ref this because now the callbacks will be firing, and they will release their reference when the
     * stream callback sequence completes. */
    cb_data->java_http_stream = (*env)->NewGlobalRef(env, j_http_stream);
    if (aws_http_stream_activate(stream)) {
        (*env)->DeleteGlobalRef(env, cb_data->java_http_stream);
        aws_jni_throw_runtime_exception(
            env, "HttpStream activate failed with error %s\n", aws_error_str(aws_last_error()));
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamRelease(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_cb_data) {

    (void)jni_class;

    struct http_stream_callback_data *cb_data = (struct http_stream_callback_data *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

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
    jlong jni_cb_data) {

    (void)jni_class;

    struct http_stream_callback_data *cb_data = (struct http_stream_callback_data *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

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
    jlong jni_cb_data,
    jint window_update) {

    (void)jni_class;

    struct http_stream_callback_data *cb_data = (struct http_stream_callback_data *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

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

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionShutdown(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {

    (void)jni_class;
    struct aws_http_connection *native_conn = (struct aws_http_connection *)jni_connection;

    if (!native_conn) {
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.Shutdown: Invalid aws_http_connection");
        return;
    }

    aws_http_connection_close(native_conn);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

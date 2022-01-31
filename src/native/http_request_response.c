/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include "crt.h"
#include "http_connection_manager.h"
#include "http_request_response.h"
#include "http_request_utils.h"
#include "java_class_ids.h"

#include <aws/common/atomics.h>
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

jobject aws_java_http_stream_from_native_new(JNIEnv *env, void *opaque, int version) {
    jlong jni_native_ptr = (jlong)opaque;
    AWS_ASSERT(jni_native_ptr);
    jobject stream = NULL;
    switch (version) {
        case AWS_HTTP_VERSION_2:
            stream = (*env)->NewObject(
                env, http2_stream_properties.stream_class, http2_stream_properties.constructor, jni_native_ptr);
            break;
        case AWS_HTTP_VERSION_1_0:
        case AWS_HTTP_VERSION_1_1:
            stream = (*env)->NewObject(
                env, http_stream_properties.stream_class, http_stream_properties.constructor, jni_native_ptr);
            break;
        default:
            aws_raise_error(AWS_ERROR_UNIMPLEMENTED);
    }
    if (!stream) {
        aws_jni_throw_runtime_exception(env, "Failed to create Java stream from native stream");
    }
    return stream;
}

void aws_java_http_stream_from_native_delete(JNIEnv *env, jobject jHttpStream) {
    /* Delete our reference to the HttpStream Object from the JVM. */
    (*env)->DeleteGlobalRef(env, jHttpStream);
}

/*******************************************************************************
 * http_stream_binding - carries around data needed by the various http request
 * callbacks.
 ******************************************************************************/

void aws_http_stream_binding_destroy(JNIEnv *env, struct http_stream_binding *callback) {

    if (callback == NULL) {
        return;
    }

    if (callback->java_http_stream) {
        aws_java_http_stream_from_native_delete(env, callback->java_http_stream);
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
struct http_stream_binding *aws_http_stream_binding_alloc(JNIEnv *env, jobject java_callback_handler) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct http_stream_binding *callback = aws_mem_calloc(allocator, 1, sizeof(struct http_stream_binding));
    AWS_FATAL_ASSERT(callback);

    // GetJavaVM() reference doesn't need a NewGlobalRef() call since it's global by default
    jint jvmresult = (*env)->GetJavaVM(env, &callback->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback->java_http_response_stream_handler = (*env)->NewGlobalRef(env, java_callback_handler);
    AWS_FATAL_ASSERT(callback->java_http_response_stream_handler);
    AWS_FATAL_ASSERT(!aws_byte_buf_init(&callback->headers_buf, allocator, 1024));

    aws_atomic_init_int(&callback->activated, 0);

    return callback;
}

static int s_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data) {
    (void)block_type;

    struct http_stream_binding *callback = (struct http_stream_binding *)user_data;
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

    struct http_stream_binding *callback = (struct http_stream_binding *)user_data;

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

    if (aws_jni_check_and_clear_exception(env)) {
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

    if (aws_jni_check_and_clear_exception(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    return AWS_OP_SUCCESS;
}

static int s_on_incoming_body_fn(struct aws_http_stream *stream, const struct aws_byte_cursor *data, void *user_data) {
    struct http_stream_binding *callback = (struct http_stream_binding *)user_data;

    size_t total_window_increment = 0;

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);
    jobject jni_payload = aws_jni_direct_byte_buffer_from_raw_ptr(env, data->ptr, data->len);

    jint window_increment = (*env)->CallIntMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseBody,
        callback->java_http_stream,
        jni_payload);

    (*env)->DeleteLocalRef(env, jni_payload);

    if (aws_jni_check_and_clear_exception(env)) {
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

    struct http_stream_binding *callback = (struct http_stream_binding *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    /* Don't invoke Java callbacks if Java HttpStream failed to completely setup */
    jint jErrorCode = error_code;
    (*env)->CallVoidMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseComplete,
        callback->java_http_stream,
        jErrorCode);

    if (aws_jni_check_and_clear_exception(env)) {
        /* Close the Connection if the Java Callback throws an Exception */
        aws_http_connection_close(aws_http_stream_get_connection(stream));
    }

    aws_http_stream_binding_destroy(env, callback);
}

jobjectArray aws_java_http_headers_from_native(JNIEnv *env, struct aws_http_headers *headers) {
    (void)headers;
    jobjectArray ret;
    const size_t header_count = aws_http_headers_count(headers);

    ret = (jobjectArray)(*env)->NewObjectArray(
        env, (jsize)header_count, http_header_properties.http_header_class, (void *)NULL);

    for (size_t index = 0; index < header_count; index += 1) {
        struct aws_http_header header;
        aws_http_headers_get_index(headers, index, &header);
        jbyteArray header_name = aws_jni_byte_array_from_cursor(env, &header.name);
        jbyteArray header_value = aws_jni_byte_array_from_cursor(env, &header.value);

        jobject java_http_header = (*env)->NewObject(
            env,
            http_header_properties.http_header_class,
            http_header_properties.constructor_method_id,
            header_name,
            header_value);

        (*env)->SetObjectArrayElement(env, ret, (jsize)index, java_http_header);
    }

    return (ret);
}

static jobject s_make_request_general(
    JNIEnv *env,
    jlong jni_connection,
    jbyteArray marshalled_request,
    jobject jni_http_request_body_stream,
    jobject jni_http_response_callback_handler,
    enum aws_http_version version) {

    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;

    if (!native_conn) {
        aws_jni_throw_null_pointer_exception(env, "HttpClientConnection.MakeRequest: Invalid aws_http_connection");
        return (jobject)NULL;
    }

    if (!jni_http_response_callback_handler) {
        aws_jni_throw_illegal_argument_exception(
            env, "HttpClientConnection.MakeRequest: Invalid jni_http_response_callback_handler");
        return (jobject)NULL;
    }

    struct http_stream_binding *callback_data = aws_http_stream_binding_alloc(env, jni_http_response_callback_handler);
    if (!callback_data) {
        /* Exception already thrown */
        return (jobject)NULL;
    }

    callback_data->native_request =
        aws_http_request_new_from_java_http_request(env, marshalled_request, jni_http_request_body_stream);
    if (callback_data->native_request == NULL) {
        /* Exception already thrown */
        aws_http_stream_binding_destroy(env, callback_data);
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

        jHttpStream = aws_java_http_stream_from_native_new(env, callback_data, version);
    }

    /* Check for errors that might have occurred while holding the lock. */
    if (!callback_data->native_stream) {
        /* Failed to create native aws_http_stream. Clean up callback_data. */
        AWS_LOGF_ERROR(AWS_LS_HTTP_CONNECTION, "Stream Request Failed. conn: %p", (void *)native_conn);
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Unable to Execute Request");
        aws_http_stream_binding_destroy(env, callback_data);
        return NULL;
    } else if (!jHttpStream) {
        /* Failed to create java HttpStream, but did create native aws_http_stream.
          Close connection and mark native_stream for release.
          callback_data will clean itself up when stream completes. */
        aws_http_connection_close(native_conn);
        aws_http_stream_release(callback_data->native_stream);
        /* Java exception has already been raised. */
        return NULL;
    }

    return jHttpStream;
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionMakeRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jbyteArray marshalled_request,
    jobject jni_http_request_body_stream,
    jobject jni_http_response_callback_handler) {
    (void)jni_class;
    return s_make_request_general(
        env,
        jni_connection,
        marshalled_request,
        jni_http_request_body_stream,
        jni_http_response_callback_handler,
        AWS_HTTP_VERSION_1_1);
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_http_Http2ClientConnection_http2ClientConnectionMakeRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jbyteArray marshalled_request,
    jobject jni_http_request_body_stream,
    jobject jni_http_response_callback_handler) {
    (void)jni_class;
    return s_make_request_general(
        env,
        jni_connection,
        marshalled_request,
        jni_http_request_body_stream,
        jni_http_response_callback_handler,
        AWS_HTTP_VERSION_2);
}

struct http_stream_chunked_callback_data {
    struct http_stream_binding *stream_cb_data;
    struct aws_byte_buf chunk_data;
    struct aws_input_stream *chunk_stream;
    jobject completion_callback;
};

static void s_cleanup_chunked_callback_data(
    JNIEnv *env,
    struct http_stream_chunked_callback_data *chunked_callback_data) {
    aws_input_stream_destroy(chunked_callback_data->chunk_stream);
    aws_byte_buf_clean_up(&chunked_callback_data->chunk_data);
    (*env)->DeleteGlobalRef(env, chunked_callback_data->completion_callback);
    aws_mem_release(aws_jni_get_allocator(), chunked_callback_data);
}

static void s_write_chunk_complete(struct aws_http_stream *stream, int error_code, void *user_data) {
    (void)stream;

    struct http_stream_chunked_callback_data *chunked_callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(chunked_callback_data->stream_cb_data->jvm);
    (*env)->CallVoidMethod(
        env,
        chunked_callback_data->completion_callback,
        http_stream_write_chunk_completion_properties.callback,
        error_code);
    aws_jni_check_and_clear_exception(env);

    s_cleanup_chunked_callback_data(env, chunked_callback_data);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamWriteChunk(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_cb_data,
    jbyteArray chunk_data,
    jboolean is_final_chunk,
    jobject completion_callback) {
    (void)jni_class;

    struct http_stream_binding *cb_data = (struct http_stream_binding *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

    struct http_stream_chunked_callback_data *chunked_callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct http_stream_chunked_callback_data));

    chunked_callback_data->stream_cb_data = cb_data;
    chunked_callback_data->completion_callback = (*env)->NewGlobalRef(env, completion_callback);

    struct aws_byte_cursor chunk_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, chunk_data);
    aws_byte_buf_init_copy_from_cursor(&chunked_callback_data->chunk_data, aws_jni_get_allocator(), chunk_cur);
    aws_jni_byte_cursor_from_jbyteArray_release(env, chunk_data, chunk_cur);

    struct aws_http1_chunk_options chunk_options = {
        .chunk_data_size = chunked_callback_data->chunk_data.len,
        .user_data = chunked_callback_data,
        .on_complete = s_write_chunk_complete,
    };

    chunk_cur = aws_byte_cursor_from_buf(&chunked_callback_data->chunk_data);
    chunked_callback_data->chunk_stream = aws_input_stream_new_from_cursor(aws_jni_get_allocator(), &chunk_cur);
    chunk_options.chunk_data = chunked_callback_data->chunk_stream;

    if (aws_http1_stream_write_chunk(stream, &chunk_options)) {
        s_cleanup_chunked_callback_data(env, chunked_callback_data);
        return AWS_OP_ERR;
    }

    if (is_final_chunk) {
        struct aws_http1_chunk_options final_chunk_options = {
            .chunk_data_size = 0,
        };

        if (aws_http1_stream_write_chunk(stream, &final_chunk_options)) {
            return AWS_OP_ERR;
        }
    }

    return AWS_OP_SUCCESS;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamActivate(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_cb_data,
    jobject j_http_stream) {
    (void)jni_class;

    struct http_stream_binding *cb_data = (struct http_stream_binding *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }

    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Activating Stream. stream: %p", (void *)stream);

    /* global ref this because now the callbacks will be firing, and they will release their reference when the
     * stream callback sequence completes. */
    cb_data->java_http_stream = (*env)->NewGlobalRef(env, j_http_stream);
    aws_atomic_store_int(&cb_data->activated, 1);
    if (aws_http_stream_activate(stream)) {
        aws_atomic_store_int(&cb_data->activated, 0);
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

    struct http_stream_binding *cb_data = (struct http_stream_binding *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }
    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Releasing Stream. stream: %p", (void *)stream);
    aws_http_stream_release(stream);

    size_t not_activated = 0;
    if (aws_atomic_compare_exchange_int(&cb_data->activated, &not_activated, 1)) {
        aws_http_stream_binding_destroy(env, cb_data);
    }
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_http_HttpStream_httpStreamGetResponseStatusCode(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_cb_data) {

    (void)jni_class;

    struct http_stream_binding *cb_data = (struct http_stream_binding *)jni_cb_data;
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

    struct http_stream_binding *cb_data = (struct http_stream_binding *)jni_cb_data;
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

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_Http2Stream_http2StreamResetStream(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_cb_data,
    jint error_code) {

    (void)jni_class;

    struct http_stream_binding *cb_data = (struct http_stream_binding *)jni_cb_data;
    struct aws_http_stream *stream = cb_data->native_stream;

    if (stream == NULL) {
        aws_jni_throw_null_pointer_exception(env, "Http2Stream is null.");
        return;
    }

    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Resetting Stream. stream: %p", (void *)stream);
    if (aws_http2_stream_reset(stream, error_code)) {
        aws_jni_throw_runtime_exception(env, "reset stream failed.");
        return;
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionShutdown(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {

    (void)jni_class;
    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;

    if (!native_conn) {
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.Shutdown: Invalid aws_http_connection");
        return;
    }

    aws_http_connection_close(native_conn);
}

JNIEXPORT jshort JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionGetVersion(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {

    (void)jni_class;
    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;

    if (!native_conn) {
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.getVersion: Invalid aws_http_connection");
        return 0;
    }
    return (jshort)aws_http_connection_get_version(native_conn);
}
struct aws_http2_callback_data {
    JavaVM *jvm;
    jobject async_callback;
};

static void s_cleanup_http2_callback_data(struct aws_http2_callback_data *callback_data) {

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    if (callback_data->async_callback) {
        (*env)->DeleteGlobalRef(env, callback_data->async_callback);
    }
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static struct aws_http2_callback_data *s_new_http2_callback_data(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject async_callback) {
    struct aws_http2_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_http2_callback_data));

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);
    callback_data->async_callback = async_callback ? (*env)->NewGlobalRef(env, async_callback) : NULL;
    AWS_FATAL_ASSERT(callback_data->async_callback != NULL);

    return callback_data;
}

static void s_on_settings_completed(struct aws_http_connection *http2_connection, int error_code, void *user_data) {
    (void)http2_connection;
    struct aws_http2_callback_data *callback_data = user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    if (error_code) {
        jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, error_code);
        (*env)->CallVoidMethod(env, callback_data->async_callback, async_callback_properties.on_failure, crt_exception);
        (*env)->DeleteLocalRef(env, crt_exception);
    } else {
        (*env)->CallVoidMethod(env, callback_data->async_callback, async_callback_properties.on_success);
    }
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    s_cleanup_http2_callback_data(callback_data);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_Http2ClientConnection_http2ClientConnectionUpdateSettings(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jobject java_async_callback,
    jlongArray java_marshalled_settings) {

    (void)jni_class;

    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;

    if (!native_conn) {
        aws_jni_throw_null_pointer_exception(
            env, "Http2ClientConnection.http2ClientConnectionUpdateSettings: Invalid aws_http_connection");
        return;
    }
    if (!java_async_callback) {
        aws_jni_throw_illegal_argument_exception(
            env, "Http2ClientConnection.http2ClientConnectionUpdateSettings: Invalid async callback");
        return;
    }
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_http2_callback_data *callback_data = s_new_http2_callback_data(env, allocator, java_async_callback);

    /* We marshalled each setting to two long integers, the long list will be number of settings times two */
    const size_t len = (*env)->GetArrayLength(env, java_marshalled_settings);
    AWS_ASSERT(len % 2 == 0);
    const size_t settings_len = len / 2;
    struct aws_http2_setting *settings =
        settings_len ? aws_mem_calloc(allocator, settings_len, sizeof(struct aws_http2_setting)) : NULL;
    int success = false;
    jlong *marshalled_settings = (*env)->GetLongArrayElements(env, java_marshalled_settings, NULL);
    for (size_t i = 0; i < settings_len; i++) {
        jlong id = marshalled_settings[i * 2];
        settings[i].id = id;
        jlong value = marshalled_settings[i * 2 + 1];
        settings[i].value = (uint32_t)value;
    }

    if (aws_http2_connection_change_settings(
            native_conn, settings, settings_len, s_on_settings_completed, callback_data)) {
        aws_jni_throw_runtime_exception(
            env, "Http2ClientConnection.http2ClientConnectionUpdateSettings: failed to change settings");
        goto done;
    }
    success = true;
done:
    aws_mem_release(allocator, settings);
    (*env)->ReleaseLongArrayElements(env, java_marshalled_settings, (jlong *)marshalled_settings, JNI_ABORT);
    if (!success) {
        s_cleanup_http2_callback_data(callback_data);
    }
    return;
}

static void s_on_ping_completed(
    struct aws_http_connection *http2_connection,
    uint64_t round_trip_time_ns,
    int error_code,
    void *user_data) {
    (void)http2_connection;
    struct aws_http2_callback_data *callback_data = user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    if (error_code) {
        jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, error_code);
        (*env)->CallVoidMethod(env, callback_data->async_callback, async_callback_properties.on_failure, crt_exception);
        (*env)->DeleteLocalRef(env, crt_exception);
    } else {
        jobject java_round_trip_time_ns = (*env)->NewObject(
            env, boxed_long_properties.long_class, boxed_long_properties.constructor, (jlong)round_trip_time_ns);
        (*env)->CallVoidMethod(
            env,
            callback_data->async_callback,
            async_callback_properties.on_success_with_object,
            java_round_trip_time_ns);
        (*env)->DeleteLocalRef(env, java_round_trip_time_ns);
    }
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    s_cleanup_http2_callback_data(callback_data);
}
JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_Http2ClientConnection_http2ClientConnectionSendPing(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jobject java_async_callback,
    jbyteArray ping_data) {

    (void)jni_class;
    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;

    if (!native_conn) {
        aws_jni_throw_null_pointer_exception(
            env, "Http2ClientConnection.http2ClientConnectionSendPing: Invalid aws_http_connection");
        return;
    }
    if (!java_async_callback) {
        aws_jni_throw_illegal_argument_exception(
            env, "Http2ClientConnection.http2ClientConnectionSendPing: Invalid async callback");
        return;
    }
    bool success = false;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_byte_cursor *ping_cur_pointer = NULL;
    struct aws_byte_cursor ping_cur;
    AWS_ZERO_STRUCT(ping_cur);
    struct aws_http2_callback_data *callback_data = s_new_http2_callback_data(env, allocator, java_async_callback);

    if (ping_data) {
        ping_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, ping_data);
        ping_cur_pointer = &ping_cur;
    }
    if (aws_http2_connection_ping(native_conn, ping_cur_pointer, s_on_ping_completed, callback_data)) {
        aws_jni_throw_runtime_exception(env, "Failed to send ping");
        goto done;
    }
    success = true;
done:
    if (ping_cur_pointer) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, ping_data, ping_cur);
    }
    if (!success) {
        s_cleanup_http2_callback_data(callback_data);
    }
    return;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_Http2ClientConnection_http2ClientConnectionSendGoAway(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jlong h2_error_code,
    jboolean allow_more_streams,
    jbyteArray debug_data) {

    (void)jni_class;
    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;
    struct aws_byte_cursor *debug_cur_pointer = NULL;
    struct aws_byte_cursor debug_cur;
    AWS_ZERO_STRUCT(debug_cur);

    if (!native_conn) {
        aws_jni_throw_runtime_exception(
            env, "Http2ClientConnection.http2ClientConnectionSendGoAway: Invalid aws_http_connection");
        return;
    }
    if (debug_data) {
        debug_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, debug_data);
        debug_cur_pointer = &debug_cur;
    }
    if (aws_http2_connection_send_goaway(native_conn, (uint32_t)h2_error_code, allow_more_streams, debug_cur_pointer)) {
        aws_jni_throw_runtime_exception(env, "Failed to send goaway");
    }
    if (debug_cur_pointer) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, debug_data, debug_cur);
    }
    return;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_Http2ClientConnection_http2ClientConnectionUpdateConnectionWindow(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_connection,
        jlong increment_size) {

    (void)jni_class;
    struct aws_http_connection_binding *connection_binding = (struct aws_http_connection_binding *)jni_connection;
    struct aws_http_connection *native_conn = connection_binding->connection;

    if (!native_conn) {
        aws_jni_throw_runtime_exception(
            env, "Http2ClientConnection.http2ClientConnectionUpdateConnectionWindow: Invalid aws_http_connection");
        return;
    }
    /* We did range check in Java already. */
    aws_http2_connection_update_window(native_conn, (uint32_t)increment_size);
    return;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

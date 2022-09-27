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
            aws_jni_throw_runtime_exception(env, "Unsupported HTTP protocol.");
            aws_raise_error(AWS_ERROR_UNIMPLEMENTED);
    }
    return stream;
}

void aws_java_http_stream_from_native_delete(JNIEnv *env, jobject jHttpStream) {
    /* Delete our reference to the HttpStream Object from the JVM. */
    (*env)->DeleteGlobalRef(env, jHttpStream);
}

/*******************************************************************************
 * http_stream_binding - Jni native represent of the Java HTTP stream object
 ******************************************************************************/

static void s_http_stream_binding_destroy(JNIEnv *env, struct http_stream_binding *binding) {

    if (binding->java_http_stream_base) {
        aws_java_http_stream_from_native_delete(env, binding->java_http_stream_base);
    }

    if (binding->java_http_response_stream_handler != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_http_response_stream_handler);
    }

    if (binding->native_request) {
        aws_http_message_release(binding->native_request);
    }
    aws_byte_buf_clean_up(&binding->headers_buf);
    aws_mem_release(aws_jni_get_allocator(), binding);
}

void *aws_http_stream_binding_acquire(struct http_stream_binding *binding) {
    if (binding == NULL) {
        return NULL;
    }
    aws_atomic_fetch_add(&binding->ref, 1);
    return binding;
}

void *aws_http_stream_binding_release(JNIEnv *env, struct http_stream_binding *binding) {
    if (binding == NULL) {
        return NULL;
    }
    size_t pre_ref = aws_atomic_fetch_sub(&binding->ref, 1);
    AWS_ASSERT(pre_ref > 0 && "stream binding refcount has gone negative");
    if (pre_ref == 1) {
        s_http_stream_binding_destroy(env, binding);
    }
    return NULL;
}

// If error occurs, A Java exception is thrown and NULL is returned.
struct http_stream_binding *aws_http_stream_binding_new(JNIEnv *env, jobject java_callback_handler) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct http_stream_binding *binding = aws_mem_calloc(allocator, 1, sizeof(struct http_stream_binding));
    AWS_FATAL_ASSERT(binding);

    // GetJavaVM() reference doesn't need a NewGlobalRef() call since it's global by default
    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->java_http_response_stream_handler = (*env)->NewGlobalRef(env, java_callback_handler);
    AWS_FATAL_ASSERT(binding->java_http_response_stream_handler);
    AWS_FATAL_ASSERT(!aws_byte_buf_init(&binding->headers_buf, allocator, 1024));

    aws_atomic_init_int(&binding->ref, 1);

    return binding;
}

int aws_java_http_stream_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data) {
    (void)block_type;

    struct http_stream_binding *binding = (struct http_stream_binding *)user_data;
    int resp_status = -1;
    int err_code = aws_http_stream_get_incoming_response_status(stream, &resp_status);
    if (err_code != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Invalid Incoming Response Status", (void *)stream);
        return AWS_OP_ERR;
    }

    binding->response_status = resp_status;

    if (aws_marshal_http_headers_to_dynamic_buffer(&binding->headers_buf, header_array, num_headers)) {
        AWS_LOGF_ERROR(
            AWS_LS_HTTP_STREAM, "id=%p: Failed to allocate buffer space for incoming headers", (void *)stream);
        return AWS_OP_ERR;
    }

    return AWS_OP_SUCCESS;
}

int aws_java_http_stream_on_incoming_header_block_done_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    void *user_data) {
    (void)stream;

    struct http_stream_binding *binding = (struct http_stream_binding *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    int result = AWS_OP_ERR;
    jint jni_block_type = block_type;

    jobject jni_headers_buf =
        aws_jni_direct_byte_buffer_from_raw_ptr(env, binding->headers_buf.buffer, binding->headers_buf.len);

    (*env)->CallVoidMethod(
        env,
        binding->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseHeaders,
        binding->java_http_stream_base,
        (jint)binding->response_status,
        (jint)block_type,
        jni_headers_buf);

    if (aws_jni_check_and_clear_exception(env)) {
        (*env)->DeleteLocalRef(env, jni_headers_buf);
        aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
        goto done;
    }

    /* instead of cleaning it up here, reset it in case another block is encountered */
    aws_byte_buf_reset(&binding->headers_buf, false);
    (*env)->DeleteLocalRef(env, jni_headers_buf);

    (*env)->CallVoidMethod(
        env,
        binding->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseHeadersDone,
        binding->java_http_stream_base,
        jni_block_type);

    if (aws_jni_check_and_clear_exception(env)) {
        aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
        goto done;
    }

    result = AWS_OP_SUCCESS;

done:

    aws_jni_release_thread_env(binding->jvm, env);
    /********** JNI ENV RELEASE **********/

    return result;
}

int aws_java_http_stream_on_incoming_body_fn(
    struct aws_http_stream *stream,
    const struct aws_byte_cursor *data,
    void *user_data) {
    struct http_stream_binding *binding = (struct http_stream_binding *)user_data;

    size_t total_window_increment = 0;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    int result = AWS_OP_ERR;

    jobject jni_payload = aws_jni_direct_byte_buffer_from_raw_ptr(env, data->ptr, data->len);

    jint window_increment = (*env)->CallIntMethod(
        env,
        binding->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseBody,
        binding->java_http_stream_base,
        jni_payload);

    (*env)->DeleteLocalRef(env, jni_payload);

    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Received Exception from onResponseBody", (void *)stream);
        aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
        goto done;
    }

    if (window_increment < 0) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Window Increment from onResponseBody < 0", (void *)stream);
        aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
        goto done;
    }

    total_window_increment += window_increment;

    if (total_window_increment > 0) {
        aws_http_stream_update_window(stream, total_window_increment);
    }

    result = AWS_OP_SUCCESS;

done:

    aws_jni_release_thread_env(binding->jvm, env);
    /********** JNI ENV RELEASE **********/

    return result;
}

void aws_java_http_stream_on_stream_complete_fn(struct aws_http_stream *stream, int error_code, void *user_data) {
    struct http_stream_binding *binding = (struct http_stream_binding *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    /* Don't invoke Java callbacks if Java HttpStream failed to completely setup */
    jint jErrorCode = error_code;
    (*env)->CallVoidMethod(
        env,
        binding->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseComplete,
        binding->java_http_stream_base,
        jErrorCode);

    if (aws_jni_check_and_clear_exception(env)) {
        /* Close the Connection if the Java Callback throws an Exception */
        aws_http_connection_close(aws_http_stream_get_connection(stream));
    }

    aws_jni_release_thread_env(binding->jvm, env);
    /********** JNI ENV RELEASE **********/
}

void aws_java_http_stream_on_stream_destroy_fn(void *user_data) {
    struct http_stream_binding *binding = (struct http_stream_binding *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }
    /* Native stream destroyed, release the binding. */
    aws_http_stream_binding_release(env, binding);
    aws_jni_release_thread_env(binding->jvm, env);
    /********** JNI ENV RELEASE **********/
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

    /* initial refcount created for the Java object */
    struct http_stream_binding *stream_binding = aws_http_stream_binding_new(env, jni_http_response_callback_handler);
    if (!stream_binding) {
        /* Exception already thrown */
        return (jobject)NULL;
    }

    stream_binding->native_request =
        aws_http_request_new_from_java_http_request(env, marshalled_request, jni_http_request_body_stream);
    if (stream_binding->native_request == NULL) {
        /* Exception already thrown */
        goto error;
    }

    struct aws_http_make_request_options request_options = {
        .self_size = sizeof(request_options),
        .request = stream_binding->native_request,
        /* Set Callbacks */
        .on_response_headers = aws_java_http_stream_on_incoming_headers_fn,
        .on_response_header_block_done = aws_java_http_stream_on_incoming_header_block_done_fn,
        .on_response_body = aws_java_http_stream_on_incoming_body_fn,
        .on_complete = aws_java_http_stream_on_stream_complete_fn,
        .on_destroy = aws_java_http_stream_on_stream_destroy_fn,
        .user_data = stream_binding,
    };

    stream_binding->native_stream = aws_http_connection_make_request(native_conn, &request_options);
    if (stream_binding->native_stream == NULL) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_CONNECTION, "Stream Request Failed. conn: %p", (void *)native_conn);
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Unable to Execute Request");
        goto error;
    }

    /* Stream created successfully, acquire on binding for the native stream lifetime. */
    aws_http_stream_binding_acquire(stream_binding);

    jobject jHttpStreamBase = aws_java_http_stream_from_native_new(env, stream_binding, version);
    if (jHttpStreamBase == NULL) {
        goto error;
    }

    AWS_LOGF_TRACE(
        AWS_LS_HTTP_CONNECTION,
        "Opened new Stream on Connection. conn: %p, stream: %p",
        (void *)native_conn,
        (void *)stream_binding->native_stream);

    return jHttpStreamBase;

error:
    aws_http_stream_release(stream_binding->native_stream);
    aws_http_stream_binding_release(env, stream_binding);
    return NULL;
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

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(chunked_callback_data->stream_cb_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    (*env)->CallVoidMethod(
        env,
        chunked_callback_data->completion_callback,
        http_stream_write_chunk_completion_properties.callback,
        error_code);
    aws_jni_check_and_clear_exception(env);

    JavaVM *jvm = chunked_callback_data->stream_cb_data->jvm;
    s_cleanup_chunked_callback_data(env, chunked_callback_data);
    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
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

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStreamBase_httpStreamBaseActivate(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_stream_binding,
    jobject j_http_stream_base) {
    (void)jni_class;

    struct http_stream_binding *binding = (struct http_stream_binding *)jni_stream_binding;
    struct aws_http_stream *stream = binding->native_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }

    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Activating Stream. stream: %p", (void *)stream);

    /* global ref this because now the callbacks will be firing, and they will release their reference when the
     * stream callback sequence completes. */
    binding->java_http_stream_base = (*env)->NewGlobalRef(env, j_http_stream_base);
    if (aws_http_stream_activate(stream)) {
        (*env)->DeleteGlobalRef(env, binding->java_http_stream_base);
        aws_jni_throw_runtime_exception(
            env, "HttpStream activate failed with error %s\n", aws_error_str(aws_last_error()));
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStreamBase_httpStreamBaseRelease(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_binding) {

    (void)jni_class;

    struct http_stream_binding *binding = (struct http_stream_binding *)jni_binding;
    struct aws_http_stream *stream = binding->native_stream;

    if (stream == NULL) {
        aws_jni_throw_runtime_exception(env, "HttpStream is null.");
        return;
    }
    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Releasing Stream. stream: %p", (void *)stream);
    aws_http_stream_release(stream);

    aws_http_stream_binding_release(env, binding);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_http_HttpStreamBase_httpStreamBaseGetResponseStatusCode(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_binding) {

    (void)jni_class;

    struct http_stream_binding *binding = (struct http_stream_binding *)jni_binding;
    struct aws_http_stream *stream = binding->native_stream;

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

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpStreamBase_httpStreamBaseIncrementWindow(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_binding,
    jint window_update) {

    (void)jni_class;

    struct http_stream_binding *binding = (struct http_stream_binding *)jni_binding;
    struct aws_http_stream *stream = binding->native_stream;

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

    struct http_stream_binding *binding = (struct http_stream_binding *)jni_cb_data;
    struct aws_http_stream *stream = binding->native_stream;

    if (stream == NULL) {
        aws_jni_throw_null_pointer_exception(env, "Http2Stream is null.");
        return;
    }

    AWS_LOGF_TRACE(AWS_LS_HTTP_STREAM, "Resetting Stream. stream: %p", (void *)stream);
    if (aws_http2_stream_reset(stream, error_code)) {
        aws_jni_throw_runtime_exception(
            env, "reset stream failed with error %d(%s).", aws_last_error(), aws_error_debug_str(aws_last_error()));
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

static void s_cleanup_http2_callback_data(struct aws_http2_callback_data *callback_data, JNIEnv *env) {
    if (callback_data == NULL || env == NULL) {
        return;
    }

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

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = callback_data->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    if (error_code) {
        jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, error_code);
        (*env)->CallVoidMethod(env, callback_data->async_callback, async_callback_properties.on_failure, crt_exception);
        (*env)->DeleteLocalRef(env, crt_exception);
    } else {
        (*env)->CallVoidMethod(env, callback_data->async_callback, async_callback_properties.on_success);
    }
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    s_cleanup_http2_callback_data(callback_data, env);

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
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
        s_cleanup_http2_callback_data(callback_data, env);
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

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = callback_data->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

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
    s_cleanup_http2_callback_data(callback_data, env);

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
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
        s_cleanup_http2_callback_data(callback_data, env);
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
    aws_http2_connection_send_goaway(native_conn, (uint32_t)h2_error_code, allow_more_streams, debug_cur_pointer);
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

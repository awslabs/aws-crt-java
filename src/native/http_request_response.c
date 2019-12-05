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

static jobject s_java_http_stream_from_native_new(JNIEnv *env, struct aws_http_stream *stream) {
    jlong jni_native_ptr = (jlong)stream;
    AWS_ASSERT(jni_native_ptr);
    return (*env)->NewObject(
        env, http_stream_properties.stream_class, http_stream_properties.constructor, jni_native_ptr);
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

    jobject java_http_response_stream_handler;
    jobject java_http_stream;
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

    aws_mutex_clean_up(&callback->setup_lock);

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

    aws_mutex_init(&callback->setup_lock);

    callback->java_http_response_stream_handler = (*env)->NewGlobalRef(env, java_callback_handler);
    AWS_FATAL_ASSERT(callback->java_http_response_stream_handler);

    return callback;
}

// Return whether the Java HttpStream object was successfully created.
static bool http_stream_callback_is_valid(struct http_stream_callback_data *callback) {
    // Lock to be sure that the setup attempt is finished
    aws_mutex_lock(&callback->setup_lock);
    bool is_setup = (callback->java_http_stream != NULL);
    aws_mutex_unlock(&callback->setup_lock);
    return is_setup;
}

static int s_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data) {

    struct http_stream_callback_data *callback = (struct http_stream_callback_data *)user_data;

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

    int resp_status = -1;
    int err_code = aws_http_stream_get_incoming_response_status(stream, &resp_status);
    if (err_code != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Invalid Incoming Response Status", (void *)stream);
        return AWS_OP_ERR;
    }

    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    /* All New Java Objects created through JNI have Thread-local references in the current Environment Frame, and
     * won't be eligible for GC until either DeleteLocalRef or PopLocalFrame is called.
     *
     * Capacity is multiple of 4 since we will need at minimum 4 Java Objects for a single Header:
     *   - byte[] name, byte[] val, HttpHeader, HttpHeader[]
     */
    jint frameCapacity = (jint)(num_headers * 4);
    jint result = (*env)->EnsureLocalCapacity(env, frameCapacity);

    if (result != 0) {
        AWS_LOGF_ERROR(
            AWS_LS_HTTP_STREAM, "id=%p: Failed to EnsureLocalCapacity. Possibly OutOfMemory.", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    jobjectArray jHeaders = aws_java_headers_array_from_native(env, header_array, num_headers);
    if (!jHeaders) {
        AWS_LOGF_ERROR(AWS_LS_HTTP_STREAM, "id=%p: Failed to create HttpHeaders", (void *)stream);
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    (*env)->CallVoidMethod(
        env,
        callback->java_http_response_stream_handler,
        http_stream_response_handler_properties.onResponseHeaders,
        callback->java_http_stream,
        resp_status,
        (jint)block_type,
        jHeaders);

    (*env)->DeleteLocalRef(env, jHeaders);

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

    if (!http_stream_callback_is_valid(callback)) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }

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

    // Don't invoke Java callbacks if Java HttpStream failed to completely setup
    if (http_stream_callback_is_valid(callback)) {

        jint jErrorCode = error_code;
        (*env)->CallVoidMethod(
            env,
            callback->java_http_response_stream_handler,
            http_stream_response_handler_properties.onResponseComplete,
            callback->java_http_stream,
            jErrorCode);

        if ((*env)->ExceptionCheck(env)) {
            // Close the Connection if the Java Callback throws an Exception
            aws_http_connection_close(aws_http_stream_get_connection(stream));
        }
    }

    http_stream_callback_destroy(env, callback);
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionMakeRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_method,
    jstring jni_uri,
    jobjectArray jni_headers,
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
        // Exception already thrown
        return (jobject)NULL;
    }

    callback_data->native_request = aws_http_request_new_from_java_http_request(
        env, jni_method, jni_uri, jni_headers, jni_http_request_body_stream);
    if (callback_data->native_request == NULL) {
        // Exception already thrown
        http_stream_callback_destroy(env, callback_data);
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

        jHttpStream = s_java_http_stream_from_native_new(env, native_stream);
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
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Unable to Execute Request");
        http_stream_callback_destroy(env, callback_data);
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

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/event-stream/event_stream_rpc_client.h>

#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

#include "crt.h"
#include "java_class_ids.h"

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

struct connection_callback_data {
    JavaVM *jvm;
    jobject java_connection_handler;
};

static void s_destroy_connection_callback_data(struct connection_callback_data *callback_data) {
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    if (callback_data->java_connection_handler) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_connection_handler);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static int s_on_connection_setup(
    struct aws_event_stream_rpc_client_connection *connection,
    int error_code,
    void *user_data) {
    (void)connection;
    struct connection_callback_data *callback_data = user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    (*env)->CallVoidMethod(
        env,
        callback_data->java_connection_handler,
        event_stream_client_connection_handler_properties.onSetup,
        (jlong)connection,
        error_code);

    int ret_code = aws_jni_check_and_clear_exception(env) ? AWS_OP_ERR : AWS_OP_SUCCESS;

    if (error_code || ret_code) {
        s_destroy_connection_callback_data(callback_data);
    }

    return ret_code;
}

static void s_on_connection_shutdown(
    struct aws_event_stream_rpc_client_connection *connection,
    int error_code,
    void *user_data) {
    (void)connection;

    struct connection_callback_data *callback_data = user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    (*env)->CallVoidMethod(
        env,
        callback_data->java_connection_handler,
        event_stream_client_connection_handler_properties.onClosed,
        error_code);
    aws_jni_check_and_clear_exception(env);

    s_destroy_connection_callback_data(callback_data);
}

static void s_connection_protocol_message(
    struct aws_event_stream_rpc_client_connection *connection,
    const struct aws_event_stream_rpc_message_args *message_args,
    void *user_data) {
    (void)connection;

    struct connection_callback_data *callback_data = user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    /* this is not how we recommend you use the array_list api, but it is correct, and it prevents the need for extra
     * allocations and copies. */
    struct aws_array_list headers_list;
    aws_array_list_init_static(
        &headers_list,
        message_args->headers,
        message_args->headers_count,
        sizeof(struct aws_event_stream_header_value_pair));
    headers_list.length = message_args->headers_count;

    size_t headers_buf_len = aws_event_stream_compute_headers_required_buffer_len(&headers_list);
    struct aws_byte_buf headers_buf;

    if (aws_byte_buf_init(&headers_buf, aws_jni_get_allocator(), headers_buf_len)) {
        /* TODO: this error needs to be communicated back and the connection needs to be shutdown. */
        return;
    }

    headers_buf.len = aws_event_stream_write_headers_to_buffer(&headers_list, headers_buf.buffer);
    aws_array_list_clean_up(&headers_list);

    struct aws_byte_cursor headers_cur = aws_byte_cursor_from_buf(&headers_buf);

    jbyteArray headers_byte_array = aws_jni_byte_array_from_cursor(env, &headers_cur);
    aws_byte_buf_clean_up(&headers_buf);

    struct aws_byte_cursor payload_cur = aws_byte_cursor_from_buf(message_args->payload);
    jbyteArray payload_byte_array = aws_jni_byte_array_from_cursor(env, &payload_cur);

    (*env)->CallVoidMethod(
        env,
        callback_data->java_connection_handler,
        event_stream_client_connection_handler_properties.onProtocolMessage,
        headers_byte_array,
        payload_byte_array,
        (jint)message_args->message_type,
        (jint)message_args->message_flags);
    aws_jni_check_and_clear_exception(env);
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_clientConnect(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray jni_host_name,
    jshort port,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jlong jni_client_bootstrap,
    jobject jni_client_connection_handler) {
    (void)jni_class;
    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;
    struct aws_socket_options *socket_options = (struct aws_socket_options *)jni_socket_options;
    struct aws_tls_ctx *tls_context = (struct aws_tls_ctx *)jni_tls_ctx;

    if (!client_bootstrap) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.clientConnect: Invalid ClientBootstrap");
        return AWS_OP_ERR;
    }

    if (!socket_options) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.clientConnect: Invalid SocketOptions");
        return AWS_OP_ERR;
    }

    struct aws_tls_connection_options connection_options;
    AWS_ZERO_STRUCT(connection_options);
    struct aws_tls_connection_options *conn_options_ptr = NULL;
    struct aws_string *host_name_str = NULL;

    if (tls_context) {
        aws_tls_connection_options_init_from_ctx(&connection_options, tls_context);
        conn_options_ptr = &connection_options;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct connection_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct connection_callback_data));

    if (!callback_data) {
        goto error;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.clientConnect: Unable to get JVM");
        goto error;
    }

    callback_data->java_connection_handler = (*env)->NewWeakGlobalRef(env, jni_client_connection_handler);

    if (!callback_data->java_connection_handler) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.clientConnect: Unable to create global weak ref");
        goto error;
    }

    const size_t host_name_len = (*env)->GetArrayLength(env, jni_host_name);
    jbyte *host_name = (*env)->GetPrimitiveArrayCritical(env, jni_host_name, NULL);
    host_name_str = aws_string_new_from_array(allocator, (uint8_t *)host_name, host_name_len);
    (*env)->ReleasePrimitiveArrayCritical(env, jni_host_name, host_name, 0);

    if (!host_name_str) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to allocate");
        goto error;
    }

    const char *c_str_host_name = aws_string_c_str(host_name_str);

    struct aws_event_stream_rpc_client_connection_options conn_options = {
        .socket_options = socket_options,
        .tls_options = conn_options_ptr,
        .user_data = callback_data,
        .port = (uint16_t)port,
        .bootstrap = client_bootstrap,
        .host_name = c_str_host_name,
        .on_connection_setup = s_on_connection_setup,
        .on_connection_shutdown = s_on_connection_shutdown,
        .on_connection_protocol_message = s_connection_protocol_message,
    };

    if (aws_event_stream_rpc_client_connection_connect(allocator, &conn_options)) {
        goto error;
    }

    aws_string_destroy(host_name_str);
    aws_tls_connection_options_clean_up(&connection_options);

    return AWS_OP_SUCCESS;

error:
    if (callback_data) {
        aws_mem_release(allocator, callback_data);
    }

    if (conn_options_ptr) {
        aws_tls_connection_options_clean_up(conn_options_ptr);
    }

    return AWS_OP_ERR;
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_isClientConnectionClosed(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection =
        (struct aws_event_stream_rpc_client_connection *)jni_connection;
    return aws_event_stream_rpc_client_connection_is_closed(connection);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_closeClientConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jint error_code) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection =
        (struct aws_event_stream_rpc_client_connection *)jni_connection;
    aws_event_stream_rpc_client_connection_close(connection, error_code);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_acquireClientConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection =
        (struct aws_event_stream_rpc_client_connection *)jni_connection;
    aws_event_stream_rpc_client_connection_acquire(connection);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_releaseClientConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection =
        (struct aws_event_stream_rpc_client_connection *)jni_connection;
    aws_event_stream_rpc_client_connection_release(connection);
}

struct message_flush_callback_args {
    JavaVM *jvm;
    jobject callback;
};

static void s_message_flush_fn(int error_code, void *user_data) {
    struct message_flush_callback_args *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    (*env)->CallVoidMethod(
        env, callback_data->callback, event_stream_server_message_flush_properties.callback, error_code);
    aws_jni_check_and_clear_exception(env);

    (*env)->DeleteGlobalRef(env, callback_data->callback);
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_sendProtocolMessage(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jbyteArray headers,
    jbyteArray payload,
    jint message_type,
    jint message_flags,
    jobject callback) {
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection =
        (struct aws_event_stream_rpc_client_connection *)jni_connection;

    struct message_flush_callback_args *callback_data = NULL;

    bool headers_init = false;
    struct aws_array_list headers_list;
    AWS_ZERO_STRUCT(headers_list);
    struct aws_byte_buf payload_buf;
    AWS_ZERO_STRUCT(payload_buf);
    int ret_val = AWS_OP_ERR;

    if (headers) {
        if (aws_event_stream_headers_list_init(&headers_list, aws_jni_get_allocator())) {
            return AWS_OP_ERR;
        }

        headers_init = true;

        struct aws_byte_cursor headers_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, headers);
        int headers_parse_error =
            aws_event_stream_read_headers_from_buffer(&headers_list, headers_cur.ptr, headers_cur.len);
        aws_jni_byte_cursor_from_jbyteArray_release(env, headers, headers_cur);

        if (headers_parse_error) {
            aws_jni_throw_runtime_exception(env, "ClientConnection.sendProtocolMessage: headers parse failed.");
            goto clean_up;
        }
    }

    if (payload) {
        struct aws_byte_cursor payload_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, payload);
        aws_byte_buf_init_copy_from_cursor(&payload_buf, aws_jni_get_allocator(), payload_cur);
        aws_jni_byte_cursor_from_jbyteArray_release(env, payload, payload_cur);

        if (!payload_buf.buffer) {
            aws_jni_throw_runtime_exception(env, "ClientConnection.sendProtocolMessage: allocation failed.");
            goto clean_up;
        }
    }

    struct aws_event_stream_rpc_message_args message_args = {
        .message_flags = message_flags,
        .message_type = message_type,
        .headers = headers_list.data,
        .headers_count = headers_list.length,
        .payload = &payload_buf,
    };

    callback_data = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct message_flush_callback_args));

    if (!callback_data) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.sendProtocolMessage: allocation failed.");
        goto clean_up;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.sendProtocolMessage: Unable to get JVM");
        goto clean_up;
    }

    callback_data->callback = (*env)->NewGlobalRef(env, callback);

    if (aws_event_stream_rpc_client_connection_send_protocol_message(
            connection, &message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.sendProtocolMessage: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:
    aws_byte_buf_clean_up(&payload_buf);

    if (headers_init) {
        aws_event_stream_headers_list_cleanup(&headers_list);
    }

    return ret_val;
}

struct continuation_callback_data {
    JavaVM *jvm;
    jobject java_continuation;
    jobject java_continuation_handler;
};

static void s_client_continuation_data_destroy(JNIEnv *env, struct continuation_callback_data *callback_data) {
    if (!callback_data) {
        return;
    }

    if (callback_data->java_continuation_handler) {
        (*env)->DeleteGlobalRef(env, callback_data->java_continuation_handler);
    }

    if (callback_data->java_continuation) {
        (*env)->DeleteGlobalRef(env, callback_data->java_continuation);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static void s_stream_continuation(
    struct aws_event_stream_rpc_client_continuation_token *token,
    const struct aws_event_stream_rpc_message_args *message_args,
    void *user_data) {
    (void)token;

    struct continuation_callback_data *callback_data = user_data;

    /* this is not how we recommend you use the array_list api, but it is correct, and it prevents the need for extra
     * allocations and copies. */
    struct aws_array_list headers_list;
    aws_array_list_init_static(
        &headers_list,
        message_args->headers,
        message_args->headers_count,
        sizeof(struct aws_event_stream_header_value_pair));
    headers_list.length = message_args->headers_count;

    size_t headers_buf_len = aws_event_stream_compute_headers_required_buffer_len(&headers_list);
    struct aws_byte_buf headers_buf;

    if (aws_byte_buf_init(&headers_buf, aws_jni_get_allocator(), headers_buf_len)) {
        /* TODO: this error needs to be communicated back and the connection needs to be shutdown. */
        return;
    }

    headers_buf.len = aws_event_stream_write_headers_to_buffer(&headers_list, headers_buf.buffer);
    aws_array_list_clean_up(&headers_list);

    struct aws_byte_cursor headers_cur = aws_byte_cursor_from_buf(&headers_buf);

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jbyteArray headers_byte_array = aws_jni_byte_array_from_cursor(env, &headers_cur);
    aws_byte_buf_clean_up(&headers_buf);

    struct aws_byte_cursor payload_cur = aws_byte_cursor_from_buf(message_args->payload);
    jbyteArray payload_byte_array = aws_jni_byte_array_from_cursor(env, &payload_cur);

    (*env)->CallVoidMethod(
        env,
        callback_data->java_continuation_handler,
        event_stream_client_continuation_handler_properties.onContinuationMessage,
        headers_byte_array,
        payload_byte_array,
        (jint)message_args->message_type,
        (jint)message_args->message_flags);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    aws_jni_check_and_clear_exception(env);
}

static void s_stream_continuation_closed(
    struct aws_event_stream_rpc_client_continuation_token *token,
    void *user_data) {
    (void)token;
    struct continuation_callback_data *continuation_callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(continuation_callback_data->jvm);

    (*env)->CallVoidMethod(
        env,
        continuation_callback_data->java_continuation,
        event_stream_client_continuation_handler_properties.onContinuationClosed);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    aws_jni_check_and_clear_exception(env);
    s_client_continuation_data_destroy(env, continuation_callback_data);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_newClientStream(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jobject continuation_handler) {
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection =
        (struct aws_event_stream_rpc_client_connection *)jni_connection;

    struct continuation_callback_data *continuation_callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct continuation_callback_data));

    if (!continuation_callback_data) {
        aws_event_stream_rpc_client_connection_close(connection, aws_last_error());
        return (jlong)NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &continuation_callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.newClientStream: Unable to get JVM");
        goto error;
    }

    continuation_callback_data->java_continuation_handler = (*env)->NewGlobalRef(env, continuation_handler);
    if (!continuation_callback_data->java_continuation_handler) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.newClientStream: Unable to create reference");
        goto error;
    }

    struct aws_event_stream_rpc_client_stream_continuation_options continuation_options = {
        .on_continuation_closed = s_stream_continuation_closed,
        .on_continuation = s_stream_continuation,
        .user_data = continuation_callback_data,
    };

    struct aws_event_stream_rpc_client_continuation_token *token =
        aws_event_stream_rpc_client_connection_new_stream(connection, &continuation_options);

    if (!token) {
        aws_jni_throw_runtime_exception(env, "ClientConnection.newClientStream: Unable to create stream");
        goto error;
    }

    return (jlong)token;

error:
    s_client_continuation_data_destroy(env, continuation_callback_data);
    return (jlong)NULL;
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnectionContinuation_activateContinuation(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_continuation_ptr,
    jobject continuation,
    jbyteArray operation_name,
    jbyteArray headers,
    jbyteArray payload,
    jint message_type,
    jint message_flags,
    jobject callback) {
    (void)jni_class;

    struct aws_event_stream_rpc_client_continuation_token *continuation_token =
        (struct aws_event_stream_rpc_client_continuation_token *)jni_continuation_ptr;

    struct continuation_callback_data *continuation_callback_data =
        aws_event_stream_rpc_client_continuation_get_user_data(continuation_token);

    struct message_flush_callback_args *callback_data = NULL;

    bool headers_init = false;
    struct aws_array_list headers_list;
    AWS_ZERO_STRUCT(headers_list);
    struct aws_byte_buf payload_buf;
    AWS_ZERO_STRUCT(payload_buf);
    struct aws_byte_buf operation_buf;
    AWS_ZERO_STRUCT(operation_buf);

    int ret_val = AWS_OP_ERR;

    continuation_callback_data->java_continuation = (*env)->NewGlobalRef(env, continuation);

    if (!continuation_callback_data->java_continuation) {
        aws_jni_throw_runtime_exception(
            env, "ClientConnectionContinuation.activateContinuation: Unable to create reference");
        goto clean_up;
    }

    if (headers) {
        if (aws_event_stream_headers_list_init(&headers_list, aws_jni_get_allocator())) {
            return AWS_OP_ERR;
        }

        headers_init = true;

        struct aws_byte_cursor headers_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, headers);
        int headers_parse_error =
            aws_event_stream_read_headers_from_buffer(&headers_list, headers_cur.ptr, headers_cur.len);
        aws_jni_byte_cursor_from_jbyteArray_release(env, headers, headers_cur);

        if (headers_parse_error) {
            goto clean_up;
        }
    }

    if (payload) {
        struct aws_byte_cursor payload_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, payload);
        aws_byte_buf_init_copy_from_cursor(&payload_buf, aws_jni_get_allocator(), payload_cur);
        aws_jni_byte_cursor_from_jbyteArray_release(env, payload, payload_cur);

        if (!payload_buf.buffer) {
            aws_jni_throw_runtime_exception(
                env, "ClientConnectionContinuation.activateContinuation: allocation failed.");
            goto clean_up;
        }
    }

    struct aws_event_stream_rpc_message_args message_args = {
        .message_flags = message_flags,
        .message_type = message_type,
        .headers = headers_list.data,
        .headers_count = headers_list.length,
        .payload = &payload_buf,
    };

    if (operation_name) {
        struct aws_byte_cursor operation_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, operation_name);
        aws_byte_buf_init_copy_from_cursor(&operation_buf, aws_jni_get_allocator(), operation_cur);
        aws_jni_byte_cursor_from_jbyteArray_release(env, payload, operation_cur);

        if (!operation_buf.buffer) {
            aws_jni_throw_runtime_exception(
                env, "ClientConnectionContinuation.activateContinuation: allocation failed.");
            goto clean_up;
        }
    }

    callback_data = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct message_flush_callback_args));

    if (!callback_data) {
        aws_jni_throw_runtime_exception(env, "ClientConnectionContinuation.activateContinuation: allocation failed.");
        goto clean_up;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ClientConnectionContinuation.activateContinuation: Unable to get JVM");
        goto clean_up;
    }

    callback_data->callback = (*env)->NewGlobalRef(env, callback);

    struct aws_byte_cursor operation_cursor = aws_byte_cursor_from_buf(&operation_buf);

    if (aws_event_stream_rpc_client_continuation_activate(
            continuation_token, operation_cursor, &message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(env, "ClientConnectionContinuation.activateContinuation: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:
    aws_byte_buf_clean_up(&payload_buf);
    aws_byte_buf_clean_up(&operation_buf);

    if (headers_init) {
        aws_event_stream_headers_list_cleanup(&headers_list);
    }

    if (callback_data && ret_val) {
        (*env)->DeleteGlobalRef(env, callback_data->callback);
        aws_mem_release(aws_jni_get_allocator(), callback_data);
    }
    return ret_val;
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnectionContinuation_sendContinuationMessage(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_continuation_ptr,
    jbyteArray headers,
    jbyteArray payload,
    jint message_type,
    jint message_flags,
    jobject callback) {
    (void)jni_class;

    struct aws_event_stream_rpc_client_continuation_token *continuation_token =
        (struct aws_event_stream_rpc_client_continuation_token *)jni_continuation_ptr;

    struct message_flush_callback_args *callback_data = NULL;
    bool headers_init = false;
    struct aws_array_list headers_list;
    AWS_ZERO_STRUCT(headers_list);
    struct aws_byte_buf payload_buf;
    AWS_ZERO_STRUCT(payload_buf);

    int ret_val = AWS_OP_ERR;

    if (headers) {
        if (aws_event_stream_headers_list_init(&headers_list, aws_jni_get_allocator())) {
            return AWS_OP_ERR;
        }

        headers_init = true;

        struct aws_byte_cursor headers_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, headers);
        int headers_parse_error =
            aws_event_stream_read_headers_from_buffer(&headers_list, headers_cur.ptr, headers_cur.len);
        aws_jni_byte_cursor_from_jbyteArray_release(env, headers, headers_cur);

        if (headers_parse_error) {
            goto clean_up;
        }
    }

    if (payload) {
        struct aws_byte_cursor payload_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, payload);
        aws_byte_buf_init_copy_from_cursor(&payload_buf, aws_jni_get_allocator(), payload_cur);
        aws_jni_byte_cursor_from_jbyteArray_release(env, payload, payload_cur);

        if (!payload_buf.buffer) {
            aws_jni_throw_runtime_exception(
                env, "ClientConnectionContinuation.sendContinuationMessage: allocation failed.");
            goto clean_up;
        }
    }

    struct aws_event_stream_rpc_message_args message_args = {
        .message_flags = message_flags,
        .message_type = message_type,
        .headers = headers_list.data,
        .headers_count = headers_list.length,
        .payload = &payload_buf,
    };

    callback_data = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct message_flush_callback_args));

    if (!callback_data) {
        aws_jni_throw_runtime_exception(
            env, "ClientConnectionContinuation.sendContinuationMessage: allocation failed.");
        goto clean_up;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ClientConnectionContinuation.sendContinuationMessage: Unable to get JVM");
        goto clean_up;
    }

    callback_data->callback = (*env)->NewGlobalRef(env, callback);

    if (aws_event_stream_rpc_client_continuation_send_message(
            continuation_token, &message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(
            env, "ClientConnectionContinuation.sendContinuationMessage: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:
    if (headers_init) {
        aws_event_stream_headers_list_cleanup(&headers_list);
    }

    aws_byte_buf_clean_up(&payload_buf);

    if (callback_data && ret_val) {
        (*env)->DeleteGlobalRef(env, callback_data->callback);
        aws_mem_release(aws_jni_get_allocator(), callback_data);
    }
    return ret_val;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnectionContinuation_releaseContinuation(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_continuation_ptr) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_continuation_token *continuation_token =
        (struct aws_event_stream_rpc_client_continuation_token *)jni_continuation_ptr;

    aws_event_stream_rpc_client_continuation_release(continuation_token);
}

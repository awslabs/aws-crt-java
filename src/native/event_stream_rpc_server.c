/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/event-stream/event_stream_rpc_server.h>

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

struct shutdown_callback_data {
    JavaVM *jvm;
    jweak java_server_listener;
    jobject java_listener_handler;
};

static void s_shutdown_callback_data_destroy(JNIEnv *env, struct shutdown_callback_data *callback_data) {
    if (!callback_data) {
        return;
    }

    if (callback_data->java_server_listener) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_server_listener);
    }

    if (callback_data->java_listener_handler) {
        (*env)->DeleteGlobalRef(env, callback_data->java_listener_handler);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

struct connection_callback_data {
    JavaVM *jvm;
    jobject java_server_connection;
    jweak java_listener_handler;
    jobject java_connection_handler;
};

static void s_server_connection_data_destroy(JNIEnv *env, struct connection_callback_data *callback_data) {
    if (!callback_data) {
        return;
    }

    if (callback_data->java_listener_handler) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_listener_handler);
    }

    if (callback_data->java_server_connection) {
        (*env)->DeleteGlobalRef(env, callback_data->java_server_connection);
    }
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static void s_server_listener_shutdown_complete(
    struct aws_event_stream_rpc_server_listener *listener,
    void *user_data) {
    (void)listener;

    struct shutdown_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jobject java_server_listener = (*env)->NewLocalRef(env, callback_data->java_server_listener);
    if (java_server_listener) {
        (*env)->CallVoidMethod(env, java_server_listener, event_stream_server_listener_properties.onShutdownComplete);
        (*env)->DeleteLocalRef(env, java_server_listener);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }

    s_shutdown_callback_data_destroy(env, callback_data);
}

struct continuation_callback_data {
    JavaVM *jvm;
    jobject java_continuation;
    jobject java_continuation_handler;
};

static void s_server_continuation_data_destroy(JNIEnv *env, struct continuation_callback_data *callback_data) {
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

static void s_stream_continuation_fn(
    struct aws_event_stream_rpc_server_continuation_token *token,
    const struct aws_event_stream_rpc_message_args *message_args,
    void *user_data) {
    (void)token;
    struct continuation_callback_data *callback_data = user_data;
    struct aws_array_list headers_list;
    aws_array_list_init_static(
        &headers_list,
        message_args->headers,
        message_args->headers_count,
        sizeof(struct aws_event_stream_header_value_pair));
    headers_list.length = message_args->headers_count;

    size_t headers_buf_len = aws_event_stream_compute_headers_required_buffer_len(&headers_list);
    struct aws_byte_buf headers_buf;
    aws_byte_buf_init(&headers_buf, aws_jni_get_allocator(), headers_buf_len);
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
        event_stream_server_connection_handler_properties.onProtocolMessage,
        headers_byte_array,
        payload_byte_array,
        (jint)message_args->message_type,
        (jint)message_args->message_flags);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    (void)(*env)->ExceptionCheck(env);
    (*env)->DeleteLocalRef(env, payload_byte_array);
    (*env)->DeleteLocalRef(env, headers_byte_array);
}

static void s_stream_continuation_closed_fn(
    struct aws_event_stream_rpc_server_continuation_token *token,
    void *user_data) {
    (void)token;
    struct continuation_callback_data *continuation_callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(continuation_callback_data->jvm);

    (*env)->CallVoidMethod(
        env,
        continuation_callback_data->java_continuation,
        event_stream_server_continuation_handler_properties.onContinuationClosed);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    (void)(*env)->ExceptionCheck(env);
    s_server_continuation_data_destroy(env, continuation_callback_data);
}

static void s_on_incoming_stream_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    struct aws_event_stream_rpc_server_continuation_token *token,
    struct aws_byte_cursor operation_name,
    struct aws_event_stream_rpc_server_stream_continuation_options *continuation_options,
    void *user_data) {
    (void)connection;

    struct connection_callback_data *callback_data = user_data;

    struct continuation_callback_data *continuation_callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct continuation_callback_data));

    continuation_callback_data->jvm = callback_data->jvm;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    continuation_callback_data->java_continuation = (*env)->NewObject(
        env,
        event_stream_server_connection_handler_properties.continuationCls,
        event_stream_server_connection_handler_properties.newContinuationConstructor,
        (jlong)token);

    (void)(*env)->ExceptionCheck(env);

    if (!continuation_callback_data->java_continuation) {
        // TODO: send a legit server error message.
        aws_event_stream_rpc_server_connection_close(connection, aws_last_error());
        s_server_connection_data_destroy(env, callback_data);
        return;
    }

    jbyteArray operationNameArray = aws_jni_byte_array_from_cursor(env, &operation_name);

    continuation_callback_data->java_continuation_handler = (*env)->CallObjectMethod(
        env,
        callback_data->java_connection_handler,
        event_stream_server_connection_handler_properties.onIncomingStream,
        continuation_callback_data->java_continuation,
        operationNameArray);
    (*env)->DeleteLocalRef(env, operationNameArray);

    continuation_options->user_data = continuation_callback_data;
    continuation_options->on_continuation = s_stream_continuation_fn;
    continuation_options->on_continuation_closed = s_stream_continuation_closed_fn;
}

static void s_connection_protocol_message_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    const struct aws_event_stream_rpc_message_args *message_args,
    void *user_data) {
    (void)connection;

    struct connection_callback_data *callback_data = user_data;
    struct aws_array_list headers_list;
    aws_array_list_init_static(
        &headers_list,
        message_args->headers,
        message_args->headers_count,
        sizeof(struct aws_event_stream_header_value_pair));
    headers_list.length = message_args->headers_count;

    uint32_t headers_buf_len = aws_event_stream_compute_headers_required_buffer_len(&headers_list);

    struct aws_byte_buf headers_buf;
    aws_byte_buf_init(&headers_buf, aws_jni_get_allocator(), headers_buf_len);
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
        callback_data->java_connection_handler,
        event_stream_server_connection_handler_properties.onProtocolMessage,
        headers_byte_array,
        payload_byte_array,
        (jint)message_args->message_type,
        (jint)message_args->message_flags);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    (void)(*env)->ExceptionCheck(env);
    (*env)->DeleteLocalRef(env, payload_byte_array);
    (*env)->DeleteLocalRef(env, headers_byte_array);
}

static int s_on_new_connection_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    int error_code,
    struct aws_event_stream_rpc_connection_options *connection_options,
    void *user_data) {

    struct shutdown_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    struct connection_callback_data *connection_callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct connection_callback_data));

    if (!connection_callback_data) {
        goto error;
    }

    connection_callback_data->jvm = callback_data->jvm;
    connection_callback_data->java_listener_handler =
        (*env)->NewWeakGlobalRef(env, callback_data->java_listener_handler);

    jobject java_server_connection = NULL;
    if (!error_code) {
        java_server_connection = (*env)->NewObject(
            env,
            event_stream_server_listener_handler_properties.connCls,
            event_stream_server_listener_handler_properties.newConnConstructor,
            connection);
        if ((*env)->ExceptionCheck(env)) {
            goto error;
        }

        connection_callback_data->java_server_connection = (*env)->NewGlobalRef(env, java_server_connection);
    }

    jobject java_connection_handler = (*env)->CallObjectMethod(
        env,
        connection_callback_data->java_listener_handler,
        event_stream_server_listener_handler_properties.onNewConnection,
        java_server_connection,
        error_code);

    if (!java_connection_handler) {
        goto error;
    }

    connection_callback_data->java_connection_handler = (*env)->NewGlobalRef(env, java_connection_handler);

    /* we got an object back so this shouldn't be possible. */
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    connection_options->on_connection_protocol_message = s_connection_protocol_message_fn;
    connection_options->on_incoming_stream = s_on_incoming_stream_fn;
    connection_options->user_data = connection_callback_data;

    return AWS_OP_SUCCESS;

error:
    if (connection_callback_data) {
        if (connection_callback_data->java_server_connection) {
            (*env)->DeleteGlobalRef(env, connection_callback_data->java_server_connection);
        }

        if (connection_callback_data->java_listener_handler) {
            (*env)->DeleteWeakGlobalRef(env, connection_callback_data->java_listener_handler);
        }
        aws_mem_release(aws_jni_get_allocator(), connection_callback_data);
    }

    return AWS_OP_ERR;
}

static void s_on_connection_shutdown_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    int error_code,
    void *user_data) {
    (void)user_data;

    struct connection_callback_data *callback_data = aws_event_stream_rpc_server_connection_get_user_data(connection);

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    jobject java_listener_handler = (*env)->NewLocalRef(env, callback_data->java_listener_handler);
    jobject java_server_connection = (*env)->NewLocalRef(env, callback_data->java_server_connection);

    AWS_FATAL_ASSERT(java_listener_handler);
    AWS_FATAL_ASSERT(java_server_connection);

    /* Tell the Java ListenerHandler that the connection shutdown. */
    (*env)->CallVoidMethod(
        env,
        java_listener_handler,
        event_stream_server_listener_handler_properties.onConnectionShutdown,
        java_server_connection,
        error_code);

    (*env)->DeleteLocalRef(env, java_server_connection);
    (*env)->DeleteLocalRef(env, java_listener_handler);

    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    /* this is the should be connection specific callback data. */
    s_server_connection_data_destroy(env, callback_data);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerListener_serverListenerNew(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_server_listener,
    jbyteArray jni_host_name,
    jshort port,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jlong jni_server_bootstrap,
    jobject jni_server_listener_handler) {
    (void)jni_class;
    struct aws_server_bootstrap *server_bootstrap = (struct aws_server_bootstrap *)jni_server_bootstrap;
    struct aws_socket_options *socket_options = (struct aws_socket_options *)jni_socket_options;
    struct aws_tls_ctx *tls_context = (struct aws_tls_ctx *)jni_tls_ctx;

    if (!server_bootstrap) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Invalid ServerBootstrap");
        return (jlong)NULL;
    }

    if (!socket_options) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Invalid SocketOptions");
        return (jlong)NULL;
    }

    struct aws_tls_connection_options connection_options;
    AWS_ZERO_STRUCT(connection_options);
    struct aws_tls_connection_options *conn_options_ptr = NULL;

    if (tls_context) {
        aws_tls_connection_options_init_from_ctx(&connection_options, tls_context);
        conn_options_ptr = &connection_options;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct shutdown_callback_data *callback_data = aws_mem_calloc(allocator, 1, sizeof(struct shutdown_callback_data));
    if (!callback_data) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to allocate");
        return (jlong)NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to get JVM");
        goto error;
    }

    callback_data->java_server_listener = (*env)->NewWeakGlobalRef(env, jni_server_listener);
    if (!callback_data->java_server_listener) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to create global weak ref");
        goto error;
    }

    callback_data->java_listener_handler = (*env)->NewGlobalRef(env, jni_server_listener_handler);
    if (!callback_data->java_listener_handler) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to create global ref");
        goto error;
    }

    const size_t host_name_len = (*env)->GetArrayLength(env, jni_host_name);
    jbyte *host_name = (*env)->GetPrimitiveArrayCritical(env, jni_host_name, NULL);
    struct aws_string *host_name_str = aws_string_new_from_array(allocator, (uint8_t *)host_name, host_name_len);
    (*env)->ReleasePrimitiveArrayCritical(env, jni_host_name, host_name, 0);

    if (!host_name_str) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to allocate");
        goto error;
    }

    const char *c_str_host_name = aws_string_c_str(host_name_str);

    struct aws_event_stream_rpc_server_listener_options listener_options = {
        .socket_options = socket_options,
        .on_destroy_callback = s_server_listener_shutdown_complete,
        .bootstrap = server_bootstrap,
        .tls_options = conn_options_ptr,
        .port = port,
        .host_name = c_str_host_name,
        .user_data = callback_data,
        .on_new_connection = s_on_new_connection_fn,
        .on_connection_shutdown = s_on_connection_shutdown_fn,
    };

    struct aws_event_stream_rpc_server_listener *listener =
        aws_event_stream_rpc_server_new_listener(allocator, &listener_options);
    aws_string_destroy(host_name_str);
    if (!listener) {
        aws_jni_throw_runtime_exception(
            env, "ServerBootstrap.server_bootstrap_new: Unable to allocate new aws_event_stream_rpc_server_listener");
        goto error;
    }

    return (jlong)listener;

error:
    s_shutdown_callback_data_destroy(env, callback_data);
    return (jlong)NULL;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerListener_release(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_listener) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_listener *listener =
        (struct aws_event_stream_rpc_server_listener *)jni_server_listener;
    if (!listener) {
        return;
    }

    aws_event_stream_rpc_server_listener_release(listener);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnection_acquire(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_connection) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_connection *connection =
        (struct aws_event_stream_rpc_server_connection *)jni_server_connection;
    aws_event_stream_rpc_server_connection_acquire(connection);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnection_release(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_connection) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_connection *connection =
        (struct aws_event_stream_rpc_server_connection *)jni_server_connection;
    aws_event_stream_rpc_server_connection_release(connection);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnection_closeConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_connection,
    jint error_code) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_connection *connection =
        (struct aws_event_stream_rpc_server_connection *)jni_server_connection;
    aws_event_stream_rpc_server_connection_close(connection, error_code);
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnection_isClosed(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_connection) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_connection *connection =
        (struct aws_event_stream_rpc_server_connection *)jni_server_connection;
    return aws_event_stream_rpc_server_connection_is_closed(connection);
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
    (*env)->DeleteGlobalRef(env, callback_data->callback);
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnection_sendProtocolMessage(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_connection,
    jbyteArray headers,
    jbyteArray payload,
    jint message_type,
    jint message_flags,
    jobject callback) {
    (void)jni_class;
    struct aws_event_stream_rpc_server_connection *connection =
        (struct aws_event_stream_rpc_server_connection *)jni_server_connection;

    struct message_flush_callback_args *callback_data = NULL;
    jbyte *payload_ptr = NULL;
    jbyte *headers_ptr = NULL;
    struct aws_array_list headers_list;
    if (aws_event_stream_headers_list_init(&headers_list, aws_jni_get_allocator())) {
        return AWS_OP_ERR;
    }

    int ret_val = AWS_OP_ERR;

    const size_t headers_len = (*env)->GetArrayLength(env, headers);
    headers_ptr = (*env)->GetPrimitiveArrayCritical(env, headers, NULL);
    int headers_parse_error =
        aws_event_stream_read_headers_from_buffer(&headers_list, (uint8_t *)headers_ptr, headers_len);

    if (headers_parse_error) {
        goto clean_up;
    }

    const size_t payload_len = (*env)->GetArrayLength(env, payload);
    payload_ptr = (*env)->GetPrimitiveArrayCritical(env, payload, NULL);

    struct aws_byte_buf payload_buf = aws_byte_buf_from_array(payload_ptr, payload_len);

    struct aws_event_stream_rpc_message_args message_args = {
        .message_flags = message_flags,
        .message_type = message_type,
        .headers = headers_list.data,
        .headers_count = headers_list.length,
        .payload = &payload_buf,
    };

    callback_data = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct message_flush_callback_args));

    if (!callback_data) {
        aws_jni_throw_runtime_exception(env, "ServerConnection.sendProtocolMessage: allocation failed.");
        goto clean_up;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ServerConnection.sendProtocolMessage: Unable to get JVM");
        goto clean_up;
    }

    callback_data->callback = (*env)->NewGlobalRef(env, callback);

    if (aws_event_stream_rpc_server_connection_send_protocol_message(
            connection, &message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(env, "ServerConnection.sendProtocolMessage: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:
    if (payload_ptr) {
        (*env)->ReleasePrimitiveArrayCritical(env, payload, payload_ptr, 0);
    }
    if (headers_ptr) {
        (*env)->ReleasePrimitiveArrayCritical(env, headers, headers_ptr, 0);
    }
    aws_event_stream_headers_list_cleanup(&headers_list);

    return ret_val;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnectionContinuation_acquire(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_continuation) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_continuation_token *continuation =
        (struct aws_event_stream_rpc_server_continuation_token *)jni_server_continuation;
    aws_event_stream_rpc_server_continuation_acquire(continuation);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnectionContinuation_release(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_continuation) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_continuation_token *continuation =
        (struct aws_event_stream_rpc_server_continuation_token *)jni_server_continuation;
    aws_event_stream_rpc_server_continuation_release(continuation);
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnectionContinuation_isClosed(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_continuation) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_continuation_token *continuation =
        (struct aws_event_stream_rpc_server_continuation_token *)jni_server_continuation;
    return aws_event_stream_rpc_server_continuation_is_closed(continuation);
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnectionContinuation_sendContinuationMessage(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_continuation,
    jbyteArray headers,
    jbyteArray payload,
    jint message_type,
    jint message_flags,
    jobject callback) {
    (void)jni_class;
    struct aws_event_stream_rpc_server_continuation_token *continuation =
        (struct aws_event_stream_rpc_server_continuation_token *)jni_server_continuation;

    struct message_flush_callback_args *callback_data = NULL;
    jbyte *payload_ptr = NULL;
    jbyte *headers_ptr = NULL;
    struct aws_array_list headers_list;
    if (aws_event_stream_headers_list_init(&headers_list, aws_jni_get_allocator())) {
        return AWS_OP_ERR;
    }

    int ret_val = AWS_OP_ERR;

    const size_t headers_len = (*env)->GetArrayLength(env, headers);
    headers_ptr = (*env)->GetPrimitiveArrayCritical(env, headers, NULL);
    int headers_parse_error =
        aws_event_stream_read_headers_from_buffer(&headers_list, (uint8_t *)headers_ptr, headers_len);

    if (headers_parse_error) {
        goto clean_up;
    }

    const size_t payload_len = (*env)->GetArrayLength(env, payload);
    payload_ptr = (*env)->GetPrimitiveArrayCritical(env, payload, NULL);

    struct aws_byte_buf payload_buf = aws_byte_buf_from_array(payload_ptr, payload_len);

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
            env, "ServerConnectionContinuation.sendContinuationMessage: allocation failed.");
        goto clean_up;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ServerConnectionContinuation.sendContinuationMessage: Unable to get JVM");
        goto clean_up;
    }

    callback_data->callback = (*env)->NewGlobalRef(env, callback);

    if (aws_event_stream_rpc_server_continuation_send_message(
            continuation, &message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(
            env, "ServerConnectionContinuation.sendContinuationMessage: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:
    if (payload_ptr) {
        (*env)->ReleasePrimitiveArrayCritical(env, payload, payload_ptr, 0);
    }
    if (headers_ptr) {
        (*env)->ReleasePrimitiveArrayCritical(env, headers, headers_ptr, 0);
    }
    aws_event_stream_headers_list_cleanup(&headers_list);

    return ret_val;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

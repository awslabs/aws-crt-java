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

    int ret_code = (*env)->ExceptionCheck(env) ? AWS_OP_ERR : AWS_OP_SUCCESS;

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
    (*env)->ExceptionCheck(env);

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
        (jint)message_args->message_flags
        );
    (void)(*env)->ExceptionCheck(env);
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
    struct connection_callback_data *callback_data = aws_mem_calloc(allocator, 1, sizeof(struct connection_callback_data));

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
    jlong jni_connection
    ) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection = (struct aws_event_stream_rpc_client_connection *)jni_connection;
    return aws_event_stream_rpc_client_connection_is_closed(connection);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_closeConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jint error_code) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection = (struct aws_event_stream_rpc_client_connection *)jni_connection;
    aws_event_stream_rpc_client_connection_close(connection, error_code);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_acquireClientConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection
    ) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection = (struct aws_event_stream_rpc_client_connection *)jni_connection;
    aws_event_stream_rpc_client_connection_acquire(connection);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_ClientConnection_releaseClientConnection(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection
) {
    (void)env;
    (void)jni_class;

    struct aws_event_stream_rpc_client_connection *connection = (struct aws_event_stream_rpc_client_connection *)jni_connection;
    aws_event_stream_rpc_client_connection_release(connection);
}


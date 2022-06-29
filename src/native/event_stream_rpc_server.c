/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/event-stream/event_stream_rpc_server.h>

#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

#include "crt.h"
#include "event_stream_message.h"
#include "java_class_ids.h"

#if defined(_MSC_VER)
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
        (*env)->DeleteGlobalRef(env, callback_data->java_listener_handler);
    }

    if (callback_data->java_server_connection) {
        (*env)->DeleteGlobalRef(env, callback_data->java_server_connection);
    }

    if (callback_data->java_connection_handler) {
        (*env)->DeleteGlobalRef(env, callback_data->java_connection_handler);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static void s_server_listener_shutdown_complete(
    struct aws_event_stream_rpc_server_listener *listener,
    void *user_data) {
    (void)listener;

    struct shutdown_callback_data *callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jobject java_server_listener = (*env)->NewLocalRef(env, callback_data->java_server_listener);
    if (java_server_listener) {
        (*env)->CallVoidMethod(env, java_server_listener, event_stream_server_listener_properties.onShutdownComplete);
        aws_jni_check_and_clear_exception(env);
        (*env)->DeleteLocalRef(env, java_server_listener);
    }

    JavaVM *jvm = callback_data->jvm;
    s_shutdown_callback_data_destroy(env, callback_data);
    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
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

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jbyteArray headers_array = aws_event_stream_rpc_marshall_headers_to_byteArray(
        aws_jni_get_allocator(), env, message_args->headers, message_args->headers_count);

    struct aws_byte_cursor payload_cur = aws_byte_cursor_from_buf(message_args->payload);
    jbyteArray payload_byte_array = aws_jni_byte_array_from_cursor(env, &payload_cur);
    (*env)->CallVoidMethod(
        env,
        callback_data->java_continuation_handler,
        event_stream_server_continuation_handler_properties.onContinuationMessage,
        headers_array,
        payload_byte_array,
        (jint)message_args->message_type,
        (jint)message_args->message_flags);
    (*env)->DeleteLocalRef(env, headers_array);
    (*env)->DeleteLocalRef(env, payload_byte_array);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    aws_jni_check_and_clear_exception(env);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/
}

static void s_stream_continuation_closed_fn(
    struct aws_event_stream_rpc_server_continuation_token *token,
    void *user_data) {
    (void)token;
    struct continuation_callback_data *continuation_callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(continuation_callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    (*env)->CallVoidMethod(
        env,
        continuation_callback_data->java_continuation_handler,
        event_stream_server_continuation_handler_properties.onContinuationClosed);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    aws_jni_check_and_clear_exception(env);

    JavaVM *jvm = continuation_callback_data->jvm;
    s_server_continuation_data_destroy(env, continuation_callback_data);
    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
}

static int s_on_incoming_stream_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    struct aws_event_stream_rpc_server_continuation_token *token,
    struct aws_byte_cursor operation_name,
    struct aws_event_stream_rpc_server_stream_continuation_options *continuation_options,
    void *user_data) {
    (void)connection;

    struct connection_callback_data *callback_data = user_data;

    jobject java_continuation = NULL;
    jobject java_continuation_handler = NULL;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    struct continuation_callback_data *continuation_callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct continuation_callback_data));

    if (!continuation_callback_data) {
        goto on_error;
    }

    continuation_callback_data->jvm = callback_data->jvm;

    java_continuation = (*env)->NewObject(
        env,
        event_stream_server_connection_handler_properties.continuationCls,
        event_stream_server_connection_handler_properties.newContinuationConstructor,
        (jlong)token);

    aws_jni_check_and_clear_exception(env);

    if (!java_continuation) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        goto on_error;
    }

    continuation_callback_data->java_continuation = (*env)->NewGlobalRef(env, java_continuation);
    if (continuation_callback_data->java_continuation == NULL) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        goto on_error;
    }

    java_continuation_handler = NULL;
    jbyteArray operation_name_array = aws_jni_byte_array_from_cursor(env, &operation_name);
    if (operation_name_array != NULL) {
        java_continuation_handler = (*env)->CallObjectMethod(
            env,
            callback_data->java_connection_handler,
            event_stream_server_connection_handler_properties.onIncomingStream,
            java_continuation,
            operation_name_array);
        (*env)->DeleteLocalRef(env, operation_name_array);
        aws_jni_check_and_clear_exception(env);
    }

    if (!java_continuation_handler) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        goto on_error;
    }

    continuation_callback_data->java_continuation_handler = (*env)->NewGlobalRef(env, java_continuation_handler);
    if (continuation_callback_data->java_continuation_handler == NULL) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        goto on_error;
    }

    continuation_options->user_data = continuation_callback_data;
    continuation_options->on_continuation = s_stream_continuation_fn;
    continuation_options->on_continuation_closed = s_stream_continuation_closed_fn;

    (*env)->DeleteLocalRef(env, java_continuation_handler);
    (*env)->DeleteLocalRef(env, java_continuation);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE SUCCESS PATH **********/

    return AWS_OP_SUCCESS;

on_error:

    if (java_continuation_handler != NULL) {
        (*env)->DeleteLocalRef(env, java_continuation_handler);
    }

    if (java_continuation != NULL) {
        (*env)->DeleteLocalRef(env, java_continuation);
    }

    aws_event_stream_rpc_server_connection_close(connection, aws_last_error());
    s_server_continuation_data_destroy(env, continuation_callback_data);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE FAILURE PATH **********/

    return AWS_OP_ERR;
}

static void s_connection_protocol_message_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    const struct aws_event_stream_rpc_message_args *message_args,
    void *user_data) {
    (void)connection;

    struct connection_callback_data *callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jbyteArray headers_array = aws_event_stream_rpc_marshall_headers_to_byteArray(
        aws_jni_get_allocator(), env, message_args->headers, message_args->headers_count);

    struct aws_byte_cursor payload_cur = aws_byte_cursor_from_buf(message_args->payload);
    jbyteArray payload_byte_array = aws_jni_byte_array_from_cursor(env, &payload_cur);

    (*env)->CallVoidMethod(
        env,
        callback_data->java_connection_handler,
        event_stream_server_connection_handler_properties.onProtocolMessage,
        headers_array,
        payload_byte_array,
        (jint)message_args->message_type,
        (jint)message_args->message_flags);
    (*env)->DeleteLocalRef(env, headers_array);
    (*env)->DeleteLocalRef(env, payload_byte_array);
    /* don't really care if they threw here, but we want to make the jvm happy that we checked */
    aws_jni_check_and_clear_exception(env);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/
}

static int s_on_new_connection_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    int error_code,
    struct aws_event_stream_rpc_connection_options *connection_options,
    void *user_data) {

    struct shutdown_callback_data *callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    jobject java_server_connection = NULL;
    jobject java_connection_handler = NULL;

    struct connection_callback_data *connection_callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct connection_callback_data));

    if (!connection_callback_data) {
        goto error;
    }

    connection_callback_data->jvm = callback_data->jvm;
    connection_callback_data->java_listener_handler = (*env)->NewGlobalRef(env, callback_data->java_listener_handler);

    if (!error_code) {
        java_server_connection = (*env)->NewObject(
            env,
            event_stream_server_listener_handler_properties.connCls,
            event_stream_server_listener_handler_properties.newConnConstructor,
            (jlong)connection);
        if (aws_jni_check_and_clear_exception(env) || java_server_connection == NULL) {
            aws_raise_error(AWS_ERROR_INVALID_STATE);
            goto error;
        }

        connection_callback_data->java_server_connection = (*env)->NewGlobalRef(env, java_server_connection);
    }

    java_connection_handler = (*env)->CallObjectMethod(
        env,
        connection_callback_data->java_listener_handler,
        event_stream_server_listener_handler_properties.onNewConnection,
        java_server_connection,
        error_code);

    /* we check whether the function succeeded when we do the null check on java_connection_handler below */
    aws_jni_check_and_clear_exception(env);

    if (!java_connection_handler) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        goto error;
    }

    connection_callback_data->java_connection_handler = (*env)->NewGlobalRef(env, java_connection_handler);
    if (connection_callback_data->java_connection_handler == NULL) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        goto error;
    }

    connection_options->on_connection_protocol_message = s_connection_protocol_message_fn;
    connection_options->on_incoming_stream = s_on_incoming_stream_fn;
    connection_options->user_data = connection_callback_data;

    (*env)->DeleteLocalRef(env, java_connection_handler);
    (*env)->DeleteLocalRef(env, java_server_connection);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE SUCCESS PATH **********/

    return AWS_OP_SUCCESS;

error:

    if (java_connection_handler != NULL) {
        (*env)->DeleteLocalRef(env, java_connection_handler);
    }

    if (java_server_connection != NULL) {
        (*env)->DeleteLocalRef(env, java_server_connection);
    }

    s_server_connection_data_destroy(env, connection_callback_data);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE ERROR PATH **********/

    return AWS_OP_ERR;
}

static void s_on_connection_shutdown_fn(
    struct aws_event_stream_rpc_server_connection *connection,
    int error_code,
    void *user_data) {
    (void)user_data;

    struct connection_callback_data *callback_data = aws_event_stream_rpc_server_connection_get_user_data(connection);

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

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
    aws_jni_check_and_clear_exception(env);

    (*env)->DeleteLocalRef(env, java_server_connection);
    (*env)->DeleteLocalRef(env, java_listener_handler);

    /* this is the should be connection specific callback data. */
    JavaVM *jvm = callback_data->jvm;
    s_server_connection_data_destroy(env, callback_data);
    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
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
    struct aws_string *host_name_str = NULL;

    if (tls_context) {
        aws_tls_connection_options_init_from_ctx(&connection_options, tls_context);
        conn_options_ptr = &connection_options;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct shutdown_callback_data *callback_data = aws_mem_calloc(allocator, 1, sizeof(struct shutdown_callback_data));
    if (!callback_data) {
        aws_jni_throw_runtime_exception(env, "ServerListener.server_listener_new: Unable to allocate");
        goto error;
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
    host_name_str = aws_string_new_from_array(allocator, (uint8_t *)host_name, host_name_len);
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
    host_name_str = NULL;

    if (!listener) {
        aws_jni_throw_runtime_exception(
            env, "ServerBootstrap.server_bootstrap_new: Unable to allocate new aws_event_stream_rpc_server_listener");
        goto error;
    }

    return (jlong)listener;

error:
    if (host_name_str) {
        aws_string_destroy(host_name_str);
    }

    if (conn_options_ptr) {
        aws_tls_connection_options_clean_up(conn_options_ptr);
    }

    s_shutdown_callback_data_destroy(env, callback_data);
    return (jlong)NULL;
}

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerListener_getBoundPort(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_listener) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_listener *listener =
        (struct aws_event_stream_rpc_server_listener *)jni_server_listener;
    if (!listener) {
        aws_jni_throw_runtime_exception(env, "ServerListener.getBoundPort: Invalid serverListener");
        return (jshort)-1;
    }

    return (jint)aws_event_stream_rpc_server_listener_get_bound_port(listener);
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
    if (connection == NULL) {
        return;
    }

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
    if (connection == NULL) {
        return;
    }

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
    if (connection == NULL) {
        return;
    }

    aws_event_stream_rpc_server_connection_close(connection, error_code);
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_eventstream_ServerConnection_isOpen(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_server_connection) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_rpc_server_connection *connection =
        (struct aws_event_stream_rpc_server_connection *)jni_server_connection;

    if (connection == NULL) {
        return false;
    }

    return aws_event_stream_rpc_server_connection_is_open(connection);
}

struct message_flush_callback_args {
    JavaVM *jvm;
    jobject callback;
};

static void s_destroy_message_flush_callback_args(JNIEnv *env, struct message_flush_callback_args *callback_args) {
    if (callback_args == NULL) {
        return;
    }

    if (callback_args->callback != NULL) {
        (*env)->DeleteGlobalRef(env, callback_args->callback);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_args);
}

static void s_message_flush_fn(int error_code, void *user_data) {
    struct message_flush_callback_args *callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    (*env)->CallVoidMethod(
        env, callback_data->callback, event_stream_server_message_flush_properties.callback, error_code);
    aws_jni_check_and_clear_exception(env);

    JavaVM *jvm = callback_data->jvm;
    s_destroy_message_flush_callback_args(env, callback_data);
    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
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

    int ret_val = AWS_OP_ERR;
    struct message_flush_callback_args *callback_data = NULL;

    struct aws_event_stream_rpc_marshalled_message marshalled_message;
    if (aws_event_stream_rpc_marshall_message_args_init(
            &marshalled_message, aws_jni_get_allocator(), env, headers, payload, NULL, message_flags, message_type)) {
        goto clean_up;
    }

    if (connection == NULL) {
        aws_jni_throw_runtime_exception(env, "ServerConnection.sendProtocolMessage: native connection is NULL.");
        goto clean_up;
    }

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
    if (callback_data->callback == NULL) {
        aws_jni_throw_runtime_exception(
            env, "ServerConnection.sendProtocolMessage: Unable to create global ref to callback");
        goto clean_up;
    }

    if (aws_event_stream_rpc_server_connection_send_protocol_message(
            connection, &marshalled_message.message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(env, "ServerConnection.sendProtocolMessage: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:

    aws_event_stream_rpc_marshall_message_args_clean_up(&marshalled_message);
    if (ret_val != AWS_OP_SUCCESS) {
        s_destroy_message_flush_callback_args(env, callback_data);
    }

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

    if (continuation == NULL) {
        return true;
    }

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

    int ret_val = AWS_OP_ERR;

    struct aws_event_stream_rpc_marshalled_message marshalled_message;
    if (aws_event_stream_rpc_marshall_message_args_init(
            &marshalled_message, aws_jni_get_allocator(), env, headers, payload, NULL, message_flags, message_type)) {
        goto clean_up;
    }

    if (continuation == NULL) {
        aws_jni_throw_runtime_exception(env, "ServerConnection.sendContinuationMessage: native continuation is NULL.");
        goto clean_up;
    }

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
    if (callback_data->callback == NULL) {
        aws_jni_throw_runtime_exception(
            env, "ServerConnection.sendContinuationMessage: Unable to create global ref to callback");
        goto clean_up;
    }

    if (aws_event_stream_rpc_server_continuation_send_message(
            continuation, &marshalled_message.message_args, s_message_flush_fn, callback_data)) {
        aws_jni_throw_runtime_exception(
            env, "ServerConnectionContinuation.sendContinuationMessage: send message failed");
        goto clean_up;
    }

    ret_val = AWS_OP_SUCCESS;

clean_up:
    aws_event_stream_rpc_marshall_message_args_clean_up(&marshalled_message);
    if (ret_val != AWS_OP_SUCCESS) {
        s_destroy_message_flush_callback_args(env, callback_data);
    }

    return ret_val;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

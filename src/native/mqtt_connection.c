/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

#include <aws/common/atomics.h>
#include <aws/common/condition_variable.h>
#include <aws/common/logging.h>
#include <aws/common/mutex.h>
#include <aws/common/string.h>
#include <aws/common/thread.h>
#include <aws/http/connection.h>
#include <aws/http/request_response.h>
#include <aws/io/channel.h>
#include <aws/io/channel_bootstrap.h>
#include <aws/io/event_loop.h>
#include <aws/io/host_resolver.h>
#include <aws/io/socket.h>
#include <aws/io/socket_channel_handler.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/mqtt/client.h>

#include <ctype.h>
#include <string.h>

#include "crt.h"

#include "http_request_utils.h"
#include "java_class_ids.h"

/*******************************************************************************
 * mqtt_jni_async_callback - carries an AsyncCallback around as user data to mqtt
 * async ops, and is used to deliver callbacks. Also hangs on to JNI references
 * to buffers and strings that need to outlive the request
 ******************************************************************************/
struct mqtt_jni_async_callback {
    struct mqtt_jni_connection *connection;
    jobject async_callback;
    struct aws_byte_buf buffer; /* payloads or other pinned resources go in here, freed when callback is delivered */
};

/*******************************************************************************
 * mqtt_jni_connection - represents an aws_mqtt_client_connection to Java
 ******************************************************************************/
struct mqtt_jni_connection {
    struct aws_mqtt_client *client; /* Provided to mqtt_connect */
    struct aws_mqtt_client_connection *client_connection;
    struct aws_socket_options socket_options;
    struct aws_tls_connection_options tls_options;

    JavaVM *jvm;
    jweak java_mqtt_connection; /* MqttClientConnection instance */
    struct mqtt_jni_async_callback *on_message;

    struct aws_atomic_var ref_count;
};

/*******************************************************************************
 * mqtt_jni_ws_handshake - Data needed to perform the async websocket handshake
 * transform operations. Destroyed when transform is complete.
 ******************************************************************************/
struct mqtt_jni_ws_handshake {
    struct mqtt_jni_connection *connection;
    struct aws_http_message *http_request;
    aws_mqtt_transform_websocket_handshake_complete_fn *complete_fn;
    void *complete_ctx;
};

static void s_mqtt_connection_destroy(JNIEnv *env, struct mqtt_jni_connection *connection);

static void s_mqtt_jni_connection_acquire(struct mqtt_jni_connection *connection) {
    size_t old_value = aws_atomic_fetch_add(&connection->ref_count, 1);

    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "mqtt_jni_connection acquire, ref count now = %d", (int)old_value + 1);
}

static void s_on_shutdown_disconnect_complete(struct aws_mqtt_client_connection *connection, void *user_data);

static void s_mqtt_jni_connection_release(struct mqtt_jni_connection *connection) {
    size_t old_value = aws_atomic_fetch_sub(&connection->ref_count, 1);

    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "mqtt_jni_connection release, ref count now = %d", (int)old_value - 1);

    if (old_value == 1) {

        if (aws_mqtt_client_connection_disconnect(
                connection->client_connection, s_on_shutdown_disconnect_complete, connection) != AWS_OP_SUCCESS) {

            /*
             * This can happen under normal code paths if the client happens to be disconnected at cleanup/shutdown
             * time. Log it (in case it was unexpected) and then invoke the shutdown callback manually.
             */
            s_on_shutdown_disconnect_complete(connection->client_connection, connection);
        }
    }
}

static struct mqtt_jni_async_callback *mqtt_jni_async_callback_new(
    struct mqtt_jni_connection *connection,
    jobject async_callback) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct mqtt_jni_async_callback *callback = aws_mem_calloc(allocator, 1, sizeof(struct mqtt_jni_async_callback));
    if (!callback) {
        /* caller will throw when they get a null */
        return NULL;
    }

    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);

    callback->connection = connection;
    callback->async_callback = async_callback ? (*env)->NewGlobalRef(env, async_callback) : NULL;

    aws_byte_buf_init(&callback->buffer, aws_jni_get_allocator(), 0);

    return callback;
}

static void mqtt_jni_async_callback_destroy(struct mqtt_jni_async_callback *callback) {
    AWS_FATAL_ASSERT(callback && callback->connection);
    JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
    if (callback->async_callback) {
        (*env)->DeleteGlobalRef(env, callback->async_callback);
    }

    aws_byte_buf_clean_up(&callback->buffer);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback);
}

static jobject s_new_mqtt_exception(JNIEnv *env, int error_code) {
    jobject exception = (*env)->NewObject(
        env, mqtt_exception_properties.jni_mqtt_exception, mqtt_exception_properties.jni_constructor, error_code);
    return exception;
}

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

/*******************************************************************************
 * new
 ******************************************************************************/
static void s_on_connection_disconnected(struct aws_mqtt_client_connection *client_connection, void *user_data);
static void s_on_connection_complete(
    struct aws_mqtt_client_connection *client_connection,
    int error_code,
    enum aws_mqtt_connect_return_code return_code,
    bool session_present,
    void *user_data) {
    (void)client_connection;
    (void)return_code;

    struct mqtt_jni_async_callback *connect_callback = user_data;
    struct mqtt_jni_connection *connection = connect_callback->connection;
    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);

    jobject mqtt_connection = (*env)->NewLocalRef(env, connection->java_mqtt_connection);
    if (mqtt_connection != NULL) {
        (*env)->CallVoidMethod(
            env, mqtt_connection, mqtt_connection_properties.on_connection_complete, error_code, session_present);

        (*env)->DeleteLocalRef(env, mqtt_connection);

        if ((*env)->ExceptionCheck(env)) {
            aws_mqtt_client_connection_disconnect(client_connection, s_on_connection_disconnected, connect_callback);
            return; /* callback and ref count will be cleaned up in s_on_connection_disconnected */
        }
    }

    mqtt_jni_async_callback_destroy(connect_callback);

    s_mqtt_jni_connection_release(connection);
}

static void s_on_connection_interrupted_internal(
    struct mqtt_jni_connection *connection,
    int error_code,
    jobject ack_callback) {

    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
    jobject mqtt_connection = (*env)->NewLocalRef(env, connection->java_mqtt_connection);
    if (mqtt_connection) {
        (*env)->CallVoidMethod(
            env, mqtt_connection, mqtt_connection_properties.on_connection_interrupted, error_code, ack_callback);

        (*env)->DeleteLocalRef(env, mqtt_connection);

        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }
}

static void s_on_connection_interrupted(
    struct aws_mqtt_client_connection *client_connection,
    int error_code,
    void *user_data) {
    (void)client_connection;

    s_on_connection_interrupted_internal(user_data, error_code, NULL);
}

static void s_on_connection_resumed(
    struct aws_mqtt_client_connection *client_connection,
    enum aws_mqtt_connect_return_code return_code,
    bool session_present,
    void *user_data) {
    (void)client_connection;
    (void)return_code;

    struct mqtt_jni_connection *connection = user_data;
    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
    jobject mqtt_connection = (*env)->NewLocalRef(env, connection->java_mqtt_connection);
    if (mqtt_connection) {

        (*env)->CallVoidMethod(env, mqtt_connection, mqtt_connection_properties.on_connection_resumed, session_present);

        (*env)->DeleteLocalRef(env, mqtt_connection);

        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }
}

static void s_on_connection_disconnected(struct aws_mqtt_client_connection *client_connection, void *user_data) {
    (void)client_connection;

    struct mqtt_jni_async_callback *connect_callback = user_data;
    struct mqtt_jni_connection *jni_connection = connect_callback->connection;

    JNIEnv *env = aws_jni_get_thread_env(jni_connection->jvm);

    s_on_connection_interrupted_internal(connect_callback->connection, 0, connect_callback->async_callback);

    mqtt_jni_async_callback_destroy(connect_callback);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    s_mqtt_jni_connection_release(jni_connection);
}

static struct mqtt_jni_connection *s_mqtt_connection_new(
    JNIEnv *env,
    struct aws_mqtt_client *client,
    jobject java_mqtt_connection) {
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct mqtt_jni_connection *connection = aws_mem_calloc(allocator, 1, sizeof(struct mqtt_jni_connection));
    if (!connection) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_connect: Out of memory allocating JNI connection");
        return NULL;
    }

    aws_atomic_store_int(&connection->ref_count, 1);
    connection->client = client;
    connection->java_mqtt_connection = (*env)->NewWeakGlobalRef(env, java_mqtt_connection);
    jint jvmresult = (*env)->GetJavaVM(env, &connection->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    connection->client_connection = aws_mqtt_client_connection_new(client);
    if (!connection->client_connection) {
        aws_jni_throw_runtime_exception(
            env,
            "MqttClientConnection.mqtt_connect: aws_mqtt_client_connection_new failed, unable to create new "
            "connection");
        goto on_error;
    }

    return connection;

on_error:

    s_mqtt_jni_connection_release(connection);

    return NULL;
}

static void s_mqtt_connection_destroy(JNIEnv *env, struct mqtt_jni_connection *connection) {
    if (connection == NULL) {
        return;
    }

    if (connection->on_message) {
        mqtt_jni_async_callback_destroy(connection->on_message);
    }

    if (connection->java_mqtt_connection) {
        (*env)->DeleteWeakGlobalRef(env, connection->java_mqtt_connection);
    }

    aws_mqtt_client_connection_destroy(connection->client_connection);

    aws_tls_connection_options_clean_up(&connection->tls_options);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, connection);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_mqtt_connection) {
    (void)jni_class;

    struct aws_mqtt_client *client = (struct aws_mqtt_client *)jni_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_new: Client is invalid/null");
        return (jlong)NULL;
    }

    /* any error after this point needs to jump to error_cleanup */
    struct mqtt_jni_connection *connection = s_mqtt_connection_new(env, client, jni_mqtt_connection);
    if (!connection) {
        return (jlong)NULL;
    }

    aws_mqtt_client_connection_set_connection_interruption_handlers(
        connection->client_connection, s_on_connection_interrupted, connection, s_on_connection_resumed, connection);

    return (jlong)connection;
}

static void s_on_shutdown_disconnect_complete(struct aws_mqtt_client_connection *connection, void *user_data) {
    (void)connection;

    struct mqtt_jni_connection *jni_connection = (struct mqtt_jni_connection *)user_data;

    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "mqtt_jni_connection shutdown complete, releasing references");

    JNIEnv *env = aws_jni_get_thread_env(jni_connection->jvm);

    jobject mqtt_connection = (*env)->NewLocalRef(env, jni_connection->java_mqtt_connection);
    if (mqtt_connection != NULL) {
        (*env)->CallVoidMethod(env, mqtt_connection, crt_resource_properties.release_references);

        (*env)->DeleteLocalRef(env, mqtt_connection);

        (*env)->ExceptionCheck(env);
    }

    s_mqtt_connection_destroy(env, jni_connection);
}

/*******************************************************************************
 * clean_up
 ******************************************************************************/
JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)jni_class;
    (void)env;

    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    s_mqtt_jni_connection_release(connection);
}

/*******************************************************************************
 * connect
 ******************************************************************************/
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionConnect(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_endpoint,
    jshort jni_port,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jstring jni_client_id,
    jboolean jni_clean_session,
    jint keep_alive_ms,
    jshort ping_timeout_ms) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_connect: Connection is invalid/null");
        return;
    }

    struct aws_byte_cursor client_id;
    AWS_ZERO_STRUCT(client_id);
    struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jstring_acquire(env, jni_endpoint);
    uint16_t port = jni_port;
    if (!port) {
        aws_jni_throw_runtime_exception(
            env,
            "MqttClientConnection.mqtt_new: Endpoint should be in the format hostname:port and port must be between 1 "
            "and 65535");
        goto cleanup;
    }

    struct mqtt_jni_async_callback *connect_callback = mqtt_jni_async_callback_new(connection, NULL);
    if (connect_callback == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_connect: Failed to create async callback");
        goto cleanup;
    }

    s_mqtt_jni_connection_acquire(connection);

    struct aws_socket_options default_socket_options;
    AWS_ZERO_STRUCT(default_socket_options);
    default_socket_options.type = AWS_SOCKET_STREAM;
    default_socket_options.connect_timeout_ms = 3000;
    struct aws_socket_options *socket_options = &default_socket_options;
    if (jni_socket_options) {
        socket_options = (struct aws_socket_options *)jni_socket_options;
    }
    memcpy(&connection->socket_options, socket_options, sizeof(struct aws_socket_options));

    /* if a tls_ctx was provided, initialize tls options */
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_ctx;
    struct aws_tls_connection_options *tls_options = NULL;
    if (tls_ctx) {
        tls_options = &connection->tls_options;
        aws_tls_connection_options_init_from_ctx(tls_options, tls_ctx);
        aws_tls_connection_options_set_server_name(tls_options, aws_jni_get_allocator(), &endpoint);
    }

    client_id = aws_jni_byte_cursor_from_jstring_acquire(env, jni_client_id);
    bool clean_session = jni_clean_session != 0;

    struct aws_mqtt_connection_options connect_options;
    AWS_ZERO_STRUCT(connect_options);
    connect_options.host_name = endpoint;
    connect_options.port = port;
    connect_options.socket_options = &connection->socket_options;
    connect_options.tls_options = tls_options;
    connect_options.client_id = client_id;
    connect_options.keep_alive_time_secs = (uint16_t)keep_alive_ms / 1000;
    connect_options.ping_timeout_ms = ping_timeout_ms;
    connect_options.clean_session = clean_session;
    connect_options.on_connection_complete = s_on_connection_complete;
    connect_options.user_data = connect_callback;

    int result = aws_mqtt_client_connection_connect(connection->client_connection, &connect_options);
    if (result != AWS_OP_SUCCESS) {
        s_mqtt_jni_connection_release(connection);
        mqtt_jni_async_callback_destroy(connect_callback);
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_connect: aws_mqtt_client_connection_connect failed");
    }

cleanup:
    aws_jni_byte_cursor_from_jstring_release(env, jni_endpoint, endpoint);
    aws_jni_byte_cursor_from_jstring_release(env, jni_client_id, client_id);
}

/*******************************************************************************
 * disconnect
 ******************************************************************************/
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionDisconnect(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jobject jni_ack) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_disconnect: Invalid connection");
        return;
    }

    struct mqtt_jni_async_callback *disconnect_callback = mqtt_jni_async_callback_new(connection, jni_ack);
    if (disconnect_callback == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_disconnect: Failed to create async callback");
        return;
    }

    s_mqtt_jni_connection_acquire(connection);

    if (aws_mqtt_client_connection_disconnect(
            connection->client_connection, s_on_connection_disconnected, disconnect_callback) != AWS_OP_SUCCESS) {
        int error = aws_last_error();
        /*
         * Disconnect invoked on a disconnected connection can happen under normal circumstances.  Invoke the callback
         * manually since it won't get invoked otherwise.
         */
        AWS_LOGF_WARN(
            AWS_LS_MQTT_CLIENT,
            "MqttClientConnection.mqtt_disconnect: error calling disconnect - %d(%s)",
            error,
            aws_error_str(error));
        s_on_connection_disconnected(connection->client_connection, disconnect_callback);
    }
}

/*******************************************************************************
 * subscribe
 ******************************************************************************/
/* called from any sub, unsub, or pub ack */
static void s_deliver_ack_success(struct mqtt_jni_async_callback *callback) {
    AWS_FATAL_ASSERT(callback);
    AWS_FATAL_ASSERT(callback->connection);

    if (callback->async_callback) {
        JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
        (*env)->CallVoidMethod(env, callback->async_callback, async_callback_properties.on_success);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }
}

static void s_deliver_ack_failure(struct mqtt_jni_async_callback *callback, int error_code) {
    AWS_FATAL_ASSERT(callback);
    AWS_FATAL_ASSERT(callback->connection);

    if (callback->async_callback) {
        JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
        jobject jni_reason = s_new_mqtt_exception(env, error_code);
        (*env)->CallVoidMethod(env, callback->async_callback, async_callback_properties.on_failure, jni_reason);
        (*env)->DeleteLocalRef(env, jni_reason);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }
}

static void s_on_op_complete(
    struct aws_mqtt_client_connection *connection,
    uint16_t packet_id,
    int error_code,
    void *user_data) {
    AWS_FATAL_ASSERT(connection);
    (void)packet_id;

    struct mqtt_jni_async_callback *callback = user_data;
    if (!callback) {
        return;
    }

    if (error_code) {
        s_deliver_ack_failure(callback, error_code);
    } else {
        s_deliver_ack_success(callback);
    }

    mqtt_jni_async_callback_destroy(callback);
}

static void s_on_ack(
    struct aws_mqtt_client_connection *connection,
    uint16_t packet_id,
    const struct aws_byte_cursor *topic,
    enum aws_mqtt_qos qos,
    int error_code,
    void *user_data) {
    (void)topic;
    (void)qos;
    s_on_op_complete(connection, packet_id, error_code, user_data);
}

static void s_cleanup_handler(void *user_data) {
    struct mqtt_jni_async_callback *handler = user_data;
    mqtt_jni_async_callback_destroy(handler);
}

static void s_on_subscription_delivered(
    struct aws_mqtt_client_connection *connection,
    const struct aws_byte_cursor *topic,
    const struct aws_byte_cursor *payload,
    void *user_data) {
    AWS_FATAL_ASSERT(connection);
    AWS_FATAL_ASSERT(topic);
    AWS_FATAL_ASSERT(payload);
    AWS_FATAL_ASSERT(user_data);

    struct mqtt_jni_async_callback *callback = user_data;
    if (!callback->async_callback) {
        return;
    }

    JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
    jbyteArray jni_payload = (*env)->NewByteArray(env, (jsize)payload->len);
    (*env)->SetByteArrayRegion(env, jni_payload, 0, (jsize)payload->len, (const signed char *)payload->ptr);

    jstring jni_topic = aws_jni_string_from_cursor(env, topic);

    (*env)->CallVoidMethod(env, callback->async_callback, message_handler_properties.deliver, jni_topic, jni_payload);

    (*env)->DeleteLocalRef(env, jni_payload);
    (*env)->DeleteLocalRef(env, jni_topic);

    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
}

JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionSubscribe(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jobject jni_handler,
    jobject jni_ack) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_subscribe: Invalid connection");
        return 0;
    }

    struct mqtt_jni_async_callback *handler = mqtt_jni_async_callback_new(connection, jni_handler);
    if (!handler) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_subscribe: Unable to allocate handler");
        return 0;
    }

    /* from here, any failure requires error_cleanup */
    struct mqtt_jni_async_callback *sub_ack = NULL;
    if (jni_ack) {
        sub_ack = mqtt_jni_async_callback_new(connection, jni_ack);
        if (!sub_ack) {
            aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_subscribe: Unable to allocate sub ack");
            goto error_cleanup;
        }
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring_acquire(env, jni_topic);
    enum aws_mqtt_qos qos = jni_qos;

    uint16_t msg_id = aws_mqtt_client_connection_subscribe(
        connection->client_connection,
        &topic,
        qos,
        s_on_subscription_delivered,
        handler,
        s_cleanup_handler,
        s_on_ack,
        sub_ack);
    aws_jni_byte_cursor_from_jstring_release(env, jni_topic, topic);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_subscribe: aws_mqtt_client_connection_subscribe failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (handler) {
        mqtt_jni_async_callback_destroy(handler);
    }

    if (sub_ack) {
        mqtt_jni_async_callback_destroy(sub_ack);
    }

    return 0;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionOnMessage(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jobject jni_handler) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqttClientConnectionOnMessage: Invalid connection");
        return;
    }

    if (!jni_handler) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqttClientConnectionOnMessage: Invalid handler");
        return;
    }

    struct mqtt_jni_async_callback *handler = mqtt_jni_async_callback_new(connection, jni_handler);
    if (!handler) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqttClientConnectionOnMessage: Unable to allocate handler");
        return;
    }

    if (aws_mqtt_client_connection_set_on_any_publish_handler(
            connection->client_connection, s_on_subscription_delivered, handler)) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqttClientConnectionOnMessage: Failed to install on_any_publish_handler");
        goto error_cleanup;
    }

    if (connection->on_message) {
        mqtt_jni_async_callback_destroy(connection->on_message);
    }

    connection->on_message = handler;

    return;

error_cleanup:
    if (handler) {
        mqtt_jni_async_callback_destroy(handler);
    }
}

/*******************************************************************************
 * unsubscribe
 ******************************************************************************/
JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionUnsubscribe(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jobject jni_ack) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_unsubscribe: Invalid connection");
        return 0;
    }

    struct mqtt_jni_async_callback *unsub_ack = mqtt_jni_async_callback_new(connection, jni_ack);
    if (!unsub_ack) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_unsubscribe: Unable to allocate unsub ack");
        goto error_cleanup;
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring_acquire(env, jni_topic);
    uint16_t msg_id =
        aws_mqtt_client_connection_unsubscribe(connection->client_connection, &topic, s_on_op_complete, unsub_ack);
    aws_jni_byte_cursor_from_jstring_release(env, jni_topic, topic);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_unsubscribe: aws_mqtt_client_connection_unsubscribe failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (unsub_ack) {
        mqtt_jni_async_callback_destroy(unsub_ack);
    }
    return 0;
}

/*******************************************************************************
 * publish
 ******************************************************************************/
JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionPublish(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jboolean jni_retain,
    jbyteArray jni_payload,
    jobject jni_ack) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_publish: Invalid connection");
        return 0;
    }

    if (!jni_payload) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_publish: Invalid/null payload");
        return 0;
    }

    struct mqtt_jni_async_callback *pub_ack = mqtt_jni_async_callback_new(connection, jni_ack);
    if (!pub_ack) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_publish: Unable to allocate pub ack");
        goto error_cleanup;
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring_acquire(env, jni_topic);
    struct aws_byte_cursor payload = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_payload);

    enum aws_mqtt_qos qos = jni_qos;
    bool retain = jni_retain != 0;

    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_byte_buf_append_dynamic(&pub_ack->buffer, &topic));
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_byte_buf_append_dynamic(&pub_ack->buffer, &payload));
    struct aws_byte_cursor pinned_payload = aws_byte_cursor_from_buf(&pub_ack->buffer);
    struct aws_byte_cursor pinned_topic = aws_byte_cursor_advance(&pinned_payload, topic.len);

    uint16_t msg_id = aws_mqtt_client_connection_publish(
        connection->client_connection, &pinned_topic, qos, retain, &pinned_payload, s_on_op_complete, pub_ack);
    aws_jni_byte_cursor_from_jstring_release(env, jni_topic, topic);
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_payload, payload);

    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_publish: aws_mqtt_client_connection_publish failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (pub_ack) {
        mqtt_jni_async_callback_destroy(pub_ack);
    }

    return 0;
}

JNIEXPORT jboolean JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionSetWill(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jboolean jni_retain,
    jbyteArray jni_payload) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_set_will: Invalid connection");
        return 0;
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring_acquire(env, jni_topic);
    struct aws_byte_cursor payload = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_payload);

    enum aws_mqtt_qos qos = jni_qos;
    bool retain = jni_retain != 0;

    int result = aws_mqtt_client_connection_set_will(connection->client_connection, &topic, qos, retain, &payload);
    aws_jni_byte_cursor_from_jstring_release(env, jni_topic, topic);
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_payload, payload);
    return (result == AWS_OP_SUCCESS);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionSetLogin(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_user,
    jstring jni_pass) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_set_login: Invalid connection");
        return;
    }

    struct aws_byte_cursor username = aws_jni_byte_cursor_from_jstring_acquire(env, jni_user);
    struct aws_byte_cursor password;
    struct aws_byte_cursor *password_ptr = NULL;
    AWS_ZERO_STRUCT(password);
    if (jni_pass != NULL) {
        password = aws_jni_byte_cursor_from_jstring_acquire(env, jni_pass);
        password_ptr = &password;
    }

    if (aws_mqtt_client_connection_set_login(connection->client_connection, &username, password_ptr)) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_set_login: Failed to set login");
    }

    aws_jni_byte_cursor_from_jstring_release(env, jni_user, username);

    if (password.len > 0) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_pass, password);
    }
}

///////
static void s_ws_handshake_destroy(struct mqtt_jni_ws_handshake *ws_handshake) {
    if (!ws_handshake) {
        return;
    }

    s_mqtt_jni_connection_release(ws_handshake->connection);
    aws_mem_release(aws_jni_get_allocator(), ws_handshake);
}

static void s_ws_handshake_transform(
    struct aws_http_message *request,
    void *user_data,
    aws_mqtt_transform_websocket_handshake_complete_fn *complete_fn,
    void *complete_ctx) {

    struct mqtt_jni_connection *connection = user_data;

    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
    struct aws_allocator *alloc = aws_jni_get_allocator();

    struct mqtt_jni_ws_handshake *ws_handshake = aws_mem_calloc(alloc, 1, sizeof(struct mqtt_jni_ws_handshake));
    if (!ws_handshake) {
        goto error;
    }

    ws_handshake->connection = connection;
    s_mqtt_jni_connection_acquire(ws_handshake->connection);

    ws_handshake->complete_ctx = complete_ctx;
    ws_handshake->complete_fn = complete_fn;
    ws_handshake->http_request = request;

    jobject java_http_request = aws_java_http_request_from_native(env, request, NULL);
    if (!java_http_request) {
        aws_raise_error(AWS_ERROR_UNKNOWN); /* TODO: given java exception, choose appropriate aws error code */
        goto error;
    }

    jobject mqtt_connection = (*env)->NewLocalRef(env, connection->java_mqtt_connection);
    if (mqtt_connection != NULL) {
        (*env)->CallVoidMethod(
            env, mqtt_connection, mqtt_connection_properties.on_websocket_handshake, java_http_request, ws_handshake);

        (*env)->DeleteLocalRef(env, mqtt_connection);

        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }

    (*env)->DeleteLocalRef(env, java_http_request);

    return;

error:;

    int error_code = aws_last_error();
    s_ws_handshake_destroy(ws_handshake);
    complete_fn(request, error_code, complete_ctx);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionUseWebsockets(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_raise_error(AWS_ERROR_INVALID_STATE);
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.useWebsockets: Invalid connection");
        return;
    }

    if (aws_mqtt_client_connection_use_websockets(
            connection->client_connection, s_ws_handshake_transform, connection, NULL, NULL)) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.useWebsockets: Failed to use websockets");
        return;
    }
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionWebsocketHandshakeComplete(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jbyteArray jni_marshalled_request,
    jobject jni_throwable,
    jlong jni_user_data) {
    (void)jni_class;
    (void)jni_connection;

    struct mqtt_jni_ws_handshake *ws_handshake = (void *)jni_user_data;
    int error_code = AWS_ERROR_SUCCESS;

    if (jni_throwable != NULL) {
        error_code = AWS_ERROR_UNKNOWN; /* TODO: given java exception, choose appropriate aws error code */
        goto done;
    }

    if (aws_apply_java_http_request_changes_to_native_request(
            env, jni_marshalled_request, NULL, ws_handshake->http_request)) {
        error_code = aws_last_error();
        goto done;
    }

done:
    ws_handshake->complete_fn(ws_handshake->http_request, error_code, ws_handshake->complete_ctx);
    s_ws_handshake_destroy(ws_handshake);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionSetWebsocketProxyOptions(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_proxy_host,
    jint jni_proxy_port,
    jlong jni_proxy_tls_context,
    jint jni_proxy_authorization_type,
    jstring jni_proxy_authorization_username,
    jstring jni_proxy_authorization_password) {

    (void)jni_class;

    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;

    struct aws_http_proxy_options proxy_options;
    AWS_ZERO_STRUCT(proxy_options);

    if (!jni_proxy_host) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.setWebsocketProxyOptions: proxyHost must not be null.");
        return;
    }

    proxy_options.host = aws_jni_byte_cursor_from_jstring_acquire(env, jni_proxy_host);
    proxy_options.port = (uint16_t)jni_proxy_port;

    proxy_options.auth_type = (enum aws_http_proxy_authentication_type)jni_proxy_authorization_type;

    if (jni_proxy_authorization_username) {
        proxy_options.auth_username = aws_jni_byte_cursor_from_jstring_acquire(env, jni_proxy_authorization_username);
    }

    if (jni_proxy_authorization_password) {
        proxy_options.auth_password = aws_jni_byte_cursor_from_jstring_acquire(env, jni_proxy_authorization_password);
    }

    struct aws_tls_connection_options proxy_tls_conn_options;
    AWS_ZERO_STRUCT(proxy_tls_conn_options);

    if (jni_proxy_tls_context != 0) {
        struct aws_tls_ctx *proxy_tls_ctx = (struct aws_tls_ctx *)jni_proxy_tls_context;
        aws_tls_connection_options_init_from_ctx(&proxy_tls_conn_options, proxy_tls_ctx);
        aws_tls_connection_options_set_server_name(
            &proxy_tls_conn_options, aws_jni_get_allocator(), &proxy_options.host);
        proxy_options.tls_options = &proxy_tls_conn_options;
    }

    if (aws_mqtt_client_connection_set_websocket_proxy_options(connection->client_connection, &proxy_options)) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.setWebsocketProxyOptions: Failed to set proxy options");
    }

    if (jni_proxy_authorization_password) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_proxy_authorization_password, proxy_options.auth_password);
    }

    if (jni_proxy_authorization_username) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_proxy_authorization_username, proxy_options.auth_username);
    }

    aws_jni_byte_cursor_from_jstring_release(env, jni_proxy_host, proxy_options.host);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

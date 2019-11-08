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

#include <aws/common/condition_variable.h>
#include <aws/common/logging.h>
#include <aws/common/mutex.h>
#include <aws/common/string.h>
#include <aws/common/thread.h>
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

#include "async_callback.h"
#include "crt.h"

/*******************************************************************************
 * JNI class field/method maps
 ******************************************************************************/

struct crt_async_callback g_async_callback;

/* methods of CrtResource */
static struct {
    jmethodID release_references;
    jmethodID add_ref;
    jmethodID close;
} s_crt_resource;

/* methods of MqttClientConnection */
static struct {
    jmethodID on_connection_complete;
    jmethodID on_connection_interrupted;
    jmethodID on_connection_resumed;
} s_mqtt_connection;

void s_cache_mqtt_connection(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttClientConnection");
    AWS_FATAL_ASSERT(cls);
    s_mqtt_connection.on_connection_complete = (*env)->GetMethodID(env, cls, "onConnectionComplete", "(IZ)V");
    AWS_FATAL_ASSERT(s_mqtt_connection.on_connection_complete);
    s_mqtt_connection.on_connection_interrupted =
        (*env)->GetMethodID(env, cls, "onConnectionInterrupted", "(ILsoftware/amazon/awssdk/crt/AsyncCallback;)V");
    AWS_FATAL_ASSERT(s_mqtt_connection.on_connection_interrupted);
    s_mqtt_connection.on_connection_resumed = (*env)->GetMethodID(env, cls, "onConnectionResumed", "(Z)V");
    AWS_FATAL_ASSERT(s_mqtt_connection.on_connection_resumed);

    jclass crt_resource_class = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtResource");
    AWS_FATAL_ASSERT(crt_resource_class);
    s_crt_resource.release_references = (*env)->GetMethodID(env, crt_resource_class, "releaseReferences", "()V");
    AWS_FATAL_ASSERT(s_crt_resource.release_references);

    s_crt_resource.add_ref = (*env)->GetMethodID(env, crt_resource_class, "addRef", "()V");
    AWS_FATAL_ASSERT(s_crt_resource.add_ref);

    s_crt_resource.close = (*env)->GetMethodID(env, crt_resource_class, "close", "()V");
    AWS_FATAL_ASSERT(s_crt_resource.close);
}

/* MqttClientConnection.MessageHandler */
static struct { jmethodID deliver; } s_message_handler;

void s_cache_message_handler(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttClientConnection$MessageHandler");
    AWS_FATAL_ASSERT(cls);
    s_message_handler.deliver = (*env)->GetMethodID(env, cls, "deliver", "([B)V");
    AWS_FATAL_ASSERT(s_message_handler.deliver);
}

static struct {
    jclass jni_mqtt_exception;
    jmethodID jni_constructor;
} s_mqtt_exception;

void s_cache_mqtt_exception(JNIEnv *env) {
    s_mqtt_exception.jni_mqtt_exception =
        (*env)->NewGlobalRef(env, (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttException"));
    AWS_FATAL_ASSERT(s_mqtt_exception.jni_mqtt_exception);
    s_mqtt_exception.jni_constructor = (*env)->GetMethodID(env, s_mqtt_exception.jni_mqtt_exception, "<init>", "(I)V");
    AWS_FATAL_ASSERT(s_mqtt_exception.jni_constructor);
}

/*******************************************************************************
 * mqtt_jni_connection - represents an aws_mqtt_client_connection to Java
 ******************************************************************************/
struct mqtt_jni_connection {
    struct aws_mqtt_client *client; /* Provided to mqtt_connect */
    struct aws_mqtt_client_connection *client_connection;
    struct aws_socket_options socket_options;
    struct aws_tls_connection_options tls_options;

    JavaVM *jvm;
    jobject mqtt_connection; /* MqttClientConnection instance */
};

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
    jobject exception =
        (*env)->NewObject(env, s_mqtt_exception.jni_mqtt_exception, s_mqtt_exception.jni_constructor, error_code);
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
    if (connection->mqtt_connection) {
        JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
        (*env)->CallVoidMethod(
            env, connection->mqtt_connection, s_mqtt_connection.on_connection_complete, error_code, session_present);
        if ((*env)->ExceptionCheck(env)) {
            aws_mqtt_client_connection_disconnect(client_connection, s_on_connection_disconnected, connect_callback);
            return; /* callback will be cleaned up in s_on_connection_disconnected */
        }
    }

    mqtt_jni_async_callback_destroy(connect_callback);
}

static void s_on_connection_interrupted_internal(
    struct mqtt_jni_connection *connection,
    int error_code,
    jobject ack_callback) {
    if (connection->mqtt_connection) {
        JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
        (*env)->CallVoidMethod(
            env, connection->mqtt_connection, s_mqtt_connection.on_connection_interrupted, error_code, ack_callback);
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
    if (connection->mqtt_connection) {
        JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
        (*env)->CallVoidMethod(
            env, connection->mqtt_connection, s_mqtt_connection.on_connection_resumed, session_present);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }
}

static void s_on_connection_disconnected(struct aws_mqtt_client_connection *client_connection, void *user_data) {
    (void)client_connection;

    struct mqtt_jni_async_callback *connect_callback = user_data;
    struct mqtt_jni_connection *jni_connection = connect_callback->connection;

    JNIEnv *env = aws_jni_get_thread_env(jni_connection->jvm);

    /* temporarily raise the ref count on the connection while the callback is executing */
    (*env)->CallVoidMethod(env, jni_connection->mqtt_connection, s_crt_resource.add_ref);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    s_on_connection_interrupted_internal(connect_callback->connection, 0, connect_callback->async_callback);

    mqtt_jni_async_callback_destroy(connect_callback);

    /* undo the temporary ref count raise */
    (*env)->CallVoidMethod(env, jni_connection->mqtt_connection, s_crt_resource.close);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
}

static void s_mqtt_connection_destroy(JNIEnv *env, struct mqtt_jni_connection *connection) {
    if (connection == NULL) {
        return;
    }

    if (connection->mqtt_connection) {
        (*env)->DeleteGlobalRef(env, connection->mqtt_connection);
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, connection);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClientConnection_mqttClientConnectionNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_mqtt_connection) {
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_mqtt_client *client = (struct aws_mqtt_client *)jni_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_new: Client is invalid/null");
        return (jlong)NULL;
    }

    /* any error after this point needs to jump to error_cleanup */
    struct mqtt_jni_connection *connection = aws_mem_calloc(allocator, 1, sizeof(struct mqtt_jni_connection));
    if (!connection) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_connect: Out of memory allocating JNI connection");
        goto error_cleanup;
    }

    connection->client = client;
    connection->mqtt_connection = (*env)->NewGlobalRef(env, jni_mqtt_connection);
    jint jvmresult = (*env)->GetJavaVM(env, &connection->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    connection->client_connection = aws_mqtt_client_connection_new(connection->client);
    if (!connection->client_connection) {
        aws_jni_throw_runtime_exception(
            env,
            "MqttClientConnection.mqtt_connect: aws_mqtt_client_connection_new failed, unable to create new "
            "connection");
        goto error_cleanup;
    }
    aws_mqtt_client_connection_set_connection_interruption_handlers(
        connection->client_connection, s_on_connection_interrupted, connection, s_on_connection_resumed, connection);

    return (jlong)connection;

error_cleanup:
    s_mqtt_connection_destroy(env, connection);

    return (jlong)NULL;
}

static void s_on_shutdown_disconnect_complete(struct aws_mqtt_client_connection *connection, void *user_data) {
    (void)connection;

    struct mqtt_jni_connection *jni_connection = (struct mqtt_jni_connection *)user_data;

    JNIEnv *env = aws_jni_get_thread_env(jni_connection->jvm);
    (*env)->CallVoidMethod(env, jni_connection->mqtt_connection, s_crt_resource.release_references);
    (*env)->ExceptionCheck(env);

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
    if (aws_mqtt_client_connection_disconnect(
            connection->client_connection, s_on_shutdown_disconnect_complete, connection) != AWS_OP_SUCCESS) {
        /*
         * This can happen under normal code paths if the client happens to be disconnected at cleanup/shutdown time.
         * Log it (in case it was unexpected) and then invoke the shutdown callback manually.
         */
        int error = aws_last_error();
        AWS_LOGF_WARN(
            AWS_LS_MQTT_CLIENT,
            "MqttClientConnection.mqtt_disconnect: error calling disconnect - %d(%s)",
            error,
            aws_error_str(error));
        s_on_shutdown_disconnect_complete(connection->client_connection, connection);
    }
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
        (*env)->CallVoidMethod(env, callback->async_callback, g_async_callback.on_success);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }
}

static void s_deliver_ack_failure(struct mqtt_jni_async_callback *callback, int error_code) {
    AWS_FATAL_ASSERT(callback);
    AWS_FATAL_ASSERT(callback->connection);

    if (callback->async_callback) {
        JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
        jobject jni_reason = s_new_mqtt_exception(env, error_code);
        (*env)->CallVoidMethod(env, callback->async_callback, g_async_callback.on_failure, jni_reason);
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
    JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
    jbyteArray jni_payload = (*env)->NewByteArray(env, (jsize)payload->len);
    (*env)->SetByteArrayRegion(env, jni_payload, 0, (jsize)payload->len, (const signed char *)payload->ptr);
    (*env)->CallVoidMethod(env, callback->async_callback, s_message_handler.deliver, jni_payload);
    (*env)->DeleteLocalRef(env, jni_payload);
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

    if (!jni_handler) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_subscribe: Invalid handler");
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
    struct aws_byte_cursor password = aws_jni_byte_cursor_from_jstring_acquire(env, jni_pass);

    if (aws_mqtt_client_connection_set_login(connection->client_connection, &username, &password)) {
        aws_jni_throw_runtime_exception(env, "MqttClientConnection.mqtt_set_login: Failed to set login");
    }

    aws_jni_byte_cursor_from_jstring_release(env, jni_user, username);
    aws_jni_byte_cursor_from_jstring_release(env, jni_pass, password);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

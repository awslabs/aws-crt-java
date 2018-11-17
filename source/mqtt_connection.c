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

#include "crt.h"

/*******************************************************************************
 * JNI class field/method maps
 ******************************************************************************/
/* methods of MqttConnection.AsyncCallback */
static struct {
    jmethodID on_success;
    jmethodID on_failure;
} s_async_callback = {0};

void s_cache_async_callback(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$AsyncCallback");
    assert(cls);
    s_async_callback.on_success = (*env)->GetMethodID(env, cls, "onSuccess", "()V");
    assert(s_async_callback.on_success);
    s_async_callback.on_failure = (*env)->GetMethodID(env, cls, "onFailure", "(Ljava/lang/String;)V");
    assert(s_async_callback.on_failure);
}

/* methods of MqttConnection.ClientCallbacks */
static struct {
    jmethodID on_connected;
    jmethodID on_disconnected;
} s_client_callbacks;

void s_cache_client_callbacks(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$ClientCallbacks");
    assert(cls);
    s_client_callbacks.on_connected = (*env)->GetMethodID(env, cls, "onConnected", "()V");
    assert(s_client_callbacks.on_connected);
    s_client_callbacks.on_disconnected = (*env)->GetMethodID(env, cls, "onDisconnected", "(Ljava/lang/String;)V");
    assert(s_client_callbacks.on_disconnected);
}

/* MqttConnection.MessageHandler */
static struct { jmethodID deliver; } s_message_handler;

void s_cache_message_handler(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$MessageHandler");
    assert(cls);
    s_message_handler.deliver = (*env)->GetMethodID(env, cls, "deliver", "([B)V");
    assert(s_message_handler.deliver);
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
    jobject client_callbacks; /* MqttConnection.ClientCallbacks */
    jobject connect_ack;      /* MqttConnection.AsyncCallback */
    jobject disconnect_ack;   /* MqttConnection.AsyncCallback */
};

/*******************************************************************************
 * mqtt_jni_async_callback - carries an AsyncCallback around as user data to mqtt
 * async ops, and is used to deliver callbacks. Also hangs on to JNI references
 * to buffers and strings that need to outlive the request
 ******************************************************************************/
struct mqtt_jni_async_callback {
    struct mqtt_jni_connection *connection;
    jobject async_callback;
    jobject jni_object_storage[2];
    struct aws_array_list jni_objects; /* store pinned objects here, they will be un-pinned in clean_up */
};

static struct mqtt_jni_async_callback *mqtt_jni_async_callback_new(
    struct mqtt_jni_connection *connection,
    jobject async_callback) {
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct mqtt_jni_async_callback *callback = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_async_callback));
    if (!callback) {
        /* caller will throw when they get a null */
        return NULL;
    }

    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);

    callback->connection = connection;
    callback->async_callback = async_callback ? (*env)->NewGlobalRef(env, async_callback) : NULL;

    aws_array_list_init_static(
        &callback->jni_objects,
        &callback->jni_object_storage,
        AWS_ARRAY_SIZE(callback->jni_object_storage),
        sizeof(jobject));
    return callback;
}

static void mqtt_jni_async_callback_clean_up(struct mqtt_jni_async_callback *callback) {
    assert(callback && callback->connection);
    JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
    if (callback->async_callback) {
        (*env)->DeleteGlobalRef(env, callback->async_callback);
    }

    const size_t num_objects = aws_array_list_length(&callback->jni_objects);
    for (size_t idx = 0; idx < num_objects; ++idx) {
        jobject obj = NULL;
        aws_array_list_get_at(&callback->jni_objects, &obj, idx);
        if (obj) {
            (*env)->DeleteGlobalRef(env, obj);
        }
    }

    aws_array_list_clean_up(&callback->jni_objects);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback);
}

/*******************************************************************************
 * new
 ******************************************************************************/
static void s_on_connect_failed(struct aws_mqtt_client_connection *client_connection, int error_code, void *user_data) {
    (void)client_connection;

    struct mqtt_jni_connection *connection = user_data;
    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
    char buf[1024];
    snprintf(buf, sizeof(buf), "Connection failed with code: %d", error_code);
    jstring message = (*env)->NewStringUTF(env, buf);
    if (connection->connect_ack) {
        (*env)->CallVoidMethod(env, connection->connect_ack, s_async_callback.on_failure, message);
        (*env)->DeleteGlobalRef(env, connection->connect_ack);
        connection->connect_ack = NULL;
    }
    (*env)->DeleteLocalRef(env, message);
}

static void s_on_connect_success(
    struct aws_mqtt_client_connection *client_connection,
    enum aws_mqtt_connect_return_code return_code,
    bool session_present,
    void *user_data) {

    (void)client_connection;
    (void)return_code;
    (void)session_present;

    struct mqtt_jni_connection *connection = user_data;
    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
    (*env)->CallVoidMethod(env, connection->client_callbacks, s_client_callbacks.on_connected);
    if (connection->connect_ack) {
        (*env)->CallVoidMethod(env, connection->connect_ack, s_async_callback.on_success);
        (*env)->DeleteGlobalRef(env, connection->connect_ack);
        connection->connect_ack = NULL;
    }
}

static void s_on_disconnect(struct aws_mqtt_client_connection *client_connection, int error_code, void *user_data) {
    (void)client_connection;
    (void)error_code;

    struct mqtt_jni_connection *connection = user_data;
    JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
    if (connection->client_callbacks) {
        char buf[1024];
        snprintf(buf, sizeof(buf), "Disconnected with code: %d", error_code);
        jstring message = (*env)->NewStringUTF(env, buf);
        (*env)->CallVoidMethod(env, connection->client_callbacks, s_client_callbacks.on_disconnected, message);
        (*env)->DeleteGlobalRef(env, connection->client_callbacks);
        connection->client_callbacks = NULL;
        (*env)->DeleteLocalRef(env, message);
    }

    if (connection->connect_ack) {
        (*env)->DeleteGlobalRef(env, connection->connect_ack);
        connection->connect_ack = NULL;
    }

    if (connection->disconnect_ack) {
        (*env)->CallVoidMethod(env, connection->disconnect_ack, s_async_callback.on_success);
        (*env)->DeleteGlobalRef(env, connection->disconnect_ack);
        connection->disconnect_ack = NULL;
    }

    /* client_connection will be cleaned up by channel shutdown */
    connection->client_connection = NULL;
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1new(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jstring jni_endpoint,
    jshort jni_port,
    jobject jni_client_callbacks,
    jlong jni_socket_options,
    jlong jni_tls_ctx) {
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_mqtt_client *client = (struct aws_mqtt_client *)jni_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_new: Client is invalid/null");
        return (jlong)NULL;
    }

    struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jstring(env, jni_endpoint);
    uint16_t port = jni_port;
    if (!port) {
        aws_jni_throw_runtime_exception(
            env,
            "MqttConnection.mqtt_new: Endpoint should be in the format hostname:port and port must be between 1 "
            "and 65535");
        return (jlong)NULL;
    }

    struct aws_socket_options default_socket_options;
    AWS_ZERO_STRUCT(default_socket_options);
    default_socket_options.domain = AWS_SOCKET_IPV4;
    default_socket_options.type = AWS_SOCKET_STREAM;
    default_socket_options.connect_timeout_ms = 3000;
    struct aws_socket_options *socket_options = &default_socket_options;
    if (jni_socket_options) {
        socket_options = (struct aws_socket_options *)jni_socket_options;
    }

    /* any error after this point needs to jump to error_cleanup */
    struct mqtt_jni_connection *connection = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_connection));
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: Out of memory allocating JNI connection");
        goto error_cleanup;
    }
    AWS_ZERO_STRUCT(*connection);
    connection->client = client;
    connection->client_callbacks = (*env)->NewGlobalRef(env, jni_client_callbacks);
    jint jvmresult = (*env)->GetJavaVM(env, &connection->jvm);
    assert(jvmresult == 0);
    memcpy(&connection->socket_options, socket_options, sizeof(struct aws_socket_options));

    /* if a tls_ctx was provided, initialize tls options */
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_ctx;
    struct aws_tls_connection_options *tls_options = NULL;
    if (tls_ctx) {
        tls_options = &connection->tls_options;
        aws_tls_connection_options_init_from_ctx(tls_options, tls_ctx);
        aws_tls_connection_options_set_server_name(tls_options, (const char*)endpoint.ptr);
    }

    struct aws_mqtt_client_connection_callbacks callbacks;
    AWS_ZERO_STRUCT(callbacks);
    callbacks.on_connection_failed = s_on_connect_failed;
    callbacks.on_connack = s_on_connect_success;
    callbacks.on_disconnect = s_on_disconnect;
    callbacks.user_data = connection;

    connection->client_connection =
        aws_mqtt_client_connection_new(connection->client, callbacks, &endpoint, port, &connection->socket_options, tls_options);
    if (!connection->client_connection) {
        aws_jni_throw_runtime_exception(
            env, "MqttConnection.mqtt_connect: aws_mqtt_client_connection_new failed, unable to create new connection");
        goto error_cleanup;
    }

    return (jlong)connection;

error_cleanup:
    if (connection) {
        aws_mem_release(allocator, connection);
    }

    return (jlong)NULL;
}

/*******************************************************************************
 * clean_up
 ******************************************************************************/
JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1clean_1up(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        return;
    }
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, connection);
}

/*******************************************************************************
 * connect
 ******************************************************************************/
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1connect(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_client_id,
    jboolean jni_clean_session,
    jshort keep_alive_ms,
    jobject jni_ack) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: Connection is invalid/null");
        return;
    }

    struct aws_byte_cursor client_id = aws_jni_byte_cursor_from_jstring(env, jni_client_id);
    bool clean_session = jni_clean_session != 0;
    connection->connect_ack = jni_ack ? (*env)->NewGlobalRef(env, jni_ack) : NULL;

    int result =
        aws_mqtt_client_connection_connect(connection->client_connection, &client_id, clean_session, keep_alive_ms);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: aws_mqtt_client_connection_connect failed");
        if (connection->connect_ack) {
            (*env)->DeleteGlobalRef(env, connection->connect_ack);
        }
    }
}

/*******************************************************************************
 * disconnect
 ******************************************************************************/
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1disconnect(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jobject jni_ack) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_disconnect: Invalid connection");
        return;
    }

    if (jni_ack) {
        connection->disconnect_ack = (*env)->NewGlobalRef(env, jni_ack);
    }

    /* all cleanup is done in the disconnect callback */
    aws_mqtt_client_connection_disconnect(connection->client_connection);
}

/*******************************************************************************
 * subscribe
 ******************************************************************************/
/* called from any sub, unsub, or pub ack */
static void s_deliver_ack_success(struct mqtt_jni_async_callback *callback) {
    assert(callback);
    assert(callback->connection);

    if (callback->async_callback) {
        JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
        (*env)->CallVoidMethod(env, callback->async_callback, s_async_callback.on_success);
    }
}

static void s_on_ack(struct aws_mqtt_client_connection *connection, uint16_t packet_id, void *user_data) {
    assert(connection);
    (void)packet_id;

    struct mqtt_jni_async_callback *callback = user_data;
    if (!callback) {
        return;
    }

    s_deliver_ack_success(callback);

    mqtt_jni_async_callback_clean_up(callback);
}

static void s_on_subscription_delivered(
    struct aws_mqtt_client_connection *connection,
    const struct aws_byte_cursor *topic,
    const struct aws_byte_cursor *payload,
    void *user_data) {
    assert(connection);
    assert(topic);
    assert(payload);
    assert(user_data);

    struct mqtt_jni_async_callback *callback = user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
    jbyteArray jni_payload = (*env)->NewByteArray(env, payload->len);
    (*env)->SetByteArrayRegion(env, jni_payload, 0, payload->len, (const signed char *)payload->ptr);
    (*env)->CallVoidMethod(env, callback->async_callback, s_message_handler.deliver, jni_payload);
    (*env)->DeleteLocalRef(env, jni_payload);
}

JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1subscribe(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jobject jni_handler,
    jobject jni_ack) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: Invalid connection");
        return 0;
    }

    if (!jni_handler) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: Invalid handler");
        return 0;
    }

    struct mqtt_jni_async_callback *handler = mqtt_jni_async_callback_new(connection, jni_handler);
    if (!handler) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: Unable to allocate handler");
        return 0;
    }

    /* from here, any failure requires error_cleanup */
    handler->connection = connection;
    handler->async_callback = (*env)->NewGlobalRef(env, jni_handler);

    struct mqtt_jni_async_callback *sub_ack = NULL;
    if (jni_ack) {
        sub_ack = mqtt_jni_async_callback_new(connection, jni_ack);
        if (!sub_ack) {
            aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: Unable to allocate sub ack");
            goto error_cleanup;
        }
    }

    /* pin the topic so it lives as long as the handler */
    jni_topic = (*env)->NewGlobalRef(env, jni_topic);
    aws_array_list_push_back(&handler->jni_objects, jni_topic);

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring(env, jni_topic);
    enum aws_mqtt_qos qos = jni_qos;

    uint16_t msg_id = aws_mqtt_client_connection_subscribe(
        connection->client_connection, &topic, qos, s_on_subscription_delivered, handler, s_on_ack, sub_ack);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(
            env, "MqttConnection.mqtt_subscribe: aws_mqtt_client_connection_subscribe failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (handler) {
        mqtt_jni_async_callback_clean_up(handler);
    }

    if (sub_ack) {
        mqtt_jni_async_callback_clean_up(sub_ack);
    }

    return 0;
}

/*******************************************************************************
 * unsubscribe
 ******************************************************************************/
JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1unsubscribe(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jstring jni_topic,
    jobject jni_ack) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_unsubscribe: Invalid connection");
        return 0;
    }

    struct mqtt_jni_async_callback *unsub_ack = mqtt_jni_async_callback_new(connection, jni_ack);
    if (!unsub_ack) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_unsubscribe: Unable to allocate unsub ack");
        goto error_cleanup;
    }

    /* pin the topic so it lives as long as the unsub request */
    jni_topic = (*env)->NewGlobalRef(env, jni_topic);
    aws_array_list_push_back(&unsub_ack->jni_objects, jni_topic);
    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring(env, jni_topic);

    uint16_t msg_id =
        aws_mqtt_client_connection_unsubscribe(connection->client_connection, &topic, s_on_ack, unsub_ack);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(
            env, "MqttConnection.mqtt_unsubscribe: aws_mqtt_client_connection_unsubscribe failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (unsub_ack) {
        mqtt_jni_async_callback_clean_up(unsub_ack);
    }
    return 0;
}

/*******************************************************************************
 * publish
 ******************************************************************************/
static void s_on_pub_ack(struct aws_mqtt_client_connection *connection, uint16_t packet_id, void *user_data) {
    assert(connection);
    struct mqtt_jni_async_callback *callback = (struct mqtt_jni_async_callback *)user_data;

    s_deliver_ack_success(callback);

    mqtt_jni_async_callback_clean_up(callback);
}

JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1publish(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jboolean jni_retain,
    jobject jni_payload,
    jobject jni_ack) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_publish: Invalid connection");
        return 0;
    }

    struct mqtt_jni_async_callback *pub_ack = mqtt_jni_async_callback_new(connection, jni_ack);
    if (!pub_ack) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_publish: Unable to allocate pub ack");
        goto error_cleanup;
    }

    /* pin topic to pub_ack's lifetime */
    jni_topic = (*env)->NewGlobalRef(env, jni_topic);
    aws_array_list_push_back(&pub_ack->jni_objects, jni_topic);
    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring(env, jni_topic);

    /* pin the buffer, to be released in pub ack, avoids a copy */
    jni_payload = (*env)->NewGlobalRef(env, jni_payload);
    aws_array_list_push_back(&pub_ack->jni_objects, jni_payload);
    struct aws_byte_cursor payload = aws_jni_byte_cursor_from_direct_byte_buffer(env, jni_payload);

    enum aws_mqtt_qos qos = jni_qos;
    bool retain = jni_retain != 0;

    uint16_t msg_id = aws_mqtt_client_connection_publish(
        connection->client_connection, &topic, qos, retain, &payload, s_on_pub_ack, pub_ack);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_publish: aws_mqtt_client_connection_publish failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (pub_ack) {
        mqtt_jni_async_callback_clean_up(pub_ack);
    }

    return 0;
}

JNIEXPORT jboolean JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1set_1will(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jboolean jni_retain,
    jobject jni_payload) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_set_will: Invalid connection");
        return 0;
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring(env, jni_topic);
    struct aws_byte_cursor payload = aws_jni_byte_cursor_from_direct_byte_buffer(env, jni_payload);

    /* exception will already have been thrown, so just return failure */
    if (!payload.ptr) {
        return false;
    }

    enum aws_mqtt_qos qos = jni_qos;
    bool retain = jni_retain != 0;

    int result = aws_mqtt_client_connection_set_will(connection->client_connection, &topic, qos, retain, &payload);
    return (result == AWS_OP_SUCCESS);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1set_1login(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jstring jni_user,
    jstring jni_pass) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_set_login: Invalid connection");
        return;
    }

    struct aws_byte_cursor username = aws_jni_byte_cursor_from_jstring(env, jni_user);
    struct aws_byte_cursor password = aws_jni_byte_cursor_from_jstring(env, jni_pass);

    if (aws_mqtt_client_connection_set_login(connection->client_connection, &username, &password)) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_set_login: Failed to set login");
    }
}

JNIEXPORT 
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1ping(JNIEnv *env, jclass jni_class, jlong jni_connection) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_ping: Invalid connection");
        return;
    }

    if (aws_mqtt_client_connection_ping(connection->client_connection)) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_ping: Failed to send ping");
    }
}

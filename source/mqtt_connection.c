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
/* fields of MqttConnection.ConnectOptions */
static struct {
    jfieldID endpoint_uri;
    jfieldID key_store_path;
    jfieldID certificate_file;
    jfieldID private_key_file;
    jfieldID use_websockets;
    jfieldID alpn;
    jfieldID client_id;
    jfieldID clean_session;
    jfieldID keep_alive_ms;
    jfieldID timeout_ms;
} s_connect_options = {0};

void s_cache_connect_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$ConnectOptions");
    assert(cls);
    s_connect_options.endpoint_uri = (*env)->GetFieldID(env, cls, "endpointUri", "Ljava/lang/String;");
    assert(s_connect_options.endpoint_uri);
    s_connect_options.key_store_path = (*env)->GetFieldID(env, cls, "keyStorePath", "Ljava/lang/String;");
    assert(s_connect_options.key_store_path);
    s_connect_options.certificate_file = (*env)->GetFieldID(env, cls, "certificateFile", "Ljava/lang/String;");
    assert(s_connect_options.certificate_file);
    s_connect_options.private_key_file = (*env)->GetFieldID(env, cls, "privateKeyFile", "Ljava/lang/String;");
    assert(s_connect_options.private_key_file);
    s_connect_options.use_websockets = (*env)->GetFieldID(env, cls, "useWebSockets", "Z");
    assert(s_connect_options.use_websockets);
    s_connect_options.alpn = (*env)->GetFieldID(env, cls, "alpn", "Ljava/lang/String;");
    assert(s_connect_options.alpn);
    s_connect_options.client_id = (*env)->GetFieldID(env, cls, "clientId", "Ljava/lang/String;");
    assert(s_connect_options.client_id);
    s_connect_options.clean_session = (*env)->GetFieldID(env, cls, "cleanSession", "Z");
    assert(s_connect_options.clean_session);
    s_connect_options.keep_alive_ms = (*env)->GetFieldID(env, cls, "keepAliveMs", "S");
    assert(s_connect_options.keep_alive_ms);
    s_connect_options.timeout_ms = (*env)->GetFieldID(env, cls, "timeout", "S");
    assert(s_connect_options.timeout_ms);
}

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
    s_message_handler.deliver = (*env)->GetMethodID(env, cls, "deliver", "(Ljava/lang/String;)V");
    assert(s_message_handler.deliver);
}

/*******************************************************************************
 * mqtt_jni_connection - represents an aws_mqtt_client_connection to Java
 ******************************************************************************/
struct mqtt_jni_connection {
    struct aws_socket_options socket_options;
    struct aws_mqtt_client *client; /* Provided to mqtt_connect */
    struct aws_mqtt_client_connection *client_connection;

    JavaVM *jvm;
    jobject client_callbacks; /* MqttConnection.ClientCallbacks */
    jobject connect_ack;      /* MqttConnection.AsyncCallback */
    jobject disconnect_ack;   /* MqttConnection.AsyncCallback */
};

/*******************************************************************************
 * mqtt_jni_async_callback - carries an AsyncCallback around as user data to mqtt
 * async ops, and is used to deliver callbacks
 ******************************************************************************/
struct mqtt_jni_async_callback {
    struct mqtt_jni_connection *connection;
    jobject async_callback;
};

/*******************************************************************************
 * connect
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
    if (connection->connect_ack) {
        (*env)->CallVoidMethod(env, connection->connect_ack, s_async_callback.on_success);
        (*env)->DeleteGlobalRef(env, connection->connect_ack);
        connection->connect_ack = NULL;
    }
    (*env)->CallVoidMethod(env, connection->client_callbacks, s_client_callbacks.on_connected);
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

    struct aws_allocator *allocator = aws_jni_get_allocator();
    /* client_connection will be cleaned up by channel shutdown */
    connection->client_connection = NULL;
    aws_mem_release(allocator, connection);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1connect(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong client_addr,
    jobject jni_options,
    jobject jni_client_callbacks,
    jobject jni_ack) {

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_mqtt_client *client = (struct aws_mqtt_client *)client_addr;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: Client is invalid/null");
        return (jlong)NULL;
    }
    if (!client->event_loop_group) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: EventLoopGroup is invalid/null");
        return (jlong)NULL;
    }
    if (!jni_options) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: ConnectOptions cannot be null");
        return (jlong)NULL;
    }

    struct aws_byte_cursor endpoint_uri =
        aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.endpoint_uri));
    struct aws_byte_cursor key_store_path = aws_jni_byte_cursor_from_jstring(
        env, (*env)->GetObjectField(env, jni_options, s_connect_options.key_store_path));
    struct aws_byte_cursor cert_path = aws_jni_byte_cursor_from_jstring(
        env, (*env)->GetObjectField(env, jni_options, s_connect_options.certificate_file));
    struct aws_byte_cursor key_path = aws_jni_byte_cursor_from_jstring(
        env, (*env)->GetObjectField(env, jni_options, s_connect_options.private_key_file));
    struct aws_byte_cursor alpn =
        aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.alpn));
    struct aws_byte_cursor client_id =
        aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.client_id));
    uint16_t keep_alive_ms = (uint16_t)(*env)->GetShortField(env, jni_options, s_connect_options.keep_alive_ms);
    uint16_t timeout_ms = (uint16_t)(*env)->GetShortField(env, jni_options, s_connect_options.timeout_ms);
    bool clean_session = (*env)->GetBooleanField(env, jni_options, s_connect_options.clean_session);

    uint16_t port = 0;
    char *port_str = strchr((char *)endpoint_uri.ptr, ':');
    if (port_str) {
        /* truncate down to just the hostname */
        endpoint_uri.len = port_str - (char *)endpoint_uri.ptr;
        *port_str = '\0';
        ++port_str;

        const char *cur = port_str;
        while (*cur) { /* ensure the port is all digits */
            if (!isdigit(*cur++)) {
                port_str = NULL;
                break;
            }
        }

        if (port_str) {
            port = (uint16_t)strtol(port_str, NULL, 10);
        }
    }

    if (!port) {
        aws_jni_throw_runtime_exception(
            env,
            "MqttConnection.mqtt_connect: Endpoint should be in the format hostname:port and port must be between 1 "
            "and 65535");
        return (jlong)NULL;
    }

    /* any error after this point needs to jump to error_cleanup */
    struct mqtt_jni_connection *connection = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_connection));
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: Out of memory allocating JNI connection");
        goto error_cleanup;
    }
    AWS_ZERO_STRUCT(*connection);

    struct aws_tls_ctx_options tls_ctx_opt;
    struct aws_tls_ctx_options *tls = NULL;
    if (cert_path.len > 0 || key_path.len > 0) {
        aws_tls_ctx_options_init_client_mtls(&tls_ctx_opt, (char *)cert_path.ptr, (char *)key_path.ptr);
        if (key_store_path.len > 0) {
            aws_tls_ctx_options_override_default_trust_store(&tls_ctx_opt, NULL, (char *)key_store_path.ptr);
        }
        if (alpn.len > 0) {
            aws_tls_ctx_options_set_alpn_list(&tls_ctx_opt, (char *)alpn.ptr);
        }
        tls = &tls_ctx_opt;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &connection->jvm);
    assert(jvmresult == 0);
    connection->client = client;
    connection->socket_options.connect_timeout_ms = (timeout_ms) ? timeout_ms : 3000;
    connection->socket_options.type = AWS_SOCKET_STREAM;

    struct aws_mqtt_client_connection_callbacks callbacks;
    AWS_ZERO_STRUCT(callbacks);
    callbacks.on_connection_failed = s_on_connect_failed;
    callbacks.on_connack = s_on_connect_success;
    callbacks.on_disconnect = s_on_disconnect;
    callbacks.user_data = connection;

    connection->client_connection = aws_mqtt_client_connection_new(
        connection->client, callbacks, &endpoint_uri, port, &connection->socket_options, tls);
    if (!connection->client_connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: aws_mqtt_client_connection_new failed, unable to create new connection");
        goto error_cleanup;
    }

    /* notify the JVM and retain references to the callback structures */
    connection->client_callbacks = (*env)->NewGlobalRef(env, jni_client_callbacks);
    connection->connect_ack = jni_ack ? (*env)->NewGlobalRef(env, jni_ack) : NULL;

    int result =
        aws_mqtt_client_connection_connect(connection->client_connection, &client_id, clean_session, keep_alive_ms);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: aws_mqtt_client_connection_connect failed");
        goto error_cleanup;
    }

    /* return connection to java */
    return (jlong)connection;

error_cleanup:
    if (connection) {
        if (connection->client_callbacks) {
            (*env)->DeleteGlobalRef(env, connection->client_callbacks);
        }
        if (connection->connect_ack) {
            (*env)->DeleteGlobalRef(env, connection->connect_ack);
        }
        if (connection->client_connection) {
            aws_mqtt_client_connection_disconnect(connection->client_connection);
            aws_mem_release(allocator, connection->client_connection);
        }
        aws_mem_release(allocator, connection);
    }

    return (jlong)NULL;
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
static void s_on_ack(struct aws_mqtt_client_connection* connection, uint16_t packet_id, void* user_data) {
    assert(connection);
    (void)packet_id;

    struct mqtt_jni_async_callback *callback = user_data;
    if (!callback) {
        return;
    }

    JNIEnv* env = aws_jni_get_thread_env(callback->connection->jvm);
    (*env)->CallVoidMethod(env, callback->async_callback, s_async_callback.on_success);
    (*env)->DeleteGlobalRef(env, callback->async_callback);

    struct aws_allocator* allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback);
}

static void s_on_subscription_delivered(struct aws_mqtt_client_connection* connection, const struct aws_byte_cursor* topic, const struct aws_byte_cursor* payload, void *user_data) {
    assert(connection);
    assert(topic);
    assert(payload);
    assert(user_data);

    struct mqtt_jni_async_callback *callback = user_data;
    JNIEnv* env = aws_jni_get_thread_env(callback->connection->jvm);
    jstring jni_payload = (*env)->NewStringUTF(env, (const char*)payload->ptr);
    (*env)->CallVoidMethod(env, callback->async_callback, s_message_handler.deliver, jni_payload);
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

    struct aws_allocator* allocator = aws_jni_get_allocator();

    struct mqtt_jni_async_callback *handler = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_async_callback));
    if (!handler) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: Unable to allocate handler");
        return 0;
    }

    /* from here, any failure requires error_cleanup */
    handler->connection = connection;
    handler->async_callback = (*env)->NewGlobalRef(env, jni_handler);

    struct mqtt_jni_async_callback *sub_ack = NULL;
    if (jni_ack) {
        sub_ack = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_async_callback));
        if (!sub_ack) {
            aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: Unable to allocate sub ack");
            goto error_cleanup;
        }
        sub_ack->connection = connection;
        sub_ack->async_callback = (*env)->NewGlobalRef(env, jni_ack);
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring(env, jni_topic);
    enum aws_mqtt_qos qos = jni_qos;

    uint16_t msg_id = aws_mqtt_client_connection_subscribe(connection->client_connection, &topic, qos, s_on_subscription_delivered, handler, s_on_ack, sub_ack);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_subscribe: aws_mqtt_client_connection_subscribe failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (handler) {
        (*env)->DeleteGlobalRef(env, handler->async_callback);
        aws_mem_release(allocator, handler);
    }

    if (sub_ack) {
        (*env)->DeleteGlobalRef(env, sub_ack->async_callback);
        aws_mem_release(allocator, sub_ack);
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

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct mqtt_jni_async_callback *unsub_ack = NULL;
    if (jni_ack) {
        unsub_ack = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_async_callback));
        if (!unsub_ack) {
            aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_unsubscribe: Unable to allocate unsub ack");
            goto error_cleanup;
        }
        unsub_ack->connection = connection;
        unsub_ack->async_callback = (*env)->NewGlobalRef(env, jni_ack);
    }

    struct aws_byte_cursor topic = aws_jni_byte_cursor_from_jstring(env, jni_topic);

    uint16_t msg_id = aws_mqtt_client_connection_unsubscribe(connection->client_connection, &topic, s_on_ack, unsub_ack);
    if (msg_id == 0) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_unsubscribe: aws_mqtt_client_connection_unsubscribe failed");
        goto error_cleanup;
    }

    return msg_id;

error_cleanup:
    if (unsub_ack) {
        (*env)->DeleteGlobalRef(env, unsub_ack->async_callback);
        aws_mem_release(allocator, unsub_ack);
    }
    return 0;
}

/*******************************************************************************
 * publish
 ******************************************************************************/
JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1publish(
    JNIEnv *env,
    jclass jni_mqtt,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jstring jni_payload,
    jobject jni_ack) {
    return 0;
}

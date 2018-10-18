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
#include <aws/mqtt/mqtt.h>

#include <string.h>
#include <ctype.h>

#include "crt.h"

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

static void s_cache_connect_options(JNIEnv* env) {
    if (AWS_LIKELY(s_connect_options.endpoint_uri)) {
        return;
    }
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$ConnectOptions");
    assert(cls);
    s_connect_options.endpoint_uri = (*env)->GetFieldID(env, cls, "endpointUri", "Ljava/lang/String;");
    s_connect_options.key_store_path = (*env)->GetFieldID(env, cls, "keyStorePath", "Ljava/lang/String;");
    s_connect_options.certificate_file = (*env)->GetFieldID(env, cls, "certificateFile", "Ljava/lang/String;");
    s_connect_options.private_key_file = (*env)->GetFieldID(env, cls, "privateKeyFile", "Ljava/lang/String;");
    s_connect_options.use_websockets = (*env)->GetFieldID(env, cls, "useWebSockets", "Z");
    s_connect_options.alpn = (*env)->GetFieldID(env, cls, "alpn", "Ljava/lang/String;");
    s_connect_options.client_id = (*env)->GetFieldID(env, cls, "clientId", "Ljava/lang/String;");
    s_connect_options.clean_session = (*env)->GetFieldID(env, cls, "cleanSession", "Z");
    s_connect_options.keep_alive_ms = (*env)->GetFieldID(env, cls, "keepAliveMs", "S");
    s_connect_options.timeout_ms = (*env)->GetFieldID(env, cls, "timeout", "S");
}

/* methods of MqttConnection.AsyncCallback */
static struct {
    jmethodID on_success;
    jmethodID on_failure;
} s_async_callback = {0};

static void s_cache_async_callback(JNIEnv* env) {
    if (AWS_LIKELY(s_async_callback.on_success)) {
        return;
    }
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$AsyncCallback");
    assert(cls);
    s_async_callback.on_success = (*env)->GetMethodID(env, cls, "onSuccess", "()V");
    s_async_callback.on_failure = (*env)->GetMethodID(env, cls, "onFailure", "(Ljava/lang/String;)V");
}

/* methods of MqttConnection.ClientCallbacks */
static struct {
    jmethodID on_connected;
    jmethodID on_disconnected;
} s_client_callbacks;

static void s_cache_client_callbacks(JNIEnv* env) {
    if (AWS_LIKELY(s_client_callbacks.on_connected)) {
        return;
    }
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$ClientCallbacks");
    assert(cls);
    s_client_callbacks.on_connected = (*env)->GetMethodID(env, cls, "onConnected", "()V");
    s_client_callbacks.on_disconnected = (*env)->GetMethodID(env, cls, "onDisconnected", "(Ljava/lang/String;)V");
}

static void s_cache_jni_classes(JNIEnv* env) {
    s_cache_connect_options(env);
    s_cache_async_callback(env);
    s_cache_client_callbacks(env);
}

struct mqtt_jni_connection {
    struct aws_socket_options socket_options;
    struct aws_mqtt_client client;
    struct aws_mqtt_client_connection *client_connection;

    JNIEnv *env;
    jobject client_callbacks; /* MqttConnection.ClientCallbacks */
    jobject connect_callback; /* MqttConnection.AsyncCallback */
};

struct mqtt_jni_callback {
    JNIEnv *env;
    jobject async_callback;
};

static void s_on_connect_failed(struct aws_mqtt_client_connection *client_connection, int error_code, void *user_data) {
    (void)client_connection;

    struct mqtt_jni_connection *connection = user_data;
    JNIEnv *env = connection->env;
    char buf[1024];
    snprintf(buf, sizeof(buf), "Connection failed with code: %d", error_code);
    jstring message = (*env)->NewStringUTF(env, buf);
    if (connection->connect_callback) {
        (*env)->CallVoidMethod(env, connection->connect_callback, s_async_callback.on_failure, message);
        (*env)->DeleteGlobalRef(env, connection->connect_callback);
        connection->connect_callback = NULL;
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
    JNIEnv *env = connection->env;
    if (connection->connect_callback) {
        (*env)->CallVoidMethod(env, connection->connect_callback, s_async_callback.on_success);
        (*env)->DeleteGlobalRef(env, connection->connect_callback);
        connection->connect_callback = NULL;
    }
    (*env)->CallVoidMethod(env, connection->client_callbacks, s_client_callbacks.on_connected);
}

static void s_on_disconnect(struct aws_mqtt_client_connection *client_connection, int error_code, void *user_data) {

    (void)client_connection;
    (void)error_code;

    struct mqtt_jni_connection *connection = user_data;
    JNIEnv *env = connection->env;
    char buf[1024];
    snprintf(buf, sizeof(buf), "Disconnected with code: %d", error_code);
    jstring message = (*env)->NewStringUTF(env, buf);
    (*env)->CallVoidMethod(env, connection->client_callbacks, s_client_callbacks.on_disconnected, message);
    (*env)->DeleteGlobalRef(env, connection->client_callbacks);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, connection->client_connection);
    aws_mem_release(allocator, connection);
}

JNIEXPORT 
jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqtt_1connect(
    JNIEnv *env,
    jclass jni_mqtt, jlong elg_addr, jobject jni_options, jobject jni_client_callbacks, jobject jni_connect_callback) 
{
    s_cache_jni_classes(env);
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_addr;
    if (!elg) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: EventLoopGroup is invalid/null");
        return (jlong)NULL;
    }
    if (!jni_options) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: ConnectOptions cannot be null");
        return (jlong)NULL;
    }

    struct aws_byte_cursor endpoint_uri = aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.endpoint_uri));
    struct aws_byte_cursor key_store_path = aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.key_store_path));
    struct aws_byte_cursor cert_path = aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.certificate_file));
    struct aws_byte_cursor key_path = aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.private_key_file));
    struct aws_byte_cursor alpn = aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.alpn));
    struct aws_byte_cursor client_id = aws_jni_byte_cursor_from_jstring(env, (*env)->GetObjectField(env, jni_options, s_connect_options.client_id));
    uint16_t keep_alive_ms = (*env)->GetShortField(env, jni_options, s_connect_options.keep_alive_ms);
    uint16_t timeout_ms = (*env)->GetShortField(env, jni_options, s_connect_options.timeout_ms);
    bool clean_session = (*env)->GetBooleanField(env, jni_options, s_connect_options.clean_session);

    uint16_t port = 0;
    const char *port_str = strchr((char *)endpoint_uri.ptr, ':');
    if (port_str) {
        /* truncate down to just the hostname */
        endpoint_uri.len = port_str - (char*)endpoint_uri.ptr;

        const char *cur = port_str + 1; /* skip ':' */
        while (*cur) { /* ensure the port is all digits */
            if (!isdigit(*cur++)) {
                port_str = NULL;
                break;
            }
        }

        if (port_str) {
            port = (uint16_t)atoi(port_str);
        }
    }
    
    if (!port) {
        aws_jni_throw_runtime_exception(env, "Endpoint should be in the format hostname:port and port must be between 1 and 65535");
        return (jlong)NULL;
    }

    /* any error after this point needs to jump to error_cleanup */
    struct mqtt_jni_connection *connection = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_connection));
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "Out of memory allocating JNI connection");
        goto error_cleanup;
    }
    AWS_ZERO_STRUCT(*connection);

    struct aws_tls_ctx_options tls_ctx_opt;
    aws_tls_ctx_options_init_client_mtls(&tls_ctx_opt, (char*)cert_path.ptr, (char*)key_path.ptr);
    if (key_store_path.len > 0) {
        aws_tls_ctx_options_override_default_trust_store(&tls_ctx_opt, NULL, (char*)key_store_path.ptr);
    }
    if (alpn.len > 0) {
        aws_tls_ctx_options_set_alpn_list(&tls_ctx_opt, (char*)alpn.ptr);
    }

    connection->socket_options.connect_timeout_ms = (timeout_ms) ? timeout_ms : 3000;
    connection->socket_options.type = AWS_SOCKET_STREAM;
    connection->socket_options.domain = AWS_SOCKET_IPV4;

    struct aws_mqtt_client_connection_callbacks callbacks;
    AWS_ZERO_STRUCT(callbacks);
    callbacks.on_connection_failed = s_on_connect_failed;
    callbacks.on_connack = s_on_connect_success;
    callbacks.on_disconnect = s_on_disconnect;
    callbacks.user_data = connection;

    int result = aws_mqtt_client_init(&connection->client, allocator, elg);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "aws_mqtt_client_init failed");
        goto error_cleanup;
    }

    connection->client_connection = aws_mqtt_client_connection_new(&connection->client, callbacks, &endpoint_uri, port, &connection->socket_options, &tls_ctx_opt);
    if (!connection->client_connection) {
        aws_jni_throw_runtime_exception(env, "Out of memory allocating client connection");
        goto error_cleanup;
    }

    /* notify the JVM and retain references to the callback structures */
    connection->client_callbacks = (*env)->NewGlobalRef(env, jni_client_callbacks);
    connection->connect_callback = (*env)->NewGlobalRef(env, jni_connect_callback);

    result = aws_mqtt_client_connection_connect(connection->client_connection, &client_id, clean_session, keep_alive_ms);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "aws_mqtt_client_connection_connect failed");
        goto error_cleanup;
    }

    /* return connection to java */
    return (jlong)connection;

error_cleanup:
    if (connection) {
        if (connection->client_connection) {
            aws_mqtt_client_connection_disconnect(connection->client_connection);
            aws_mem_release(allocator, connection->client_connection);
        }
        aws_mem_release(allocator, connection);
    }

    return (jlong)NULL;
}

JNIEXPORT 
void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1disconnect(JNIEnv *env, jclass jni_mqtt, jlong jni_connection) {
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "Invalid connection");
        return;
    }

    /* all cleanup is done in the disconnect callback */
    aws_mqtt_client_connection_disconnect(connection->client_connection);
}

JNIEXPORT 
void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1subscribe(JNIEnv *env, jclass jni_mqtt, jlong jni_connection) {

}

JNIEXPORT 
void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1unsubscribe(JNIEnv *env, jclass jni_mqtt, jlong jni_connection) {

}

JNIEXPORT 
void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1publish(JNIEnv *env, jclass jni_mqtt, jlong jni_connection) {

}

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
    s_async_callback.on_failure = (*env)->GetMethodID(env, cls, "onFailure", "(Ljava/lang/Throwable;)V");
    assert(s_async_callback.on_failure);
}

/* methods of MqttConnection */
static struct {
    jmethodID on_connection_complete;
    jmethodID on_connection_interrupted;
    jmethodID on_connection_resumed;
} s_mqtt_connection;

void s_cache_mqtt_connection(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection");
    assert(cls);
    s_mqtt_connection.on_connection_complete = (*env)->GetMethodID(env, cls, "onConnectionComplete", "(IZ)V");
    assert(s_mqtt_connection.on_connection_complete);
    s_mqtt_connection.on_connection_interrupted = (*env)->GetMethodID(env, cls, "onConnectionInterrupted", "(I)V");
    assert(s_mqtt_connection.on_connection_interrupted);
    s_mqtt_connection.on_connection_resumed = (*env)->GetMethodID(env, cls, "onConnectionResumed", "(Z)V");
    assert(s_mqtt_connection.on_connection_resumed);
}

/* MqttConnection.MessageHandler */
static struct { jmethodID deliver; } s_message_handler;

void s_cache_message_handler(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttConnection$MessageHandler");
    assert(cls);
    s_message_handler.deliver = (*env)->GetMethodID(env, cls, "deliver", "([B)V");
    assert(s_message_handler.deliver);
}

static struct {
    jclass jni_mqtt_exception;
    jmethodID jni_constructor;
} s_mqtt_exception;

void s_cache_mqtt_exception(JNIEnv *env) {
    s_mqtt_exception.jni_mqtt_exception = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttException");
    assert(s_mqtt_exception.jni_mqtt_exception);
    s_mqtt_exception.jni_constructor = (*env)->GetMethodID(env, s_mqtt_exception.jni_mqtt_exception, "<init>", "(I)V");
    assert(s_mqtt_exception.jni_mqtt_exception);
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
    jobject mqtt_connection; /* MqttConnection instance */

    bool disconnect_requested;
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
static void s_on_connection_complete(
    struct aws_mqtt_client_connection *client_connection,
    int error_code,
    enum aws_mqtt_connect_return_code return_code,
    bool session_present,
    void *user_data) {
    (void)client_connection;
    (void)return_code;

    struct mqtt_jni_connection *connection = user_data;
    if (connection->mqtt_connection) {
        JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
        (*env)->CallVoidMethod(
            env, connection->mqtt_connection, s_mqtt_connection.on_connection_complete, error_code, session_present);
    }
}

static void s_on_connection_interrupted(
    struct aws_mqtt_client_connection *client_connection,
    int error_code,
    void *user_data) {
    (void)client_connection;

    struct mqtt_jni_connection *connection = user_data;

    if (connection->mqtt_connection) {
        JNIEnv *env = aws_jni_get_thread_env(connection->jvm);
        (*env)->CallVoidMethod(
            env, connection->mqtt_connection, s_mqtt_connection.on_connection_interrupted, error_code);
    }
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
    }
}

static void s_on_connection_disconnected(struct aws_mqtt_client_connection *client_connection, void *user_data) {
    s_on_connection_interrupted(client_connection, 0, user_data);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_mqtt_connection) {
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_mqtt_client *client = (struct aws_mqtt_client *)jni_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_new: Client is invalid/null");
        return (jlong)NULL;
    }

    /* any error after this point needs to jump to error_cleanup */
    struct mqtt_jni_connection *connection = aws_mem_acquire(allocator, sizeof(struct mqtt_jni_connection));
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: Out of memory allocating JNI connection");
        goto error_cleanup;
    }
    AWS_ZERO_STRUCT(*connection);
    connection->client = client;
    connection->mqtt_connection = (*env)->NewGlobalRef(env, jni_mqtt_connection);
    connection->disconnect_requested = false;
    jint jvmresult = (*env)->GetJavaVM(env, &connection->jvm);
    (void)jvmresult;
    assert(jvmresult == 0);

    connection->client_connection = aws_mqtt_client_connection_new(connection->client);
    if (!connection->client_connection) {
        aws_jni_throw_runtime_exception(
            env, "MqttConnection.mqtt_connect: aws_mqtt_client_connection_new failed, unable to create new connection");
        goto error_cleanup;
    }
    aws_mqtt_client_connection_set_connection_interruption_handlers(
        connection->client_connection, s_on_connection_interrupted, connection, s_on_connection_resumed, connection);

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
JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)env;
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        return;
    }
    if (connection->mqtt_connection) {
        (*env)->DeleteGlobalRef(env, connection->mqtt_connection);
    }
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, connection);
}

/*******************************************************************************
 * connect
 ******************************************************************************/
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionConnect(
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
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: Connection is invalid/null");
        return;
    }

    struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jstring(env, jni_endpoint);
    uint16_t port = jni_port;
    if (!port) {
        aws_jni_throw_runtime_exception(
            env,
            "MqttConnection.mqtt_new: Endpoint should be in the format hostname:port and port must be between 1 "
            "and 65535");
        return;
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

    struct aws_byte_cursor client_id = aws_jni_byte_cursor_from_jstring(env, jni_client_id);
    bool clean_session = jni_clean_session != 0;

    struct aws_mqtt_connection_options connect_options = {
        .host_name = endpoint,
        .port = port,
        .socket_options = &connection->socket_options,
        .tls_options = tls_options,
        .client_id = client_id,
        .keep_alive_time_secs = keep_alive_ms / 1000,
        .ping_timeout_ms = ping_timeout_ms,
        .clean_session = clean_session,
        .on_connection_complete = s_on_connection_complete,
        .user_data = connection,
    };

    int result = aws_mqtt_client_connection_connect(connection->client_connection, &connect_options);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_connect: aws_mqtt_client_connection_connect failed");
    }
}

/*******************************************************************************
 * disconnect
 ******************************************************************************/
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionDisconnect(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_disconnect: Invalid connection");
        return;
    }

    connection->disconnect_requested = true;

    aws_mqtt_client_connection_disconnect(connection->client_connection, s_on_connection_disconnected, connection);
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

static void s_deliver_ack_failure(struct mqtt_jni_async_callback *callback, int error_code) {
    assert(callback);
    assert(callback->connection);

    if (callback->async_callback) {
        JNIEnv *env = aws_jni_get_thread_env(callback->connection->jvm);
        jobject jni_reason = s_new_mqtt_exception(env, error_code);
        (*env)->CallVoidMethod(env, callback->async_callback, s_async_callback.on_failure, jni_reason);
        (*env)->DeleteLocalRef(env, jni_reason);
    }
}

static void s_on_op_complete(
    struct aws_mqtt_client_connection *connection,
    uint16_t packet_id,
    int error_code,
    void *user_data) {
    assert(connection);
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

    mqtt_jni_async_callback_clean_up(callback);
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
    mqtt_jni_async_callback_clean_up(handler);
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
    jbyteArray jni_payload = (*env)->NewByteArray(env, (jsize)payload->len);
    (*env)->SetByteArrayRegion(env, jni_payload, 0, (jsize)payload->len, (const signed char *)payload->ptr);
    (*env)->CallVoidMethod(env, callback->async_callback, s_message_handler.deliver, jni_payload);
    (*env)->DeleteLocalRef(env, jni_payload);
}

JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionSubscribe(
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
        connection->client_connection,
        &topic,
        qos,
        s_on_subscription_delivered,
        handler,
        s_cleanup_handler,
        s_on_ack,
        sub_ack);
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
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionUnsubscribe(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jobject jni_ack) {
    (void)jni_class;
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
        aws_mqtt_client_connection_unsubscribe(connection->client_connection, &topic, s_on_op_complete, unsub_ack);
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
JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionPublish(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jboolean jni_retain,
    jobject jni_payload,
    jobject jni_ack) {
    (void)jni_class;
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
        connection->client_connection, &topic, qos, retain, &payload, s_on_op_complete, pub_ack);
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

JNIEXPORT jboolean JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionSetWill(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_topic,
    jint jni_qos,
    jboolean jni_retain,
    jobject jni_payload) {
    (void)jni_class;
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

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionSetLogin(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jstring jni_user,
    jstring jni_pass) {
    (void)jni_class;
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
void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttConnection_mqttConnectionPing(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {
    (void)jni_class;
    struct mqtt_jni_connection *connection = (struct mqtt_jni_connection *)jni_connection;
    if (!connection) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_ping: Invalid connection");
        return;
    }

    if (aws_mqtt_client_connection_ping(connection->client_connection)) {
        aws_jni_throw_runtime_exception(env, "MqttConnection.mqtt_ping: Failed to send ping");
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

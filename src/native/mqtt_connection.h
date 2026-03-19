/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#ifndef AWS_JAVA_CRT_MQTT_CONNECTION_H
#define AWS_JAVA_CRT_MQTT_CONNECTION_H

#include <jni.h>

#include <aws/common/atomics.h>
#include <aws/common/byte_buf.h>
#include <aws/io/socket.h>
#include <aws/io/tls_channel_handler.h>

struct aws_mqtt_client;
struct aws_mqtt_client_connection;
struct mqtt_jni_connection;

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

    JavaVM *jvm;
    jobject java_mqtt_connection; /* MqttClientConnection instance */
    struct mqtt_jni_async_callback *on_message;

    struct aws_atomic_var ref_count;
};

#endif /* AWS_JAVA_CRT_MQTT_CONNECTION_H */

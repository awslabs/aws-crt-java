/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/mqtt/client.h>

#include <crt.h>

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

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_mqtt_MqttClient_mqttClientNew(JNIEnv *env, jclass jni_class, jlong jni_bootstrap) {
    (void)jni_class;
    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)jni_bootstrap;
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(env, "Invalid ClientBootstrap");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_mqtt_client *client = aws_mqtt_client_new(allocator, bootstrap);
    if (client == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_init: aws_mqtt_client_new failed");
        return (jlong)NULL;
    }

    return (jlong)client;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClient_mqttClientDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_mqtt_client) {
    (void)jni_class;
    struct aws_mqtt_client *client = (struct aws_mqtt_client *)jni_mqtt_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_destroy: Invalid/null client");
        return;
    }

    aws_mqtt_client_release(client);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

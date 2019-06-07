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
    struct aws_mqtt_client *client = aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_client));
    if (!client) {
        aws_jni_throw_runtime_exception(
            env, "MqttClient.mqtt_client_init: aws_mem_calloc failed, unable to allocate new aws_mqtt_client");
        return (jlong)NULL;
    }

    AWS_ZERO_STRUCT(*client);

    int result = aws_mqtt_client_init(client, allocator, bootstrap);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_init: aws_mqtt_client_init failed");
        goto error_cleanup;
    }

    return (jlong)client;

error_cleanup:
    if (client) {
        aws_mqtt_client_clean_up(client);
        aws_mem_release(allocator, client);
    }

    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClient_mqttClientDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_mqtt_client) {
    (void)jni_class;
    struct aws_mqtt_client *client = (struct aws_mqtt_client *)jni_mqtt_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_clean_up: Invalid/null client");
        return;
    }

    aws_mqtt_client_clean_up(client);
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, client);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

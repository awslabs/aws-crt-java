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

#include "java_class_ids.h"
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

struct mqtt_client_binding {
    JavaVM *jvm;
    struct aws_mqtt_client *client;
    jobject java_client_bootstrap;
    jobject java_tls_context;
};

static void s_mqtt_client_binding_destroy(JNIEnv *env, struct mqtt_client_binding *binding) {
    if (binding == NULL) {
        return;
    }

    if (binding->java_client_bootstrap != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_client_bootstrap);
    }

    if (binding->java_tls_context != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_tls_context);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static struct mqtt_client_binding *s_mqtt_client_binding_new(
    JNIEnv *env,
    jobject java_client_bootstrap,
    jobject java_tls_context,
    struct aws_mqtt_client *client) {
    struct mqtt_client_binding *binding =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct mqtt_client_binding));
    if (binding == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->client = client;
    binding->java_client_bootstrap = (*env)->NewGlobalRef(env, java_client_bootstrap);
    if (binding->java_client_bootstrap != NULL) {
        goto on_error;
    }

    if (java_tls_context != NULL) {
        binding->java_tls_context = (*env)->NewGlobalRef(env, java_tls_context);
        if (binding->java_tls_context != NULL) {
            goto on_error;
        }
    }

    return binding;

on_error:

    s_mqtt_client_binding_destroy(env, binding);

    return NULL;
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClient_mqttClientNew(
    JNIEnv *env,
    jclass jni_class,
    jobject java_client_bootstrap,
    jobject java_tls_context) {
    (void)jni_class;

    struct aws_mqtt_client *client = NULL;
    struct mqtt_client_binding *binding = NULL;

    if (java_client_bootstrap == NULL) {
        aws_jni_throw_runtime_exception(env, "Invalid ClientBootstrap");
        return (jlong)NULL;
    }

    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)(*env)->CallLongMethod(
        env, java_client_bootstrap, crt_resource_properties.get_native_handle_method_id);
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(env, "Invalid ClientBootstrap");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    client = aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_client));
    if (!client) {
        aws_jni_throw_runtime_exception(
            env, "MqttClient.mqtt_client_init: aws_mem_calloc failed, unable to allocate new aws_mqtt_client");
        return (jlong)NULL;
    }

    int result = aws_mqtt_client_init(client, allocator, bootstrap);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_init: aws_mqtt_client_init failed");
        goto error_cleanup;
    }

    binding = s_mqtt_client_binding_new(env, java_client_bootstrap, java_tls_context, client);
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_init: failed to create binding");
        goto error_cleanup;
    }

    return (jlong)binding;

error_cleanup:

    if (client) {
        aws_mqtt_client_clean_up(client);
        aws_mem_release(allocator, client);
    }

    if (binding) {
        s_mqtt_client_binding_destroy(env, binding);
    }

    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt_MqttClient_mqttClientDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_mqtt_client_binding) {
    (void)jni_class;
    struct mqtt_client_binding *binding = (struct mqtt_client_binding *)jni_mqtt_client_binding;
    if (!binding) {
        aws_jni_throw_runtime_exception(env, "MqttClient.mqtt_client_clean_up: Invalid/null client binding");
        return;
    }

    aws_mqtt_client_clean_up(binding->client);

    s_mqtt_client_binding_destroy(env, binding);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

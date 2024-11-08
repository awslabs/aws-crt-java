/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/mqtt/request-response/request_response_client.h>

#include "crt.h"

#include "java_class_ids.h"
#include "mqtt5_client_jni.h"
#include "mqtt_connection.h"

struct aws_crt_mqtt_request_response_client_binding {
    struct aws_allocator *allocator;
    JavaVM *jvm;

    struct aws_mqtt_request_response_client *client;
    jobject java_client;
};

static void s_destroy_mqtt_request_response_client_binding(void *context) {
    struct aws_crt_mqtt_request_response_client_binding *binding = context;
    if (binding == NULL) {
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = binding->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT5_CLIENT, "??");
        goto done;
    }

    if (binding->java_client) {
        (*env)->DeleteGlobalRef(env, binding->java_client);
    }

    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);

done:

    aws_mem_release(binding->allocator, binding);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_iot_MqttRequestResponseClient_mqttRequestResponseClientNewFrom5(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_mqtt_request_response_client,
    jlong protocol_client_binding,
    jint max_request_response_subscriptions,
    jint max_streaming_subscriptions,
    jint operation_timeout_seconds) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    if (max_request_response_subscriptions < 0 || max_streaming_subscriptions < 0 || operation_timeout_seconds < 0) {
        aws_jni_throw_runtime_exception(env, "??");
        return (jlong)NULL;
    }

    struct aws_mqtt5_client_java_jni *mqtt_client_binding = (struct aws_mqtt5_client_java_jni *)protocol_client_binding;
    if (mqtt_client_binding == NULL) {
        aws_jni_throw_runtime_exception(env, "??");
        return (jlong)NULL;
    }

    struct aws_mqtt5_client *protocol_client = mqtt_client_binding->client;
    if (protocol_client == NULL) {
        aws_jni_throw_runtime_exception(env, "??");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_crt_mqtt_request_response_client_binding *binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_crt_mqtt_request_response_client_binding));
    binding->allocator = allocator;

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "??");
        goto error;
    }

    binding->java_client = (*env)->NewGlobalRef(env, jni_mqtt_request_response_client);
    if (!binding->java_client) {
        aws_jni_throw_runtime_exception(env, "??");
        goto error;
    }

    struct aws_mqtt_request_response_client_options options = {
        .max_request_response_subscriptions = (size_t)max_request_response_subscriptions,
        .max_streaming_subscriptions = (size_t)max_streaming_subscriptions,
        .operation_timeout_seconds = (uint32_t)operation_timeout_seconds,
        .terminated_callback = s_destroy_mqtt_request_response_client_binding,
        .user_data = binding,
    };

    binding->client = aws_mqtt_request_response_client_new_from_mqtt5_client(allocator, protocol_client, &options);
    if (binding->client == NULL) {
        aws_jni_throw_runtime_exception(env, "??");
        goto error;
    }

    return (jlong)binding;

error:

    s_destroy_mqtt_request_response_client_binding(binding);

    return (jlong)NULL;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_iot_MqttRequestResponseClient_mqttRequestResponseClientNewFrom311(
        JNIEnv *env,
        jclass jni_class,
        jobject jni_mqtt_request_response_client,
        jlong protocol_client_binding,
        jint max_request_response_subscriptions,
        jint max_streaming_subscriptions,
        jint operation_timeout_seconds) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    if (max_request_response_subscriptions < 0 || max_streaming_subscriptions < 0 || operation_timeout_seconds < 0) {
        aws_jni_throw_runtime_exception(env, "??");
        return (jlong)NULL;
    }

    struct mqtt_jni_connection *mqtt_connection_binding = (struct mqtt_jni_connection *)protocol_client_binding;
    if (mqtt_connection_binding == NULL) {
        aws_jni_throw_runtime_exception(env, "??");
        return (jlong)NULL;
    }

    struct aws_mqtt_client_connection *protocol_client = mqtt_connection_binding->client_connection;
    if (protocol_client == NULL) {
        aws_jni_throw_runtime_exception(env, "??");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_crt_mqtt_request_response_client_binding *binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_crt_mqtt_request_response_client_binding));
    binding->allocator = allocator;

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "??");
        goto error;
    }

    binding->java_client = (*env)->NewGlobalRef(env, jni_mqtt_request_response_client);
    if (!binding->java_client) {
        aws_jni_throw_runtime_exception(env, "??");
        goto error;
    }

    struct aws_mqtt_request_response_client_options options = {
        .max_request_response_subscriptions = (size_t)max_request_response_subscriptions,
        .max_streaming_subscriptions = (size_t)max_streaming_subscriptions,
        .operation_timeout_seconds = (uint32_t)operation_timeout_seconds,
        .terminated_callback = s_destroy_mqtt_request_response_client_binding,
        .user_data = binding,
    };

    binding->client = aws_mqtt_request_response_client_new_from_mqtt311_client(allocator, protocol_client, &options);
    if (binding->client == NULL) {
        aws_jni_throw_runtime_exception(env, "??");
        goto error;
    }

    return (jlong)binding;

error:

    s_destroy_mqtt_request_response_client_binding(binding);

    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_iot_MqttRequestResponseClient_mqttRequestResponseClientDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_mqtt_request_response_client_handle) {
    (void)jni_class;
    (void)env;

    struct aws_crt_mqtt_request_response_client_binding *binding =
        (struct aws_crt_mqtt_request_response_client_binding *)jni_mqtt_request_response_client_handle;
    if (binding != NULL) {
        aws_mqtt_request_response_client_release(binding->client);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

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
        AWS_LOGF_ERROR(AWS_LS_MQTT5_CLIENT, "JNI env no longer resolvable; JVM likely shutting down");
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

    if (max_request_response_subscriptions < 2 || max_streaming_subscriptions < 0 || operation_timeout_seconds < 0) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom5: Invalid configuration value");
        return (jlong)NULL;
    }

    struct aws_mqtt5_client_java_jni *mqtt_client_binding = (struct aws_mqtt5_client_java_jni *)protocol_client_binding;
    if (mqtt_client_binding == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom5: null protocol client binding");
        return (jlong)NULL;
    }

    struct aws_mqtt5_client *protocol_client = mqtt_client_binding->client;
    if (protocol_client == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom5: null protocol client");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_crt_mqtt_request_response_client_binding *binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_crt_mqtt_request_response_client_binding));
    binding->allocator = allocator;

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom5: failed to get JVM");
        goto error;
    }

    binding->java_client = (*env)->NewGlobalRef(env, jni_mqtt_request_response_client);
    if (!binding->java_client) {
        aws_jni_throw_runtime_exception(
            env, "MqttRequestResponseClientNewFrom5: failed to acquire strong ref to client jobject");
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
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom5: failed to create native client");
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
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom311: Invalid configuration value");
        return (jlong)NULL;
    }

    struct mqtt_jni_connection *mqtt_connection_binding = (struct mqtt_jni_connection *)protocol_client_binding;
    if (mqtt_connection_binding == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom311: null protocol client binding");
        return (jlong)NULL;
    }

    struct aws_mqtt_client_connection *protocol_client = mqtt_connection_binding->client_connection;
    if (protocol_client == NULL) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom311: null protocol client");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_crt_mqtt_request_response_client_binding *binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_crt_mqtt_request_response_client_binding));
    binding->allocator = allocator;

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom311: failed to get JVM");
        goto error;
    }

    binding->java_client = (*env)->NewGlobalRef(env, jni_mqtt_request_response_client);
    if (!binding->java_client) {
        aws_jni_throw_runtime_exception(
            env, "MqttRequestResponseClientNewFrom311: failed to acquire strong ref to client jobject");
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
        aws_jni_throw_runtime_exception(env, "MqttRequestResponseClientNewFrom311: failed to create native client");
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

struct aws_request_response_operation_binding {
    struct aws_allocator *allocator;

    jweak operation_future;
};

/*
All cursors in these structures are from calls to aws_jni_byte_cursor_from_jstring_acquire and the like which means
non-null ptrs must be released with the appropriate JNI call, hence the cleanup implementation.
*/

struct aws_jni_response_path {
    jstring java_response_topic;
    struct aws_byte_cursor response_topic;

    jstring java_correlation_token_json_path;
    struct aws_byte_cursor correlation_token_json_path;
};

struct aws_jni_subscription {
    jstring java_topic_filter;
    struct aws_byte_cursor topic_filter;
};

struct aws_request_response_operation_jni_owned_parameters {
    struct aws_array_list response_paths;
    struct aws_array_list subscriptions;

    jstring java_publish_topic;
    struct aws_byte_cursor publish_topic;

    jbyteArray java_payload;
    struct aws_byte_cursor payload;

    jstring java_correlation_token;
    struct aws_byte_cursor correlation_token;
};

static int s_aws_request_response_operation_parameters_init(
    struct aws_request_response_operation_parameters *params,
    struct aws_allocator *allocator) {
    AWS_ZERO_STRUCT(*params);

    aws_array_list_init_dynamic(&params->response_paths, allocator, 2, sizeof(struct aws_jni_response_path));
    aws_array_list_init_dynamic(&params->subscriptions, allocator, 2, sizeof(struct aws_jni_subscription));
}

static void s_aws_request_response_operation_parameters_clean_up(
    struct aws_request_response_operation_parameters *params,
    JNIEnv *env) {
    if (!params) {
        return NULL;
    }

    for (size_t i = 0; i < aws_array_list_length(&params->response_paths); ++i) {
        struct aws_jni_response_path response_path;
        AWS_ZERO_STRUCT(aws_jni_response_path);

        aws_array_list_get_at(&params->response_paths, &response_path, i);

        aws_jni_byte_cursor_from_jstring_release(env, response_path.java_response_topic, response_path.response_topic);
        aws_jni_byte_cursor_from_jstring_release(
            env, response_path.java_correlation_token_json_path, response_path.correlation_token_json_path);
    }
    aws_array_list_clean_up(&params->response_paths);

    for (size_t i = 0; i < aws_array_list_length(&params->subscriptions); ++i) {
        struct aws_jni_subscription subscription;
        AWS_ZERO_STRUCT(subscription);

        aws_array_list_get_at(&params->subscriptions, &subscription, i);
        aws_jni_byte_cursor_from_jstring_release(env, subscription.java_topic_filter, subscription.topic_filter);
    }
    aws_array_list_clean_up(&params->subscriptions);

    aws_jni_byte_cursor_from_jstring_release(env, params->java_publish_topic, &params->publish_topic);
    aws_jni_byte_cursor_from_jbyteArray_release(env, params->java_payload, &params->payload);
    aws_jni_byte_cursor_from_jstring_release(env, params->java_correlation_token, &params->correlation_token);
}

static int s_aws_request_response_operation_jni_owned_parameters_init_from_jobject(
    struct aws_request_response_operation_jni_owned_parameters *params,
    struct aws_allocator *allocator,
    jobject java_request_response_operation,
    JNIEnv *env) {
    s_aws_request_response_operation_parameters_init(params, allocator);

    if (!java_request_response_operations) {
        aws_jni_throw_runtime_exception(
            env, "mqttRequestResponseClientSubmitRequest - request response options are null");
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }

    // responsePaths
    ? ? ;

    // subscriptions
    jobject java_subscriptions = (jobject)(*env)->GetObjectField(
        env, java_request_response_operation, request_response_operation_properties.subscriptions_field_id);
    jint subscription_count =
        (*env)->CallIntMethod(env, java_subscriptions, boxed_array_list_properties.size_method_id);
    if (subscription_count <= 0) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - subscriptions is empty");
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }

    for (size_t i = 0; i < (size_t)subscription_count; ++i) {
    }

    // publishTopic
    params->java_publish_topic = (jstring)(*env)->GetObjectField(
        env, java_request_response_operation, request_response_operation_properties.publish_topic_field_id);
    if (!params->java_publish_topic) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - publish topic is null");
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }
    params->publish_topic = aws_jni_byte_cursor_from_jstring_acquire(env, params->java_publish_topic);

    // payload
    params->java_payload = (jbyteArray)(*env)->GetObjectField(
        env, java_request_response_operation, request_response_operation_properties.payload_field_id);
    if (!params->java_payload) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - payload is null");
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }
    params->payload = aws_jni_byte_cursor_from_jbyteArray_acquire(env, params->java_payload);

    // correlationToken (optional)
    params->java_correlation_token = (jstring)(*env)->GetObjectField(
        env, java_request_response_operation, request_response_operation_properties.correlation_token_field_id);
    if (params->java_correlation_token) {
        params->correlation_token = aws_jni_byte_cursor_from_jstring_acquire(env, params->java_correlation_token);
    }

    return AWS_OP_SUCCESS;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_iot_MqttRequestResponseClient_mqttRequestResponseClientSubmitRequest(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_mqtt_request_response_client_handle,
        jobject java_request,
        jobject java_result_future) {

    (void)jni_class;

    struct aws_request_response_operation_binding *binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_request_response_operation_binding));

    (void)env;
    (void)jni_mqtt_request_response_client_handle;
    (void)java_request;
    (void)java_result_future;

error:
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

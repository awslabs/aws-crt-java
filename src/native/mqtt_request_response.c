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
        AWS_LOGF_ERROR(AWS_LS_JAVA_CRT_GENERAL, "JNI env no longer resolvable; JVM likely shutting down");
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

    JavaVM *jvm;

    jobject operation_future;
};

static void s_aws_request_response_operation_binding_destroy(struct aws_request_response_operation_binding *binding) {
    if (!binding) {
        return;
    }

    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);

    if (env && binding->operation_future) {
        (*env)->DeleteGlobalRef(env, binding->operation_future);
    }

    aws_jni_release_thread_env(binding->jvm, env);

    aws_mem_release(binding->allocator, binding);
}

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

static void s_aws_request_response_operation_parameters_init(
    struct aws_request_response_operation_jni_owned_parameters *params,
    struct aws_allocator *allocator) {
    AWS_ZERO_STRUCT(*params);

    aws_array_list_init_dynamic(&params->response_paths, allocator, 2, sizeof(struct aws_jni_response_path));
    aws_array_list_init_dynamic(&params->subscriptions, allocator, 2, sizeof(struct aws_jni_subscription));
}

static void s_aws_request_response_operation_jni_owned_parameters_clean_up(
    struct aws_request_response_operation_jni_owned_parameters *params,
    JNIEnv *env) {
    if (!params) {
        return;
    }

    for (size_t i = 0; i < aws_array_list_length(&params->response_paths); ++i) {
        struct aws_jni_response_path response_path;
        AWS_ZERO_STRUCT(response_path);

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

    aws_jni_byte_cursor_from_jstring_release(env, params->java_publish_topic, params->publish_topic);
    aws_jni_byte_cursor_from_jbyteArray_release(env, params->java_payload, params->payload);
    aws_jni_byte_cursor_from_jstring_release(env, params->java_correlation_token, params->correlation_token);
}

static int s_aws_request_response_operation_jni_owned_parameters_init_from_jobject(
    struct aws_request_response_operation_jni_owned_parameters *params,
    struct aws_allocator *allocator,
    jobject java_request_response_operation,
    JNIEnv *env) {
    s_aws_request_response_operation_parameters_init(params, allocator);

    if (!java_request_response_operation) {
        aws_jni_throw_runtime_exception(
            env, "mqttRequestResponseClientSubmitRequest - request response options are null");
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }

    // responsePaths
    jobject java_response_paths = (jobject)(*env)->GetObjectField(
        env, java_request_response_operation, request_response_operation_properties.response_paths_field_id);
    jint response_path_count =
        (*env)->CallIntMethod(env, java_response_paths, boxed_array_list_properties.size_method_id);
    if (response_path_count <= 0) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - response paths is empty");
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }

    for (size_t i = 0; i < (size_t)response_path_count; ++i) {
        jobject java_response_path =
            (*env)->CallObjectMethod(env, java_response_paths, boxed_array_list_properties.get_method_id, (jint)i);
        if (!java_response_path) {
            aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - null response path");
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }

        jstring java_response_topic =
            (jstring)(*env)->GetObjectField(env, java_response_path, response_path_properties.response_topic_field_id);
        if (!java_response_topic) {
            aws_jni_throw_runtime_exception(
                env, "mqttRequestResponseClientSubmitRequest - null response path response topic");
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }

        struct aws_jni_response_path response_path = {
            .java_response_topic = java_response_topic,
            .response_topic = aws_jni_byte_cursor_from_jstring_acquire(env, java_response_topic),
        };

        jstring java_correlation_token_json_path = (jstring)(*env)->GetObjectField(
            env, java_response_path, response_path_properties.correlation_token_json_path_field_id);

        if (java_correlation_token_json_path) {
            response_path.java_correlation_token_json_path = java_correlation_token_json_path;
            response_path.correlation_token_json_path =
                aws_jni_byte_cursor_from_jstring_acquire(env, java_correlation_token_json_path);
        }

        aws_array_list_push_back(&params->response_paths, &response_path);
    }

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
        jstring java_subscription_topic_filter =
            (*env)->CallObjectMethod(env, java_subscriptions, boxed_array_list_properties.get_method_id, (jint)i);
        if (!java_subscription_topic_filter) {
            aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - null subscription");
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }

        struct aws_jni_subscription subscription = {
            .java_topic_filter = java_subscription_topic_filter,
            .topic_filter = aws_jni_byte_cursor_from_jstring_acquire(env, java_subscription_topic_filter),
        };

        aws_array_list_push_back(&params->subscriptions, &subscription);
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

static void s_on_request_response_operation_completion(
    const struct aws_mqtt_rr_incoming_publish_event *publish_event,
    int error_code,
    void *user_data) {

    jobject java_result = NULL;
    jstring java_topic = NULL;
    jbyteArray java_payload = NULL;
    struct aws_request_response_operation_binding *binding = user_data;
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (!env) {
        goto done;
    }

    if (error_code == AWS_ERROR_SUCCESS) {
        java_result = (*env)->NewObject(
            env,
            mqtt_request_response_properties.mqtt_request_response_class,
            mqtt_request_response_properties.constructor_method_id);
        if (java_result != NULL) {
            java_topic = aws_jni_string_from_cursor(env, &publish_event->topic);
            (*env)->SetObjectField(env, java_result, mqtt_request_response_properties.topic_field_id, java_topic);

            java_payload = aws_jni_byte_array_from_cursor(env, &publish_event->payload);
            (*env)->SetObjectField(env, java_result, mqtt_request_response_properties.payload_field_id, java_payload);
        }
    }

    if (java_result != NULL) {
        (*env)->CallBooleanMethod(
            env, binding->operation_future, completable_future_properties.complete_method_id, java_result);
    } else {
        int final_error_code = (error_code == AWS_ERROR_SUCCESS) ? AWS_ERROR_UNKNOWN : error_code;
        jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, final_error_code);
        (*env)->CallBooleanMethod(
            env,
            binding->operation_future,
            completable_future_properties.complete_exceptionally_method_id,
            crt_exception);

        (*env)->DeleteLocalRef(env, crt_exception);
    }

    aws_jni_check_and_clear_exception(env);

done:

    if (java_result != NULL) {
        (*env)->DeleteLocalRef(env, java_result);
    }

    if (java_topic != NULL) {
        (*env)->DeleteLocalRef(env, java_topic);
    }

    if (java_payload != NULL) {
        (*env)->DeleteLocalRef(env, java_payload);
    }

    aws_jni_release_thread_env(binding->jvm, env);

    s_aws_request_response_operation_binding_destroy(binding);
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_iot_MqttRequestResponseClient_mqttRequestResponseClientSubmitRequest(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_mqtt_request_response_client_handle,
        jobject java_request,
        jobject java_result_future) {

    (void)jni_class;

    struct aws_crt_mqtt_request_response_client_binding *rr_client_binding =
        (struct aws_crt_mqtt_request_response_client_binding *)jni_mqtt_request_response_client_handle;
    struct aws_mqtt_request_response_client *rr_client = rr_client_binding->client;
    if (!rr_client || !java_request || !java_result_future) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest: null parameter");
        return;
    }

    JavaVM *jvm = NULL;
    jint jvmresult = (*env)->GetJavaVM(env, &jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest: failed to get JVM");
        return;
    }

    struct aws_request_response_operation_binding *binding = NULL;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_request_response_operation_jni_owned_parameters request_params;
    if (s_aws_request_response_operation_jni_owned_parameters_init_from_jobject(
            &request_params, allocator, java_request, env)) {
        s_aws_request_response_operation_jni_owned_parameters_clean_up(&request_params, env);
        return;
    }

    binding = aws_mem_calloc(allocator, 1, sizeof(struct aws_request_response_operation_binding));
    binding->allocator = allocator;
    binding->operation_future = (*env)->NewGlobalRef(env, java_result_future);
    binding->jvm = jvm;

    struct aws_mqtt_request_operation_options request_options;
    AWS_ZERO_STRUCT(request_options);

    size_t subscription_count = aws_array_list_length(&request_params.subscriptions);
    AWS_VARIABLE_LENGTH_ARRAY(struct aws_byte_cursor, subscriptions, subscription_count);
    for (size_t i = 0; i < subscription_count; ++i) {
        struct aws_jni_subscription subscription;
        AWS_ZERO_STRUCT(subscription);
        aws_array_list_get_at(&request_params.subscriptions, &subscription, i);

        subscriptions[i] = subscription.topic_filter;
    }

    size_t response_path_count = aws_array_list_length(&request_params.response_paths);
    AWS_VARIABLE_LENGTH_ARRAY(struct aws_mqtt_request_operation_response_path, response_paths, response_path_count);
    for (size_t i = 0; i < response_path_count; ++i) {
        struct aws_jni_response_path response_path;
        AWS_ZERO_STRUCT(response_path);
        aws_array_list_get_at(&request_params.response_paths, &response_path, i);

        response_paths[i].topic = response_path.response_topic;
        response_paths[i].correlation_token_json_path = response_path.correlation_token_json_path;
    }

    request_options.subscription_topic_filters = subscriptions;
    request_options.subscription_topic_filter_count = subscription_count;
    request_options.response_paths = response_paths;
    request_options.response_path_count = response_path_count;
    request_options.publish_topic = request_params.publish_topic;
    request_options.serialized_request = request_params.payload;
    request_options.correlation_token = request_params.correlation_token;
    request_options.completion_callback = s_on_request_response_operation_completion;
    request_options.user_data = binding;

    if (aws_mqtt_request_response_client_submit_request(rr_client, &request_options)) {
        aws_jni_throw_runtime_exception(env, "mqttRequestResponseClientSubmitRequest - failed to submit request");
        goto error;
    }

    goto done;

error:

    s_aws_request_response_operation_binding_destroy(binding);

done:

    s_aws_request_response_operation_jni_owned_parameters_clean_up(&request_params, env);
}

struct aws_streaming_operation_binding {
    struct aws_allocator *allocator;

    JavaVM *jvm;

    struct aws_mqtt_rr_client_operation *stream;

    jobject java_incoming_publish_event_callback;
    jobject java_subscription_status_event_callback;
};

static void s_aws_streaming_operation_binding_destroy(struct aws_streaming_operation_binding *binding) {
    if (!binding) {
        return;
    }

    // tearing down a stream is asynchronous and should have been done earlier if needed
    AWS_FATAL_ASSERT(binding->stream == NULL);

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = binding->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_JAVA_CRT_GENERAL, "JNI env no longer resolvable; JVM likely shutting down");
        goto done;
    }

    if (binding->java_incoming_publish_event_callback) {
        (*env)->DeleteGlobalRef(env, binding->java_incoming_publish_event_callback);
    }

    if (binding->java_subscription_status_event_callback) {
        (*env)->DeleteGlobalRef(env, binding->java_subscription_status_event_callback);
    }

    aws_jni_release_thread_env(binding->jvm, env);

done:

    aws_mem_release(binding->allocator, binding);
}

static void s_aws_mqtt_streaming_operation_subscription_status_callback(
    enum aws_rr_streaming_subscription_event_type status,
    int error_code,
    void *user_data) {

    struct aws_streaming_operation_binding *binding = user_data;
    if (!binding->java_subscription_status_event_callback) {
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = binding->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_JAVA_CRT_GENERAL, "JNI env no longer resolvable; JVM likely shutting down");
        return;
    }

    jobject java_subscription_status_event_type = NULL;
    jobject java_subscription_status_event = NULL;

    java_subscription_status_event_type = (*env)->CallStaticObjectMethod(
        env,
        subscription_status_event_type_properties.subscription_status_event_type_class,
        subscription_status_event_type_properties.get_enum_value_from_integer_method_id,
        (jint)status);
    if (java_subscription_status_event_type == NULL) {
        AWS_LOGF_ERROR(
            AWS_LS_JAVA_CRT_GENERAL,
            "s_aws_mqtt_streaming_operation_subscription_status_callback - could not create subscription status event "
            "type");
        goto done;
    }

    java_subscription_status_event = (*env)->NewObject(
        env,
        subscription_status_event_properties.subscription_status_event_class,
        subscription_status_event_properties.constructor_method_id,
        java_subscription_status_event_type,
        (jint)error_code);

    if (java_subscription_status_event == NULL) {
        AWS_LOGF_ERROR(
            AWS_LS_JAVA_CRT_GENERAL,
            "s_aws_mqtt_streaming_operation_subscription_status_callback - could not create subscription status event");
        goto done;
    }

    (*env)->CallVoidMethod(
        env,
        binding->java_subscription_status_event_callback,
        consumer_properties.accept_method_id,
        java_subscription_status_event);

    aws_jni_check_and_clear_exception(env);

done:

    if (java_subscription_status_event_type != NULL) {
        (*env)->DeleteLocalRef(env, java_subscription_status_event_type);
    }

    if (java_subscription_status_event != NULL) {
        (*env)->DeleteLocalRef(env, java_subscription_status_event);
    }

    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

static void s_aws_mqtt_streaming_operation_incoming_publish_callback(
    const struct aws_mqtt_rr_incoming_publish_event *publish_event,
    void *user_data) {

    struct aws_streaming_operation_binding *binding = user_data;
    if (!binding->java_incoming_publish_event_callback) {
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = binding->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_JAVA_CRT_GENERAL, "JNI env no longer resolvable; JVM likely shutting down");
        return;
    }

    jobject java_payload = NULL;
    jstring java_topic = NULL;
    jobject java_incoming_publish_event = NULL;

    java_payload = aws_jni_byte_array_from_cursor(env, &publish_event->payload);
    java_topic = aws_jni_string_from_cursor(env, &publish_event->topic);

    java_incoming_publish_event = (*env)->NewObject(
        env,
        incoming_publish_event_properties.incoming_publish_event_class,
        incoming_publish_event_properties.constructor_method_id,
        java_payload,
        java_topic);

    if (java_incoming_publish_event == NULL) {
        AWS_LOGF_ERROR(
            AWS_LS_JAVA_CRT_GENERAL,
            "s_aws_mqtt_streaming_operation_incoming_publish_callback - could not create incoming publish event");
        goto done;
    }

    jstring java_content_type = NULL;
    if (publish_event->content_type != NULL) {
        java_content_type = aws_jni_string_from_cursor(env, publish_event->content_type);
        (*env)->CallVoidMethod(
            env, java_incoming_publish_event, incoming_publish_event_properties.set_content_type_id, java_content_type);
    }

    (*env)->CallVoidMethod(
        env,
        binding->java_incoming_publish_event_callback,
        consumer_properties.accept_method_id,
        java_incoming_publish_event);

    aws_jni_check_and_clear_exception(env);

done:

    if (java_payload != NULL) {
        (*env)->DeleteLocalRef(env, java_payload);
    }

    if (java_topic != NULL) {
        (*env)->DeleteLocalRef(env, java_topic);
    }

    if (java_incoming_publish_event != NULL) {
        (*env)->DeleteLocalRef(env, java_incoming_publish_event);
    }

    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

static void s_aws_mqtt_streaming_operation_terminated_callback(void *user_data) {
    struct aws_streaming_operation_binding *binding = user_data;

    s_aws_streaming_operation_binding_destroy(binding);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_iot_StreamingOperation_streamingOperationNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_mqtt_request_response_client_handle,
    jobject java_options) {

    (void)jni_class;

    aws_cache_jni_ids(env);

    struct aws_crt_mqtt_request_response_client_binding *rr_client_binding =
        (struct aws_crt_mqtt_request_response_client_binding *)jni_mqtt_request_response_client_handle;
    if (rr_client_binding == NULL) {
        aws_jni_throw_runtime_exception(env, "streamingOperationNew: null request-response client binding");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_streaming_operation_binding *binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_streaming_operation_binding));
    binding->allocator = allocator;

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "streamingOperationNew: failed to get JVM");
        goto error;
    }

    struct aws_mqtt_streaming_operation_options stream_options;
    AWS_ZERO_STRUCT(stream_options);
    stream_options.subscription_status_callback = s_aws_mqtt_streaming_operation_subscription_status_callback;
    stream_options.incoming_publish_callback = s_aws_mqtt_streaming_operation_incoming_publish_callback;
    stream_options.terminated_callback = s_aws_mqtt_streaming_operation_terminated_callback;
    stream_options.user_data = binding;

    jstring java_topic =
        (jstring)(*env)->GetObjectField(env, java_options, streaming_operation_options_properties.topic_field_id);
    if (!java_topic) {
        aws_jni_throw_runtime_exception(env, "streamingOperationNew - topic is null");
        goto error;
    }
    stream_options.topic_filter = aws_jni_byte_cursor_from_jstring_acquire(env, java_topic);

    jobject java_incoming_publish_event_callback = (*env)->GetObjectField(
        env, java_options, streaming_operation_options_properties.incoming_publish_event_callback_field_id);
    if (java_incoming_publish_event_callback) {
        binding->java_incoming_publish_event_callback = (*env)->NewGlobalRef(env, java_incoming_publish_event_callback);
    }

    jobject java_subscription_status_event_callback = (*env)->GetObjectField(
        env, java_options, streaming_operation_options_properties.subscription_status_event_callback_field_id);
    if (java_subscription_status_event_callback) {
        binding->java_subscription_status_event_callback =
            (*env)->NewGlobalRef(env, java_subscription_status_event_callback);
    }

    binding->stream =
        aws_mqtt_request_response_client_create_streaming_operation(rr_client_binding->client, &stream_options);

    aws_jni_byte_cursor_from_jstring_release(env, java_topic, stream_options.topic_filter);

    if (!binding->stream) {
        aws_jni_throw_runtime_exception(env, "streamingOperationNew - failed to create native stream");
        goto error;
    }

    return (jlong)binding;

error:

    AWS_FATAL_ASSERT(binding && !binding->stream);

    s_aws_streaming_operation_binding_destroy(binding);

    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_iot_StreamingOperation_streamingOperationOpen(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_streaming_operation_handle) {

    (void)jni_class;

    struct aws_streaming_operation_binding *binding =
        (struct aws_streaming_operation_binding *)jni_streaming_operation_handle;
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "streamingOperationOpen - stream already closed");
        return;
    }

    struct aws_mqtt_rr_client_operation *stream = binding->stream;

    if (aws_mqtt_rr_client_operation_activate(stream)) {
        aws_jni_throw_runtime_exception(env, "streamingOperationOpen - failed to open stream");
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_iot_StreamingOperation_streamingOperationDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_streaming_operation_handle) {

    (void)env;
    (void)jni_class;

    struct aws_streaming_operation_binding *binding =
        (struct aws_streaming_operation_binding *)jni_streaming_operation_handle;
    if (binding == NULL) {
        return;
    }

    struct aws_mqtt_rr_client_operation *stream = binding->stream;

    binding->stream = NULL;
    aws_mqtt_rr_client_operation_release(stream);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <aws/mqtt/v5/mqtt5_listener.h>

#include <aws/http/proxy.h>
#include <aws/io/event_loop.h>
#include <aws/io/socket.h>
#include <aws/io/tls_channel_handler.h>

#include <crt.h>
#include <http_request_utils.h>
#include <java_class_ids.h>
#include <jni.h>
#include <mqtt5_client_jni.h>
#include <mqtt5_packets.h>
#include <mqtt5_utils.h>

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
 * CLIENT ONLY STRUCTS
 ******************************************************************************/

struct aws_mqtt5_listener_java_jni {
    struct aws_mqtt5_listener *listener;
    jobject jni_listener;
    // The listener should keep a reference to mqtt5 client
    jobject jni_mqtt5_client;
    JavaVM *jvm;

    jobject jni_listener_publish_events;
    jobject jni_lifecycle_events;
};

/*******************************************************************************
 * HELPER FUNCTION (LOGGING)
 ******************************************************************************/

static void s_aws_mqtt5_listener_log_and_throw_exception(JNIEnv *env, const char *message, int error_code) {
    AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "%s - error code: %i", message, error_code);
    aws_jni_throw_runtime_exception(env, "%s - error code: %i", message, error_code);
}

/*******************************************************************************
 * HELPER FUNCTIONS
 ******************************************************************************/

static void aws_mqtt5_listener_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_listener_java_jni *java_listener) {
    AWS_PRECONDITION(java_listener);
    if (!java_listener) {
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_MQTT5_GENERAL, "java_listener=%p: Destroying MQTT5 listener", (void *)java_listener);

    if (java_listener->jni_mqtt5_client) {
        (*env)->DeleteGlobalRef(env, java_listener->jni_mqtt5_client);
    }
    if (java_listener->jni_listener_publish_events) {
        (*env)->DeleteGlobalRef(env, java_listener->jni_listener_publish_events);
    }
    if (java_listener->jni_lifecycle_events) {
        (*env)->DeleteGlobalRef(env, java_listener->jni_lifecycle_events);
    }
    if (java_listener->jni_listener) {
        (*env)->DeleteGlobalRef(env, java_listener->jni_listener);
    }

    /* Frees allocated memory */
    aws_mem_release(allocator, java_listener);
}

static void s_aws_count_allocation(const void *pointer, size_t *counter) {
    if (pointer != NULL) {
        *counter += 1;
    }
}

/*******************************************************************************
 * MQTT5 CALLBACK FUNCTIONS
 ******************************************************************************/

static void s_aws_mqtt5_listener_java_lifecycle_event(const struct aws_mqtt5_client_lifecycle_event *event) {

    struct aws_mqtt5_listener_java_jni *java_listener = (struct aws_mqtt5_listener_java_jni *)event->user_data;
    if (!java_listener) {
        AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "LifecycleEvent: invalid client");
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = java_listener->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "LifecycleEvent: could not get env");
        return;
    }

    /* Calculate the number of references needed (1 is always needed for the return struct) */
    size_t references_needed = 1;
    if (event->connack_data != NULL) {
        /* A ConnAck packet will need 2 references at minimum */
        references_needed += 2;
        /* Optionals */
        s_aws_count_allocation(event->connack_data->assigned_client_identifier, &references_needed);
        s_aws_count_allocation(event->connack_data->authentication_data, &references_needed);
        s_aws_count_allocation(event->connack_data->authentication_method, &references_needed);
        s_aws_count_allocation(event->connack_data->maximum_packet_size, &references_needed);
        s_aws_count_allocation(event->connack_data->maximum_qos, &references_needed);
        s_aws_count_allocation(event->connack_data->reason_string, &references_needed);
        s_aws_count_allocation(event->connack_data->receive_maximum, &references_needed);
        s_aws_count_allocation(event->connack_data->response_information, &references_needed);
        s_aws_count_allocation(event->connack_data->retain_available, &references_needed);
        s_aws_count_allocation(event->connack_data->server_keep_alive, &references_needed);
        s_aws_count_allocation(event->connack_data->server_reference, &references_needed);
        s_aws_count_allocation(event->connack_data->session_expiry_interval, &references_needed);
        s_aws_count_allocation(event->connack_data->shared_subscriptions_available, &references_needed);
        s_aws_count_allocation(event->connack_data->subscription_identifiers_available, &references_needed);
        s_aws_count_allocation(event->connack_data->topic_alias_maximum, &references_needed);
        s_aws_count_allocation(event->connack_data->wildcard_subscriptions_available, &references_needed);
        /* Add user properties */
        references_needed += event->connack_data->user_property_count * 2;
        references_needed += 2; /* Add 2 for arrays to hold user properties */
    }
    if (event->disconnect_data != NULL) {
        /* A Disconnect packet will need 1 reference at a minimum */
        references_needed += 1;
        /* Optionals */
        s_aws_count_allocation(event->disconnect_data->reason_string, &references_needed);
        s_aws_count_allocation(event->disconnect_data->server_reference, &references_needed);
        s_aws_count_allocation(event->disconnect_data->session_expiry_interval_seconds, &references_needed);
        /* Add user properties */
        references_needed += event->disconnect_data->user_property_count * 2;
        references_needed += 2; /* Add 1 for array to hold user properties */
    }
    if (event->settings != NULL) {
        /* Negotiated settings only need 2 references, one for the ClientID and another for the object */
        references_needed += 2;
    }

    /* Make a local frame so we can clean memory */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)references_needed);
    if (local_frame_result != 0) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "LifecycleEvent: could not push local JNI frame with 14 allocation minimum!", AWS_ERROR_INVALID_STATE);
        aws_jni_release_thread_env(jvm, env);
        return;
    }

    jobject connack_data = NULL;
    if (event->connack_data != NULL) {
        connack_data = s_aws_mqtt5_client_create_jni_connack_packet_from_native(env, event->connack_data);
        if (connack_data == NULL) {
            AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "LifecycleEvent: creating ConnAck packet failed!");
            goto clean_up;
        }
    }

    jobject disconnect_data = NULL;
    if (event->disconnect_data != NULL) {
        disconnect_data = s_aws_mqtt5_client_create_jni_disconnect_packet_from_native(env, event->disconnect_data);
        if (disconnect_data == NULL) {
            AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "LifecycleEvent: creating Disconnect packet failed!");
            goto clean_up;
        }
    }

    jobject negotiated_settings_data = NULL;
    if (event->settings != NULL) {
        negotiated_settings_data = s_aws_mqtt5_client_create_jni_negotiated_settings_from_native(env, event->settings);
    }

    jobject jni_lifecycle_events = java_listener->jni_lifecycle_events;
    if (!jni_lifecycle_events) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "LifecycleEvent: no lifecycle events found!", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }

    jobject java_lifecycle_return_data;

    switch (event->event_type) {
        case AWS_MQTT5_CLET_ATTEMPTING_CONNECT:

            /* Make the OnAttemptingConnectReturn struct */
            java_lifecycle_return_data = (*env)->NewObject(
                env,
                mqtt5_on_attempting_connect_return_properties.return_class,
                mqtt5_on_attempting_connect_return_properties.return_constructor_id);
            aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_attempting_connect_id,
                java_listener->jni_mqtt5_client,
                java_lifecycle_return_data);
            break;
        case AWS_MQTT5_CLET_CONNECTION_SUCCESS:

            /* Make the OnConnectionSuccessReturn struct */
            java_lifecycle_return_data = (*env)->NewObject(
                env,
                mqtt5_on_connection_success_return_properties.return_class,
                mqtt5_on_connection_success_return_properties.return_constructor_id,
                connack_data,
                negotiated_settings_data);
            aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

            /* Set OnConnected BEFORE calling the callback so it is accurate in the callback itself. */
            (*env)->CallBooleanMethod(
                env, java_listener->jni_mqtt5_client, mqtt5_client_properties.client_set_is_connected, true);

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_connection_success_id,
                java_listener->jni_mqtt5_client,
                java_lifecycle_return_data);
            break;
        case AWS_MQTT5_CLET_CONNECTION_FAILURE: {
            jint error_code = (jint)event->error_code;

            /* Make the OnConnectionFailureReturn struct */
            java_lifecycle_return_data = (*env)->NewObject(
                env,
                mqtt5_on_connection_failure_return_properties.return_class,
                mqtt5_on_connection_failure_return_properties.return_constructor_id,
                error_code,
                connack_data);
            aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_connection_failure_id,
                java_listener->jni_mqtt5_client,
                java_lifecycle_return_data);
            break;
        }
        case AWS_MQTT5_CLET_DISCONNECTION: {
            jint error_code = (jint)event->error_code;

            /* Make the OnDisconnectionReturn struct */
            java_lifecycle_return_data = (*env)->NewObject(
                env,
                mqtt5_on_disconnection_return_properties.return_class,
                mqtt5_on_disconnection_return_properties.return_constructor_id,
                error_code,
                disconnect_data);
            aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

            /* Set OnConnected BEFORE calling the callback so it is accurate in the callback itself. */
            (*env)->CallBooleanMethod(
                env, java_listener->jni_mqtt5_client, mqtt5_client_properties.client_set_is_connected, false);

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_disconnection_id,
                java_listener->jni_mqtt5_client,
                java_lifecycle_return_data);
            break;
        }
        case AWS_MQTT5_CLET_STOPPED:

            /* Make the OnStopped struct */
            java_lifecycle_return_data = (*env)->NewObject(
                env,
                mqtt5_on_stopped_return_properties.return_class,
                mqtt5_on_stopped_return_properties.return_constructor_id);
            aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_stopped_id,
                java_listener->jni_mqtt5_client,
                java_lifecycle_return_data);
            break;
        default:
            AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "LifecycleEvent: unsupported event type: %i", event->event_type);
    }

    goto clean_up;

clean_up:

    aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    (*env)->PopLocalFrame(env, NULL);
    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

static bool s_aws_mqtt5_listener_java_publish_received(
    const struct aws_mqtt5_packet_publish_view *publish,
    void *user_data) {

    bool callback_result = false;
    struct aws_mqtt5_listener_java_jni *java_listener = (struct aws_mqtt5_listener_java_jni *)user_data;
    if (!java_listener) {
        AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "publishReceived function: invalid listener");
        return false;
    }

    if (!publish) {
        AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "publishReceived function: invalid publish packet");
        return false;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = java_listener->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT5_GENERAL, "publishReceived function: could not get env");
        return false;
    }

    /* Calculate the number of references needed */
    size_t references_needed = 0;
    {
        /* One reference is needed for the PublishReturn */
        references_needed += 1;

        /* A Publish packet will need 5 references at minimum */
        references_needed += 5;
        /* Optionals */
        s_aws_count_allocation(publish->content_type, &references_needed);
        s_aws_count_allocation(publish->correlation_data, &references_needed);
        s_aws_count_allocation(publish->message_expiry_interval_seconds, &references_needed);
        s_aws_count_allocation(publish->response_topic, &references_needed);
        s_aws_count_allocation(publish->topic_alias, &references_needed);
        s_aws_count_allocation(publish->payload_format, &references_needed);
        /* Add user properties and subscription identifiers */
        references_needed += publish->user_property_count * 2;
        references_needed += 1; /* Add 1 for array to hold user properties */
        if (publish->subscription_identifier_count > 0) {
            references_needed += publish->subscription_identifier_count;
            references_needed += 1; /* Add 1 for array */
        }
    }

    /**
     * Push a new local frame so any local references we make are tied to it. Then we can pop it to free memory.
     */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)references_needed);
    if (local_frame_result != 0) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env,
            "publishReceived function: could not push local JNI frame with 12 allocation minimum!",
            AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }

    /* The return result */
    jobject publish_packet_return_data;

    /* Make the PublishPacket */
    jobject publish_packet_data = s_aws_mqtt5_client_create_jni_publish_packet_from_native(env, publish);
    if (publish_packet_data == NULL) {
        goto clean_up;
    }

    /* Make the PublishReturn struct that will hold all of the data that is passed to Java */
    publish_packet_return_data = (*env)->NewObject(
        env,
        mqtt5_publish_return_properties.return_class,
        mqtt5_publish_return_properties.return_constructor_id,
        publish_packet_data);
    aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    if (java_listener->jni_listener_publish_events) {
        // jni_listener_publish_events returns a jboolean, cast it to bool
        callback_result = (bool)((*env)->CallBooleanMethod(
            env,
            java_listener->jni_listener_publish_events,
            mqtt5_publish_events_properties.publish_events_publish_received_id,
            java_listener->jni_mqtt5_client,
            publish_packet_return_data));
        aws_jni_check_and_clear_exception(env); /* To hide JNI warning */
    }
    goto clean_up;

clean_up:

    (*env)->PopLocalFrame(env, NULL);
    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
    return callback_result;
}

static void s_aws_mqtt5_listener_java_termination(void *complete_ctx) {
    struct aws_mqtt5_listener_java_jni *java_listener = (struct aws_mqtt5_listener_java_jni *)complete_ctx;
    if (!java_listener) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT5_GENERAL, "MQTT5 listener termination function in JNI called, but with invalid java_listener");
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = java_listener->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(
            AWS_LS_MQTT5_GENERAL, "MQTT5 listener termination function in JNI called, but could not get env");
        return;
    }

    (*env)->CallVoidMethod(env, java_listener->jni_listener, crt_resource_properties.release_references);
    java_listener->listener = NULL;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mqtt5_listener_java_destroy(env, allocator, java_listener);

    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

/*******************************************************************************
 * JNI FUNCTIONS
 ******************************************************************************/

/* Create and Destroy
**************************************/

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Listener_mqtt5ListenerNew(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_listener_options,
    jobject jni_mqtt5_client,
    jobject jni_listener) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_mqtt5_callback_set callback_set;
    AWS_ZERO_STRUCT(callback_set);
    struct aws_mqtt5_listener_config listener_options;
    AWS_ZERO_STRUCT(listener_options);

    /**
     * Push a new local frame so any local allocations we make are tied to it. Then we can pop it to free memory.
     * * Reference: https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html#PushLocalFrame
     * In Java JNI allocations here, we have 21 allocations so we need at least that many.
     * According to this Stackoverflow, it should expand if we use more: https://stackoverflow.com/a/70946713
     * (NOTE: We cannot get the exact here because we are pulling from Java objects and we have no way to know how many
     * that will need)
     */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)4);
    if (local_frame_result != 0) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env,
            "MQTT5 listener new: could not push local JNI frame with 4 allocation minimum",
            AWS_ERROR_INVALID_STATE);
        return (jlong)NULL;
    }

    struct aws_mqtt5_listener_java_jni *java_listener =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_listener_java_jni));
    AWS_LOGF_DEBUG(AWS_LS_MQTT5_GENERAL, "java_listener=%p: Initalizing MQTT5 listener", (void *)java_listener);
    if (java_listener == NULL) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener new: could not initialize new listener", AWS_ERROR_INVALID_STATE);
        return (jlong)NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &java_listener->jvm);
    if (jvmresult != 0) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener new: Unable to get JVM", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }

    java_listener->jni_mqtt5_client = (*env)->NewGlobalRef(env, jni_mqtt5_client);
    java_listener->jni_listener = (*env)->NewGlobalRef(env, jni_listener);

    callback_set.lifecycle_event_handler = &s_aws_mqtt5_listener_java_lifecycle_event;
    callback_set.lifecycle_event_handler_user_data = (void *)java_listener;

    callback_set.listener_publish_received_handler = &s_aws_mqtt5_listener_java_publish_received;
    callback_set.listener_publish_received_handler_user_data = (void *)java_listener;

    jobject jni_listener_publish_events =
        (*env)->GetObjectField(env, jni_listener_options, mqtt5_listener_options_properties.listener_publish_events_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener new: error getting publish events", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_listener_publish_events != NULL) {
        java_listener->jni_listener_publish_events = (*env)->NewGlobalRef(env, jni_listener_publish_events);
    }

    jobject jni_lifecycle_events =
        (*env)->GetObjectField(env, jni_listener_options, mqtt5_listener_options_properties.lifecycle_events_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener new: error getting lifecycle events", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_lifecycle_events != NULL) {
        java_listener->jni_lifecycle_events = (*env)->NewGlobalRef(env, jni_lifecycle_events);
    }

    listener_options.termination_callback = &s_aws_mqtt5_listener_java_termination;
    listener_options.termination_callback_user_data = (void *)java_listener;

    listener_options.listener_callbacks = callback_set;

    // Get aws_mqtt5_client_java_jni reference
    jlong jni_mqtt5_client_pointer =
        (*env)->CallLongMethod(env, jni_mqtt5_client, crt_resource_properties.get_native_handle_method_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener new: could not get native handle for mqtt5 client", AWS_ERROR_INVALID_ARGUMENT);
        goto clean_up;
    }

    struct aws_mqtt5_client_java_jni *java_class_client = (struct aws_mqtt5_client_java_jni *)jni_mqtt5_client_pointer;
    if (!java_class_client) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener new: could not get native handle for mqtt5 client", AWS_ERROR_INVALID_ARGUMENT);
        goto clean_up;
    }
    listener_options.client = java_class_client->client;

    /* Make the MQTT5 listener */
    java_listener->listener = aws_mqtt5_listener_new(allocator, &listener_options);
    /* Did we successfully make a client? If not, then throw an exception */
    if (java_listener->listener == NULL) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env,
            "MQTT5 listener new: Was unable to create client due to option configuration! Enable error logging to see "
            "reason",
            AWS_ERROR_MQTT5_CLIENT_OPTIONS_VALIDATION);
        // TODO : ADD MQTT5 LISTENER ERRORS
        goto clean_up;
    }

    goto clean_up;

clean_up:
    (*env)->PopLocalFrame(env, NULL);

    if (java_listener->listener != NULL) {
        return (jlong)java_listener;
    }

    aws_mqtt5_listener_java_destroy(env, allocator, java_listener);
    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Listener_mqtt5ListenerDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_listener) {
    (void)jni_class;

    struct aws_mqtt5_listener_java_jni *java_listener = (struct aws_mqtt5_listener_java_jni *)jni_listener;
    if (!java_listener) {
        s_aws_mqtt5_listener_log_and_throw_exception(
            env, "MQTT5 listener destroy: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }

    // If the client is NOT null it can be shut down normally
    struct aws_allocator *allocator = aws_jni_get_allocator();
    if (java_listener->listener) {
        aws_mqtt5_listener_release(java_listener->listener);
    } else {
        aws_mqtt5_listener_java_destroy(env, allocator, java_listener);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

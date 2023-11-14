/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <crt.h>
#include <java_class_ids.h>
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

int s_set_jni_uint32_t_field_in_packet(
    JNIEnv *env,
    const uint32_t *native_integer,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {

    if (native_integer != NULL) {
        jobject jni_long = (*env)->NewObject(
            env, boxed_long_properties.long_class, boxed_long_properties.constructor, (jlong)*native_integer);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create uint32 field %s", field_name);
            return AWS_OP_ERR;
        }
        (*env)->SetObjectField(env, packet, field_id, jni_long);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set uint32 field %s", field_name);
            return AWS_OP_ERR;
        }
        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    }
    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

int s_set_jni_uint16_t_field_in_packet(
    JNIEnv *env,
    const uint16_t *native_integer,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {
    if (native_integer != NULL) {
        jobject jni_int = (*env)->NewObject(
            env,
            boxed_integer_properties.integer_class,
            boxed_integer_properties.integer_constructor_id,
            (jint)*native_integer);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create uint16 field %s", field_name);
            return AWS_OP_ERR;
        }
        (*env)->SetObjectField(env, packet, field_id, jni_int);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set uint16 field %s", field_name);
            return AWS_OP_ERR;
        }
        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    }
    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

int s_set_jni_bool_field_in_packet(
    JNIEnv *env,
    const bool *native_boolean,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {
    if (native_boolean != NULL) {
        jobject jni_boolean = (*env)->NewObject(
            env,
            boxed_boolean_properties.boolean_class,
            boxed_boolean_properties.boolean_constructor_id,
            (jboolean)*native_boolean);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create boolean field %s", field_name);
            return AWS_OP_ERR;
        }

        (*env)->SetObjectField(env, packet, field_id, jni_boolean);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set boolean field %s", field_name);
            return AWS_OP_ERR;
        }

        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    }
    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

int s_set_jni_string_field_in_packet(
    JNIEnv *env,
    const struct aws_byte_cursor *native_cursor,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {
    if (native_cursor != NULL) {
        jstring jni_string = aws_jni_string_from_cursor(env, native_cursor);
        (*env)->SetObjectField(env, packet, field_id, jni_string);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set string field %s", field_name);
            return AWS_OP_ERR;
        }
        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    }
    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

int s_set_jni_byte_array_field_in_packet(
    JNIEnv *env,
    const struct aws_byte_cursor *native_cursor,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {
    if (native_cursor != NULL) {
        jbyteArray jni_byte = aws_jni_byte_array_from_cursor(env, native_cursor);
        (*env)->SetObjectField(env, packet, field_id, jni_byte);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set string field %s", field_name);
            return AWS_OP_ERR;
        }
        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    }
    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

int s_set_user_properties_field(
    JNIEnv *env,
    const size_t user_property_count,
    const struct aws_mqtt5_user_property *packet_properties,
    jobject packet,
    jfieldID user_property_field_id) {
    /* No properties - nothing to do */
    if (packet_properties == NULL) {
        return AWS_OP_SUCCESS;
    }

    if (user_property_count > 0) {
        jobject jni_user_properties_list = (*env)->NewObject(
            env, boxed_array_list_properties.list_class, boxed_array_list_properties.list_constructor_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not make new user properties list");
            return AWS_OP_ERR;
        }

        (*env)->SetObjectField(env, packet, user_property_field_id, jni_user_properties_list);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set new user properties list");
            return AWS_OP_ERR;
        }

        for (size_t i = 0; i < user_property_count; ++i) {
            const struct aws_mqtt5_user_property *property = &packet_properties[i];
            jstring jni_new_property_name = aws_jni_string_from_cursor(env, &property->name);
            jstring jni_new_property_value = aws_jni_string_from_cursor(env, &property->value);

            jobject jni_new_property = (*env)->NewObject(
                env,
                mqtt5_user_property_properties.user_property_class,
                mqtt5_user_property_properties.property_constructor_id,
                jni_new_property_name,
                jni_new_property_value);
            if (aws_jni_check_and_clear_exception(env)) {
                AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not make new user property");
                return AWS_OP_ERR;
            }

            jboolean jni_add_result = (*env)->CallBooleanMethod(
                env, jni_user_properties_list, boxed_list_properties.list_add_id, jni_new_property);
            if (aws_jni_check_and_clear_exception(env)) {
                AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not add new user property");
                return AWS_OP_ERR;
            }

            if ((bool)jni_add_result == false) {
                return AWS_OP_ERR;
            }
        }
    }
    return AWS_OP_SUCCESS;
}

int s_set_int_enum_in_packet(
    JNIEnv *env,
    const int *int_enum,
    jobject packet,
    jmethodID set_enum_field_id,
    bool optional) {

    if (int_enum) {
        if (*int_enum < 0) {
            return AWS_OP_ERR;
        }
        (*env)->CallVoidMethod(env, packet, set_enum_field_id, (jint)*int_enum);
        if (aws_jni_check_and_clear_exception(env)) {
            return AWS_OP_ERR;
        }

        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    }

    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

jobject s_aws_mqtt5_client_create_jni_connack_packet_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_packet_connack_view *native_connack_data) {

    jobject connack_data = (*env)->NewObject(
        env,
        mqtt5_connack_packet_properties.connack_packet_class,
        mqtt5_connack_packet_properties.connack_constructor_id);

    (*env)->SetBooleanField(
        env,
        connack_data,
        mqtt5_connack_packet_properties.connack_session_present_field_id,
        (jboolean)native_connack_data->session_present);

    int reason_code_int = (int)native_connack_data->reason_code;
    if (reason_code_int < 0) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Error when creating ConnAckPacket from native: Reason code is negative!");
        return NULL;
    }
    (*env)->CallVoidMethod(
        env, connack_data, mqtt5_connack_packet_properties.connack_native_add_reason_code_id, (jint)reason_code_int);

    /* Set all of the optional data in Java classes */
    if (s_set_jni_uint32_t_field_in_packet(
            env,
            native_connack_data->session_expiry_interval,
            connack_data,
            mqtt5_connack_packet_properties.connack_session_expiry_interval_field_id,
            "session expiry interval",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_uint16_t_field_in_packet(
            env,
            native_connack_data->receive_maximum,
            connack_data,
            mqtt5_connack_packet_properties.connack_receive_maximum_field_id,
            "receive maximum",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    };

    if (native_connack_data->maximum_qos) {
        int *maximum_qos_int = (int *)native_connack_data->maximum_qos;
        if (s_set_int_enum_in_packet(
                env,
                maximum_qos_int,
                connack_data,
                mqtt5_connack_packet_properties.connack_native_add_maximum_qos_id,
                true) != AWS_OP_SUCCESS) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "Error when creating ConnAckPacket from native: Could not set maximum QOS");
            return NULL;
        }
    }

    if (s_set_jni_bool_field_in_packet(
            env,
            native_connack_data->retain_available,
            connack_data,
            mqtt5_connack_packet_properties.connack_retain_available_field_id,
            "retain available",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_uint32_t_field_in_packet(
            env,
            native_connack_data->maximum_packet_size,
            connack_data,
            mqtt5_connack_packet_properties.connack_maximum_packet_size_field_id,
            "maximum packet size",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_string_field_in_packet(
            env,
            native_connack_data->assigned_client_identifier,
            connack_data,
            mqtt5_connack_packet_properties.connack_assigned_client_identifier_field_id,
            "assigned client identifier",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_string_field_in_packet(
            env,
            native_connack_data->reason_string,
            connack_data,
            mqtt5_connack_packet_properties.connack_reason_string_field_id,
            "reason string",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_bool_field_in_packet(
            env,
            native_connack_data->wildcard_subscriptions_available,
            connack_data,
            mqtt5_connack_packet_properties.connack_wildcard_subscriptions_available_field_id,
            "wildcard subscriptions available",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    };
    if (s_set_jni_bool_field_in_packet(
            env,
            native_connack_data->subscription_identifiers_available,
            connack_data,
            mqtt5_connack_packet_properties.connack_subscription_identifiers_available_field_id,
            "subscription identifiers available",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    };
    if (s_set_jni_bool_field_in_packet(
            env,
            native_connack_data->shared_subscriptions_available,
            connack_data,
            mqtt5_connack_packet_properties.connack_shared_subscriptions_available_field_id,
            "shared subscriptions available",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    };
    if (s_set_jni_uint16_t_field_in_packet(
            env,
            native_connack_data->server_keep_alive,
            connack_data,
            mqtt5_connack_packet_properties.connack_server_keep_alive_field_id,
            "server keep alive",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    };
    if (s_set_jni_string_field_in_packet(
            env,
            native_connack_data->response_information,
            connack_data,
            mqtt5_connack_packet_properties.connack_response_information_field_id,
            "response information",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_string_field_in_packet(
            env,
            native_connack_data->server_reference,
            connack_data,
            mqtt5_connack_packet_properties.connack_server_reference_field_id,
            "server reference",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_user_properties_field(
            env,
            native_connack_data->user_property_count,
            native_connack_data->user_properties,
            connack_data,
            mqtt5_connack_packet_properties.connack_user_properties_field_id) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT, "Error when creating ConnAckPacket from native: could not add user property!");
        return NULL;
    }

    return connack_data;
}

jobject s_aws_mqtt5_client_create_jni_disconnect_packet_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_packet_disconnect_view *native_disconnect_data) {

    jobject disconnect_packet_data = (*env)->NewObject(
        env,
        mqtt5_disconnect_packet_properties.disconnect_packet_class,
        mqtt5_disconnect_packet_properties.disconnect_constructor_id);

    int reason_code_int = (int)native_disconnect_data->reason_code;
    if (s_set_int_enum_in_packet(
            env,
            &reason_code_int,
            disconnect_packet_data,
            mqtt5_disconnect_packet_properties.disconnect_native_add_disconnect_reason_code_id,
            false) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT, "Error when creating DisconnectPacket from native: Could not set reason code");
        return NULL;
    }

    if (s_set_jni_uint32_t_field_in_packet(
            env,
            native_disconnect_data->session_expiry_interval_seconds,
            disconnect_packet_data,
            mqtt5_disconnect_packet_properties.disconnect_session_expiry_interval_seconds_field_id,
            "session expiry interval seconds",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_string_field_in_packet(
            env,
            native_disconnect_data->reason_string,
            disconnect_packet_data,
            mqtt5_disconnect_packet_properties.disconnect_reason_string_field_id,
            "reason string",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_jni_string_field_in_packet(
            env,
            native_disconnect_data->server_reference,
            disconnect_packet_data,
            mqtt5_disconnect_packet_properties.disconnect_session_server_reference_field_id,
            "server reference",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_user_properties_field(
            env,
            native_disconnect_data->user_property_count,
            native_disconnect_data->user_properties,
            disconnect_packet_data,
            mqtt5_disconnect_packet_properties.disconnect_user_properties_field_id) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT, "Error when creating DisconnectPacket from native: could not add user property!");
        return NULL;
    }

    return disconnect_packet_data;
}

jobject s_aws_mqtt5_client_create_jni_negotiated_settings_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_negotiated_settings *native_negotiated_settings_data) {

    jobject negotiated_settings_data = (*env)->NewObject(
        env,
        mqtt5_negotiated_settings_properties.negotiated_settings_class,
        mqtt5_negotiated_settings_properties.negotiated_settings_constructor_id);

    (*env)->CallVoidMethod(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_native_set_qos_id,
        (jint)native_negotiated_settings_data->maximum_qos);
    aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    (*env)->SetLongField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_session_expiry_interval_field_id,
        (jlong)native_negotiated_settings_data->session_expiry_interval);
    (*env)->SetIntField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_receive_maximum_from_server_field_id,
        (jint)native_negotiated_settings_data->receive_maximum_from_server);
    (*env)->SetLongField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_maximum_packet_size_to_server_field_id,
        (jlong)native_negotiated_settings_data->maximum_packet_size_to_server);
    (*env)->SetIntField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_server_keep_alive_field_id,
        (jint)native_negotiated_settings_data->server_keep_alive);
    (*env)->SetBooleanField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_retain_available_field_id,
        (jboolean)native_negotiated_settings_data->retain_available);
    (*env)->SetBooleanField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_wildcard_subscriptions_available_field_id,
        (jboolean)native_negotiated_settings_data->wildcard_subscriptions_available);
    (*env)->SetBooleanField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_subscription_identifiers_available_field_id,
        (jboolean)native_negotiated_settings_data->subscription_identifiers_available);
    (*env)->SetBooleanField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_shared_subscriptions_available_field_id,
        (jboolean)native_negotiated_settings_data->shared_subscriptions_available);
    (*env)->SetBooleanField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_rejoined_session_field_id,
        (jboolean)native_negotiated_settings_data->rejoined_session);

    struct aws_byte_cursor client_id_storage_cursor =
        aws_byte_cursor_from_buf(&native_negotiated_settings_data->client_id_storage);
    jstring jni_assigned_client_id = aws_jni_string_from_cursor(env, &client_id_storage_cursor);
    (*env)->SetObjectField(
        env,
        negotiated_settings_data,
        mqtt5_negotiated_settings_properties.negotiated_settings_assigned_client_id_field_id,
        jni_assigned_client_id);

    return negotiated_settings_data;
}

jobject s_aws_mqtt5_client_create_jni_publish_packet_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_packet_publish_view *publish) {
    jobject publish_packet_data = (*env)->NewObject(
        env,
        mqtt5_publish_packet_properties.publish_packet_class,
        mqtt5_publish_packet_properties.publish_constructor_id);

    jbyteArray jni_payload = aws_jni_byte_array_from_cursor(env, &publish->payload);
    (*env)->SetObjectField(
        env, publish_packet_data, mqtt5_publish_packet_properties.publish_payload_field_id, jni_payload);

    int publish_qos_int = (int)publish->qos;
    if (s_set_int_enum_in_packet(
            env,
            &publish_qos_int,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_native_set_qos_id,
            false) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Error when creating PublishPacket from native: Could not set QOS");
        return NULL;
    }

    if (s_set_jni_bool_field_in_packet(
            env,
            &publish->retain,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_retain_field_id,
            "retain",
            false) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Error when creating PublishPacket from native: Could not set retain");
        return NULL;
    }

    jstring jni_topic = aws_jni_string_from_cursor(env, &publish->topic);
    (*env)->SetObjectField(env, publish_packet_data, mqtt5_publish_packet_properties.publish_topic_field_id, jni_topic);

    if (publish->payload_format) {
        if (s_set_int_enum_in_packet(
                env,
                (int *)publish->payload_format,
                publish_packet_data,
                mqtt5_publish_packet_properties.publish_native_set_payload_format_indicator_id,
                true) != AWS_OP_SUCCESS) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "Error when creating PublishPacket from native: Could not set payload format");
            return NULL;
        }
    }

    if (s_set_jni_uint32_t_field_in_packet(
            env,
            publish->message_expiry_interval_seconds,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_message_expiry_interval_seconds_field_id,
            "message expiry interval seconds",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_jni_string_field_in_packet(
            env,
            publish->response_topic,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_response_topic_field_id,
            "response topic",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_jni_byte_array_field_in_packet(
            env,
            publish->correlation_data,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_correlation_data_field_id,
            "correlation data",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_jni_string_field_in_packet(
            env,
            publish->content_type,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_content_type_field_id,
            "content type",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (publish->subscription_identifier_count && publish->subscription_identifiers) {
        jobject jni_subscription_identifiers = (*env)->NewObject(
            env, boxed_array_list_properties.list_class, boxed_array_list_properties.list_constructor_id);
        (*env)->SetObjectField(
            env,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_subscription_identifiers_field_id,
            jni_subscription_identifiers);

        for (size_t i = 0; i < publish->subscription_identifier_count; ++i) {
            const uint32_t *identifier = &publish->subscription_identifiers[i];
            jobject jni_identifier_obj = (*env)->NewObject(
                env, boxed_long_properties.long_class, boxed_long_properties.constructor, (jlong)*identifier);
            jboolean jni_add_result = (*env)->CallBooleanMethod(
                env, jni_subscription_identifiers, boxed_list_properties.list_add_id, jni_identifier_obj);
            if (aws_jni_check_and_clear_exception(env)) {
                AWS_LOGF_ERROR(
                    AWS_LS_MQTT_CLIENT,
                    "When creating PublishPacket from native could not add subscription identifier!");
                return NULL;
            }
            if ((bool)jni_add_result == false) {
                AWS_LOGF_ERROR(
                    AWS_LS_MQTT_CLIENT,
                    "When creating PublishPacket from native could not add subscription identifier!");
                return NULL;
            }
        }
    }

    if (s_set_user_properties_field(
            env,
            publish->user_property_count,
            publish->user_properties,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_user_properties_field_id) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "When creating PublishPacket from native could not add user properties!");
        return NULL;
    }

    return publish_packet_data;
}

jobject s_aws_mqtt5_client_create_jni_puback_packet_from_native(
    JNIEnv *env,
    struct aws_mqtt5_packet_puback_view *puback_packet) {
    /* Make the PubAck packet */
    jobject puback_packet_data = (*env)->NewObject(
        env, mqtt5_puback_packet_properties.puback_packet_class, mqtt5_puback_packet_properties.puback_constructor_id);

    int reason_code_int = (int)puback_packet->reason_code;
    if (s_set_int_enum_in_packet(
            env,
            &reason_code_int,
            puback_packet_data,
            mqtt5_puback_packet_properties.puback_native_add_reason_code_id,
            false) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Error when creating PubAck result from native: Could not set reason code");
        return NULL;
    }
    if (s_set_jni_string_field_in_packet(
            env,
            puback_packet->reason_string,
            puback_packet_data,
            mqtt5_puback_packet_properties.puback_reason_string_field_id,
            "reason string",
            true) != AWS_OP_SUCCESS) {
        return NULL;
    }
    if (s_set_user_properties_field(
            env,
            puback_packet->user_property_count,
            puback_packet->user_properties,
            puback_packet_data,
            mqtt5_puback_packet_properties.puback_user_properties_field_id) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "When creating PubAckPacket from native could not add user property!");
        return NULL;
    }
    return puback_packet_data;
}

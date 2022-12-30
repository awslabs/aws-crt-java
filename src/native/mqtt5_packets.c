/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <mqtt5_packets.h>

#include <aws/mqtt/v5/mqtt5_client.h>
#include <crt.h>

#include <java_class_ids.h>

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
 * STRUCT DEFINITIONS
 ******************************************************************************/

struct aws_mqtt5_packet_connect_view_java_jni {
    struct aws_mqtt5_packet_connect_view packet;

    struct aws_byte_buf client_id_buf;
    struct aws_byte_cursor client_id_cursor;
    struct aws_byte_buf username_buf;
    struct aws_byte_cursor username_cursor;
    struct aws_byte_buf password_buf;
    struct aws_byte_cursor password_cursor;
    uint32_t session_expiry_interval_seconds;
    uint8_t request_response_information;
    uint8_t request_problem_information;
    uint16_t receive_maximum;
    uint16_t topic_alias_maximum;
    uint32_t maximum_packet_size_bytes;
    uint32_t will_delay_interval_seconds;
    uint16_t keep_alive_interval_seconds;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
    struct aws_mqtt5_packet_publish_view_java_jni *will_publish_packet;
};

struct aws_mqtt5_packet_disconnect_view_java_jni {
    struct aws_mqtt5_packet_disconnect_view packet;

    struct aws_byte_buf reason_string_buf;
    struct aws_byte_cursor reason_string_cursor;
    struct aws_byte_buf server_reference_buf;
    struct aws_byte_cursor server_reference_cursor;
    uint32_t session_expiry_interval_seconds;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct aws_mqtt5_packet_publish_view_java_jni {
    struct aws_mqtt5_packet_publish_view packet;

    struct aws_byte_buf payload_buf;
    struct aws_byte_cursor payload_cursor;
    struct aws_byte_buf topic_buf;
    struct aws_byte_cursor topic_cursor;
    enum aws_mqtt5_payload_format_indicator payload_format;
    uint32_t message_expiry_interval_seconds;
    uint16_t topic_alias;
    struct aws_byte_buf response_topic_buf;
    struct aws_byte_cursor response_topic_cursor;
    struct aws_byte_buf correlation_data_buf;
    struct aws_byte_cursor correlation_data_cursor;
    struct aws_byte_buf content_type_buf;
    struct aws_byte_cursor content_type_cursor;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct aws_mqtt5_packet_subscribe_view_java_jni {
    struct aws_mqtt5_packet_subscribe_view packet;

    /* Contains aws_mqtt5_subscription_view pointers */
    struct aws_array_list topic_filters;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_subscription_topic_filters;
    uint32_t subscription_identifier;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct aws_mqtt5_packet_unsubscribe_view_java_jni {
    struct aws_mqtt5_packet_unsubscribe_view packet;

    /* Contains aws_byte_cursor pointers */
    struct aws_array_list topic_filters;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_topic_filters;
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct buffer_and_cursor_array_holder_struct {
    struct aws_byte_cursor cursor;
    struct aws_byte_buf buffer;
};

/*******************************************************************************
 * HELPER FUNCTIONS
 ******************************************************************************/

static int s_populate_user_properties(
    JNIEnv *env,
    jobject jni_user_properties_list,
    size_t java_packet_native_user_property_count,
    const struct aws_mqtt5_user_property **java_packet_native_user_properties,
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list *java_packet_user_properties_holder,
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list *java_packet_user_properties_struct_holder) {

    if (jni_user_properties_list) {
        for (size_t i = 0; i < java_packet_native_user_property_count; i++) {
            jobject jni_property =
                (*env)->CallObjectMethod(env, jni_user_properties_list, boxed_list_properties.list_get_id, (jint)i);
            if (!jni_property || aws_jni_check_and_clear_exception(env)) {
                AWS_LOGF_ERROR(
                    AWS_LS_MQTT_CLIENT,
                    "Could not populate user properties due to being unable to get property in list from Java");
                return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
            }

            jstring jni_property_key =
                (jstring)(*env)->GetObjectField(env, jni_property, mqtt5_user_property_properties.property_key_id);
            if (aws_jni_check_and_clear_exception(env) || !jni_property_key) {
                AWS_LOGF_ERROR(
                    AWS_LS_MQTT_CLIENT,
                    "Could not populate user properties due to exception when getting property key");
                return aws_raise_error(AWS_ERROR_INVALID_STATE);
            }
            jstring jni_property_value =
                (jstring)(*env)->GetObjectField(env, jni_property, mqtt5_user_property_properties.property_value_id);
            if (aws_jni_check_and_clear_exception(env) || !jni_property_value) {
                AWS_LOGF_ERROR(
                    AWS_LS_MQTT_CLIENT,
                    "Could not populate user properties due to exception when getting property value");
                return aws_raise_error(AWS_ERROR_INVALID_STATE);
            }

            if (!jni_property_key) {
                AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Error reading a user property: Key in user property was NULL!");
                return aws_raise_error(AWS_ERROR_INVALID_STATE);
            }
            if (!jni_property_value) {
                AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Error reading a user property: Key in user property was NULL!");
                return aws_raise_error(AWS_ERROR_INVALID_STATE);
            }

            // Get a temporary cursor from JNI, copy it, and then destroy the JNI version, leaving the byte_buffer copy.
            // This gets around JNI stuff going out of scope.
            struct buffer_and_cursor_array_holder_struct holder_property_key;
            struct aws_byte_cursor tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_property_key);
            aws_byte_buf_init_copy_from_cursor(&holder_property_key.buffer, aws_jni_get_allocator(), tmp_cursor);
            holder_property_key.cursor = aws_byte_cursor_from_buf(&holder_property_key.buffer);
            aws_jni_byte_cursor_from_jstring_release(env, jni_property_key, tmp_cursor);
            jni_property_key = NULL;

            // Get a temporary cursor from JNI, copy it, and then destroy the JNI version, leaving the byte_buffer copy.
            // This gets around JNI stuff going out of scope.
            struct buffer_and_cursor_array_holder_struct holder_property_value;
            tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_property_value);
            aws_byte_buf_init_copy_from_cursor(&holder_property_value.buffer, aws_jni_get_allocator(), tmp_cursor);
            holder_property_value.cursor = aws_byte_cursor_from_buf(&holder_property_value.buffer);
            aws_jni_byte_cursor_from_jstring_release(env, jni_property_value, tmp_cursor);
            jni_property_value = NULL;

            aws_array_list_push_back(java_packet_user_properties_holder, (void *)&holder_property_key);
            aws_array_list_push_back(java_packet_user_properties_holder, (void *)&holder_property_value);

            struct aws_mqtt5_user_property jni_property_struct = {
                .name = holder_property_key.cursor,
                .value = holder_property_value.cursor,
            };
            aws_array_list_push_back(java_packet_user_properties_struct_holder, (void *)&jni_property_struct);
        }
        *java_packet_native_user_properties =
            (struct aws_mqtt5_user_property *)java_packet_user_properties_struct_holder->data;
    }
    return AWS_OP_SUCCESS;
}

static int s_allocate_user_properties_array_holders(
    struct aws_allocator *allocator,
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list *holder_array,
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list *user_property_array,
    size_t init_entries) {

    if (init_entries > 0) {
        if (aws_array_list_init_dynamic(
                holder_array, allocator, 2 * init_entries, sizeof(struct buffer_and_cursor_array_holder_struct)) !=
                AWS_OP_SUCCESS ||
            aws_array_list_init_dynamic(
                user_property_array, allocator, 2 * init_entries, sizeof(struct aws_mqtt5_user_property)) !=
                AWS_OP_SUCCESS) {
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
    }
    return AWS_OP_SUCCESS;
}

static void s_cleanup_two_aws_array(
    JNIEnv *env,
    struct aws_array_list *user_properties_holder,
    struct aws_array_list *user_properties_struct_holder) {

    (void)env;
    if (aws_array_list_is_valid(user_properties_holder)) {
        /**
         * Note that this ONLY frees the array holders for the non-struct array.
         * We want to keep the struct one in memory since it belongs to a packet or similar.
         * If both need to be freed, then we assume whomever is calling this will handle it.
         */
        for (size_t i = 0; i < aws_array_list_length(user_properties_holder); i++) {
            struct buffer_and_cursor_array_holder_struct holder;
            aws_array_list_get_at(user_properties_holder, &holder, i);
            if (aws_byte_buf_is_valid(&holder.buffer)) {
                aws_byte_buf_clean_up(&holder.buffer);
            }
        }
        aws_array_list_clean_up(user_properties_holder);
    }
    if (aws_array_list_is_valid(user_properties_struct_holder)) {
        aws_array_list_clean_up(user_properties_struct_holder);
    }
}

int aws_get_uint16_from_jobject(
    JNIEnv *env,
    jobject object,
    jfieldID object_field,
    char *object_name,
    char *field_name,
    uint16_t *result,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jobject jlong_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jlong_obj) {
        jlong jlong_value = (*env)->CallLongMethod(env, jlong_obj, boxed_long_properties.long_value_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int64_t jlong_value_check = (int64_t)jlong_value;
        if (jlong_value_check < 0) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is less than 0", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        } else if (jlong_value_check > UINT16_MAX) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is more than UINT16_MAX", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }
        *result = (uint16_t)jlong_value;

        if (was_value_set != NULL) {
            *was_value_set = true;
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

int aws_get_uint32_from_jobject(
    JNIEnv *env,
    jobject object,
    jfieldID object_field,
    char *object_name,
    char *field_name,
    uint32_t *result,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jobject jlong_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jlong_obj) {
        jlong jlong_value = (*env)->CallLongMethod(env, jlong_obj, boxed_long_properties.long_value_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int64_t jlong_value_check = (int64_t)jlong_value;
        if (jlong_value_check < 0) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is less than 0", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        } else if (jlong_value_check > UINT32_MAX) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is more than UINT32_MAX", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }
        *result = (uint32_t)jlong_value_check;

        if (was_value_set != NULL) {
            *was_value_set = true;
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

int aws_get_uint64_from_jobject(
    JNIEnv *env,
    jobject object,
    jfieldID object_field,
    char *object_name,
    char *field_name,
    uint64_t *result,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jobject jlong_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jlong_obj) {
        jlong jlong_value = (*env)->CallLongMethod(env, jlong_obj, boxed_long_properties.long_value_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int64_t jlong_value_check = (int64_t)jlong_value;
        if (jlong_value_check < 0) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is less than 0", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }
        *result = (uint64_t)jlong_value_check;

        if (was_value_set != NULL) {
            *was_value_set = true;
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

int aws_get_string_from_jobject(
    JNIEnv *env,
    jobject object,
    jfieldID object_field,
    char *object_name,
    char *field_name,
    struct aws_byte_buf *result_buf,
    struct aws_byte_cursor *result_cursor,
    bool is_optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jstring jstring_value = (jstring)(*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jstring_value) {
        // Get the data, copy it, and then release the JNI stuff
        struct aws_byte_cursor tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jstring_value);
        aws_byte_buf_init_copy_from_cursor(result_buf, aws_jni_get_allocator(), tmp_cursor);
        *result_cursor = aws_byte_cursor_from_buf(result_buf);
        aws_jni_byte_cursor_from_jstring_release(env, jstring_value, tmp_cursor);

        if (was_value_set != NULL) {
            *was_value_set = true;
        }

        if (!is_optional) {
            return AWS_OP_SUCCESS;
        }
    }
    if (is_optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

int aws_get_byte_array_from_jobject(
    JNIEnv *env,
    jobject object,
    jfieldID object_field,
    char *object_name,
    char *field_name,
    struct aws_byte_buf *result_buf,
    struct aws_byte_cursor *result_cursor,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jbyteArray jbyte_array_value = (jbyteArray)(*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jbyte_array_value) {
        // Get the data, copy it, and then release the JNI stuff
        struct aws_byte_cursor tmp_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jbyte_array_value);
        aws_byte_buf_init_copy_from_cursor(result_buf, aws_jni_get_allocator(), tmp_cursor);
        *result_cursor = aws_byte_cursor_from_buf(result_buf);
        aws_jni_byte_cursor_from_jbyteArray_release(env, jbyte_array_value, tmp_cursor);

        if (was_value_set != NULL) {
            *was_value_set = true;
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

int aws_get_boolean_from_jobject(
    JNIEnv *env,
    jobject object,
    jfieldID object_field,
    char *object_name,
    char *field_name,
    uint8_t *result_boolean_int,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jobject jboolean_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jboolean_obj) {
        jboolean jboolean_value =
            (*env)->CallBooleanMethod(env, jboolean_obj, boxed_boolean_properties.boolean_get_value_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting native value from %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *result_boolean_int = (uint8_t)jboolean_value;
        if (was_value_set != NULL) {
            *was_value_set = true;
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

int aws_get_enum_from_jobject(
    JNIEnv *env,
    jobject object,
    jmethodID object_enum_field,
    char *object_name,
    char *enum_name,
    jmethodID enum_value_field,
    uint32_t *enum_value_destination,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    if (enum_value_destination == NULL) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT,
            "%s create_from_java: Error getting %s due to null destination",
            object_name,
            enum_name);
        return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    }

    jobject jni_retain_handling_type = (*env)->CallObjectMethod(env, object, object_enum_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, enum_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jni_retain_handling_type) {
        jint enum_value = (*env)->CallIntMethod(env, jni_retain_handling_type, enum_value_field);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting native value from %s", object_name, enum_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        if (enum_value < 0) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Native value from %s is less than 0", object_name, enum_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        } else if ((uint64_t)enum_value > UINT16_MAX) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT,
                "%s create_from_java: Native value from %s is more than UINT16_MAX",
                object_name,
                enum_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *enum_value_destination = (uint32_t)enum_value;
        if (was_value_set != NULL) {
            *was_value_set = true;
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

static int s_get_user_properties_from_packet_optional(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    char *packet_name,
    size_t *packet_user_property_count,
    /* Contains buffer_and_cursor_array_holder_struct pointers */
    struct aws_array_list *jni_user_properties_holder,
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list *jni_user_properties_struct_holder,
    const struct aws_mqtt5_user_property **packet_properties) {

    struct aws_allocator *allocator = aws_jni_get_allocator();

    jobject jni_list = (*env)->GetObjectField(env, packet, packet_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting user properties list", packet_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jni_list) {
        jint jni_user_properties_size = (*env)->CallIntMethod(env, jni_list, boxed_list_properties.list_size_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting user properties list size", packet_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *packet_user_property_count = (size_t)jni_user_properties_size;
        if (AWS_OP_SUCCESS != s_allocate_user_properties_array_holders(
                                  allocator,
                                  jni_user_properties_holder,
                                  jni_user_properties_struct_holder,
                                  *packet_user_property_count)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Could not create user properties array", packet_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int populate_result = s_populate_user_properties(
            env,
            jni_list,
            *packet_user_property_count,
            packet_properties,
            jni_user_properties_holder,
            jni_user_properties_struct_holder);
        if (populate_result != AWS_OP_SUCCESS) {
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
    }
    return AWS_OP_SUCCESS;
}

static int s_get_qos_from_packet(
    JNIEnv *env,
    jobject packet,
    jmethodID packet_field,
    char *packet_name,
    enum aws_mqtt5_qos *packet_qos,
    bool optional,
    bool *was_value_set) {

    if (was_value_set != NULL) {
        *was_value_set = false;
    }

    jobject jni_qos = (*env)->CallObjectMethod(env, packet, packet_field);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting QoS", packet_name);
        return AWS_OP_ERR;
    }
    if (jni_qos) {
        jint jni_qos_value = (*env)->CallIntMethod(env, jni_qos, mqtt5_packet_qos_properties.qos_get_value_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting native value from QoS", packet_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *packet_qos = (enum aws_mqtt5_qos)jni_qos_value;
        if (was_value_set != NULL) {
            *was_value_set = true;
        }
        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    } else {
        if (!optional) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: QoS not found", packet_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
    }
    if (optional) {
        return AWS_OP_SUCCESS;
    } else {
        return AWS_OP_ERR;
    }
}

static char s_connect_packet_string[] = "ConnectPacket";
static char s_disconnect_packet_string[] = "DisconnectPacket";
static char s_publish_packet_string[] = "PublishPacket";
static char s_subscribe_packet_string[] = "SubscribePacket";
static char s_unsubscribe_packet_string[] = "UnsubscribePacket";

/*******************************************************************************
 * CONNECT PACKET FUNCTIONS
 ******************************************************************************/

void aws_mqtt5_packet_connect_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_connect_view_java_jni *java_packet) {
    if (!java_packet) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "id=%p: Destroying ConnectPacket", (void *)java_packet);

    if (aws_byte_buf_is_valid(&java_packet->client_id_buf)) {
        aws_byte_buf_clean_up(&java_packet->client_id_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->username_buf)) {
        aws_byte_buf_clean_up(&java_packet->username_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->password_buf)) {
        aws_byte_buf_clean_up(&java_packet->password_buf);
    }
    if (java_packet->will_publish_packet) {
        aws_mqtt5_packet_publish_view_java_destroy(env, allocator, java_packet->will_publish_packet);
    }

    s_cleanup_two_aws_array(
        env, &java_packet->jni_user_properties_holder, &java_packet->jni_user_properties_struct_holder);
    aws_mem_release(allocator, java_packet);
}

/**
 * Creates a JNI connack packet from the given Java connack packet and returns it. It creates a new packet but it does
 * NOT free it. You will need to call aws_mqtt5_packet_connect_view_java_destroy when you are done with it.
 */
struct aws_mqtt5_packet_connect_view_java_jni *aws_mqtt5_packet_connect_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_connect_packet) {
    struct aws_mqtt5_packet_connect_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_connect_view_java_jni));
    if (java_packet == NULL) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "ConnectPacket create_from_java: Creating new ConnectPacket failed");
        return NULL;
    }

    /* Needed to track if optionals are set or not */
    bool was_value_set = false;

    if (aws_get_uint16_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_keep_alive_interval_seconds_field_id,
            s_connect_packet_string,
            "keep alive interval seconds",
            &java_packet->keep_alive_interval_seconds,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.keep_alive_interval_seconds = java_packet->keep_alive_interval_seconds;
    }

    if (aws_get_string_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_client_id_field_id,
            s_connect_packet_string,
            "client ID",
            &java_packet->client_id_buf,
            &java_packet->client_id_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.client_id = java_packet->client_id_cursor;
    }

    if (aws_get_string_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_username_field_id,
            s_connect_packet_string,
            "username",
            &java_packet->username_buf,
            &java_packet->username_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.username = &java_packet->username_cursor;
    }

    if (aws_get_byte_array_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_password_field_id,
            s_connect_packet_string,
            "password",
            &java_packet->password_buf,
            &java_packet->password_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.password = &java_packet->password_cursor;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_session_expiry_interval_seconds_field_id,
            s_connect_packet_string,
            "session expiry interval seconds",
            &java_packet->session_expiry_interval_seconds,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.session_expiry_interval_seconds = &java_packet->session_expiry_interval_seconds;
    }

    if (aws_get_boolean_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_request_response_information_field_id,
            s_connect_packet_string,
            "request response information",
            &java_packet->request_response_information,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.request_response_information = &java_packet->request_response_information;
    }

    if (aws_get_boolean_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_request_problem_information_field_id,
            s_connect_packet_string,
            "request problem information",
            &java_packet->request_problem_information,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.request_problem_information = &java_packet->request_problem_information;
    }

    if (aws_get_uint16_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_receive_maximum_field_id,
            s_connect_packet_string,
            "receive maximum",
            &java_packet->receive_maximum,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.receive_maximum = &java_packet->receive_maximum;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_maximum_packet_size_bytes_field_id,
            s_connect_packet_string,
            "maximum packet size",
            &java_packet->maximum_packet_size_bytes,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.maximum_packet_size_bytes = &java_packet->maximum_packet_size_bytes;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_will_delay_interval_seconds_field_id,
            s_connect_packet_string,
            "will delay interval",
            &java_packet->will_delay_interval_seconds,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.will_delay_interval_seconds = &java_packet->will_delay_interval_seconds;
    }

    jobject jni_will_packet =
        (*env)->GetObjectField(env, java_connect_packet, mqtt5_connect_packet_properties.connect_will_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "ConnectPacket create_from_java: Error getting will packet");
        goto on_error;
    }
    if (jni_will_packet) {
        java_packet->will_publish_packet =
            aws_mqtt5_packet_publish_view_create_from_java(env, allocator, jni_will_packet);
        if (java_packet->will_publish_packet == NULL) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "ConnectPacket create_from_java: Error getting will packet");
            goto on_error;
        }
        java_packet->packet.will = &java_packet->will_publish_packet->packet;
    }

    if (s_get_user_properties_from_packet_optional(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_user_properties_field_id,
            s_connect_packet_string,
            &java_packet->packet.user_property_count,
            &java_packet->jni_user_properties_holder,
            &java_packet->jni_user_properties_struct_holder,
            &java_packet->packet.user_properties) == AWS_OP_ERR) {
        goto on_error;
    }

    return java_packet;

on_error:

    /* Clean up */
    aws_mqtt5_packet_connect_view_java_destroy(env, allocator, java_packet);
    return NULL;
}

struct aws_mqtt5_packet_connect_view *aws_mqtt5_packet_connect_view_get_packet(
    struct aws_mqtt5_packet_connect_view_java_jni *java_packet) {
    if (java_packet) {
        return &java_packet->packet;
    } else {
        return NULL;
    }
}

/*******************************************************************************
 * PACKET DISCONNECT FUNCTIONS
 ******************************************************************************/

void aws_mqtt5_packet_disconnect_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_disconnect_view_java_jni *java_packet) {
    if (!java_packet) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "id=%p: Destroying DisconnectPacket", (void *)java_packet);

    if (aws_byte_buf_is_valid(&java_packet->reason_string_buf)) {
        aws_byte_buf_clean_up(&java_packet->reason_string_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->server_reference_buf)) {
        aws_byte_buf_clean_up(&java_packet->server_reference_buf);
    }

    s_cleanup_two_aws_array(
        env, &java_packet->jni_user_properties_holder, &java_packet->jni_user_properties_struct_holder);
    aws_mem_release(allocator, java_packet);
}

struct aws_mqtt5_packet_disconnect_view_java_jni *aws_mqtt5_packet_disconnect_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_disconnect_packet) {

    struct aws_mqtt5_packet_disconnect_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_disconnect_view_java_jni));
    if (java_packet == NULL) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "DisconnectPacket create_from_java: Creating new DisconnectPacket failed");
        return NULL;
    }

    /* Needed to track if optionals are set or not */
    bool was_value_set = false;

    uint32_t reason_code_enum;
    if (aws_get_enum_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_get_reason_code_id,
            s_disconnect_packet_string,
            "reason code",
            mqtt5_disconnect_reason_code_properties.code_get_value_id,
            &reason_code_enum,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.reason_code = (enum aws_mqtt5_disconnect_reason_code)reason_code_enum;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_session_expiry_interval_seconds_field_id,
            s_disconnect_packet_string,
            "session expiry interval seconds",
            &java_packet->session_expiry_interval_seconds,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.session_expiry_interval_seconds = &java_packet->session_expiry_interval_seconds;
    }

    if (aws_get_string_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_reason_string_field_id,
            s_disconnect_packet_string,
            "reason string",
            &java_packet->reason_string_buf,
            &java_packet->reason_string_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.reason_string = &java_packet->reason_string_cursor;
    }

    if (aws_get_string_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_session_server_reference_field_id,
            s_disconnect_packet_string,
            "server reference",
            &java_packet->server_reference_buf,
            &java_packet->server_reference_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.server_reference = &java_packet->server_reference_cursor;
    }

    if (s_get_user_properties_from_packet_optional(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_user_properties_field_id,
            s_disconnect_packet_string,
            &java_packet->packet.user_property_count,
            &java_packet->jni_user_properties_holder,
            &java_packet->jni_user_properties_struct_holder,
            &java_packet->packet.user_properties) == AWS_OP_ERR) {
        goto on_error;
    }

    return java_packet;

on_error:

    /* Clean up */
    aws_mqtt5_packet_disconnect_view_java_destroy(env, allocator, java_packet);
    return NULL;
}

struct aws_mqtt5_packet_disconnect_view *aws_mqtt5_packet_disconnect_view_get_packet(
    struct aws_mqtt5_packet_disconnect_view_java_jni *java_packet) {
    if (java_packet) {
        return &java_packet->packet;
    } else {
        return NULL;
    }
}

/*******************************************************************************
 * PUBLISH PACKET FUNCTIONS
 ******************************************************************************/

void aws_mqtt5_packet_publish_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_publish_view_java_jni *java_packet) {
    if (!java_packet) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "id=%p: Destroying PublishPacket", (void *)java_packet);

    if (aws_byte_buf_is_valid(&java_packet->payload_buf)) {
        aws_byte_buf_clean_up(&java_packet->payload_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->topic_buf)) {
        aws_byte_buf_clean_up(&java_packet->topic_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->response_topic_buf)) {
        aws_byte_buf_clean_up(&java_packet->response_topic_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->correlation_data_buf)) {
        aws_byte_buf_clean_up(&java_packet->correlation_data_buf);
    }
    if (aws_byte_buf_is_valid(&java_packet->content_type_buf)) {
        aws_byte_buf_clean_up(&java_packet->content_type_buf);
    }

    s_cleanup_two_aws_array(
        env, &java_packet->jni_user_properties_holder, &java_packet->jni_user_properties_struct_holder);
    aws_mem_release(allocator, java_packet);
}

struct aws_mqtt5_packet_publish_view_java_jni *aws_mqtt5_packet_publish_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_publish_packet) {

    struct aws_mqtt5_packet_publish_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_publish_view_java_jni));
    if (java_packet == NULL) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishPacket create_from_java: Creating new PublishPacket failed");
        return NULL;
    }

    /* Needed to track if optionals are set or not */
    bool was_value_set = false;

    if (aws_get_byte_array_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_payload_field_id,
            s_publish_packet_string,
            "payload",
            &java_packet->correlation_data_buf,
            &java_packet->payload_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.payload = java_packet->payload_cursor;
    }

    if (s_get_qos_from_packet(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_get_qos_id,
            s_publish_packet_string,
            &java_packet->packet.qos,
            false,
            NULL) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishPacket create_from_java: QOS not found");
        goto on_error;
    }

    uint8_t packet_retain;
    if (aws_get_boolean_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_retain_field_id,
            s_publish_packet_string,
            "retain",
            &packet_retain,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.retain = (bool)packet_retain;
    }

    if (aws_get_string_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_topic_field_id,
            s_publish_packet_string,
            "topic",
            &java_packet->topic_buf,
            &java_packet->topic_cursor,
            false,
            NULL) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishPacket create_from_java: No topic found");
        goto on_error;
    }
    java_packet->packet.topic = java_packet->topic_cursor;

    uint32_t format_enum;
    if (aws_get_enum_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_get_payload_format_id,
            s_publish_packet_string,
            "payload format",
            mqtt5_payload_format_indicator_properties.format_get_value_id,
            &format_enum,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->payload_format = (enum aws_mqtt5_payload_format_indicator)format_enum;
        java_packet->packet.payload_format = &java_packet->payload_format;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_message_expiry_interval_seconds_field_id,
            s_publish_packet_string,
            "message expiry interval seconds",
            &java_packet->message_expiry_interval_seconds,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.message_expiry_interval_seconds = &java_packet->message_expiry_interval_seconds;
    }

    if (aws_get_string_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_response_topic_field_id,
            s_publish_packet_string,
            "response topic",
            &java_packet->response_topic_buf,
            &java_packet->response_topic_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.response_topic = &java_packet->response_topic_cursor;
    }

    if (aws_get_byte_array_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_correlation_data_field_id,
            s_publish_packet_string,
            "correlation data",
            &java_packet->correlation_data_buf,
            &java_packet->correlation_data_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.correlation_data = &java_packet->correlation_data_cursor;
    }

    if (aws_get_string_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_content_type_field_id,
            s_publish_packet_string,
            "content type",
            &java_packet->content_type_buf,
            &java_packet->content_type_cursor,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.content_type = &java_packet->content_type_cursor;
    }

    if (s_get_user_properties_from_packet_optional(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_user_properties_field_id,
            s_publish_packet_string,
            &java_packet->packet.user_property_count,
            &java_packet->jni_user_properties_holder,
            &java_packet->jni_user_properties_struct_holder,
            &java_packet->packet.user_properties) == AWS_OP_ERR) {
        goto on_error;
    }

    return java_packet;

on_error:

    /* Clean up */
    aws_mqtt5_packet_publish_view_java_destroy(env, allocator, java_packet);
    return NULL;
}

struct aws_mqtt5_packet_publish_view *aws_mqtt5_packet_publish_view_get_packet(
    struct aws_mqtt5_packet_publish_view_java_jni *java_packet) {
    if (java_packet) {
        return &java_packet->packet;
    } else {
        return NULL;
    }
}

/*******************************************************************************
 * SUBSCRIBE PACKET FUNCTIONS
 ******************************************************************************/

void aws_mqtt5_packet_subscribe_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_subscribe_view_java_jni *java_packet) {
    if (!java_packet) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "id=%p: Destroying SubscribePacket", (void *)java_packet);

    s_cleanup_two_aws_array(
        env, &java_packet->jni_user_properties_holder, &java_packet->jni_user_properties_struct_holder);
    s_cleanup_two_aws_array(env, &java_packet->jni_subscription_topic_filters, &java_packet->topic_filters);
    aws_mem_release(allocator, java_packet);
}

struct aws_mqtt5_packet_subscribe_view_java_jni *aws_mqtt5_packet_subscribe_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_subscribe_packet) {

    jobject jni_subscriptions = (*env)->GetObjectField(
        env, java_subscribe_packet, mqtt5_subscribe_packet_properties.subscribe_subscriptions_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        return NULL;
    }
    if (!jni_subscriptions) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT,
            "SubscribePacket create_from_java: Creating new SubscribePacket failed due to no subscriptions!");
        return NULL;
    }
    jint jni_subscriptions_size = (*env)->CallIntMethod(env, jni_subscriptions, boxed_list_properties.list_size_id);
    if (aws_jni_check_and_clear_exception(env)) {
        return NULL;
    }
    size_t subscriptions_filter_size = (size_t)jni_subscriptions_size;

    if (subscriptions_filter_size <= 0) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: subscriptions count is 0");
        return NULL;
    }

    struct aws_mqtt5_packet_subscribe_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_subscribe_view_java_jni));
    if (java_packet == NULL) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: Creating new SubscribePacket failed");
        return NULL;
    }
    int array_init = aws_array_list_init_dynamic(
        &java_packet->topic_filters, allocator, subscriptions_filter_size, sizeof(struct aws_mqtt5_subscription_view));
    if (array_init != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: Creating new SubscribePacket failed");
        goto on_error;
    }
    int jni_array_init = aws_array_list_init_dynamic(
        &java_packet->jni_subscription_topic_filters,
        allocator,
        subscriptions_filter_size,
        sizeof(struct buffer_and_cursor_array_holder_struct));
    if (jni_array_init != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: Creating new SubscribePacket failed");
        goto on_error;
    }

    /* Needed to track if optionals are set or not */
    bool was_value_set = false;

    if (aws_get_uint32_from_jobject(
            env,
            java_subscribe_packet,
            mqtt5_subscribe_packet_properties.subscribe_subscription_identifier_field_id,
            s_subscribe_packet_string,
            "subscription identifier",
            &java_packet->subscription_identifier,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto on_error;
    }
    if (was_value_set) {
        java_packet->packet.subscription_identifier = &java_packet->subscription_identifier;
    }

    java_packet->packet.subscription_count = subscriptions_filter_size;
    for (size_t i = 0; i < subscriptions_filter_size; i++) {
        /* Populate */
        struct aws_mqtt5_subscription_view subscription_view;
        struct buffer_and_cursor_array_holder_struct holder;

        jobject jni_packet_subscribe_subscription =
            (*env)->CallObjectMethod(env, jni_subscriptions, boxed_list_properties.list_get_id, (jint)i);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: Error getting topic filters");
            goto on_error;
        }

        jstring jni_topic_filter = (jstring)(*env)->CallObjectMethod(
            env, jni_packet_subscribe_subscription, mqtt5_subscription_properties.subscribe_get_topic_filter_id);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: Error getting subscription topic filter");
            goto on_error;
        }
        if (jni_topic_filter) {
            // Get a temporary cursor from JNI, copy it, and then destroy the JNI version, leaving the byte_buffer copy.
            // This gets around JNI stuff going out of scope.
            struct aws_byte_cursor tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_topic_filter);
            aws_byte_buf_init_copy_from_cursor(&holder.buffer, aws_jni_get_allocator(), tmp_cursor);
            holder.cursor = aws_byte_cursor_from_buf(&holder.buffer);
            aws_jni_byte_cursor_from_jstring_release(env, jni_topic_filter, tmp_cursor);
            subscription_view.topic_filter = holder.cursor;
            jni_topic_filter = NULL;
        } else {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: subscription topic filter is required");
            goto on_error;
        }

        if (s_get_qos_from_packet(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_get_qos_id,
                s_subscribe_packet_string,
                &subscription_view.qos,
                false,
                NULL) == AWS_OP_ERR) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribePacket create_from_java: subscription QoS is required");
            goto on_error;
        }

        uint8_t subscription_no_local;
        if (aws_get_boolean_from_jobject(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_no_local_field_id,
                s_subscribe_packet_string,
                "no local",
                &subscription_no_local,
                true,
                &was_value_set) != AWS_OP_SUCCESS) {
            goto on_error;
        }
        if (was_value_set) {
            subscription_view.no_local = (bool)subscription_no_local;
        }

        uint8_t retain_as_published;
        if (aws_get_boolean_from_jobject(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_retain_as_published_field_id,
                s_subscribe_packet_string,
                "no local",
                &retain_as_published,
                true,
                &was_value_set) != AWS_OP_SUCCESS) {
            goto on_error;
        }
        if (was_value_set) {
            subscription_view.retain_as_published = (bool)retain_as_published;
        }

        uint32_t retain_enum;
        if (aws_get_enum_from_jobject(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_get_retain_handling_type_id,
                s_subscribe_packet_string,
                "subscription retain handling type",
                mqtt5_retain_handling_type_properties.retain_get_value_id,
                &retain_enum,
                true,
                &was_value_set) == AWS_OP_ERR) {
            goto on_error;
        }
        if (was_value_set) {
            subscription_view.retain_handling_type = (enum aws_mqtt5_retain_handling_type)retain_enum;
        }

        aws_array_list_push_back(&java_packet->topic_filters, (void *)&subscription_view);
        aws_array_list_push_back(&java_packet->jni_subscription_topic_filters, (void *)&holder);
    }
    java_packet->packet.subscriptions = (struct aws_mqtt5_subscription_view *)java_packet->topic_filters.data;

    if (s_get_user_properties_from_packet_optional(
            env,
            java_subscribe_packet,
            mqtt5_subscribe_packet_properties.subscribe_user_properties_field_id,
            s_subscribe_packet_string,
            &java_packet->packet.user_property_count,
            &java_packet->jni_user_properties_holder,
            &java_packet->jni_user_properties_struct_holder,
            &java_packet->packet.user_properties) == AWS_OP_ERR) {
        goto on_error;
    }

    return java_packet;

on_error:

    /* Clean up */
    aws_mqtt5_packet_subscribe_view_java_destroy(env, allocator, java_packet);
    return NULL;
}

struct aws_mqtt5_packet_subscribe_view *aws_mqtt5_packet_subscribe_view_get_packet(
    struct aws_mqtt5_packet_subscribe_view_java_jni *java_packet) {
    if (java_packet) {
        return &java_packet->packet;
    } else {
        return NULL;
    }
}

/*******************************************************************************
 * UNSUBSCRIBE PACKET FUNCTIONS
 ******************************************************************************/

void aws_mqtt5_packet_unsubscribe_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_packet) {
    if (!java_packet) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "id=%p: Destroying UnsubscribePacket", (void *)java_packet);

    s_cleanup_two_aws_array(
        env, &java_packet->jni_user_properties_holder, &java_packet->jni_user_properties_struct_holder);
    s_cleanup_two_aws_array(env, &java_packet->jni_topic_filters, &java_packet->topic_filters);
    aws_mem_release(allocator, java_packet);
}

struct aws_mqtt5_packet_unsubscribe_view_java_jni *aws_mqtt5_packet_unsubscribe_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_unsubscribe_packet) {

    jobject jni_topic_filters = (*env)->GetObjectField(
        env, java_unsubscribe_packet, mqtt5_unsubscribe_packet_properties.unsubscribe_subscriptions_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        return NULL;
    }
    if (!jni_topic_filters) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT,
            "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed due to no topic filters");
        return NULL;
    }

    size_t topic_filter_size = 0;
    jint jni_topic_filter_size = (*env)->CallIntMethod(env, jni_topic_filters, boxed_list_properties.list_size_id);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT,
            "UnsubscribePacket create_from_java: Created new UnsubscribePacket failed due to no topic filters");
        return NULL;
    }
    int64_t jni_topic_filter_size_check = (int64_t)jni_topic_filter_size;
    if (jni_topic_filter_size_check < 0) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribePacket create_from_java: No topic filters found");
        return NULL;
    } else {
        topic_filter_size = (size_t)jni_topic_filter_size;
    }

    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_unsubscribe_view_java_jni));
    if (java_packet == NULL) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed");
        return NULL;
    }
    int array_init = aws_array_list_init_dynamic(
        &java_packet->topic_filters, allocator, topic_filter_size, sizeof(struct aws_byte_cursor));
    if (array_init != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed");
        goto on_error;
    }
    int jni_array_init = aws_array_list_init_dynamic(
        &java_packet->jni_topic_filters,
        allocator,
        topic_filter_size,
        sizeof(struct buffer_and_cursor_array_holder_struct));
    if (jni_array_init != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed");
        goto on_error;
    }

    java_packet->packet.topic_filter_count = topic_filter_size;

    for (size_t i = 0; i < topic_filter_size; i++) {
        /* Populate */
        struct buffer_and_cursor_array_holder_struct holder;

        jstring jni_topic_filter =
            (jstring)(*env)->CallObjectMethod(env, jni_topic_filters, boxed_list_properties.list_get_id, (jint)i);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "UnsubscribePacket create_from_java: Error getting subscription topic filter");
            goto on_error;
        }
        if (jni_topic_filter) {
            // Get a temporary cursor from JNI, copy it, and then destroy the JNI version, leaving the byte_buffer copy.
            // This gets around JNI stuff going out of scope.
            struct aws_byte_cursor tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_topic_filter);
            aws_byte_buf_init_copy_from_cursor(&holder.buffer, aws_jni_get_allocator(), tmp_cursor);
            holder.cursor = aws_byte_cursor_from_buf(&holder.buffer);
            aws_jni_byte_cursor_from_jstring_release(env, jni_topic_filter, tmp_cursor);
            jni_topic_filter = NULL;
        }
        aws_array_list_push_back(&java_packet->topic_filters, (void *)&holder.cursor);
        aws_array_list_push_back(&java_packet->jni_topic_filters, (void *)&holder);
    }
    java_packet->packet.topic_filters = (struct aws_byte_cursor *)java_packet->topic_filters.data;

    if (s_get_user_properties_from_packet_optional(
            env,
            java_unsubscribe_packet,
            mqtt5_unsubscribe_packet_properties.unsubscribe_user_properties_field_id,
            s_unsubscribe_packet_string,
            &java_packet->packet.user_property_count,
            &java_packet->jni_user_properties_holder,
            &java_packet->jni_user_properties_struct_holder,
            &java_packet->packet.user_properties) == AWS_OP_ERR) {
        goto on_error;
    }

    return java_packet;

on_error:

    /* Clean up */
    aws_mqtt5_packet_unsubscribe_view_java_destroy(env, allocator, java_packet);
    return NULL;
}

struct aws_mqtt5_packet_unsubscribe_view *aws_mqtt5_packet_unsubscribe_view_get_packet(
    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_packet) {
    if (java_packet) {
        return &java_packet->packet;
    } else {
        return NULL;
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

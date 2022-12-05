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

    jstring jni_client_id;
    struct aws_byte_cursor client_id_cursor;
    jstring jni_username;
    struct aws_byte_cursor username_cursor;
    jbyteArray jni_password;
    struct aws_byte_cursor password_cursor;
    uint32_t session_expiry_interval_seconds;
    uint8_t request_response_information;
    uint8_t request_problem_information;
    uint16_t receive_maximum;
    uint16_t topic_alias_maximum;
    uint32_t maximum_packet_size_bytes;
    uint32_t will_delay_interval_seconds;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
    struct aws_mqtt5_packet_publish_view_java_jni *will_publish_packet;
};

struct aws_mqtt5_packet_disconnect_view_java_jni {
    struct aws_mqtt5_packet_disconnect_view packet;

    jstring jni_reason_string;
    struct aws_byte_cursor reason_string_cursor;
    jstring jni_server_reference;
    struct aws_byte_cursor server_reference_cursor;
    uint32_t session_expiry_interval_seconds;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct aws_mqtt5_packet_publish_view_java_jni {
    struct aws_mqtt5_packet_publish_view packet;

    jbyteArray jni_payload;
    struct aws_byte_cursor payload_cursor;
    jstring jni_topic;
    struct aws_byte_cursor topic_cursor;
    enum aws_mqtt5_payload_format_indicator payload_format;
    uint32_t message_expiry_interval_seconds;
    uint16_t topic_alias;
    jstring jni_response_topic;
    struct aws_byte_cursor response_topic_cursor;
    jbyteArray jni_correlation_data;
    struct aws_byte_cursor correlation_data_cursor;
    jstring jni_content_type;
    struct aws_byte_cursor content_type_cursor;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct aws_mqtt5_packet_subscribe_view_java_jni {
    struct aws_mqtt5_packet_subscribe_view packet;

    /* Contains aws_mqtt5_subscription_view pointers */
    struct aws_array_list topic_filters;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_subscription_topic_filters;
    uint32_t subscription_identifier;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct aws_mqtt5_packet_unsubscribe_view_java_jni {
    struct aws_mqtt5_packet_unsubscribe_view packet;

    /* Contains aws_byte_cursor pointers */
    struct aws_array_list topic_filters;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_topic_filters;
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list jni_user_properties_holder;
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list jni_user_properties_struct_holder;
};

struct jstring_array_holder_struct {
    jstring jni_string;
    struct aws_byte_cursor cursor;
};

/*******************************************************************************
 * HELPER FUNCTIONS
 ******************************************************************************/

static void s_log_and_throw_exception(JNIEnv *env, const char *message) {
    AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s", message);
    aws_jni_throw_runtime_exception(env, "%s", message);
}

static int s_populate_user_properties(
    JNIEnv *env,
    jobject jni_user_properties_list,
    size_t java_packet_native_user_property_count,
    const struct aws_mqtt5_user_property **java_packet_native_user_properties,
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list *java_packet_user_properties_holder,
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list *java_packet_user_properties_struct_holder) {

    if (jni_user_properties_list) {
        for (size_t i = 0; i < java_packet_native_user_property_count; i++) {
            jobject jni_property =
                (*env)->CallObjectMethod(env, jni_user_properties_list, boxed_list_properties.list_get_id, (jint)i);
            if (!jni_property || aws_jni_check_and_clear_exception(env)) {
                s_log_and_throw_exception(
                    env, "Could not populate user properties due to being unable to get property in list from Java");
                return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
            }

            jstring jni_property_key =
                (jstring)(*env)->GetObjectField(env, jni_property, mqtt5_user_property_properties.property_key_id);
            if (aws_jni_check_and_clear_exception(env)) {
                s_log_and_throw_exception(
                    env, "Could not populate user properties due to exception when getting property key");
                return aws_raise_error(AWS_ERROR_INVALID_STATE);
            }
            jstring jni_property_value =
                (jstring)(*env)->GetObjectField(env, jni_property, mqtt5_user_property_properties.property_value_id);
            if (aws_jni_check_and_clear_exception(env)) {
                s_log_and_throw_exception(
                    env, "Could not populate user properties due to exception when getting property value");
                return aws_raise_error(AWS_ERROR_INVALID_STATE);
            }

            struct jstring_array_holder_struct holder_property_key = {
                .jni_string = jni_property_key,
                .cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_property_key),
            };

            struct jstring_array_holder_struct holder_property_value = {
                .jni_string = jni_property_value,
                .cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_property_value),
            };
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
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list *holder_array,
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list *user_property_array,
    size_t init_entries) {
    if (aws_array_list_init_dynamic(
            holder_array, allocator, 2 * init_entries, sizeof(struct jstring_array_holder_struct)) != AWS_OP_SUCCESS ||
        aws_array_list_init_dynamic(
            user_property_array, allocator, 2 * init_entries, sizeof(struct aws_mqtt5_user_property)) !=
            AWS_OP_SUCCESS) {
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    return AWS_OP_SUCCESS;
}

static void s_cleanup_two_aws_array(
    JNIEnv *env,
    struct aws_array_list *user_properties_holder,
    struct aws_array_list *user_properties_struct_holder) {

    if (aws_array_list_is_valid(user_properties_holder)) {
        /**
         * Note that this ONLY frees the array holders for the non-struct array.
         * We want to keep the struct one in memory since it belongs to a packet or similar.
         * If both need to be freed, then we assume whomever is calling this will handle it.
         */
        for (size_t i = 0; i < aws_array_list_length(user_properties_holder); i++) {
            struct jstring_array_holder_struct holder;
            aws_array_list_get_at(user_properties_holder, &holder, i);
            aws_jni_byte_cursor_from_jstring_release(env, holder.jni_string, holder.cursor);
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
    uint16_t **destination,
    bool optional) {

    jobject jlong_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jlong_obj) {
        jlong jlong_value = (*env)->CallLongMethod(env, jlong_obj, boxed_long_properties.long_value_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int64_t jlong_value_check = (int64_t)jlong_value;
        if (jlong_value_check < 0) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is less than 0", object_name, field_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: %s is less than 0", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        } else if (jlong_value_check > UINT16_MAX) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is more than UINT16_MAX", object_name, field_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: %s is more than UINT16_MAX", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }
        *result = (uint16_t)jlong_value;
        *destination = result;

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
    uint32_t **destination,
    bool optional) {

    jobject jlong_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jlong_obj) {
        jlong jlong_value = (*env)->CallLongMethod(env, jlong_obj, boxed_long_properties.long_value_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int64_t jlong_value_check = (int64_t)jlong_value;
        if (jlong_value_check < 0) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is less than 0", object_name, field_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: %s is less than 0", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        } else if (jlong_value_check > UINT32_MAX) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is more than UINT32_MAX", object_name, field_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: %s is more than UINT32_MAX", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }
        *result = (uint32_t)jlong_value_check;
        *destination = result;
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
    uint64_t **destination,
    bool optional) {

    jobject jlong_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jlong_obj) {
        jlong jlong_value = (*env)->CallLongMethod(env, jlong_obj, boxed_long_properties.long_value_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        int64_t jlong_value_check = (int64_t)jlong_value;
        if (jlong_value_check < 0) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: %s is less than 0", object_name, field_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: %s is less than 0", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }
        *result = (uint64_t)jlong_value_check;
        *destination = result;
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
    jstring result_jstring,
    struct aws_byte_cursor *result_cursor,
    struct aws_byte_cursor **destination,
    bool is_optional) {

    jstring jstring_value = (jstring)(*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jstring_value) {
        result_jstring = jstring_value;
        *result_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, result_jstring);
        *destination = result_cursor;
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
    jbyteArray result_jbyte_array,
    struct aws_byte_cursor *result_cursor,
    struct aws_byte_cursor **destination,
    bool optional) {

    jbyteArray jbyte_array_value = (jbyteArray)(*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jbyte_array_value) {
        result_jbyte_array = jbyte_array_value;
        *result_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, result_jbyte_array);
        *destination = result_cursor;
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
    uint8_t **destination,
    bool optional) {

    jobject jboolean_obj = (*env)->GetObjectField(env, object, object_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, field_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, field_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jboolean_obj) {
        jboolean jboolean_value =
            (*env)->CallBooleanMethod(env, jboolean_obj, boxed_boolean_properties.boolean_get_value_id);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting native value from %s", object_name, field_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Error getting native value from %s", object_name, field_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *result_boolean_int = (uint8_t)jboolean_value;
        *destination = result_boolean_int;
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
    bool optional) {

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
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting %s", object_name, enum_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting %s", object_name, enum_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jni_retain_handling_type) {
        jint enum_value = (*env)->CallIntMethod(env, jni_retain_handling_type, enum_value_field);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting native value from %s", object_name, enum_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Error getting native value from %s", object_name, enum_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        if (enum_value < 0) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Native value from %s is less than 0", object_name, enum_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Native value from %s is less than 0", object_name, enum_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        } else if ((uint64_t)enum_value > UINT16_MAX) {
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT,
                "%s create_from_java: Native value from %s is more than UINT16_MAX",
                object_name,
                enum_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Native value from %s is more than UINT16_MAX", object_name, enum_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *enum_value_destination = (uint32_t)enum_value;
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
    /* Contains jstring_array_holder_struct pointers */
    struct aws_array_list *jni_user_properties_holder,
    /* Contains aws_mqtt5_user_property pointers */
    struct aws_array_list *jni_user_properties_struct_holder,
    const struct aws_mqtt5_user_property **packet_properties) {

    struct aws_allocator *allocator = aws_jni_get_allocator();

    jobject jni_list = (*env)->GetObjectField(env, packet, packet_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting user properties list", packet_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting user properties list", packet_name);
        return aws_raise_error(AWS_ERROR_INVALID_STATE);
    }
    if (jni_list) {
        jint jni_user_properties_size = (*env)->CallIntMethod(env, jni_list, boxed_list_properties.list_size_id);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting user properties list size", packet_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Error getting user properties list size", packet_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *packet_user_property_count = (size_t)jni_user_properties_size;
        if (AWS_OP_SUCCESS != s_allocate_user_properties_array_holders(
                                  allocator,
                                  jni_user_properties_holder,
                                  jni_user_properties_struct_holder,
                                  *packet_user_property_count)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(
                AWS_LS_MQTT_CLIENT, "%s create_from_java: Could not create user properties array", packet_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Could not create user properties array", packet_name);
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
    bool optional) {

    jobject jni_qos = (*env)->CallObjectMethod(env, packet, packet_field);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting QoS", packet_name);
        aws_jni_throw_runtime_exception(env, "%s create_from_java: Error getting QoS", packet_name);
        return AWS_OP_ERR;
    }
    if (jni_qos) {
        jint jni_qos_value = (*env)->CallIntMethod(env, jni_qos, mqtt5_packet_qos_properties.qos_get_value_id);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: Error getting native value from QoS", packet_name);
            aws_jni_throw_runtime_exception(
                env, "%s create_from_java: Error getting native value from QoS", packet_name);
            return aws_raise_error(AWS_ERROR_INVALID_STATE);
        }
        *packet_qos = (enum aws_mqtt5_qos)jni_qos_value;
        if (!optional) {
            return AWS_OP_SUCCESS;
        }
    } else {
        if (!optional) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s create_from_java: QoS not found", packet_name);
            aws_jni_throw_runtime_exception(env, "%s create_from_java: QoS not found", packet_name);
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

    if (java_packet->jni_client_id) {
        aws_jni_byte_cursor_from_jstring_release(env, java_packet->jni_client_id, java_packet->client_id_cursor);
    }
    if (java_packet->jni_username) {
        aws_jni_byte_cursor_from_jstring_release(env, java_packet->jni_username, java_packet->username_cursor);
    }
    if (java_packet->jni_password) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, java_packet->jni_password, java_packet->password_cursor);
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
        s_log_and_throw_exception(env, "ConnectPacket create_from_java: Creating new ConnectPacket failed");
        return NULL;
    }

    uint16_t keep_alive_interval_seconds = 0;
    uint16_t *pointer_keep_alive_interval_seconds = &java_packet->packet.keep_alive_interval_seconds;
    if (aws_get_uint16_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_keep_alive_interval_seconds_field_id,
            s_connect_packet_string,
            "keep alive interval seconds",
            &keep_alive_interval_seconds,
            &pointer_keep_alive_interval_seconds,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    java_packet->packet.keep_alive_interval_seconds = *pointer_keep_alive_interval_seconds;

    struct aws_byte_cursor *pointer_client_id_cursor = &java_packet->packet.client_id;
    if (aws_get_string_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_client_id_field_id,
            s_connect_packet_string,
            "client ID",
            java_packet->jni_client_id,
            &java_packet->client_id_cursor,
            &pointer_client_id_cursor,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    java_packet->packet.client_id = *pointer_client_id_cursor;

    if (aws_get_string_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_username_field_id,
            s_connect_packet_string,
            "username",
            java_packet->jni_username,
            &java_packet->username_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.username,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_byte_array_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_password_field_id,
            s_connect_packet_string,
            "password",
            java_packet->jni_password,
            &java_packet->password_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.password,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_session_expiry_interval_seconds_field_id,
            s_connect_packet_string,
            "session expiry interval seconds",
            &java_packet->session_expiry_interval_seconds,
            (uint32_t **)&java_packet->packet.session_expiry_interval_seconds,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_boolean_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_request_response_information_field_id,
            s_connect_packet_string,
            "request response information",
            &java_packet->request_response_information,
            (uint8_t **)&java_packet->packet.request_response_information,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_boolean_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_request_problem_information_field_id,
            s_connect_packet_string,
            "request problem information",
            &java_packet->request_problem_information,
            (uint8_t **)&java_packet->packet.request_problem_information,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_uint16_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_receive_maximum_field_id,
            s_connect_packet_string,
            "receive maximum",
            &java_packet->receive_maximum,
            (uint16_t **)&java_packet->packet.receive_maximum,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_maximum_packet_size_bytes_field_id,
            s_connect_packet_string,
            "maximum packet size",
            &java_packet->maximum_packet_size_bytes,
            (uint32_t **)&java_packet->packet.maximum_packet_size_bytes,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_connect_packet,
            mqtt5_connect_packet_properties.connect_will_delay_interval_seconds_field_id,
            s_connect_packet_string,
            "will delay interval",
            &java_packet->will_delay_interval_seconds,
            (uint32_t **)&java_packet->packet.will_delay_interval_seconds,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    jobject jni_will_packet =
        (*env)->GetObjectField(env, java_connect_packet, mqtt5_connect_packet_properties.connect_will_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_log_and_throw_exception(env, "ConnectPacket create_from_java: Error getting will packet");
        goto on_error;
    }
    if (jni_will_packet) {
        java_packet->will_publish_packet =
            aws_mqtt5_packet_publish_view_create_from_java(env, allocator, jni_will_packet);
        if (java_packet->will_publish_packet == NULL) {
            s_log_and_throw_exception(env, "ConnectPacket create_from_java: Error creating will packet!");
            goto on_error;
        }
        java_packet->packet.will = &java_packet->will_publish_packet->packet;
    }

    // if (s_get_user_properties_from_packet_optional(
    //         env,
    //         java_connect_packet,
    //         mqtt5_connect_packet_properties.connect_user_properties_field_id,
    //         s_connect_packet_string,
    //         &java_packet->packet.user_property_count,
    //         &java_packet->jni_user_properties_holder,
    //         &java_packet->jni_user_properties_struct_holder,
    //         &java_packet->packet.user_properties) == AWS_OP_ERR) {
    //     goto on_error;
    // }

    return java_packet;

on_error:

    // Clean up
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

    if (java_packet->jni_reason_string) {
        aws_jni_byte_cursor_from_jstring_release(
            env, java_packet->jni_reason_string, java_packet->reason_string_cursor);
    }
    if (java_packet->jni_server_reference) {
        aws_jni_byte_cursor_from_jstring_release(
            env, java_packet->jni_server_reference, java_packet->server_reference_cursor);
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
        s_log_and_throw_exception(env, "DisconnectPacket create_from_java: Creating new DisconnectPacket failed");
        return NULL;
    }

    uint32_t reason_code_enum = UINT32_MAX;
    if (aws_get_enum_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_get_reason_code_id,
            s_disconnect_packet_string,
            "reason code",
            mqtt5_disconnect_reason_code_properties.code_get_value_id,
            &reason_code_enum,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    if (reason_code_enum != UINT32_MAX) {
        java_packet->packet.reason_code = (enum aws_mqtt5_disconnect_reason_code)reason_code_enum;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_session_expiry_interval_seconds_field_id,
            s_disconnect_packet_string,
            "session expiry interval seconds",
            &java_packet->session_expiry_interval_seconds,
            (uint32_t **)&java_packet->packet.session_expiry_interval_seconds,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_string_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_reason_string_field_id,
            s_disconnect_packet_string,
            "reason string",
            java_packet->jni_reason_string,
            &java_packet->reason_string_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.reason_string,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_string_from_jobject(
            env,
            java_disconnect_packet,
            mqtt5_disconnect_packet_properties.disconnect_session_server_reference_field_id,
            s_disconnect_packet_string,
            "server reference",
            java_packet->jni_server_reference,
            &java_packet->server_reference_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.server_reference,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    // if (s_get_user_properties_from_packet_optional(
    //         env,
    //         java_disconnect_packet,
    //         mqtt5_disconnect_packet_properties.disconnect_user_properties_field_id,
    //         s_disconnect_packet_string,
    //         &java_packet->packet.user_property_count,
    //         &java_packet->jni_user_properties_holder,
    //         &java_packet->jni_user_properties_struct_holder,
    //         &java_packet->packet.user_properties) == AWS_OP_ERR) {
    //     goto on_error;
    // }

    return java_packet;

on_error:

    // Clean up
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

    if (java_packet->jni_payload) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, java_packet->jni_payload, java_packet->payload_cursor);
    }
    if (java_packet->jni_topic) {
        aws_jni_byte_cursor_from_jstring_release(env, java_packet->jni_topic, java_packet->topic_cursor);
    }
    if (java_packet->jni_response_topic) {
        aws_jni_byte_cursor_from_jstring_release(
            env, java_packet->jni_response_topic, java_packet->response_topic_cursor);
    }
    if (java_packet->jni_correlation_data) {
        aws_jni_byte_cursor_from_jbyteArray_release(
            env, java_packet->jni_correlation_data, java_packet->correlation_data_cursor);
    }
    if (java_packet->jni_content_type) {
        aws_jni_byte_cursor_from_jstring_release(env, java_packet->jni_content_type, java_packet->content_type_cursor);
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
        s_log_and_throw_exception(env, "PublishPacket create_from_java: Creating new PublishPacket failed");
        return NULL;
    }

    struct aws_byte_cursor *pointer_payload = &java_packet->packet.payload;
    if (aws_get_byte_array_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_payload_field_id,
            s_publish_packet_string,
            "payload",
            java_packet->jni_payload,
            &java_packet->payload_cursor,
            &pointer_payload,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    java_packet->packet.payload = *pointer_payload;

    if (s_get_qos_from_packet(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_get_qos_id,
            s_publish_packet_string,
            &java_packet->packet.qos,
            false) == AWS_OP_ERR) {
        s_log_and_throw_exception(env, "PublishPacket create_from_java: QOS not found");
        goto on_error;
    }

    uint8_t packet_retain = UINT8_MAX;
    uint8_t *pointer_packet_retain = &packet_retain;
    if (aws_get_boolean_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_retain_field_id,
            s_publish_packet_string,
            "retain",
            &packet_retain,
            &pointer_packet_retain,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    if (packet_retain != UINT8_MAX) {
        java_packet->packet.retain = (bool)packet_retain;
    }

    struct aws_byte_cursor *pointer_topic = &java_packet->packet.topic;
    if (aws_get_string_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_topic_field_id,
            s_publish_packet_string,
            "topic",
            java_packet->jni_topic,
            &java_packet->topic_cursor,
            &pointer_topic,
            false) == AWS_OP_ERR) {
        s_log_and_throw_exception(env, "PublishPacket create_from_java: No topic found");
        goto on_error;
    }
    java_packet->packet.topic = *pointer_topic;

    uint32_t format_enum = UINT32_MAX;
    if (aws_get_enum_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_get_payload_format_id,
            s_publish_packet_string,
            "payload format",
            mqtt5_payload_format_indicator_properties.format_get_value_id,
            &format_enum,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    if (format_enum != UINT32_MAX) {
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
            (uint32_t **)&java_packet->packet.message_expiry_interval_seconds,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_string_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_response_topic_field_id,
            s_publish_packet_string,
            "response topic",
            java_packet->jni_response_topic,
            &java_packet->response_topic_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.response_topic,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_byte_array_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_correlation_data_field_id,
            s_publish_packet_string,
            "correlation data",
            java_packet->jni_correlation_data,
            &java_packet->correlation_data_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.correlation_data,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    if (aws_get_string_from_jobject(
            env,
            java_publish_packet,
            mqtt5_publish_packet_properties.publish_content_type_field_id,
            s_publish_packet_string,
            "content type",
            java_packet->jni_content_type,
            &java_packet->content_type_cursor,
            (struct aws_byte_cursor **)&java_packet->packet.content_type,
            true) == AWS_OP_ERR) {
        goto on_error;
    }

    // if (s_get_user_properties_from_packet_optional(
    //         env,
    //         java_publish_packet,
    //         mqtt5_publish_packet_properties.publish_user_properties_field_id,
    //         s_publish_packet_string,
    //         &java_packet->packet.user_property_count,
    //         &java_packet->jni_user_properties_holder,
    //         &java_packet->jni_user_properties_struct_holder,
    //         &java_packet->packet.user_properties) == AWS_OP_ERR) {
    //     goto on_error;
    // }

    return java_packet;

on_error:

    // Clean up
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
        s_log_and_throw_exception(
            env, "SubscribePacket create_from_java: Creating new SubscribePacket failed due to no subscriptions!");
        return NULL;
    }
    jint jni_subscriptions_size = (*env)->CallIntMethod(env, jni_subscriptions, boxed_list_properties.list_size_id);
    if (aws_jni_check_and_clear_exception(env)) {
        return NULL;
    }
    size_t subscriptions_filter_size = (size_t)jni_subscriptions_size;

    if (subscriptions_filter_size <= 0) {
        s_log_and_throw_exception(env, "SubscribePacket create_from_java: subscriptions count is 0");
        return NULL;
    }

    struct aws_mqtt5_packet_subscribe_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_subscribe_view_java_jni));
    if (java_packet == NULL) {
        s_log_and_throw_exception(env, "SubscribePacket create_from_java: Creating new SubscribePacket failed");
        return NULL;
    }
    int array_init = aws_array_list_init_dynamic(
        &java_packet->topic_filters, allocator, subscriptions_filter_size, sizeof(struct aws_mqtt5_subscription_view));
    if (array_init != AWS_OP_SUCCESS) {
        s_log_and_throw_exception(env, "SubscribePacket create_from_java: Creating new SubscribePacket failed");
        goto on_error;
    }
    int jni_array_init = aws_array_list_init_dynamic(
        &java_packet->jni_subscription_topic_filters,
        allocator,
        subscriptions_filter_size,
        sizeof(struct jstring_array_holder_struct));
    if (jni_array_init != AWS_OP_SUCCESS) {
        s_log_and_throw_exception(env, "SubscribePacket create_from_java: Creating new SubscribePacket failed");
        goto on_error;
    }

    if (aws_get_uint32_from_jobject(
            env,
            java_subscribe_packet,
            mqtt5_subscribe_packet_properties.subscribe_subscription_identifier_field_id,
            s_subscribe_packet_string,
            "subscription identifier",
            &java_packet->subscription_identifier,
            (uint32_t **)&java_packet->packet.subscription_identifier,
            true) == AWS_OP_ERR) {
        goto on_error;
    }
    java_packet->packet.subscription_count = subscriptions_filter_size;

    for (size_t i = 0; i < subscriptions_filter_size; i++) {
        // Populate
        struct aws_mqtt5_subscription_view subscription_view;
        struct jstring_array_holder_struct holder;

        jobject jni_packet_subscribe_subscription =
            (*env)->CallObjectMethod(env, jni_subscriptions, boxed_list_properties.list_get_id, (jint)i);
        if (aws_jni_check_and_clear_exception(env)) {
            s_log_and_throw_exception(env, "SubscribePacket create_from_java: Error getting topic filters");
            goto on_error;
        }

        jstring jni_topic_filter = (jstring)(*env)->CallObjectMethod(
            env, jni_packet_subscribe_subscription, mqtt5_subscription_properties.subscribe_get_topic_filter_id);
        if (aws_jni_check_and_clear_exception(env)) {
            s_log_and_throw_exception(env, "SubscribePacket create_from_java: Error getting subscription topic filter");
            goto on_error;
        }
        if (jni_topic_filter) {
            holder.jni_string = jni_topic_filter;
            holder.cursor = aws_jni_byte_cursor_from_jstring_acquire(env, holder.jni_string);
            subscription_view.topic_filter = holder.cursor;
        } else {
            s_log_and_throw_exception(env, "SubscribePacket create_from_java: subscription topic filter is required");
            goto on_error;
        }

        if (s_get_qos_from_packet(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_get_qos_id,
                s_subscribe_packet_string,
                &subscription_view.qos,
                false) == AWS_OP_ERR) {
            s_log_and_throw_exception(env, "SubscribePacket create_from_java: subscription QoS is required");
            goto on_error;
        }

        uint8_t subscription_no_local = UINT8_MAX;
        uint8_t *pointer_subscription_no_local = &subscription_no_local;
        if (aws_get_boolean_from_jobject(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_no_local_field_id,
                s_subscribe_packet_string,
                "no local",
                &subscription_no_local,
                &pointer_subscription_no_local,
                true) != AWS_OP_SUCCESS) {
            goto on_error;
        }
        if (subscription_no_local != UINT8_MAX) {
            subscription_view.no_local = (bool)subscription_no_local;
        }

        uint8_t retain_as_published = UINT8_MAX;
        uint8_t *pointer_retain_as_published = &retain_as_published;
        if (aws_get_boolean_from_jobject(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_retain_as_published_field_id,
                s_subscribe_packet_string,
                "no local",
                &retain_as_published,
                &pointer_retain_as_published,
                true) != AWS_OP_SUCCESS) {
            goto on_error;
        }
        if (retain_as_published != UINT8_MAX) {
            subscription_view.retain_as_published = (bool)retain_as_published;
        }

        uint32_t retain_enum = UINT32_MAX;
        if (aws_get_enum_from_jobject(
                env,
                jni_packet_subscribe_subscription,
                mqtt5_subscription_properties.subscribe_get_retain_handling_type_id,
                s_subscribe_packet_string,
                "subscription retain handling type",
                mqtt5_retain_handling_type_properties.retain_get_value_id,
                &retain_enum,
                true) == AWS_OP_ERR) {
            goto on_error;
        }
        if (retain_enum != UINT32_MAX) {
            subscription_view.retain_handling_type = (enum aws_mqtt5_retain_handling_type)retain_enum;
        }

        aws_array_list_push_back(&java_packet->topic_filters, (void *)&subscription_view);
        aws_array_list_push_back(&java_packet->jni_subscription_topic_filters, (void *)&holder);
    }
    java_packet->packet.subscriptions = (struct aws_mqtt5_subscription_view *)java_packet->topic_filters.data;

    // if (s_get_user_properties_from_packet_optional(
    //         env,
    //         java_subscribe_packet,
    //         mqtt5_subscribe_packet_properties.subscribe_user_properties_field_id,
    //         s_subscribe_packet_string,
    //         &java_packet->packet.user_property_count,
    //         &java_packet->jni_user_properties_holder,
    //         &java_packet->jni_user_properties_struct_holder,
    //         &java_packet->packet.user_properties) == AWS_OP_ERR) {
    //     goto on_error;
    // }

    return java_packet;

on_error:

    // Clean up
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
        s_log_and_throw_exception(
            env, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed due to no topic filters");
        return NULL;
    }

    size_t topic_filter_size = 0;
    jint jni_topic_filter_size = (*env)->CallIntMethod(env, jni_topic_filters, boxed_list_properties.list_size_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_log_and_throw_exception(
            env, "UnsubscribePacket create_from_java: Created new UnsubscribePacket failed due to no topic filters");
        return NULL;
    }
    int64_t jni_topic_filter_size_check = (int64_t)jni_topic_filter_size;
    if (jni_topic_filter_size_check < 0) {
        s_log_and_throw_exception(env, "UnsubscribePacket create_from_java: No topic filters found");
        return NULL;
    } else {
        topic_filter_size = (size_t)jni_topic_filter_size;
    }

    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_packet =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_packet_unsubscribe_view_java_jni));
    if (java_packet == NULL) {
        s_log_and_throw_exception(env, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed");
        return NULL;
    }
    int array_init = aws_array_list_init_dynamic(
        &java_packet->topic_filters, allocator, topic_filter_size, sizeof(struct aws_byte_cursor));
    if (array_init != AWS_OP_SUCCESS) {
        s_log_and_throw_exception(env, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed");
        goto on_error;
    }
    int jni_array_init = aws_array_list_init_dynamic(
        &java_packet->jni_topic_filters, allocator, topic_filter_size, sizeof(struct jstring_array_holder_struct));
    if (jni_array_init != AWS_OP_SUCCESS) {
        s_log_and_throw_exception(env, "UnsubscribePacket create_from_java: Creating new UnsubscribePacket failed");
        goto on_error;
    }

    java_packet->packet.topic_filter_count = topic_filter_size;

    for (size_t i = 0; i < topic_filter_size; i++) {
        // Populate
        struct jstring_array_holder_struct holder;

        jstring jni_topic_filter =
            (jstring)(*env)->CallObjectMethod(env, jni_topic_filters, boxed_list_properties.list_get_id, (jint)i);
        if (aws_jni_check_and_clear_exception(env)) {
            s_log_and_throw_exception(
                env, "UnsubscribePacket create_from_java: Error getting subscription topic filter");
            goto on_error;
        }
        if (jni_topic_filter) {
            holder.jni_string = jni_topic_filter;
            holder.cursor = aws_jni_byte_cursor_from_jstring_acquire(env, holder.jni_string);
        }
        aws_array_list_push_back(&java_packet->topic_filters, (void *)&holder.cursor);
        aws_array_list_push_back(&java_packet->jni_topic_filters, (void *)&holder);
    }
    java_packet->packet.topic_filters = (struct aws_byte_cursor *)java_packet->topic_filters.data;

    // if (s_get_user_properties_from_packet_optional(
    //         env,
    //         java_unsubscribe_packet,
    //         mqtt5_unsubscribe_packet_properties.unsubscribe_user_properties_field_id,
    //         s_unsubscribe_packet_string,
    //         &java_packet->packet.user_property_count,
    //         &java_packet->jni_user_properties_holder,
    //         &java_packet->jni_user_properties_struct_holder,
    //         &java_packet->packet.user_properties) == AWS_OP_ERR) {
    //     goto on_error;
    // }

    return java_packet;

on_error:

    // Clean up
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

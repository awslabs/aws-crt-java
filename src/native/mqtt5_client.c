/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <aws/mqtt/v5/mqtt5_client.h>

#include <aws/http/proxy.h>
#include <aws/io/event_loop.h>
#include <aws/io/socket.h>
#include <aws/io/tls_channel_handler.h>

#include <crt.h>
#include <http_request_utils.h>
#include <java_class_ids.h>
#include <jni.h>
#include <mqtt5_packets.h>

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

struct aws_mqtt5_client_java_jni {
    struct aws_mqtt5_client *client;
    jobject jni_client;
    JavaVM *jvm;

    struct aws_tls_connection_options tls_options;
    struct aws_tls_connection_options http_proxy_tls_options;

    jobject jni_publish_events;
    jobject jni_lifecycle_events;
};

struct aws_mqtt5_client_publish_return_data {
    struct aws_mqtt5_client_java_jni *java_client;
    jobject jni_publish_future;
};

struct aws_mqtt5_client_subscribe_return_data {
    struct aws_mqtt5_client_java_jni *java_client;
    jobject jni_subscribe_future;
};

struct aws_mqtt5_client_unsubscribe_return_data {
    struct aws_mqtt5_client_java_jni *java_client;
    jobject jni_unsubscribe_future;
};

struct aws_http_proxy_options_java_jni {
    struct aws_http_proxy_options options;

    jstring *jni_proxy_host;
    struct aws_byte_cursor proxy_host_cursor;
    jstring *jni_proxy_authorization_username;
    struct aws_byte_cursor authorization_username_cursor;
    jstring *jni_proxy_authorization_password;
    struct aws_byte_cursor authorization_password_cursor;
};

/*******************************************************************************
 * HELPER FUNCTION (LOGGING)
 ******************************************************************************/

static void s_aws_mqtt5_client_log_and_throw_exception(JNIEnv *env, const char *message, int error_code) {
    AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "%s - error code: %i", message, error_code);
    aws_jni_throw_runtime_exception(env, "%s - error code: %i", message, error_code);
}

/*******************************************************************************
 * HTTP PROXY FUNCTIONS
 ******************************************************************************/

static void s_aws_mqtt5_http_proxy_options_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_http_proxy_options_java_jni *http_options) {
    if (!http_options) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "id=%p: Destroying JavaHttpProxyOptions", (void *)http_options);

    if (http_options->jni_proxy_host) {
        aws_jni_byte_cursor_from_jstring_release(env, *http_options->jni_proxy_host, http_options->proxy_host_cursor);
    }
    if (http_options->jni_proxy_authorization_username) {
        aws_jni_byte_cursor_from_jstring_release(
            env, *http_options->jni_proxy_authorization_username, http_options->authorization_username_cursor);
    }
    if (http_options->jni_proxy_authorization_password) {
        aws_jni_byte_cursor_from_jstring_release(
            env, *http_options->jni_proxy_authorization_password, http_options->authorization_password_cursor);
    }

    /* Frees all allocated memory */
    aws_mem_release(allocator, http_options);
}

static struct aws_http_proxy_options_java_jni *s_aws_mqtt5_http_proxy_options_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_client_java_jni *java_client,
    jobject java_http_proxy_options) {
    /* Cannot fail */
    struct aws_http_proxy_options_java_jni *http_options =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_http_proxy_options_java_jni));
    AWS_LOGF_DEBUG(
        AWS_LS_MQTT_CLIENT, "JavaHttpProxyOptions=%p: Created new JavaHttpProxyOptions", (void *)http_options);

    jobject jni_proxy_connection_type = (*env)->CallObjectMethod(
        env, java_http_proxy_options, http_proxy_options_properties.proxy_get_connection_type_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_connection_type) {
        jint jni_proxy_connection_type_value = (*env)->CallIntMethod(
            env, jni_proxy_connection_type, http_proxy_connection_type_properties.proxy_get_value_id);
        if (aws_jni_check_and_clear_exception(env)) {
            goto on_error;
        }
        if (jni_proxy_connection_type_value) {
            int64_t jni_proxy_connection_type_value_check = (int64_t)jni_proxy_connection_type_value;
            if (jni_proxy_connection_type_value_check < 0) {
                s_aws_mqtt5_client_log_and_throw_exception(
                    env, "HTTP Proxy Options connection type is less than 0", AWS_ERROR_INVALID_ARGUMENT);
                goto on_error;
            } else if (jni_proxy_connection_type_value_check > AWS_HPCT_HTTP_TUNNEL) { /* The (current) maximum enum */
                                                                                       /* value */
                s_aws_mqtt5_client_log_and_throw_exception(
                    env,
                    "HTTP Proxy Options connection type is more than maximum allowed value",
                    AWS_ERROR_INVALID_ARGUMENT);
                goto on_error;
            } else {
                http_options->options.connection_type =
                    (enum aws_http_proxy_connection_type)jni_proxy_connection_type_value;
            }
        }
    }

    jstring jni_proxy_host = (jstring)(*env)->CallObjectMethod(
        env, java_http_proxy_options, http_proxy_options_properties.proxy_get_proxy_host_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_host) {
        http_options->jni_proxy_host = &jni_proxy_host;
        http_options->proxy_host_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, *http_options->jni_proxy_host);
        http_options->options.host = http_options->proxy_host_cursor;
    }

    jint jni_proxy_port =
        (*env)->CallIntMethod(env, java_http_proxy_options, http_proxy_options_properties.proxy_get_proxy_port_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_port) {
        int64_t jni_proxy_port_check = (int64_t)jni_proxy_port;
        if (jni_proxy_port_check < 0) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "HTTP Proxy Options port is less than 0", AWS_ERROR_INVALID_ARGUMENT);
            goto on_error;
        } else if (jni_proxy_port_check > UINT16_MAX) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "HTTP Proxy Options port is more than UINT16_MAX", AWS_ERROR_INVALID_ARGUMENT);
            goto on_error;
        } else {
            http_options->options.port = (uint16_t)jni_proxy_port;
        }
    }

    jobject jni_proxy_tls_context = (*env)->CallObjectMethod(
        env, java_http_proxy_options, http_proxy_options_properties.proxy_get_proxy_tls_context_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_tls_context) {
        jlong jni_proxy_tls_context_long =
            (*env)->CallLongMethod(env, jni_proxy_tls_context, crt_resource_properties.get_native_handle_method_id);

        struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_proxy_tls_context_long;
        if (tls_ctx) {
            aws_tls_connection_options_init_from_ctx(&java_client->http_proxy_tls_options, tls_ctx);
            aws_tls_connection_options_set_server_name(
                &java_client->http_proxy_tls_options, allocator, &http_options->options.host);
            http_options->options.tls_options = &java_client->http_proxy_tls_options;
        }
    }

    jobject jni_proxy_authorization_type = (*env)->CallObjectMethod(
        env, java_http_proxy_options, http_proxy_options_properties.proxy_get_proxy_authorization_type_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_authorization_type) {
        jint jni_proxy_authorization_type_value = (*env)->CallIntMethod(
            env, jni_proxy_authorization_type, http_proxy_connection_type_properties.proxy_get_value_id);
        if (aws_jni_check_and_clear_exception(env)) {
            goto on_error;
        }
        http_options->options.auth_type = (enum aws_http_proxy_authentication_type)jni_proxy_authorization_type_value;
    }

    jstring jni_proxy_authorization_username = (jstring)(*env)->CallObjectMethod(
        env, java_http_proxy_options, http_proxy_options_properties.proxy_get_authorization_username_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_authorization_username) {
        http_options->jni_proxy_authorization_username = &jni_proxy_authorization_username;
        http_options->authorization_username_cursor =
            aws_jni_byte_cursor_from_jstring_acquire(env, *http_options->jni_proxy_authorization_username);
        http_options->options.auth_username = http_options->authorization_username_cursor;
    }

    jstring jni_proxy_authorization_password = (jstring)(*env)->CallObjectMethod(
        env, java_http_proxy_options, http_proxy_options_properties.proxy_get_authorization_password_id);
    if (aws_jni_check_and_clear_exception(env)) {
        goto on_error;
    }
    if (jni_proxy_authorization_password) {
        http_options->jni_proxy_authorization_password = &jni_proxy_authorization_password;
        http_options->authorization_password_cursor =
            aws_jni_byte_cursor_from_jstring_acquire(env, *http_options->jni_proxy_authorization_password);
        http_options->options.auth_password = http_options->authorization_password_cursor;
    }

    return http_options;

on_error:
    s_aws_mqtt5_http_proxy_options_java_destroy(env, allocator, http_options);
    return NULL;
}

/*******************************************************************************
 * HELPER FUNCTIONS
 ******************************************************************************/

static void aws_mqtt5_client_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_client_java_jni *java_client) {
    AWS_PRECONDITION(java_client);
    if (!java_client) {
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "java_client=%p: Destroying MQTT5 client", (void *)java_client);

    if (java_client->jni_client) {
        (*env)->DeleteGlobalRef(env, java_client->jni_client);
    }
    if (java_client->jni_publish_events) {
        (*env)->DeleteGlobalRef(env, java_client->jni_publish_events);
    }
    if (java_client->jni_lifecycle_events) {
        (*env)->DeleteGlobalRef(env, java_client->jni_lifecycle_events);
    }

    aws_tls_connection_options_clean_up(&java_client->tls_options);
    aws_tls_connection_options_clean_up(&java_client->http_proxy_tls_options);

    /* Frees allocated memory */
    aws_mem_release(allocator, java_client);
}

static int s_set_jni_uint64_t_field_in_packet(
    JNIEnv *env,
    const uint64_t *native_integer,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {

    if (native_integer != NULL) {
        jobject jni_int = (*env)->NewObject(
            env, boxed_long_properties.long_class, boxed_long_properties.constructor, (jlong)*native_integer);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create uint64 field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not create uint64 field %s", field_name);
            return AWS_OP_ERR;
        }
        (*env)->SetObjectField(env, packet, field_id, jni_int);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set uint64 field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not set uint64 field %s", field_name);
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

static int s_set_jni_uint32_t_field_in_packet(
    JNIEnv *env,
    const uint32_t *native_integer,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional) {

    if (native_integer != NULL) {
        jobject jni_int = (*env)->NewObject(
            env, boxed_long_properties.long_class, boxed_long_properties.constructor, (jlong)*native_integer);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create uint32 field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not create uint32 field %s", field_name);
            return AWS_OP_ERR;
        }
        (*env)->SetObjectField(env, packet, field_id, jni_int);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set uint32 field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not set uint32 field %s", field_name);
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

static int s_set_jni_uint16_t_field_in_packet(
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
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create uint16 field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not create uint16 field %s", field_name);
            return AWS_OP_ERR;
        }
        (*env)->SetObjectField(env, packet, field_id, jni_int);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set uint16 field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not set uint16 field %s", field_name);
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

static int s_set_jni_bool_field_in_packet(
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
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create boolean field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not create boolean field %s", field_name);
            return AWS_OP_ERR;
        }

        (*env)->SetObjectField(env, packet, field_id, jni_boolean);
        if (aws_jni_check_and_clear_exception(env)) {
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set boolean field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not set boolean field %s", field_name);
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

static int s_set_jni_string_field_in_packet(
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
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set string field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not set string field %s", field_name);
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

static int s_set_jni_byte_array_field_in_packet(
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
            /* Due to formatted string, easier to just call directly than use s_log_and_throw_exception */
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not set string field %s", field_name);
            aws_jni_throw_runtime_exception(env, "Could not set string field %s", field_name);
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

static int s_set_user_properties_field(
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
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "Could not make new user properties list", AWS_ERROR_INVALID_STATE);
            return AWS_OP_ERR;
        }

        (*env)->SetObjectField(env, packet, user_property_field_id, jni_user_properties_list);
        if (aws_jni_check_and_clear_exception(env)) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "Could not make set new user properties list", AWS_ERROR_INVALID_STATE);
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
                s_aws_mqtt5_client_log_and_throw_exception(
                    env, "Could not make new user property", AWS_ERROR_INVALID_STATE);
                return AWS_OP_ERR;
            }

            jboolean jni_add_result = (*env)->CallBooleanMethod(
                env, jni_user_properties_list, boxed_list_properties.list_add_id, jni_new_property);
            if (aws_jni_check_and_clear_exception(env)) {
                s_aws_mqtt5_client_log_and_throw_exception(
                    env, "Could not add new user property", AWS_ERROR_INVALID_STATE);
                return AWS_OP_ERR;
            }

            if ((bool)jni_add_result == false) {
                return AWS_OP_ERR;
            }
        }
    }
    return AWS_OP_SUCCESS;
}

static int s_set_int_enum_in_packet(
    JNIEnv *env,
    const int *int_enum,
    jobject packet,
    jmethodID set_enum_field_id,
    bool optional) {

    if (int_enum) {
        if (*int_enum < 0) {
            return AWS_OP_ERR;
        }
        (*env)->CallIntMethod(env, packet, set_enum_field_id, (jint)*int_enum);
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

static jobject s_aws_mqtt5_client_create_jni_connack_packet_from_native(
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
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Error when creating ConnAckPacket from native: Reason code is negative!", AWS_ERROR_INVALID_STATE);
        return NULL;
    }
    (*env)->CallIntMethod(
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
            s_aws_mqtt5_client_log_and_throw_exception(
                env,
                "Error when creating ConnAckPacket from native: Could not set maximum QOS",
                AWS_ERROR_INVALID_STATE);
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
        s_aws_mqtt5_client_log_and_throw_exception(
            env,
            "Error when creating ConnAckPacket from native: could not add user property!",
            AWS_ERROR_INVALID_STATE);
        return NULL;
    }

    return connack_data;
}

static jobject s_aws_mqtt5_client_create_jni_disconnect_packet_from_native(
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
        s_aws_mqtt5_client_log_and_throw_exception(
            env,
            "Error when creating DisconnectPacket from native: Could not set reason code",
            AWS_ERROR_INVALID_STATE);
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

static jobject s_aws_mqtt5_client_create_jni_negotiated_settings_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_negotiated_settings *native_negotiated_settings_data) {

    jobject negotiated_settings_data = (*env)->NewObject(
        env,
        mqtt5_negotiated_settings_properties.negotiated_settings_class,
        mqtt5_negotiated_settings_properties.negotiated_settings_constructor_id);

    (*env)->CallObjectMethod(
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

static jobject s_aws_mqtt5_client_create_jni_puback_packet_from_native(
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
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Error when creating PubAck result from native: Could not set reason code", AWS_ERROR_INVALID_STATE);
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

static jobject s_aws_mqtt5_client_create_jni_publish_packet_from_native(
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
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Error when creating PublishPacket from native: Could not set QOS", AWS_ERROR_INVALID_STATE);
        return NULL;
    }

    if (s_set_jni_bool_field_in_packet(
            env,
            &publish->retain,
            publish_packet_data,
            mqtt5_publish_packet_properties.publish_retain_field_id,
            "retain",
            false) != AWS_OP_SUCCESS) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Error when creating PublishPacket from native: Could not set retain", AWS_ERROR_INVALID_STATE);
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
            s_aws_mqtt5_client_log_and_throw_exception(
                env,
                "Error when creating PublishPacket from native: Could not set payload format",
                AWS_ERROR_INVALID_STATE);
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

static void s_complete_future_with_exception(JNIEnv *env, jobject future, int error_code) {
    if (!env || !future) {
        return;
    }

    jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, error_code);
    (*env)->CallBooleanMethod(
        env, future, completable_future_properties.complete_exceptionally_method_id, crt_exception);
    aws_jni_check_and_clear_exception(env);
    (*env)->DeleteLocalRef(env, crt_exception);
}

static void s_aws_count_allocation(const void *pointer, size_t *counter) {
    if (pointer != NULL) {
        *counter += 1;
    }
}

static char s_client_string[] = "MQTT5 Client";

/*******************************************************************************
 * MQTT5 CALLBACK FUNCTIONS
 ******************************************************************************/

static void s_aws_mqtt5_client_java_lifecycle_event(const struct aws_mqtt5_client_lifecycle_event *event) {

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)event->user_data;
    if (!java_client) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "LifecycleEvent: invalid client");
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = java_client->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "LifecycleEvent: could not get env");
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
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "LifecycleEvent: could not push local JNI frame with 14 allocation minimum!", AWS_ERROR_INVALID_STATE);
        aws_jni_release_thread_env(jvm, env);
        return;
    }

    jobject connack_data = NULL;
    if (event->connack_data != NULL) {
        connack_data = s_aws_mqtt5_client_create_jni_connack_packet_from_native(env, event->connack_data);
        if (connack_data == NULL) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "LifecycleEvent: creating ConnAck packet failed!", AWS_ERROR_INVALID_STATE);
            goto clean_up;
        }
    }

    jobject disconnect_data = NULL;
    if (event->disconnect_data != NULL) {
        disconnect_data = s_aws_mqtt5_client_create_jni_disconnect_packet_from_native(env, event->disconnect_data);
        if (disconnect_data == NULL) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "LifecycleEvent: creating Disconnect packet failed!", AWS_ERROR_INVALID_STATE);
            goto clean_up;
        }
    }

    jobject negotiated_settings_data = NULL;
    if (event->settings != NULL) {
        negotiated_settings_data = s_aws_mqtt5_client_create_jni_negotiated_settings_from_native(env, event->settings);
    }

    jobject jni_lifecycle_events = java_client->jni_lifecycle_events;
    if (!jni_lifecycle_events) {
        s_aws_mqtt5_client_log_and_throw_exception(
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
                java_client->jni_client,
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
                env, java_client->jni_client, mqtt5_client_properties.client_set_is_connected, true);

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_connection_success_id,
                java_client->jni_client,
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
                java_client->jni_client,
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
                env, java_client->jni_client, mqtt5_client_properties.client_set_is_connected, false);

            (*env)->CallObjectMethod(
                env,
                jni_lifecycle_events,
                mqtt5_lifecycle_events_properties.lifecycle_disconnection_id,
                java_client->jni_client,
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
                java_client->jni_client,
                java_lifecycle_return_data);
            break;
        default:
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "LifecycleEvent: unsupported event type: %i", event->event_type);
    }

    goto clean_up;

clean_up:

    aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    (*env)->PopLocalFrame(env, NULL);
    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

static void s_aws_mqtt5_client_java_publish_received(
    const struct aws_mqtt5_packet_publish_view *publish,
    void *user_data) {

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)user_data;
    if (!java_client) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "publishReceived function: invalid client");
        return;
    }

    if (!publish) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "publishReceived function: invalid publish packet");
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = java_client->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "publishReceived function: could not get env");
        return;
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
        s_aws_mqtt5_client_log_and_throw_exception(
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

    if (java_client->jni_publish_events) {
        (*env)->CallObjectMethod(
            env,
            java_client->jni_publish_events,
            mqtt5_publish_events_properties.publish_events_publish_received_id,
            java_client->jni_client,
            publish_packet_return_data);
        aws_jni_check_and_clear_exception(env); /* To hide JNI warning */
    }
    goto clean_up;

clean_up:

    (*env)->PopLocalFrame(env, NULL);
    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

static void s_aws_mqtt5_client_java_publish_callback_destructor(
    JNIEnv *env,
    struct aws_mqtt5_client_publish_return_data *callback_return_data) {
    struct aws_allocator *allocator = aws_jni_get_allocator();

    if (callback_return_data != NULL) {
        if (callback_return_data->jni_publish_future && env != NULL) {
            (*env)->DeleteGlobalRef(env, callback_return_data->jni_publish_future);
        }
        aws_mem_release(allocator, callback_return_data);
    }
}

static void s_aws_mqtt5_client_java_publish_completion(
    enum aws_mqtt5_packet_type packet_type,
    const void *packet,
    int error_code,
    void *user_data) {

    int exception_error_code = error_code;
    JavaVM *jvm = NULL;
    JNIEnv *env = NULL;

    struct aws_mqtt5_client_publish_return_data *return_data = (struct aws_mqtt5_client_publish_return_data *)user_data;
    if (!return_data) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishCompletion function: invalid return data!");
        return;
    }

    struct aws_mqtt5_client_java_jni *java_client = return_data->java_client;
    if (!java_client) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishCompletion function: invalid client");
        goto clean_up;
    }

    /********** JNI ENV ACQUIRE **********/
    jvm = java_client->jvm;
    env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishCompletion function: could not get env");
        goto clean_up;
    }

    /* Get the future for this specific publish and complete right away if there is an error */
    jobject jni_publish_future = return_data->jni_publish_future;
    if (error_code != AWS_OP_SUCCESS) {
        goto exception;
    }

    /* If this result is supposed to have a packet and does not, then error right away */
    if (packet_type != AWS_MQTT5_PT_NONE && packet == NULL) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishCompletion function: packet type but no packet!");
        goto exception;
    }

    /* Calculate the number of references needed */
    size_t references_needed = 0;
    if (packet_type == AWS_MQTT5_PT_PUBACK) {
        struct aws_mqtt5_packet_puback_view *puback_packet = (struct aws_mqtt5_packet_puback_view *)packet;
        /* A PubAck packet will need 2 references at minimum */
        references_needed += 2;
        /* Optionals */
        s_aws_count_allocation(puback_packet->reason_string, &references_needed);
        /* Add user properties */
        references_needed += puback_packet->user_property_count * 2;
        references_needed += 1; /* Add 1 for array to hold user properties */
    }

    /**
     * Push a new local frame so any local references we make are tied to it. Then we can pop it to free memory.
     */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)references_needed);
    if (local_frame_result != 0) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT,
            "PublishCompletion function: could not push local JNI frame with 12 allocation minimum");
        exception_error_code = AWS_ERROR_INVALID_STATE;
        goto exception;
    }

    /* The result */
    jobject publish_packet_result_data;

    if (packet_type == AWS_MQTT5_PT_NONE) {
        /* QoS 0 */
        publish_packet_result_data = (*env)->NewObject(
            env, mqtt5_publish_result_properties.result_class, mqtt5_publish_result_properties.result_constructor_id);
        aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    } else if (packet_type == AWS_MQTT5_PT_PUBACK) {
        /* QoS 1 */
        struct aws_mqtt5_packet_puback_view *puback_packet = (struct aws_mqtt5_packet_puback_view *)packet;

        /* Make the PubAck packet */
        jobject puback_packet_data = s_aws_mqtt5_client_create_jni_puback_packet_from_native(env, puback_packet);
        if (puback_packet_data == NULL) {
            exception_error_code = AWS_ERROR_INVALID_STATE;
            goto exception;
        }

        /* Make the result and populate it with data made above */
        publish_packet_result_data = (*env)->NewObject(
            env,
            mqtt5_publish_result_properties.result_class,
            mqtt5_publish_result_properties.result_puback_constructor_id,
            puback_packet_data);
        aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    } else {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishCompletion function called with unknown packet type!");
        exception_error_code = AWS_ERROR_INVALID_STATE;
        goto exception;
    }

    /* Complete the future */
    (*env)->CallBooleanMethod(
        env, jni_publish_future, completable_future_properties.complete_method_id, publish_packet_result_data);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "PublishCompletion function: exception when completing future");
        goto exception;
    }

    goto clean_up;

exception:
    s_complete_future_with_exception(env, jni_publish_future, exception_error_code);
    goto clean_up;

clean_up:
    s_aws_mqtt5_client_java_publish_callback_destructor(env, return_data);
    if (env != NULL) {
        (*env)->PopLocalFrame(env, NULL);
        /********** JNI ENV RELEASE **********/
        aws_jni_release_thread_env(jvm, env);
    }
    return;
}

static void s_aws_mqtt5_client_java_subscribe_callback_destructor(
    JNIEnv *env,
    struct aws_mqtt5_client_subscribe_return_data *callback_return_data) {
    struct aws_allocator *allocator = aws_jni_get_allocator();

    if (callback_return_data != NULL) {
        if (callback_return_data->jni_subscribe_future && env != NULL) {
            (*env)->DeleteGlobalRef(env, callback_return_data->jni_subscribe_future);
        }
        aws_mem_release(allocator, callback_return_data);
    }
}

static void s_aws_mqtt5_client_java_subscribe_completion(
    const struct aws_mqtt5_packet_suback_view *suback,
    int error_code,
    void *user_data) {

    int exception_error_code = error_code;
    JNIEnv *env = NULL;
    JavaVM *jvm = NULL;

    struct aws_mqtt5_client_subscribe_return_data *return_data =
        (struct aws_mqtt5_client_subscribe_return_data *)user_data;
    if (!return_data) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribeCompletion: invalid return data!");
        return;
    }
    struct aws_mqtt5_client_java_jni *java_client = return_data->java_client;
    if (!java_client) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribeCompletion: invalid client");
        goto clean_up;
    }

    /********** JNI ENV ACQUIRE **********/
    jvm = java_client->jvm;
    env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "SubscribeCompletion: could not get env");
        goto clean_up;
    }

    /* Get the future for this specific subscribe */
    jobject jni_subscribe_future = return_data->jni_subscribe_future;

    if (error_code != AWS_OP_SUCCESS) {
        exception_error_code = error_code;
        goto exception;
    }

    /* Calculate the number of references needed */
    size_t references_needed = 0;
    if (suback != NULL) {
        /* A SubAck packet will need 1 references at minimum */
        references_needed += 1;
        /* Optionals */
        s_aws_count_allocation(suback->reason_string, &references_needed);
        /* Add user properties and reason codes */
        references_needed += (suback->user_property_count) * 2;
        references_needed += 1; /* Add 1 for arrays to hold user properties */
        if (suback->reason_code_count > 0) {
            references_needed += suback->reason_code_count;
            references_needed += 1; /* Add 1 for arrays to hold reason codes */
        }
    }

    /**
     * Push a new local frame so any local references we make are tied to it. Then we can pop it to free memory.
     */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)references_needed);
    if (local_frame_result != 0) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT, "SubscribeCompletion: could not push local JNI frame with 4 allocation minimum");
        exception_error_code = AWS_ERROR_INVALID_STATE;
        goto exception;
    }

    /* The SubAck to return (if present) */
    jobject suback_packet_data = NULL;

    if (suback != NULL) {
        suback_packet_data = (*env)->NewObject(
            env,
            mqtt5_suback_packet_properties.suback_packet_class,
            mqtt5_suback_packet_properties.suback_constructor_id);

        if (s_set_jni_string_field_in_packet(
                env,
                suback->reason_string,
                suback_packet_data,
                mqtt5_suback_packet_properties.suback_reason_string_field_id,
                "reason string",
                true) != AWS_OP_SUCCESS) {
            goto clean_up;
        }

        if (suback->reason_codes != NULL) {
            if (suback->reason_code_count > 0) {
                for (size_t i = 0; i < suback->reason_code_count; ++i) {
                    const enum aws_mqtt5_suback_reason_code *reason_code_data = &suback->reason_codes[i];
                    if (s_set_int_enum_in_packet(
                            env,
                            (int *)reason_code_data,
                            suback_packet_data,
                            mqtt5_suback_packet_properties.suback_native_add_suback_code_id,
                            false) != AWS_OP_SUCCESS) {
                        s_aws_mqtt5_client_log_and_throw_exception(
                            env,
                            "Error when creating SubAckPacket from native: Could not set reason code",
                            AWS_ERROR_INVALID_STATE);
                        exception_error_code = AWS_ERROR_INVALID_STATE;
                        goto exception;
                    }
                }
            }
        }

        if (s_set_user_properties_field(
                env,
                suback->user_property_count,
                suback->user_properties,
                suback_packet_data,
                mqtt5_suback_packet_properties.suback_user_properties_field_id) == AWS_OP_ERR) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "When creating PubAckPacket from native could not add user property!");
            exception_error_code = AWS_ERROR_INVALID_STATE;
            goto exception;
        }
    }

    /* Complete the promise */
    (*env)->CallBooleanMethod(
        env, jni_subscribe_future, completable_future_properties.complete_method_id, suback_packet_data);
    aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    goto clean_up;

exception:
    s_complete_future_with_exception(env, jni_subscribe_future, exception_error_code);
    goto clean_up;

clean_up:
    s_aws_mqtt5_client_java_subscribe_callback_destructor(env, return_data);
    if (env != NULL) {
        (*env)->PopLocalFrame(env, NULL);
        /********** JNI ENV RELEASE **********/
        aws_jni_release_thread_env(jvm, env);
    }
}

static void s_aws_mqtt5_client_java_unsubscribe_callback_destructor(
    JNIEnv *env,
    struct aws_mqtt5_client_unsubscribe_return_data *callback_return_data) {
    struct aws_allocator *allocator = aws_jni_get_allocator();

    if (callback_return_data != NULL) {
        if (callback_return_data->jni_unsubscribe_future && env != NULL) {
            (*env)->DeleteGlobalRef(env, callback_return_data->jni_unsubscribe_future);
        }
        aws_mem_release(allocator, callback_return_data);
    }
}

static void s_aws_mqtt5_client_java_unsubscribe_completion(
    const struct aws_mqtt5_packet_unsuback_view *unsuback,
    int error_code,
    void *user_data) {

    int exception_error_code = error_code;
    JNIEnv *env = NULL;
    JavaVM *jvm = NULL;

    struct aws_mqtt5_client_unsubscribe_return_data *return_data =
        (struct aws_mqtt5_client_unsubscribe_return_data *)user_data;
    if (!return_data) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribeCompletion: invalid return data!");
        return;
    }

    struct aws_mqtt5_client_java_jni *java_client = return_data->java_client;
    if (!java_client) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribeCompletion: invalid client");
        goto clean_up;
    }

    /********** JNI ENV ACQUIRE **********/
    jvm = java_client->jvm;
    env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "UnsubscribeCompletion: could not get env");
        goto clean_up;
    }

    /* Get the future for this specific unsubscribe */
    jobject jni_unsubscribe_future = return_data->jni_unsubscribe_future;

    /* Calculate the number of references needed */
    size_t references_needed = 0;
    if (unsuback != NULL) {
        /* A UnsubAck packet will need 1 reference at minimum */
        references_needed += 1;
        /* Optionals */
        s_aws_count_allocation(unsuback->reason_string, &references_needed);
        /* Add user properties and reason codes */
        references_needed += (unsuback->user_property_count) * 2;
        references_needed += 1; /* Add 1 for array to hold user properties */
        if (unsuback->reason_code_count > 0) {
            references_needed += unsuback->reason_code_count;
            references_needed += 1; /* Add 1 for array to hold reason codes */
        }
    }

    /**
     * Push a new local frame so any local allocations we make are tied to it. Then we can pop it to free memory.
     */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)references_needed);
    if (local_frame_result != 0) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT, "UnsubscribeCompletion: could not push local JNI frame with 4 allocation minimum");
        exception_error_code = AWS_ERROR_INVALID_STATE;
        goto exception;
    }

    if (error_code != AWS_OP_SUCCESS) {
        exception_error_code = error_code;
        goto exception;
    }

    jobject unsuback_packet_data = (*env)->NewObject(
        env,
        mqtt5_unsuback_packet_properties.unsuback_packet_class,
        mqtt5_unsuback_packet_properties.unsuback_constructor_id);

    if (s_set_jni_string_field_in_packet(
            env,
            unsuback->reason_string,
            unsuback_packet_data,
            mqtt5_unsuback_packet_properties.unsuback_reason_string_field_id,
            "reason string",
            true) != AWS_OP_SUCCESS) {
        goto clean_up;
    }

    if (unsuback->reason_codes) {
        if (unsuback->reason_code_count > 0) {
            for (size_t i = 0; i < unsuback->reason_code_count; ++i) {
                const enum aws_mqtt5_unsuback_reason_code *reason_code_data = &unsuback->reason_codes[i];
                if (s_set_int_enum_in_packet(
                        env,
                        (int *)reason_code_data,
                        unsuback_packet_data,
                        mqtt5_unsuback_packet_properties.unsuback_native_add_unsuback_code_id,
                        false) != AWS_OP_SUCCESS) {
                    s_aws_mqtt5_client_log_and_throw_exception(
                        env,
                        "Error when creating UnsubAckPacket from native: Could not set reason code",
                        AWS_ERROR_INVALID_STATE);
                    exception_error_code = AWS_ERROR_INVALID_STATE;
                    goto exception;
                }
            }
        }
    }

    if (s_set_user_properties_field(
            env,
            unsuback->user_property_count,
            unsuback->user_properties,
            unsuback_packet_data,
            mqtt5_unsuback_packet_properties.unsuback_user_properties_field_id) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "When creating UnsubAckPacket from native could not add user property!");
        exception_error_code = AWS_ERROR_INVALID_STATE;
        goto exception;
    }

    /* Complete the promise */
    (*env)->CallBooleanMethod(
        env, jni_unsubscribe_future, completable_future_properties.complete_method_id, unsuback_packet_data);
    aws_jni_check_and_clear_exception(env); /* To hide JNI warning */

    goto clean_up;

exception:
    s_complete_future_with_exception(env, jni_unsubscribe_future, exception_error_code);
    goto clean_up;

clean_up:
    s_aws_mqtt5_client_java_unsubscribe_callback_destructor(env, return_data);
    if (env != NULL) {
        (*env)->PopLocalFrame(env, NULL);
        /********** JNI ENV RELEASE **********/
        aws_jni_release_thread_env(jvm, env);
    }
}

static void s_aws_mqtt5_client_java_termination(void *complete_ctx) {
    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)complete_ctx;
    if (!java_client) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_CLIENT, "MQTT5 client termination function in JNI called, but with invalid java_client");
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JavaVM *jvm = java_client->jvm;
    JNIEnv *env = aws_jni_acquire_thread_env(jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "MQTT5 client termination function in JNI called, but could not get env");
        return;
    }

    (*env)->CallVoidMethod(env, java_client->jni_client, crt_resource_properties.release_references);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mqtt5_client_java_destroy(env, allocator, java_client);

    /********** JNI ENV RELEASE **********/
    aws_jni_release_thread_env(jvm, env);
}

/*******************************************************************************
 * MQTT5 CLIENT FUNCTIONS
 ******************************************************************************/

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalStart(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client) {
    (void)jni_class;

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.start: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!java_client->client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.start: Invalid/null native client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    int return_result = aws_mqtt5_client_start(java_client->client);

    if (return_result != AWS_OP_SUCCESS) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.start: aws_mqtt5_client_start returned a non AWS_OP_SUCCESS code!", aws_last_error());
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalStop(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_disconnect_packet) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.stop: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!java_client->client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.stop: Invalid/null native client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }

    struct aws_mqtt5_packet_disconnect_view_java_jni *java_disconnect_packet = NULL;
    int return_result = AWS_OP_ERR;

    if (jni_disconnect_packet) {
        java_disconnect_packet =
            aws_mqtt5_packet_disconnect_view_create_from_java(env, allocator, jni_disconnect_packet);

        if (!java_disconnect_packet) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "Mqtt5Client.stop: Invalid/null disconnect packet", aws_last_error());
            goto clean_up;
        }
    }

    return_result = aws_mqtt5_client_stop(
        java_client->client, aws_mqtt5_packet_disconnect_view_get_packet(java_disconnect_packet), NULL);
    if (return_result != AWS_OP_SUCCESS) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.stop: aws_mqtt5_client_stop returned a non AWS_OP_SUCCESS code!", return_result);
    }

clean_up:
    if (java_disconnect_packet) {
        aws_mqtt5_packet_disconnect_view_java_destroy(env, allocator, java_disconnect_packet);
    }
    return;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalPublish(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_publish_packet,
    jobject jni_publish_future) {
    (void)jni_class;

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.publish: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!java_client->client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.publish: Invalid/null native client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!jni_publish_packet) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.publish: Invalid/null publish packet", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!jni_publish_future) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.publish: Invalid/null publish future", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    /* Cannot fail */
    struct aws_mqtt5_client_publish_return_data *return_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_client_publish_return_data));
    return_data->java_client = java_client;

    struct aws_mqtt5_publish_completion_options completion_options;
    completion_options.completion_callback = &s_aws_mqtt5_client_java_publish_completion;
    completion_options.completion_user_data = (void *)return_data;

    struct aws_mqtt5_packet_publish_view_java_jni *java_publish_packet =
        aws_mqtt5_packet_publish_view_create_from_java(env, allocator, jni_publish_packet);
    if (!java_publish_packet) {
        goto exception;
    }

    return_data->jni_publish_future = (*env)->NewGlobalRef(env, jni_publish_future);
    int return_result = aws_mqtt5_client_publish(
        java_client->client, aws_mqtt5_packet_publish_view_get_packet(java_publish_packet), &completion_options);
    if (return_result != AWS_OP_SUCCESS) {
        goto exception;
    }
    goto clean_up;

exception:
    s_complete_future_with_exception(env, jni_publish_future, AWS_ERROR_MQTT5_OPERATION_PROCESSING_FAILURE);
    if (java_publish_packet) {
        aws_mqtt5_packet_publish_view_java_destroy(env, allocator, java_publish_packet);
    }
    if (return_data) {
        s_aws_mqtt5_client_java_publish_callback_destructor(env, return_data);
    }
    s_aws_mqtt5_client_log_and_throw_exception(env, "Mqtt5Client.publish: Unsuccessful publish", AWS_ERROR_INVALID_STATE);
    return;

clean_up:
    if (java_publish_packet) {
        aws_mqtt5_packet_publish_view_java_destroy(env, allocator, java_publish_packet);
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalSubscribe(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_subscribe_packet,
    jobject jni_subscribe_future) {
    (void)jni_class;

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.subscribe: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!java_client->client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.subscribe: Invalid/null native client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!jni_subscribe_packet) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.subscribe: Invalid/null subscribe packet", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!jni_subscribe_future) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.subscribe: Invalid/null subscribe future", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    /* Cannot fail */
    struct aws_mqtt5_client_subscribe_return_data *return_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_client_subscribe_return_data));
    return_data->java_client = java_client;

    struct aws_mqtt5_subscribe_completion_options completion_options;
    completion_options.completion_callback = &s_aws_mqtt5_client_java_subscribe_completion;
    completion_options.completion_user_data = (void *)return_data;

    struct aws_mqtt5_packet_subscribe_view_java_jni *java_subscribe_packet =
        aws_mqtt5_packet_subscribe_view_create_from_java(env, allocator, jni_subscribe_packet);
    if (java_subscribe_packet == NULL) {
        goto exception;
    }

    return_data->jni_subscribe_future = (*env)->NewGlobalRef(env, jni_subscribe_future);
    int return_result = aws_mqtt5_client_subscribe(
        java_client->client, aws_mqtt5_packet_subscribe_view_get_packet(java_subscribe_packet), &completion_options);
    if (return_result != AWS_OP_SUCCESS) {
        goto exception;
    }
    goto clean_up;

exception:
    s_complete_future_with_exception(env, jni_subscribe_future, AWS_ERROR_MQTT5_OPERATION_PROCESSING_FAILURE);
    if (java_subscribe_packet) {
        aws_mqtt5_packet_subscribe_view_java_destroy(env, allocator, java_subscribe_packet);
    }
    if (return_data) {
        s_aws_mqtt5_client_java_subscribe_callback_destructor(env, return_data);
    }
    s_aws_mqtt5_client_log_and_throw_exception(env, "Mqtt5Client.subscribe: Unsuccessful subscribe", AWS_ERROR_INVALID_STATE);
    return;

clean_up:
    if (java_subscribe_packet) {
        aws_mqtt5_packet_subscribe_view_java_destroy(env, allocator, java_subscribe_packet);
    }
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalUnsubscribe(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client,
    jobject jni_unsubscribe_packet,
    jobject jni_unsubscribe_future) {
    (void)jni_class;

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.unsubscribe: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!java_client->client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.unsubscribe: Invalid/null native client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!jni_unsubscribe_packet) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.unsubscribe: Invalid/null unsubscribe packet", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    if (!jni_unsubscribe_future) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.unsubscribe: Invalid/null unsubscribe future", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    /* Cannot fail */
    struct aws_mqtt5_client_unsubscribe_return_data *return_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_client_unsubscribe_return_data));
    return_data->java_client = java_client;

    struct aws_mqtt5_unsubscribe_completion_options completion_options;
    completion_options.completion_callback = &s_aws_mqtt5_client_java_unsubscribe_completion;
    completion_options.completion_user_data = (void *)return_data;

    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_unsubscribe_packet =
        aws_mqtt5_packet_unsubscribe_view_create_from_java(env, allocator, jni_unsubscribe_packet);
    if (!java_unsubscribe_packet) {
        goto exception;
    }

    return_data->jni_unsubscribe_future = (*env)->NewGlobalRef(env, jni_unsubscribe_future);
    int return_result = aws_mqtt5_client_unsubscribe(
        java_client->client,
        aws_mqtt5_packet_unsubscribe_view_get_packet(java_unsubscribe_packet),
        &completion_options);
    if (return_result != AWS_OP_SUCCESS) {
        goto exception;
    }
    goto clean_up;

exception:
    s_complete_future_with_exception(env, jni_unsubscribe_future, AWS_ERROR_MQTT5_OPERATION_PROCESSING_FAILURE);
    if (java_unsubscribe_packet) {
        aws_mqtt5_packet_unsubscribe_view_java_destroy(env, allocator, java_unsubscribe_packet);
    }
    if (return_data) {
        s_aws_mqtt5_client_java_unsubscribe_callback_destructor(env, return_data);
    }
    s_aws_mqtt5_client_log_and_throw_exception(env, "Mqtt5Client.unsubscribe: Unsuccessful unsubscribe", AWS_ERROR_INVALID_STATE);
    return;

clean_up:
    if (java_unsubscribe_packet) {
        aws_mqtt5_packet_unsubscribe_view_java_destroy(env, allocator, java_unsubscribe_packet);
    }
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalGetOperationStatistics(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_client) {
    (void)jni_class;

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.getOperationStatistics: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return NULL;
    }
    if (!java_client->client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "Mqtt5Client.getOperationStatistics: Invalid/null native client", AWS_ERROR_INVALID_ARGUMENT);
        return NULL;
    }

    /* Construct Java object */
    jobject jni_operation_statistics = (*env)->NewObject(
        env,
        mqtt5_client_operation_statistics_properties.statistics_class,
        mqtt5_client_operation_statistics_properties.statistics_constructor_id);
    if (jni_operation_statistics == NULL) {
        return NULL;
    }

    struct aws_mqtt5_client_operation_statistics client_stats;
    aws_mqtt5_client_get_stats(java_client->client, &client_stats);

    if (s_set_jni_uint64_t_field_in_packet(
            env,
            &client_stats.incomplete_operation_count,
            jni_operation_statistics,
            mqtt5_client_operation_statistics_properties.incomplete_operation_count_field_id,
            "incomplete operation count",
            false) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_jni_uint64_t_field_in_packet(
            env,
            &client_stats.incomplete_operation_size,
            jni_operation_statistics,
            mqtt5_client_operation_statistics_properties.incomplete_operation_size_field_id,
            "incomplete operation size",
            false) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_jni_uint64_t_field_in_packet(
            env,
            &client_stats.unacked_operation_count,
            jni_operation_statistics,
            mqtt5_client_operation_statistics_properties.unacked_operation_count_field_id,
            "unacked operation count",
            false) != AWS_OP_SUCCESS) {
        return NULL;
    }

    if (s_set_jni_uint64_t_field_in_packet(
            env,
            &client_stats.unacked_operation_size,
            jni_operation_statistics,
            mqtt5_client_operation_statistics_properties.unacked_operation_size_field_id,
            "unacked operation size",
            false) != AWS_OP_SUCCESS) {
        return NULL;
    }

    return jni_operation_statistics;
}

/*******************************************************************************
 * WEBSOCKET FUNCTIONS
 ******************************************************************************/
struct mqtt5_jni_ws_handshake {
    struct aws_mqtt5_client_java_jni *java_client;
    struct aws_http_message *http_request;
    aws_mqtt5_transform_websocket_handshake_complete_fn *complete_fn;
    void *complete_ctx;
    struct aws_allocator *allocator;
};

static void s_ws_handshake_destroy(struct mqtt5_jni_ws_handshake *ws_handshake) {
    if (!ws_handshake) {
        return;
    }
    aws_mem_release(ws_handshake->allocator, ws_handshake);
}

static void s_aws_mqtt5_client_java_websocket_handshake_transform(
    struct aws_http_message *request,
    void *user_data,
    aws_mqtt5_transform_websocket_handshake_complete_fn *complete_fn,
    void *complete_ctx) {

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)user_data;
    if (!java_client) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Websocket handshake function in JNI called without valid client");
        return;
    }

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(java_client->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        complete_fn(request, AWS_ERROR_INVALID_STATE, complete_ctx);
        return;
    }

    struct aws_allocator *alloc = aws_jni_get_allocator();

    /* Cannot fail */
    struct mqtt5_jni_ws_handshake *ws_handshake = aws_mem_calloc(alloc, 1, sizeof(struct mqtt5_jni_ws_handshake));

    ws_handshake->java_client = java_client;
    ws_handshake->complete_ctx = complete_ctx;
    ws_handshake->complete_fn = complete_fn;
    ws_handshake->http_request = request;
    ws_handshake->allocator = alloc;

    jobject java_http_request = aws_java_http_request_from_native(env, request, NULL);
    if (!java_http_request) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_CLIENT, "Could not create a HttpRequest for Java in MQTT5 client");
        aws_raise_error(AWS_ERROR_UNKNOWN);
        goto error;
    }

    if (java_client->jni_client) {
        jobject jni_client = java_client->jni_client;
        (*env)->CallVoidMethod(
            env, jni_client, mqtt5_client_properties.client_on_websocket_handshake_id, java_http_request, ws_handshake);
        AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    }

    (*env)->DeleteLocalRef(env, java_http_request);
    aws_jni_release_thread_env(java_client->jvm, env);
    /********** JNI ENV RELEASE SUCCESS PATH **********/

    return;

error:;
    int error_code = aws_last_error();
    s_ws_handshake_destroy(ws_handshake);
    complete_fn(request, error_code, complete_ctx);
    aws_jni_release_thread_env(java_client->jvm, env);
    /********** JNI ENV RELEASE FAILURE PATH **********/
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientInternalWebsocketHandshakeComplete(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection,
    jbyteArray jni_marshalled_request,
    jobject jni_throwable,
    jlong jni_user_data) {
    (void)jni_class;
    (void)jni_connection;

    struct mqtt5_jni_ws_handshake *ws_handshake = (void *)jni_user_data;
    int error_code = AWS_ERROR_SUCCESS;

    if (jni_throwable != NULL) {
        if ((*env)->IsInstanceOf(env, jni_throwable, crt_runtime_exception_properties.crt_runtime_exception_class)) {
            error_code = (*env)->GetIntField(env, jni_throwable, crt_runtime_exception_properties.error_code_field_id);
        }
        if (error_code == AWS_ERROR_SUCCESS) {
            error_code = AWS_ERROR_UNKNOWN; /* is there anything more that could be done here? */
        }
        goto done;
    }

    if (aws_apply_java_http_request_changes_to_native_request(
            env, jni_marshalled_request, NULL, ws_handshake->http_request)) {
        error_code = aws_last_error();
        goto done;
    }

done:
    ws_handshake->complete_fn(ws_handshake->http_request, error_code, ws_handshake->complete_ctx);
    s_ws_handshake_destroy(ws_handshake);
}

/*******************************************************************************
 * JNI FUNCTIONS
 ******************************************************************************/

/* Create and Destroy
**************************************/

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientNew(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_options,
    jobject jni_connect_options,
    jobject jni_bootstrap,
    jobject jni_client) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_mqtt5_packet_connect_view_java_jni *connect_options = NULL;
    struct aws_mqtt5_client_options client_options;
    AWS_ZERO_STRUCT(client_options);
    struct aws_http_proxy_options_java_jni *java_http_proxy_options = NULL;
    jstring jni_host_name = NULL;

    /* Needed to track if optionals are set or not */
    bool was_value_set = false;

    /**
     * Push a new local frame so any local allocations we make are tied to it. Then we can pop it to free memory.
     * * Reference: https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/functions.html#PushLocalFrame
     * In Java JNI allocations here, we have 21 allocations so we need at least that many.
     * According to this Stackoverflow, it should expand if we use more: https://stackoverflow.com/a/70946713
     * (NOTE: We cannot get the exact here because we are pulling from Java objects and we have no way to know how many
     * that will need)
     */
    jint local_frame_result = (*env)->PushLocalFrame(env, (jint)21);
    if (local_frame_result != 0) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env,
            "MQTT5 client new: could not push local JNI frame with 21 allocation minimum",
            AWS_ERROR_INVALID_STATE);
        return (jlong)NULL;
    }

    struct aws_mqtt5_client_java_jni *java_client =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt5_client_java_jni));
    AWS_LOGF_DEBUG(AWS_LS_MQTT_CLIENT, "java_client=%p: Initalizing MQTT5 client", (void *)java_client);
    if (java_client == NULL) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: could not initialize new client", AWS_ERROR_INVALID_STATE);
        return (jlong)NULL;
    }

    if (aws_get_string_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.options_host_name_field_id,
            s_client_string,
            "Host Name",
            &jni_host_name,
            &client_options.host_name,
            false,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }

    uint16_t port = 0;
    if (aws_get_uint16_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.options_port_field_id,
            s_client_string,
            "port",
            &port,
            false,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.port = port;
    }

    if (!jni_bootstrap) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: no bootstrap found", AWS_ERROR_INVALID_ARGUMENT);
        goto clean_up;
    }
    jlong jni_bootstrap_pointer =
        (*env)->CallLongMethod(env, jni_bootstrap, crt_resource_properties.get_native_handle_method_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: could not get native handle for bootstrap", AWS_ERROR_INVALID_ARGUMENT);
        goto clean_up;
    }
    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)jni_bootstrap_pointer;
    client_options.bootstrap = bootstrap;

    struct aws_socket_options *socket_options = NULL;
    struct aws_socket_options tmp_socket_options;
    jobject jni_socket_options =
        (*env)->CallObjectMethod(env, jni_options, mqtt5_client_options_properties.options_get_socket_options_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: error getting socket options", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_socket_options) {
        jlong jni_socket_options_pointer =
            (*env)->CallLongMethod(env, jni_socket_options, crt_resource_properties.get_native_handle_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "MQTT5 client new: could not get native handle for socket options", AWS_ERROR_INVALID_ARGUMENT);
            goto clean_up;
        }
        socket_options = (struct aws_socket_options *)jni_socket_options_pointer;
    }
    if (socket_options == NULL) {
        tmp_socket_options.type = AWS_SOCKET_STREAM;
        tmp_socket_options.domain = AWS_SOCKET_IPV4;
        tmp_socket_options.connect_timeout_ms = 10000;
        client_options.socket_options = &tmp_socket_options;
    } else {
        client_options.socket_options = socket_options;
    }

    jobject jni_tls_options =
        (*env)->CallObjectMethod(env, jni_options, mqtt5_client_options_properties.options_get_tls_options_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: error getting tls options", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_tls_options) {
        jlong jni_tls_pointer =
            (*env)->CallLongMethod(env, jni_tls_options, crt_resource_properties.get_native_handle_method_id);
        if (aws_jni_check_and_clear_exception(env)) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "MQTT5 client new: could not get native handle for tls options", AWS_ERROR_INVALID_ARGUMENT);
            goto clean_up;
        }
        struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_pointer;
        if (tls_ctx) {
            aws_tls_connection_options_init_from_ctx(&java_client->tls_options, tls_ctx);
            aws_tls_connection_options_set_server_name(&java_client->tls_options, allocator, &client_options.host_name);
            client_options.tls_options = &java_client->tls_options;
        }
    } else {
        client_options.tls_options = NULL;
    }

    jobject jni_http_proxy_options =
        (*env)->GetObjectField(env, jni_options, mqtt5_client_options_properties.http_proxy_options_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: error getting http proxy options", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_http_proxy_options) {
        java_http_proxy_options =
            s_aws_mqtt5_http_proxy_options_create_from_java(env, allocator, java_client, jni_http_proxy_options);
        client_options.http_proxy_options = &java_http_proxy_options->options;

        if (client_options.http_proxy_options->connection_type != AWS_HPCT_HTTP_TUNNEL) {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "MQTT5 client new: http proxy connection type has to be set to tunnel", AWS_ERROR_INVALID_STATE);
            goto clean_up;
        }
    }

    if (jni_connect_options) {
        connect_options = aws_mqtt5_packet_connect_view_create_from_java(env, allocator, jni_connect_options);
        if (connect_options != NULL || aws_jni_check_and_clear_exception(env)) {
            client_options.connect_options = aws_mqtt5_packet_connect_view_get_packet(connect_options);
        } else {
            s_aws_mqtt5_client_log_and_throw_exception(
                env, "MQTT5 client new: error getting connect options", AWS_ERROR_INVALID_STATE);
            goto clean_up;
        }
    }

    uint32_t session_behavior = UINT32_MAX;
    if (aws_get_enum_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.options_get_session_behavior_id,
            s_client_string,
            "session behavior",
            mqtt5_client_session_behavior_properties.client_get_value_id,
            &session_behavior,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.session_behavior = (enum aws_mqtt5_client_session_behavior_type)session_behavior;
    }

    uint32_t extended_validation_and_flow_control_options = UINT32_MAX;
    if (aws_get_enum_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.options_get_extended_validation_and_flow_control_options_id,
            s_client_string,
            "offline queue behavior",
            mqtt5_client_extended_validation_and_flow_control_options.client_get_value_id,
            &extended_validation_and_flow_control_options,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.extended_validation_and_flow_control_options =
            (enum aws_mqtt5_extended_validation_and_flow_control_options)extended_validation_and_flow_control_options;
    }

    uint32_t offline_queue_enum = UINT32_MAX;
    if (aws_get_enum_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.options_get_offline_queue_behavior_id,
            s_client_string,
            "offline queue behavior",
            mqtt5_client_offline_queue_behavior_type_properties.client_get_value_id,
            &offline_queue_enum,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.offline_queue_behavior = (enum aws_mqtt5_client_operation_queue_behavior_type)offline_queue_enum;
    }

    uint32_t retry_jitter_enum = UINT32_MAX;
    if (aws_get_enum_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.options_get_retry_jitter_mode_id,
            s_client_string,
            "retry jitter mode",
            mqtt5_client_jitter_mode_properties.client_get_value_id,
            &retry_jitter_enum,
            true,
            &was_value_set) == AWS_OP_ERR) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.retry_jitter_mode = (enum aws_exponential_backoff_jitter_mode)retry_jitter_enum;
    }

    uint64_t min_reconnect_delay_ms = 0;
    if (aws_get_uint64_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.min_reconnect_delay_ms_field_id,
            s_client_string,
            "minimum reconnect delay",
            &min_reconnect_delay_ms,
            true,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.min_reconnect_delay_ms = min_reconnect_delay_ms;
    }

    uint64_t max_reconnect_delay_ms = 0;
    if (aws_get_uint64_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.max_reconnect_delay_ms_field_id,
            s_client_string,
            "maximum reconnect delay",
            &max_reconnect_delay_ms,
            true,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.max_reconnect_delay_ms = max_reconnect_delay_ms;
    }

    uint64_t min_connected_time_to_reset_reconnect_delay_ms = 0;
    if (aws_get_uint64_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.min_connected_time_to_reset_reconnect_delay_ms_field_id,
            s_client_string,
            "minimum connected time to reset reconnect delay",
            &min_connected_time_to_reset_reconnect_delay_ms,
            true,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.min_connected_time_to_reset_reconnect_delay_ms = min_connected_time_to_reset_reconnect_delay_ms;
    }

    uint32_t ping_timeout = 0;
    if (aws_get_uint32_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.ping_timeout_ms_field_id,
            s_client_string,
            "ping timeout",
            &ping_timeout,
            true,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.ping_timeout_ms = ping_timeout;
    }

    uint32_t connack_timeout = 0;
    if (aws_get_uint32_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.connack_timeout_ms_field_id,
            s_client_string,
            "ConnAck timeout",
            &connack_timeout,
            true,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.connack_timeout_ms = connack_timeout;
    }

    uint32_t ack_timeout = 0;
    if (aws_get_uint32_from_jobject(
            env,
            jni_options,
            mqtt5_client_options_properties.ack_timeout_seconds_field_id,
            s_client_string,
            "Ack timeout",
            &ack_timeout,
            true,
            &was_value_set) != AWS_OP_SUCCESS) {
        goto clean_up;
    }
    if (was_value_set) {
        client_options.ack_timeout_seconds = ack_timeout;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &java_client->jvm);
    if (jvmresult != 0) {
        s_aws_mqtt5_client_log_and_throw_exception(env, "MQTT5 client new: Unable to get JVM", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    java_client->jni_client = (*env)->NewGlobalRef(env, jni_client);

    client_options.lifecycle_event_handler = &s_aws_mqtt5_client_java_lifecycle_event;
    client_options.lifecycle_event_handler_user_data = (void *)java_client;

    client_options.publish_received_handler = &s_aws_mqtt5_client_java_publish_received;
    client_options.publish_received_handler_user_data = (void *)java_client;

    /* Are we using websockets? */
    jobject jni_websocket_handshake =
        (*env)->GetObjectField(env, jni_client, mqtt5_client_properties.websocket_handshake_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: error getting websocket handshake transform", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_websocket_handshake) {
        client_options.websocket_handshake_transform = &s_aws_mqtt5_client_java_websocket_handshake_transform;
        client_options.websocket_handshake_transform_user_data = (void *)java_client;
    }

    jobject jni_publish_events =
        (*env)->GetObjectField(env, jni_options, mqtt5_client_options_properties.publish_events_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: error getting publish events", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_publish_events != NULL) {
        java_client->jni_publish_events = (*env)->NewGlobalRef(env, jni_publish_events);
    }

    jobject jni_lifecycle_events =
        (*env)->GetObjectField(env, jni_options, mqtt5_client_options_properties.lifecycle_events_field_id);
    if (aws_jni_check_and_clear_exception(env)) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client new: error getting lifecycle events", AWS_ERROR_INVALID_STATE);
        goto clean_up;
    }
    if (jni_lifecycle_events != NULL) {
        java_client->jni_lifecycle_events = (*env)->NewGlobalRef(env, jni_lifecycle_events);
    }

    client_options.client_termination_handler = &s_aws_mqtt5_client_java_termination;
    client_options.client_termination_handler_user_data = (void *)java_client;

    /* Make the MQTT5 client */
    java_client->client = aws_mqtt5_client_new(allocator, &client_options);
    /* Did we successfully make a client? If not, then throw an exception */
    if (java_client->client == NULL) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env,
            "MQTT5 client new: Was unable to create client due to option configuration! Enable error logging to see "
            "reason",
            AWS_ERROR_MQTT5_CLIENT_OPTIONS_VALIDATION);
        goto clean_up;
    }

    goto clean_up;

clean_up:

    aws_mqtt5_packet_connect_view_java_destroy(env, allocator, connect_options);
    s_aws_mqtt5_http_proxy_options_java_destroy(env, allocator, java_http_proxy_options);
    if (jni_host_name != NULL) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_host_name, client_options.host_name);
    }
    (*env)->PopLocalFrame(env, NULL);

    if (java_client->client != NULL) {
        return (jlong)java_client;
    }

    aws_mqtt5_client_java_destroy(env, allocator, java_client);
    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_mqtt5_Mqtt5Client_mqtt5ClientDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_mqtt_client) {
    (void)jni_class;

    struct aws_mqtt5_client_java_jni *java_client = (struct aws_mqtt5_client_java_jni *)jni_mqtt_client;
    if (!java_client) {
        s_aws_mqtt5_client_log_and_throw_exception(
            env, "MQTT5 client destroy: Invalid/null client", AWS_ERROR_INVALID_ARGUMENT);
        return;
    }
    java_client->client = aws_mqtt5_client_release(java_client->client);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

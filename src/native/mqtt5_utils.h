/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#ifndef AWS_JNI_MQTT5_UTILS_H
#define AWS_JNI_MQTT5_UTILS_H

#include <jni.h>

struct aws_mqtt5_user_property;
struct aws_mqtt5_packet_connack_view;
struct aws_mqtt5_packet_disconnect_view;
struct aws_mqtt5_negotiated_settings;
struct aws_mqtt5_packet_publish_view;
struct aws_mqtt5_packet_puback_view;

int s_set_jni_uint32_t_field_in_packet(
    JNIEnv *env,
    const uint32_t *native_integer,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional);

int s_set_jni_uint16_t_field_in_packet(
    JNIEnv *env,
    const uint16_t *native_integer,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional);

int s_set_jni_bool_field_in_packet(
    JNIEnv *env,
    const bool *native_boolean,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional);

int s_set_jni_string_field_in_packet(
    JNIEnv *env,
    const struct aws_byte_cursor *native_cursor,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional);

int s_set_jni_byte_array_field_in_packet(
    JNIEnv *env,
    const struct aws_byte_cursor *native_cursor,
    jobject packet,
    jfieldID field_id,
    char *field_name,
    bool optional);

int s_set_user_properties_field(
    JNIEnv *env,
    const size_t user_property_count,
    const struct aws_mqtt5_user_property *packet_properties,
    jobject packet,
    jfieldID user_property_field_id);

int s_set_int_enum_in_packet(
    JNIEnv *env,
    const int *int_enum,
    jobject packet,
    jmethodID set_enum_field_id,
    bool optional);

jobject s_aws_mqtt5_client_create_jni_connack_packet_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_packet_connack_view *native_connack_data);

jobject s_aws_mqtt5_client_create_jni_disconnect_packet_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_packet_disconnect_view *native_disconnect_data);

jobject s_aws_mqtt5_client_create_jni_negotiated_settings_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_negotiated_settings *native_negotiated_settings_data);

jobject s_aws_mqtt5_client_create_jni_publish_packet_from_native(
    JNIEnv *env,
    const struct aws_mqtt5_packet_publish_view *publish);

jobject s_aws_mqtt5_client_create_jni_puback_packet_from_native(
    JNIEnv *env,
    struct aws_mqtt5_packet_puback_view *puback_packet);

#endif /* AWS_JNI_MQTT5_UTILS_H */

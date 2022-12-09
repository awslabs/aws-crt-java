/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#ifndef AWS_JNI_PACKETS_H
#define AWS_JNI_PACKETS_H

#include <jni.h>

#include <aws/mqtt/v5/mqtt5_client.h>
#include <crt.h>

struct aws_mqtt5_packet_connect_view_java_jni;
struct aws_mqtt5_packet_disconnect_view_java_jni;
struct aws_mqtt5_packet_publish_view_java_jni;
struct aws_mqtt5_packet_subscribe_view_java_jni;
struct aws_mqtt5_packet_unsubscribe_view_java_jni;

int aws_get_uint16_from_jobject(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    uint16_t *result,
    uint16_t **destination,
    bool optional);

int aws_get_uint32_from_jobject(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    uint32_t *result,
    uint32_t **destination,
    bool optional);

int aws_get_uint64_from_jobject(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    uint64_t *result,
    uint64_t **destination,
    bool optional);

int aws_get_string_from_jobject(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    jstring result_jstring,
    struct aws_byte_cursor *result_cursor,
    struct aws_byte_cursor **destination,
    bool is_optional);

int aws_get_byte_array_from_jobject(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    jbyteArray result_jbyte_array,
    struct aws_byte_cursor *result_cursor,
    struct aws_byte_cursor **destination,
    bool optional);

int aws_get_boolean_from_jobject(
    JNIEnv *env,
    jobject packet,
    jfieldID packet_field,
    uint8_t *result_boolean_int,
    uint8_t **destination,
    bool optional);

int aws_get_enum_from_jobject(
    JNIEnv *env,
    jobject packet,
    jmethodID packet_enum_field,
    jmethodID enum_value_vield,
    uint32_t *enum_value_destination,
    bool optional);

void aws_mqtt5_packet_connect_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_connect_view_java_jni *java_packet);

struct aws_mqtt5_packet_connect_view_java_jni *aws_mqtt5_packet_connect_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_connect_packet);

struct aws_mqtt5_packet_connect_view *aws_mqtt5_packet_connect_view_get_packet(
    struct aws_mqtt5_packet_connect_view_java_jni *java_packet);

void aws_mqtt5_packet_disconnect_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_disconnect_view_java_jni *java_packet);

struct aws_mqtt5_packet_disconnect_view_java_jni *aws_mqtt5_packet_disconnect_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_disconnect_packet);

struct aws_mqtt5_packet_disconnect_view *aws_mqtt5_packet_disconnect_view_get_packet(
    struct aws_mqtt5_packet_disconnect_view_java_jni *java_packet);

void aws_mqtt5_packet_publish_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_publish_view_java_jni *java_packet);

struct aws_mqtt5_packet_publish_view_java_jni *aws_mqtt5_packet_publish_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_publish_packet);

struct aws_mqtt5_packet_publish_view *aws_mqtt5_packet_publish_view_get_packet(
    struct aws_mqtt5_packet_publish_view_java_jni *java_packet);

void aws_mqtt5_packet_subscribe_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_subscribe_view_java_jni *java_packet);

struct aws_mqtt5_packet_subscribe_view_java_jni *aws_mqtt5_packet_subscribe_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_subscribe_packet);

struct aws_mqtt5_packet_subscribe_view *aws_mqtt5_packet_subscribe_view_get_packet(
    struct aws_mqtt5_packet_subscribe_view_java_jni *java_packet);

void aws_mqtt5_packet_unsubscribe_view_java_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_packet);

struct aws_mqtt5_packet_unsubscribe_view_java_jni *aws_mqtt5_packet_unsubscribe_view_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_unsubscribe_packet);

struct aws_mqtt5_packet_unsubscribe_view *aws_mqtt5_packet_unsubscribe_view_get_packet(
    struct aws_mqtt5_packet_unsubscribe_view_java_jni *java_packet);

#endif /* AWS_JNI_PACKETS_H */

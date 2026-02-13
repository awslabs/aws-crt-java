/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#ifndef AWS_JNI_IOT_DEVICE_SDK_METRICS_H
#define AWS_JNI_IOT_DEVICE_SDK_METRICS_H

#include <aws/mqtt/mqtt.h>
#include <crt.h>
#include <jni.h>

struct aws_mqtt_iot_metrics_java_jni {
    struct aws_mqtt_iot_metrics metrics;
    struct aws_byte_buf library_name_buf;
    struct aws_byte_cursor library_name_cursor;
};

void aws_mqtt_iot_metrics_java_jni_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt_iot_metrics_java_jni *java_metrics);

struct aws_mqtt_iot_metrics_java_jni *aws_mqtt_iot_metrics_java_jni_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_iot_device_sdk_metrics);

struct aws_mqtt_iot_metrics *aws_mqtt_iot_metrics_java_jni_get_metrics(
    struct aws_mqtt_iot_metrics_java_jni *java_metrics);

#endif /* AWS_JNI_IOT_DEVICE_SDK_METRICS_H */

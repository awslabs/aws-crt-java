/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include "iot_device_sdk_metrics.h"
#include "mqtt5_packets.h"
#include <aws/mqtt/mqtt.h>
#include <crt.h>
#include <java_class_ids.h>


static char s_iot_device_sdk_metrics_string[] = "IoTDeviceSDKMetrics";

void aws_mqtt_iot_sdk_metrics_java_jni_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt_iot_sdk_metrics_java_jni *java_metrics) {
    (void)env;

    if (!java_metrics) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_GENERAL, "id=%p: Destroying IoTDeviceSDKMetrics", (void *)java_metrics);

    if (aws_byte_buf_is_valid(&java_metrics->library_name_buf)) {
        aws_byte_buf_clean_up(&java_metrics->library_name_buf);
    }

    aws_mem_release(allocator, java_metrics);
}

struct aws_mqtt_iot_sdk_metrics_java_jni *aws_mqtt_iot_sdk_metrics_java_jni_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_iot_device_sdk_metrics) {

    struct aws_mqtt_iot_sdk_metrics_java_jni *java_metrics =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_iot_sdk_metrics_java_jni));
    if (java_metrics == NULL) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics create_from_java: Creating new IoTDeviceSDKMetrics failed");
        return NULL;
    }

    if (aws_get_string_from_jobject(
            env,
            java_iot_device_sdk_metrics,
            iot_device_sdk_metrics_properties.library_name_field_id,
            s_iot_device_sdk_metrics_string,
            "library name",
            &java_metrics->library_name_buf,
            &java_metrics->library_name_cursor,
            false,
            NULL) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics create_from_java: No library name found");
        goto on_error;
    }
    java_metrics->metrics.library_name = java_metrics->library_name_cursor;

    return java_metrics;

on_error:
    /* Clean up */
    aws_mqtt_iot_sdk_metrics_java_jni_destroy(env, allocator, java_metrics);
    return NULL;
}

struct aws_mqtt_iot_sdk_metrics *aws_mqtt_iot_sdk_metrics_java_jni_get_metrics(
    struct aws_mqtt_iot_sdk_metrics_java_jni *java_metrics) {
    if (java_metrics) {
        return &java_metrics->metrics;
    } else {
        return NULL;
    }
}

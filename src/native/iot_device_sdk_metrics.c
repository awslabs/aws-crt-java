/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

/**
 * JNI bridge for IoT Device SDK Metrics.
 *
 * This file is responsible for converting the Java IoTDeviceSDKMetrics object
 * (library name + metadata key-value entries) into a native aws_mqtt_iot_metrics
 * struct that the C MQTT layer uses to append SDK telemetry to the CONNECT packet
 * username field.
 *
 */
#include <jni.h>

#include "iot_device_sdk_metrics.h"
#include "mqtt5_packets.h"
#include <aws/mqtt/mqtt.h>
#include <crt.h>
#include <java_class_ids.h>

static char s_iot_device_sdk_metrics_string[] = "IoTDeviceSDKMetrics";

/* Frees all native memory associated with a parsed metrics struct. */
void aws_mqtt_iot_metrics_java_jni_destroy(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_mqtt_iot_metrics_java_jni *java_metrics) {
    (void)env;

    if (!java_metrics) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_MQTT_GENERAL, "id=%p: Destroying IoTDeviceSDKMetrics", (void *)java_metrics);

    if (aws_byte_buf_is_valid(&java_metrics->library_name_buf)) {
        aws_byte_buf_clean_up(&java_metrics->library_name_buf);
    }

    if (aws_byte_buf_is_valid(&java_metrics->metadata_storage)) {
        aws_byte_buf_clean_up(&java_metrics->metadata_storage);
    }

    if (java_metrics->metadata_entries) {
        aws_mem_release(allocator, java_metrics->metadata_entries);
    }

    aws_mem_release(allocator, java_metrics);
}

/* Parses a Java IoTDeviceSDKMetrics object into a native metrics struct for the C MQTT layer. */
struct aws_mqtt_iot_metrics_java_jni *aws_mqtt_iot_metrics_java_jni_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_iot_device_sdk_metrics) {

    /* Allocate the holder struct (zero-initialized) */
    struct aws_mqtt_iot_metrics_java_jni *java_metrics =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_iot_metrics_java_jni));

    /* Extract the library name string (e.g. "IoTDeviceSDK/Java") */
    if (aws_get_string_from_jobject(
            env,
            java_iot_device_sdk_metrics,
            iot_device_sdk_metrics_properties.library_name_field_id,
            s_iot_device_sdk_metrics_string,
            "library name",
            &java_metrics->library_name_buf,
            &java_metrics->metrics.library_name,
            false,
            NULL) == AWS_OP_ERR) {
        AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics create_from_java: No library name found");
        goto on_error;
    }

    /* Parse metadataEntries list */
    jobject metadata_list = (*env)->GetObjectField(
        env, java_iot_device_sdk_metrics, iot_device_sdk_metrics_properties.metadata_entries_field_id);

    if (metadata_list != NULL && !aws_jni_check_and_clear_exception(env)) {
        jint count = (*env)->CallIntMethod(env, metadata_list, boxed_list_properties.list_size_id);
        if (count > 0 && !aws_jni_check_and_clear_exception(env)) {
            /* Pre-allocate entry array for all metadata key-value pairs */
            java_metrics->metadata_entries =
                aws_mem_calloc(allocator, (size_t)count, sizeof(struct aws_mqtt_metadata_entry));

            /* First pass: compute exact buffer size needed */
            size_t total_size = 0;
            for (jint i = 0; i < count; i++) {
                jobject entry = (*env)->CallObjectMethod(env, metadata_list, boxed_list_properties.list_get_id, i);
                if (entry == NULL || aws_jni_check_and_clear_exception(env)) {
                    AWS_LOGF_ERROR(
                        AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: failed to get metadata entry at index %d", (int)i);
                    goto on_error;
                }
                jstring key_jstr =
                    (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.key_field_id);
                jstring value_jstr =
                    (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.value_field_id);
                total_size += (size_t)(*env)->GetStringUTFLength(env, key_jstr);
                total_size += (size_t)(*env)->GetStringUTFLength(env, value_jstr);
                (*env)->DeleteLocalRef(env, key_jstr);
                (*env)->DeleteLocalRef(env, value_jstr);
                (*env)->DeleteLocalRef(env, entry);
            }

            AWS_LOGF_INFO(
                AWS_LS_MQTT_GENERAL,
                "IoTDeviceSDKMetrics: first pass complete, total_size=%zu for %d entries",
                total_size,
                (int)count);

            /* Allocate exact size  */
            aws_byte_buf_init(&java_metrics->metadata_storage, allocator, total_size);

            /* Second pass: copy strings into contiguous buffer and set cursors pointing into it */
            for (jint i = 0; i < count; i++) {
                jobject entry = (*env)->CallObjectMethod(env, metadata_list, boxed_list_properties.list_get_id, i);
                if (entry == NULL || aws_jni_check_and_clear_exception(env)) {
                    AWS_LOGF_ERROR(
                        AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: failed to get metadata entry at index %d", (int)i);
                    goto on_error;
                }

                jstring key_jstr =
                    (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.key_field_id);
                jstring value_jstr =
                    (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.value_field_id);

                struct aws_byte_cursor key_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, key_jstr);
                size_t key_offset = java_metrics->metadata_storage.len;
                aws_byte_buf_append(&java_metrics->metadata_storage, &key_cursor);
                aws_jni_byte_cursor_from_jstring_release(env, key_jstr, key_cursor);

                struct aws_byte_cursor value_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, value_jstr);
                size_t value_offset = java_metrics->metadata_storage.len;
                aws_byte_buf_append(&java_metrics->metadata_storage, &value_cursor);
                aws_jni_byte_cursor_from_jstring_release(env, value_jstr, value_cursor);

                java_metrics->metadata_entries[java_metrics->metadata_count].key =
                    aws_byte_cursor_from_array(java_metrics->metadata_storage.buffer + key_offset, key_cursor.len);
                java_metrics->metadata_entries[java_metrics->metadata_count].value =
                    aws_byte_cursor_from_array(java_metrics->metadata_storage.buffer + value_offset, value_cursor.len);

                AWS_LOGF_INFO(
                    AWS_LS_MQTT_GENERAL,
                    "IoTDeviceSDKMetrics metadata[%zu]: key=\"" PRInSTR "\" value=\"" PRInSTR "\"",
                    java_metrics->metadata_count,
                    AWS_BYTE_CURSOR_PRI(java_metrics->metadata_entries[java_metrics->metadata_count].key),
                    AWS_BYTE_CURSOR_PRI(java_metrics->metadata_entries[java_metrics->metadata_count].value));

                java_metrics->metadata_count++;

                (*env)->DeleteLocalRef(env, key_jstr);
                (*env)->DeleteLocalRef(env, value_jstr);
                (*env)->DeleteLocalRef(env, entry);
            }

            /* Wire up the C metrics struct to point at our parsed data */
            java_metrics->metrics.metadata_entries = java_metrics->metadata_entries;
            java_metrics->metrics.metadata_count = java_metrics->metadata_count;

            AWS_LOGF_INFO(
                AWS_LS_MQTT_GENERAL,
                "IoTDeviceSDKMetrics: parsing complete, %zu metadata entries set",
                java_metrics->metadata_count);
        }

        (*env)->DeleteLocalRef(env, metadata_list);
    }

    return java_metrics;

on_error:
    /* clean up */
    aws_mqtt_iot_metrics_java_jni_destroy(env, allocator, java_metrics);
    return NULL;
}

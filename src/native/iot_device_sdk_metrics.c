/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

/**
 * JNI bridge for IoT Device SDK Metrics.
 *
 * Converts a Java IoTDeviceSDKMetrics object (library name + metadata key-value
 * entries) into a native aws_mqtt_iot_metrics struct that the C MQTT layer uses
 * to append SDK telemetry to the CONNECT packet username field.
 *
 */
#include <jni.h>

#include "iot_device_sdk_metrics.h"
#include "mqtt5_packets.h"
#include <aws/mqtt/mqtt.h>
#include <crt.h>
#include <java_class_ids.h>

static char s_iot_device_sdk_metrics_string[] = "IoTDeviceSDKMetrics";
struct aws_metadata_buf_holder {
    struct aws_byte_cursor cursor;
    struct aws_byte_buf buffer;
};

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

    /* Free the library name buffer */
    if (aws_byte_buf_is_valid(&java_metrics->library_name_buf)) {
        aws_byte_buf_clean_up(&java_metrics->library_name_buf);
    }

    /* Free each individual key/value buffer stored in the array_list */
    if (aws_array_list_is_valid(&java_metrics->metadata_bufs)) {
        size_t buf_count = aws_array_list_length(&java_metrics->metadata_bufs);
        for (size_t i = 0; i < buf_count; i++) {
            struct aws_metadata_buf_holder *holder = NULL;
            aws_array_list_get_at_ptr(&java_metrics->metadata_bufs, (void **)&holder, i);
            aws_byte_buf_clean_up(&holder->buffer);
        }
        aws_array_list_clean_up(&java_metrics->metadata_bufs);
    }

    /* Free the pre-allocated entries array */
    if (java_metrics->metadata_entries) {
        aws_mem_release(allocator, java_metrics->metadata_entries);
    }

    /* Free the wrapper struct itself */
    aws_mem_release(allocator, java_metrics);
}

/* Parses a Java IoTDeviceSDKMetrics object into a native metrics struct for the C MQTT layer. */
struct aws_mqtt_iot_metrics_java_jni *aws_mqtt_iot_metrics_java_jni_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_iot_device_sdk_metrics) {

    jobject metadata_list = NULL;

    /* Zero-initialize so all fields are safe for cleanup on any error path */
    struct aws_mqtt_iot_metrics_java_jni *java_metrics =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_iot_metrics_java_jni));

    AWS_LOGF_DEBUG(AWS_LS_MQTT_GENERAL, "id=%p: Creating IoTDeviceSDKMetrics from Java object", (void *)java_metrics);

    /*
     * Extract the library name (e.g. "IoTDeviceSDK/Java").
     * Copies the Java String into library_name_buf and sets metrics.library_name
     * as a cursor pointing into that buffer.
     */
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

    /* Read the Java List<IoTMetricsMetadata> field */
    metadata_list = (*env)->GetObjectField(
        env, java_iot_device_sdk_metrics, iot_device_sdk_metrics_properties.metadata_entries_field_id);

    /* Null list is valid — return metrics with just library name */
    if (metadata_list == NULL || aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_DEBUG(AWS_LS_MQTT_GENERAL, "id=%p: IoTDeviceSDKMetrics no metadata entries", (void *)java_metrics);
        return java_metrics;
    }

    /* Get list size */
    jint count = (*env)->CallIntMethod(env, metadata_list, boxed_list_properties.list_size_id);

    /* Empty list is valid — return metrics with just library name */
    if (aws_jni_check_and_clear_exception(env) || count <= 0) {
        AWS_LOGF_DEBUG(AWS_LS_MQTT_GENERAL, "id=%p: IoTDeviceSDKMetrics metadata list empty", (void *)java_metrics);
        (*env)->DeleteLocalRef(env, metadata_list);
        return java_metrics;
    }

    /* Pre-allocate entries array since we know the count */
    java_metrics->metadata_entries = aws_mem_calloc(allocator, (size_t)count, sizeof(struct aws_mqtt_metadata_entry));

    /* Init array_list to hold individual byte_bufs (2 per entry: key + value) */
    if (aws_array_list_init_dynamic(
            &java_metrics->metadata_bufs, allocator, (size_t)count * 2, sizeof(struct aws_metadata_buf_holder))) {
        goto on_error;
    }

    for (jint i = 0; i < count; i++) {
        /* Call List.get(i) to get the IoTMetricsMetadata object */
        jobject entry = (*env)->CallObjectMethod(env, metadata_list, boxed_list_properties.list_get_id, i);
        if (!entry || aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: failed to get entry at index %d", (int)i);
            (*env)->DeleteLocalRef(env, entry);
            goto on_error;
        }

        /* Read the key field. Empty string is allowed; null Java field is not. */
        jstring key_jstr = (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.key_field_id);
        if (aws_jni_check_and_clear_exception(env) || !key_jstr) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: exception or null key at index %d", (int)i);
            (*env)->DeleteLocalRef(env, key_jstr);
            (*env)->DeleteLocalRef(env, entry);
            goto on_error;
        }

        /* Read the value field. Empty string is allowed; null Java field is not. */
        jstring value_jstr =
            (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.value_field_id);
        if (aws_jni_check_and_clear_exception(env) || !value_jstr) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: exception or null value at index %d", (int)i);
            goto on_error;
        }

        /* Key: acquire JVM bytes → copy into own buffer → release JVM bytes.
         * The local copy lets us hold onto the data after the JVM release. */
        struct aws_metadata_buf_holder holder_key;
        struct aws_byte_cursor tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, key_jstr);
        aws_byte_buf_init_copy_from_cursor(&holder_key.buffer, allocator, tmp_cursor);
        holder_key.cursor = aws_byte_cursor_from_buf(&holder_key.buffer);
        aws_jni_byte_cursor_from_jstring_release(env, key_jstr, tmp_cursor);
        key_jstr = NULL;

        /* Value: same pattern */
        struct aws_metadata_buf_holder holder_value;
        tmp_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, value_jstr);
        aws_byte_buf_init_copy_from_cursor(&holder_value.buffer, allocator, tmp_cursor);
        holder_value.cursor = aws_byte_cursor_from_buf(&holder_value.buffer);
        aws_jni_byte_cursor_from_jstring_release(env, value_jstr, tmp_cursor);
        value_jstr = NULL;

        /* Store holders so destroy can free each buffer later */
        aws_array_list_push_back(&java_metrics->metadata_bufs, &holder_key);
        aws_array_list_push_back(&java_metrics->metadata_bufs, &holder_value);

        /* Set entry cursors — point at heap memory owned by the holders */
        java_metrics->metadata_entries[i].key = holder_key.cursor;
        java_metrics->metadata_entries[i].value = holder_value.cursor;

        AWS_LOGF_DEBUG(
            AWS_LS_MQTT_GENERAL,
            "IoTDeviceSDKMetrics: metadata[%d] key=\"" PRInSTR "\" value=\"" PRInSTR "\"",
            (int)i,
            AWS_BYTE_CURSOR_PRI(java_metrics->metadata_entries[i].key),
            AWS_BYTE_CURSOR_PRI(java_metrics->metadata_entries[i].value));

        /* Release JNI local ref for this iteration. */
        (*env)->DeleteLocalRef(env, entry);
    }

    /* Point the C metrics struct at our parsed data.*/
    java_metrics->metadata_count = (size_t)count;
    java_metrics->metrics.metadata_entries = java_metrics->metadata_entries;
    java_metrics->metrics.metadata_count = java_metrics->metadata_count;

    AWS_LOGF_DEBUG(
        AWS_LS_MQTT_GENERAL,
        "id=%p: IoTDeviceSDKMetrics creation complete, %d metadata entries",
        (void *)java_metrics,
        (int)count);

    (*env)->DeleteLocalRef(env, metadata_list);
    return java_metrics;

on_error:
    if (metadata_list != NULL) {
        (*env)->DeleteLocalRef(env, metadata_list);
    }
    aws_mqtt_iot_metrics_java_jni_destroy(env, allocator, java_metrics);
    return NULL;
}

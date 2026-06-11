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

/*******************************************************************************
 * HELPER FUNCTIONS
 ******************************************************************************/
/**
 * Parse a java list of objects (each having a String key field and a String value field)
 * into a contiguous buffer.
 *
 * Two-pass approach:
 *  Pass 1 - measure total byte size of all keys and value
 *  Pass 2 - copy into one buffer, set cursors, pointing into it.
 *
 * @param env JNI environment
 * @param allocator     CRT allocator
 * @param java_list     Java List jobject (must not be NULL, count must be > 0)
 * @param count         Number of elements in the list
 * @param key_field_id  JNI field ID for the String key on each list element
 * @param value_field_id JNI field ID for the String value on each list element
 * @param out_storage   Output buffer that will own all string bytes
 * @param out_entries   Pre-allocated array of count entries to populate
 *
 * @return AWS_OP_SUCCESS or AWS_OP_ERR
 */
static int s_parse_string_pair_list(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_list,
    size_t count,
    jfieldID key_field_id,
    jfieldID value_field_id,
    struct aws_byte_buf *out_storage,
    struct aws_mqtt_metadata_entry *out_entries) {

    /*
     * First pass: iterate all entries to compute the total byte size needed for
     * all key and value strings combined. This lets us do a single allocation.
     */
    size_t total_size = 0;
    for (size_t i = 0; i < count; i++) {
        /* Call List.get(i) to retrieve the list object at this index */
        jobject entry = (*env)->CallObjectMethod(env, java_list, boxed_list_properties.list_get_id, (jint)i);
        if (entry == NULL || aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: failed to get entry at index %d", (int)i);
            return AWS_OP_ERR;
        }

        /* Read the key and value String fields from the entry */
        jstring key_jstr = (jstring)(*env)->GetObjectField(env, entry, key_field_id);
        jstring value_jstr = (jstring)(*env)->GetObjectField(env, entry, value_field_id);

        /* Accumulate byte lengths (modified-UTF8). */
        total_size += (size_t)(*env)->GetStringUTFLength(env, key_jstr);
        total_size += (size_t)(*env)->GetStringUTFLength(env, value_jstr);

        /* Release local refs to avoid exhausting JNI local ref table in large lists */
        (*env)->DeleteLocalRef(env, key_jstr);
        (*env)->DeleteLocalRef(env, value_jstr);
        (*env)->DeleteLocalRef(env, entry);
    }
    AWS_LOGF_DEBUG(
        AWS_LS_MQTT_GENERAL,
        "IoTDeviceSDKMetrics: first pass complete, total_size=%zu for %d entries",
        total_size,
        (int)count);
    /* Allocate one contiguous buffer for all strings */
    if (aws_byte_buf_init(out_storage, allocator, total_size)) {
        AWS_LOGF_ERROR(
            AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: failed to allocate %zu bytes for metadata storage", total_size);
        return AWS_OP_ERR;
    }

    /*
     * Second pass: copy each key/value string into the contiguous buffer and
     * build aws_byte_cursor structs that point into it at the correct offsets.
     */
    for (size_t i = 0; i < count; i++) {
        jobject entry = (*env)->CallObjectMethod(env, java_list, boxed_list_properties.list_get_id, (jint)i);
        if (entry == NULL || aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(AWS_LS_MQTT_GENERAL, "IoTDeviceSDKMetrics: failed to get entry at index %d", (int)i);
            return AWS_OP_ERR;
        }

        jstring key_jstr = (jstring)(*env)->GetObjectField(env, entry, key_field_id);
        jstring value_jstr = (jstring)(*env)->GetObjectField(env, entry, value_field_id);

        /* Acquire the raw UTF-8 bytes from the JVM for the key string */
        struct aws_byte_cursor key_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, key_jstr);

        /* Record where in the buffer this key will start */
        size_t key_offset = out_storage->len;
        /* Copy key bytes into our contiguous buffer */
        aws_byte_buf_append(out_storage, &key_cursor);
        /* Release the JVM's internal string memory*/
        aws_jni_byte_cursor_from_jstring_release(env, key_jstr, key_cursor);

        /* same pattern as above */
        struct aws_byte_cursor value_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, value_jstr);
        size_t value_offset = out_storage->len;
        aws_byte_buf_append(out_storage, &value_cursor);
        aws_jni_byte_cursor_from_jstring_release(env, value_jstr, value_cursor);

        out_entries[i].key = aws_byte_cursor_from_array(out_storage->buffer + key_offset, key_cursor.len);
        out_entries[i].value = aws_byte_cursor_from_array(out_storage->buffer + value_offset, value_cursor.len);

        AWS_LOGF_DEBUG(
            AWS_LS_MQTT_GENERAL,
            "IoTDeviceSDKMetrics: metadata[%d] key=\"" PRInSTR "\" value=\"" PRInSTR "\"",
            (int)i,
            AWS_BYTE_CURSOR_PRI(out_entries[i].key),
            AWS_BYTE_CURSOR_PRI(out_entries[i].value));

        (*env)->DeleteLocalRef(env, key_jstr);
        (*env)->DeleteLocalRef(env, value_jstr);
        (*env)->DeleteLocalRef(env, entry);
    }

    AWS_LOGF_DEBUG(
        AWS_LS_MQTT_GENERAL,
        "IoTDeviceSDKMetrics: second pass complete, %d entries parsed into %zu bytes",
        (int)count,
        out_storage->len);

    return AWS_OP_SUCCESS;
}

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

    jobject metadata_list = NULL;

    struct aws_mqtt_iot_metrics_java_jni *java_metrics =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_iot_metrics_java_jni));

    AWS_LOGF_DEBUG(AWS_LS_MQTT_GENERAL, "id=%p: Creating IoTDeviceSDKMetrics from Java object", (void *)java_metrics);
    /* Extract the library name string */
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

    /* Get the metadata list */
    metadata_list = (*env)->GetObjectField(
        env, java_iot_device_sdk_metrics, iot_device_sdk_metrics_properties.metadata_entries_field_id);

    if (metadata_list == NULL || aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_DEBUG(
            AWS_LS_MQTT_GENERAL,
            "id=%p: IoTDeviceSDKMetrics no metadata entries, returning with library name only",
            (void *)java_metrics);
        return java_metrics;
    }

    jint count = (*env)->CallIntMethod(env, metadata_list, boxed_list_properties.list_size_id);
    if (aws_jni_check_and_clear_exception(env) || count <= 0) {
        AWS_LOGF_DEBUG(
            AWS_LS_MQTT_GENERAL,
            "id=%p: IoTDeviceSDKMetrics metadata list empty or size() failed",
            (void *)java_metrics);
        (*env)->DeleteLocalRef(env, metadata_list);
        return java_metrics;
    }

    /* Allocate entry array */
    java_metrics->metadata_entries = aws_mem_calloc(allocator, (size_t)count, sizeof(struct aws_mqtt_metadata_entry));

    /* Use helper to do the two-pass parse */
    if (s_parse_string_pair_list(
            env,
            allocator,
            metadata_list,
            (size_t)count,
            iot_metrics_metadata_properties.key_field_id,
            iot_metrics_metadata_properties.value_field_id,
            &java_metrics->metadata_storage,
            java_metrics->metadata_entries) == AWS_OP_ERR) {
        goto on_error;
    }

    /* Wire up the C struct */
    java_metrics->metadata_count = (size_t)count;
    java_metrics->metrics.metadata_entries = java_metrics->metadata_entries;
    java_metrics->metrics.metadata_count = (size_t)count;

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

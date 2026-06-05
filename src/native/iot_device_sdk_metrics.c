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

struct aws_mqtt_iot_metrics_java_jni *aws_mqtt_iot_metrics_java_jni_create_from_java(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject java_iot_device_sdk_metrics) {

    struct aws_mqtt_iot_metrics_java_jni *java_metrics =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_mqtt_iot_metrics_java_jni));

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
            java_metrics->metadata_entries =
                aws_mem_calloc(allocator, (size_t)count, sizeof(struct aws_mqtt_metadata_entry));

            /* Pre-allocate storage buffer for all key/value strings */
            aws_byte_buf_init(&java_metrics->metadata_storage, allocator, (size_t)count * 64);

            for (jint i = 0; i < count; i++) {
                jobject entry = (*env)->CallObjectMethod(env, metadata_list, boxed_list_properties.list_get_id, i);
                if (entry == NULL || aws_jni_check_and_clear_exception(env)) {
                    continue;
                }

                jstring key_jstr =
                    (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.key_field_id);
                jstring value_jstr =
                    (jstring)(*env)->GetObjectField(env, entry, iot_metrics_metadata_properties.value_field_id);

                if (key_jstr != NULL && value_jstr != NULL) {
                    const char *key_chars = (*env)->GetStringUTFChars(env, key_jstr, NULL);
                    size_t key_len = (size_t)(*env)->GetStringUTFLength(env, key_jstr);
                    const char *value_chars = (*env)->GetStringUTFChars(env, value_jstr, NULL);
                    size_t value_len = (size_t)(*env)->GetStringUTFLength(env, value_jstr);

                    /* Append key to storage buffer */
                    size_t key_offset = java_metrics->metadata_storage.len;
                    aws_byte_buf_append_dynamic(
                        &java_metrics->metadata_storage,
                        &(struct aws_byte_cursor){.ptr = (uint8_t *)key_chars, .len = key_len});

                    /* Append value to storage buffer */
                    size_t value_offset = java_metrics->metadata_storage.len;
                    aws_byte_buf_append_dynamic(
                        &java_metrics->metadata_storage,
                        &(struct aws_byte_cursor){.ptr = (uint8_t *)value_chars, .len = value_len});

                    java_metrics->metadata_entries[java_metrics->metadata_count].key =
                        aws_byte_cursor_from_array(java_metrics->metadata_storage.buffer + key_offset, key_len);
                    java_metrics->metadata_entries[java_metrics->metadata_count].value =
                        aws_byte_cursor_from_array(java_metrics->metadata_storage.buffer + value_offset, value_len);
                    java_metrics->metadata_count++;

                    (*env)->ReleaseStringUTFChars(env, key_jstr, key_chars);
                    (*env)->ReleaseStringUTFChars(env, value_jstr, value_chars);
                }

                (*env)->DeleteLocalRef(env, key_jstr);
                (*env)->DeleteLocalRef(env, value_jstr);
                (*env)->DeleteLocalRef(env, entry);
            }

            /* Set the metrics struct fields */
            java_metrics->metrics.metadata_entries = java_metrics->metadata_entries;
            java_metrics->metrics.metadata_count = java_metrics->metadata_count;
        }

        (*env)->DeleteLocalRef(env, metadata_list);
    }

    return java_metrics;

on_error:
    /* clean up */
    aws_mqtt_iot_metrics_java_jni_destroy(env, allocator, java_metrics);
    return NULL;
}

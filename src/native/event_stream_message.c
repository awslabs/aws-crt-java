/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/event-stream/event_stream.h>

#include "crt.h"
#include "java_class_ids.h"

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_eventstream_Message_messageNew(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray headers,
    jbyteArray payload) {
    (void)jni_class;

    struct aws_event_stream_message *message =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct aws_event_stream_message));

    if (!message) {
        aws_jni_throw_runtime_exception(env, "Message.MessageNew: Allocation failed!");
        return (jlong)NULL;
    }

    struct aws_array_list *headers_list_alias = NULL;
    struct aws_byte_buf *payload_alias = NULL;
    struct aws_array_list headers_list;
    AWS_ZERO_STRUCT(headers_list);
    struct aws_byte_buf payload_buf;
    AWS_ZERO_STRUCT(payload_buf);
    void *headers_blob = NULL;
    void *payload_blob = NULL;

    if (headers) {
        jsize headers_blob_len = (*env)->GetArrayLength(env, headers);
        headers_blob = (*env)->GetPrimitiveArrayCritical(env, headers, NULL);

        if (!headers_blob) {
            aws_jni_throw_runtime_exception(env, "Message.MessageNew: acquiring headers array region failed!");
            goto error;
        }

        if (aws_event_stream_headers_list_init(&headers_list, aws_jni_get_allocator())) {
            aws_jni_throw_runtime_exception(env, "Message.MessageNew: initializing headers failed!");
            goto error;
        }

        int headers_parse_error =
            aws_event_stream_read_headers_from_buffer(&headers_list, (uint8_t *)headers_blob, headers_blob_len);

        if (headers_parse_error) {
            aws_jni_throw_runtime_exception(env, "Message.MessageNew: parsing headers failed!");
            goto error;
        }

        headers_list_alias = &headers_list;
    }

    if (payload) {
        jsize payload_blob_len = (*env)->GetArrayLength(env, payload);
        payload_blob = (*env)->GetPrimitiveArrayCritical(env, payload, NULL);

        if (!payload_blob) {
            aws_jni_throw_runtime_exception(env, "Message.MessageNew: acquiring payload array region failed!");
            goto error;
        }

        payload_buf = aws_byte_buf_from_array(payload_blob, (size_t)payload_blob_len);
        payload_alias = &payload_buf;
    }

    if (aws_event_stream_message_init(message, aws_jni_get_allocator(), headers_list_alias, payload_alias)) {
        goto error;
    }

    if (headers_blob) {
        (*env)->ReleasePrimitiveArrayCritical(env, headers, headers_blob, 0);
    }

    if (payload_blob) {
        (*env)->ReleasePrimitiveArrayCritical(env, payload, payload_blob, 0);
    }

    if (headers_list.length) {
        aws_array_list_clean_up(&headers_list);
    }

    return (jlong)message;

error:
    if (headers_blob) {
        (*env)->ReleasePrimitiveArrayCritical(env, headers, headers_blob, 0);
    }

    if (payload_blob) {
        (*env)->ReleasePrimitiveArrayCritical(env, payload, payload_blob, 0);
    }

    if (headers_list.length) {
        aws_array_list_clean_up(&headers_list);
    }

    return (jlong)NULL;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_Message_messageDelete(
    JNIEnv *env,
    jclass jni_class,
    jlong message_ptr) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_message *message = (struct aws_event_stream_message *)message_ptr;
    aws_event_stream_message_clean_up(message);
    aws_mem_release(aws_jni_get_allocator(), message);
}

JNIEXPORT
jobject JNICALL Java_software_amazon_awssdk_crt_eventstream_Message_messageBuffer(
    JNIEnv *env,
    jclass jni_class,
    jlong message_ptr) {
    (void)jni_class;

    struct aws_event_stream_message *message = (struct aws_event_stream_message *)message_ptr;
    const uint8_t *buffer = aws_event_stream_message_buffer(message);
    size_t buffer_len = aws_event_stream_message_total_length(message);

    return aws_jni_direct_byte_buffer_from_raw_ptr(env, buffer, (jlong)buffer_len);
}

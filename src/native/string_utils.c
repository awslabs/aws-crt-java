/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/common/encoding.h>
#include <aws/common/string.h>

#include "crt.h"

JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_utils_StringUtils_stringUtilsBase64Encode(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray jni_data) {
    (void)jni_class;

    struct aws_byte_cursor data_cursor;
    AWS_ZERO_STRUCT(data_cursor);
    struct aws_byte_buf formatted_data;
    AWS_ZERO_STRUCT(formatted_data);
    jbyteArray return_data = NULL;

    // Explicitly return NULL if the data we get is null to avoid an exception. NULL should return NULL
    if (jni_data == NULL) {
        return return_data;
    }

    data_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_data);
    if (data_cursor.ptr == NULL) {
        return return_data;
    }

    // Determine how much space we need
    size_t terminated_length = 0;
    if (aws_base64_compute_encoded_len(data_cursor.len, &terminated_length) != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "StringUtils: Could not determine length for base64 encode");
        goto clean_up;
    }

    aws_byte_buf_init(&formatted_data, aws_jni_get_allocator(), terminated_length);
    int result = aws_base64_encode(&data_cursor, &formatted_data);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "StringUtils: Could not perform base64 encode");
        goto clean_up;
    }

    struct aws_byte_cursor formatted_data_cursor = aws_byte_cursor_from_buf(&formatted_data);
    return_data = aws_jni_byte_array_from_cursor(env, &formatted_data_cursor);

clean_up:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_data, data_cursor);
    aws_byte_buf_clean_up_secure(&formatted_data);
    return return_data;
}

JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_utils_StringUtils_stringUtilsBase64Decode(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray jni_data) {
    (void)jni_class;

    struct aws_byte_cursor data_cursor;
    AWS_ZERO_STRUCT(data_cursor);
    struct aws_byte_buf formatted_data;
    AWS_ZERO_STRUCT(formatted_data);
    jbyteArray return_data = NULL;

    // Explicitly return NULL if the data we get is null to avoid an exception. NULL should return NULL
    if (jni_data == NULL) {
        return return_data;
    }

    data_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_data);
    if (data_cursor.ptr == NULL) {
        return NULL;
    }

    // Determine how much space we need
    size_t terminated_length = 0;
    if (aws_base64_compute_decoded_len(&data_cursor, &terminated_length) != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "StringUtils: Could not determine length for base64 decode");
        goto clean_up;
    }

    aws_byte_buf_init(&formatted_data, aws_jni_get_allocator(), terminated_length);
    int result = aws_base64_decode(&data_cursor, &formatted_data);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "StringUtils: Could not perform base64 decode");
        goto clean_up;
    }

    struct aws_byte_cursor formatted_data_cursor = aws_byte_cursor_from_buf(&formatted_data);
    return_data = aws_jni_byte_array_from_cursor(env, &formatted_data_cursor);

clean_up:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_data, data_cursor);
    aws_byte_buf_clean_up_secure(&formatted_data);
    return return_data;
}

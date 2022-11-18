/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/common/string.h>
#include <aws/common/encoding.h>

#include "crt.h"


JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_utils_StringUtils_stringUtilsBase64Encode(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray jni_data) {
    (void)jni_class;

    struct aws_byte_cursor data_cursor;
    AWS_ZERO_STRUCT(data_cursor);
    if (jni_data != NULL) {
        data_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_data);
    } else {
        return NULL;
    }

    // Determine how much space we need
    size_t terminated_length = 0;
    if (aws_base64_compute_encoded_len(data_cursor.len, &terminated_length) != AWS_OP_SUCCESS) {
        goto clean_up;
    }

    struct aws_byte_buf formatted_data;
    AWS_ZERO_STRUCT(formatted_data);
    aws_byte_buf_init(&formatted_data, aws_jni_get_allocator(), terminated_length);
    int result = aws_base64_encode(&data_cursor, &formatted_data);
    if (result != AWS_OP_SUCCESS) {
        aws_byte_buf_clean_up(&formatted_data);
        goto clean_up;
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_data, data_cursor);

    struct aws_byte_cursor formatted_data_cursor = aws_byte_cursor_from_buf(&formatted_data);
    jbyteArray return_data = aws_jni_byte_array_from_cursor(env, &formatted_data_cursor);
    aws_byte_buf_clean_up_secure(&formatted_data);

    return return_data;

clean_up:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_data, data_cursor);
    return NULL;
}

JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_utils_StringUtils_stringUtilsBase64Decode(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray jni_data) {
    (void)jni_class;

    struct aws_byte_cursor data_cursor;
    AWS_ZERO_STRUCT(data_cursor);
    if (jni_data != NULL) {
        data_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_data);
    } else {
        return NULL;
    }

    // Determine how much space we need
    size_t terminated_length = 0;
    if (aws_base64_compute_decoded_len(&data_cursor, &terminated_length) != AWS_OP_SUCCESS) {
        goto clean_up;
    }

    struct aws_byte_buf formatted_data;
    AWS_ZERO_STRUCT(formatted_data);
    aws_byte_buf_init(&formatted_data, aws_jni_get_allocator(), terminated_length);
    int result = aws_base64_decode(&data_cursor, &formatted_data);
    if (result != AWS_OP_SUCCESS) {
        aws_byte_buf_clean_up(&formatted_data);
        goto clean_up;
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_data, data_cursor);

    struct aws_byte_cursor formatted_data_cursor = aws_byte_cursor_from_buf(&formatted_data);
    jbyteArray return_data = aws_jni_byte_array_from_cursor(env, &formatted_data_cursor);
    aws_byte_buf_clean_up_secure(&formatted_data);

    return return_data;

clean_up:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_data, data_cursor);
    return NULL;
}

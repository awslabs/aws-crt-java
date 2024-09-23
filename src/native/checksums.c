/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/checksums/crc.h>

#include "crt.h"
#include "java_class_ids.h"

jint crc32_common(
    JNIEnv *env,
    jbyteArray input,
    jint previous,
    const size_t start,
    size_t length,
    uint32_t (*checksum_fn)(const uint8_t *, size_t, uint32_t)) {
    struct aws_byte_cursor c_byte_array = aws_jni_byte_cursor_from_jbyteArray_acquire(env, input);
    struct aws_byte_cursor cursor = c_byte_array;
    aws_byte_cursor_advance(&cursor, start);
    cursor.len = aws_min_size(length, cursor.len);
    jint res_signed = (jint)checksum_fn(cursor.ptr, cursor.len, previous);
    aws_jni_byte_cursor_from_jbyteArray_release(env, input, c_byte_array);
    return res_signed;
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_CRC32_crc32(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous,
    jint offset,
    jint length) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    return crc32_common(env, input, previous, offset, length, aws_checksums_crc32_ex);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_CRC32C_crc32c(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous,
    jint offset,
    jint length) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    return crc32_common(env, input, previous, offset, length, aws_checksums_crc32c_ex);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_CRC64NVME_crc64nvme(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous,
    jint offset,
    jint length) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_cursor c_byte_array = aws_jni_byte_cursor_from_jbyteArray_acquire(env, input);
    struct aws_byte_cursor cursor = c_byte_array;
    aws_byte_cursor_advance(&cursor, offset);
    cursor.len = aws_min_size(length, cursor.len);
    jint res_signed = (jint)aws_checksums_crc64nvme_ex(cursor.ptr, cursor.len, previous);
    aws_jni_byte_cursor_from_jbyteArray_release(env, input, c_byte_array);
    return res_signed;
}

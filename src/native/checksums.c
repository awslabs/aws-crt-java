/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/checksums/crc.h>

#include "crt.h"

jint crc_common(
    JNIEnv *env,
    jbyteArray input,
    jint previous,
    const size_t start,
    size_t length,
    uint32_t (*checksum_fn)(const uint8_t *, int, uint32_t)) {
    struct aws_byte_cursor cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, input);
    aws_byte_cursor_advance(&cursor, start);
    cursor.len = aws_min_i64(length, cursor.len);
    uint32_t res = (uint32_t)previous;
    while (cursor.len > INT_MAX) {
        res = checksum_fn(cursor.ptr, INT_MAX, res);
        aws_byte_cursor_advance(&cursor, INT_MAX);
    }
    jint res_signed = (jint)checksum_fn(cursor.ptr, (int)cursor.len, res);
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
    return crc_common(env, input, previous, offset, length, aws_checksums_crc32);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_CRC32C_crc32c(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous,
    jint offset,
    jint length) {
    (void)jni_class;
    return crc_common(env, input, previous, offset, length, aws_checksums_crc32c);
}

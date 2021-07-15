/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/checksums/crc.h>

#include "crt.h"

jint crc_common(JNIEnv *env, jbyteArray input, jint previous, uint32_t (*checksum_fn)(const uint8_t *, int, uint32_t)) {
    struct aws_byte_cursor cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, input);
    size_t len = cursor.len;
    uint32_t res = (uint32_t)previous;
    uint8_t *buf = cursor.ptr;
    while (len > INT_MAX) {
        res = checksum_fn(buf, INT_MAX, res);
        buf += (size_t)INT_MAX;
        len -= (size_t)INT_MAX;
    }
    jint res_signed = (jint)checksum_fn(buf, (int)len, res);
    return res_signed;
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_Checksums_crc32(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous) {
    (void)jni_class;
    return crc_common(env, input, previous, aws_checksums_crc32);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_Checksums_crc32c(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous) {
    (void)jni_class;
    return crc_common(env, input, previous, aws_checksums_crc32c);
}

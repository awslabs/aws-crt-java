/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/checksums/crc.h>

#include "crt.h"

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_Crc_crc32(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous) {
        (void) jni_class;
        struct aws_byte_cursor cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, input);
        return (jint)aws_checksums_crc32(cursor.ptr, cursor.len, (uint32_t)previous);
}

JNIEXPORT jint JNICALL Java_software_amazon_awssdk_crt_checksums_Crc_crc32c(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jint previous) {
        (void) jni_class;
        struct aws_byte_cursor cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, input);
        return (jint)aws_checksums_crc32c(cursor.ptr, cursor.len, (uint32_t)previous);
}

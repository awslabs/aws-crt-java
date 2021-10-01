/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "crt.h"
#include <aws/io/uri.h>
#include <jni.h>

static jbyteArray s_encoding_common(
    JNIEnv *env,
    jbyteArray buffer,
    jbyteArray cursor,
    int (*encoding_fn)(struct aws_byte_buf *, const struct aws_byte_cursor *)) {

    struct aws_byte_cursor c_intermediate_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, buffer);
    struct aws_byte_cursor c_byte_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, cursor);
    jbyteArray uri_encoding = NULL;
    struct aws_byte_buf c_byte_buf;
    aws_byte_buf_init_copy_from_cursor(&c_byte_buf, aws_jni_get_allocator(), c_intermediate_cursor);
    if (encoding_fn(&c_byte_buf, &c_byte_cursor)) {
        aws_jni_throw_runtime_exception(env, "uri.encodingCommon: failed to encode buffer");
        goto clean_up;
    }
    struct aws_byte_cursor uri_encoding_cursor = aws_byte_cursor_from_buf(&c_byte_buf);
    uri_encoding = aws_jni_byte_array_from_cursor(env, &uri_encoding_cursor);
clean_up:
    aws_jni_byte_cursor_from_jbyteArray_release(env, buffer, c_intermediate_cursor);
    aws_jni_byte_cursor_from_jbyteArray_release(env, cursor, c_byte_cursor);
    aws_byte_buf_clean_up(&c_byte_buf);
    return uri_encoding;
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_io_Uri_appendEncodingUriPath(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray encoded,
    jbyteArray path) {
    (void)jni_class;
    return s_encoding_common(env, encoded, path, aws_byte_buf_append_encoding_uri_path);
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_io_Uri_appendEncodingUriParam(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray encoded,
    jbyteArray param) {
    (void)jni_class;
    return s_encoding_common(env, encoded, param, aws_byte_buf_append_encoding_uri_param);
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_io_Uri_appendDecodingUri(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray base,
    jbyteArray encoded) {
    (void)jni_class;
    return s_encoding_common(env, base, encoded, aws_byte_buf_append_decoding_uri);
}

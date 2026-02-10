/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "crt.h"
#include "java_class_ids.h"
#include <jni.h>

#include <aws/checksums/xxhash.h>

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash64Compute(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray message, 
    jlong seed) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 8);

    struct aws_byte_cursor message_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, message);
    if (message_cursor.ptr == NULL) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash64Compute: failed to pin message bytes");
        return NULL;
    }

    jbyteArray hash = NULL;
    if (aws_xxhash64_compute(seed, message_cursor, &hash_buffer)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash64Compute: failed to compute hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hash = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, message, message_cursor);
    aws_byte_buf_clean_up(&hash_buffer);

    return hash;
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash3_64Compute(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray message, 
    jlong seed) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 8);

    struct aws_byte_cursor message_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, message);
    if (message_cursor.ptr == NULL) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_64Compute: failed to pin message bytes");
        return NULL;
    }

    jbyteArray hash = NULL;
    if (aws_xxhash3_64_compute(seed, message_cursor, &hash_buffer)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_64Compute: failed to compute hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hash = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, message, message_cursor);
    aws_byte_buf_clean_up(&hash_buffer);

    return hash;
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash3_128Compute(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray message, 
    jlong seed) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 8);

    struct aws_byte_cursor message_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, message);
    if (message_cursor.ptr == NULL) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_128Compute: failed to pin message bytes");
        return NULL;
    }

    jbyteArray hash = NULL;
    if (aws_xxhash3_128_compute(seed, message_cursor, &hash_buffer)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_128Compute: failed to compute hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hash = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, message, message_cursor);
    aws_byte_buf_clean_up(&hash_buffer);

    return hash;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash64Create(
    JNIEnv *env,
    jclass jni_class,
    jlong seed) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = aws_xxhash64_new(aws_jni_get_allocator(), seed);
    if (hash == NULL) {
        return (jlong)0;
    }

    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    return (jlong)hash;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash3_64Create(
    JNIEnv *env,
    jclass jni_class,
    jlong seed) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = aws_xxhash3_64_new(aws_jni_get_allocator(), seed);
    if (hash == NULL) {
        return (jlong)0;
    }

    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    return (jlong)hash;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash3_128Create(
    JNIEnv *env,
    jclass jni_class,
    jlong seed) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = aws_xxhash3_128_new(aws_jni_get_allocator(), seed);
    if (hash == NULL) {
        return (jlong)0;
    }

    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    return (jlong)hash;
}

JNIEXPORT
void JNICALL
    Java_software_amazon_awssdk_crt_cal_EccKeyPair_xxHashRelease(JNIEnv *env, jclass jni_class, jlong hash_ptr) {
    (void)jni_ekp;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = (struct aws_xxhash *)hash_ptr;

    aws_xxhash_destroy(hash);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHashUpdate(
    JNIEnv *env,
    jclass jni_class,
    jlong hash_ptr,
    jbyteArray message) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = (struct aws_xxhash *)hash_ptr;

    struct aws_byte_cursor message_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, message);
    if (message_cursor.ptr == NULL) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHashUpdate: failed to pin message bytes");
        return;
    }

    if (aws_xxhash_update(hash, message_cursor)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHashUpdate: failed to update hash");
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, message, message_cursor);
}

JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHashFinalize(
    JNIEnv *env,
    jclass jni_class,
    jlong hash_ptr) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = (struct aws_xxhash *)hash_ptr;

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 16);

    jbyteArray hash_out = NULL;
    if (aws_xxhash_finalize(hash, &hash_buffer)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHashFinalize: failed to finalize hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hashhash_out = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_byte_buf_clean_up(&hash_buffer);

    return hash_out;
}

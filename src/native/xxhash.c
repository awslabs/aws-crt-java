/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "crt.h"
#include "java_class_ids.h"
#include <jni.h>

#include <aws/checksums/xxhash.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'jni_tls_ctx_options *' */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash64Compute(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jlong seed) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 8);

    struct aws_byte_cursor c_byte_array = aws_jni_byte_cursor_from_jbyteArray_critical_acquire(env, input);
    if (AWS_UNLIKELY(c_byte_array.ptr == NULL)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash64Compute: failed to pin input bytes");
        return NULL;
    }

    jbyteArray hash = NULL;
    int result = aws_xxhash64_compute(seed, c_byte_array, &hash_buffer);
    aws_jni_byte_cursor_from_jbyteArray_critical_release(env, input, c_byte_array);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash64Compute: failed to compute hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hash = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_byte_buf_clean_up(&hash_buffer);

    return hash;
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash364Compute(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jlong seed) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 8);

    struct aws_byte_cursor c_byte_array = aws_jni_byte_cursor_from_jbyteArray_critical_acquire(env, input);
    if (AWS_UNLIKELY(c_byte_array.ptr == NULL)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_64Compute: failed to pin input bytes");
        return NULL;
    }

    jbyteArray hash = NULL;
    int result = aws_xxhash3_64_compute(seed, c_byte_array, &hash_buffer);
    aws_jni_byte_cursor_from_jbyteArray_critical_release(env, input, c_byte_array);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_64Compute: failed to compute hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hash = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_byte_buf_clean_up(&hash_buffer);

    return hash;
}

JNIEXPORT jbyteArray JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash3128Compute(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray input,
    jlong seed) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_byte_buf hash_buffer;
    AWS_ZERO_STRUCT(hash_buffer);

    aws_byte_buf_init(&hash_buffer, aws_jni_get_allocator(), 16);

    struct aws_byte_cursor c_byte_array = aws_jni_byte_cursor_from_jbyteArray_critical_acquire(env, input);
    if (AWS_UNLIKELY(c_byte_array.ptr == NULL)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_128Compute: failed to pin input bytes");
        return NULL;
    }

    jbyteArray hash = NULL;
    int result = aws_xxhash3_128_compute(seed, c_byte_array, &hash_buffer);
    aws_jni_byte_cursor_from_jbyteArray_critical_release(env, input, c_byte_array);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash3_128Compute: failed to compute hash");
    } else {
        struct aws_byte_cursor hash_cursor = aws_byte_cursor_from_buf(&hash_buffer);
        hash = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_byte_buf_clean_up(&hash_buffer);

    return hash;
}

JNIEXPORT
jlong JNICALL
    Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash64Create(JNIEnv *env, jclass jni_class, jlong seed) {

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
jlong JNICALL
    Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash364Create(JNIEnv *env, jclass jni_class, jlong seed) {

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
jlong JNICALL
    Java_software_amazon_awssdk_crt_checksums_XXHash_xxHash3128Create(JNIEnv *env, jclass jni_class, jlong seed) {

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
    Java_software_amazon_awssdk_crt_checksums_XXHash_xxHashRelease(JNIEnv *env, jclass jni_class, jlong hash_ptr) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = (struct aws_xxhash *)hash_ptr;

    aws_xxhash_destroy(hash);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_checksums_XXHash_xxHashUpdate(
    JNIEnv *env,
    jclass jni_class,
    jlong hash_ptr,
    jbyteArray input) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_xxhash *hash = (struct aws_xxhash *)hash_ptr;

    struct aws_byte_cursor c_byte_array = aws_jni_byte_cursor_from_jbyteArray_critical_acquire(env, input);
    if (AWS_UNLIKELY(c_byte_array.ptr == NULL)) {
        aws_jni_throw_runtime_exception(env, "XXHash.xxHash64Compute: failed to pin input bytes");
    } else {
        if (aws_xxhash_update(hash, c_byte_array)) {
            aws_jni_throw_runtime_exception(env, "XXHash.xxHashUpdate: failed to update hash");
        }
    }

    aws_jni_byte_cursor_from_jbyteArray_critical_release(env, input, c_byte_array);
}

JNIEXPORT
jbyteArray JNICALL
    Java_software_amazon_awssdk_crt_checksums_XXHash_xxHashFinalize(JNIEnv *env, jclass jni_class, jlong hash_ptr) {

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
        hash_out = aws_jni_byte_array_from_cursor(env, &hash_cursor);
    }

    aws_byte_buf_clean_up(&hash_buffer);

    return hash_out;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

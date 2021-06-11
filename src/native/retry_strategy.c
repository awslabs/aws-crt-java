/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "java_class_ids.h"
#include <aws/io/retry_strategy.h>
#include <jni.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'jni_tls_ctx_options *' */
#        pragma warning(disable : 4221)
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_ExponentialBackoffRetryOptions_exponentialBackoffRetryOptionsNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_elg,
    jlong jni_max_retries,
    jlong jni_backoff_scale_factor_ms,
    jint jni_jitter_mode) {
    (void)env;
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_exponential_backoff_retry_options *options =
        (struct aws_exponential_backoff_retry_options *)aws_mem_calloc(
            allocator, 1, sizeof(struct aws_exponential_backoff_retry_options));

    if (!options) {
        aws_jni_throw_runtime_exception(
            env,
            "ExponentialBackoffRetryOptions.exponentialBackoffRetryOptionsNew: Unable to allocate new "
            "aws_exponential_backoff_retry_options");
        return (jlong)NULL;
    }

    AWS_ZERO_STRUCT(*options);
    options->el_group = (struct aws_event_loop_group *)jni_elg;
    options->max_retries = jni_max_retries;
    options->backoff_scale_factor_ms = (uint32_t)jni_backoff_scale_factor_ms;
    options->jitter_mode = jni_jitter_mode;

    return (jlong)options;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_ExponentialBackoffRetryOptions_exponentialBackoffRetryOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options) {
    (void)env;
    (void)jni_class;

    struct aws_exponential_backoff_retry_options *options = (struct aws_exponential_backoff_retry_options *)jni_options;

    if (!options) {
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, options);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_StandardRetryOptions_standardRetryOptionsNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_backoff_retry_options,
    jlong initial_bucket_capacity) {
    (void)env;
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_standard_retry_options *options =
        (struct aws_standard_retry_options *)aws_mem_calloc(allocator, 1, sizeof(struct aws_standard_retry_options));

    if (!options) {
        aws_jni_throw_runtime_exception(
            env, "StandardRetryOptions.standardRetryOptionsNew: Unable to allocate new aws_standard_retry_options");
        return (jlong)NULL;
    }

    AWS_ZERO_STRUCT(*options);

    struct aws_exponential_backoff_retry_options *backoff_retry_options =
        (struct aws_exponential_backoff_retry_options *)jni_backoff_retry_options;

    if (backoff_retry_options != NULL) {
        options->backoff_retry_options = *backoff_retry_options;
    }

    options->initial_bucket_capacity = initial_bucket_capacity;
    return (jlong)options;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_StandardRetryOptions_standardRetryOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options) {
    (void)env;
    (void)jni_class;

    struct aws_standard_retry_options *options = (struct aws_standard_retry_options *)jni_options;

    if (!options) {
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, options);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

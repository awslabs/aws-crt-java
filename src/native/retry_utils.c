/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "java_class_ids.h"
#include <aws/io/retry_strategy.h>
#include <inttypes.h>
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

int aws_exponential_backoff_retry_options_from_java(
    JNIEnv *env,
    jobject jni_backoff_retry_options,
    struct aws_exponential_backoff_retry_options *backoff_retry_options) {

    struct aws_event_loop_group *el_group = NULL;

    jobject jni_el_group = (*env)->GetObjectField(
        env, jni_backoff_retry_options, exponential_backoff_retry_options_properties.el_group_field_id);

    if (jni_el_group != NULL) {
        el_group = (struct aws_event_loop_group *)(*env)->CallLongMethod(
            env, jni_el_group, cleanable_crt_resource_properties.get_native_handle_method_id);
    }

    jlong jni_max_retries = (*env)->GetLongField(
        env, jni_backoff_retry_options, exponential_backoff_retry_options_properties.max_retries_field_id);

    jlong jni_backoff_scale_factor_ms = (*env)->GetLongField(
        env, jni_backoff_retry_options, exponential_backoff_retry_options_properties.backoff_scale_factor_ms_field_id);

    jobject jni_jitter_mode_obj = (*env)->GetObjectField(
        env, jni_backoff_retry_options, exponential_backoff_retry_options_properties.jitter_mode_field_id);

    jint jni_jitter_mode = (*env)->GetIntField(
        env, jni_jitter_mode_obj, exponential_backoff_retry_options_properties.jitter_mode_value_field_id);

    if (jni_max_retries < 0 || (uint64_t)jni_max_retries > SIZE_MAX) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        aws_jni_throw_runtime_exception(
            env,
            "ExponentialBackoffRetryOptions.exponentialBackoffRetryOptionsNew: Max-Retries value must be between 0 and "
            "%" PRIu64,
            (uint64_t)SIZE_MAX);
        return AWS_OP_ERR;
    }

    if (jni_backoff_scale_factor_ms < 0 || jni_backoff_scale_factor_ms > UINT_MAX) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        aws_jni_throw_runtime_exception(
            env,
            "ExponentialBackoffRetryOptions.exponentialBackoffRetryOptionsNew: Backoff-Scale-Factor-MS must be between "
            "0 and %u",
            UINT_MAX);
        return AWS_OP_ERR;
    }

    AWS_ZERO_STRUCT(*backoff_retry_options);
    backoff_retry_options->el_group = el_group;
    backoff_retry_options->max_retries = (size_t)jni_max_retries;
    backoff_retry_options->backoff_scale_factor_ms = (uint32_t)jni_backoff_scale_factor_ms;
    backoff_retry_options->jitter_mode = jni_jitter_mode;

    return AWS_OP_SUCCESS;
}

int aws_standard_retry_options_from_java(
    JNIEnv *env,
    jobject jni_standard_retry_options,
    struct aws_standard_retry_options *standard_retry_options) {

    jobject jni_backoff_retry_options = (*env)->GetObjectField(
        env, jni_standard_retry_options, standard_retry_options_properties.backoff_retry_options_field_id);
    jlong jni_initial_bucket_capacity = (*env)->GetLongField(
        env, jni_standard_retry_options, standard_retry_options_properties.initial_bucket_capacity_field_id);

    if (jni_initial_bucket_capacity < 0 || (uint64_t)jni_initial_bucket_capacity > SIZE_MAX) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        aws_jni_throw_runtime_exception(
            env,
            "StandardRetryOptions.standardRetryOptionsNew: Initial-Bucket-Capacity value must be between 0 and "
            "%" PRIu64,
            (uint64_t)SIZE_MAX);
        return AWS_OP_ERR;
    }

    AWS_ZERO_STRUCT(*standard_retry_options);

    if (jni_backoff_retry_options != NULL) {
        aws_exponential_backoff_retry_options_from_java(
            env, jni_backoff_retry_options, &standard_retry_options->backoff_retry_options);
    }

    standard_retry_options->initial_bucket_capacity = (size_t)jni_initial_bucket_capacity;

    return AWS_OP_SUCCESS;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

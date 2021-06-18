/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "java_class_ids.h"
#include "retry_utils.h"
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

static jmethodID s_getCopiedToNativeMethodId(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/test/RetryOptionsTest$CopyToNativeCallback");
    AWS_FATAL_ASSERT(cls);

    jmethodID on_copied_to_native_method_id = (*env)->GetMethodID(env, cls, "onCopiedToNative", "(J)V");
    AWS_FATAL_ASSERT(on_copied_to_native_method_id);

    return on_copied_to_native_method_id;
}

JNIEXPORT jboolean JNICALL
    Java_software_amazon_awssdk_crt_test_RetryOptionsTest_copyExponentialBackoffRetryOptionsToNative(
        JNIEnv *env,
        jclass jni_class,
        jobject jni_retry_options,
        jobject jni_callback) {
    (void)jni_class;

    jmethodID on_copied_to_native_method_id = s_getCopiedToNativeMethodId(env);

    struct aws_exponential_backoff_retry_options retry_options;

    if (aws_exponential_backoff_retry_options_from_java(env, jni_retry_options, &retry_options)) {
        return false;
    }

    (*env)->CallVoidMethod(env, jni_callback, on_copied_to_native_method_id, (jlong)&retry_options);

    if (aws_jni_check_and_clear_exception(env)) {
        return false;
    }

    return true;
}

JNIEXPORT jboolean JNICALL Java_software_amazon_awssdk_crt_test_RetryOptionsTest_copyStandardRetryOptionsToNative(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_retry_options,
    jobject jni_callback) {
    (void)jni_class;

    jmethodID on_copied_to_native_method_id = s_getCopiedToNativeMethodId(env);

    struct aws_standard_retry_options retry_options;

    if (aws_standard_retry_options_from_java(env, jni_retry_options, &retry_options)) {
        return false;
    }

    (*env)->CallVoidMethod(env, jni_callback, on_copied_to_native_method_id, (jlong)&retry_options);

    if (aws_jni_check_and_clear_exception(env)) {
        return false;
    }

    return true;
}

uint64_t s_test_generate_random_mock(void) {
    return (uint64_t)0;
}

struct compare_test_case {
    struct aws_standard_retry_options options;
    bool expected_standard_retry_result;
    bool expected_backoff_retry_result;
};

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_test_RetryOptionsTest_nativeTestOptionsCompare(JNIEnv *env, jclass jni_class) {
    (void)jni_class;

    struct aws_standard_retry_options base_standard_retry_options = {
        .backoff_retry_options =
            {
                .max_retries = 12,
                .backoff_scale_factor_ms = 345,
                .jitter_mode = AWS_EXPONENTIAL_BACKOFF_JITTER_DECORRELATED,
                .generate_random = s_test_generate_random_mock,
            },
        .initial_bucket_capacity = 789,
    };

    struct compare_test_case test_cases[6];

    test_cases[0].options = base_standard_retry_options;
    test_cases[0].expected_standard_retry_result = true;
    test_cases[0].expected_backoff_retry_result = true;

    test_cases[1].options = base_standard_retry_options;
    test_cases[1].options.backoff_retry_options.max_retries = 0;
    test_cases[1].expected_standard_retry_result = false;
    test_cases[1].expected_backoff_retry_result = false;

    test_cases[2].options = base_standard_retry_options;
    test_cases[2].options.backoff_retry_options.backoff_scale_factor_ms = 0;
    test_cases[2].expected_standard_retry_result = false;
    test_cases[2].expected_backoff_retry_result = false;

    test_cases[3].options = base_standard_retry_options;
    test_cases[3].options.backoff_retry_options.jitter_mode = AWS_EXPONENTIAL_BACKOFF_JITTER_DEFAULT;
    test_cases[3].expected_standard_retry_result = false;
    test_cases[3].expected_backoff_retry_result = false;

    test_cases[4].options = base_standard_retry_options;
    test_cases[4].options.backoff_retry_options.generate_random = NULL;
    test_cases[4].expected_standard_retry_result = false;
    test_cases[4].expected_backoff_retry_result = false;

    test_cases[5].options = base_standard_retry_options;
    test_cases[5].options.initial_bucket_capacity = 0;
    test_cases[5].expected_standard_retry_result = false;
    test_cases[5].expected_backoff_retry_result = true;

    for (size_t i = 0; i < sizeof(test_cases) / sizeof(test_cases[0]); ++i) {
        struct compare_test_case *test_case = &test_cases[i];

        if (aws_standard_retry_options_equals(&base_standard_retry_options, &test_case->options) !=
            test_case->expected_standard_retry_result) {
            aws_raise_error(AWS_ERROR_UNKNOWN);
            aws_jni_throw_runtime_exception(env, "Test case %d for standard-retry-options failed", (int)i);
        }

        if (aws_exponential_backoff_retry_options_equals(
                &base_standard_retry_options.backoff_retry_options, &test_case->options.backoff_retry_options) !=
            test_case->expected_backoff_retry_result) {
            aws_raise_error(AWS_ERROR_UNKNOWN);
            aws_jni_throw_runtime_exception(env, "Test case %d for exponential-backoff-retry-options failed", (int)i);
        }
    }
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_test_RetryOptionsTest_nativeTestCopyToNative(JNIEnv *env, jclass jni_class) {
    (void)jni_class;

    struct aws_standard_retry_options expected_standard_retry_options = {
        .backoff_retry_options =
            {
                .max_retries = 12,
                .backoff_scale_factor_ms = 345,
                .jitter_mode = AWS_EXPONENTIAL_BACKOFF_JITTER_DECORRELATED,
            },
        .initial_bucket_capacity = 789,
    };

    /* Setup a Java Jitter Mode */
    jfieldID decorrelatedJitterModeFieldId = (*env)->GetStaticFieldID(
        env,
        exponential_backoff_retry_options_properties.jitter_mode_class,
        "Decorrelated",
        "Lsoftware/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions$JitterMode;");
    jobject decorrelatedJitterMode = (*env)->GetStaticObjectField(
        env, exponential_backoff_retry_options_properties.jitter_mode_class, decorrelatedJitterModeFieldId);

    /* Setup Exponential Backoff Retry Options */
    jobject jni_backoff_retry_options = (*env)->NewObject(
        env,
        exponential_backoff_retry_options_properties.exponential_backoff_retry_options_class,
        exponential_backoff_retry_options_properties.exponential_backoff_retry_options_constructor_method_id);

    (*env)->SetObjectField(
        env, jni_backoff_retry_options, exponential_backoff_retry_options_properties.el_group_field_id, NULL);

    (*env)->SetLongField(
        env,
        jni_backoff_retry_options,
        exponential_backoff_retry_options_properties.max_retries_field_id,
        (jlong)expected_standard_retry_options.backoff_retry_options.max_retries);

    (*env)->SetLongField(
        env,
        jni_backoff_retry_options,
        exponential_backoff_retry_options_properties.backoff_scale_factor_ms_field_id,
        (jlong)expected_standard_retry_options.backoff_retry_options.backoff_scale_factor_ms);

    (*env)->SetObjectField(
        env,
        jni_backoff_retry_options,
        exponential_backoff_retry_options_properties.jitter_mode_field_id,
        decorrelatedJitterMode);

    struct aws_exponential_backoff_retry_options backoff_retry_options;

    if (aws_exponential_backoff_retry_options_from_java(env, jni_backoff_retry_options, &backoff_retry_options)) {
        return;
    }

    if (!aws_exponential_backoff_retry_options_equals(
            &backoff_retry_options, &expected_standard_retry_options.backoff_retry_options)) {
        aws_raise_error(AWS_ERROR_UNKNOWN);
        aws_jni_throw_runtime_exception(
            env, "Copied java fields do not match expected exponential-backoff-retry-options.");
        return;
    }

    /* Setup Standard Retry Options */
    jobject jni_standard_retry_options = (*env)->NewObject(
        env,
        standard_retry_options_properties.standard_retry_options_class,
        standard_retry_options_properties.standard_retry_options_constructor_method_id);

    (*env)->SetObjectField(
        env,
        jni_standard_retry_options,
        standard_retry_options_properties.backoff_retry_options_field_id,
        jni_backoff_retry_options);
    (*env)->SetLongField(
        env, jni_standard_retry_options, standard_retry_options_properties.initial_bucket_capacity_field_id, 789);

    struct aws_standard_retry_options standard_retry_options;

    if (aws_standard_retry_options_from_java(env, jni_standard_retry_options, &standard_retry_options)) {
        return;
    }

    if (!aws_standard_retry_options_equals(&standard_retry_options, &expected_standard_retry_options)) {
        aws_raise_error(AWS_ERROR_UNKNOWN);
        aws_jni_throw_runtime_exception(env, "Copied java fields do not match expected standard-retry-options.");
        return;
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

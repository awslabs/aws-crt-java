#ifndef AWS_JNI_RETRY_UTILS_H
#define AWS_JNI_RETRY_UTILS_H

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

struct aws_exponential_backoff_retry_options;
struct aws_standard_retry_options;

int aws_exponential_backoff_retry_options_from_java(
    JNIEnv *env,
    jobject jni_backoff_retry_options,
    struct aws_exponential_backoff_retry_options *backoff_retry_options);

int aws_standard_retry_options_from_java(
    JNIEnv *env,
    jobject jni_standard_retry_options,
    struct aws_standard_retry_options *standard_retry_options);

bool aws_exponential_backoff_retry_options_equals(
    const struct aws_exponential_backoff_retry_options *options,
    const struct aws_exponential_backoff_retry_options *expected_options);

bool aws_standard_retry_options_equals(
    const struct aws_standard_retry_options *options,
    const struct aws_standard_retry_options *expected_options);

#endif /* AWS_JNI_RETRY_STRATEGY_H */

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_AWS_SIGNING_H
#define AWS_JNI_CRT_AWS_SIGNING_H

#include <aws/common/byte_buf.h>
#include <jni.h>

struct aws_signing_config_aws;

struct aws_signing_config_data {
    JavaVM *jvm;
    struct aws_string *region;
    struct aws_string *service;
    struct aws_string *signed_body_value;

    jobject java_sign_header_predicate;
    struct aws_credentials *credentials;
    jobject java_credentials_provider;
};

/* Initialize the native `config` from Java Object and Keep the required data around with `config_data`. You need to
 * clean up the `config_data` after the signing config is not used anymore */
int aws_build_signing_config(
    JNIEnv *env,
    jobject java_config,
    struct aws_signing_config_data *config_data,
    struct aws_signing_config_aws *config);

void aws_signing_config_data_clean_up(struct aws_signing_config_data *data, JNIEnv *env);

#endif /* AWS_JNI_CRT_AWS_SIGNING_H */

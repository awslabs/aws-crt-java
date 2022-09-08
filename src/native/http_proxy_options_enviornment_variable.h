/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_HTTP_PROXY_ENVIRONMENT_VARIABLE_OPTIONS_H
#define AWS_JNI_CRT_HTTP_PROXY_ENVIRONMENT_VARIABLE_OPTIONS_H

#include <jni.h>

struct proxy_env_var_settings;
struct aws_tls_connection_options;
struct aws_tls_ctx;

void aws_http_proxy_environment_variable_options_jni_init(
    struct proxy_env_var_settings *options,
    jint environment_variable_proxy_connection_type,
    struct aws_tls_connection_options *tls_options,
    jint environment_variable_setting,
    struct aws_tls_ctx *proxy_tls_ctx);

void aws_http_proxy_environment_variable_options_jni_clean_up(struct proxy_env_var_settings *options);

#endif /* AWS_JNI_CRT_HTTP_PROXY_ENVIRONMENT_VARIABLE_OPTIONS_H */

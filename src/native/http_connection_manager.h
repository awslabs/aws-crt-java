/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_HTTP_CONNECTION_MANAGER_H
#define AWS_JNI_CRT_HTTP_CONNECTION_MANAGER_H

#include <jni.h>

struct aws_http_proxy_options;
struct aws_tls_connection_options;
struct aws_tls_ctx;

void aws_http_proxy_options_jni_init(
    JNIEnv *env,
    struct aws_http_proxy_options *options,
    jint proxy_connection_type,
    struct aws_tls_connection_options *tls_options,
    jbyteArray proxy_host,
    uint16_t proxy_port,
    jbyteArray proxy_authorization_username,
    jbyteArray proxy_authorization_password,
    int proxy_authorization_type,
    struct aws_tls_ctx *proxy_tls_ctx);

void aws_http_proxy_options_jni_clean_up(
    JNIEnv *env,
    struct aws_http_proxy_options *options,
    jbyteArray proxy_host,
    jbyteArray proxy_authorization_username,
    jbyteArray proxy_authorization_password);

#endif /* AWS_JNI_CRT_HTTP_CONNECTION_MANAGER_H */

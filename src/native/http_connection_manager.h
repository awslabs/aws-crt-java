/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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

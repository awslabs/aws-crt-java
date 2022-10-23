/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_HTTP_CONNECTION_MANAGER_H
#define AWS_JNI_CRT_HTTP_CONNECTION_MANAGER_H

#include <jni.h>

struct aws_http_connection;
struct aws_http_connection_manager;
struct aws_http_proxy_options;
struct aws_tls_connection_options;
struct aws_tls_ctx;

struct aws_http_connection_binding {
    JavaVM *jvm;
    jobject java_acquire_connection_future;
    struct aws_http_connection_manager *manager;
    struct aws_http_connection *connection;
};

#endif /* AWS_JNI_CRT_HTTP_CONNECTION_MANAGER_H */

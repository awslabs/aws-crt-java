/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/http/proxy.h>
#include <aws/io/tls_channel_handler.h>

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
    struct aws_tls_ctx *proxy_tls_ctx) {

    struct aws_allocator *allocator = aws_jni_get_allocator();

    options->connection_type = proxy_connection_type;
    options->port = proxy_port;
    options->auth_type = proxy_authorization_type;

    if (proxy_host != NULL) {
        options->host = aws_jni_byte_cursor_from_jbyteArray_acquire(env, proxy_host);
    }

    if (proxy_authorization_username != NULL) {
        options->auth_username = aws_jni_byte_cursor_from_jbyteArray_acquire(env, proxy_authorization_username);
    }

    if (proxy_authorization_password != NULL) {
        options->auth_password = aws_jni_byte_cursor_from_jbyteArray_acquire(env, proxy_authorization_password);
    }

    if (proxy_tls_ctx != NULL) {
        aws_tls_connection_options_init_from_ctx(tls_options, proxy_tls_ctx);
        aws_tls_connection_options_set_server_name(tls_options, allocator, &options->host);
        options->tls_options = tls_options;
    }
}

void aws_http_proxy_options_jni_clean_up(
    JNIEnv *env,
    struct aws_http_proxy_options *options,
    jbyteArray proxy_host,
    jbyteArray proxy_authorization_username,
    jbyteArray proxy_authorization_password) {

    if (options->host.ptr != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, proxy_host, options->host);
    }

    if (options->auth_username.ptr != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, proxy_authorization_username, options->auth_username);
    }

    if (options->auth_password.ptr != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, proxy_authorization_password, options->auth_password);
    }

    if (options->tls_options != NULL) {
        aws_tls_connection_options_clean_up((struct aws_tls_connection_options *)options->tls_options);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

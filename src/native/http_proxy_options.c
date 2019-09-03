/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

#include <jni.h>

#include <aws/http/connection.h>

#include "crt.h"

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

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsNew(JNIEnv *env, jclass jni_class) {
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_http_proxy_options *options =
        (struct aws_http_proxy_options *)aws_mem_calloc(allocator, 1, sizeof(struct aws_http_proxy_options));
    if (!options) {
        aws_jni_throw_runtime_exception(
            env, "HttpProxyOptions.httpProxyOptionsNew: Unable to allocate new aws_http_proxy_options");
        return (jlong)NULL;
    }
    AWS_ZERO_STRUCT(*options);
    return (jlong)options;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, options);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsSetAuthorizationType(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jint jni_authorization_type) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    options->auth_type = (enum aws_http_proxy_authorization_type)jni_authorization_type;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsSetPort(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jint jni_port) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    options->port = jni_port;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsSetHost(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jstring jni_host) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    options->host = aws_jni_byte_cursor_from_jstring(env, jni_host);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsSetAuthorizationUsername(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jstring jni_username) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    options->auth_username = aws_jni_byte_cursor_from_jstring(env, jni_username);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsSetAuthorizationPassword(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jstring jni_password) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    options->auth_password = aws_jni_byte_cursor_from_jstring(env, jni_password);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpProxyOptions_httpProxyOptionsSetTlsConnectionOptions(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    klong jni_tls_connection_options) {
    (void)env;
    (void)jni_class;
    struct aws_http_proxy_options *options = (struct aws_http_proxy_options *)jni_options;
    if (!options) {
        return;
    }

    options->tls_connection_options = (struct aws_tls_connection_options *)jni_tls_connection_options;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

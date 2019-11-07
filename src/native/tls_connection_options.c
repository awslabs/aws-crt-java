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

#include <aws/io/tls_channel_handler.h>

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
jlong JNICALL Java_software_amazon_awssdk_crt_io_TlsConnectionOptions_tlsConnectionOptionsNew(
    JNIEnv *env,
    jclass jni_class,
    jlong native_context) {
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_tls_connection_options *options =
        (struct aws_tls_connection_options *)aws_mem_calloc(allocator, 1, sizeof(struct aws_tls_connection_options));
    if (!options) {
        aws_jni_throw_runtime_exception(
            env, "TlsConnectionOptions.tlsConnectionOptionsNew: Unable to allocate new aws_tls_connection_options");
        return (jlong)NULL;
    }
    AWS_ZERO_STRUCT(*options);
    struct aws_tls_ctx *ctx = (struct aws_tls_ctx *)native_context;
    aws_tls_connection_options_init_from_ctx(options, ctx);

    return (jlong)options;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsConnectionOptions_tlsConnectionOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options) {
    (void)env;
    (void)jni_class;
    struct aws_tls_connection_options *options = (struct aws_tls_connection_options *)jni_options;
    if (!options) {
        return;
    }

    aws_tls_connection_options_clean_up(options);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, options);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsConnectionOptions_tlsConnectionOptionsSetServerName(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jstring jni_server_name) {
    (void)env;
    (void)jni_class;
    struct aws_tls_connection_options *options = (struct aws_tls_connection_options *)jni_options;
    if (!options) {
        return;
    }

    struct aws_byte_cursor server_name_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_server_name);
    struct aws_allocator *allocator = aws_jni_get_allocator();
    if (aws_tls_connection_options_set_server_name(options, allocator, &server_name_cursor)) {
        aws_jni_throw_runtime_exception(
            env, "TlsConnectionOptions.tlsConnectionOptionsSetServerName: Unable to set server name");
    }
    aws_jni_byte_cursor_from_jstring_release(env, jni_server_name, server_name_cursor);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsConnectionOptions_tlsConnectionOptionsSetAlpnList(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jstring jni_alpn_list) {
    (void)env;
    (void)jni_class;
    struct aws_tls_connection_options *options = (struct aws_tls_connection_options *)jni_options;
    if (!options) {
        return;
    }

    struct aws_byte_cursor alpn_list_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_alpn_list);
    struct aws_allocator *allocator = aws_jni_get_allocator();
    if (aws_tls_connection_options_set_alpn_list(options, allocator, (char *)alpn_list_cursor.ptr)) {
        aws_jni_throw_runtime_exception(
            env, "TlsConnectionOptions.tlsConnectionOptionsSetAlpnList: Unable to set alpn list");
    }
    aws_jni_byte_cursor_from_jstring_release(env, jni_alpn_list, alpn_list_cursor);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

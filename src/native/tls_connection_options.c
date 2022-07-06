/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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
    jstring jni_alpn,
    jstring jni_server_name,
    jint jni_timeout_ms,
    jlong native_context) {
    (void)jni_class;
    struct aws_tls_ctx *ctx = (struct aws_tls_ctx *)native_context;
    if (!ctx) {
        aws_jni_throw_illegal_argument_exception(env, "TlsContext cannot be null for TlsConnectionOptions");
        return (jlong)0;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_tls_connection_options *options =
        (struct aws_tls_connection_options *)aws_mem_calloc(allocator, 1, sizeof(struct aws_tls_connection_options));

    aws_tls_connection_options_init_from_ctx(options, ctx);
    if (jni_alpn) {
        const char *alpn_chars = (*env)->GetStringUTFChars(env, jni_alpn, NULL);
        if (!alpn_chars) {
            aws_jni_throw_runtime_exception(env, "Failed to get alpnList string");
            goto on_error;
        }
        int err = aws_tls_connection_options_set_alpn_list(options, allocator, alpn_chars);
        (*env)->ReleaseStringUTFChars(env, jni_alpn, alpn_chars);
        if (err) {
            goto on_error;
        }
    }
    if (jni_server_name) {
        struct aws_byte_cursor server_name_cur = aws_jni_byte_cursor_from_jstring_acquire(env, jni_server_name);
        int err = aws_tls_connection_options_set_server_name(options, allocator, &server_name_cur);
        aws_jni_byte_cursor_from_jstring_release(env, jni_server_name, server_name_cur);
        if (err) {
            goto on_error;
        }
    }
    if (jni_timeout_ms) {
        options->timeout_ms = jni_timeout_ms;
    }

    return (jlong)options;
on_error:
    aws_tls_connection_options_clean_up(options);
    aws_mem_release(allocator, options);
    return (jlong)0;
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

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

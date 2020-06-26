/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/common/string.h>
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
jlong JNICALL
    Java_software_amazon_awssdk_crt_io_TlsContext_tlsContextNew(JNIEnv *env, jclass jni_class, jlong jni_options) {
    (void)jni_class;
    struct aws_tls_ctx_options *options = (struct aws_tls_ctx_options *)jni_options;
    if (!options) {
        aws_jni_throw_runtime_exception(env, "TlsContext.tls_ctx_new: Invalid TlsOptions");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_tls_ctx *tls_ctx = aws_tls_client_ctx_new(allocator, options);
    if (!tls_ctx) {
        aws_jni_throw_runtime_exception(env, "TlsContext.tls_ctx_new: Failed to create new aws_tls_ctx");
        return (jlong)NULL;
    }
    return (jlong)tls_ctx;
}

JNIEXPORT
void JNICALL
    Java_software_amazon_awssdk_crt_io_TlsContext_tlsContextDestroy(JNIEnv *env, jclass jni_class, jlong jni_ctx) {
    (void)env;
    (void)jni_class;
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_ctx;
    if (!tls_ctx) {
        return;
    }

    aws_tls_ctx_destroy(tls_ctx);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

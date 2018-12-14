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

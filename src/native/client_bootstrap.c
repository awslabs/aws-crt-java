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

#include <aws/io/channel_bootstrap.h>

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
jlong JNICALL Java_software_amazon_awssdk_crt_io_ClientBootstrap_clientBootstrapNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_elg) {
    (void)jni_class;
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)jni_elg;
    if (!elg) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_client_bootstrap *bootstrap = aws_client_bootstrap_new(allocator, elg, NULL, NULL);
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(
            env, "ClientBootstrap.client_bootstrap_new: Unable to allocate new aws_client_bootstrap");
        return (jlong)NULL;
    }

    return (jlong)bootstrap;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_ClientBootstrap_clientBootstrapDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_bootstrap) {
    (void)env;
    (void)jni_class;
    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)jni_bootstrap;
    if (!bootstrap) {
        return;
    }

    aws_client_bootstrap_destroy(bootstrap);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

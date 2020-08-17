/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/io/channel_bootstrap.h>

#include "crt.h"
#include "java_class_ids.h"

#if _MSC_VER
#    pragma warning(disable : 4204) /* non-constant aggregate initializer */
#endif

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
jlong JNICALL Java_software_amazon_awssdk_crt_io_ServerBootstrap_serverBootstrapNew(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_bootstrap,
    jlong jni_elg) {
    (void)jni_class;
    /* we're going to need this at some point. Keep it here until we do. */
    (void)jni_bootstrap;
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)jni_elg;

    if (!elg) {
        aws_jni_throw_runtime_exception(env, "ServerBootstrap.server_bootstrap_new: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_server_bootstrap *bootstrap = aws_server_bootstrap_new(allocator, elg);
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(
            env, "ServerBootstrap.server_bootstrap_new: Unable to allocate new aws_server_bootstrap");
        return (jlong)NULL;
    }

    return (jlong)bootstrap;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_ServerBootstrap_serverBootstrapDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_bootstrap) {
    (void)env;
    (void)jni_class;
    struct aws_server_bootstrap *bootstrap = (struct aws_server_bootstrap *)jni_bootstrap;
    if (!bootstrap) {
        return;
    }

    aws_server_bootstrap_release(bootstrap);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

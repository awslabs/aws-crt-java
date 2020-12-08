/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/io/host_resolver.h>

#include "crt.h"

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'aws_event_loop_group *' */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_io_HostResolver_hostResolverNew(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_elg,
    jint max_entries) {

    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_event_loop_group *el_group = (struct aws_event_loop_group *)jni_elg;

    if (!el_group) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverNew: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    if (max_entries <= 0) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverNew: max_entries must be >= 0");
        return (jlong)NULL;
    }

    struct aws_host_resolver_default_options resolver_options = {
        .max_entries = max_entries,
        .el_group = el_group,
    };

    struct aws_host_resolver *resolver = aws_host_resolver_new_default(allocator, &resolver_options);
    if (resolver == NULL) {
        aws_jni_throw_runtime_exception(env, "aws_host_resolver_new_default failed");
        return (jlong)NULL;
    }

    return (jlong)resolver;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_HostResolver_hostResolverRelease(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_host_resolver) {

    (void)jni_class;

    struct aws_host_resolver *resolver = (struct aws_host_resolver *)jni_host_resolver;
    if (!resolver) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverRelease: Invalid aws_host_resolver");
        return;
    }

    aws_host_resolver_release(resolver);

    return;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

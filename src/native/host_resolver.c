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

#include <aws/io/host_resolver.h>

#include "crt.h"
#include "java_class_ids.h"

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

struct host_resolver_binding {
    JavaVM *jvm;
    struct aws_host_resolver *resolver;
    jobject java_event_loop_group;
};

static void s_host_resolver_binding_destroy(JNIEnv *env, struct host_resolver_binding *binding) {
    if (binding == NULL) {
        return;
    }

    if (binding->java_event_loop_group != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_event_loop_group);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static struct host_resolver_binding *s_host_resolver_binding_new(
    JNIEnv *env,
    jobject java_event_loop_group,
    struct aws_host_resolver *resolver) {
    struct host_resolver_binding *binding =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct host_resolver_binding));
    if (binding == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->resolver = resolver;
    binding->java_event_loop_group = (*env)->NewGlobalRef(env, java_event_loop_group);
    if (binding->java_event_loop_group == NULL) {
        goto on_error;
    }

    return binding;

on_error:

    s_host_resolver_binding_destroy(env, binding);

    return NULL;
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_io_HostResolver_hostResolverNew(
    JNIEnv *env,
    jclass jni_class,
    jobject java_elg,
    jlong jni_elg_handle,
    jint max_entries) {

    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_host_resolver *resolver = NULL;
    struct host_resolver_binding *binding = NULL;

    if (java_elg == NULL) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverNew: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    struct aws_event_loop_group *el_group = (struct aws_event_loop_group *)jni_elg_handle;
    if (el_group == NULL) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverNew: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    if (max_entries <= 0) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverNew: max_entries must be >= 0");
        return (jlong)NULL;
    }

    resolver = aws_mem_calloc(allocator, 1, sizeof(struct aws_host_resolver));
    AWS_FATAL_ASSERT(resolver);

    if (aws_host_resolver_init_default(resolver, allocator, (size_t)max_entries, el_group)) {
        aws_jni_throw_runtime_exception(env, "aws_host_resolver_init_default failed");
        goto error;
    }

    binding = s_host_resolver_binding_new(env, java_elg, resolver);
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "HostResolver_hostResolverNew failed to create binding");
        goto error;
    }

    return (jlong)binding;

error:

    if (resolver != NULL) {
        aws_host_resolver_clean_up(resolver);
        aws_mem_release(allocator, resolver);
    }

    if (binding != NULL) {
        s_host_resolver_binding_destroy(env, binding);
    }

    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_HostResolver_hostResolverRelease(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_host_resolver_binding) {

    (void)jni_class;

    struct host_resolver_binding *binding = (struct host_resolver_binding *)jni_host_resolver_binding;

    if (!binding) {
        aws_jni_throw_runtime_exception(env, "HostResolver.hostResolverRelease: Invalid aws_host_resolver binding");
        return;
    }

    aws_host_resolver_clean_up(binding->resolver);

    s_host_resolver_binding_destroy(env, binding);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

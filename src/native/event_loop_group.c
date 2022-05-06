/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/io/event_loop.h>
#include <aws/io/logging.h>

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
jlong JNICALL
    Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupNew(JNIEnv *env, jclass jni_elg, jint num_threads) {
    (void)jni_elg;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_event_loop_group *elg = aws_event_loop_group_new_default(allocator, (uint16_t)num_threads, NULL);
    if (elg == NULL) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: aws_event_loop_group_new_default failed");
        goto on_error;
    }

    return (jlong)elg;

on_error:

    return (jlong)NULL;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupNewPinnedToCpuGroup(
    JNIEnv *env,
    jclass jni_elg,
    jint cpu_group,
    jint num_threads) {
    (void)jni_elg;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_event_loop_group *elg = aws_event_loop_group_new_default_pinned_to_cpu_group(
        allocator, (uint16_t)num_threads, (uint16_t)cpu_group, NULL);
    if (elg == NULL) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: eventLoopGroupNewPinnedToCpuGroup failed");
        goto on_error;
    }

    return (jlong)elg;

on_error:

    return (jlong)NULL;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupDestroy(
    JNIEnv *env,
    jclass jni_elg,
    jlong elg_addr) {
    (void)jni_elg;
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_addr;
    if (!elg) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.eventLoopGroupDestroy: instance should be non-null at release time");
        return;
    }

    aws_event_loop_group_release(elg);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

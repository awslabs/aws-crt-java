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

#include <aws/io/event_loop.h>

#include "crt.h"

#if UINTPTR_MAX == 0xffffffff
#    ifdef __clang__
#        pragma clang diagnostic push
#        pragma clang diagnostic ignored "-Werror=pointer-to-int-cast"
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Werror=pointer-to-int-cast"
#    endif
#endif

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_EventLoopGroup_event_1loop_1group_1new(
    JNIEnv *env,
    jclass jni_elg,
    jint num_threads) {

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_event_loop_group *elg = aws_mem_acquire(allocator, sizeof(struct aws_event_loop_group));
    if (!elg) {
        aws_jni_throw_runtime_exception(
            env,
            "EventLoopGroup.event_loop_group_new: aws_mem_acquire failed, unable to allocate new aws_event_loop_group");
        return (jlong)NULL;
    }
    int result = aws_event_loop_group_default_init(elg, allocator, (uint16_t)num_threads);
    if (result != AWS_OP_SUCCESS) {
        aws_event_loop_group_clean_up(elg);
        aws_mem_release(allocator, elg);
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: aws_event_loop_group_default_init failed");
        return (jlong)NULL;
    }

    return (jlong)elg;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_EventLoopGroup_event_1loop_1group_1clean_1up(
    JNIEnv *env,
    jclass jni_elg,
    jlong elg_addr) {
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_addr;
    if (!elg) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_clean_up: instance should be non-null at clean_up time");
        return;
    }

    aws_event_loop_group_clean_up(elg);
    aws_mem_release(elg->allocator, elg);
}

#if UINTPTR_MAX == 0xffffffff
#    ifdef __clang__
#        pragma clang diagnostic pop
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

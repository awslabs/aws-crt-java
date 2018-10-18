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
#include <aws/common/system_info.h>

#include "crt.h"

/* cached id for looking up EventLoopGroup._elg */
static jfieldID s_elg_field = 0;

/* push the elg into the EventLoopGroup as _elg */
void aws_jni_event_loop_group_pack(JNIEnv *env, jobject jni_elg, struct aws_event_loop_group* elg) {
    if (AWS_UNLIKELY(!s_elg_field)) {
        jclass elg_class = (*env)->GetObjectClass(env, jni_elg);
        s_elg_field = (*env)->GetFieldID(env, elg_class, "_elg", "J");
    }

    (*env)->SetLongField(env, jni_elg, s_elg_field, (jlong)elg);
}

/* pull _elg out of an EventLoopGroup */
struct aws_event_loop_group *aws_jni_event_loop_group_unpack(JNIEnv *env, jobject jni_elg) {
    if (AWS_UNLIKELY(!s_elg_field)) {
        jclass elg_class = (*env)->GetObjectClass(env, jni_elg);
        s_elg_field = (*env)->GetFieldID(env, elg_class, "_elg", "J");
    }

    jlong elg_value = (*env)->GetLongField(env, jni_elg, s_elg_field);
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_value;
    return elg;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_EventLoopGroup_init(JNIEnv *env, jobject jni_elg, jint num_threads) {
    struct aws_event_loop_group *elg = aws_jni_event_loop_group_unpack(env, jni_elg);
    assert(!elg && "EventLoopGroup._elg should be 0 initialized in the constructor");

    struct aws_allocator *allocator = aws_jni_get_allocator();
    elg = aws_mem_acquire(allocator, sizeof(struct aws_event_loop_group));
    int result = aws_event_loop_group_default_init(elg, allocator, num_threads);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "aws_event_loop_group_default_init failed");
        return;
    }

    aws_jni_event_loop_group_pack(env, jni_elg, elg);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_EventLoopGroup_clean_1up(JNIEnv *env, jobject jni_elg) {
    struct aws_event_loop_group *elg = aws_jni_event_loop_group_unpack(env, jni_elg);
    if (!elg) {
        aws_jni_throw_runtime_exception(env, "EventLoopGroup._elg should be non-zero at clean_up time");
        return;
    }

    aws_event_loop_group_clean_up(elg);
    aws_mem_release(elg->allocator, elg);

    aws_jni_event_loop_group_pack(env, jni_elg, NULL);
}

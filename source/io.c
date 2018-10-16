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
#include <crt.h>
#include <aws/io/event_loop.h>
#include <aws/common/system_info.h>

/* cached id for looking up EventLoopGroup._elg */
static jfieldID s_elg_field = 0;

JNIEXPORT
void JNICALL Java_com_amazon_aws_EventLoopGroup_init(JNIEnv *env, jobject jni_elg, jint num_threads) {
    if (!s_elg_field) {
        jclass elg_class = (*env)->GetObjectClass(env, jni_elg);
        s_elg_field = (*env)->GetFieldID(env, elg_class, "_elg", "J");
    }
    
    jlong elg_value = (*env)->GetLongField(env, jni_elg, s_elg_field);
    assert(elg_value == 0 && "EventLoopGroup._elg should be 0 initialized in the constructor");

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_event_loop_group *elg = aws_mem_acquire(allocator, sizeof(struct aws_event_loop_group));
    /* clamp num_threads between (1, n CPUs) */
    if (num_threads > aws_system_info_processor_count()) {
        num_threads = aws_system_info_processor_count();
    }
    if (num_threads < 1) {
        num_threads = 1;
    }
    aws_event_loop_group_default_init(elg, allocator, num_threads);

    /* push the elg back into the EventLoopGroup as _elg */
    elg_value = (jlong)elg;
    (*env)->SetLongField(env, jni_elg, s_elg_field, elg_value);
}

JNIEXPORT
void JNICALL Java_com_amazon_aws_EventLoopGroup_clean_1up(JNIEnv *env, jobject jni_elg) {
    assert(s_elg_field && "EventLoopGroup.clean_up was called without corresponding init");

    jlong elg_value = (*env)->GetLongField(env, jni_elg, s_elg_field);
    assert(elg_value && "EventLoopGroup._elg should be non-zero at clean_up time");

    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_value;
    aws_event_loop_group_clean_up(elg);

    aws_mem_release(elg->allocator, elg);

    /* push 0 back into the EventLoopGroup as _elg */
    elg_value = 0;
    (*env)->SetLongField(env, jni_elg, s_elg_field, elg_value);
}

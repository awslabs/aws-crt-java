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
#include <aws/io/logging.h>

#include "crt.h"
#include "java_class_ids.h"

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
    struct aws_event_loop_group *elg = aws_mem_calloc(allocator, 1, sizeof(struct aws_event_loop_group));
    AWS_FATAL_ASSERT(elg);

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

struct event_loop_group_cleanup_callback_data {
    JavaVM *jvm;
    jlong elg_addr;
    jobject java_event_loop_group;
};

static void s_event_loop_group_cleanup_completion_callback(void *user_data) {
    struct event_loop_group_cleanup_callback_data *callback_data = user_data;

    // Elg memory can be freed
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)callback_data->elg_addr;
    aws_mem_release(elg->allocator, elg);

    AWS_LOGF_DEBUG(AWS_LS_IO_EVENT_LOOP, "Event Loop Shutdown Complete");

    // Tell the Java event loop group that cleanup is done.  This lets it release its references.
    JavaVM *jvm = callback_data->jvm;
    JNIEnv *env = NULL;
    /* fetch the env manually, rather than through the helper which will install an exit callback */
#ifdef ANDROID
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);
#else
    /* awkward temp to get around gcc 4.1 strict aliasing incorrect warnings */
    void *temp_env = NULL;
    (*jvm)->AttachCurrentThread(jvm, (void **)&temp_env, NULL);
    env = temp_env;
#endif
    (*env)->CallVoidMethod(env, callback_data->java_event_loop_group, event_loop_group_properties.onCleanupComplete);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    // Remove the ref that was probably keeping the Java event loop group alive.
    (*env)->DeleteGlobalRef(env, callback_data->java_event_loop_group);

    // We're done with this callback data, free it.
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback_data);

    (*jvm)->DetachCurrentThread(jvm);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupDestroy(
    JNIEnv *env,
    jclass jni_elg,
    jobject elg_jobject,
    jlong elg_addr) {
    (void)jni_elg;
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_addr;
    if (!elg) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.eventLoopGroupDestroy: instance should be non-null at clean_up time");
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct event_loop_group_cleanup_callback_data *callback_data =
        aws_mem_acquire(allocator, sizeof(struct event_loop_group_cleanup_callback_data));
    callback_data->java_event_loop_group = (*env)->NewGlobalRef(env, elg_jobject);
    callback_data->elg_addr = elg_addr;

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_event_loop_group_clean_up_async(elg, s_event_loop_group_cleanup_completion_callback, callback_data);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

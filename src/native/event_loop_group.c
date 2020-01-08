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

#include <aws/common/task_scheduler.h>
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
    (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
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

struct task_data {
    JavaVM *jvm;
    struct aws_task task;
    jobject jfunction;
    struct aws_event_loop *loop;
};

static void s_run_task(struct aws_task *task, void *arg, enum aws_task_status status) {
    (void)status;
    struct task_data *task_data = AWS_CONTAINER_OF(task, struct task_data, task);

    AWS_LOGF_DEBUG(AWS_LS_IO_EVENT_LOOP, "Java Scheduled Task");

    JavaVM *jvm = task_data->jvm;
    JNIEnv *env = NULL;
    jint jerr = (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
    AWS_FATAL_ASSERT(jerr == JNI_OK && "Failed AttachCurrentThread");
    AWS_FATAL_ASSERT(env && "env NULL after AttachCurrentThread");

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/EventLoopGroup$FunctionWrapper");
    AWS_FATAL_ASSERT(cls && "FindClass(FunctionWrapper) failed");

    jmethodID method_id = (*env)->GetMethodID(env, cls, "deliver", "([B)[B");
    AWS_FATAL_ASSERT(method_id && "deliver() method not found");

    jbyteArray jarray = (*env)->NewByteArray(env, 128);
    AWS_FATAL_ASSERT(jarray && "NewByteArray failed");

    jobject jreturned = (*env)->CallObjectMethod(env, task_data->jfunction, method_id, jarray);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env) && "Exception from callback");

    AWS_FATAL_ASSERT(jreturned != NULL);
    (*env)->DeleteLocalRef(env, jreturned);
    (*env)->DeleteLocalRef(env, jarray);

    jerr = (*jvm)->DetachCurrentThread(jvm);
    AWS_FATAL_ASSERT(jerr == JNI_OK && "Failed DetachCurrentThread");

    aws_event_loop_schedule_task_now(task_data->loop, &task_data->task);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupScheduleTask(
    JNIEnv *env,
    jclass jni_elg,
    jlong elg_addr,
    jobject jfunction) {

    (void)jni_elg;
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)elg_addr;
    AWS_FATAL_ASSERT(elg && "bad elg");
    AWS_FATAL_ASSERT(jfunction && "bad function");

    struct task_data *task_data = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct task_data));
    AWS_FATAL_ASSERT(task_data && "calloc task failed");

    task_data->jfunction = (*env)->NewGlobalRef(env, jfunction);
    AWS_FATAL_ASSERT(task_data->jfunction && "NewGlobalRef failed");

    jint jvmresult = (*env)->GetJavaVM(env, &task_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0 && "GetJavaVM failed");

    aws_task_init(&task_data->task, s_run_task, task_data, "Java scheduled task");

    task_data->loop = aws_event_loop_group_get_next_loop(elg);
    AWS_FATAL_ASSERT(task_data->loop && "next loop failed");

    aws_event_loop_schedule_task_now(task_data->loop, &task_data->task);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

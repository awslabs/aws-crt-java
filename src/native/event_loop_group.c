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

struct event_loop_group_cleanup_callback_data {
    JavaVM *jvm;
    jobject java_event_loop_group;
};

static void s_event_loop_group_cleanup_completion_callback(void *user_data) {
    struct event_loop_group_cleanup_callback_data *callback_data = user_data;

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

    if (env == NULL) {
        fprintf(stdout, "\n>>>>>>>>>> ENV is NULL\n");
    } else {
        fprintf(stdout, "\n>>>>>>>>>> ENV is not null\n");
    }
    if (jni == NULL) {
        fprintf(stdout, "\n>>>>>>>>>> ENV is NULL\n");
    } else {
        fprintf(stdout, "\n>>>>>>>>>> ENV is not null\n");
    }
    if (callback_data == NULL) {
        fprintf(stdout, "\n>>>>>>>>>> callback_data is NULL\n");
    } else {
        fprintf(stdout, "\n>>>>>>>>>> callback_data is not null\n");
    }
    if (callback_data->java_event_loop_group == NULL) {
        fprintf(stdout, "\n>>>>>>>>>> callback_data->java_event_loop_group is NULL\n");
    } else {
        fprintf(stdout, "\n>>>>>>>>>> callback_data->java_event_loop_group is not null\n");
    }
    if (event_loop_group_properties.onCleanupComplete == NULL) {
        fprintf(stdout, "\n>>>>>>>>>> event_loop_group_properties.onCleanupComplete is NULL\n");
    } else {
        fprintf(stdout, "\n>>>>>>>>>> event_loop_group_properties.onCleanupComplete is not null\n");
    }

    (*env)->CallVoidMethod(env, callback_data->java_event_loop_group, event_loop_group_properties.onCleanupComplete);
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));

    // Remove the ref that was probably keeping the Java event loop group alive.
    (*env)->DeleteGlobalRef(env, callback_data->java_event_loop_group);

    // We're done with this callback data, free it.
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback_data);

    (*jvm)->DetachCurrentThread(jvm);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupNew(
    JNIEnv *env,
    jclass jni_elg,
    jobject elg_jobject,
    jint num_threads) {
    (void)jni_elg;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct event_loop_group_cleanup_callback_data *callback_data =
        aws_mem_acquire(allocator, sizeof(struct event_loop_group_cleanup_callback_data));
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: shutdown callback data allocation failed");
        goto on_error;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_shutdown_callback_options shutdown_options = {
        .shutdown_callback_fn = s_event_loop_group_cleanup_completion_callback,
        .shutdown_callback_user_data = callback_data,
    };

    struct aws_event_loop_group *elg =
        aws_event_loop_group_new_default(allocator, (uint16_t)num_threads, &shutdown_options);
    if (elg == NULL) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: aws_event_loop_group_new_default failed");
        goto on_error;
    }

    callback_data->java_event_loop_group = (*env)->NewGlobalRef(env, elg_jobject);

    return (jlong)elg;

on_error:

    aws_mem_release(allocator, callback_data);

    return (jlong)NULL;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupNewPinnedToCpuGroup(
    JNIEnv *env,
    jclass jni_elg,
    jobject elg_jobject,
    jint cpu_group,
    jint num_threads) {
    (void)jni_elg;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct event_loop_group_cleanup_callback_data *callback_data =
        aws_mem_acquire(allocator, sizeof(struct event_loop_group_cleanup_callback_data));
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: shutdown callback data allocation failed");
        goto on_error;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_shutdown_callback_options shutdown_options = {
        .shutdown_callback_fn = s_event_loop_group_cleanup_completion_callback,
        .shutdown_callback_user_data = callback_data,
    };

    struct aws_event_loop_group *elg = aws_event_loop_group_new_default_pinned_to_cpu_group(
        allocator, (uint16_t)num_threads, (uint16_t)cpu_group, &shutdown_options);
    if (elg == NULL) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: eventLoopGroupNewPinnedToCpuGroup failed");
        goto on_error;
    }

    callback_data->java_event_loop_group = (*env)->NewGlobalRef(env, elg_jobject);

    return (jlong)elg;

on_error:

    aws_mem_release(allocator, callback_data);

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

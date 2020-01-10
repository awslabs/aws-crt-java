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

struct event_loop_group_binding {
    JavaVM *jvm;
    struct aws_event_loop_group *native_elg;
    jweak java_shutdown_complete_future;
};

static void s_event_loop_group_binding_destroy(struct event_loop_group_binding *binding, JNIEnv *env) {
    if (binding == NULL) {
        return;
    }

    if (binding->java_shutdown_complete_future != NULL) {
        (*env)->DeleteWeakGlobalRef(env, binding->java_shutdown_complete_future);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static struct event_loop_group_binding *s_event_loop_group_binding_new(
    JNIEnv *env,
    jobject java_event_loop_group,
    struct aws_event_loop_group *native_elg,
    jobject java_shutdown_complete_future) {
    struct event_loop_group_binding *binding =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct event_loop_group_binding));
    if (binding == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->native_elg = native_elg;

    binding->java_shutdown_complete_future = (*env)->NewWeakGlobalRef(env, java_shutdown_complete_future);
    if (binding->java_shutdown_complete_future == NULL) {
        goto error;
    }

    return binding;

error:

    s_event_loop_group_binding_destroy(binding, env);

    return NULL;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupNew(
    JNIEnv *env,
    jclass jni_elg,
    jobject java_event_loop_group,
    jobject shutdown_complete_future,
    jint num_threads) {
    (void)jni_elg;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct event_loop_group_binding *binding = NULL;
    struct aws_event_loop_group *elg = NULL;

    elg = aws_mem_calloc(allocator, 1, sizeof(struct aws_event_loop_group));
    if (elg == NULL) {
        aws_jni_throw_runtime_exception(env, "EventLoopGroup.event_loop_group_new: allocation failed");
        goto error;
    }

    int result = aws_event_loop_group_default_init(elg, allocator, (uint16_t)num_threads);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.event_loop_group_new: aws_event_loop_group_default_init failed");
        goto error;
    }

    binding = s_event_loop_group_binding_new(env, java_event_loop_group, elg, shutdown_complete_future);
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "EventLoopGroup.event_loop_group_new: failed to create binding");
        goto error;
    }

    return (jlong)binding;

error:

    if (binding != NULL) {
        s_event_loop_group_binding_destroy(binding, env);
    }

    if (elg != NULL) {
        aws_event_loop_group_clean_up(elg);
        aws_mem_release(allocator, elg);
    }

    return (jlong)NULL;
}

static void s_event_loop_group_cleanup_completion_callback(void *user_data) {
    struct event_loop_group_binding *binding = user_data;

    // Elg memory can be freed
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)binding->native_elg;
    aws_mem_release(elg->allocator, elg);

    AWS_LOGF_DEBUG(AWS_LS_IO_EVENT_LOOP, "Event Loop Shutdown Complete");

    // Tell the Java event loop group that cleanup is done.  This lets it release its references.
    JavaVM *jvm = binding->jvm;
    JNIEnv *env = NULL;
    /* fetch the env manually, rather than through the helper which will install an exit callback */
    (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);

    if (env != NULL) {
        (*env)->CallVoidMethod(
            env, binding->java_shutdown_complete_future, completable_future_properties.complete_method_id);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

        // We're done with this callback data, free it.
        s_event_loop_group_binding_destroy(binding, env);
    }

    (*jvm)->DetachCurrentThread(jvm);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_EventLoopGroup_eventLoopGroupDestroy(
    JNIEnv *env,
    jclass jni_elg,
    jlong elg_binding) {
    (void)jni_elg;
    struct event_loop_group_binding *binding = (struct event_loop_group_binding *)elg_binding;
    if (!binding) {
        aws_jni_throw_runtime_exception(
            env, "EventLoopGroup.eventLoopGroupDestroy: instance should be non-null at clean_up time");
        return;
    }

    aws_event_loop_group_clean_up_async(binding->native_elg, s_event_loop_group_cleanup_completion_callback, binding);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

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

#include <aws/io/channel_bootstrap.h>

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

struct shutdown_callback_data {
    JavaVM *jvm;
    jweak java_client_bootstrap;
};

static void s_shutdown_callback_data_destroy(JNIEnv *env, struct shutdown_callback_data *callback_data) {
    if (!callback_data) {
        return;
    }

    if (callback_data->java_client_bootstrap) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_client_bootstrap);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static void s_client_bootstrap_shutdown_complete(void *user_data) {
    struct shutdown_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jobject java_client_bootstrap = (*env)->NewLocalRef(env, callback_data->java_client_bootstrap);
    if (java_client_bootstrap) {
        // Tell the Java ClientBootstrap that cleanup is done.  This lets it release its references.
        (*env)->CallVoidMethod(env, java_client_bootstrap, client_bootstrap_properties.onShutdownComplete);
        (*env)->DeleteLocalRef(env, java_client_bootstrap);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }

    s_shutdown_callback_data_destroy(env, callback_data);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_ClientBootstrap_clientBootstrapNew(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_bootstrap,
    jlong jni_elg,
    jlong jni_hr) {
    (void)jni_class;
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)jni_elg;
    struct aws_host_resolver *resolver = (struct aws_host_resolver *)jni_hr;

    if (!elg) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    if (!resolver) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid HostResolver");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct shutdown_callback_data *callback_data = aws_mem_calloc(allocator, 1, sizeof(struct shutdown_callback_data));
    if (!callback_data) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Unable to allocate");
        return (jlong)NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    if (jvmresult != 0) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Unable to get JVM");
        goto error;
    }

    callback_data->java_client_bootstrap = (*env)->NewWeakGlobalRef(env, jni_bootstrap);
    if (!callback_data->java_client_bootstrap) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Unable to create global weak ref");
        goto error;
    }

    struct aws_client_bootstrap_options bootstrap_options = {
        .event_loop_group = elg,
        .host_resolver = resolver,
        .on_shutdown_complete = s_client_bootstrap_shutdown_complete,
        .user_data = callback_data,
    };

    struct aws_client_bootstrap *bootstrap = aws_client_bootstrap_new(allocator, &bootstrap_options);
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(
            env, "ClientBootstrap.client_bootstrap_new: Unable to allocate new aws_client_bootstrap");
        goto error;
    }

    return (jlong)bootstrap;

error:
    s_shutdown_callback_data_destroy(env, callback_data);
    return (jlong)NULL;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_ClientBootstrap_clientBootstrapDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_bootstrap) {
    (void)env;
    (void)jni_class;
    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)jni_bootstrap;
    if (!bootstrap) {
        return;
    }

    aws_client_bootstrap_release(bootstrap);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

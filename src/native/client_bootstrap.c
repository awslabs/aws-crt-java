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

struct client_bootstrap_binding {
    JavaVM *jvm;
    jweak java_client_bootstrap;
    jobject java_event_loop_group;
    jobject java_host_resolver;
};

static void s_client_bootstrap_binding_destroy(JNIEnv *env, struct client_bootstrap_binding *binding) {
    if (!binding) {
        return;
    }

    if (binding->java_client_bootstrap != NULL) {
        (*env)->DeleteWeakGlobalRef(env, binding->java_client_bootstrap);
    }

    if (binding->java_event_loop_group != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_event_loop_group);
    }

    if (binding->java_host_resolver != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_host_resolver);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static struct client_bootstrap_binding *s_client_bootstrap_binding_new(
    JNIEnv *env,
    jobject java_client_bootstrap,
    jobject java_event_loop_group,
    jobject java_host_resolver) {

    struct client_bootstrap_binding *binding =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct client_bootstrap_binding));
    if (binding == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->java_client_bootstrap = (*env)->NewWeakGlobalRef(env, java_client_bootstrap);
    if (binding->java_client_bootstrap == NULL) {
        goto on_error;
    }

    binding->java_event_loop_group = (*env)->NewGlobalRef(env, java_event_loop_group);
    if (binding->java_event_loop_group == NULL) {
        goto on_error;
    }

    binding->java_host_resolver = (*env)->NewGlobalRef(env, java_host_resolver);
    if (binding->java_host_resolver == NULL) {
        goto on_error;
    }

    return binding;

on_error:

    s_client_bootstrap_binding_destroy(env, binding);

    return NULL;
}

static void s_client_bootstrap_shutdown_complete(void *user_data) {
    struct client_bootstrap_binding *binding = user_data;

    JNIEnv *env = aws_jni_get_thread_env(binding->jvm);

    s_client_bootstrap_binding_destroy(env, binding);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_ClientBootstrap_clientBootstrapNew(
    JNIEnv *env,
    jclass jni_class,
    jobject java_bootstrap,
    jobject java_elg,
    jobject java_hr) {
    (void)jni_class;

    if (java_elg == NULL) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    if (java_hr == NULL) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid HostResolver");
        return (jlong)NULL;
    }

    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)(*env)->CallLongMethod(
        env, java_elg, crt_resource_properties.get_native_handle_method_id);
    if (!elg) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    struct aws_host_resolver *resolver = (struct aws_host_resolver *)(*env)->CallLongMethod(
        env, java_hr, crt_resource_properties.get_native_handle_method_id);
    if (!resolver) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid HostResolver");
        return (jlong)NULL;
    }

    struct client_bootstrap_binding *binding = s_client_bootstrap_binding_new(env, java_bootstrap, java_elg, java_hr);
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Unable to construct binding");
        return (jlong)NULL;
    }

    struct aws_client_bootstrap_options bootstrap_options = {
        .event_loop_group = elg,
        .host_resolver = resolver,
        .on_shutdown_complete = s_client_bootstrap_shutdown_complete,
        .user_data = binding,
    };

    struct aws_client_bootstrap *bootstrap = aws_client_bootstrap_new(aws_jni_get_allocator(), &bootstrap_options);
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(
            env, "ClientBootstrap.client_bootstrap_new: Unable to allocate new aws_client_bootstrap");
        goto error;
    }

    return (jlong)bootstrap;

error:

    s_client_bootstrap_binding_destroy(env, binding);
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

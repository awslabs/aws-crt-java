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

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_ClientBootstrap_client_1bootstrap_1new(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_elg) {
    struct aws_event_loop_group *elg = (struct aws_event_loop_group *)jni_elg;
    if (!elg) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap.client_bootstrap_new: Invalid EventLoopGroup");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_client_bootstrap *bootstrap =
        (struct aws_client_bootstrap *)aws_mem_acquire(allocator, sizeof(struct aws_client_bootstrap));
    if (!bootstrap) {
        aws_jni_throw_runtime_exception(
            env, "ClientBootstrap.client_bootstrap_new: Unable to allocate new aws_client_bootstrap");
        return (jlong)NULL;
    }

    if (aws_client_bootstrap_init(bootstrap, allocator, elg, NULL, NULL)) {
        aws_jni_throw_runtime_exception(
            env, "ClientBootstrap.client_bootstrap_new: Unable to initalize new aws_client_boostrap");
        goto error_cleanup;
    }

    return (jlong)bootstrap;

error_cleanup:
    if (bootstrap) {
        aws_mem_release(allocator, bootstrap);
    }
    return (jlong)NULL;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_ClientBootstrap_client_1bootstrap_1clean_1up(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_bootstrap) {
    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)jni_bootstrap;
    if (!bootstrap) {
        return;
    }

    aws_client_bootstrap_clean_up(bootstrap);
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, bootstrap);
}

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

#include <aws/io/socket.h>

#include "crt.h"

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if !defined(_MSC_VER) && UINTPTR_MAX == 0xffffffff
#    pragma GCC diagnostic push
#    pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#    pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#endif

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1new(JNIEnv *env, jclass jni_class) {
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_socket_options *options =
        (struct aws_socket_options *)aws_mem_acquire(allocator, sizeof(struct aws_socket_options));
    if (!options) {
        aws_jni_throw_runtime_exception(
            env, "SocketOptions.socket_options_new: Unable to allocate new aws_socket_options");
        return (jlong)NULL;
    }
    AWS_ZERO_STRUCT(*options);
    options->connect_timeout_ms = 3000;
    options->type = AWS_SOCKET_STREAM;
    return (jlong)options;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1clean_1up(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options) {
    (void)env;
    (void)jni_class;
    struct aws_socket_options *options = (struct aws_socket_options *)jni_options;
    if (!options) {
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, options);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1set_1domain(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jint jni_domain) {
    (void)env;
    (void)jni_class;
    struct aws_socket_options *options = (struct aws_socket_options *)jni_options;
    if (!options) {
        return;
    }

    options->domain = (enum aws_socket_domain)jni_domain;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1set_1type(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jint jni_type) {
    (void)env;
    (void)jni_class;
    struct aws_socket_options *options = (struct aws_socket_options *)jni_options;
    if (!options) {
        return;
    }

    options->type = (enum aws_socket_type)jni_type;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1set_1connect_1timeout_1ms(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jint jni_timeout) {
    (void)env;
    (void)jni_class;
    struct aws_socket_options *options = (struct aws_socket_options *)jni_options;
    if (!options) {
        return;
    }

    options->connect_timeout_ms = jni_timeout;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1set_1keep_1alive_1interval_1sec(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jshort jni_interval) {
    (void)env;
    (void)jni_class;
    struct aws_socket_options *options = (struct aws_socket_options *)jni_options;
    if (!options) {
        return;
    }

    options->keep_alive_interval_sec = jni_interval;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_SocketOptions_socket_1options_1set_1keep_1alive_1timeout_1sec(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_options,
    jshort jni_timeout) {
    (void)env;
    (void)jni_class;
    struct aws_socket_options *options = (struct aws_socket_options *)jni_options;
    if (!options) {
        return;
    }

    options->keep_alive_timeout_sec = jni_timeout;
}

#if !defined(_MSC_VER) && UINTPTR_MAX == 0xffffffff
#    pragma GCC diagnostic pop
#endif

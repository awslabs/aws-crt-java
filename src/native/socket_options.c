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
jlong JNICALL Java_software_amazon_awssdk_crt_io_SocketOptions_socketOptionsNew(
    JNIEnv *env,
    jclass jni_class,
    jint domain,
    jint type,
    jint connect_timeout_ms,
    jint keep_alive_interval_secs,
    jint keep_alive_timeout_secs) {
    (void)env;
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_socket_options *options =
        (struct aws_socket_options *)aws_mem_calloc(allocator, 1, sizeof(struct aws_socket_options));
    AWS_FATAL_ASSERT(options);

    options->domain = domain;
    options->type = type;
    options->connect_timeout_ms = connect_timeout_ms;
    options->keep_alive_interval_sec = (short)keep_alive_interval_secs;
    options->keep_alive_timeout_sec = (short)keep_alive_timeout_secs;

    return (jlong)options;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_SocketOptions_socketOptionsDestroy(
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

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

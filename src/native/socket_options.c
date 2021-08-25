/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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
    jint keep_alive_timeout_secs,
    jint windows_named_pipe_open_flags) {
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
    options->windows_named_pipe_open_flags = windows_named_pipe_open_flags;

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

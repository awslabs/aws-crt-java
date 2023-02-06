/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/http/proxy.h>
#include <aws/io/tls_channel_handler.h>

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

void aws_http_proxy_environment_variable_setting_jni_init(
    struct proxy_env_var_settings *options,
    jint environment_variable_proxy_connection_type,
    jint environment_variable_type,
    struct aws_tls_connection_options *proxy_tls_connection_options) {
    options->connection_type = environment_variable_proxy_connection_type;
    options->env_var_type = environment_variable_type;
    options->tls_options = proxy_tls_connection_options;
}

void aws_http_proxy_environment_variable_setting_jni_clean_up(struct proxy_env_var_settings *options) {
    /* Nothing to cleanup */
    (void) options;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

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

#include <aws/common/logging.h>

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

#if _MSC_VER
#    pragma warning(disable : 4204) /* non-constant aggregate initializer */
#endif

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_Log_log(
    JNIEnv *env,
    jclass jni_class,
    jint jni_level,
    jint jni_subject,
    jstring jni_logstring) {
    (void)jni_class;

    const char *raw_string = (*env)->GetStringUTFChars(env, jni_logstring, NULL);
    AWS_LOGF((enum aws_log_level)jni_level, jni_subject, "%s", raw_string);
    (*env)->ReleaseStringUTFChars(env, jni_logstring, raw_string);
}

static struct aws_logger s_logger;
static bool s_initialized_logger = false;

extern int g_memory_tracing;

static void s_aws_init_logging_internal(JNIEnv *env, struct aws_logger_standard_options *options) {
    struct aws_allocator *allocator = aws_jni_get_allocator();

    if (g_memory_tracing == 0) {
        if (aws_logger_init_standard(&s_logger, allocator, options)) {
            aws_jni_throw_runtime_exception(env, "Failed to initialize standard logger");
            return;
        }
    } else {
        if (aws_logger_init_noalloc(&s_logger, allocator, options)) {
            aws_jni_throw_runtime_exception(env, "Failed to initialize no-alloc logger");
            return;
        }
    }

    aws_logger_set(&s_logger);
    s_initialized_logger = true;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_Log_initLoggingToStdout(JNIEnv *env, jclass jni_crt_class, jint level) {
    (void)jni_crt_class;

    struct aws_logger_standard_options log_options = {.level = level, .file = stdout};

    s_aws_init_logging_internal(env, &log_options);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_Log_initLoggingToStderr(JNIEnv *env, jclass jni_crt_class, jint level) {
    (void)jni_crt_class;

    struct aws_logger_standard_options log_options = {.level = level, .file = stderr};

    s_aws_init_logging_internal(env, &log_options);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_Log_initLoggingToFile(
    JNIEnv *env,
    jclass jni_crt_class,
    jint level,
    jstring jni_filename) {
    (void)jni_crt_class;

    const char *filename = (*env)->GetStringUTFChars(env, jni_filename, NULL);
    struct aws_logger_standard_options log_options = {.level = level, .filename = filename};
    s_aws_init_logging_internal(env, &log_options);
    (*env)->ReleaseStringUTFChars(env, jni_filename, filename);
}

void aws_jni_cleanup_logging(void) {
    if (aws_logger_get() == &s_logger) {
        aws_logger_set(NULL);
    }

    if (s_initialized_logger) {
        aws_logger_clean_up(&s_logger);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

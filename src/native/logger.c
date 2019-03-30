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

#include <aws/common/common.h>
#include <aws/common/string.h>
#include <aws/io/io.h>
#include <aws/io/logging.h>

#include <crt.h>

static struct aws_logger s_logger;

JNIEXPORT
void JNICALL
Java_software_amazon_awssdk_crt_io_Logger_configureLoggerWithFd(JNIEnv *env, jclass jni_logger, jobject jni_fd, jint jni_level) {
    if (jni_fd == NULL) {
        aws_jni_throw_runtime_exception(env, "FileDescriptor must not be null");
        return;
    }

    jclass fd_class = (*env)->FindClass(env, "java/io/FileDescriptor");
    if (!fd_class) {
        aws_jni_throw_runtime_exception(env, "Failed to find FileDescriptor class");
        return;
    }

    jfieldID descriptor_field = (*env)->GetFieldID(env, fd_class, "fd", "I");
    if (descriptor_field == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to find FileDescriptor descriptor/fd field");
        return;
    }

    if (jni_level > AWS_LOG_LEVEL_TRACE || jni_level < AWS_LOG_LEVEL_NONE) {
        aws_jni_throw_runtime_exception(env, "Invalid logging level: %d", jni_level);
        return;
    }

    int fd = (*env)->GetIntField(env, jni_fd, descriptor_field);
    FILE* fp = fdopen(fd, "w");
    if (!fp) {
        aws_jni_throw_runtime_exception(env, "Unable to open fd %d for writing", fd);
        return;
    }
    enum aws_log_level level = jni_level;
    struct aws_logger_standard_options log_options = {.level = level, .file = fp};
    if (aws_logger_init_standard(&s_logger, aws_jni_get_allocator(), &log_options)) {
        aws_jni_throw_runtime_exception(env, "Failed to initialize aws_logger");
        return;
    }

    aws_logger_set(&s_logger);
}

JNIEXPORT
void JNICALL
Java_software_amazon_awssdk_crt_io_Logger_configureLoggerWithFilename(JNIEnv *env, jclass jni_logger, jstring jni_filename, jint jni_level) {
    if (jni_filename == NULL) {
        aws_jni_throw_runtime_exception(env, "filename must not be null");
        return;
    }

    if (jni_level > AWS_LOG_LEVEL_TRACE || jni_level < AWS_LOG_LEVEL_NONE) {
        aws_jni_throw_runtime_exception(env, "Invalid logging level: %d", jni_level);
        return;
    }

    enum aws_log_level level = jni_level;
    const char* filename = (*env)->GetStringUTFChars(env, jni_filename, NULL);
    struct aws_logger_standard_options log_options = {.level = level, .filename = filename };
    if (aws_logger_init_standard(&s_logger, aws_jni_get_allocator(), &log_options)) {
        aws_jni_throw_runtime_exception(env, "Failed to initialize aws_logger");
        return;
    }

    aws_logger_set(&s_logger);
}

JNIEXPORT
void JNICALL
Java_software_amazon_awssdk_crt_io_Logger_flushLogger(JNIEnv *env, jclass jni_logger) {
    if (aws_logger_get() == &s_logger) {
        /* until we have aws_logger_flush, we flush all writable streams */
        fflush(NULL);
    }
}


/* called at exit of the java process by the CRT atexit callback */
void s_logger_cleanup() {
    if (aws_logger_get() == &s_logger) {
        aws_logger_set(NULL);
        aws_logger_clean_up(&s_logger);
    }
}

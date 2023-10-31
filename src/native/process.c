/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <aws/common/process.h>
#include <jni.h>

#include "java_class_ids.h"

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_Process_processGetPid(JNIEnv *env, jclass jni_crt_class) {
    (void)jni_crt_class;
    aws_cache_jni_ids(env);

    return (jint)aws_get_pid();
}

JNIEXPORT
jlong JNICALL
    Java_software_amazon_awssdk_crt_Process_processGetMaxIOHandlesSoftLimit(JNIEnv *env, jclass jni_crt_class) {
    (void)jni_crt_class;
    aws_cache_jni_ids(env);

    return (jlong)aws_get_soft_limit_io_handles();
}

JNIEXPORT
jlong JNICALL
    Java_software_amazon_awssdk_crt_Process_processGetMaxIOHandlesHardLimit(JNIEnv *env, jclass jni_crt_class) {
    (void)jni_crt_class;
    aws_cache_jni_ids(env);

    return (jlong)aws_get_hard_limit_io_handles();
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_Process_processSetMaxIOHandlesSoftLimit(
    JNIEnv *env,
    jclass jni_crt_class,
    jlong max_handles) {
    (void)jni_crt_class;
    (void)max_handles;
    aws_cache_jni_ids(env);

    return aws_set_soft_limit_io_handles((size_t)max_handles) == AWS_OP_SUCCESS;
}

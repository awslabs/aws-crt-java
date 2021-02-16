/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <aws/common/process.h>
#include <jni.h>

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_Process_processGetPid(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    return (jint)aws_get_pid();
}

JNIEXPORT
jlong JNICALL
    Java_software_amazon_awssdk_crt_Process_processGetMaxIOHandlesSoftLimit(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    return (jlong)aws_get_soft_limit_io_handles();
}

JNIEXPORT
jlong JNICALL
    Java_software_amazon_awssdk_crt_Process_processGetMaxIOHandlesHardLimit(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    return (jlong)aws_get_hard_limit_io_handles();
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_Process_processSetMaxIOHandlesSoftLimit(
    JNIEnv *env,
    jclass jni_crt_class,
    jlong max_handles) {
    (void)env;
    (void)jni_crt_class;
    (void)max_handles;
    return aws_set_soft_limit_io_handles((size_t)max_handles) == AWS_OP_SUCCESS;
}

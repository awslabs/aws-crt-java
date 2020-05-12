/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
    return aws_set_soft_limit_io_handles((size_t)max_handles) == AWS_OP_SUCCESS;
}

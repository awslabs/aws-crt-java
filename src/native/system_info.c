/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/common/system_info.h>

#include "crt.h"
#include "java_class_ids.h"

JNIEXPORT
jint JNICALL Java_software_amazon_awssdk_crt_SystemInfo_processorCount(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;

    return (jint)aws_system_info_processor_count();
}

JNIEXPORT
jshort JNICALL Java_software_amazon_awssdk_crt_SystemInfo_cpuGroupCount(JNIEnv *env, jclass cls) {
    (void)env;
    (void)cls;

    return aws_get_cpu_group_count();
}

JNIEXPORT
jobjectArray JNICALL
    Java_software_amazon_awssdk_crt_SystemInfo_cpuInfoForGroup(JNIEnv *env, jclass cls, jshort groupIdx) {
    (void)cls;

    size_t cpu_count = aws_get_cpu_count_for_group(groupIdx);

    struct aws_cpu_info *cpu_info = aws_mem_calloc(aws_default_allocator(), 1, sizeof(struct aws_cpu_info));
    AWS_FATAL_ASSERT(cpu_info && "allocation failed in Java_software_amazon_awssdk_crt_SystemInfo_getCpuIdsForGroup");

    jobjectArray cpu_info_array = (*env)->NewObjectArray(env, cpu_count, cpu_info_properties.cpu_info_class, NULL);

    for (size_t i = 0; i < cpu_count; ++i) {
        jobject cpu_info_obj =
            (*env)->NewObject(env, cpu_info_properties.cpu_info_class, cpu_info_properties.cpu_info_constructor);
        (*env)->SetObjectArrayElement(env, cpu_info_array, 1, cpu_info_obj);
    }

    aws_mem_release(aws_default_allocator(), cpu_info);

    return cpu_info_array;
}

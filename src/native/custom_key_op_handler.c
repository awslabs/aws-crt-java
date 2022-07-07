/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "custom_key_op_handler.h"


struct custom_key_op_handler *aws_custom_key_op_handler_java_new(JNIEnv *env, struct aws_allocator *allocator, jobject jni_custom_key_op) {
    struct custom_key_op_handler *java_custom_key_op_handler = aws_mem_calloc(allocator, 1, sizeof(struct custom_key_op_handler));
    if (java_custom_key_op_handler == NULL) {
        return NULL;
    }

    if ((*env)->GetJavaVM(env, &java_custom_key_op_handler->jvm) != 0) {
        aws_jni_throw_runtime_exception(env, "failed to get JVM");
        aws_mem_release(allocator, java_custom_key_op_handler);
        return NULL;
    }

    java_custom_key_op_handler->jni_key_operations_options = (*env)->NewGlobalRef(env, jni_custom_key_op);
    if (java_custom_key_op_handler->jni_key_operations_options == NULL) {
        aws_jni_throw_runtime_exception(env, "failed to make global reference for custom key operation options in JNI");
        aws_mem_release(allocator, java_custom_key_op_handler);
        return NULL;
    }

    AWS_LOGF_DEBUG(AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p: Initalizing Custom Key Operations", (void *)java_custom_key_op_handler);
    return java_custom_key_op_handler;
}

static void aws_custom_key_op_handler_java_release(JNIEnv *env, struct aws_allocator *allocator, struct custom_key_op_handler *java_custom_key_op_handler) {
    AWS_PRECONDITION(!java_custom_key_op_handler);
    if (!java_custom_key_op_handler) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p: Destroying Custom Key Operations", (void *)java_custom_key_op_handler);

    if (java_custom_key_op_handler->jni_key_operations_options) {
        (*env)->DeleteGlobalRef(env, java_custom_key_op_handler->jni_key_operations_options);
    }

    /* Frees allocated memory */
    aws_mem_release(allocator, java_custom_key_op_handler);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_io_TlsContextCustomKeyOperationOptions_tlsContextCustomKeyOperationOptionsNew(
    JNIEnv *env,
    jclass jni_class,
    jobject jni_custom_key_op) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct custom_key_op_handler *java_custom_key_op_handler = aws_custom_key_op_handler_java_new(env, allocator, jni_custom_key_op);
    if (java_custom_key_op_handler == NULL) {
        aws_jni_throw_runtime_exception(env, "TlsContextCustomKeyOperationOptions new: Could not create new custom key operator!");
        return (jlong)NULL;
    }
    return (jlong)java_custom_key_op_handler;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextCustomKeyOperationOptions_tlsContextCustomKeyOperationOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_custom_key_op) {
    (void)jni_class;

    struct custom_key_op_handler *java_custom_key_op_handler = (struct custom_key_op_handler *)jni_custom_key_op;
    if (!java_custom_key_op_handler) {
        aws_jni_throw_runtime_exception(env, "TlsContextCustomKeyOperationOptions destroy: Invalid/null custom key operator");
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_custom_key_op_handler_java_release(env, allocator, java_custom_key_op_handler);
}

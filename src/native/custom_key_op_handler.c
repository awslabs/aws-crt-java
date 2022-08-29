/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "custom_key_op_handler.h"

#include "java_class_ids.h"
#include "tls_context_pkcs11_options.h"
#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

struct aws_jni_custom_key_op_handler {
    /**
     * The C class containing the key operations. We extend/define it's VTable to allow
     * us to have it call into the customer's Java code.
     */
    struct aws_custom_key_op_handler base;

    /** A pointer to the JVM for getting JNI threads */
    JavaVM *jvm;

    /**
     * A reference to the Java class that this struct is linked to.
     * The interface, strings, etc, can be gotten from this class.
     */
    jobject jni_custom_key_op;

    /** The allocator to use */
    struct aws_allocator *allocator;
};

static void s_aws_custom_key_op_handler_perform_operation(
    struct aws_custom_key_op_handler *key_op_handler,
    struct aws_tls_key_operation *operation) {

    struct aws_jni_custom_key_op_handler *op_handler = (struct aws_jni_custom_key_op_handler *)key_op_handler->impl;
    AWS_FATAL_ASSERT(op_handler != NULL);

    jbyteArray jni_input_data = NULL;
    jobject jni_operation = NULL;
    bool success = false;
    AWS_ASSERT(operation != NULL);

    /* Get the Java ENV */
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    if (env == NULL) {
        /* JVM is likely shutting down. Do not crash but log error. */
        AWS_LOGF_ERROR(
            AWS_LS_COMMON_IO,
            "java_custom_key_op_handler=%p perform operation: Could not get Java ENV!",
            (void *)op_handler);
        goto clean_up;
    }

    /* Create DirectByteBuffer */
    struct aws_byte_cursor input_data = aws_tls_key_operation_get_input(operation);
    jni_input_data = aws_jni_byte_array_from_cursor(env, &input_data);
    if (jni_input_data == NULL) {
        aws_jni_check_and_clear_exception(env);
        goto clean_up;
    }

    /* Create TlsKeyOperation */
    jni_operation = (*env)->NewObject(
        env,
        tls_key_operation_properties.cls,
        tls_key_operation_properties.constructor,
        (jlong)(intptr_t)operation,
        jni_input_data,
        (jint)aws_tls_key_operation_get_type(operation),
        (jint)aws_tls_key_operation_get_signature_algorithm(operation),
        (jint)aws_tls_key_operation_get_digest_algorithm(operation));
    if (jni_operation == NULL) {
        aws_jni_check_and_clear_exception(env);
        goto clean_up;
    }

    /**
     * Invoke TlsKeyOperationHandler.performOperation() through the invokePerformOperation
     * function. This function will also catch any exceptions and clear the operation
     * with an exception should it occur.
     */
    (*env)->CallStaticVoidMethod(
        env,
        tls_key_operation_properties.cls,
        tls_key_operation_properties.invoke_operation_id,
        op_handler->jni_custom_key_op,
        jni_operation);
    /**
     * This should never fail because the function we're calling, invokePerformOperation,
     * wraps the user callback in a try-catch block that will catch any exceptions.
     */
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    success = true;

clean_up:
    if (jni_input_data) {
        (*env)->DeleteLocalRef(env, jni_input_data);
    }
    if (jni_operation) {
        (*env)->DeleteLocalRef(env, jni_operation);
    }
    if (!success) {
        aws_tls_key_operation_complete_with_error(operation, AWS_ERROR_UNKNOWN);
    }

    /* Release the Java ENV */
    aws_jni_release_thread_env(op_handler->jvm, env);
}

static void s_aws_custom_key_op_handler_destroy(struct aws_custom_key_op_handler *key_op_handler) {

    struct aws_jni_custom_key_op_handler *op_handler = (struct aws_jni_custom_key_op_handler *)key_op_handler->impl;

    /* Get the Java ENV */
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    if (env == NULL) {
        /* JVM is likely shutting down. Do not crash but log error. */
        AWS_LOGF_ERROR(
            AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p destroy: Could not get Java ENV!", (void *)op_handler);
        return;
    }

    /* Release the global reference */
    if (op_handler->jni_custom_key_op) {
        (*env)->DeleteGlobalRef(env, op_handler->jni_custom_key_op);
    }

    /* Release the Java ENV */
    aws_jni_release_thread_env(op_handler->jvm, env);

    /* Release the Java struct */
    aws_mem_release(op_handler->allocator, op_handler);
}

static struct aws_custom_key_op_handler_vtable s_aws_custom_key_op_handler_vtable = {
    .on_key_operation = s_aws_custom_key_op_handler_perform_operation,
};

struct aws_custom_key_op_handler *aws_custom_key_op_handler_java_new(JNIEnv *env, jobject jni_custom_key_op) {

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_jni_custom_key_op_handler));

    if ((*env)->GetJavaVM(env, &java_custom_key_op_handler->jvm) != 0) {
        aws_jni_throw_runtime_exception(env, "failed to get JVM");
        aws_mem_release(allocator, java_custom_key_op_handler);
        return NULL;
    }

    aws_ref_count_init(
        &java_custom_key_op_handler->base.ref_count,
        &java_custom_key_op_handler->base,
        (aws_simple_completion_callback *)s_aws_custom_key_op_handler_destroy);
    java_custom_key_op_handler->base.vtable = &s_aws_custom_key_op_handler_vtable;
    java_custom_key_op_handler->base.impl = (void *)java_custom_key_op_handler;
    /* Make a global reference so the Java interface is kept alive */
    java_custom_key_op_handler->jni_custom_key_op = (*env)->NewGlobalRef(env, jni_custom_key_op);
    AWS_FATAL_ASSERT(java_custom_key_op_handler->jni_custom_key_op != NULL);
    java_custom_key_op_handler->allocator = allocator;

    AWS_LOGF_DEBUG(
        AWS_LS_COMMON_IO,
        "java_custom_key_op_handler=%p: Initalizing Custom Key Operations",
        (void *)java_custom_key_op_handler);

    return &java_custom_key_op_handler->base;
}

void aws_custom_key_op_handler_java_release(struct aws_custom_key_op_handler *custom_key_op_handler) {

    if (custom_key_op_handler == NULL) {
        return;
    }
    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler =
        (struct aws_jni_custom_key_op_handler *)custom_key_op_handler->impl;

    AWS_LOGF_DEBUG(
        AWS_LS_COMMON_IO,
        "java_custom_key_op_handler=%p: Releasing Custom Key Operations (may destroy custom key operations if "
        "this Java class holds the last reference)",
        (void *)java_custom_key_op_handler);

    /**
     * Release the reference (which will only clean everything up if this is the last thing holding a reference)
     */
    aws_custom_key_op_handler_release(&java_custom_key_op_handler->base);
}

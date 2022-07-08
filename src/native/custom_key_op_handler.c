/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "custom_key_op_handler.h"

static void s_aws_custom_key_op_handler_perform_operation(struct aws_custom_key_op_handler *key_op_handler, struct aws_tls_key_operation *operation) {

    struct aws_jni_custom_key_op_handler *op_handler = (struct aws_jni_custom_key_op_handler *)key_op_handler->impl;
    AWS_FATAL_ASSERT(op_handler != NULL);

    // Get the Java ENV
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    AWS_FATAL_ASSERT(env != NULL);

    jbyteArray jni_input_data = NULL;
    jobject jni_operation = NULL;
    bool success = false;

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
        (jlong)operation,
        jni_input_data,
        (jint)aws_tls_key_operation_get_type(operation),
        (jint)aws_tls_key_operation_get_signature_algorithm(operation),
        (jint)aws_tls_key_operation_get_digest_algorithm(operation));
    if (jni_operation == NULL) {
        aws_jni_check_and_clear_exception(env);
        goto clean_up;
    }

    // TODO - handle the situation where a operation is never completed and the tls_context_options is going
    // to be destroyed.

    // Invoke TlsKeyOperationHandler.performOperation() through the invokePerformOperation
    // function. This function will also catch any exceptions and clear the operation
    // with an exception should it occur.
    (*env)->CallVoidMethod(
        env, op_handler->jni_custom_key_op,
        tls_key_operation_handler_properties.invoke_perform_operation_id,
        jni_operation);

    aws_jni_check_and_clear_exception(env);
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

    // Release the Java ENV
    aws_jni_release_thread_env(op_handler->jvm, env);
}

static struct aws_custom_key_op_handler_vtable s_aws_custom_key_op_handler_vtable = {
    .destroy = NULL,
    .on_key_operation = s_aws_custom_key_op_handler_perform_operation,
    .on_ctx_destroy = NULL,
};

static void s_aws_custom_key_op_handler_destroy(struct aws_custom_key_op_handler *impl) {

    struct aws_jni_custom_key_op_handler *op_handler = (struct aws_jni_custom_key_op_handler *)impl->impl;

    // Get the Java ENV
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    if (env == NULL) {
        // TODO: Log/Handle error!
        return;
    }

    // Release the global reference
    if (op_handler->jni_custom_key_op) {
        // Let the customer clean up their stuff if needed
        (*env)->CallVoidMethod(
            env, op_handler->jni_custom_key_op,
            tls_key_operation_handler_properties.invoke_on_cleanup_id);
        (*env)->DeleteGlobalRef(env, op_handler->jni_custom_key_op);
    }

    // Release the Java ENV
    aws_jni_release_thread_env(op_handler->jvm, env);

    aws_mem_release(impl->allocator, impl);
}

static struct aws_custom_key_op_handler *s_aws_custom_key_op_handler_new(
    struct aws_allocator *allocator,
    struct aws_jni_custom_key_op_handler *op_handler) {

    struct aws_custom_key_op_handler *impl =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_custom_key_op_handler));

    impl->allocator = allocator;
    impl->vtable = &s_aws_custom_key_op_handler_vtable;
    aws_ref_count_init(
        &impl->ref_count, impl, (aws_simple_completion_callback *)s_aws_custom_key_op_handler_destroy);

    impl->impl = (void *)op_handler;

    // Get the Java ENV
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    if (env == NULL) {
        // TODO: Log/Handle error!
        return NULL;
    }

    // Make the global reference
    if (op_handler->jni_custom_key_op != NULL) {
        op_handler->jni_custom_key_op = (*env)->NewGlobalRef(env, op_handler->jni_custom_key_op);
    }

    // Release the Java ENV
    aws_jni_release_thread_env(op_handler->jvm, env);

    return impl;
}

// ============================================


struct aws_jni_custom_key_op_handler *aws_custom_key_op_handler_java_new(JNIEnv *env, struct aws_allocator *allocator, jobject jni_custom_key_op) {
    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler = aws_mem_calloc(allocator, 1, sizeof(struct aws_jni_custom_key_op_handler));
    if (java_custom_key_op_handler == NULL) {
        return NULL;
    }

    if ((*env)->GetJavaVM(env, &java_custom_key_op_handler->jvm) != 0) {
        aws_jni_throw_runtime_exception(env, "failed to get JVM");
        aws_mem_release(allocator, java_custom_key_op_handler);
        return NULL;
    }

    java_custom_key_op_handler->jni_custom_key_op = jni_custom_key_op;
    java_custom_key_op_handler->key_handler = s_aws_custom_key_op_handler_new(allocator, java_custom_key_op_handler);

    AWS_LOGF_DEBUG(AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p: Initalizing Custom Key Operations", (void *)java_custom_key_op_handler);
    return java_custom_key_op_handler;
}

void aws_custom_key_op_handler_java_release(struct aws_allocator *allocator, struct aws_jni_custom_key_op_handler *java_custom_key_op_handler) {
    AWS_PRECONDITION(!java_custom_key_op_handler);
    if (!java_custom_key_op_handler) {
        return;
    }
    AWS_LOGF_DEBUG(AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p: Destroying Custom Key Operations", (void *)java_custom_key_op_handler);

    // (Potentially) Release the reference
    if (java_custom_key_op_handler->key_handler != NULL) {
        aws_ref_count_release(&java_custom_key_op_handler->key_handler->ref_count);
    }

    /* Frees allocated memory */
    aws_mem_release(allocator, java_custom_key_op_handler);
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "custom_key_op_handler.h"

static void s_aws_custom_key_op_handler_perform_operation(
    struct aws_custom_key_op_handler *key_op_handler,
    struct aws_tls_key_operation *operation) {

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

    // Invoke TlsKeyOperationHandler.performOperation() through the invokePerformOperation
    // function. This function will also catch any exceptions and clear the operation
    // with an exception should it occur.
    (*env)->CallVoidMethod(
        env,
        op_handler->jni_custom_key_op,
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

static bool s_aws_custom_key_op_handler_get_certificate(
    struct aws_custom_key_op_handler *key_op_handler,
    struct aws_byte_buf *certificate_output) {
    struct aws_jni_custom_key_op_handler *op_handler = (struct aws_jni_custom_key_op_handler *)key_op_handler->impl;
    AWS_FATAL_ASSERT(op_handler != NULL);

    struct aws_allocator *allocator = op_handler->allocator;

    /* certificate needs to be set, but there are multiple ways to return it */
    if ((op_handler->cert_file_path.ptr != NULL) && (op_handler->cert_file_contents.ptr != NULL)) {
        return false;
    } else if (op_handler->cert_file_path.ptr != NULL) {
        struct aws_string *tmp_string = aws_string_new_from_cursor(allocator, &op_handler->cert_file_path);
        int op = aws_byte_buf_init_from_file(certificate_output, allocator, aws_string_c_str(tmp_string));
        aws_string_destroy(tmp_string);
        if (op != AWS_OP_SUCCESS) {
            return false;
        }
    } else if (op_handler->cert_file_contents.ptr != NULL) {
        if (aws_byte_buf_init_copy_from_cursor(certificate_output, allocator, op_handler->cert_file_contents)) {
            return false;
        }
    } else {
        return false;
    }
    return true;
}

static void s_aws_custom_key_op_handler_destroy(struct aws_custom_key_op_handler *key_op_handler) {

    struct aws_jni_custom_key_op_handler *op_handler = (struct aws_jni_custom_key_op_handler *)key_op_handler->impl;

    // Get the Java ENV
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    if (env == NULL) {
        // JVM is likely shutting down. Do not crash but log error.
        AWS_LOGF_ERROR(
            AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p destroy: Could not get Java ENV!", (void *)op_handler);
        return;
    }

    // Release the global reference
    if (op_handler->jni_custom_key_op) {
        // Let the customer clean up their stuff if needed
        (*env)->CallVoidMethod(
            env, op_handler->jni_custom_key_op, tls_key_operation_handler_properties.invoke_on_cleanup_id);

        (*env)->DeleteGlobalRef(env, op_handler->jni_custom_key_op);
    }

    // Release the Java ENV
    aws_jni_release_thread_env(op_handler->jvm, env);

    aws_mem_release(op_handler->allocator, key_op_handler);

    // Release the Java struct
    aws_mem_release(op_handler->allocator, op_handler);
}

static struct aws_custom_key_op_handler_vtable s_aws_custom_key_op_handler_vtable = {
    .destroy = s_aws_custom_key_op_handler_destroy,
    .on_key_operation = s_aws_custom_key_op_handler_perform_operation,
    .get_certificate = s_aws_custom_key_op_handler_get_certificate,
};

static struct aws_custom_key_op_handler *s_aws_custom_key_op_handler_new(
    struct aws_allocator *allocator,
    struct aws_jni_custom_key_op_handler *op_handler) {

    struct aws_custom_key_op_handler *key_op_handler = aws_custom_key_op_handler_new(allocator);
    key_op_handler->vtable = &s_aws_custom_key_op_handler_vtable;
    key_op_handler->impl = (void *)op_handler;

    // Get the Java ENV
    JNIEnv *env = aws_jni_acquire_thread_env(op_handler->jvm);
    if (env == NULL) {
        // JVM is likely shutting down. Do not crash but log error.
        AWS_LOGF_ERROR(
            AWS_LS_COMMON_IO, "java_custom_key_op_handler=%p new: Could not get Java ENV!", (void *)op_handler);
        return NULL;
    }

    // Make the global reference
    if (op_handler->jni_custom_key_op != NULL) {
        op_handler->jni_custom_key_op = (*env)->NewGlobalRef(env, op_handler->jni_custom_key_op);
    }

    // Release the Java ENV
    aws_jni_release_thread_env(op_handler->jvm, env);

    return key_op_handler;
}

struct aws_jni_custom_key_op_handler *aws_custom_key_op_handler_java_new(
    JNIEnv *env,
    struct aws_allocator *allocator,
    jobject jni_custom_key_op) {

    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_jni_custom_key_op_handler));
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
    java_custom_key_op_handler->allocator = allocator;

    AWS_LOGF_DEBUG(
        AWS_LS_COMMON_IO,
        "java_custom_key_op_handler=%p: Initalizing Custom Key Operations",
        (void *)java_custom_key_op_handler);
    return java_custom_key_op_handler;
}

void aws_custom_key_op_handler_java_release(
    struct aws_allocator *allocator,
    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler) {

    (void)allocator;

    if (!java_custom_key_op_handler) {
        return;
    }
    AWS_LOGF_DEBUG(
        AWS_LS_COMMON_IO,
        "java_custom_key_op_handler=%p: Releasing Custom Key Operations (may destroy custom key operations if "
        "this Java class holds the last reference)",
        (void *)java_custom_key_op_handler);

    // Release the reference (which will only clean everything up if this is the last thing holding a reference)
    if (java_custom_key_op_handler->key_handler != NULL) {
        java_custom_key_op_handler->key_handler =
            aws_custom_key_op_handler_release(java_custom_key_op_handler->key_handler);
    }
}

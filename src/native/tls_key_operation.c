/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/io/tls_channel_handler.h>

#include "crt.h"
#include "java_class_ids.h"

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'aws_pkcs11_lib *' */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_TlsKeyOperation_tlsKeyOperationComplete(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_operation,
    jbyteArray jni_output_data) {

    (void)jni_class;
    struct aws_tls_key_operation *operation = (struct aws_tls_key_operation *)jni_operation;

    struct aws_byte_cursor output_data = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_output_data);

    aws_tls_key_operation_complete(operation, output_data);

    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_output_data, output_data);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_TlsKeyOperation_tlsKeyOperationCompleteExceptionally(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_operation,
    jthrowable jni_throwable) {

    (void)jni_class;
    struct aws_tls_key_operation *operation = (struct aws_tls_key_operation *)jni_operation;

    int error_code = 0;
    if ((*env)->IsInstanceOf(env, jni_throwable, crt_runtime_exception_properties.crt_runtime_exception_class)) {
        error_code = (*env)->GetIntField(env, jni_throwable, crt_runtime_exception_properties.error_code_field_id);
    }

    if (error_code == 0) {
        error_code = AWS_ERROR_UNKNOWN; /* is there anything more that could be done here? */
    }

    aws_tls_key_operation_complete_with_error(operation, error_code);
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

#include "crt.h"
#include "java_class_ids.h"
#include "tls_context_pkcs11_options.h"

/**
 * A struct that contains all the data for a Java custom_key_op_handler that is exposed
 * in Java so the customer can perform their own custom private key operation handler functions.
 */
struct aws_jni_custom_key_op_handler {
    JavaVM *jvm;

    // A reference to the Java class that this struct is linked to.
    // The interface, strings, etc, can be gotten from this class.
    jobject jni_custom_key_op;

    // The C class containing the key operations. We extend/define it's VTable to allow
    // us to have it call into the customer's Java code.
    struct aws_custom_key_op_handler *key_handler;

    // The allocator to use
    struct aws_allocator *allocator;

    /**
     * Certificate's file path on disk (UTF-8).
     * The certificate must be PEM formatted and UTF-8 encoded.
     * Zero out if passing in certificate by some other means (such as file contents).
     * (Can also be zero out if it is unused, like in PKCS11 implementation)
     */
    struct aws_byte_cursor cert_file_path;

    /**
     * Certificate's file contents (UTF-8).
     * The certificate must be PEM formatted and UTF-8 encoded.
     * Zero out if passing in certificate by some other means (such as file path).
     * (Can also be zero out if it is unused, like in PKCS11 implementation)
     */
    struct aws_byte_cursor cert_file_contents;
};

struct aws_jni_custom_key_op_handler *aws_custom_key_op_handler_java_new(JNIEnv *env, struct aws_allocator *allocator, jobject jni_custom_key_op);
void aws_custom_key_op_handler_java_release(struct aws_allocator *allocator, struct aws_jni_custom_key_op_handler *java_custom_key_op_handler);

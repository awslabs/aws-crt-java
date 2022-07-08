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

// TODO - be implemented/used for TlsKeyOperationHandler.java instead of TlsContextCustomKeyOperationOptions.java

// TODO - document
struct aws_jni_custom_key_op_handler {
    JavaVM *jvm;

    // A reference to the Java class that this struct is linked to.
    // The interface, strings, etc, can be gotten from this class.
    jobject jni_custom_key_op;

    // The C class containing the key operations. We extend/define it's VTable to allow
    // us to have it call into the customer's Java code.
    struct aws_custom_key_op_handler *key_handler;
};

struct aws_jni_custom_key_op_handler *aws_custom_key_op_handler_java_new(JNIEnv *env, struct aws_allocator *allocator, jobject jni_custom_key_op);
void aws_custom_key_op_handler_java_release(struct aws_allocator *allocator, struct aws_jni_custom_key_op_handler *java_custom_key_op_handler);

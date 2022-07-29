/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#ifndef AWS_JNI_CRT_CUSTOM_KEY_OP_HANDLER_H
#define AWS_JNI_CRT_CUSTOM_KEY_OP_HANDLER_H

#include <jni.h>
#include "crt.h"

/**
 * A struct that contains all the data for a Java custom_key_op_handler that is exposed
 * in Java so the customer can perform their own custom private key operation handler functions.
 */
struct aws_jni_custom_key_op_handler;

struct aws_jni_custom_key_op_handler *aws_custom_key_op_handler_java_new(
    JNIEnv *env,
    jobject jni_custom_key_op);

void aws_custom_key_op_handler_java_release(
    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler);

struct aws_custom_key_op_handler *aws_custom_key_op_handler_java_get_handler(
    struct aws_jni_custom_key_op_handler *java_custom_key_op_handler);

#endif /* AWS_JNI_CRT_CUSTOM_KEY_OP_HANDLER_H */

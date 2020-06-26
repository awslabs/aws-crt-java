/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_CREDENTIALS_H
#define AWS_JNI_CRT_CREDENTIALS_H

#include <jni.h>

struct aws_credentials;

struct aws_credentials *aws_credentials_new_from_java_credentials(JNIEnv *env, jobject java_credentials);

#endif /* AWS_JNI_CRT_CREDENTIALS_H */

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_CREDENTIALS_H
#define AWS_JNI_CRT_CREDENTIALS_H

#include <jni.h>

struct aws_credentials;

struct aws_credentials *aws_credentials_new_from_java_credentials(JNIEnv *env, jobject java_credentials);
jobject aws_java_credentials_from_native_new(JNIEnv *env, const struct aws_credentials *credentials);

#endif /* AWS_JNI_CRT_CREDENTIALS_H */

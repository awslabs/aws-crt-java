/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

#ifndef AWS_JNI_CRT_H
#define AWS_JNI_CRT_H

#include <aws/common/byte_buf.h>
#include <aws/common/common.h>
#include <jni.h>

struct aws_allocator *aws_jni_get_allocator();

/*******************************************************************************
 * aws_jni_throw_runtime_exception - throws a crt.CrtRuntimeException with the
 * supplied message, sprintf formatted. Control WILL return from this function, 
 * so after calling it, make sure to clean up any native resources before exiting
 * the calling JNIEXPORT function.
 ******************************************************************************/
void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...);

/*******************************************************************************
 * aws_jni_byte_cursor_from_jstring - Creates an aws_byte_cursor from the UTF-8
 * characters extracted from the supplied jstring. The string value is null-terminated.
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jstring(JNIEnv *env, jstring str);

/*******************************************************************************
 * aws_jni_get_thread_env - Gets the JNIEnv for the current thread from the VM,
 * attaching the env if necessary
 ******************************************************************************/
JNIEnv *aws_jni_get_thread_env(JavaVM *jvm);

#endif /* AWS_JNI_CRT_H */

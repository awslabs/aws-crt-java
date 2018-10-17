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

#include <aws/jni/com_amazon_aws_CrtTest.h>
#include <aws/common/common.h>
#include <crt.h>

#include <stdio.h>

JNIEXPORT
void JNICALL Java_com_amazon_aws_CrtTest_doIt(JNIEnv *env, jobject obj) {
    (void)env;
    (void)obj;
    printf("I DID THE THING\n");
}

JNIEXPORT void JNICALL Java_com_amazon_aws_CrtTest_throwRuntimeExceptionNew(JNIEnv *env, jobject obj) {
    (void)obj;
    jclass runtime_exception = (*env)->FindClass(env, "com/amazon/aws/RuntimeException");
    (*env)->ThrowNew(env, runtime_exception, "Test RuntimeException via new");
}

JNIEXPORT void JNICALL Java_com_amazon_aws_CrtTest_throwRuntimeExceptionAPI(JNIEnv *env, jobject obj) {
    (void)obj;
    aws_jni_throw_runtime_exception(env, "Test RuntimeException via API");
}

struct aws_allocator *aws_jni_get_allocator() {
    return aws_default_allocator();
}

void aws_jni_throw_runtime_exception(JNIEnv * env, const char *msg) {
    char buf[1024];
    jclass runtime_exception = (*env)->FindClass(env, "com/amazon/aws/RuntimeException");
    snprintf(buf, sizeof(buf), "%s: %s", msg, aws_error_str(aws_last_error()));
    (*env)->ThrowNew(env, runtime_exception, buf);
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jstring(JNIEnv *env, jstring str) {
    return aws_byte_cursor_from_array((*env)->GetStringUTFChars(env, str, NULL), (*env)->GetStringUTFLength(env, str));
}

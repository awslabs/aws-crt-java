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

#include <aws/common/common.h>
#include <aws/io/io.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/mqtt/mqtt.h>

#include <stdio.h>

#include "crt.h"

struct aws_allocator *aws_jni_get_allocator() {
    return aws_default_allocator();
}

void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    char buf[1024];
    vsnprintf(buf, sizeof(buf), msg, args);
    va_end(args);

    char exception[1280];
    snprintf(exception, sizeof(exception), "%s (aws_last_error: %s)", buf, aws_error_str(aws_last_error()));
    jclass runtime_exception = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtRuntimeException");
    (*env)->ThrowNew(env, runtime_exception, exception);
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jstring(JNIEnv *env, jstring str) {
    return aws_byte_cursor_from_array(
        (*env)->GetStringUTFChars(env, str, NULL), (size_t)(*env)->GetStringUTFLength(env, str));
}

JNIEnv *aws_jni_get_thread_env(JavaVM *jvm) {
    JNIEnv *env = NULL;
    jint result = (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
    assert(result == JNI_OK);
    return env;
}

static void s_cache_jni_classes(JNIEnv *env) {
    extern void s_cache_connect_options(JNIEnv *);
    extern void s_cache_async_callback(JNIEnv *);
    extern void s_cache_client_callbacks(JNIEnv *);
    extern void s_cache_message_handler(JNIEnv *);
    s_cache_connect_options(env);
    s_cache_async_callback(env);
    s_cache_client_callbacks(env);
    s_cache_message_handler(env);
}

static void s_jni_atexit() {
    aws_tls_clean_up_static_state();
}

/* Called as the entry point, immediately after the shared lib is loaded the first time by JNI */
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_aws_1crt_1init(JNIEnv *env, jclass jni_crt_class) {
    aws_load_error_strings();
    aws_io_load_error_strings();
    aws_mqtt_load_error_strings();

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_tls_init_static_state(allocator);

    s_cache_jni_classes(env);

    atexit(s_jni_atexit);
}

#if defined(ENABLE_JNI_TESTS)
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_testing_CrtTest_doIt(JNIEnv *env, jobject obj) {
    (void)env;
    (void)obj;
    printf("I DID THE THING\n");
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_testing_CrtTest_throwRuntimeExceptionNew(JNIEnv *env, jobject obj) {
    (void)obj;
    jclass runtime_exception = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtRuntimeException");
    (*env)->ThrowNew(env, runtime_exception, "Test RuntimeException via new");
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_testing_CrtTest_throwRuntimeExceptionAPI(JNIEnv *env, jobject obj) {
    (void)obj;
    aws_jni_throw_runtime_exception(env, "Test RuntimeException via API");
}
#endif /* ENABLE_JNI_TESTS */

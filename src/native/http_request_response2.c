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

#include <crt.h>
#include <jni.h>
#include <string.h>

#include <aws/http/request_response.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'jni_tls_ctx_options *' */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif


JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2New(
    JNIEnv *env,
    jclass jni_class) {

    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_http_message *request = aws_http_message_new_request(allocator);
    if (request == NULL) {
        aws_jni_throw_runtime_exception(
                env, "HttpRequest2.httpRequest2New: failed to create request");
    }

    return (jlong)request;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2Destroy(
    JNIEnv *env,
    jclass jni_cp,
    jlong request_addr) {
    (void)jni_cp;
    struct aws_http_message *request = (struct aws_http_message *)request_addr;
    if (!request) {
        aws_jni_throw_runtime_exception(
            env, "HttpRequest2.httpRequest2Destroy: instance should be non-null at destruction time");
        return;
    }

    aws_http_message_destroy(request);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2SetBodyStream(
    JNIEnv *env,
    jclass jni_cp,
    jlong request_addr,
    jlong stream_addr) {

    (void)jni_cp;
    struct aws_http_message *request = (struct aws_http_message *)request_addr;
    struct aws_input_stream *body_stream = (struct aws_input_stream *)stream_addr;

    aws_http_message_set_body_stream(request, body_stream);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2SetMethod(
    JNIEnv *env,
    jclass jni_cp,
    jlong request_addr,
    jstring java_method) {

    (void)jni_cp;
    struct aws_http_message *request = (struct aws_http_message *)request_addr;

    struct aws_byte_cursor method_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, java_method);
    aws_http_message_set_request_method(request, method_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, java_method, method_cursor);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2SetPath(
    JNIEnv *env,
    jclass jni_cp,
    jlong request_addr,
    jstring java_path) {

    (void)jni_cp;
    struct aws_http_message *request = (struct aws_http_message *)request_addr;

    struct aws_byte_cursor path_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, java_path);
    aws_http_message_set_request_path(request, path_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, java_path, path_cursor);
}

static jstring s_jstring_from_cursor(JNIEnv *env, struct aws_byte_cursor *cursor) {
    struct aws_byte_buf temp_buf;
    if (aws_byte_buf_init(&temp_buf, aws_jni_get_allocator(), cursor->len + 1)) {
        return NULL;
    }

    if (aws_byte_buf_append(&temp_buf, cursor)) {
        return NULL;
    }

    temp_buf.buffer[temp_buf.len++] = 0;

    jstring java_string = (*env)->NewStringUTF(env, (const char *) temp_buf.buffer);

    aws_byte_buf_clean_up(&temp_buf);

    return java_string;
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2GetPath(
    JNIEnv *env,
    jclass jni_cp,
    jlong request_addr) {

    (void)jni_cp;
    struct aws_http_message *request = (struct aws_http_message *)request_addr;

    struct aws_byte_cursor path_cursor;
    if (aws_http_message_get_request_path(request, &path_cursor)) {
        return NULL;
    }

    return s_jstring_from_cursor(env, &path_cursor);
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_http_HttpRequest2_httpRequest2GetMethod(
    JNIEnv *env,
    jclass jni_cp,
    jlong request_addr) {

    (void)jni_cp;
    struct aws_http_message *request = (struct aws_http_message *)request_addr;

    struct aws_byte_cursor method_cursor;
    if (aws_http_message_get_request_method(request, &method_cursor)) {
        return NULL;
    }

    return s_jstring_from_cursor(env, &method_cursor);
}


#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif
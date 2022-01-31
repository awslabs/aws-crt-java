/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_HTTP_REQUEST_RESPONSE_H
#define AWS_JNI_CRT_HTTP_REQUEST_RESPONSE_H

#include <jni.h>

struct aws_http_message;
struct aws_http_stream;
struct aws_byte_buf;
struct aws_atomic_var;

struct http_stream_binding {
    JavaVM *jvm;

    // TEMP: Until Java API changes to match "H1B" native HTTP API,
    // create aws_http_message and aws_input_stream under the hood.
    struct aws_http_message *native_request;

    jobject java_http_response_stream_handler;
    jobject java_http_stream;
    struct aws_http_stream *native_stream;
    struct aws_byte_buf headers_buf;
    int response_status;

    /*
     * Unactivated streams must have their callback data destroyed at release time
     */
    struct aws_atomic_var activated;
};

jobject aws_java_http_stream_from_native_new(JNIEnv *env, void *opaque, int version);
void aws_java_http_stream_from_native_delete(JNIEnv *env, jobject jHttpStream);

void aws_http_stream_binding_destroy(JNIEnv *env, struct http_stream_binding *callback);

// If error occurs, A Java exception is thrown and NULL is returned.
struct http_stream_binding *aws_http_stream_binding_alloc(JNIEnv *env, jobject java_callback_handler);

#endif /* AWS_JNI_CRT_HTTP_REQUEST_RESPONSE_H */

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_HTTP_REQUEST_RESPONSE_H
#define AWS_JNI_CRT_HTTP_REQUEST_RESPONSE_H

#include <aws/http/request_response.h>
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
    jobject java_http_stream_base;
    struct aws_http_stream *native_stream;
    struct aws_byte_buf headers_buf;
    int response_status;

    /* For the native http stream and the Java stream object */
    struct aws_atomic_var ref;
};

jobject aws_java_http_stream_from_native_new(JNIEnv *env, void *opaque, int version);
void aws_java_http_stream_from_native_delete(JNIEnv *env, jobject jHttpStream);

void *aws_http_stream_binding_release(JNIEnv *env, struct http_stream_binding *binding);
void *aws_http_stream_binding_acquire(struct http_stream_binding *binding);

// If error occurs, A Java exception is thrown and NULL is returned.
struct http_stream_binding *aws_http_stream_binding_new(JNIEnv *env, jobject java_callback_handler);

/* Default callbacks using binding */
int aws_java_http_stream_on_incoming_headers_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    const struct aws_http_header *header_array,
    size_t num_headers,
    void *user_data);
int aws_java_http_stream_on_incoming_header_block_done_fn(
    struct aws_http_stream *stream,
    enum aws_http_header_block block_type,
    void *user_data);
int aws_java_http_stream_on_incoming_body_fn(
    struct aws_http_stream *stream,
    const struct aws_byte_cursor *data,
    void *user_data);
void aws_java_http_stream_on_stream_complete_fn(struct aws_http_stream *stream, int error_code, void *user_data);
void aws_java_http_stream_on_stream_destroy_fn(void *user_data);

#endif /* AWS_JNI_CRT_HTTP_REQUEST_RESPONSE_H */

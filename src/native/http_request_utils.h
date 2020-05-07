/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

#ifndef AWS_JNI_CRT_HTTP_REQUEST_UTILS_H
#define AWS_JNI_CRT_HTTP_REQUEST_UTILS_H

#include <jni.h>

#include <aws/common/byte_buf.h>

struct aws_allocator;
struct aws_http_header;
struct aws_http_headers;
struct aws_http_message;
struct aws_input_stream;

struct aws_input_stream *aws_input_stream_new_from_java_http_request_body_stream(
    struct aws_allocator *allocator,
    JNIEnv *env,
    jobject http_request_body_stream);

struct aws_http_message *aws_http_request_new_from_java_http_request(
    JNIEnv *env,
    jbyteArray marshalled_request,
    jobject jni_body_stream);

int aws_marshal_http_headers_to_dynamic_buffer(
    struct aws_byte_buf *buf,
    const struct aws_http_header *header_array,
    size_t num_headers);

/* if this fails a java exception has been set. */
int aws_apply_java_http_request_changes_to_native_request(
    JNIEnv *env,
    jbyteArray marshalled_request,
    jobject jni_body_stream,
    struct aws_http_message *message);

/* if this fails a java exception has been set. */
jobject aws_java_http_request_from_native(JNIEnv *env, struct aws_http_message *message, jobject request_body_stream);

#endif /* AWS_JNI_CRT_HTTP_REQUEST_UTILS_H */

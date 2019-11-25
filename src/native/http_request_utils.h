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

struct aws_allocator;
struct aws_http_message;
struct aws_input_stream;

struct aws_input_stream *aws_input_stream_new_from_java_http_request_body_stream(
    struct aws_allocator *allocator,
    JNIEnv *env,
    jobject http_request_body_stream);

struct aws_http_message *aws_http_request_new_from_java_http_request(
    JNIEnv *env,
    jstring jni_method,
    jstring jni_uri,
    jobjectArray jni_headers,
    jobject jni_body_stream);

#endif /* AWS_JNI_CRT_HTTP_REQUEST_UTILS_H */

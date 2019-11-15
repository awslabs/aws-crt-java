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

#include "http_request_body_stream.h"

#include <crt.h>
#include <jni.h>


static struct {
    jmethodID send_outgoing_body;
    jmethodID reset_position;
} s_http_request_body_stream;

void s_cache_http_request_body_stream(JNIEnv *) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpRequestBodyStream");
    AWS_FATAL_ASSERT(cls);

    s_http_request_body_stream.send_outgoing_body = (*env)->GetMethodID(
            env, cls, "sendRequestBody", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;Ljava/nio/ByteBuffer;)Z");
    AWS_FATAL_ASSERT(s_crt_http_stream_handler.send_outgoing_body);

    s_http_request_body_stream.reset_position = (*env)->GetMethodID(
            env, cls, "resetPosition", "()V");
    AWS_FATAL_ASSERT(s_crt_http_stream_handler.reset_position);
}

struct aws_http_request_body_stream_impl {
    JavaVM *jvm;
    jobject http_request_body_stream;
    jobject http_stream;
    bool body_done;
};

static int s_aws_input_stream_seek(
    struct aws_input_stream *stream,
    aws_off_t offset,
    enum aws_stream_seek_basis basis) {
    struct aws_http_request_body_stream_impl *impl = stream->impl;

    int result = AWS_OP_SUCCESS;
    if (impl->http_request_body_stream != NULL) {
        if (basis != AWS_SSB_BEGIN || offset != 0) {
            return AWS_OP_ERR;
        }

        JNIEnv *env = aws_jni_get_thread_env(impl->jvm);
        if (!(*env)->CallBooleanMethod(env, impl->http_request_body_stream,
                                            s_http_request_body_stream.reset_position)) {
            result = AWS_OP_ERR;
        }
    }

    if (result == AWS_OP_SUCCESS) {
        impl->body_done = false;
    }

    return result;
}

static int s_aws_input_stream_read(struct aws_input_stream *stream, struct aws_byte_buf *dest) {
    struct aws_http_request_body_stream_impl *impl = stream->impl;

    if (impl->http_request_body_stream == NULL) {
        impl->body_done = true;
        return AWS_OP_SUCCESS;
    }

    if (impl->body_done) {
        return AWS_OP_SUCCESS;
    }

    JNIEnv *env = aws_jni_get_thread_env(impl->jvm);

    size_t out_remaining = dst->capacity - dst->len;

    jobject direct_buffer = aws_jni_direct_byte_buffer_from_raw_ptr(env, dst->buffer + dst->len, out_remaining);

    impl->body_done = (*env)->CallBooleanMethod(
            env,
            impl->http_request_body_stream,
            s_http_request_body_stream.send_outgoing_body,
            impl->http_stream,
            direct_buffer);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    size_t amt_written = aws_jni_byte_buffer_get_position(env, direct_buffer);
    dst->len += amt_written;

    (*env)->DeleteLocalRef(env, direct_buffer);

    return AWS_OP_SUCCESS;
}

static int s_aws_input_stream_get_status(struct aws_input_stream *stream, struct aws_stream_status *status) {
    struct aws_http_request_body_stream_impl *impl = stream->impl;

    status->is_end_of_stream = impl->body_done;
    status->is_valid = true;

    return AWS_OP_SUCCESS;
}

static int s_aws_input_stream_get_length(struct aws_input_stream *stream, int64_t *length) {
    (void)stream;
    (void)length;

    return AWS_OP_ERR;
}

static void s_aws_input_stream_destroy(struct aws_input_stream *stream) {
    struct aws_http_request_body_stream_impl *impl = stream->impl;
    JNIEnv *env = aws_jni_get_thread_env(impl->jvm);

    if (impl->http_stream != NULL) {
        (*env)->DeleteGlobalRef(env, impl->http_stream);
    }

    if (impl->http_request_body_stream != NULL) {
        (*env)->DeleteGlobalRef(env, impl->http_request_body_stream);
    }

    aws_mem_release(stream->allocator, stream);
}

static struct aws_input_stream_vtable s_aws_input_stream_vtable = {
    .seek = s_aws_input_stream_seek,
    .read = s_aws_input_stream_read,
    .get_status = s_aws_input_stream_get_status,
    .get_length = s_aws_input_stream_get_length,
    .destroy = s_aws_input_stream_destroy,
    };

struct aws_input_stream *aws_input_stream_new_from_java_http_request_body_stream(struct aws_allocator *allocator, JNIEnv *env, jobject http_request_body_stream, jobject http_stream) {
    struct aws_input_stream *input_stream = NULL;
    struct aws_http_request_body_stream_impl *impl = NULL;

    aws_mem_acquire_many(
            allocator, 2, &input_stream, sizeof(struct aws_input_stream), &impl, sizeof(struct aws_http_request_body_stream_impl));

    if (!input_stream) {
        return NULL;
    }

    AWS_ZERO_STRUCT(*input_stream);
    AWS_ZERO_STRUCT(*impl);

    input_stream->allocator = allocator;
    input_stream->vtable = &s_aws_input_stream_vtable;
    input_stream->impl = impl;

    jint jvmresult = (*env)->GetJavaVM(env, &impl->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    impl->http_stream = (*env)->NewGlobalRef(env, http_stream);
    if (impl->http_stream == NULL) {
        goto on_error;
    }

    if (http_request_body_stream != NULL) {
        impl->http_request_body_stream = (*env)->NewGlobalRef(env, http_request_body_stream);
        if (impl->http_request_body_stream == NULL) {
            goto on_error;
        }
    } else {
        impl->body_done = true;
    }

    return input_stream;

on_error:

    aws_input_stream_destroy(input_stream);

    return NULL;
}

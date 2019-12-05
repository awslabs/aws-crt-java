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

#include "http_request_utils.h"

#include "crt.h"
#include "java_class_ids.h"

#include <aws/http/http.h>
#include <aws/http/request_response.h>
#include <aws/io/stream.h>

#if _MSC_VER
#    pragma warning(disable : 4204) /* non-constant aggregate initializer */
#endif

struct aws_http_request_body_stream_impl {
    JavaVM *jvm;
    jobject http_request_body_stream;
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
        if (!(*env)->CallBooleanMethod(
                env, impl->http_request_body_stream, http_request_body_stream_properties.reset_position)) {
            result = AWS_OP_ERR;
        }

        if ((*env)->ExceptionCheck(env)) {
            return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
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

    size_t out_remaining = dest->capacity - dest->len;

    jobject direct_buffer = aws_jni_direct_byte_buffer_from_raw_ptr(env, dest->buffer + dest->len, out_remaining);

    impl->body_done = (*env)->CallBooleanMethod(
        env, impl->http_request_body_stream, http_request_body_stream_properties.send_outgoing_body, direct_buffer);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    size_t amt_written = aws_jni_byte_buffer_get_position(env, direct_buffer);
    dest->len += amt_written;

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

struct aws_input_stream *aws_input_stream_new_from_java_http_request_body_stream(
    struct aws_allocator *allocator,
    JNIEnv *env,
    jobject http_request_body_stream) {
    struct aws_input_stream *input_stream = NULL;
    struct aws_http_request_body_stream_impl *impl = NULL;

    aws_mem_acquire_many(
        allocator,
        2,
        &input_stream,
        sizeof(struct aws_input_stream),
        &impl,
        sizeof(struct aws_http_request_body_stream_impl));

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

struct aws_http_message *aws_http_request_new_from_java_http_request(
    JNIEnv *env,
    jstring jni_method,
    jstring jni_uri,
    jobjectArray jni_headers,
    jobject jni_body_stream) {

    struct aws_http_message *request = aws_http_message_new_request(aws_jni_get_allocator());
    if (request == NULL) {
        aws_jni_throw_runtime_exception(env, "aws_http_request_new_from_java_http_request: Unable to allocate request");
        return NULL;
    }

    struct aws_byte_cursor method_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_method);
    int result = aws_http_message_set_request_method(request, method_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, jni_method, method_cursor);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Unable to set Method");
        goto on_error;
    }

    struct aws_byte_cursor path_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, jni_uri);
    result = aws_http_message_set_request_path(request, path_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, jni_uri, path_cursor);
    if (result != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Unable to set Path");
        goto on_error;
    }

    jsize num_headers = (*env)->GetArrayLength(env, jni_headers);
    for (jsize i = 0; i < num_headers; ++i) {
        jobject jHeader = (*env)->GetObjectArrayElement(env, jni_headers, i);
        jbyteArray jname = (*env)->GetObjectField(env, jHeader, http_header_properties.name);
        jbyteArray jvalue = (*env)->GetObjectField(env, jHeader, http_header_properties.value);

        const size_t name_len = (*env)->GetArrayLength(env, jname);
        const size_t value_len = (*env)->GetArrayLength(env, jvalue);

        jbyte *name = (*env)->GetPrimitiveArrayCritical(env, jname, NULL);
        struct aws_byte_cursor name_cursor = aws_byte_cursor_from_array(name, name_len);

        jbyte *value = (*env)->GetPrimitiveArrayCritical(env, jvalue, NULL);
        struct aws_byte_cursor value_cursor = aws_byte_cursor_from_array(value, value_len);

        struct aws_http_header c_header = {
            .name = name_cursor,
            .value = value_cursor,
        };

        result = aws_http_message_add_header(request, c_header);

        (*env)->ReleasePrimitiveArrayCritical(env, jname, name, 0);
        (*env)->ReleasePrimitiveArrayCritical(env, jvalue, value, 0);

        (*env)->DeleteLocalRef(env, jname);
        (*env)->DeleteLocalRef(env, jvalue);
        (*env)->DeleteLocalRef(env, jHeader);

        if (result != AWS_OP_SUCCESS) {
            aws_jni_throw_runtime_exception(env, "HttpClientConnection.MakeRequest: Header[%d] error", i);
            goto on_error;
        }
    }

    if (jni_body_stream != NULL) {
        struct aws_input_stream *body_stream =
            aws_input_stream_new_from_java_http_request_body_stream(aws_jni_get_allocator(), env, jni_body_stream);
        if (body_stream == NULL) {
            aws_jni_throw_runtime_exception(env, "aws_fill_out_request: Error building body stream");
            goto on_error;
        }

        aws_http_message_set_body_stream(request, body_stream);
    }

    return request;

on_error:

    /* Don't need to destroy input stream since it's the last thing created */
    aws_http_message_destroy(request);

    return NULL;
}

jobjectArray aws_java_headers_array_from_native(
    JNIEnv *env,
    const struct aws_http_header *header_array,
    size_t num_headers) {

    jobjectArray jArray = (*env)->NewObjectArray(env, (jsize)num_headers, http_header_properties.header_class, NULL);

    for (size_t i = 0; i < num_headers; i++) {
        jobject jHeader =
            (*env)->NewObject(env, http_header_properties.header_class, http_header_properties.constructor);

        jbyteArray actual_name = aws_jni_byte_array_from_cursor(env, &(header_array[i].name));
        jbyteArray actual_value = aws_jni_byte_array_from_cursor(env, &(header_array[i].value));

        // Overwrite with actual values
        (*env)->SetObjectField(env, jHeader, http_header_properties.name, actual_name);
        (*env)->SetObjectField(env, jHeader, http_header_properties.value, actual_value);
        (*env)->SetObjectArrayElement(env, jArray, (jsize)i, jHeader);

        (*env)->DeleteLocalRef(env, actual_name);
        (*env)->DeleteLocalRef(env, actual_value);
        (*env)->DeleteLocalRef(env, jHeader);
    }

    return jArray;
}

jobjectArray aws_java_headers_array_from_http_headers(JNIEnv *env, const struct aws_http_headers *headers) {

    size_t header_count = aws_http_headers_count(headers);
    jobjectArray jArray = (*env)->NewObjectArray(env, (jsize)header_count, http_header_properties.header_class, NULL);

    for (size_t i = 0; i < header_count; i++) {
        struct aws_http_header header;
        AWS_ZERO_STRUCT(header);
        AWS_FATAL_ASSERT(aws_http_headers_get_index(headers, i, &header) == AWS_OP_SUCCESS);

        jobject jHeader =
            (*env)->NewObject(env, http_header_properties.header_class, http_header_properties.constructor);

        jbyteArray actual_name = aws_jni_byte_array_from_cursor(env, &header.name);
        jbyteArray actual_value = aws_jni_byte_array_from_cursor(env, &header.value);

        // Overwrite with actual values
        (*env)->SetObjectField(env, jHeader, http_header_properties.name, actual_name);
        (*env)->SetObjectField(env, jHeader, http_header_properties.value, actual_value);
        (*env)->SetObjectArrayElement(env, jArray, (jsize)i, jHeader);

        (*env)->DeleteLocalRef(env, actual_name);
        (*env)->DeleteLocalRef(env, actual_value);
        (*env)->DeleteLocalRef(env, jHeader);
    }

    return jArray;
}

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

#include <aws/io/stream.h>

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

struct aws_input_stream_byte_array_impl {
    struct aws_byte_buf bytes;
    struct aws_input_stream *cursor_stream;
};

static int s_aws_input_stream_byte_array_seek(
    struct aws_input_stream *stream,
    aws_off_t offset,
    enum aws_stream_seek_basis basis) {

    struct aws_input_stream_byte_array_impl *impl = stream->impl;
    return aws_input_stream_seek(impl->cursor_stream, offset, basis);
}

static int s_aws_input_stream_byte_array_read(struct aws_input_stream *stream, struct aws_byte_buf *dest) {
    struct aws_input_stream_byte_array_impl *impl = stream->impl;
    return aws_input_stream_read(impl->cursor_stream, dest);
}

static int s_aws_input_stream_byte_array_get_status(struct aws_input_stream *stream, struct aws_stream_status *status) {

    struct aws_input_stream_byte_array_impl *impl = stream->impl;
    return aws_input_stream_get_status(impl->cursor_stream, status);
}

static int s_aws_input_stream_byte_array_get_length(struct aws_input_stream *stream, int64_t *out_length) {
    struct aws_input_stream_byte_array_impl *impl = stream->impl;
    return aws_input_stream_get_length(impl->cursor_stream, out_length);
}

static void s_aws_input_stream_byte_array_destroy(struct aws_input_stream *stream) {
    if (stream == NULL) {
        return;
    }

    struct aws_input_stream_byte_array_impl *impl = stream->impl;
    aws_byte_buf_clean_up(&impl->bytes);

    aws_mem_release(stream->allocator, stream);
}

static struct aws_input_stream_vtable s_aws_input_stream_byte_array_vtable = {
    .seek = s_aws_input_stream_byte_array_seek,
    .read = s_aws_input_stream_byte_array_read,
    .get_status = s_aws_input_stream_byte_array_get_status,
    .get_length = s_aws_input_stream_byte_array_get_length,
    .destroy = s_aws_input_stream_byte_array_destroy,
};

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_io_ByteArrayAwsInputStream_awsInputStreamByteArrayNew(
    JNIEnv *env,
    jclass jni_class,
    jobject java_stream,
    jbyteArray stream_data) {

    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_input_stream *input_stream = NULL;
    struct aws_input_stream_byte_array_impl *impl = NULL;

    aws_mem_acquire_many(
        allocator,
        2,
        &input_stream,
        sizeof(struct aws_input_stream),
        &impl,
        sizeof(struct aws_input_stream_byte_array_impl));

    if (!input_stream) {
        return (jlong)NULL;
    }

    AWS_ZERO_STRUCT(*input_stream);
    AWS_ZERO_STRUCT(*impl);

    input_stream->allocator = allocator;
    input_stream->vtable = &s_aws_input_stream_byte_array_vtable;
    input_stream->impl = impl;

    struct aws_byte_cursor bytes_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, stream_data);
    aws_byte_buf_init(&impl->bytes, allocator, bytes_cursor.len);
    aws_byte_buf_append(&impl->bytes, &bytes_cursor);
    aws_jni_byte_cursor_from_jbyteArray_release(env, stream_data, bytes_cursor);

    struct aws_byte_cursor buf_cursor = aws_byte_cursor_from_buf(&impl->bytes);
    impl->cursor_stream = aws_input_stream_new_from_cursor(allocator, &buf_cursor);

    return (jlong)input_stream;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_AwsInputStream_awsInputStreamDestroy(
    JNIEnv *env,
    jclass jni_cp,
    jlong stream_addr) {
    (void)jni_cp;
    struct aws_input_stream *stream = (struct aws_input_stream *)stream_addr;
    if (!stream) {
        aws_jni_throw_runtime_exception(
            env, "AwsInputStream.awsInputStreamDestroy: instance should be non-null at destruction time");
        return;
    }

    aws_input_stream_destroy(stream);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

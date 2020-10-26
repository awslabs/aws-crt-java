/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/event-stream/event_stream.h>

#include "crt.h"
#include "event_stream_message.h"
#include "java_class_ids.h"

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_eventstream_Message_messageNew(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray headers,
    jbyteArray payload) {
    (void)jni_class;

    struct aws_event_stream_message *message =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct aws_event_stream_message));

    if (!message) {
        aws_jni_throw_runtime_exception(env, "Message.MessageNew: Allocation failed!");
        return (jlong)NULL;
    }

    struct aws_event_stream_message *return_message = NULL;

    struct aws_event_stream_rpc_marshalled_message marshalled_message;
    if (aws_event_stream_rpc_marshall_message_args_init(
            &marshalled_message, aws_jni_get_allocator(), env, headers, payload, NULL, 0, 0)) {
        goto clean_up;
    }

    if (aws_event_stream_message_init(
            message, aws_jni_get_allocator(), &marshalled_message.headers_list, &marshalled_message.payload_buf)) {
        goto clean_up;
    }

    return_message = message;

clean_up:
    aws_event_stream_rpc_marshall_message_args_clean_up(&marshalled_message);

    if (!return_message) {
        aws_mem_release(aws_jni_get_allocator(), message);
    }

    return (jlong)return_message;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_eventstream_Message_messageDelete(
    JNIEnv *env,
    jclass jni_class,
    jlong message_ptr) {
    (void)env;
    (void)jni_class;
    struct aws_event_stream_message *message = (struct aws_event_stream_message *)message_ptr;
    aws_event_stream_message_clean_up(message);
    aws_mem_release(aws_jni_get_allocator(), message);
}

JNIEXPORT
jobject JNICALL Java_software_amazon_awssdk_crt_eventstream_Message_messageBuffer(
    JNIEnv *env,
    jclass jni_class,
    jlong message_ptr) {
    (void)jni_class;

    struct aws_event_stream_message *message = (struct aws_event_stream_message *)message_ptr;
    const uint8_t *buffer = aws_event_stream_message_buffer(message);
    size_t buffer_len = aws_event_stream_message_total_length(message);

    return aws_jni_direct_byte_buffer_from_raw_ptr(env, buffer, (jlong)buffer_len);
}

int aws_event_stream_rpc_marshall_message_args_init(
    struct aws_event_stream_rpc_marshalled_message *message_args,
    struct aws_allocator *allocator,
    JNIEnv *env,
    jbyteArray headers,
    jbyteArray payload,
    jbyteArray operation_name,
    jint message_flags,
    jint message_type) {
    AWS_ZERO_STRUCT(*message_args);
    message_args->allocator = allocator;

    if (headers) {
        if (aws_event_stream_headers_list_init(&message_args->headers_list, allocator)) {
            aws_jni_throw_runtime_exception(env, "EventStreamRPCMessage: headers allocation failed.");
            return AWS_OP_ERR;
        }

        message_args->headers_init = true;

        struct aws_byte_cursor headers_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, headers);
        /* copy because JNI is stupid and the buffer that the headers parser runs from needs the memory to stick around
         * until the final message creation happens. */
        aws_byte_buf_init_copy_from_cursor(&message_args->headers_buf, allocator, headers_cur);
        int headers_parse_error = aws_event_stream_read_headers_from_buffer(
            &message_args->headers_list, message_args->headers_buf.buffer, message_args->headers_buf.len);
        aws_jni_byte_cursor_from_jbyteArray_release(env, headers, headers_cur);

        if (headers_parse_error) {
            aws_jni_throw_runtime_exception(env, "EventStreamRPCMessage: headers allocation failed.");
            goto clean_up;
        }
    }

    if (payload) {
        struct aws_byte_cursor payload_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, payload);
        aws_byte_buf_init_copy_from_cursor(&message_args->payload_buf, allocator, payload_cur);
        aws_jni_byte_cursor_from_jbyteArray_release(env, payload, payload_cur);

        if (!message_args->payload_buf.buffer) {
            aws_jni_throw_runtime_exception(env, "EventStreamRPCMessage: allocation failed.");
            goto clean_up;
        }
    }

    message_args->message_args.message_type = message_type;
    message_args->message_args.message_flags = message_flags;
    message_args->message_args.headers = message_args->headers_list.data;
    message_args->message_args.headers_count = message_args->headers_list.length;
    message_args->message_args.payload = &message_args->payload_buf;

    if (operation_name) {
        struct aws_byte_cursor operation_cur = aws_jni_byte_cursor_from_jbyteArray_acquire(env, operation_name);
        aws_byte_buf_init_copy_from_cursor(&message_args->operation_buf, allocator, operation_cur);
        aws_jni_byte_cursor_from_jbyteArray_release(env, operation_name, operation_cur);

        if (!message_args->operation_buf.buffer) {
            aws_jni_throw_runtime_exception(env, "CEventStreamRPCMessage: allocation failed.");
            goto clean_up;
        }
    }

    return AWS_OP_SUCCESS;

clean_up:
    aws_byte_buf_clean_up(&message_args->headers_buf);
    aws_byte_buf_clean_up(&message_args->payload_buf);
    aws_byte_buf_clean_up(&message_args->operation_buf);

    if (message_args->headers_init) {
        aws_event_stream_headers_list_cleanup(&message_args->headers_list);
    }

    return AWS_OP_ERR;
}

void aws_event_stream_rpc_marshall_message_args_clean_up(struct aws_event_stream_rpc_marshalled_message *message_args) {
    aws_byte_buf_clean_up(&message_args->headers_buf);
    aws_byte_buf_clean_up(&message_args->payload_buf);
    aws_byte_buf_clean_up(&message_args->operation_buf);

    if (message_args->headers_init) {
        aws_event_stream_headers_list_cleanup(&message_args->headers_list);
        message_args->headers_init = false;
    }
}

jbyteArray aws_event_stream_rpc_marshall_headers_to_byteArray(
    struct aws_allocator *allocator,
    JNIEnv *env,
    struct aws_event_stream_header_value_pair *headers_array,
    size_t length) {
    /* this is not how we recommend you use the array_list api, but it is correct, and it prevents the need for extra
     * allocations and copies. */
    struct aws_array_list headers_list;
    aws_array_list_init_static(&headers_list, headers_array, length, sizeof(struct aws_event_stream_header_value_pair));
    headers_list.length = length;

    uint32_t headers_buf_len = aws_event_stream_compute_headers_required_buffer_len(&headers_list);

    struct aws_byte_buf headers_buf;

    if (aws_byte_buf_init(&headers_buf, allocator, headers_buf_len)) {
        return NULL;
    }

    headers_buf.len = aws_event_stream_write_headers_to_buffer(&headers_list, headers_buf.buffer);

    aws_array_list_clean_up(&headers_list);

    struct aws_byte_cursor headers_cur = aws_byte_cursor_from_buf(&headers_buf);

    jbyteArray headers_byte_array = aws_jni_byte_array_from_cursor(env, &headers_cur);
    aws_byte_buf_clean_up(&headers_buf);

    return headers_byte_array;
}

#ifndef CRT_EVENT_STREAM_MESSAGE_H
#define CRT_EVENT_STREAM_MESSAGE_H
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include <jni.h>

#include <aws/common/byte_buf.h>
#include <aws/event-stream/event_stream_rpc.h>

struct aws_event_stream_rpc_marshalled_message {
    struct aws_allocator *allocator;
    bool headers_init;
    struct aws_array_list headers_list;
    struct aws_byte_buf headers_buf;
    struct aws_byte_buf payload_buf;
    struct aws_byte_buf operation_buf;
    struct aws_event_stream_rpc_message_args message_args;
};

int aws_event_stream_rpc_marshall_message_args_init(
    struct aws_event_stream_rpc_marshalled_message *message_args,
    struct aws_allocator *allocator,
    JNIEnv *env,
    jbyteArray headers,
    jbyteArray payload,
    jbyteArray operation,
    jint message_flags,
    jint message_type);

void aws_event_stream_rpc_marshall_message_args_clean_up(struct aws_event_stream_rpc_marshalled_message *message_args);

jbyteArray aws_event_stream_rpc_marshall_headers_to_byteArray(
    struct aws_allocator *allocator,
    JNIEnv *env,
    struct aws_event_stream_header_value_pair *pair,
    size_t length);

#endif /* CRT_EVENT_STREAM_MESSAGE_H */

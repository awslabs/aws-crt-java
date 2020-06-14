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

#ifndef AWS_JNI_CRT_JAVA_CLASS_IDS_H
#define AWS_JNI_CRT_JAVA_CLASS_IDS_H

#include <jni.h>

/* HttpRequestBodyStream */
struct java_http_request_body_stream_properties {
    jmethodID send_outgoing_body;
    jmethodID reset_position;
};
extern struct java_http_request_body_stream_properties http_request_body_stream_properties;

/* AwsSigningConfig */
struct java_aws_signing_config_properties {
    jclass aws_signing_config_class;
    jfieldID algorithm_field_id;
    jfieldID signature_type_field_id;
    jfieldID region_field_id;
    jfieldID service_field_id;
    jfieldID time_field_id;
    jfieldID credentials_field_id;
    jfieldID credentials_provider_field_id;
    jfieldID should_sign_header_field_id;
    jfieldID use_double_uri_encode_field_id;
    jfieldID should_normalize_uri_path_field_id;
    jfieldID omit_session_token_field_id;
    jfieldID signed_body_value_field_id;
    jfieldID signed_body_header_field_id;
    jfieldID expiration_in_seconds_field_id;
};
extern struct java_aws_signing_config_properties aws_signing_config_properties;

/* Predicate<T> */
struct java_predicate_properties {
    jclass predicate_class;
    jmethodID test_method_id;
};
extern struct java_predicate_properties predicate_properties;

/* HttpRequest */
struct java_http_request_properties {
    jclass http_request_class;
    jmethodID constructor_method_id;
    jfieldID body_stream_field_id;
};
extern struct java_http_request_properties http_request_properties;

/* CrtResource */
struct java_crt_resource_properties {
    jmethodID get_native_handle_method_id;
    jmethodID release_references;
    jmethodID add_ref;
    jmethodID close;
};
extern struct java_crt_resource_properties crt_resource_properties;

/* MqttClientConnection */
struct java_mqtt_connection_properties {
    jmethodID on_connection_complete;
    jmethodID on_connection_interrupted;
    jmethodID on_connection_resumed;
    jmethodID on_websocket_handshake;
};
extern struct java_mqtt_connection_properties mqtt_connection_properties;

/* MqttClientConnection.MessageHandler */
struct java_message_handler_properties {
    jmethodID deliver;
};
extern struct java_message_handler_properties message_handler_properties;

/* MqttException */
struct java_mqtt_exception_properties {
    jclass jni_mqtt_exception;
    jmethodID jni_constructor;
};
extern struct java_mqtt_exception_properties mqtt_exception_properties;

/* ByteBuffer */
struct java_byte_buffer_properties {
    jclass cls;
    jmethodID get_capacity; /* The total number of bytes in the internal byte array. Stays constant. */
    jmethodID get_limit;    /* The max allowed read/write position of the Buffer. limit must be <= capacity. */
    jmethodID set_limit;
    jmethodID get_position; /* The current read/write position of the Buffer. position must be <= limit */
    jmethodID set_position;
    jmethodID get_remaining; /* Remaining number of bytes before the limit is reached. Equal to (limit - position). */
    jmethodID wrap;          /* Creates a new ByteBuffer Object from a Java byte[]. */
};
extern struct java_byte_buffer_properties byte_buffer_properties;

/* CredentialsProvider */
struct java_credentials_provider_properties {
    jmethodID on_shutdown_complete_method_id;
    jmethodID on_get_credentials_complete_method_id;
};
extern struct java_credentials_provider_properties credentials_provider_properties;

/* Credentials */
struct java_credentials_properties {
    jclass credentials_class;
    jmethodID constructor_method_id;
    jfieldID access_key_id_field_id;
    jfieldID secret_access_key_field_id;
    jfieldID session_token_field_id;
};
extern struct java_credentials_properties credentials_properties;

/* AsyncCallback */
struct java_async_callback_properties {
    jmethodID on_success;
    jmethodID on_failure;
};
extern struct java_async_callback_properties async_callback_properties;

/* EventLoopGroup */
struct java_event_loop_group_properties {
    jmethodID onCleanupComplete;
};
extern struct java_event_loop_group_properties event_loop_group_properties;

/* ClientBootstrap */
struct java_client_bootstrap_properties {
    jmethodID onShutdownComplete;
};
extern struct java_client_bootstrap_properties client_bootstrap_properties;

/* HttpClientConnectionManager */
struct java_http_client_connection_manager_properties {
    jmethodID onConnectionAcquired;
    jmethodID onShutdownComplete;
};
extern struct java_http_client_connection_manager_properties http_client_connection_manager_properties;

/* HttpStream */
struct java_http_stream_properties {
    jclass stream_class;
    jmethodID constructor;
    jmethodID close;
};
extern struct java_http_stream_properties http_stream_properties;

/* HttpStreamResponseHandler */
struct java_http_stream_response_handler_native_adapter_properties {
    jmethodID onResponseHeaders;
    jmethodID onResponseHeadersDone;
    jmethodID onResponseBody;
    jmethodID onResponseComplete;
};
extern struct java_http_stream_response_handler_native_adapter_properties http_stream_response_handler_properties;

/* CompletableFuture */
struct java_completable_future_properties {
    jmethodID complete_method_id;
    jmethodID complete_exceptionally_method_id;
};
extern struct java_completable_future_properties completable_future_properties;

/* CrtRuntimeException */
struct java_crt_runtime_exception_properties {
    jclass crt_runtime_exception_class;
    jmethodID constructor_method_id;
};
extern struct java_crt_runtime_exception_properties crt_runtime_exception_properties;

void cache_java_class_ids(JNIEnv *env);

#endif /* AWS_JNI_CRT_JAVA_CLASS_IDS_H */

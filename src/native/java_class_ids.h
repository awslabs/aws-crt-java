/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_JAVA_CLASS_IDS_H
#define AWS_JNI_CRT_JAVA_CLASS_IDS_H

#include <jni.h>

/* HttpRequestBodyStream */
struct java_http_request_body_stream_properties {
    jmethodID send_outgoing_body;
    jmethodID reset_position;
    jmethodID get_length;
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

/* java/lang/Long */
struct java_boxed_long_properties {
    jclass long_class;
    jmethodID constructor;
    jmethodID long_value_method_id;
};
extern struct java_boxed_long_properties boxed_long_properties;

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

/* MqttClientConnection.MqttClientOperationStatistics */
struct java_mqtt_connection_operation_statistics_properties {
    jclass statistics_class;
    jmethodID statistics_constructor_id;
    jfieldID incomplete_operation_count_field_id;
    jfieldID incomplete_operation_size_field_id;
    jfieldID unacked_operation_count_field_id;
    jfieldID unacked_operation_size_field_id;
};
extern struct java_mqtt_connection_operation_statistics_properties mqtt_connection_operation_statistics_properties;

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

/* DelegateCredentialsHandler */
struct java_credentials_handler_properties {
    jmethodID on_handler_get_credentials_method_id;
};
extern struct java_credentials_handler_properties credentials_handler_properties;

/* AsyncCallback */
struct java_async_callback_properties {
    jmethodID on_success_with_object;
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

/* TlsContextPkcs11Options */
struct java_tls_context_pkcs11_options_properties {
    jfieldID pkcs11Lib;
    jfieldID userPin;
    jfieldID slotId;
    jfieldID tokenLabel;
    jfieldID privateKeyObjectLabel;
    jfieldID certificateFilePath;
    jfieldID certificateFileContents;
};
extern struct java_tls_context_pkcs11_options_properties tls_context_pkcs11_options_properties;

/* TlsContextCustomKeyOperationOptions */
struct java_tls_context_custom_key_operation_options_properties {
    jfieldID operation_handler_field_id;
    jfieldID certificate_file_path_field_id;
    jfieldID certificate_file_contents_field_id;
};
extern struct java_tls_context_custom_key_operation_options_properties
    tls_context_custom_key_operation_options_properties;

/* TlsKeyOperationHandler */
struct java_tls_key_operation_handler_properties {
    jmethodID perform_operation_id;
};
extern struct java_tls_key_operation_handler_properties tls_key_operation_handler_properties;

/* TlsKeyOperation */
struct java_tls_key_operation_properties {
    jclass cls;
    jmethodID constructor;
    jmethodID invoke_operation_id;
};
extern struct java_tls_key_operation_properties tls_key_operation_properties;

/* HttpClientConnectionManager */
struct java_http_client_connection_manager_properties {
    jmethodID onShutdownComplete;
};
extern struct java_http_client_connection_manager_properties http_client_connection_manager_properties;

/* Http2StreamManager */
struct java_http2_stream_manager_properties {
    jmethodID onShutdownComplete;
};
extern struct java_http2_stream_manager_properties http2_stream_manager_properties;

/* HttpClientConnection */
struct java_http_client_connection_properties {
    jclass http_client_connection_class;
    jmethodID on_connection_acquired_method_id;
};
extern struct java_http_client_connection_properties http_client_connection_properties;

/* HttpStream */
struct java_http_stream_properties {
    jclass stream_class;
    jmethodID constructor;
    jmethodID close;
};
extern struct java_http_stream_properties http_stream_properties;

/* Http2Stream */
struct java_http2_stream_properties {
    jclass stream_class;
    jmethodID constructor;
};
extern struct java_http2_stream_properties http2_stream_properties;

/* HttpStreamResponseHandler */
struct java_http_stream_response_handler_native_adapter_properties {
    jmethodID onResponseHeaders;
    jmethodID onResponseHeadersDone;
    jmethodID onResponseBody;
    jmethodID onResponseComplete;
};
extern struct java_http_stream_response_handler_native_adapter_properties http_stream_response_handler_properties;

/* HttpStreamWriteChunkCompletionCallback */
struct java_http_stream_write_chunk_completion_properties {
    jmethodID callback;
};
extern struct java_http_stream_write_chunk_completion_properties http_stream_write_chunk_completion_properties;

/* EventStreamServerListener */
struct java_event_stream_server_listener_properties {
    jmethodID onShutdownComplete;
};
extern struct java_event_stream_server_listener_properties event_stream_server_listener_properties;

/* EventStreamServerListenerHandler */
struct java_event_stream_server_listener_handler_properties {
    jmethodID onNewConnection;
    jmethodID onConnectionShutdown;
    jmethodID newConnConstructor;
    jclass connCls;
};
extern struct java_event_stream_server_listener_handler_properties event_stream_server_listener_handler_properties;

struct java_event_stream_server_connection_handler_properties {
    jmethodID onProtocolMessage;
    jmethodID onIncomingStream;
    jmethodID newContinuationConstructor;
    jclass continuationCls;
};
extern struct java_event_stream_server_connection_handler_properties event_stream_server_connection_handler_properties;

struct java_event_stream_server_continuation_handler_properties {
    jmethodID onContinuationMessage;
    jmethodID onContinuationClosed;
};
extern struct java_event_stream_server_continuation_handler_properties
    event_stream_server_continuation_handler_properties;

struct java_event_stream_client_connection_handler_properties {
    jmethodID onSetup;
    jmethodID onProtocolMessage;
    jmethodID onClosed;
};
extern struct java_event_stream_client_connection_handler_properties event_stream_client_connection_handler_properties;

struct java_event_stream_client_continuation_handler_properties {
    jmethodID onContinuationMessage;
    jmethodID onContinuationClosed;
};
extern struct java_event_stream_client_continuation_handler_properties
    event_stream_client_continuation_handler_properties;

struct java_event_stream_message_flush_properties {
    jmethodID callback;
};
extern struct java_event_stream_message_flush_properties event_stream_server_message_flush_properties;

struct java_cpu_info_properties {
    jclass cpu_info_class;
    jmethodID cpu_info_constructor;
};
extern struct java_cpu_info_properties cpu_info_properties;

struct java_s3_client_properties {
    jmethodID onShutdownComplete;
};
extern struct java_s3_client_properties s3_client_properties;

/* S3Client */
struct java_s3_meta_request_properties {
    jmethodID onShutdownComplete;
};
extern struct java_s3_meta_request_properties s3_meta_request_properties;

/* S3MetaRequestResponseHandlerNativeAdapter */
struct java_s3_meta_request_response_handler_native_adapter_properties {
    jmethodID onResponseBody;
    jmethodID onFinished;
    jmethodID onResponseHeaders;
    jmethodID onProgress;
};
extern struct java_s3_meta_request_response_handler_native_adapter_properties
    s3_meta_request_response_handler_native_adapter_properties;

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
    jfieldID error_code_field_id;
};
extern struct java_crt_runtime_exception_properties crt_runtime_exception_properties;

/* EccKeyPair */
struct java_ecc_key_pair_properties {
    jclass ecc_key_pair_class;
    jmethodID constructor;
};
extern struct java_ecc_key_pair_properties ecc_key_pair_properties;

/* CRT */
struct java_crt_properties {
    jclass crt_class;
    jmethodID test_jni_exception_method_id;
};
extern struct java_crt_properties crt_properties;

/* AwsSigningResult */
struct java_aws_signing_result_properties {
    jclass aws_signing_result_class;
    jmethodID constructor;
    jfieldID signed_request_field_id;
    jfieldID signature_field_id;
};
extern struct java_aws_signing_result_properties aws_signing_result_properties;

/* HttpHeader */
struct java_http_header_properties {
    jclass http_header_class;
    jmethodID constructor_method_id; /* (byte[], byte[]) */
};
extern struct java_http_header_properties http_header_properties;

/* HtppConnectionManagerMetrics */
struct java_http_manager_metrics_properties {
    jclass http_manager_metrics_class;
    jmethodID constructor_method_id;
};
extern struct java_http_manager_metrics_properties http_manager_metrics_properties;

/* ExponentialBackoffRetryOptions */
struct java_aws_exponential_backoff_retry_options_properties {
    jclass exponential_backoff_retry_options_class;
    jmethodID exponential_backoff_retry_options_constructor_method_id;
    jfieldID el_group_field_id;
    jfieldID max_retries_field_id;
    jfieldID backoff_scale_factor_ms_field_id;
    jfieldID jitter_mode_field_id;

    jclass jitter_mode_class;
    jfieldID jitter_mode_value_field_id;
};
extern struct java_aws_exponential_backoff_retry_options_properties exponential_backoff_retry_options_properties;

/* StandardRetryOptions */
struct java_aws_standard_retry_options_properties {
    jclass standard_retry_options_class;
    jmethodID standard_retry_options_constructor_method_id;
    jfieldID backoff_retry_options_field_id;
    jfieldID initial_bucket_capacity_field_id;
};
extern struct java_aws_standard_retry_options_properties standard_retry_options_properties;

/* DirectoryTraversalHandler */
struct java_aws_directory_traversal_handler_properties {
    jclass directory_traversal_handler_class;
    jmethodID on_directory_entry_method_id;
};
extern struct java_aws_directory_traversal_handler_properties directory_traversal_handler_properties;

/* DirectoryEntry */
struct java_aws_directory_entry_properties {
    jclass directory_entry_class;
    jmethodID directory_entry_constructor_method_id;
    jfieldID path_field_id;
    jfieldID relative_path_field_id;
    jfieldID is_directory_field_id;
    jfieldID is_symlink_field_id;
    jfieldID is_file_field_id;
    jfieldID file_size_field_id;
};
extern struct java_aws_directory_entry_properties directory_entry_properties;

/* S3MetaRequestProgress */
struct java_aws_s3_meta_request_progress {
    jclass s3_meta_request_progress_class;
    jmethodID s3_meta_request_progress_constructor_method_id;
    jfieldID bytes_transferred_field_id;
    jfieldID content_length_field_id;
};
extern struct java_aws_s3_meta_request_progress s3_meta_request_progress_properties;

/* S3TcpKeepAliveOptions */
struct java_aws_s3_tcp_keep_alive_options_properties {
    jclass s3_tcp_keep_alive_options_class;
    jmethodID s3_tcp_keep_alive_options_constructor_method_id;
    jfieldID keep_alive_interval_sec_field_id;
    jfieldID keep_alive_timeout_sec_field_id;
    jfieldID keep_alive_max_failed_probes_field_id;
};
extern struct java_aws_s3_tcp_keep_alive_options_properties s3_tcp_keep_alive_options_properties;

/* ResumeToken */
struct java_aws_s3_meta_request_resume_token {
    jclass s3_meta_request_resume_token_class;
    jmethodID s3_meta_request_resume_token_constructor_method_id;
    jfieldID native_type_field_id;
    jfieldID part_size_field_id;
    jfieldID total_num_parts_field_id;
    jfieldID num_parts_completed_field_id;
    jfieldID upload_id_field_id;
};
extern struct java_aws_s3_meta_request_resume_token s3_meta_request_resume_token_properties;

/* mqtt5.packets.ConnAckPacket */
struct java_aws_mqtt5_connack_packet_properties {
    jclass connack_packet_class;

    jmethodID connack_constructor_id;
    jmethodID connack_native_add_maximum_qos_id;
    jmethodID connack_native_add_reason_code_id;

    jfieldID connack_session_present_field_id;
    jfieldID connack_reason_code_field_id;
    jfieldID connack_session_expiry_interval_field_id;
    jfieldID connack_receive_maximum_field_id;
    jfieldID connack_maximum_qos_field_id;
    jfieldID connack_retain_available_field_id;
    jfieldID connack_maximum_packet_size_field_id;
    jfieldID connack_assigned_client_identifier_field_id;
    jfieldID connack_reason_string_field_id;
    jfieldID connack_wildcard_subscriptions_available_field_id;
    jfieldID connack_subscription_identifiers_available_field_id;
    jfieldID connack_shared_subscriptions_available_field_id;
    jfieldID connack_server_keep_alive_field_id;
    jfieldID connack_response_information_field_id;
    jfieldID connack_server_reference_field_id;
    jfieldID connack_user_properties_field_id;
};
extern struct java_aws_mqtt5_connack_packet_properties mqtt5_connack_packet_properties;

/* mqtt5.packets.ConnAckPacket.ConnectReasonCode */
struct java_aws_mqtt5_connect_reason_code_properties {
    jclass reason_code_class;
    jmethodID code_get_value_id;
    jmethodID code_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_connect_reason_code_properties mqtt5_connect_reason_code_properties;

/* mqtt5.packets.PacketConnnect */
struct java_aws_mqtt5_connect_packet_properties {
    jclass connect_packet_class;

    jfieldID connect_keep_alive_interval_seconds_field_id;
    jfieldID connect_client_id_field_id;
    jfieldID connect_username_field_id;
    jfieldID connect_password_field_id;
    jfieldID connect_session_expiry_interval_seconds_field_id;
    jfieldID connect_request_response_information_field_id;
    jfieldID connect_request_problem_information_field_id;
    jfieldID connect_receive_maximum_field_id;
    jfieldID connect_maximum_packet_size_bytes_field_id;
    jfieldID connect_will_delay_interval_seconds_field_id;
    jfieldID connect_will_field_id;
    jfieldID connect_user_properties_field_id;
};
extern struct java_aws_mqtt5_connect_packet_properties mqtt5_connect_packet_properties;

/* mqtt5.packets.PacketDisconnnect */
struct java_aws_mqtt5_disconnect_packet_properties {
    jclass disconnect_packet_class;

    jmethodID disconnect_constructor_id;
    jmethodID disconnect_native_add_disconnect_reason_code_id;
    jmethodID disconnect_get_reason_code_id;

    jfieldID disconnect_reason_code_field_id;
    jfieldID disconnect_session_expiry_interval_seconds_field_id;
    jfieldID disconnect_reason_string_field_id;
    jfieldID disconnect_session_server_reference_field_id;
    jfieldID disconnect_user_properties_field_id;
};
extern struct java_aws_mqtt5_disconnect_packet_properties mqtt5_disconnect_packet_properties;

/* mqtt5.packets.PacketDisconnnect.DisconnectReasonCode */
struct java_aws_mqtt5_disconnect_reason_code_properties {
    jclass reason_code_class;
    jmethodID code_get_value_id;
    jmethodID code_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_disconnect_reason_code_properties mqtt5_disconnect_reason_code_properties;

/* mqtt5.packets.PubAckPacket */
struct java_aws_mqtt5_puback_packet_properties {
    jclass puback_packet_class;
    jmethodID puback_constructor_id;
    jmethodID puback_native_add_reason_code_id;

    jfieldID puback_reason_code_field_id;
    jfieldID puback_reason_string_field_id;
    jfieldID puback_user_properties_field_id;
};
extern struct java_aws_mqtt5_puback_packet_properties mqtt5_puback_packet_properties;

/* mqtt5.packets.PubAckPacket.PubAckReasonCode */
struct java_aws_mqtt5_puback_reason_code_properties {
    jclass reason_code_class;
    jmethodID code_get_value_id;
    jmethodID code_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_puback_reason_code_properties mqtt5_puback_reason_code_properties;

/* mqtt5.packets.PublishPacket */
struct java_aws_mqtt5_publish_packet_properties {
    jclass publish_packet_class;

    jmethodID publish_constructor_id;
    jmethodID publish_native_set_qos_id;
    jmethodID publish_native_set_payload_format_indicator_id;
    jmethodID publish_get_qos_id;
    jmethodID publish_get_payload_format_id;

    jfieldID publish_payload_field_id;
    jfieldID publish_qos_field_id;
    jfieldID publish_retain_field_id;
    jfieldID publish_topic_field_id;
    jfieldID publish_payload_format_field_id;
    jfieldID publish_message_expiry_interval_seconds_field_id;
    jfieldID publish_response_topic_field_id;
    jfieldID publish_correlation_data_field_id;
    jfieldID publish_content_type_field_id;
    jfieldID publish_subscription_identifiers_field_id;
    jfieldID publish_user_properties_field_id;
};
extern struct java_aws_mqtt5_publish_packet_properties mqtt5_publish_packet_properties;

/* mqtt5.packets.PublishPacket.PayloadFormatIndicator */
struct java_aws_mqtt5_payload_format_indicator_properties {
    jclass payload_format_class;
    jmethodID format_get_value_id;
    jmethodID format_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_payload_format_indicator_properties mqtt5_payload_format_indicator_properties;

/* mqtt5.NegotiatedSettings */
struct java_aws_mqtt5_negotiated_settings_properties {
    jclass negotiated_settings_class;

    jmethodID negotiated_settings_constructor_id;
    jmethodID negotiated_settings_native_set_qos_id;

    jfieldID negotiated_settings_maximum_qos_field_id;
    jfieldID negotiated_settings_session_expiry_interval_field_id;
    jfieldID negotiated_settings_receive_maximum_from_server_field_id;
    jfieldID negotiated_settings_maximum_packet_size_to_server_field_id;
    jfieldID negotiated_settings_server_keep_alive_field_id;
    jfieldID negotiated_settings_retain_available_field_id;
    jfieldID negotiated_settings_wildcard_subscriptions_available_field_id;
    jfieldID negotiated_settings_subscription_identifiers_available_field_id;
    jfieldID negotiated_settings_shared_subscriptions_available_field_id;
    jfieldID negotiated_settings_rejoined_session_field_id;
    jfieldID negotiated_settings_assigned_client_id_field_id;
};
extern struct java_aws_mqtt5_negotiated_settings_properties mqtt5_negotiated_settings_properties;

/* http.HttpProxyOptions */
struct java_aws_http_proxy_options_properties {
    jclass http_proxy_options_class;

    jmethodID proxy_get_connection_type_id;
    jmethodID proxy_get_proxy_host_id;
    jmethodID proxy_get_proxy_port_id;
    jmethodID proxy_get_proxy_tls_context_id;
    jmethodID proxy_get_proxy_authorization_type_id;
    jmethodID proxy_get_authorization_username_id;
    jmethodID proxy_get_authorization_password_id;
};
extern struct java_aws_http_proxy_options_properties http_proxy_options_properties;

/* http.HttpProxyOptions.HttpProxyConnectionType */
struct java_aws_http_proxy_connection_type_properties {
    jclass http_proxy_connection_type_class;
    jmethodID proxy_get_value_id;
};
extern struct java_aws_http_proxy_connection_type_properties http_proxy_connection_type_properties;

/* mqtt5.ClientOptions */
struct java_aws_mqtt5_client_options_properties {
    jclass client_options_class;

    // Functions for CRT resource references so we can
    // better control them when they are not present
    jmethodID options_get_bootstrap_id;
    jmethodID options_get_socket_options_id;
    jmethodID options_get_tls_options_id;
    jmethodID options_get_session_behavior_id;
    jmethodID options_get_extended_validation_and_flow_control_options_id;
    jmethodID options_get_offline_queue_behavior_id;
    jmethodID options_get_retry_jitter_mode_id;

    jfieldID options_host_name_field_id;
    jfieldID options_port_field_id;
    jfieldID http_proxy_options_field_id;
    // We skip connect options since that is passed in directly
    // since it can be made outside of the builder
    jfieldID session_behavior_field_id;
    jfieldID extended_validation_and_flow_control_options_field_id;
    jfieldID offline_queue_behavior_field_id;
    jfieldID retry_jitter_mode_field_id;
    jfieldID min_reconnect_delay_ms_field_id;
    jfieldID max_reconnect_delay_ms_field_id;
    jfieldID min_connected_time_to_reset_reconnect_delay_ms_field_id;
    jfieldID ping_timeout_ms_field_id;
    jfieldID connack_timeout_ms_field_id;
    jfieldID ack_timeout_seconds_field_id;
    jfieldID publish_events_field_id;
    jfieldID lifecycle_events_field_id;
};
extern struct java_aws_mqtt5_client_options_properties mqtt5_client_options_properties;

/* mqtt5.Client */
struct java_aws_mqtt5_client_properties {
    jclass client_class;
    jmethodID client_on_websocket_handshake_id;
    jmethodID client_set_is_connected;
    jfieldID websocket_handshake_field_id;
};
extern struct java_aws_mqtt5_client_properties mqtt5_client_properties;

/* mqtt5.Mqtt5ClientOperationStatistics */
struct java_aws_mqtt5_client_operation_statistics_properties {
    jclass statistics_class;
    jmethodID statistics_constructor_id;
    jfieldID incomplete_operation_count_field_id;
    jfieldID incomplete_operation_size_field_id;
    jfieldID unacked_operation_count_field_id;
    jfieldID unacked_operation_size_field_id;
};
extern struct java_aws_mqtt5_client_operation_statistics_properties mqtt5_client_operation_statistics_properties;

/* mqtt5.ClientOptions.ClientSessionBehavior */
struct java_aws_mqtt5_client_session_behavior_type_properties {
    jclass mqtt5_client_session_behavior_class;
    jmethodID client_get_value_id;
};
extern struct java_aws_mqtt5_client_session_behavior_type_properties mqtt5_client_session_behavior_properties;

/* mqtt5.ClientOptions.ExtendedValidationAndFlowControlOptions */
struct java_aws_mqtt5_client_extended_validation_and_flow_control_options {
    jclass mqtt5_client_extended_validation_and_flow_control_options_class;
    jmethodID client_get_value_id;
};
extern struct java_aws_mqtt5_client_extended_validation_and_flow_control_options
    mqtt5_client_extended_validation_and_flow_control_options;

/* mqtt5.ClientOptions.ClientOfflineQueueBehavior */
struct java_aws_mqtt5_client_offline_queue_behavior_type_properties {
    jclass mqtt5_client_offline_queue_behavior_type_class;
    jmethodID client_get_value_id;
};
extern struct java_aws_mqtt5_client_offline_queue_behavior_type_properties
    mqtt5_client_offline_queue_behavior_type_properties;

/* mqtt5.ClientOptions.JitterMode */
struct java_aws_mqtt5_client_jitter_mode_properties {
    jclass mqtt5_client_jitter_mode_class;
    jmethodID client_get_value_id;
};
extern struct java_aws_mqtt5_client_jitter_mode_properties mqtt5_client_jitter_mode_properties;

/* mqtt5.packets.SubscribePacket */
struct java_aws_mqtt5_subscribe_packet_properties {
    jclass subscribe_packet_class;
    jfieldID subscribe_subscriptions_field_id;
    jfieldID subscribe_subscription_identifier_field_id;
    jfieldID subscribe_user_properties_field_id;
};
extern struct java_aws_mqtt5_subscribe_packet_properties mqtt5_subscribe_packet_properties;

/* mqtt5.packets.SubscribePacket.Subscription */
struct java_aws_mqtt5_subscription_properties {
    jclass subscribe_subscription_class;
    jfieldID subscribe_no_local_field_id;
    jfieldID subscribe_retain_as_published_field_id;

    jmethodID subscribe_get_topic_filter_id;
    jmethodID subscribe_get_qos_id;
    jmethodID subscribe_get_no_local_id;
    jmethodID subscribe_get_retain_as_published_id;
    jmethodID subscribe_get_retain_handling_type_id;
};
extern struct java_aws_mqtt5_subscription_properties mqtt5_subscription_properties;

/* mqtt5.QOS */
struct java_aws_mqtt5_packet_qos_properties {
    jclass packet_qos_class;
    jmethodID qos_get_value_id;
    jmethodID qos_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_packet_qos_properties mqtt5_packet_qos_properties;

/* mqtt5.packets.SubscribePacket.RetainHandlingType */
struct java_aws_mqtt5_retain_handling_type_properties {
    jclass retain_handling_type_class;
    jmethodID retain_get_value_id;
};
extern struct java_aws_mqtt5_retain_handling_type_properties mqtt5_retain_handling_type_properties;

/* mqtt5.packets.SubAckPacket.SubAckReasonCode */
struct java_aws_mqtt5_suback_reason_code_properties {
    jclass reason_code_class;
    jmethodID reason_get_value_id;
    jmethodID reason_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_suback_reason_code_properties mqtt5_suback_reason_code_properties;

/* mqtt5.packets.SubAckPacket */
struct java_aws_mqtt5_packet_suback_properties {
    jclass suback_packet_class;
    jmethodID suback_constructor_id;
    jmethodID suback_native_add_suback_code_id;

    jfieldID suback_reason_string_field_id;
    jfieldID suback_reason_codes_field_id;
    jfieldID suback_user_properties_field_id;
};
extern struct java_aws_mqtt5_packet_suback_properties mqtt5_suback_packet_properties;

/* mqtt5.packets.UnsubscribePacket */
struct java_aws_mqtt5_packet_unsubscribe_properties {
    jclass unsubscribe_packet_class;
    jfieldID unsubscribe_subscriptions_field_id;
    jfieldID unsubscribe_user_properties_field_id;
};
extern struct java_aws_mqtt5_packet_unsubscribe_properties mqtt5_unsubscribe_packet_properties;

/* mqtt5.packets.UnsubAckPacket */
struct java_aws_mqtt5_packet_unsuback_properties {
    jclass unsuback_packet_class;
    jmethodID unsuback_constructor_id;
    jmethodID unsuback_native_add_unsuback_code_id;

    jfieldID unsuback_reason_string_field_id;
    jfieldID unsuback_reason_codes_field_id;
    jfieldID unsuback_user_properties_field_id;
};
extern struct java_aws_mqtt5_packet_unsuback_properties mqtt5_unsuback_packet_properties;

/* mqtt5.packets.UnsubAckPacket.UnsubAckReasonCode */
struct java_aws_mqtt5_unsuback_reason_code_properties {
    jclass reason_code_class;
    jmethodID reason_get_value_id;
    jmethodID reason_s_get_enum_value_from_integer_id;
};
extern struct java_aws_mqtt5_unsuback_reason_code_properties mqtt5_unsuback_reason_code_properties;

/* mqtt5.packets.UserProperty */
struct java_aws_mqtt5_user_property_properties {
    jclass user_property_class;
    jmethodID property_constructor_id;
    jfieldID property_key_id;
    jfieldID property_value_id;
};
extern struct java_aws_mqtt5_user_property_properties mqtt5_user_property_properties;

/* mqtt5.Mqtt5ClientOptions.PublishEvents */
struct java_aws_mqtt5_publish_events {
    jclass publish_events_class;
    jmethodID publish_events_publish_received_id;
};
extern struct java_aws_mqtt5_publish_events mqtt5_publish_events_properties;

/* mqtt5.Mqtt5ListenerOptions.PublishEvents */
struct java_aws_mqtt5_listener_publish_events {
    jclass listener_publish_events_class;
    jmethodID listener_publish_events_publish_received_id;
};
extern struct java_aws_mqtt5_listener_publish_events mqtt5_listener_publish_events_properties;

/* mqtt5.Mqtt5ClientOptions.LifecycleEvents */
struct java_aws_mqtt5_lifecycle_events {
    jclass lifecycle_events_class;
    jmethodID lifecycle_attempting_connect_id;
    jmethodID lifecycle_connection_success_id;
    jmethodID lifecycle_connection_failure_id;
    jmethodID lifecycle_disconnection_id;
    jmethodID lifecycle_stopped_id;
};
extern struct java_aws_mqtt5_lifecycle_events mqtt5_lifecycle_events_properties;

/* mqtt5.PublishResult */
struct java_aws_mqtt5_publish_result_properties {
    jclass result_class;
    jmethodID result_constructor_id;
    jmethodID result_puback_constructor_id;
};
extern struct java_aws_mqtt5_publish_result_properties mqtt5_publish_result_properties;

/* mqtt5.PublishReturn */
struct java_aws_mqtt5_publish_return_properties {
    jclass return_class;
    jmethodID return_constructor_id;
};
extern struct java_aws_mqtt5_publish_return_properties mqtt5_publish_return_properties;

/* mqtt5.OnStoppedReturn */
struct java_aws_mqtt5_on_stopped_return_properties {
    jclass return_class;
    jmethodID return_constructor_id;
};
extern struct java_aws_mqtt5_on_stopped_return_properties mqtt5_on_stopped_return_properties;

/* mqtt5.OnAttemptingConnectReturn */
struct java_aws_mqtt5_on_attempting_connect_return_properties {
    jclass return_class;
    jmethodID return_constructor_id;
};
extern struct java_aws_mqtt5_on_attempting_connect_return_properties mqtt5_on_attempting_connect_return_properties;

/* mqtt5.OnConnectionSuccessReturn */
struct java_aws_mqtt5_on_connection_success_return_properties {
    jclass return_class;
    jmethodID return_constructor_id;
};
extern struct java_aws_mqtt5_on_connection_success_return_properties mqtt5_on_connection_success_return_properties;

/* mqtt5.OnConnectionFailureReturn */
struct java_aws_mqtt5_on_connection_failure_return_properties {
    jclass return_class;
    jmethodID return_constructor_id;
};
extern struct java_aws_mqtt5_on_connection_failure_return_properties mqtt5_on_connection_failure_return_properties;

/* mqtt5.OnDisconnectionReturn */
struct java_aws_mqtt5_on_disconnection_return_properties {
    jclass return_class;
    jmethodID return_constructor_id;
};
extern struct java_aws_mqtt5_on_disconnection_return_properties mqtt5_on_disconnection_return_properties;

/* mqtt5.ListenerOptions */
struct java_aws_mqtt5_listener_options_properties {
    jclass listener_options_class;

    jfieldID listener_publish_events_field_id;
    jfieldID lifecycle_events_field_id;
};
extern struct java_aws_mqtt5_listener_options_properties mqtt5_listener_options_properties;

/* java/lang/Integer */
struct java_boxed_integer_properties {
    jclass integer_class;
    jmethodID integer_constructor_id;
    jmethodID integer_get_value_id;
};
extern struct java_boxed_integer_properties boxed_integer_properties;

/* java/lang/Boolean */
struct java_boxed_boolean_properties {
    jclass boolean_class;
    jmethodID boolean_constructor_id;
    jmethodID boolean_get_value_id;
};
extern struct java_boxed_boolean_properties boxed_boolean_properties;

/* java/util/List */
struct java_boxed_list_properties {
    jclass list_class;
    jmethodID list_size_id;
    jmethodID list_get_id;
    jmethodID list_add_id;
};
extern struct java_boxed_list_properties boxed_list_properties;

/* java/util/ArrayList */
struct java_boxed_array_list_properties {
    jclass list_class;
    jmethodID list_constructor_id;
};
extern struct java_boxed_array_list_properties boxed_array_list_properties;

void cache_java_class_ids(JNIEnv *env);

#endif /* AWS_JNI_CRT_JAVA_CLASS_IDS_H */

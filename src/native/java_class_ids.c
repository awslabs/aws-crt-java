/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "java_class_ids.h"

#include <aws/common/assert.h>

struct java_http_request_body_stream_properties http_request_body_stream_properties;

static void s_cache_http_request_body_stream(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpRequestBodyStream");
    AWS_FATAL_ASSERT(cls);

    http_request_body_stream_properties.send_outgoing_body =
        (*env)->GetMethodID(env, cls, "sendRequestBody", "(Ljava/nio/ByteBuffer;)Z");
    AWS_FATAL_ASSERT(http_request_body_stream_properties.send_outgoing_body);

    http_request_body_stream_properties.reset_position = (*env)->GetMethodID(env, cls, "resetPosition", "()Z");
    AWS_FATAL_ASSERT(http_request_body_stream_properties.reset_position);

    http_request_body_stream_properties.get_length = (*env)->GetMethodID(env, cls, "getLength", "()J");
    AWS_FATAL_ASSERT(http_request_body_stream_properties.get_length);
}

struct java_aws_signing_config_properties aws_signing_config_properties;

static void s_cache_aws_signing_config(JNIEnv *env) {
    jclass aws_signing_config_class =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/signing/AwsSigningConfig");
    AWS_FATAL_ASSERT(aws_signing_config_class);
    aws_signing_config_properties.aws_signing_config_class = (*env)->NewGlobalRef(env, aws_signing_config_class);

    aws_signing_config_properties.algorithm_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "algorithm", "I");
    AWS_FATAL_ASSERT(aws_signing_config_properties.algorithm_field_id);

    aws_signing_config_properties.signature_type_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "signatureType", "I");
    AWS_FATAL_ASSERT(aws_signing_config_properties.signature_type_field_id);

    aws_signing_config_properties.region_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "region", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(aws_signing_config_properties.region_field_id);

    aws_signing_config_properties.service_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "service", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(aws_signing_config_properties.service_field_id);

    aws_signing_config_properties.time_field_id = (*env)->GetFieldID(env, aws_signing_config_class, "time", "J");
    AWS_FATAL_ASSERT(aws_signing_config_properties.time_field_id);

    aws_signing_config_properties.credentials_provider_field_id = (*env)->GetFieldID(
        env,
        aws_signing_config_class,
        "credentialsProvider",
        "Lsoftware/amazon/awssdk/crt/auth/credentials/CredentialsProvider;");
    AWS_FATAL_ASSERT(aws_signing_config_properties.credentials_provider_field_id);

    aws_signing_config_properties.credentials_field_id = (*env)->GetFieldID(
        env, aws_signing_config_class, "credentials", "Lsoftware/amazon/awssdk/crt/auth/credentials/Credentials;");
    AWS_FATAL_ASSERT(aws_signing_config_properties.credentials_field_id);

    aws_signing_config_properties.should_sign_header_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "shouldSignHeader", "Ljava/util/function/Predicate;");
    AWS_FATAL_ASSERT(aws_signing_config_properties.should_sign_header_field_id);

    aws_signing_config_properties.use_double_uri_encode_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "useDoubleUriEncode", "Z");
    AWS_FATAL_ASSERT(aws_signing_config_properties.use_double_uri_encode_field_id);

    aws_signing_config_properties.should_normalize_uri_path_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "shouldNormalizeUriPath", "Z");
    AWS_FATAL_ASSERT(aws_signing_config_properties.should_normalize_uri_path_field_id);

    aws_signing_config_properties.omit_session_token_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "omitSessionToken", "Z");
    AWS_FATAL_ASSERT(aws_signing_config_properties.omit_session_token_field_id);

    aws_signing_config_properties.signed_body_value_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "signedBodyValue", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(aws_signing_config_properties.signed_body_value_field_id);

    aws_signing_config_properties.signed_body_header_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "signedBodyHeader", "I");
    AWS_FATAL_ASSERT(aws_signing_config_properties.signed_body_header_field_id);

    aws_signing_config_properties.expiration_in_seconds_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "expirationInSeconds", "J");
    AWS_FATAL_ASSERT(aws_signing_config_properties.expiration_in_seconds_field_id);
}

struct java_predicate_properties predicate_properties;

static void s_cache_predicate(JNIEnv *env) {
    jclass predicate_class = (*env)->FindClass(env, "java/util/function/Predicate");
    AWS_FATAL_ASSERT(predicate_class);
    predicate_properties.predicate_class = (*env)->NewGlobalRef(env, predicate_class);

    predicate_properties.test_method_id = (*env)->GetMethodID(env, predicate_class, "test", "(Ljava/lang/Object;)Z");
    AWS_FATAL_ASSERT(predicate_properties.test_method_id);
}

struct java_boxed_long_properties boxed_long_properties;

static void s_cache_boxed_long(JNIEnv *env) {
    jclass boxed_long_class = (*env)->FindClass(env, "java/lang/Long");
    AWS_FATAL_ASSERT(boxed_long_class);
    boxed_long_properties.long_class = (*env)->NewGlobalRef(env, boxed_long_class);

    boxed_long_properties.constructor = (*env)->GetMethodID(env, boxed_long_class, "<init>", "(J)V");
    AWS_FATAL_ASSERT(boxed_long_properties.constructor);

    boxed_long_properties.long_value_method_id = (*env)->GetMethodID(env, boxed_long_class, "longValue", "()J");
    AWS_FATAL_ASSERT(boxed_long_properties.long_value_method_id);
}

struct java_http_request_properties http_request_properties;

static void s_cache_http_request(JNIEnv *env) {
    jclass http_request_class = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpRequest");
    AWS_FATAL_ASSERT(http_request_class);
    http_request_properties.http_request_class = (*env)->NewGlobalRef(env, http_request_class);

    http_request_properties.constructor_method_id = (*env)->GetMethodID(
        env,
        http_request_class,
        "<init>",
        "(Ljava/nio/ByteBuffer;Lsoftware/amazon/awssdk/crt/http/HttpRequestBodyStream;)V");
    AWS_FATAL_ASSERT(http_request_properties.constructor_method_id);

    http_request_properties.body_stream_field_id = (*env)->GetFieldID(
        env, http_request_class, "bodyStream", "Lsoftware/amazon/awssdk/crt/http/HttpRequestBodyStream;");
    AWS_FATAL_ASSERT(http_request_properties.body_stream_field_id);
}

struct java_crt_resource_properties crt_resource_properties;

static void s_cache_crt_resource(JNIEnv *env) {
    jclass crt_resource_class = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtResource");
    AWS_FATAL_ASSERT(crt_resource_class);

    crt_resource_properties.get_native_handle_method_id =
        (*env)->GetMethodID(env, crt_resource_class, "getNativeHandle", "()J");
    AWS_FATAL_ASSERT(crt_resource_properties.get_native_handle_method_id);

    crt_resource_properties.release_references =
        (*env)->GetMethodID(env, crt_resource_class, "releaseReferences", "()V");
    AWS_FATAL_ASSERT(crt_resource_properties.release_references);

    crt_resource_properties.add_ref = (*env)->GetMethodID(env, crt_resource_class, "addRef", "()V");
    AWS_FATAL_ASSERT(crt_resource_properties.add_ref);

    crt_resource_properties.close = (*env)->GetMethodID(env, crt_resource_class, "close", "()V");
    AWS_FATAL_ASSERT(crt_resource_properties.close);
}

struct java_mqtt_connection_properties mqtt_connection_properties;

static void s_cache_mqtt_connection(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttClientConnection");
    AWS_FATAL_ASSERT(cls);

    mqtt_connection_properties.on_connection_complete = (*env)->GetMethodID(env, cls, "onConnectionComplete", "(IZ)V");
    AWS_FATAL_ASSERT(mqtt_connection_properties.on_connection_complete);

    mqtt_connection_properties.on_connection_interrupted =
        (*env)->GetMethodID(env, cls, "onConnectionInterrupted", "(ILsoftware/amazon/awssdk/crt/AsyncCallback;)V");
    AWS_FATAL_ASSERT(mqtt_connection_properties.on_connection_interrupted);

    mqtt_connection_properties.on_connection_resumed = (*env)->GetMethodID(env, cls, "onConnectionResumed", "(Z)V");
    AWS_FATAL_ASSERT(mqtt_connection_properties.on_connection_resumed);

    mqtt_connection_properties.on_websocket_handshake =
        (*env)->GetMethodID(env, cls, "onWebsocketHandshake", "(Lsoftware/amazon/awssdk/crt/http/HttpRequest;J)V");
    AWS_FATAL_ASSERT(mqtt_connection_properties.on_websocket_handshake);
}

struct java_message_handler_properties message_handler_properties;

static void s_cache_message_handler(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttClientConnection$MessageHandler");
    AWS_FATAL_ASSERT(cls);

    message_handler_properties.deliver = (*env)->GetMethodID(env, cls, "deliver", "(Ljava/lang/String;[BZIZ)V");
    AWS_FATAL_ASSERT(message_handler_properties.deliver);
}

struct java_mqtt_exception_properties mqtt_exception_properties;

static void s_cache_mqtt_exception(JNIEnv *env) {
    mqtt_exception_properties.jni_mqtt_exception =
        (*env)->NewGlobalRef(env, (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttException"));
    AWS_FATAL_ASSERT(mqtt_exception_properties.jni_mqtt_exception);

    mqtt_exception_properties.jni_constructor =
        (*env)->GetMethodID(env, mqtt_exception_properties.jni_mqtt_exception, "<init>", "(I)V");
    AWS_FATAL_ASSERT(mqtt_exception_properties.jni_constructor);
}

struct java_mqtt_connection_operation_statistics_properties mqtt_connection_operation_statistics_properties;

static void s_cache_mqtt_client_connection_operation_statistics(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt/MqttClientConnectionOperationStatistics");
    AWS_FATAL_ASSERT(cls);
    mqtt_connection_operation_statistics_properties.statistics_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt_connection_operation_statistics_properties.statistics_class);
    // Functions
    mqtt_connection_operation_statistics_properties.statistics_constructor_id =
        (*env)->GetMethodID(env, mqtt_connection_operation_statistics_properties.statistics_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt_connection_operation_statistics_properties.statistics_constructor_id);
    // Field IDs
    mqtt_connection_operation_statistics_properties.incomplete_operation_count_field_id = (*env)->GetFieldID(
        env, mqtt_connection_operation_statistics_properties.statistics_class, "incompleteOperationCount", "J");
    AWS_FATAL_ASSERT(mqtt_connection_operation_statistics_properties.incomplete_operation_count_field_id);
    mqtt_connection_operation_statistics_properties.incomplete_operation_size_field_id = (*env)->GetFieldID(
        env, mqtt_connection_operation_statistics_properties.statistics_class, "incompleteOperationSize", "J");
    AWS_FATAL_ASSERT(mqtt_connection_operation_statistics_properties.incomplete_operation_size_field_id);
    mqtt_connection_operation_statistics_properties.unacked_operation_count_field_id = (*env)->GetFieldID(
        env, mqtt_connection_operation_statistics_properties.statistics_class, "unackedOperationCount", "J");
    AWS_FATAL_ASSERT(mqtt_connection_operation_statistics_properties.unacked_operation_count_field_id);
    mqtt_connection_operation_statistics_properties.unacked_operation_size_field_id = (*env)->GetFieldID(
        env, mqtt_connection_operation_statistics_properties.statistics_class, "unackedOperationSize", "J");
    AWS_FATAL_ASSERT(mqtt_connection_operation_statistics_properties.unacked_operation_size_field_id);
}

struct java_byte_buffer_properties byte_buffer_properties;

static void s_cache_byte_buffer(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/nio/ByteBuffer");
    AWS_FATAL_ASSERT(cls);

    // FindClass() returns local JNI references that become eligible for GC once this native method returns to Java.
    // Call NewGlobalRef() so that this class reference doesn't get Garbage collected.
    byte_buffer_properties.cls = (*env)->NewGlobalRef(env, cls);

    byte_buffer_properties.get_capacity = (*env)->GetMethodID(env, cls, "capacity", "()I");
    AWS_FATAL_ASSERT(byte_buffer_properties.get_capacity);

    byte_buffer_properties.get_limit = (*env)->GetMethodID(env, cls, "limit", "()I");
    AWS_FATAL_ASSERT(byte_buffer_properties.get_limit);

    byte_buffer_properties.set_limit = (*env)->GetMethodID(env, cls, "limit", "(I)Ljava/nio/Buffer;");
    AWS_FATAL_ASSERT(byte_buffer_properties.set_limit);

    byte_buffer_properties.get_position = (*env)->GetMethodID(env, cls, "position", "()I");
    AWS_FATAL_ASSERT(byte_buffer_properties.get_position);

    byte_buffer_properties.set_position = (*env)->GetMethodID(env, cls, "position", "(I)Ljava/nio/Buffer;");
    AWS_FATAL_ASSERT(byte_buffer_properties.set_position);

    byte_buffer_properties.get_remaining = (*env)->GetMethodID(env, cls, "remaining", "()I");
    AWS_FATAL_ASSERT(byte_buffer_properties.get_remaining);

    byte_buffer_properties.wrap = (*env)->GetStaticMethodID(env, cls, "wrap", "([B)Ljava/nio/ByteBuffer;");
    AWS_FATAL_ASSERT(byte_buffer_properties.wrap);
}

struct java_credentials_provider_properties credentials_provider_properties;

static void s_cache_credentials_provider(JNIEnv *env) {
    jclass provider_class = (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/credentials/CredentialsProvider");
    AWS_FATAL_ASSERT(provider_class);

    credentials_provider_properties.on_shutdown_complete_method_id =
        (*env)->GetMethodID(env, provider_class, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(credentials_provider_properties.on_shutdown_complete_method_id);

    credentials_provider_properties.on_get_credentials_complete_method_id = (*env)->GetMethodID(
        env,
        provider_class,
        "onGetCredentialsComplete",
        "(Ljava/util/concurrent/CompletableFuture;Lsoftware/amazon/awssdk/crt/auth/credentials/Credentials;)V");
    AWS_FATAL_ASSERT(credentials_provider_properties.on_get_credentials_complete_method_id);
}

struct java_credentials_properties credentials_properties;

static void s_cache_credentials(JNIEnv *env) {
    credentials_properties.credentials_class =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/credentials/Credentials");
    AWS_FATAL_ASSERT(credentials_properties.credentials_class);
    credentials_properties.credentials_class = (*env)->NewGlobalRef(env, credentials_properties.credentials_class);

    credentials_properties.constructor_method_id =
        (*env)->GetMethodID(env, credentials_properties.credentials_class, "<init>", "()V");
    AWS_FATAL_ASSERT(credentials_properties.constructor_method_id);

    credentials_properties.access_key_id_field_id =
        (*env)->GetFieldID(env, credentials_properties.credentials_class, "accessKeyId", "[B");
    AWS_FATAL_ASSERT(credentials_properties.access_key_id_field_id);

    credentials_properties.secret_access_key_field_id =
        (*env)->GetFieldID(env, credentials_properties.credentials_class, "secretAccessKey", "[B");
    AWS_FATAL_ASSERT(credentials_properties.secret_access_key_field_id);

    credentials_properties.session_token_field_id =
        (*env)->GetFieldID(env, credentials_properties.credentials_class, "sessionToken", "[B");
    AWS_FATAL_ASSERT(credentials_properties.session_token_field_id);
}

struct java_credentials_handler_properties credentials_handler_properties;

static void s_cache_credentials_handler(JNIEnv *env) {
    jclass handler_cls =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/credentials/DelegateCredentialsHandler");
    AWS_FATAL_ASSERT(handler_cls);

    credentials_handler_properties.on_handler_get_credentials_method_id = (*env)->GetMethodID(
        env, handler_cls, "getCredentials", "()Lsoftware/amazon/awssdk/crt/auth/credentials/Credentials;");
    AWS_FATAL_ASSERT(credentials_handler_properties.on_handler_get_credentials_method_id);
}

struct java_async_callback_properties async_callback_properties;

static void s_cache_async_callback(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/AsyncCallback");
    AWS_FATAL_ASSERT(cls);

    async_callback_properties.on_success_with_object =
        (*env)->GetMethodID(env, cls, "onSuccess", "(Ljava/lang/Object;)V");
    AWS_FATAL_ASSERT(async_callback_properties.on_success_with_object);

    async_callback_properties.on_success = (*env)->GetMethodID(env, cls, "onSuccess", "()V");
    AWS_FATAL_ASSERT(async_callback_properties.on_success);

    async_callback_properties.on_failure = (*env)->GetMethodID(env, cls, "onFailure", "(Ljava/lang/Throwable;)V");
    AWS_FATAL_ASSERT(async_callback_properties.on_failure);
}

struct java_event_loop_group_properties event_loop_group_properties;

static void s_cache_event_loop_group(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/EventLoopGroup");
    AWS_FATAL_ASSERT(cls);

    event_loop_group_properties.onCleanupComplete = (*env)->GetMethodID(env, cls, "onCleanupComplete", "()V");
    AWS_FATAL_ASSERT(event_loop_group_properties.onCleanupComplete);
}

struct java_client_bootstrap_properties client_bootstrap_properties;

static void s_cache_client_bootstrap(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/ClientBootstrap");
    AWS_FATAL_ASSERT(cls);

    client_bootstrap_properties.onShutdownComplete = (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(client_bootstrap_properties.onShutdownComplete);
}

struct java_tls_context_pkcs11_options_properties tls_context_pkcs11_options_properties;

static void s_cache_tls_context_pkcs11_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/TlsContextPkcs11Options");
    AWS_FATAL_ASSERT(cls);

    tls_context_pkcs11_options_properties.pkcs11Lib =
        (*env)->GetFieldID(env, cls, "pkcs11Lib", "Lsoftware/amazon/awssdk/crt/io/Pkcs11Lib;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.pkcs11Lib);

    tls_context_pkcs11_options_properties.userPin = (*env)->GetFieldID(env, cls, "userPin", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.userPin);

    tls_context_pkcs11_options_properties.slotId = (*env)->GetFieldID(env, cls, "slotId", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.slotId);

    tls_context_pkcs11_options_properties.tokenLabel = (*env)->GetFieldID(env, cls, "tokenLabel", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.tokenLabel);

    tls_context_pkcs11_options_properties.privateKeyObjectLabel =
        (*env)->GetFieldID(env, cls, "privateKeyObjectLabel", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.privateKeyObjectLabel);

    tls_context_pkcs11_options_properties.certificateFilePath =
        (*env)->GetFieldID(env, cls, "certificateFilePath", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.certificateFilePath);

    tls_context_pkcs11_options_properties.certificateFileContents =
        (*env)->GetFieldID(env, cls, "certificateFileContents", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_pkcs11_options_properties.certificateFileContents);
}

struct java_tls_key_operation_properties tls_key_operation_properties;

static void s_cache_tls_key_operation(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/TlsKeyOperation");
    AWS_FATAL_ASSERT(cls);
    tls_key_operation_properties.cls = (*env)->NewGlobalRef(env, cls);

    tls_key_operation_properties.constructor = (*env)->GetMethodID(env, cls, "<init>", "(J[BIII)V");
    AWS_FATAL_ASSERT(tls_key_operation_properties.constructor);

    tls_key_operation_properties.invoke_operation_id = (*env)->GetStaticMethodID(
        env,
        cls,
        "invokePerformOperation",
        "(Lsoftware/amazon/awssdk/crt/io/TlsKeyOperationHandler;Lsoftware/amazon/awssdk/crt/io/TlsKeyOperation;)V");
    AWS_FATAL_ASSERT(tls_key_operation_properties.invoke_operation_id);
}

struct java_tls_context_custom_key_operation_options_properties tls_context_custom_key_operation_options_properties;

static void s_cache_tls_context_custom_key_operation_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/TlsContextCustomKeyOperationOptions");
    AWS_FATAL_ASSERT(cls);

    tls_context_custom_key_operation_options_properties.operation_handler_field_id =
        (*env)->GetFieldID(env, cls, "operationHandler", "Lsoftware/amazon/awssdk/crt/io/TlsKeyOperationHandler;");
    AWS_FATAL_ASSERT(tls_context_custom_key_operation_options_properties.operation_handler_field_id);

    tls_context_custom_key_operation_options_properties.certificate_file_path_field_id =
        (*env)->GetFieldID(env, cls, "certificateFilePath", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_custom_key_operation_options_properties.certificate_file_path_field_id);

    tls_context_custom_key_operation_options_properties.certificate_file_contents_field_id =
        (*env)->GetFieldID(env, cls, "certificateFileContents", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(tls_context_custom_key_operation_options_properties.certificate_file_contents_field_id);
}

struct java_tls_key_operation_handler_properties tls_key_operation_handler_properties;

static void s_cache_tls_key_operation_handler(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/TlsKeyOperationHandler");
    AWS_FATAL_ASSERT(cls);

    tls_key_operation_handler_properties.perform_operation_id =
        (*env)->GetMethodID(env, cls, "performOperation", "(Lsoftware/amazon/awssdk/crt/io/TlsKeyOperation;)V");
    AWS_FATAL_ASSERT(tls_key_operation_handler_properties.perform_operation_id);
}

struct java_http_client_connection_manager_properties http_client_connection_manager_properties;

static void s_cache_http_client_connection_manager(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpClientConnectionManager");
    AWS_FATAL_ASSERT(cls);

    http_client_connection_manager_properties.onShutdownComplete =
        (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(http_client_connection_manager_properties.onShutdownComplete);
}

struct java_http2_stream_manager_properties http2_stream_manager_properties;

static void s_cache_http2_stream_manager(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/Http2StreamManager");
    AWS_FATAL_ASSERT(cls);

    http2_stream_manager_properties.onShutdownComplete = (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(http2_stream_manager_properties.onShutdownComplete);
}

struct java_http_client_connection_properties http_client_connection_properties;

static void s_cache_http_client_connection(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpClientConnection");
    AWS_FATAL_ASSERT(cls);
    http_client_connection_properties.http_client_connection_class = (*env)->NewGlobalRef(env, cls);

    http_client_connection_properties.on_connection_acquired_method_id =
        (*env)->GetStaticMethodID(env, cls, "onConnectionAcquired", "(Ljava/util/concurrent/CompletableFuture;JI)V");
    AWS_FATAL_ASSERT(http_client_connection_properties.on_connection_acquired_method_id);
}

struct java_http_stream_properties http_stream_properties;

static void s_cache_http_stream(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpStream");
    AWS_FATAL_ASSERT(cls);
    http_stream_properties.stream_class = (*env)->NewGlobalRef(env, cls);

    http_stream_properties.constructor = (*env)->GetMethodID(env, cls, "<init>", "(J)V");
    AWS_FATAL_ASSERT(http_stream_properties.constructor);

    http_stream_properties.close = (*env)->GetMethodID(env, cls, "close", "()V");
    AWS_FATAL_ASSERT(http_stream_properties.close);
}

struct java_http2_stream_properties http2_stream_properties;

static void s_cache_http2_stream(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/Http2Stream");
    AWS_FATAL_ASSERT(cls);
    http2_stream_properties.stream_class = (*env)->NewGlobalRef(env, cls);

    http2_stream_properties.constructor = (*env)->GetMethodID(env, cls, "<init>", "(J)V");
    AWS_FATAL_ASSERT(http2_stream_properties.constructor);
}

struct java_http_stream_response_handler_native_adapter_properties http_stream_response_handler_properties;

static void s_cache_http_stream_response_handler_native_adapter(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpStreamResponseHandlerNativeAdapter");
    AWS_FATAL_ASSERT(cls);

    http_stream_response_handler_properties.onResponseHeaders = (*env)->GetMethodID(
        env, cls, "onResponseHeaders", "(Lsoftware/amazon/awssdk/crt/http/HttpStreamBase;IILjava/nio/ByteBuffer;)V");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseHeaders);

    http_stream_response_handler_properties.onResponseHeadersDone =
        (*env)->GetMethodID(env, cls, "onResponseHeadersDone", "(Lsoftware/amazon/awssdk/crt/http/HttpStreamBase;I)V");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseHeadersDone);

    http_stream_response_handler_properties.onResponseBody = (*env)->GetMethodID(
        env, cls, "onResponseBody", "(Lsoftware/amazon/awssdk/crt/http/HttpStreamBase;Ljava/nio/ByteBuffer;)I");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseBody);

    http_stream_response_handler_properties.onResponseComplete =
        (*env)->GetMethodID(env, cls, "onResponseComplete", "(Lsoftware/amazon/awssdk/crt/http/HttpStreamBase;I)V");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseComplete);
}

struct java_http_stream_write_chunk_completion_properties http_stream_write_chunk_completion_properties;

static void s_cache_http_stream_write_chunk_completion_properties(JNIEnv *env) {
    jclass cls =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpStream$HttpStreamWriteChunkCompletionCallback");
    AWS_FATAL_ASSERT(cls);

    http_stream_write_chunk_completion_properties.callback = (*env)->GetMethodID(env, cls, "onChunkCompleted", "(I)V");
    AWS_FATAL_ASSERT(http_stream_write_chunk_completion_properties.callback);
}

struct java_event_stream_server_listener_properties event_stream_server_listener_properties;

static void s_cache_event_stream_server_listener_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ServerListener");
    AWS_FATAL_ASSERT(cls);

    event_stream_server_listener_properties.onShutdownComplete =
        (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(event_stream_server_listener_properties.onShutdownComplete);
}

struct java_event_stream_server_listener_handler_properties event_stream_server_listener_handler_properties;

static void s_cache_event_stream_server_listener_handler_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ServerListenerHandler");
    AWS_FATAL_ASSERT(cls);

    event_stream_server_listener_handler_properties.connCls =
        (*env)->NewGlobalRef(env, (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ServerConnection"));
    AWS_FATAL_ASSERT(event_stream_server_listener_handler_properties.connCls);

    event_stream_server_listener_handler_properties.newConnConstructor =
        (*env)->GetMethodID(env, event_stream_server_listener_handler_properties.connCls, "<init>", "(J)V");
    AWS_FATAL_ASSERT(event_stream_server_listener_handler_properties.newConnConstructor);

    event_stream_server_listener_handler_properties.onNewConnection = (*env)->GetMethodID(
        env,
        cls,
        "onNewConnection",
        "(Lsoftware/amazon/awssdk/crt/eventstream/ServerConnection;I)Lsoftware/amazon/awssdk/crt/eventstream/"
        "ServerConnectionHandler;");
    AWS_FATAL_ASSERT(event_stream_server_listener_handler_properties.onNewConnection);
    event_stream_server_listener_handler_properties.onConnectionShutdown = (*env)->GetMethodID(
        env, cls, "onConnectionShutdownShim", "(Lsoftware/amazon/awssdk/crt/eventstream/ServerConnection;I)V");
    AWS_FATAL_ASSERT(event_stream_server_listener_handler_properties.onConnectionShutdown);
}

struct java_event_stream_server_connection_handler_properties event_stream_server_connection_handler_properties;

static void s_cache_event_stream_server_connection_handler_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ServerConnectionHandler");
    AWS_FATAL_ASSERT(cls);

    event_stream_server_connection_handler_properties.continuationCls = (*env)->NewGlobalRef(
        env, (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ServerConnectionContinuation"));
    AWS_FATAL_ASSERT(event_stream_server_connection_handler_properties.continuationCls);

    event_stream_server_connection_handler_properties.newContinuationConstructor =
        (*env)->GetMethodID(env, event_stream_server_connection_handler_properties.continuationCls, "<init>", "(J)V");
    AWS_FATAL_ASSERT(event_stream_server_connection_handler_properties.newContinuationConstructor);

    event_stream_server_connection_handler_properties.onProtocolMessage =
        (*env)->GetMethodID(env, cls, "onProtocolMessage", "([B[BII)V");
    AWS_FATAL_ASSERT(event_stream_server_connection_handler_properties.onProtocolMessage);

    event_stream_server_connection_handler_properties.onIncomingStream = (*env)->GetMethodID(
        env,
        cls,
        "onIncomingStream",
        "(Lsoftware/amazon/awssdk/crt/eventstream/ServerConnectionContinuation;[B)Lsoftware/amazon/awssdk/crt/"
        "eventstream/ServerConnectionContinuationHandler;");
    AWS_FATAL_ASSERT(event_stream_server_connection_handler_properties.onIncomingStream);
}

struct java_event_stream_server_continuation_handler_properties event_stream_server_continuation_handler_properties;

static void s_cache_event_stream_server_continuation_handler_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ServerConnectionContinuationHandler");
    AWS_FATAL_ASSERT(cls);

    event_stream_server_continuation_handler_properties.onContinuationMessage =
        (*env)->GetMethodID(env, cls, "onContinuationMessageShim", "([B[BII)V");
    AWS_FATAL_ASSERT(event_stream_server_continuation_handler_properties.onContinuationMessage);
    event_stream_server_continuation_handler_properties.onContinuationClosed =
        (*env)->GetMethodID(env, cls, "onContinuationClosedShim", "()V");
    AWS_FATAL_ASSERT(event_stream_server_continuation_handler_properties.onContinuationClosed);
}

struct java_event_stream_client_connection_handler_properties event_stream_client_connection_handler_properties;

static void s_cache_event_stream_client_connection_handler_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ClientConnectionHandler");
    AWS_FATAL_ASSERT(cls);

    event_stream_client_connection_handler_properties.onSetup =
        (*env)->GetMethodID(env, cls, "onConnectionSetupShim", "(JI)V");
    AWS_FATAL_ASSERT(event_stream_client_connection_handler_properties.onSetup);
    event_stream_client_connection_handler_properties.onProtocolMessage =
        (*env)->GetMethodID(env, cls, "onProtocolMessage", "([B[BII)V");
    AWS_FATAL_ASSERT(event_stream_client_connection_handler_properties.onProtocolMessage);
    event_stream_client_connection_handler_properties.onClosed =
        (*env)->GetMethodID(env, cls, "onConnectionClosedShim", "(I)V");
    AWS_FATAL_ASSERT(event_stream_client_connection_handler_properties.onClosed);
}

struct java_event_stream_client_continuation_handler_properties event_stream_client_continuation_handler_properties;

static void s_cache_event_stream_client_continuation_handler_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/ClientConnectionContinuationHandler");
    AWS_FATAL_ASSERT(cls);

    event_stream_client_continuation_handler_properties.onContinuationMessage =
        (*env)->GetMethodID(env, cls, "onContinuationMessageShim", "([B[BII)V");
    AWS_FATAL_ASSERT(event_stream_client_continuation_handler_properties.onContinuationMessage);
    event_stream_client_continuation_handler_properties.onContinuationClosed =
        (*env)->GetMethodID(env, cls, "onContinuationClosedShim", "()V");
    AWS_FATAL_ASSERT(event_stream_client_continuation_handler_properties.onContinuationClosed);
}

struct java_event_stream_message_flush_properties event_stream_server_message_flush_properties;

static void s_cache_event_stream_message_flush_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/eventstream/MessageFlushCallback");
    AWS_FATAL_ASSERT(cls);

    event_stream_server_message_flush_properties.callback = (*env)->GetMethodID(env, cls, "onCallbackInvoked", "(I)V");
    AWS_FATAL_ASSERT(event_stream_server_message_flush_properties.callback);
}

struct java_cpu_info_properties cpu_info_properties;

static void s_cache_cpu_info_properties(JNIEnv *env) {
    cpu_info_properties.cpu_info_class =
        (*env)->NewGlobalRef(env, (*env)->FindClass(env, "software/amazon/awssdk/crt/SystemInfo$CpuInfo"));
    AWS_FATAL_ASSERT(cpu_info_properties.cpu_info_class);

    cpu_info_properties.cpu_info_constructor =
        (*env)->GetMethodID(env, cpu_info_properties.cpu_info_class, "<init>", "(IZ)V");
}

struct java_s3_client_properties s3_client_properties;

static void s_cache_s3_client_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/s3/S3Client");
    AWS_FATAL_ASSERT(cls);

    s3_client_properties.onShutdownComplete = (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(s3_client_properties.onShutdownComplete);
}

struct java_s3_meta_request_properties s3_meta_request_properties;

static void s_cache_s3_meta_request_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/s3/S3MetaRequest");
    AWS_FATAL_ASSERT(cls);

    s3_meta_request_properties.onShutdownComplete = (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(s3_meta_request_properties.onShutdownComplete);
}

struct java_s3_meta_request_response_handler_native_adapter_properties
    s3_meta_request_response_handler_native_adapter_properties;

static void s_cache_s3_meta_request_response_handler_native_adapter_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/s3/S3MetaRequestResponseHandlerNativeAdapter");
    AWS_FATAL_ASSERT(cls);

    s3_meta_request_response_handler_native_adapter_properties.onResponseBody =
        (*env)->GetMethodID(env, cls, "onResponseBody", "([BJJ)I");
    AWS_FATAL_ASSERT(s3_meta_request_response_handler_native_adapter_properties.onResponseBody);

    s3_meta_request_response_handler_native_adapter_properties.onFinished =
        (*env)->GetMethodID(env, cls, "onFinished", "(II[BIZ)V");
    AWS_FATAL_ASSERT(s3_meta_request_response_handler_native_adapter_properties.onFinished);

    s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders =
        (*env)->GetMethodID(env, cls, "onResponseHeaders", "(ILjava/nio/ByteBuffer;)V");
    AWS_FATAL_ASSERT(s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders);

    s3_meta_request_response_handler_native_adapter_properties.onProgress =
        (*env)->GetMethodID(env, cls, "onProgress", "(Lsoftware/amazon/awssdk/crt/s3/S3MetaRequestProgress;)V");
    AWS_FATAL_ASSERT(s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders);
}

struct java_completable_future_properties completable_future_properties;

static void s_cache_completable_future(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/util/concurrent/CompletableFuture");
    AWS_FATAL_ASSERT(cls);

    completable_future_properties.complete_method_id =
        (*env)->GetMethodID(env, cls, "complete", "(Ljava/lang/Object;)Z");
    AWS_FATAL_ASSERT(completable_future_properties.complete_method_id != NULL);

    completable_future_properties.complete_exceptionally_method_id =
        (*env)->GetMethodID(env, cls, "completeExceptionally", "(Ljava/lang/Throwable;)Z");
    AWS_FATAL_ASSERT(completable_future_properties.complete_exceptionally_method_id != NULL);
}

struct java_crt_runtime_exception_properties crt_runtime_exception_properties;

static void s_cache_crt_runtime_exception(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtRuntimeException");
    AWS_FATAL_ASSERT(cls);
    crt_runtime_exception_properties.crt_runtime_exception_class = (*env)->NewGlobalRef(env, cls);

    crt_runtime_exception_properties.constructor_method_id = (*env)->GetMethodID(env, cls, "<init>", "(I)V");
    AWS_FATAL_ASSERT(crt_runtime_exception_properties.constructor_method_id);

    crt_runtime_exception_properties.error_code_field_id = (*env)->GetFieldID(env, cls, "errorCode", "I");
    AWS_FATAL_ASSERT(crt_runtime_exception_properties.error_code_field_id);
}

struct java_ecc_key_pair_properties ecc_key_pair_properties;

static void s_cache_ecc_key_pair(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/cal/EccKeyPair");
    AWS_FATAL_ASSERT(cls);
    ecc_key_pair_properties.ecc_key_pair_class = (*env)->NewGlobalRef(env, cls);

    ecc_key_pair_properties.constructor = (*env)->GetMethodID(env, cls, "<init>", "(J)V");
    AWS_FATAL_ASSERT(ecc_key_pair_properties.constructor);
}

struct java_crt_properties crt_properties;

static void s_cache_crt(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/CRT");
    AWS_FATAL_ASSERT(cls);
    crt_properties.crt_class = (*env)->NewGlobalRef(env, cls);

    crt_properties.test_jni_exception_method_id = (*env)->GetStaticMethodID(env, cls, "testJniException", "(Z)V");
    AWS_FATAL_ASSERT(crt_properties.test_jni_exception_method_id);
}

struct java_aws_signing_result_properties aws_signing_result_properties;

static void s_cache_aws_signing_result(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/signing/AwsSigningResult");
    AWS_FATAL_ASSERT(cls);
    aws_signing_result_properties.aws_signing_result_class = (*env)->NewGlobalRef(env, cls);

    aws_signing_result_properties.constructor = (*env)->GetMethodID(env, cls, "<init>", "()V");
    AWS_FATAL_ASSERT(aws_signing_result_properties.constructor);

    aws_signing_result_properties.signed_request_field_id =
        (*env)->GetFieldID(env, cls, "signedRequest", "Lsoftware/amazon/awssdk/crt/http/HttpRequest;");
    AWS_FATAL_ASSERT(aws_signing_result_properties.signed_request_field_id);

    aws_signing_result_properties.signature_field_id = (*env)->GetFieldID(env, cls, "signature", "[B");
    AWS_FATAL_ASSERT(aws_signing_result_properties.signature_field_id);
}

struct java_http_header_properties http_header_properties;

static void s_cache_http_header(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpHeader");
    AWS_FATAL_ASSERT(cls);
    http_header_properties.http_header_class = (*env)->NewGlobalRef(env, cls);

    http_header_properties.constructor_method_id = (*env)->GetMethodID(env, cls, "<init>", "([B[B)V");
    AWS_FATAL_ASSERT(http_header_properties.constructor_method_id);
}

struct java_http_manager_metrics_properties http_manager_metrics_properties;
static void s_cache_http_manager_metrics(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpManagerMetrics");
    AWS_FATAL_ASSERT(cls);
    http_manager_metrics_properties.http_manager_metrics_class = (*env)->NewGlobalRef(env, cls);

    http_manager_metrics_properties.constructor_method_id = (*env)->GetMethodID(env, cls, "<init>", "(JJJ)V");
    AWS_FATAL_ASSERT(http_manager_metrics_properties.constructor_method_id);
}

struct java_aws_exponential_backoff_retry_options_properties exponential_backoff_retry_options_properties;

static void s_cache_exponential_backoff_retry_options(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions");
    AWS_FATAL_ASSERT(cls);
    exponential_backoff_retry_options_properties.exponential_backoff_retry_options_class =
        (*env)->NewGlobalRef(env, cls);

    exponential_backoff_retry_options_properties.exponential_backoff_retry_options_constructor_method_id =
        (*env)->GetMethodID(
            env, exponential_backoff_retry_options_properties.exponential_backoff_retry_options_class, "<init>", "()V");
    AWS_FATAL_ASSERT(
        exponential_backoff_retry_options_properties.exponential_backoff_retry_options_constructor_method_id);

    exponential_backoff_retry_options_properties.el_group_field_id =
        (*env)->GetFieldID(env, cls, "eventLoopGroup", "Lsoftware/amazon/awssdk/crt/io/EventLoopGroup;");
    AWS_FATAL_ASSERT(exponential_backoff_retry_options_properties.el_group_field_id);

    exponential_backoff_retry_options_properties.max_retries_field_id = (*env)->GetFieldID(env, cls, "maxRetries", "J");
    AWS_FATAL_ASSERT(exponential_backoff_retry_options_properties.max_retries_field_id);

    exponential_backoff_retry_options_properties.backoff_scale_factor_ms_field_id =
        (*env)->GetFieldID(env, cls, "backoffScaleFactorMS", "J");
    AWS_FATAL_ASSERT(exponential_backoff_retry_options_properties.backoff_scale_factor_ms_field_id);

    exponential_backoff_retry_options_properties.jitter_mode_field_id = (*env)->GetFieldID(
        env, cls, "jitterMode", "Lsoftware/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions$JitterMode;");
    AWS_FATAL_ASSERT(exponential_backoff_retry_options_properties.jitter_mode_field_id);

    jclass jitter_mode_cls =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions$JitterMode");
    AWS_FATAL_ASSERT(jitter_mode_cls);
    exponential_backoff_retry_options_properties.jitter_mode_class = (*env)->NewGlobalRef(env, jitter_mode_cls);

    exponential_backoff_retry_options_properties.jitter_mode_value_field_id =
        (*env)->GetFieldID(env, jitter_mode_cls, "value", "I");

    AWS_FATAL_ASSERT(exponential_backoff_retry_options_properties.jitter_mode_value_field_id);
}

struct java_aws_standard_retry_options_properties standard_retry_options_properties;

static void s_cache_standard_retry_options(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/StandardRetryOptions");
    AWS_FATAL_ASSERT(cls);
    standard_retry_options_properties.standard_retry_options_class = (*env)->NewGlobalRef(env, cls);

    standard_retry_options_properties.standard_retry_options_constructor_method_id =
        (*env)->GetMethodID(env, standard_retry_options_properties.standard_retry_options_class, "<init>", "()V");

    standard_retry_options_properties.backoff_retry_options_field_id = (*env)->GetFieldID(
        env, cls, "backoffRetryOptions", "Lsoftware/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions;");
    AWS_FATAL_ASSERT(standard_retry_options_properties.backoff_retry_options_field_id);

    standard_retry_options_properties.initial_bucket_capacity_field_id =
        (*env)->GetFieldID(env, cls, "initialBucketCapacity", "J");
    AWS_FATAL_ASSERT(standard_retry_options_properties.initial_bucket_capacity_field_id);
}

struct java_aws_directory_traversal_handler_properties directory_traversal_handler_properties;

static void s_cache_directory_traversal_handler(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/DirectoryTraversalHandler");
    AWS_FATAL_ASSERT(cls);
    directory_traversal_handler_properties.directory_traversal_handler_class = (*env)->NewGlobalRef(env, cls);

    directory_traversal_handler_properties.on_directory_entry_method_id = (*env)->GetMethodID(
        env,
        directory_traversal_handler_properties.directory_traversal_handler_class,
        "onDirectoryEntry",
        "(Lsoftware/amazon/awssdk/crt/io/DirectoryEntry;)Z");
}

struct java_aws_directory_entry_properties directory_entry_properties;

static void s_cache_directory_entry(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/DirectoryEntry");
    AWS_FATAL_ASSERT(cls);
    directory_entry_properties.directory_entry_class = (*env)->NewGlobalRef(env, cls);

    directory_entry_properties.directory_entry_constructor_method_id =
        (*env)->GetMethodID(env, directory_entry_properties.directory_entry_class, "<init>", "()V");

    directory_entry_properties.path_field_id = (*env)->GetFieldID(env, cls, "path", "Ljava/lang/String;");

    directory_entry_properties.relative_path_field_id =
        (*env)->GetFieldID(env, cls, "relativePath", "Ljava/lang/String;");

    directory_entry_properties.is_directory_field_id = (*env)->GetFieldID(env, cls, "isDirectory", "Z");

    directory_entry_properties.is_symlink_field_id = (*env)->GetFieldID(env, cls, "isSymLink", "Z");

    directory_entry_properties.is_file_field_id = (*env)->GetFieldID(env, cls, "isFile", "Z");

    directory_entry_properties.file_size_field_id = (*env)->GetFieldID(env, cls, "fileSize", "J");
}

struct java_aws_s3_meta_request_progress s3_meta_request_progress_properties;

static void s_cache_s3_meta_request_progress(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/s3/S3MetaRequestProgress");
    AWS_FATAL_ASSERT(cls);
    s3_meta_request_progress_properties.s3_meta_request_progress_class = (*env)->NewGlobalRef(env, cls);

    s3_meta_request_progress_properties.s3_meta_request_progress_constructor_method_id =
        (*env)->GetMethodID(env, s3_meta_request_progress_properties.s3_meta_request_progress_class, "<init>", "()V");

    s3_meta_request_progress_properties.bytes_transferred_field_id =
        (*env)->GetFieldID(env, cls, "bytesTransferred", "J");
    s3_meta_request_progress_properties.content_length_field_id = (*env)->GetFieldID(env, cls, "contentLength", "J");
}
struct java_aws_s3_tcp_keep_alive_options_properties s3_tcp_keep_alive_options_properties;

static void s_cache_s3_tcp_keep_alive_options(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/s3/S3TcpKeepAliveOptions");
    AWS_FATAL_ASSERT(cls);
    s3_tcp_keep_alive_options_properties.s3_tcp_keep_alive_options_class = (*env)->NewGlobalRef(env, cls);

    s3_tcp_keep_alive_options_properties.s3_tcp_keep_alive_options_constructor_method_id =
        (*env)->GetMethodID(env, s3_tcp_keep_alive_options_properties.s3_tcp_keep_alive_options_class, "<init>", "()V");

    s3_tcp_keep_alive_options_properties.keep_alive_interval_sec_field_id =
        (*env)->GetFieldID(env, cls, "keepAliveIntervalSec", "S");

    s3_tcp_keep_alive_options_properties.keep_alive_timeout_sec_field_id =
        (*env)->GetFieldID(env, cls, "keepAliveTimeoutSec", "S");

    s3_tcp_keep_alive_options_properties.keep_alive_max_failed_probes_field_id =
        (*env)->GetFieldID(env, cls, "keepAliveMaxFailedProbes", "S");
}

struct java_aws_s3_meta_request_resume_token s3_meta_request_resume_token_properties;

static void s_cache_s3_meta_request_resume_token(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/s3/ResumeToken");
    AWS_FATAL_ASSERT(cls);
    s3_meta_request_resume_token_properties.s3_meta_request_resume_token_class = (*env)->NewGlobalRef(env, cls);

    s3_meta_request_resume_token_properties.s3_meta_request_resume_token_constructor_method_id =
        (*env)->GetMethodID(env, s3_meta_request_progress_properties.s3_meta_request_progress_class, "<init>", "()V");

    s3_meta_request_resume_token_properties.native_type_field_id = (*env)->GetFieldID(env, cls, "nativeType", "I");
    AWS_FATAL_ASSERT(s3_meta_request_resume_token_properties.native_type_field_id);
    s3_meta_request_resume_token_properties.part_size_field_id = (*env)->GetFieldID(env, cls, "partSize", "J");
    s3_meta_request_resume_token_properties.total_num_parts_field_id =
        (*env)->GetFieldID(env, cls, "totalNumParts", "J");
    s3_meta_request_resume_token_properties.num_parts_completed_field_id =
        (*env)->GetFieldID(env, cls, "numPartsCompleted", "J");
    s3_meta_request_resume_token_properties.upload_id_field_id =
        (*env)->GetFieldID(env, cls, "uploadId", "Ljava/lang/String;");
}

struct java_aws_mqtt5_connack_packet_properties mqtt5_connack_packet_properties;

static void s_cache_mqtt5_connack_packet(JNIEnv *env) {
    (void)env;

    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/ConnAckPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_connack_packet_properties.connack_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_packet_class);

    // Functions
    mqtt5_connack_packet_properties.connack_constructor_id =
        (*env)->GetMethodID(env, mqtt5_connack_packet_properties.connack_packet_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_constructor_id);
    mqtt5_connack_packet_properties.connack_native_add_maximum_qos_id =
        (*env)->GetMethodID(env, mqtt5_connack_packet_properties.connack_packet_class, "nativeAddMaximumQOS", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_native_add_maximum_qos_id);
    mqtt5_connack_packet_properties.connack_native_add_reason_code_id =
        (*env)->GetMethodID(env, mqtt5_connack_packet_properties.connack_packet_class, "nativeAddReasonCode", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_native_add_reason_code_id);
    // Field IDs
    mqtt5_connack_packet_properties.connack_session_present_field_id =
        (*env)->GetFieldID(env, mqtt5_connack_packet_properties.connack_packet_class, "sessionPresent", "Z");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_session_present_field_id);
    mqtt5_connack_packet_properties.connack_reason_code_field_id = (*env)->GetFieldID(
        env,
        mqtt5_connack_packet_properties.connack_packet_class,
        "reasonCode",
        "Lsoftware/amazon/awssdk/crt/mqtt5/packets/ConnAckPacket$ConnectReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_reason_code_field_id);
    mqtt5_connack_packet_properties.connack_session_expiry_interval_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "sessionExpiryIntervalSeconds", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_session_expiry_interval_field_id);
    mqtt5_connack_packet_properties.connack_receive_maximum_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "receiveMaximum", "Ljava/lang/Integer;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_receive_maximum_field_id);
    mqtt5_connack_packet_properties.connack_maximum_qos_field_id = (*env)->GetFieldID(
        env,
        mqtt5_connack_packet_properties.connack_packet_class,
        "maximumQOS",
        "Lsoftware/amazon/awssdk/crt/mqtt5/QOS;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_maximum_qos_field_id);
    mqtt5_connack_packet_properties.connack_retain_available_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "retainAvailable", "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_retain_available_field_id);
    mqtt5_connack_packet_properties.connack_maximum_packet_size_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "maximumPacketSize", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_maximum_packet_size_field_id);
    mqtt5_connack_packet_properties.connack_assigned_client_identifier_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "assignedClientIdentifier", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_assigned_client_identifier_field_id);
    mqtt5_connack_packet_properties.connack_reason_string_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "reasonString", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_reason_string_field_id);
    mqtt5_connack_packet_properties.connack_wildcard_subscriptions_available_field_id = (*env)->GetFieldID(
        env,
        mqtt5_connack_packet_properties.connack_packet_class,
        "wildcardSubscriptionsAvailable",
        "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_wildcard_subscriptions_available_field_id);
    mqtt5_connack_packet_properties.connack_subscription_identifiers_available_field_id = (*env)->GetFieldID(
        env,
        mqtt5_connack_packet_properties.connack_packet_class,
        "subscriptionIdentifiersAvailable",
        "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_subscription_identifiers_available_field_id);
    mqtt5_connack_packet_properties.connack_shared_subscriptions_available_field_id = (*env)->GetFieldID(
        env,
        mqtt5_connack_packet_properties.connack_packet_class,
        "sharedSubscriptionsAvailable",
        "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_shared_subscriptions_available_field_id);
    mqtt5_connack_packet_properties.connack_server_keep_alive_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "serverKeepAlive", "Ljava/lang/Integer;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_server_keep_alive_field_id);
    mqtt5_connack_packet_properties.connack_response_information_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "responseInformation", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_response_information_field_id);
    mqtt5_connack_packet_properties.connack_server_reference_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "serverReference", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_server_reference_field_id);
    mqtt5_connack_packet_properties.connack_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_connack_packet_properties.connack_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_connack_packet_properties.connack_user_properties_field_id);
}

struct java_aws_mqtt5_connect_reason_code_properties mqtt5_connect_reason_code_properties;

static void s_cache_mqtt5_connect_reason_code(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/ConnAckPacket$ConnectReasonCode");
    AWS_FATAL_ASSERT(cls);
    mqtt5_connect_reason_code_properties.reason_code_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_connect_reason_code_properties.reason_code_class);
    // Functions
    mqtt5_connect_reason_code_properties.code_get_value_id =
        (*env)->GetMethodID(env, mqtt5_connect_reason_code_properties.reason_code_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_connect_reason_code_properties.code_get_value_id);
    // Static functions
    mqtt5_connect_reason_code_properties.code_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_connect_reason_code_properties.reason_code_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/packets/ConnAckPacket$ConnectReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_connect_reason_code_properties.code_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_connect_packet_properties mqtt5_connect_packet_properties;

static void s_cache_mqtt5_connect_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/ConnectPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_connect_packet_properties.connect_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_packet_class);
    // Field IDs
    mqtt5_connect_packet_properties.connect_keep_alive_interval_seconds_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "keepAliveIntervalSeconds", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_keep_alive_interval_seconds_field_id);
    mqtt5_connect_packet_properties.connect_client_id_field_id =
        (*env)->GetFieldID(env, mqtt5_connect_packet_properties.connect_packet_class, "clientId", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_client_id_field_id);
    mqtt5_connect_packet_properties.connect_username_field_id =
        (*env)->GetFieldID(env, mqtt5_connect_packet_properties.connect_packet_class, "username", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_username_field_id);
    mqtt5_connect_packet_properties.connect_password_field_id =
        (*env)->GetFieldID(env, mqtt5_connect_packet_properties.connect_packet_class, "password", "[B");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_password_field_id);
    mqtt5_connect_packet_properties.connect_session_expiry_interval_seconds_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "sessionExpiryIntervalSeconds", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_session_expiry_interval_seconds_field_id);
    mqtt5_connect_packet_properties.connect_request_response_information_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "requestResponseInformation", "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_request_response_information_field_id);
    mqtt5_connect_packet_properties.connect_request_problem_information_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "requestProblemInformation", "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_request_problem_information_field_id);
    mqtt5_connect_packet_properties.connect_receive_maximum_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "receiveMaximum", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_receive_maximum_field_id);
    mqtt5_connect_packet_properties.connect_maximum_packet_size_bytes_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "maximumPacketSizeBytes", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_maximum_packet_size_bytes_field_id);
    mqtt5_connect_packet_properties.connect_will_delay_interval_seconds_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "willDelayIntervalSeconds", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_will_delay_interval_seconds_field_id);
    mqtt5_connect_packet_properties.connect_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_connect_packet_properties.connect_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_user_properties_field_id);
    mqtt5_connect_packet_properties.connect_will_field_id = (*env)->GetFieldID(
        env,
        mqtt5_connect_packet_properties.connect_packet_class,
        "will",
        "Lsoftware/amazon/awssdk/crt/mqtt5/packets/PublishPacket;");
    AWS_FATAL_ASSERT(mqtt5_connect_packet_properties.connect_will_field_id);
}

struct java_aws_mqtt5_disconnect_packet_properties mqtt5_disconnect_packet_properties;

static void s_cache_mqtt5_disconnect_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/DisconnectPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_disconnect_packet_properties.disconnect_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_packet_class);
    // Functions
    mqtt5_disconnect_packet_properties.disconnect_constructor_id =
        (*env)->GetMethodID(env, mqtt5_disconnect_packet_properties.disconnect_packet_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_constructor_id);
    mqtt5_disconnect_packet_properties.disconnect_native_add_disconnect_reason_code_id = (*env)->GetMethodID(
        env, mqtt5_disconnect_packet_properties.disconnect_packet_class, "nativeAddDisconnectReasonCode", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_native_add_disconnect_reason_code_id);
    mqtt5_disconnect_packet_properties.disconnect_get_reason_code_id = (*env)->GetMethodID(
        env,
        mqtt5_disconnect_packet_properties.disconnect_packet_class,
        "getReasonCode",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/packets/DisconnectPacket$DisconnectReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_get_reason_code_id);
    // Field IDs
    mqtt5_disconnect_packet_properties.disconnect_reason_code_field_id = (*env)->GetFieldID(
        env,
        mqtt5_disconnect_packet_properties.disconnect_packet_class,
        "reasonCode",
        "Lsoftware/amazon/awssdk/crt/mqtt5/packets/DisconnectPacket$DisconnectReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_reason_code_field_id);
    mqtt5_disconnect_packet_properties.disconnect_session_expiry_interval_seconds_field_id = (*env)->GetFieldID(
        env,
        mqtt5_disconnect_packet_properties.disconnect_packet_class,
        "sessionExpiryIntervalSeconds",
        "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_session_expiry_interval_seconds_field_id);
    mqtt5_disconnect_packet_properties.disconnect_reason_string_field_id = (*env)->GetFieldID(
        env, mqtt5_disconnect_packet_properties.disconnect_packet_class, "reasonString", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_reason_string_field_id);
    mqtt5_disconnect_packet_properties.disconnect_session_server_reference_field_id = (*env)->GetFieldID(
        env, mqtt5_disconnect_packet_properties.disconnect_packet_class, "serverReference", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_session_server_reference_field_id);
    mqtt5_disconnect_packet_properties.disconnect_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_disconnect_packet_properties.disconnect_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_packet_properties.disconnect_user_properties_field_id);
}

struct java_aws_mqtt5_disconnect_reason_code_properties mqtt5_disconnect_reason_code_properties;

static void s_cache_mqtt5_disconnect_reason_code(JNIEnv *env) {
    jclass cls =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/DisconnectPacket$DisconnectReasonCode");
    AWS_FATAL_ASSERT(cls);
    mqtt5_disconnect_reason_code_properties.reason_code_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_disconnect_reason_code_properties.reason_code_class);
    // Functions
    mqtt5_disconnect_reason_code_properties.code_get_value_id =
        (*env)->GetMethodID(env, mqtt5_disconnect_reason_code_properties.reason_code_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_disconnect_reason_code_properties.code_get_value_id);
    // Static functions
    mqtt5_disconnect_reason_code_properties.code_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_disconnect_reason_code_properties.reason_code_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/packets/DisconnectPacket$DisconnectReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_disconnect_reason_code_properties.code_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_puback_packet_properties mqtt5_puback_packet_properties;

static void s_cache_mqtt5_puback_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/PubAckPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_puback_packet_properties.puback_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_puback_packet_properties.puback_packet_class);
    // Functions
    mqtt5_puback_packet_properties.puback_constructor_id =
        (*env)->GetMethodID(env, mqtt5_puback_packet_properties.puback_packet_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_puback_packet_properties.puback_constructor_id);
    mqtt5_puback_packet_properties.puback_native_add_reason_code_id =
        (*env)->GetMethodID(env, mqtt5_puback_packet_properties.puback_packet_class, "nativeAddReasonCode", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_puback_packet_properties.puback_native_add_reason_code_id);
    // Field IDs
    mqtt5_puback_packet_properties.puback_reason_code_field_id = (*env)->GetFieldID(
        env,
        mqtt5_puback_packet_properties.puback_packet_class,
        "reasonCode",
        "Lsoftware/amazon/awssdk/crt/mqtt5/packets/PubAckPacket$PubAckReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_puback_packet_properties.puback_reason_code_field_id);
    mqtt5_puback_packet_properties.puback_reason_string_field_id = (*env)->GetFieldID(
        env, mqtt5_puback_packet_properties.puback_packet_class, "reasonString", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_puback_packet_properties.puback_reason_string_field_id);
    mqtt5_puback_packet_properties.puback_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_puback_packet_properties.puback_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_puback_packet_properties.puback_user_properties_field_id);
}

struct java_aws_mqtt5_puback_reason_code_properties mqtt5_puback_reason_code_properties;

static void s_cache_mqtt5_puback_reason_code(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/PubAckPacket$PubAckReasonCode");
    AWS_FATAL_ASSERT(cls);
    mqtt5_puback_reason_code_properties.reason_code_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_puback_reason_code_properties.reason_code_class);
    // Functions
    mqtt5_puback_reason_code_properties.code_get_value_id =
        (*env)->GetMethodID(env, mqtt5_puback_reason_code_properties.reason_code_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_puback_reason_code_properties.code_get_value_id);
    // Static functions
    mqtt5_puback_reason_code_properties.code_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_puback_reason_code_properties.reason_code_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/packets/PubAckPacket$PubAckReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_puback_reason_code_properties.code_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_publish_packet_properties mqtt5_publish_packet_properties;

static void s_cache_mqtt5_publish_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/PublishPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_publish_packet_properties.publish_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_packet_class);
    // Functions
    mqtt5_publish_packet_properties.publish_constructor_id =
        (*env)->GetMethodID(env, mqtt5_publish_packet_properties.publish_packet_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_constructor_id);

    mqtt5_publish_packet_properties.publish_native_set_qos_id =
        (*env)->GetMethodID(env, mqtt5_publish_packet_properties.publish_packet_class, "nativeSetQOS", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_native_set_qos_id);
    mqtt5_publish_packet_properties.publish_native_set_payload_format_indicator_id = (*env)->GetMethodID(
        env, mqtt5_publish_packet_properties.publish_packet_class, "nativeSetPayloadFormatIndicator", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_native_set_payload_format_indicator_id);
    mqtt5_publish_packet_properties.publish_get_qos_id = (*env)->GetMethodID(
        env,
        mqtt5_publish_packet_properties.publish_packet_class,
        "getQOS",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/QOS;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_get_qos_id);

    mqtt5_publish_packet_properties.publish_get_payload_format_id = (*env)->GetMethodID(
        env,
        mqtt5_publish_packet_properties.publish_packet_class,
        "getPayloadFormat",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/packets/PublishPacket$PayloadFormatIndicator;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_get_payload_format_id);

    // FieldIDs
    mqtt5_publish_packet_properties.publish_payload_field_id =
        (*env)->GetFieldID(env, mqtt5_publish_packet_properties.publish_packet_class, "payload", "[B");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_payload_field_id);
    mqtt5_publish_packet_properties.publish_qos_field_id = (*env)->GetFieldID(
        env,
        mqtt5_publish_packet_properties.publish_packet_class,
        "packetQOS",
        "Lsoftware/amazon/awssdk/crt/mqtt5/QOS;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_qos_field_id);
    mqtt5_publish_packet_properties.publish_retain_field_id =
        (*env)->GetFieldID(env, mqtt5_publish_packet_properties.publish_packet_class, "retain", "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_retain_field_id);
    mqtt5_publish_packet_properties.publish_topic_field_id =
        (*env)->GetFieldID(env, mqtt5_publish_packet_properties.publish_packet_class, "topic", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_topic_field_id);
    mqtt5_publish_packet_properties.publish_payload_format_field_id = (*env)->GetFieldID(
        env,
        mqtt5_publish_packet_properties.publish_packet_class,
        "payloadFormat",
        "Lsoftware/amazon/awssdk/crt/mqtt5/packets/PublishPacket$PayloadFormatIndicator;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_payload_format_field_id);
    mqtt5_publish_packet_properties.publish_message_expiry_interval_seconds_field_id = (*env)->GetFieldID(
        env, mqtt5_publish_packet_properties.publish_packet_class, "messageExpiryIntervalSeconds", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_message_expiry_interval_seconds_field_id);
    mqtt5_publish_packet_properties.publish_response_topic_field_id = (*env)->GetFieldID(
        env, mqtt5_publish_packet_properties.publish_packet_class, "responseTopic", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_response_topic_field_id);
    mqtt5_publish_packet_properties.publish_correlation_data_field_id =
        (*env)->GetFieldID(env, mqtt5_publish_packet_properties.publish_packet_class, "correlationData", "[B");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_correlation_data_field_id);
    mqtt5_publish_packet_properties.publish_content_type_field_id = (*env)->GetFieldID(
        env, mqtt5_publish_packet_properties.publish_packet_class, "contentType", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_content_type_field_id);
    mqtt5_publish_packet_properties.publish_subscription_identifiers_field_id = (*env)->GetFieldID(
        env, mqtt5_publish_packet_properties.publish_packet_class, "subscriptionIdentifiers", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_subscription_identifiers_field_id);
    mqtt5_publish_packet_properties.publish_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_publish_packet_properties.publish_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_publish_packet_properties.publish_user_properties_field_id);
}

struct java_aws_mqtt5_payload_format_indicator_properties mqtt5_payload_format_indicator_properties;

static void s_cache_mqtt5_payload_format_indicator(JNIEnv *env) {
    jclass cls =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/PublishPacket$PayloadFormatIndicator");
    AWS_FATAL_ASSERT(cls);
    mqtt5_payload_format_indicator_properties.payload_format_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_payload_format_indicator_properties.payload_format_class);
    // Functions
    mqtt5_payload_format_indicator_properties.format_get_value_id =
        (*env)->GetMethodID(env, mqtt5_payload_format_indicator_properties.payload_format_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_payload_format_indicator_properties.format_get_value_id);
    // Static functions
    mqtt5_payload_format_indicator_properties.format_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_payload_format_indicator_properties.payload_format_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/packets/PublishPacket$PayloadFormatIndicator;");
    AWS_FATAL_ASSERT(mqtt5_payload_format_indicator_properties.format_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_negotiated_settings_properties mqtt5_negotiated_settings_properties;

static void s_cache_mqtt5_negotiated_settings(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/NegotiatedSettings");
    AWS_FATAL_ASSERT(cls);
    mqtt5_negotiated_settings_properties.negotiated_settings_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_class);
    // Functions
    mqtt5_negotiated_settings_properties.negotiated_settings_constructor_id =
        (*env)->GetMethodID(env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_constructor_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_native_set_qos_id = (*env)->GetMethodID(
        env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "nativeSetQOS", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_native_set_qos_id);
    // Field IDs
    mqtt5_negotiated_settings_properties.negotiated_settings_maximum_qos_field_id = (*env)->GetFieldID(
        env,
        mqtt5_negotiated_settings_properties.negotiated_settings_class,
        "maximumQOS",
        "Lsoftware/amazon/awssdk/crt/mqtt5/QOS;");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_maximum_qos_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_session_expiry_interval_field_id = (*env)->GetFieldID(
        env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "sessionExpiryInterval", "J");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_session_expiry_interval_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_receive_maximum_from_server_field_id = (*env)->GetFieldID(
        env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "receiveMaximumFromServer", "I");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_receive_maximum_from_server_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_maximum_packet_size_to_server_field_id =
        (*env)->GetFieldID(
            env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "maximumPacketSizeToServer", "J");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_maximum_packet_size_to_server_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_server_keep_alive_field_id =
        (*env)->GetFieldID(env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "serverKeepAlive", "I");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_server_keep_alive_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_retain_available_field_id =
        (*env)->GetFieldID(env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "retainAvailable", "Z");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_retain_available_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_wildcard_subscriptions_available_field_id =
        (*env)->GetFieldID(
            env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "wildcardSubscriptionsAvailable", "Z");
    AWS_FATAL_ASSERT(
        mqtt5_negotiated_settings_properties.negotiated_settings_wildcard_subscriptions_available_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_subscription_identifiers_available_field_id =
        (*env)->GetFieldID(
            env,
            mqtt5_negotiated_settings_properties.negotiated_settings_class,
            "subscriptionIdentifiersAvailable",
            "Z");
    AWS_FATAL_ASSERT(
        mqtt5_negotiated_settings_properties.negotiated_settings_subscription_identifiers_available_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_shared_subscriptions_available_field_id =
        (*env)->GetFieldID(
            env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "sharedSubscriptionsAvailable", "Z");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_shared_subscriptions_available_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_rejoined_session_field_id =
        (*env)->GetFieldID(env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "rejoinedSession", "Z");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_rejoined_session_field_id);
    mqtt5_negotiated_settings_properties.negotiated_settings_assigned_client_id_field_id = (*env)->GetFieldID(
        env, mqtt5_negotiated_settings_properties.negotiated_settings_class, "assignedClientID", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_negotiated_settings_properties.negotiated_settings_assigned_client_id_field_id);
}

struct java_aws_http_proxy_options_properties http_proxy_options_properties;

static void s_cache_http_proxy_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpProxyOptions");
    AWS_FATAL_ASSERT(cls);
    http_proxy_options_properties.http_proxy_options_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(http_proxy_options_properties.http_proxy_options_class);
    // Functions
    http_proxy_options_properties.proxy_get_connection_type_id = (*env)->GetMethodID(
        env,
        http_proxy_options_properties.http_proxy_options_class,
        "getConnectionType",
        "()Lsoftware/amazon/awssdk/crt/http/HttpProxyOptions$HttpProxyConnectionType;");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_connection_type_id);
    http_proxy_options_properties.proxy_get_proxy_host_id = (*env)->GetMethodID(
        env, http_proxy_options_properties.http_proxy_options_class, "getHost", "()Ljava/lang/String;");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_proxy_host_id);
    http_proxy_options_properties.proxy_get_proxy_port_id =
        (*env)->GetMethodID(env, http_proxy_options_properties.http_proxy_options_class, "getPort", "()I");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_proxy_port_id);
    http_proxy_options_properties.proxy_get_proxy_tls_context_id = (*env)->GetMethodID(
        env,
        http_proxy_options_properties.http_proxy_options_class,
        "getTlsContext",
        "()Lsoftware/amazon/awssdk/crt/io/TlsContext;");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_proxy_tls_context_id);
    http_proxy_options_properties.proxy_get_proxy_authorization_type_id = (*env)->GetMethodID(
        env,
        http_proxy_options_properties.http_proxy_options_class,
        "getAuthorizationType",
        "()Lsoftware/amazon/awssdk/crt/http/HttpProxyOptions$HttpProxyAuthorizationType;");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_proxy_authorization_type_id);
    http_proxy_options_properties.proxy_get_authorization_username_id = (*env)->GetMethodID(
        env,
        http_proxy_options_properties.http_proxy_options_class,
        "getAuthorizationUsername",
        "()Ljava/lang/String;");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_authorization_username_id);
    http_proxy_options_properties.proxy_get_authorization_password_id = (*env)->GetMethodID(
        env,
        http_proxy_options_properties.http_proxy_options_class,
        "getAuthorizationPassword",
        "()Ljava/lang/String;");
    AWS_FATAL_ASSERT(http_proxy_options_properties.proxy_get_authorization_password_id);
}

struct java_aws_http_proxy_connection_type_properties http_proxy_connection_type_properties;

static void s_cache_http_proxy_connection_type(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpProxyOptions$HttpProxyConnectionType");
    AWS_FATAL_ASSERT(cls);
    http_proxy_connection_type_properties.http_proxy_connection_type_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(http_proxy_connection_type_properties.http_proxy_connection_type_class);
    // Functions
    http_proxy_connection_type_properties.proxy_get_value_id = (*env)->GetMethodID(
        env, http_proxy_connection_type_properties.http_proxy_connection_type_class, "getValue", "()I");
    AWS_FATAL_ASSERT(http_proxy_connection_type_properties.proxy_get_value_id);
}

struct java_aws_mqtt5_client_options_properties mqtt5_client_options_properties;

static void s_cache_mqtt5_client_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_options_properties.client_options_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.client_options_class);
    // Functions
    mqtt5_client_options_properties.options_get_bootstrap_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getBootstrap",
        "()Lsoftware/amazon/awssdk/crt/io/ClientBootstrap;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_bootstrap_id);
    mqtt5_client_options_properties.options_get_socket_options_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getSocketOptions",
        "()Lsoftware/amazon/awssdk/crt/io/SocketOptions;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_socket_options_id);
    mqtt5_client_options_properties.options_get_tls_options_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getTlsContext",
        "()Lsoftware/amazon/awssdk/crt/io/TlsContext;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_tls_options_id);
    mqtt5_client_options_properties.options_get_session_behavior_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getSessionBehavior",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ClientSessionBehavior;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_session_behavior_id);
    mqtt5_client_options_properties.options_get_extended_validation_and_flow_control_options_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getExtendedValidationAndFlowControlOptions",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ExtendedValidationAndFlowControlOptions;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_extended_validation_and_flow_control_options_id);
    mqtt5_client_options_properties.options_get_offline_queue_behavior_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getOfflineQueueBehavior",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ClientOfflineQueueBehavior;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_offline_queue_behavior_id);
    mqtt5_client_options_properties.options_get_retry_jitter_mode_id = (*env)->GetMethodID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "getRetryJitterMode",
        "()Lsoftware/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions$JitterMode;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_get_retry_jitter_mode_id);
    // Field IDs
    mqtt5_client_options_properties.options_host_name_field_id =
        (*env)->GetFieldID(env, mqtt5_client_options_properties.client_options_class, "hostName", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_host_name_field_id);
    mqtt5_client_options_properties.options_port_field_id =
        (*env)->GetFieldID(env, mqtt5_client_options_properties.client_options_class, "port", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.options_port_field_id);
    mqtt5_client_options_properties.http_proxy_options_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "httpProxyOptions",
        "Lsoftware/amazon/awssdk/crt/http/HttpProxyOptions;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.http_proxy_options_field_id);
    mqtt5_client_options_properties.session_behavior_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "sessionBehavior",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ClientSessionBehavior;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.session_behavior_field_id);
    mqtt5_client_options_properties.extended_validation_and_flow_control_options_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "extendedValidationAndFlowControlOptions",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ExtendedValidationAndFlowControlOptions;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.extended_validation_and_flow_control_options_field_id);
    mqtt5_client_options_properties.offline_queue_behavior_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "offlineQueueBehavior",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ClientOfflineQueueBehavior;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.offline_queue_behavior_field_id);
    mqtt5_client_options_properties.retry_jitter_mode_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "retryJitterMode",
        "Lsoftware/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions$JitterMode;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.retry_jitter_mode_field_id);
    mqtt5_client_options_properties.min_reconnect_delay_ms_field_id = (*env)->GetFieldID(
        env, mqtt5_client_options_properties.client_options_class, "minReconnectDelayMs", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.min_reconnect_delay_ms_field_id);
    mqtt5_client_options_properties.max_reconnect_delay_ms_field_id = (*env)->GetFieldID(
        env, mqtt5_client_options_properties.client_options_class, "maxReconnectDelayMs", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.max_reconnect_delay_ms_field_id);
    mqtt5_client_options_properties.min_connected_time_to_reset_reconnect_delay_ms_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "minConnectedTimeToResetReconnectDelayMs",
        "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.min_connected_time_to_reset_reconnect_delay_ms_field_id);
    mqtt5_client_options_properties.ping_timeout_ms_field_id = (*env)->GetFieldID(
        env, mqtt5_client_options_properties.client_options_class, "pingTimeoutMs", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.ping_timeout_ms_field_id);
    mqtt5_client_options_properties.connack_timeout_ms_field_id = (*env)->GetFieldID(
        env, mqtt5_client_options_properties.client_options_class, "connackTimeoutMs", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.connack_timeout_ms_field_id);
    mqtt5_client_options_properties.ack_timeout_seconds_field_id = (*env)->GetFieldID(
        env, mqtt5_client_options_properties.client_options_class, "ackTimeoutSeconds", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.ack_timeout_seconds_field_id);
    mqtt5_client_options_properties.publish_events_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "publishEvents",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$PublishEvents;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.publish_events_field_id);
    mqtt5_client_options_properties.lifecycle_events_field_id = (*env)->GetFieldID(
        env,
        mqtt5_client_options_properties.client_options_class,
        "lifecycleEvents",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$LifecycleEvents;");
    AWS_FATAL_ASSERT(mqtt5_client_options_properties.lifecycle_events_field_id);
}

struct java_aws_mqtt5_client_properties mqtt5_client_properties;

static void s_cache_mqtt5_client_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5Client");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_properties.client_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_client_properties.client_class);
    // Functions
    mqtt5_client_properties.client_on_websocket_handshake_id = (*env)->GetMethodID(
        env,
        mqtt5_client_properties.client_class,
        "onWebsocketHandshake",
        "(Lsoftware/amazon/awssdk/crt/http/HttpRequest;J)V");
    AWS_FATAL_ASSERT(mqtt5_client_properties.client_on_websocket_handshake_id);

    mqtt5_client_properties.client_set_is_connected =
        (*env)->GetMethodID(env, mqtt5_client_properties.client_class, "setIsConnected", "(Z)V");
    AWS_FATAL_ASSERT(mqtt5_client_properties.client_set_is_connected);
    // Field IDs
    mqtt5_client_properties.websocket_handshake_field_id = (*env)->GetFieldID(
        env, mqtt5_client_properties.client_class, "websocketHandshakeTransform", "Ljava/util/function/Consumer;");
    AWS_FATAL_ASSERT(mqtt5_client_properties.websocket_handshake_field_id);
}

struct java_aws_mqtt5_client_operation_statistics_properties mqtt5_client_operation_statistics_properties;

static void s_cache_mqtt5_client_operation_statistics_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOperationStatistics");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_operation_statistics_properties.statistics_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_client_operation_statistics_properties.statistics_class);
    // Functions
    mqtt5_client_operation_statistics_properties.statistics_constructor_id =
        (*env)->GetMethodID(env, mqtt5_client_operation_statistics_properties.statistics_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_client_operation_statistics_properties.statistics_constructor_id);
    // Field IDs
    mqtt5_client_operation_statistics_properties.incomplete_operation_count_field_id = (*env)->GetFieldID(
        env, mqtt5_client_operation_statistics_properties.statistics_class, "incompleteOperationCount", "J");
    AWS_FATAL_ASSERT(mqtt5_client_operation_statistics_properties.incomplete_operation_count_field_id);
    mqtt5_client_operation_statistics_properties.incomplete_operation_size_field_id = (*env)->GetFieldID(
        env, mqtt5_client_operation_statistics_properties.statistics_class, "incompleteOperationSize", "J");
    AWS_FATAL_ASSERT(mqtt5_client_operation_statistics_properties.incomplete_operation_size_field_id);
    mqtt5_client_operation_statistics_properties.unacked_operation_count_field_id = (*env)->GetFieldID(
        env, mqtt5_client_operation_statistics_properties.statistics_class, "unackedOperationCount", "J");
    AWS_FATAL_ASSERT(mqtt5_client_operation_statistics_properties.unacked_operation_count_field_id);
    mqtt5_client_operation_statistics_properties.unacked_operation_size_field_id = (*env)->GetFieldID(
        env, mqtt5_client_operation_statistics_properties.statistics_class, "unackedOperationSize", "J");
    AWS_FATAL_ASSERT(mqtt5_client_operation_statistics_properties.unacked_operation_size_field_id);
}

struct java_aws_mqtt5_client_session_behavior_type_properties mqtt5_client_session_behavior_properties;

static void s_cache_mqtt5_client_session_behavior(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ClientSessionBehavior");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_session_behavior_properties.mqtt5_client_session_behavior_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_client_session_behavior_properties.mqtt5_client_session_behavior_class);
    // Functions
    mqtt5_client_session_behavior_properties.client_get_value_id = (*env)->GetMethodID(
        env, mqtt5_client_session_behavior_properties.mqtt5_client_session_behavior_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_client_session_behavior_properties.client_get_value_id);
}

struct java_aws_mqtt5_client_extended_validation_and_flow_control_options
    mqtt5_client_extended_validation_and_flow_control_options;

static void s_cache_mqtt5_client_extended_validation_and_flow_control_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(
        env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ExtendedValidationAndFlowControlOptions");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_extended_validation_and_flow_control_options
        .mqtt5_client_extended_validation_and_flow_control_options_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_client_extended_validation_and_flow_control_options
                         .mqtt5_client_extended_validation_and_flow_control_options_class);
    // Functions
    mqtt5_client_extended_validation_and_flow_control_options.client_get_value_id = (*env)->GetMethodID(
        env,
        mqtt5_client_extended_validation_and_flow_control_options
            .mqtt5_client_extended_validation_and_flow_control_options_class,
        "getValue",
        "()I");
    AWS_FATAL_ASSERT(mqtt5_client_extended_validation_and_flow_control_options.client_get_value_id);
}

struct java_aws_mqtt5_client_offline_queue_behavior_type_properties mqtt5_client_offline_queue_behavior_type_properties;

static void s_cache_mqtt5_client_offline_queue_behavior_type(JNIEnv *env) {
    jclass cls =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$ClientOfflineQueueBehavior");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_offline_queue_behavior_type_properties.mqtt5_client_offline_queue_behavior_type_class =
        (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(
        mqtt5_client_offline_queue_behavior_type_properties.mqtt5_client_offline_queue_behavior_type_class);
    // Functions
    mqtt5_client_offline_queue_behavior_type_properties.client_get_value_id = (*env)->GetMethodID(
        env,
        mqtt5_client_offline_queue_behavior_type_properties.mqtt5_client_offline_queue_behavior_type_class,
        "getValue",
        "()I");
    AWS_FATAL_ASSERT(mqtt5_client_offline_queue_behavior_type_properties.client_get_value_id);
}

struct java_aws_mqtt5_client_jitter_mode_properties mqtt5_client_jitter_mode_properties;

static void s_cache_mqtt5_client_jitter_mode(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/ExponentialBackoffRetryOptions$JitterMode");
    AWS_FATAL_ASSERT(cls);
    mqtt5_client_jitter_mode_properties.mqtt5_client_jitter_mode_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_client_jitter_mode_properties.mqtt5_client_jitter_mode_class);
    // Functions
    mqtt5_client_jitter_mode_properties.client_get_value_id =
        (*env)->GetMethodID(env, mqtt5_client_jitter_mode_properties.mqtt5_client_jitter_mode_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_client_jitter_mode_properties.client_get_value_id);
}

struct java_aws_mqtt5_subscribe_packet_properties mqtt5_subscribe_packet_properties;

static void s_cache_mqtt5_subscribe_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/SubscribePacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_subscribe_packet_properties.subscribe_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_subscribe_packet_properties.subscribe_packet_class);
    // Functions
    mqtt5_subscribe_packet_properties.subscribe_subscriptions_field_id = (*env)->GetFieldID(
        env, mqtt5_subscribe_packet_properties.subscribe_packet_class, "subscriptions", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_subscribe_packet_properties.subscribe_subscriptions_field_id);
    mqtt5_subscribe_packet_properties.subscribe_subscription_identifier_field_id = (*env)->GetFieldID(
        env, mqtt5_subscribe_packet_properties.subscribe_packet_class, "subscriptionIdentifier", "Ljava/lang/Long;");
    AWS_FATAL_ASSERT(mqtt5_subscribe_packet_properties.subscribe_subscription_identifier_field_id);
    mqtt5_subscribe_packet_properties.subscribe_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_subscribe_packet_properties.subscribe_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_subscribe_packet_properties.subscribe_user_properties_field_id);
}

struct java_aws_mqtt5_subscription_properties mqtt5_subscription_properties;

static void s_cache_mqtt5_subscribe_subscription(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/SubscribePacket$Subscription");
    AWS_FATAL_ASSERT(cls);
    mqtt5_subscription_properties.subscribe_subscription_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_subscription_class);

    // Field IDs
    mqtt5_subscription_properties.subscribe_no_local_field_id = (*env)->GetFieldID(
        env, mqtt5_subscription_properties.subscribe_subscription_class, "noLocal", "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_no_local_field_id);
    mqtt5_subscription_properties.subscribe_retain_as_published_field_id = (*env)->GetFieldID(
        env, mqtt5_subscription_properties.subscribe_subscription_class, "retainAsPublished", "Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_retain_as_published_field_id);
    // Functions
    mqtt5_subscription_properties.subscribe_get_topic_filter_id = (*env)->GetMethodID(
        env, mqtt5_subscription_properties.subscribe_subscription_class, "getTopicFilter", "()Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_get_topic_filter_id);
    mqtt5_subscription_properties.subscribe_get_qos_id = (*env)->GetMethodID(
        env,
        mqtt5_subscription_properties.subscribe_subscription_class,
        "getQOS",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/QOS;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_get_qos_id);
    mqtt5_subscription_properties.subscribe_get_no_local_id = (*env)->GetMethodID(
        env, mqtt5_subscription_properties.subscribe_subscription_class, "getNoLocal", "()Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_get_no_local_id);
    mqtt5_subscription_properties.subscribe_get_retain_as_published_id = (*env)->GetMethodID(
        env,
        mqtt5_subscription_properties.subscribe_subscription_class,
        "getRetainAsPublished",
        "()Ljava/lang/Boolean;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_get_retain_as_published_id);
    mqtt5_subscription_properties.subscribe_get_retain_handling_type_id = (*env)->GetMethodID(
        env,
        mqtt5_subscription_properties.subscribe_subscription_class,
        "getRetainHandlingType",
        "()Lsoftware/amazon/awssdk/crt/mqtt5/packets/SubscribePacket$RetainHandlingType;");
    AWS_FATAL_ASSERT(mqtt5_subscription_properties.subscribe_get_retain_handling_type_id);
}

struct java_aws_mqtt5_packet_qos_properties mqtt5_packet_qos_properties;

static void s_cache_mqtt5_packet_qos(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/QOS");
    AWS_FATAL_ASSERT(cls);
    mqtt5_packet_qos_properties.packet_qos_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_packet_qos_properties.packet_qos_class);
    // Functions
    mqtt5_packet_qos_properties.qos_get_value_id =
        (*env)->GetMethodID(env, mqtt5_packet_qos_properties.packet_qos_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_packet_qos_properties.qos_get_value_id);
    // Static functions
    mqtt5_packet_qos_properties.qos_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_packet_qos_properties.packet_qos_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/QOS;");
    AWS_FATAL_ASSERT(mqtt5_packet_qos_properties.qos_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_retain_handling_type_properties mqtt5_retain_handling_type_properties;

static void s_cache_mqtt5_retain_handling_type(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/SubscribePacket$RetainHandlingType");
    AWS_FATAL_ASSERT(cls);
    mqtt5_retain_handling_type_properties.retain_handling_type_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_retain_handling_type_properties.retain_handling_type_class);
    // Functions
    mqtt5_retain_handling_type_properties.retain_get_value_id =
        (*env)->GetMethodID(env, mqtt5_retain_handling_type_properties.retain_handling_type_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_retain_handling_type_properties.retain_get_value_id);
}

struct java_aws_mqtt5_suback_reason_code_properties mqtt5_suback_reason_code_properties;

static void s_cache_mqtt5_suback_reason_code(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/SubAckPacket$SubAckReasonCode");
    AWS_FATAL_ASSERT(cls);
    mqtt5_suback_reason_code_properties.reason_code_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_suback_reason_code_properties.reason_code_class);
    // Functions
    mqtt5_suback_reason_code_properties.reason_get_value_id =
        (*env)->GetMethodID(env, mqtt5_suback_reason_code_properties.reason_code_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_suback_reason_code_properties.reason_get_value_id);
    // Static functions
    mqtt5_suback_reason_code_properties.reason_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_suback_reason_code_properties.reason_code_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/packets/SubAckPacket$SubAckReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_suback_reason_code_properties.reason_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_packet_suback_properties mqtt5_suback_packet_properties;

static void s_cache_mqtt5_suback_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/SubAckPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_suback_packet_properties.suback_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_suback_packet_properties.suback_packet_class);
    // Functions
    mqtt5_suback_packet_properties.suback_constructor_id =
        (*env)->GetMethodID(env, mqtt5_suback_packet_properties.suback_packet_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_suback_packet_properties.suback_constructor_id);

    mqtt5_suback_packet_properties.suback_native_add_suback_code_id =
        (*env)->GetMethodID(env, mqtt5_suback_packet_properties.suback_packet_class, "nativeAddSubackCode", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_suback_packet_properties.suback_native_add_suback_code_id);
    // Field IDs
    mqtt5_suback_packet_properties.suback_reason_string_field_id = (*env)->GetFieldID(
        env, mqtt5_suback_packet_properties.suback_packet_class, "reasonString", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_suback_packet_properties.suback_reason_string_field_id);
    mqtt5_suback_packet_properties.suback_reason_codes_field_id =
        (*env)->GetFieldID(env, mqtt5_suback_packet_properties.suback_packet_class, "reasonCodes", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_suback_packet_properties.suback_reason_codes_field_id);
    mqtt5_suback_packet_properties.suback_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_suback_packet_properties.suback_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_suback_packet_properties.suback_user_properties_field_id);
}

struct java_aws_mqtt5_packet_unsubscribe_properties mqtt5_unsubscribe_packet_properties;

static void s_cache_mqtt5_unsubscribe_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/UnsubscribePacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_unsubscribe_packet_properties.unsubscribe_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_unsubscribe_packet_properties.unsubscribe_packet_class);
    // Field IDs
    mqtt5_unsubscribe_packet_properties.unsubscribe_subscriptions_field_id = (*env)->GetFieldID(
        env, mqtt5_unsubscribe_packet_properties.unsubscribe_packet_class, "subscriptions", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_unsubscribe_packet_properties.unsubscribe_subscriptions_field_id);
    mqtt5_unsubscribe_packet_properties.unsubscribe_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_unsubscribe_packet_properties.unsubscribe_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_unsubscribe_packet_properties.unsubscribe_user_properties_field_id);
}

struct java_aws_mqtt5_packet_unsuback_properties mqtt5_unsuback_packet_properties;

static void s_cache_mqtt5_unsuback_packet(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/UnsubAckPacket");
    AWS_FATAL_ASSERT(cls);
    mqtt5_unsuback_packet_properties.unsuback_packet_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_unsuback_packet_properties.unsuback_packet_class);
    // Functions
    mqtt5_unsuback_packet_properties.unsuback_constructor_id =
        (*env)->GetMethodID(env, mqtt5_unsuback_packet_properties.unsuback_packet_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_unsuback_packet_properties.unsuback_constructor_id);

    mqtt5_unsuback_packet_properties.unsuback_native_add_unsuback_code_id = (*env)->GetMethodID(
        env, mqtt5_unsuback_packet_properties.unsuback_packet_class, "nativeAddUnsubackCode", "(I)V");
    AWS_FATAL_ASSERT(mqtt5_unsuback_packet_properties.unsuback_native_add_unsuback_code_id);
    // Field IDs
    mqtt5_unsuback_packet_properties.unsuback_reason_string_field_id = (*env)->GetFieldID(
        env, mqtt5_unsuback_packet_properties.unsuback_packet_class, "reasonString", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_unsuback_packet_properties.unsuback_reason_string_field_id);
    mqtt5_unsuback_packet_properties.unsuback_reason_codes_field_id = (*env)->GetFieldID(
        env, mqtt5_unsuback_packet_properties.unsuback_packet_class, "reasonCodes", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_unsuback_packet_properties.unsuback_reason_codes_field_id);
    mqtt5_unsuback_packet_properties.unsuback_user_properties_field_id = (*env)->GetFieldID(
        env, mqtt5_unsuback_packet_properties.unsuback_packet_class, "userProperties", "Ljava/util/List;");
    AWS_FATAL_ASSERT(mqtt5_unsuback_packet_properties.unsuback_user_properties_field_id);
}

struct java_aws_mqtt5_unsuback_reason_code_properties mqtt5_unsuback_reason_code_properties;

static void s_cache_mqtt5_unsuback_reason_code(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/UnsubAckPacket$UnsubAckReasonCode");
    AWS_FATAL_ASSERT(cls);
    mqtt5_unsuback_reason_code_properties.reason_code_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_unsuback_reason_code_properties.reason_code_class);
    // Functions
    mqtt5_unsuback_reason_code_properties.reason_get_value_id =
        (*env)->GetMethodID(env, mqtt5_unsuback_reason_code_properties.reason_code_class, "getValue", "()I");
    AWS_FATAL_ASSERT(mqtt5_unsuback_reason_code_properties.reason_get_value_id);
    // Static functions
    mqtt5_unsuback_reason_code_properties.reason_s_get_enum_value_from_integer_id = (*env)->GetStaticMethodID(
        env,
        mqtt5_unsuback_reason_code_properties.reason_code_class,
        "getEnumValueFromInteger",
        "(I)Lsoftware/amazon/awssdk/crt/mqtt5/packets/UnsubAckPacket$UnsubAckReasonCode;");
    AWS_FATAL_ASSERT(mqtt5_unsuback_reason_code_properties.reason_s_get_enum_value_from_integer_id);
}

struct java_aws_mqtt5_user_property_properties mqtt5_user_property_properties;

static void s_cache_mqtt5_user_property(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/packets/UserProperty");
    AWS_FATAL_ASSERT(cls);
    mqtt5_user_property_properties.user_property_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_user_property_properties.user_property_class);
    // Functions
    mqtt5_user_property_properties.property_constructor_id = (*env)->GetMethodID(
        env, mqtt5_user_property_properties.user_property_class, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    AWS_FATAL_ASSERT(mqtt5_user_property_properties.property_constructor_id);
    // Field IDs
    mqtt5_user_property_properties.property_key_id =
        (*env)->GetFieldID(env, mqtt5_user_property_properties.user_property_class, "key", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_user_property_properties.property_key_id);
    mqtt5_user_property_properties.property_value_id =
        (*env)->GetFieldID(env, mqtt5_user_property_properties.user_property_class, "value", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(mqtt5_user_property_properties.property_value_id);
}

struct java_aws_mqtt5_publish_events mqtt5_publish_events_properties;

static void s_cache_mqtt5_publish_events_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$PublishEvents");
    AWS_FATAL_ASSERT(cls);
    mqtt5_publish_events_properties.publish_events_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_publish_events_properties.publish_events_class);
    // Functions
    mqtt5_publish_events_properties.publish_events_publish_received_id = (*env)->GetMethodID(
        env,
        mqtt5_publish_events_properties.publish_events_class,
        "onMessageReceived",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/PublishReturn;)V");
    AWS_FATAL_ASSERT(mqtt5_publish_events_properties.publish_events_publish_received_id);
}

struct java_aws_mqtt5_listener_publish_events mqtt5_listener_publish_events_properties;

static void s_cache_mqtt5_listener_publish_events_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ListenerOptions$ListenerPublishEvents");
    AWS_FATAL_ASSERT(cls);
    mqtt5_listener_publish_events_properties.listener_publish_events_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_listener_publish_events_properties.listener_publish_events_class);
    // Functions
    mqtt5_listener_publish_events_properties.listener_publish_events_publish_received_id = (*env)->GetMethodID(
        env,
        mqtt5_listener_publish_events_properties.listener_publish_events_class,
        "onMessageReceived",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/PublishReturn;)Z");
    AWS_FATAL_ASSERT(mqtt5_listener_publish_events_properties.listener_publish_events_publish_received_id);
}

struct java_aws_mqtt5_lifecycle_events mqtt5_lifecycle_events_properties;

static void s_cache_mqtt5_lifecycle_events_properties(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$LifecycleEvents");
    AWS_FATAL_ASSERT(cls);
    mqtt5_lifecycle_events_properties.lifecycle_events_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_lifecycle_events_properties.lifecycle_events_class);
    // Functions
    mqtt5_lifecycle_events_properties.lifecycle_attempting_connect_id = (*env)->GetMethodID(
        env,
        mqtt5_lifecycle_events_properties.lifecycle_events_class,
        "onAttemptingConnect",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/"
        "OnAttemptingConnectReturn;)V");
    AWS_FATAL_ASSERT(mqtt5_lifecycle_events_properties.lifecycle_attempting_connect_id);
    mqtt5_lifecycle_events_properties.lifecycle_connection_success_id = (*env)->GetMethodID(
        env,
        mqtt5_lifecycle_events_properties.lifecycle_events_class,
        "onConnectionSuccess",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/"
        "OnConnectionSuccessReturn;)V");
    AWS_FATAL_ASSERT(mqtt5_lifecycle_events_properties.lifecycle_connection_success_id);
    mqtt5_lifecycle_events_properties.lifecycle_connection_failure_id = (*env)->GetMethodID(
        env,
        mqtt5_lifecycle_events_properties.lifecycle_events_class,
        "onConnectionFailure",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/"
        "OnConnectionFailureReturn;)V");
    AWS_FATAL_ASSERT(mqtt5_lifecycle_events_properties.lifecycle_connection_failure_id);
    mqtt5_lifecycle_events_properties.lifecycle_disconnection_id = (*env)->GetMethodID(
        env,
        mqtt5_lifecycle_events_properties.lifecycle_events_class,
        "onDisconnection",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/OnDisconnectionReturn;)V");
    AWS_FATAL_ASSERT(mqtt5_lifecycle_events_properties.lifecycle_disconnection_id);
    mqtt5_lifecycle_events_properties.lifecycle_stopped_id = (*env)->GetMethodID(
        env,
        mqtt5_lifecycle_events_properties.lifecycle_events_class,
        "onStopped",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5Client;Lsoftware/amazon/awssdk/crt/mqtt5/OnStoppedReturn;)V");
    AWS_FATAL_ASSERT(mqtt5_lifecycle_events_properties.lifecycle_stopped_id);
}

struct java_aws_mqtt5_publish_result_properties mqtt5_publish_result_properties;

static void s_cache_mqtt5_puback_result(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/PublishResult");
    AWS_FATAL_ASSERT(cls);
    mqtt5_publish_result_properties.result_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_publish_result_properties.result_class);
    // Functions
    mqtt5_publish_result_properties.result_constructor_id =
        (*env)->GetMethodID(env, mqtt5_publish_result_properties.result_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_publish_result_properties.result_constructor_id);
    mqtt5_publish_result_properties.result_puback_constructor_id = (*env)->GetMethodID(
        env,
        mqtt5_publish_result_properties.result_class,
        "<init>",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/packets/PubAckPacket;)V");
    AWS_FATAL_ASSERT(mqtt5_publish_result_properties.result_puback_constructor_id);
}

struct java_aws_mqtt5_publish_return_properties mqtt5_publish_return_properties;

static void s_cache_mqtt5_publish_return(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/PublishReturn");
    AWS_FATAL_ASSERT(cls);
    mqtt5_publish_return_properties.return_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_publish_return_properties.return_class);
    // Functions
    mqtt5_publish_return_properties.return_constructor_id = (*env)->GetMethodID(
        env,
        mqtt5_publish_return_properties.return_class,
        "<init>",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/packets/PublishPacket;)V");
    AWS_FATAL_ASSERT(mqtt5_publish_return_properties.return_constructor_id);
}

struct java_aws_mqtt5_on_stopped_return_properties mqtt5_on_stopped_return_properties;

static void s_cache_mqtt5_on_stopped_return(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/OnStoppedReturn");
    AWS_FATAL_ASSERT(cls);
    mqtt5_on_stopped_return_properties.return_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_on_stopped_return_properties.return_class);
    // Functions
    mqtt5_on_stopped_return_properties.return_constructor_id =
        (*env)->GetMethodID(env, mqtt5_on_stopped_return_properties.return_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_on_stopped_return_properties.return_constructor_id);
}

struct java_aws_mqtt5_on_attempting_connect_return_properties mqtt5_on_attempting_connect_return_properties;

static void s_cache_mqtt5_on_attempting_connect_return(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/OnAttemptingConnectReturn");
    AWS_FATAL_ASSERT(cls);
    mqtt5_on_attempting_connect_return_properties.return_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_on_attempting_connect_return_properties.return_class);
    // Functions
    mqtt5_on_attempting_connect_return_properties.return_constructor_id =
        (*env)->GetMethodID(env, mqtt5_on_attempting_connect_return_properties.return_class, "<init>", "()V");
    AWS_FATAL_ASSERT(mqtt5_on_attempting_connect_return_properties.return_constructor_id);
}

struct java_aws_mqtt5_on_connection_success_return_properties mqtt5_on_connection_success_return_properties;

static void s_cache_mqtt5_on_connection_success_return(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/OnConnectionSuccessReturn");
    AWS_FATAL_ASSERT(cls);
    mqtt5_on_connection_success_return_properties.return_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_on_connection_success_return_properties.return_class);
    // Functions
    mqtt5_on_connection_success_return_properties.return_constructor_id = (*env)->GetMethodID(
        env,
        mqtt5_on_connection_success_return_properties.return_class,
        "<init>",
        "(Lsoftware/amazon/awssdk/crt/mqtt5/packets/ConnAckPacket;Lsoftware/amazon/awssdk/crt/mqtt5/"
        "NegotiatedSettings;)V");
    AWS_FATAL_ASSERT(mqtt5_on_connection_success_return_properties.return_constructor_id);
}

struct java_aws_mqtt5_on_connection_failure_return_properties mqtt5_on_connection_failure_return_properties;

static void s_cache_mqtt5_on_connection_failure_return(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/OnConnectionFailureReturn");
    AWS_FATAL_ASSERT(cls);
    mqtt5_on_connection_failure_return_properties.return_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_on_connection_failure_return_properties.return_class);
    // Functions
    mqtt5_on_connection_failure_return_properties.return_constructor_id = (*env)->GetMethodID(
        env,
        mqtt5_on_connection_failure_return_properties.return_class,
        "<init>",
        "(ILsoftware/amazon/awssdk/crt/mqtt5/packets/ConnAckPacket;)V");
    AWS_FATAL_ASSERT(mqtt5_on_connection_failure_return_properties.return_constructor_id);
}

struct java_aws_mqtt5_on_disconnection_return_properties mqtt5_on_disconnection_return_properties;

static void s_cache_mqtt5_on_disconnection_return(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/OnDisconnectionReturn");
    AWS_FATAL_ASSERT(cls);
    mqtt5_on_disconnection_return_properties.return_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_on_disconnection_return_properties.return_class);
    // Functions
    mqtt5_on_disconnection_return_properties.return_constructor_id = (*env)->GetMethodID(
        env,
        mqtt5_on_disconnection_return_properties.return_class,
        "<init>",
        "(ILsoftware/amazon/awssdk/crt/mqtt5/packets/DisconnectPacket;)V");
    AWS_FATAL_ASSERT(mqtt5_on_disconnection_return_properties.return_constructor_id);
}

struct java_aws_mqtt5_listener_options_properties mqtt5_listener_options_properties;

static void s_cache_mqtt5_listener_options(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/mqtt5/Mqtt5ListenerOptions");
    AWS_FATAL_ASSERT(cls);
    mqtt5_listener_options_properties.listener_options_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(mqtt5_listener_options_properties.listener_options_class);
    // Functions
    mqtt5_listener_options_properties.listener_publish_events_field_id = (*env)->GetFieldID(
        env,
        mqtt5_listener_options_properties.listener_options_class,
        "listenerPublishEvents",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ListenerOptions$ListenerPublishEvents;");
    AWS_FATAL_ASSERT(mqtt5_listener_options_properties.listener_publish_events_field_id);
    mqtt5_listener_options_properties.lifecycle_events_field_id = (*env)->GetFieldID(
        env,
        mqtt5_listener_options_properties.listener_options_class,
        "lifecycleEvents",
        "Lsoftware/amazon/awssdk/crt/mqtt5/Mqtt5ClientOptions$LifecycleEvents;");
    AWS_FATAL_ASSERT(mqtt5_listener_options_properties.lifecycle_events_field_id);
}

struct java_boxed_integer_properties boxed_integer_properties;

static void s_cache_boxed_integer(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/lang/Integer");
    AWS_FATAL_ASSERT(cls);
    boxed_integer_properties.integer_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(boxed_integer_properties.integer_class);
    // functions
    boxed_integer_properties.integer_constructor_id =
        (*env)->GetMethodID(env, boxed_integer_properties.integer_class, "<init>", "(I)V");
    AWS_FATAL_ASSERT(boxed_integer_properties.integer_constructor_id);
    boxed_integer_properties.integer_get_value_id =
        (*env)->GetMethodID(env, boxed_integer_properties.integer_class, "intValue", "()I");
    AWS_FATAL_ASSERT(boxed_integer_properties.integer_get_value_id);
}

struct java_boxed_boolean_properties boxed_boolean_properties;

static void s_cache_boxed_boolean(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/lang/Boolean");
    AWS_FATAL_ASSERT(cls);
    boxed_boolean_properties.boolean_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(boxed_boolean_properties.boolean_class);
    // functions
    boxed_boolean_properties.boolean_constructor_id =
        (*env)->GetMethodID(env, boxed_boolean_properties.boolean_class, "<init>", "(Z)V");
    AWS_FATAL_ASSERT(boxed_boolean_properties.boolean_constructor_id);
    boxed_boolean_properties.boolean_get_value_id =
        (*env)->GetMethodID(env, boxed_boolean_properties.boolean_class, "booleanValue", "()Z");
    AWS_FATAL_ASSERT(boxed_boolean_properties.boolean_get_value_id);
}

struct java_boxed_list_properties boxed_list_properties;

static void s_cache_boxed_list(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/util/List");
    AWS_FATAL_ASSERT(cls);
    boxed_list_properties.list_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(boxed_list_properties.list_class);
    // functions
    boxed_list_properties.list_size_id = (*env)->GetMethodID(env, boxed_list_properties.list_class, "size", "()I");
    AWS_FATAL_ASSERT(boxed_list_properties.list_size_id);
    boxed_list_properties.list_get_id =
        (*env)->GetMethodID(env, boxed_list_properties.list_class, "get", "(I)Ljava/lang/Object;");
    AWS_FATAL_ASSERT(boxed_list_properties.list_get_id);
    boxed_list_properties.list_add_id =
        (*env)->GetMethodID(env, boxed_list_properties.list_class, "add", "(Ljava/lang/Object;)Z");
    AWS_FATAL_ASSERT(boxed_list_properties.list_add_id);
}

struct java_boxed_array_list_properties boxed_array_list_properties;

static void s_cache_boxed_array_list(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/util/ArrayList");
    AWS_FATAL_ASSERT(cls);
    boxed_array_list_properties.list_class = (*env)->NewGlobalRef(env, cls);
    AWS_FATAL_ASSERT(boxed_array_list_properties.list_class);
    // functions
    boxed_array_list_properties.list_constructor_id =
        (*env)->GetMethodID(env, boxed_array_list_properties.list_class, "<init>", "()V");
    AWS_FATAL_ASSERT(boxed_array_list_properties.list_constructor_id);
}

void cache_java_class_ids(JNIEnv *env) {
    s_cache_http_request_body_stream(env);
    s_cache_aws_signing_config(env);
    s_cache_predicate(env);
    s_cache_boxed_long(env);
    s_cache_http_request(env);
    s_cache_crt_resource(env);
    s_cache_mqtt_connection(env);
    s_cache_message_handler(env);
    s_cache_mqtt_exception(env);
    s_cache_mqtt_client_connection_operation_statistics(env);
    s_cache_byte_buffer(env);
    s_cache_credentials_provider(env);
    s_cache_credentials(env);
    s_cache_credentials_handler(env);
    s_cache_async_callback(env);
    s_cache_event_loop_group(env);
    s_cache_client_bootstrap(env);
    s_cache_tls_context_pkcs11_options(env);
    s_cache_tls_key_operation(env);
    s_cache_tls_context_custom_key_operation_options(env);
    s_cache_tls_key_operation_handler(env);
    s_cache_http_client_connection_manager(env);
    s_cache_http2_stream_manager(env);
    s_cache_http_client_connection(env);
    s_cache_http_stream(env);
    s_cache_http2_stream(env);
    s_cache_http_stream_response_handler_native_adapter(env);
    s_cache_http_stream_write_chunk_completion_properties(env);
    s_cache_event_stream_server_listener_properties(env);
    s_cache_event_stream_server_listener_handler_properties(env);
    s_cache_event_stream_server_connection_handler_properties(env);
    s_cache_event_stream_server_continuation_handler_properties(env);
    s_cache_event_stream_client_connection_handler_properties(env);
    s_cache_event_stream_client_continuation_handler_properties(env);
    s_cache_event_stream_message_flush_properties(env);
    s_cache_cpu_info_properties(env);
    s_cache_s3_client_properties(env);
    s_cache_s3_meta_request_properties(env);
    s_cache_s3_meta_request_response_handler_native_adapter_properties(env);
    s_cache_completable_future(env);
    s_cache_crt_runtime_exception(env);
    s_cache_ecc_key_pair(env);
    s_cache_crt(env);
    s_cache_aws_signing_result(env);
    s_cache_http_header(env);
    s_cache_http_manager_metrics(env);
    s_cache_exponential_backoff_retry_options(env);
    s_cache_standard_retry_options(env);
    s_cache_directory_traversal_handler(env);
    s_cache_directory_entry(env);
    s_cache_s3_tcp_keep_alive_options(env);
    s_cache_s3_meta_request_progress(env);
    s_cache_s3_meta_request_resume_token(env);
    s_cache_mqtt5_connack_packet(env);
    s_cache_mqtt5_connect_packet(env);
    s_cache_mqtt5_connect_reason_code(env);
    s_cache_mqtt5_disconnect_packet(env);
    s_cache_mqtt5_disconnect_reason_code(env);
    s_cache_mqtt5_puback_packet(env);
    s_cache_mqtt5_puback_reason_code(env);
    s_cache_mqtt5_publish_packet(env);
    s_cache_mqtt5_payload_format_indicator(env);
    s_cache_mqtt5_negotiated_settings(env);
    s_cache_http_proxy_options(env);
    s_cache_http_proxy_connection_type(env);
    s_cache_mqtt5_client_options(env);
    s_cache_mqtt5_client_properties(env);
    s_cache_mqtt5_client_operation_statistics_properties(env);
    s_cache_mqtt5_client_session_behavior(env);
    s_cache_mqtt5_client_extended_validation_and_flow_control_options(env);
    s_cache_mqtt5_client_offline_queue_behavior_type(env);
    s_cache_mqtt5_client_jitter_mode(env);
    s_cache_mqtt5_subscribe_packet(env);
    s_cache_mqtt5_subscribe_subscription(env);
    s_cache_mqtt5_packet_qos(env);
    s_cache_mqtt5_retain_handling_type(env);
    s_cache_mqtt5_suback_reason_code(env);
    s_cache_mqtt5_suback_packet(env);
    s_cache_mqtt5_unsubscribe_packet(env);
    s_cache_mqtt5_unsuback_packet(env);
    s_cache_mqtt5_unsuback_reason_code(env);
    s_cache_mqtt5_user_property(env);
    s_cache_mqtt5_publish_events_properties(env);
    s_cache_mqtt5_listener_publish_events_properties(env);
    s_cache_mqtt5_lifecycle_events_properties(env);
    s_cache_mqtt5_puback_result(env);
    s_cache_mqtt5_publish_return(env);
    s_cache_mqtt5_on_stopped_return(env);
    s_cache_mqtt5_on_attempting_connect_return(env);
    s_cache_mqtt5_on_connection_success_return(env);
    s_cache_mqtt5_on_connection_failure_return(env);
    s_cache_mqtt5_on_disconnection_return(env);
    s_cache_mqtt5_listener_options(env);
    s_cache_boxed_integer(env);
    s_cache_boxed_boolean(env);
    s_cache_boxed_list(env);
    s_cache_boxed_array_list(env);
}

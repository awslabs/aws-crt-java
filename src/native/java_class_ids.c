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

struct java_http_client_connection_manager_properties http_client_connection_manager_properties;

static void s_cache_http_client_connection_manager(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpClientConnectionManager");
    AWS_FATAL_ASSERT(cls);

    http_client_connection_manager_properties.onShutdownComplete =
        (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(http_client_connection_manager_properties.onShutdownComplete);
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
        env, cls, "onResponseHeaders", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;IILjava/nio/ByteBuffer;)V");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseHeaders);

    http_stream_response_handler_properties.onResponseHeadersDone =
        (*env)->GetMethodID(env, cls, "onResponseHeadersDone", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;I)V");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseHeadersDone);

    http_stream_response_handler_properties.onResponseBody = (*env)->GetMethodID(
        env, cls, "onResponseBody", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;Ljava/nio/ByteBuffer;)I");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseBody);

    http_stream_response_handler_properties.onResponseComplete =
        (*env)->GetMethodID(env, cls, "onResponseComplete", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;I)V");
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
        (*env)->GetMethodID(env, cls, "onFinished", "(II[B)V");
    AWS_FATAL_ASSERT(s3_meta_request_response_handler_native_adapter_properties.onFinished);

    s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders =
        (*env)->GetMethodID(env, cls, "onResponseHeaders", "(ILjava/nio/ByteBuffer;)V");
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
    s_cache_byte_buffer(env);
    s_cache_credentials_provider(env);
    s_cache_credentials(env);
    s_cache_credentials_handler(env);
    s_cache_async_callback(env);
    s_cache_event_loop_group(env);
    s_cache_client_bootstrap(env);
    s_cache_tls_context_pkcs11_options(env);
    s_cache_http_client_connection_manager(env);
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
    s_cache_exponential_backoff_retry_options(env);
    s_cache_standard_retry_options(env);
    s_cache_directory_traversal_handler(env);
    s_cache_directory_entry(env);
}

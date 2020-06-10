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
        (*env)->GetFieldID(env, aws_signing_config_class, "signedBodyValue", "I");
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

    message_handler_properties.deliver = (*env)->GetMethodID(env, cls, "deliver", "(Ljava/lang/String;[B)V");
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

struct java_async_callback_properties async_callback_properties;

static void s_cache_async_callback(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/AsyncCallback");
    AWS_FATAL_ASSERT(cls);

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

struct java_http_client_connection_manager_properties http_client_connection_manager_properties;

static void s_cache_http_client_connection_manager(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpClientConnectionManager");
    AWS_FATAL_ASSERT(cls);

    http_client_connection_manager_properties.onConnectionAcquired =
        (*env)->GetMethodID(env, cls, "onConnectionAcquired", "(JI)V");
    AWS_FATAL_ASSERT(http_client_connection_manager_properties.onConnectionAcquired);

    http_client_connection_manager_properties.onShutdownComplete =
        (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(http_client_connection_manager_properties.onShutdownComplete);
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

    http_stream_response_handler_properties.onResponseBody =
        (*env)->GetMethodID(env, cls, "onResponseBody", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;[B)I");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseBody);

    http_stream_response_handler_properties.onResponseComplete =
        (*env)->GetMethodID(env, cls, "onResponseComplete", "(Lsoftware/amazon/awssdk/crt/http/HttpStream;I)V");
    AWS_FATAL_ASSERT(http_stream_response_handler_properties.onResponseComplete);
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

    crt_runtime_exception_properties.constructor_method_id =
        (*env)->GetMethodID(env, cls, "<init>", "(ILjava/lang/String;)V");
    AWS_FATAL_ASSERT(crt_runtime_exception_properties.constructor_method_id);
}

void cache_java_class_ids(JNIEnv *env) {
    s_cache_http_request_body_stream(env);
    s_cache_aws_signing_config(env);
    s_cache_predicate(env);
    s_cache_http_request(env);
    s_cache_crt_resource(env);
    s_cache_mqtt_connection(env);
    s_cache_message_handler(env);
    s_cache_mqtt_exception(env);
    s_cache_byte_buffer(env);
    s_cache_credentials_provider(env);
    s_cache_credentials(env);
    s_cache_async_callback(env);
    s_cache_event_loop_group(env);
    s_cache_client_bootstrap(env);
    s_cache_http_client_connection_manager(env);
    s_cache_http_stream(env);
    s_cache_http_stream_response_handler_native_adapter(env);
    s_cache_completable_future(env);
    s_cache_crt_runtime_exception(env);
}

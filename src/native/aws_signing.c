/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"

#include "credentials.h"
#include "http_request_utils.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/auth/credentials.h>
#include <aws/auth/signable.h>
#include <aws/auth/signing.h>
#include <aws/auth/signing_result.h>
#include <aws/common/string.h>
#include <aws/http/request_response.h>
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

struct s_aws_sign_request_callback_data {
    JavaVM *jvm;
    jobject java_future;
    jobject java_original_request;
    jobject java_original_chunk_body;
    jobject java_sign_header_predicate;
    struct aws_input_stream *chunk_body_stream;
    struct aws_http_message *native_request;
    struct aws_signable *original_message_signable;
    struct aws_string *region;
    struct aws_string *service;
    struct aws_string *signed_body_value;
    struct aws_string *previous_chunk_signature;
    struct aws_credentials *credentials;
};

static void s_cleanup_callback_data(struct s_aws_sign_request_callback_data *callback_data) {

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    (*env)->DeleteGlobalRef(env, callback_data->java_future);

    if (callback_data->java_original_request != NULL) {
        (*env)->DeleteGlobalRef(env, callback_data->java_original_request);
    }

    if (callback_data->java_original_chunk_body != NULL) {
        (*env)->DeleteGlobalRef(env, callback_data->java_original_chunk_body);
    }

    if (callback_data->java_sign_header_predicate) {
        (*env)->DeleteGlobalRef(env, callback_data->java_sign_header_predicate);
    }

    if (callback_data->native_request) {
        aws_http_message_release(callback_data->native_request);
    }

    if (callback_data->original_message_signable) {
        aws_signable_destroy(callback_data->original_message_signable);
    }

    if (callback_data->credentials) {
        aws_credentials_release(callback_data->credentials);
    }

    if (callback_data->chunk_body_stream != NULL) {
        aws_input_stream_destroy(callback_data->chunk_body_stream);
    }

    aws_string_destroy(callback_data->region);
    aws_string_destroy(callback_data->service);
    aws_string_destroy(callback_data->signed_body_value);
    aws_string_destroy(callback_data->previous_chunk_signature);

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static jobject s_create_signed_java_http_request(
    JNIEnv *env,
    struct aws_http_message *native_request,
    jobject java_original_request) {
    jobject jni_body_stream =
        (*env)->GetObjectField(env, java_original_request, http_request_properties.body_stream_field_id);

    jobject http_request = aws_java_http_request_from_native(env, native_request, jni_body_stream);

    if (jni_body_stream != NULL) {
        (*env)->DeleteLocalRef(env, jni_body_stream);
    }

    return http_request;
}

static void s_complete_signing_exceptionally(
    JNIEnv *env,
    struct s_aws_sign_request_callback_data *callback_data,
    int error_code) {

    jint jni_error_code = error_code;
    struct aws_byte_cursor error_cursor = aws_byte_cursor_from_c_str(aws_error_name(error_code));
    jstring jni_error_string = aws_jni_string_from_cursor(env, &error_cursor);
    AWS_FATAL_ASSERT(jni_error_string);

    jobject crt_exception = (*env)->NewObject(
        env,
        crt_runtime_exception_properties.crt_runtime_exception_class,
        crt_runtime_exception_properties.constructor_method_id,
        jni_error_code,
        jni_error_string);
    AWS_FATAL_ASSERT(crt_exception);

    (*env)->CallBooleanMethod(
        env, callback_data->java_future, completable_future_properties.complete_exceptionally_method_id, crt_exception);

    (*env)->ExceptionCheck(env);
    (*env)->DeleteLocalRef(env, jni_error_string);
    (*env)->DeleteLocalRef(env, crt_exception);
}

static void s_aws_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {

    struct s_aws_sign_request_callback_data *callback_data = userdata;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    if (result == NULL || error_code != AWS_ERROR_SUCCESS) {
        s_complete_signing_exceptionally(
            env, callback_data, (error_code != AWS_ERROR_SUCCESS) ? error_code : AWS_ERROR_UNKNOWN);
        goto done;
    }

    if (aws_apply_signing_result_to_http_request(callback_data->native_request, aws_jni_get_allocator(), result)) {
        s_complete_signing_exceptionally(env, callback_data, aws_last_error());
        goto done;
    }

    jobject java_signed_request =
        s_create_signed_java_http_request(env, callback_data->native_request, callback_data->java_original_request);

    if (java_signed_request == NULL) {
        s_complete_signing_exceptionally(env, callback_data, aws_last_error());
        goto done;
    }

    (*env)->CallBooleanMethod(
        env, callback_data->java_future, completable_future_properties.complete_method_id, java_signed_request);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    (*env)->DeleteLocalRef(env, java_signed_request);

    /* I have no idea what we should do here... but the JVM really doesn't like us NOT calling this function after
       we cross the barrier. */
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

done:

    s_cleanup_callback_data(callback_data);
}

static void s_aws_chunk_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {

    struct s_aws_sign_request_callback_data *callback_data = userdata;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    if (result == NULL || error_code != AWS_ERROR_SUCCESS) {
        s_complete_signing_exceptionally(
            env, callback_data, (error_code != AWS_ERROR_SUCCESS) ? error_code : AWS_ERROR_UNKNOWN);
        goto done;
    }

    struct aws_string *signature = NULL;
    aws_signing_result_get_property(result, g_aws_signature_property_name, &signature);

    struct aws_byte_cursor signature_cursor = aws_byte_cursor_from_string(signature);
    jstring java_chunk_signature = aws_jni_string_from_cursor(env, &signature_cursor);

    (*env)->CallBooleanMethod(
        env, callback_data->java_future, completable_future_properties.complete_method_id, java_chunk_signature);

    (*env)->DeleteLocalRef(env, java_chunk_signature);

    /* I have no idea what we should do here... but the JVM really doesn't like us NOT calling this function after
       we cross the barrier. */
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

done:

    s_cleanup_callback_data(callback_data);
}

static bool s_should_sign_header(const struct aws_byte_cursor *name, void *user_data) {
    struct s_aws_sign_request_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jstring header_name = aws_jni_string_from_cursor(env, name);

    bool result = (*env)->CallBooleanMethod(
        env, callback_data->java_sign_header_predicate, predicate_properties.test_method_id, (jobject)header_name);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    (*env)->DeleteLocalRef(env, header_name);

    return result;
}

static int s_build_signing_config(
    JNIEnv *env,
    struct s_aws_sign_request_callback_data *callback_data,
    jobject java_config,
    struct aws_signing_config_aws *config) {
    (void)callback_data;
    (void)java_config;
    (void)config;

    config->config_type = AWS_SIGNING_CONFIG_AWS;
    config->algorithm = (enum aws_signing_algorithm)(*env)->GetIntField(
        env, java_config, aws_signing_config_properties.algorithm_field_id);
    config->signature_type = (enum aws_signature_type)(*env)->GetIntField(
        env, java_config, aws_signing_config_properties.signature_type_field_id);

    jstring region = (jstring)(*env)->GetObjectField(env, java_config, aws_signing_config_properties.region_field_id);
    callback_data->region = aws_jni_new_string_from_jstring(env, region);
    config->region = aws_byte_cursor_from_string(callback_data->region);

    jstring service = (jstring)(*env)->GetObjectField(env, java_config, aws_signing_config_properties.service_field_id);
    callback_data->service = aws_jni_new_string_from_jstring(env, service);
    config->service = aws_byte_cursor_from_string(callback_data->service);

    int64_t epoch_time_millis = (*env)->GetLongField(env, java_config, aws_signing_config_properties.time_field_id);
    aws_date_time_init_epoch_millis(&config->date, (uint64_t)epoch_time_millis);

    jobject sign_header_predicate =
        (*env)->GetObjectField(env, java_config, aws_signing_config_properties.should_sign_header_field_id);
    if (sign_header_predicate != NULL) {
        callback_data->java_sign_header_predicate = (*env)->NewGlobalRef(env, sign_header_predicate);
        AWS_FATAL_ASSERT(callback_data->java_sign_header_predicate != NULL);

        config->should_sign_header = s_should_sign_header;
        config->should_sign_header_ud = callback_data;
    }

    config->flags.use_double_uri_encode =
        (*env)->GetBooleanField(env, java_config, aws_signing_config_properties.use_double_uri_encode_field_id);
    config->flags.should_normalize_uri_path =
        (*env)->GetBooleanField(env, java_config, aws_signing_config_properties.should_normalize_uri_path_field_id);
    config->flags.omit_session_token =
        (*env)->GetBooleanField(env, java_config, aws_signing_config_properties.omit_session_token_field_id);

    jstring signed_body_value =
        (jstring)(*env)->GetObjectField(env, java_config, aws_signing_config_properties.signed_body_value_field_id);
    if (signed_body_value == NULL) {
        AWS_ZERO_STRUCT(config->signed_body_value);
    } else {
        callback_data->signed_body_value = aws_jni_new_string_from_jstring(env, signed_body_value);
        config->signed_body_value = aws_byte_cursor_from_string(callback_data->signed_body_value);
    }

    config->signed_body_header =
        (*env)->GetIntField(env, java_config, aws_signing_config_properties.signed_body_header_field_id);

    jobject provider =
        (*env)->GetObjectField(env, java_config, aws_signing_config_properties.credentials_provider_field_id);
    if (provider != NULL) {
        config->credentials_provider =
            (void *)(*env)->CallLongMethod(env, provider, crt_resource_properties.get_native_handle_method_id);
        if ((*env)->ExceptionCheck(env)) {
            return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
        }
    }

    jobject credentials = (*env)->GetObjectField(env, java_config, aws_signing_config_properties.credentials_field_id);
    if (credentials != NULL) {
        callback_data->credentials = aws_credentials_new_from_java_credentials(env, credentials);
        config->credentials = callback_data->credentials;
    }

    config->expiration_in_seconds =
        (uint64_t)(*env)->GetLongField(env, java_config, aws_signing_config_properties.expiration_in_seconds_field_id);

    if ((*env)->ExceptionCheck(env)) {
        return aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
    }

    return AWS_OP_SUCCESS;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigner_awsSignerSignRequest(
    JNIEnv *env,
    jclass jni_class,
    jobject java_http_request,
    jbyteArray marshalled_request,
    jobject java_signing_config,
    jobject java_future) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct s_aws_sign_request_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s_aws_sign_request_callback_data));
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to allocated sign request callback data");
        return;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_future = (*env)->NewGlobalRef(env, java_future);
    AWS_FATAL_ASSERT(callback_data->java_future != NULL);

    callback_data->java_original_request = (*env)->NewGlobalRef(env, java_http_request);
    AWS_FATAL_ASSERT(callback_data->java_original_request != NULL);

    /* Build a native aws_signing_config_aws object */
    struct aws_signing_config_aws signing_config;
    AWS_ZERO_STRUCT(signing_config);

    if (s_build_signing_config(env, callback_data, java_signing_config, &signing_config)) {
        aws_jni_throw_runtime_exception(env, "Failed to create signing configuration");
        goto on_error;
    }

    jobject java_http_request_body_stream =
        (*env)->GetObjectField(env, java_http_request, http_request_properties.body_stream_field_id);

    callback_data->native_request =
        aws_http_request_new_from_java_http_request(env, marshalled_request, java_http_request_body_stream);
    if (callback_data->native_request == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create native http request from Java HttpRequest");
        goto on_error;
    }

    callback_data->original_message_signable = aws_signable_new_http_request(allocator, callback_data->native_request);
    if (callback_data->original_message_signable == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create signable from http request");
        goto on_error;
    }

    /* Sign the native request */
    if (aws_sign_request_aws(
            allocator,
            callback_data->original_message_signable,
            (struct aws_signing_config_base *)&signing_config,
            s_aws_signing_complete,
            callback_data)) {
        aws_jni_throw_runtime_exception(env, "Failed to initiate signing process for HttpRequest");
        goto on_error;
    }

    return;

on_error:

    s_cleanup_callback_data(callback_data);

    (*env)->ExceptionClear(env);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigner_awsSignerSignChunk(
    JNIEnv *env,
    jclass jni_class,
    jobject java_chunk_body_stream,
    jstring previous_signature,
    jobject java_signing_config,
    jobject java_future) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct s_aws_sign_request_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s_aws_sign_request_callback_data));
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to allocate chunk signing callback data");
        return;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_future = (*env)->NewGlobalRef(env, java_future);
    AWS_FATAL_ASSERT(callback_data->java_future != NULL);

    if (java_chunk_body_stream != NULL) {
        callback_data->java_original_chunk_body = (*env)->NewGlobalRef(env, java_chunk_body_stream);
        AWS_FATAL_ASSERT(callback_data->java_original_chunk_body != NULL);

        callback_data->chunk_body_stream = aws_input_stream_new_from_java_http_request_body_stream(
            aws_jni_get_allocator(), env, java_chunk_body_stream);
        if (callback_data->chunk_body_stream == NULL) {
            aws_jni_throw_runtime_exception(env, "Error building chunk body stream");
            goto on_error;
        }
    }

    /* Build a native aws_signing_config_aws object */
    struct aws_signing_config_aws signing_config;
    AWS_ZERO_STRUCT(signing_config);

    if (s_build_signing_config(env, callback_data, java_signing_config, &signing_config)) {
        aws_jni_throw_runtime_exception(env, "Failed to create signing configuration");
        goto on_error;
    }

    callback_data->previous_chunk_signature = aws_jni_new_string_from_jstring(env, previous_signature);
    struct aws_byte_cursor previous_signature_cursor =
        aws_byte_cursor_from_string(callback_data->previous_chunk_signature);

    callback_data->original_message_signable =
        aws_signable_new_chunk(allocator, callback_data->chunk_body_stream, previous_signature_cursor);
    if (callback_data->original_message_signable == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create signable from chunk data");
        goto on_error;
    }

    /* Sign the native request */
    if (aws_sign_request_aws(
            allocator,
            callback_data->original_message_signable,
            (struct aws_signing_config_base *)&signing_config,
            s_aws_chunk_signing_complete,
            callback_data)) {
        aws_jni_throw_runtime_exception(env, "Failed to initiate signing process for Chunk");
        goto on_error;
    }

    return;

on_error:

    s_cleanup_callback_data(callback_data);

    (*env)->ExceptionClear(env);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

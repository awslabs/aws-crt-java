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
#include <aws/cal/ecc.h>
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
    jobject java_signing_result_future;
    jobject java_original_request;
    jobject java_original_chunk_body;
    jobject java_sign_header_predicate;
    jbyteArray java_previous_signature;
    struct aws_http_headers *trailing_headers;
    struct aws_input_stream *chunk_body_stream;
    struct aws_http_message *native_request;
    struct aws_signable *original_message_signable;
    struct aws_string *region;
    struct aws_string *service;
    struct aws_string *signed_body_value;
    struct aws_byte_cursor previous_signature;
    struct aws_credentials *credentials;
};

static void s_cleanup_callback_data(struct s_aws_sign_request_callback_data *callback_data) {

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env != NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    (*env)->DeleteGlobalRef(env, callback_data->java_signing_result_future);

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
        struct aws_input_stream *input_stream = aws_http_message_get_body_stream(callback_data->native_request);
        if (input_stream != NULL) {
            aws_input_stream_destroy(input_stream);
        }
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
    if (callback_data->trailing_headers != NULL) {
        aws_http_headers_release(callback_data->trailing_headers);
    }
    aws_string_destroy(callback_data->region);
    aws_string_destroy(callback_data->service);
    aws_string_destroy(callback_data->signed_body_value);

    if (callback_data->previous_signature.len > 0 && callback_data->java_previous_signature != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(
            env, callback_data->java_previous_signature, callback_data->previous_signature);
    }

    if (callback_data->java_previous_signature != NULL) {
        (*env)->DeleteGlobalRef(env, callback_data->java_previous_signature);
    }

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

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

    if (error_code == AWS_ERROR_SUCCESS) {
        error_code = AWS_ERROR_UNKNOWN;
    }

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
        env,
        callback_data->java_signing_result_future,
        completable_future_properties.complete_exceptionally_method_id,
        crt_exception);

    aws_jni_check_and_clear_exception(env);
    (*env)->DeleteLocalRef(env, jni_error_string);
    (*env)->DeleteLocalRef(env, crt_exception);
}

static void s_aws_complete_signing_result(
    JNIEnv *env,
    struct aws_signing_result *result,
    struct s_aws_sign_request_callback_data *callback_data,
    jobject java_signed_request) {
    jbyteArray java_signature = NULL;
    jobject java_signing_result = NULL;

    struct aws_string *signature = NULL;
    aws_signing_result_get_property(result, g_aws_signature_property_name, &signature);

    struct aws_byte_cursor signature_cursor = aws_byte_cursor_from_string(signature);
    java_signature = aws_jni_byte_array_from_cursor(env, &signature_cursor);

    java_signing_result = (*env)->NewObject(
        env, aws_signing_result_properties.aws_signing_result_class, aws_signing_result_properties.constructor);
    if ((*env)->ExceptionCheck(env) || java_signing_result == NULL) {
        s_complete_signing_exceptionally(env, callback_data, AWS_ERROR_UNKNOWN);
        goto done;
    }

    (*env)->SetObjectField(
        env, java_signing_result, aws_signing_result_properties.signed_request_field_id, java_signed_request);
    (*env)->SetObjectField(env, java_signing_result, aws_signing_result_properties.signature_field_id, java_signature);

    (*env)->CallBooleanMethod(
        env,
        callback_data->java_signing_result_future,
        completable_future_properties.complete_method_id,
        java_signing_result);

    /* I have no idea what we should do here... but the JVM really doesn't like us NOT calling this function after
       we cross the barrier. */
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));

done:

    if (java_signature != NULL) {
        (*env)->DeleteLocalRef(env, java_signature);
    }

    if (java_signing_result != NULL) {
        (*env)->DeleteLocalRef(env, java_signing_result);
    }

    if (java_signed_request != NULL) {
        (*env)->DeleteLocalRef(env, java_signed_request);
    }
}

static void s_aws_request_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {

    struct s_aws_sign_request_callback_data *callback_data = userdata;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    if (result == NULL || error_code != AWS_ERROR_SUCCESS) {
        s_complete_signing_exceptionally(env, callback_data, error_code);
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

    s_aws_complete_signing_result(env, result, callback_data, java_signed_request);

done:

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

    s_cleanup_callback_data(callback_data);
}

static void s_aws_chunk_like_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {

    struct s_aws_sign_request_callback_data *callback_data = userdata;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    if (result == NULL || error_code != AWS_ERROR_SUCCESS) {
        s_complete_signing_exceptionally(env, callback_data, error_code);
        goto done;
    }

    s_aws_complete_signing_result(env, result, callback_data, NULL);

done:

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

    s_cleanup_callback_data(callback_data);
}

static void s_aws_chunk_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {
    s_aws_chunk_like_signing_complete(result, error_code, userdata);
}

static void s_aws_trailing_headers_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {
    s_aws_chunk_like_signing_complete(result, error_code, userdata);
}

static bool s_should_sign_header(const struct aws_byte_cursor *name, void *user_data) {
    struct s_aws_sign_request_callback_data *callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return false;
    }

    jstring header_name = aws_jni_string_from_cursor(env, name);

    bool result = (*env)->CallBooleanMethod(
        env, callback_data->java_sign_header_predicate, predicate_properties.test_method_id, (jobject)header_name);
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));

    (*env)->DeleteLocalRef(env, header_name);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

    return result;
}

static int s_build_signing_config(
    JNIEnv *env,
    struct s_aws_sign_request_callback_data *callback_data,
    jobject java_config,
    struct aws_signing_config_aws *config) {

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
        aws_jni_check_and_clear_exception(env);
    }

    jobject credentials = (*env)->GetObjectField(env, java_config, aws_signing_config_properties.credentials_field_id);
    if (credentials != NULL) {
        callback_data->credentials = aws_credentials_new_from_java_credentials(env, credentials);
        config->credentials = callback_data->credentials;
    }

    config->expiration_in_seconds =
        (uint64_t)(*env)->GetLongField(env, java_config, aws_signing_config_properties.expiration_in_seconds_field_id);

    if (aws_jni_check_and_clear_exception(env)) {
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
    jobject java_signing_result_future) {

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

    callback_data->java_signing_result_future = (*env)->NewGlobalRef(env, java_signing_result_future);
    AWS_FATAL_ASSERT(callback_data->java_signing_result_future != NULL);

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
            s_aws_request_signing_complete,
            callback_data)) {
        aws_jni_throw_runtime_exception(env, "Failed to initiate signing process for HttpRequest");
        goto on_error;
    }

    return;

on_error:

    s_cleanup_callback_data(callback_data);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigner_awsSignerSignChunk(
    JNIEnv *env,
    jclass jni_class,
    jobject java_chunk_body_stream,
    jbyteArray java_previous_signature,
    jobject java_signing_config,
    jobject java_signing_result_future) {

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

    callback_data->java_signing_result_future = (*env)->NewGlobalRef(env, java_signing_result_future);
    AWS_FATAL_ASSERT(callback_data->java_signing_result_future != NULL);

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

    callback_data->java_previous_signature = (*env)->NewGlobalRef(env, java_previous_signature);
    callback_data->previous_signature = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_previous_signature);

    callback_data->original_message_signable =
        aws_signable_new_chunk(allocator, callback_data->chunk_body_stream, callback_data->previous_signature);
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
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigner_awsSignerSignTrailingHeaders(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray marshalled_headers,
    jbyteArray java_previous_signature,
    jobject java_signing_config,
    jobject java_signing_result_future) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct s_aws_sign_request_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s_aws_sign_request_callback_data));
    /* we no longer worry about allocation failures */

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_signing_result_future = (*env)->NewGlobalRef(env, java_signing_result_future);
    AWS_FATAL_ASSERT(callback_data->java_signing_result_future != NULL);

    callback_data->trailing_headers = aws_http_headers_new_from_java_http_headers(env, marshalled_headers);
    if (callback_data->trailing_headers == NULL) {
        goto on_error;
    }

    /* Build a native aws_signing_config_aws object */
    struct aws_signing_config_aws signing_config;
    AWS_ZERO_STRUCT(signing_config);

    if (s_build_signing_config(env, callback_data, java_signing_config, &signing_config)) {
        aws_jni_throw_runtime_exception(env, "Failed to create signing configuration");
        goto on_error;
    }

    callback_data->java_previous_signature = (*env)->NewGlobalRef(env, java_previous_signature);
    callback_data->previous_signature = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_previous_signature);

    callback_data->original_message_signable = aws_signable_new_trailing_headers(
        allocator, callback_data->trailing_headers, callback_data->previous_signature);
    if (callback_data->original_message_signable == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create signable from trailing headers data");
        goto on_error;
    }

    /* Sign the native request */
    if (aws_sign_request_aws(
            allocator,
            callback_data->original_message_signable,
            (struct aws_signing_config_base *)&signing_config,
            s_aws_trailing_headers_signing_complete,
            callback_data)) {
        aws_jni_throw_runtime_exception(env, "Failed to initiate signing process for trailing headers");
        goto on_error;
    }

    return;

on_error:

    s_cleanup_callback_data(callback_data);
}

JNIEXPORT
bool JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigningUtils_awsSigningUtilsVerifyEcdsaSignature(
    JNIEnv *env,
    jclass jni_class,
    jobject java_http_request,
    jbyteArray java_marshalled_request,
    jstring java_expected_canonical_request,
    jobject java_signing_config,
    jbyteArray java_signature,
    jstring java_verifier_pub_x,
    jstring java_verifier_pub_y) {

    (void)jni_class;

    bool success = false;

    struct aws_string *expected_canonical_request = NULL;
    struct aws_byte_cursor signature_cursor;
    AWS_ZERO_STRUCT(signature_cursor);
    struct aws_string *pub_x = NULL;
    struct aws_string *pub_y = NULL;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct s_aws_sign_request_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s_aws_sign_request_callback_data));
    if (callback_data == NULL) {
        goto done;
    }

    if (java_signature == NULL) {
        goto done;
    }

    signature_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_signature);
    if (signature_cursor.len == 0) {
        goto done;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    /* Build a native aws_signing_config_aws object */
    struct aws_signing_config_aws signing_config;
    AWS_ZERO_STRUCT(signing_config);

    if (s_build_signing_config(env, callback_data, java_signing_config, &signing_config)) {
        goto done;
    }

    jobject java_http_request_body_stream =
        (*env)->GetObjectField(env, java_http_request, http_request_properties.body_stream_field_id);

    callback_data->native_request =
        aws_http_request_new_from_java_http_request(env, java_marshalled_request, java_http_request_body_stream);
    if (callback_data->native_request == NULL) {
        goto done;
    }

    callback_data->original_message_signable = aws_signable_new_http_request(allocator, callback_data->native_request);
    if (callback_data->original_message_signable == NULL) {
        goto done;
    }

    expected_canonical_request = aws_jni_new_string_from_jstring(env, java_expected_canonical_request);
    pub_x = aws_jni_new_string_from_jstring(env, java_verifier_pub_x);
    pub_y = aws_jni_new_string_from_jstring(env, java_verifier_pub_y);

    if (aws_verify_sigv4a_signing(
            allocator,
            callback_data->original_message_signable,
            (struct aws_signing_config_base *)&signing_config,
            aws_byte_cursor_from_string(expected_canonical_request),
            signature_cursor,
            aws_byte_cursor_from_string(pub_x),
            aws_byte_cursor_from_string(pub_y))) {
        aws_jni_throw_runtime_exception(env, aws_error_str(aws_last_error()));
        goto done;
    }

    success = true;

done:

    s_cleanup_callback_data(callback_data);

    aws_string_destroy(expected_canonical_request);
    if (signature_cursor.len > 0) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, java_signature, signature_cursor);
    }
    aws_string_destroy(pub_x);
    aws_string_destroy(pub_y);

    return success;
}

JNIEXPORT
bool JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigningUtils_awsSigningUtilsVerifyRawSha256EcdsaSignature(
    JNIEnv *env,
    jclass jni_class,
    jbyteArray java_string_to_sign,
    jbyteArray java_signature,
    jstring java_verifier_pub_x,
    jstring java_verifier_pub_y) {

    (void)jni_class;

    bool success = false;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_byte_cursor string_to_sign_cursor;
    AWS_ZERO_STRUCT(string_to_sign_cursor);
    struct aws_byte_cursor signature_cursor;
    AWS_ZERO_STRUCT(signature_cursor);

    struct aws_ecc_key_pair *ecc_key = NULL;
    struct aws_string *pub_x = NULL;
    struct aws_string *pub_y = NULL;

    if (java_string_to_sign == NULL || java_signature == NULL || java_verifier_pub_x == NULL ||
        java_verifier_pub_y == NULL) {
        goto done;
    }

    pub_x = aws_jni_new_string_from_jstring(env, java_verifier_pub_x);
    pub_y = aws_jni_new_string_from_jstring(env, java_verifier_pub_y);
    if (pub_x == NULL || pub_y == NULL) {
        goto done;
    }

    ecc_key = aws_ecc_key_new_from_hex_coordinates(
        allocator, AWS_CAL_ECDSA_P256, aws_byte_cursor_from_string(pub_x), aws_byte_cursor_from_string(pub_y));
    if (ecc_key == NULL) {
        goto done;
    }

    string_to_sign_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_string_to_sign);
    signature_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_signature);

    if (aws_validate_v4a_authorization_value(allocator, ecc_key, string_to_sign_cursor, signature_cursor)) {
        goto done;
    }

    success = true;

done:

    if (string_to_sign_cursor.len > 0) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, java_string_to_sign, string_to_sign_cursor);
    }

    if (signature_cursor.len > 0) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, java_signature, signature_cursor);
    }

    aws_string_destroy(pub_x);
    aws_string_destroy(pub_y);

    aws_ecc_key_pair_release(ecc_key);

    return success;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

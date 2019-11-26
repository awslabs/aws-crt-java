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

#include "crt.h"
#include "http_request_utils.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/auth/credentials.h>
#include <aws/auth/signable.h>
#include <aws/auth/signing.h>
#include <aws/common/string.h>
#include <aws/http/request_response.h>

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
    jobject java_sign_param_predicate;
    struct aws_http_message *native_request;
    struct aws_signable *original_message_signable;
    struct aws_string *region;
    struct aws_string *service;
};

static void s_cleanup_callback_data(struct s_aws_sign_request_callback_data *callback_data) {

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    (*env)->DeleteGlobalRef(env, callback_data->java_future);
    (*env)->DeleteGlobalRef(env, callback_data->java_original_request);

    if (callback_data->java_sign_param_predicate) {
        (*env)->DeleteGlobalRef(env, callback_data->java_sign_param_predicate);
    }

    if (callback_data->native_request) {
        aws_http_message_release(callback_data->native_request);
    }

    if (callback_data->original_message_signable) {
        aws_signable_destroy(callback_data->original_message_signable);
    }

    aws_string_destroy(callback_data->region);
    aws_string_destroy(callback_data->service);

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static jobject s_create_signed_java_http_request(
    JNIEnv *env,
    struct aws_http_message *native_request,
    jobject java_original_request) {
    jstring jni_uri = NULL;
    jobjectArray jni_headers = NULL;
    jobject http_request = NULL;

    jstring jni_method = (*env)->GetObjectField(env, java_original_request, http_request_properties.method_field_id);
    jobject jni_body_stream =
        (*env)->GetObjectField(env, java_original_request, http_request_properties.body_stream_field_id);

    struct aws_byte_cursor path_cursor;
    if (aws_http_message_get_request_path(native_request, &path_cursor)) {
        goto done;
    }

    jni_uri = aws_jni_string_from_cursor(env, &path_cursor);
    if (jni_uri == NULL) {
        goto done;
    }

    jni_headers = aws_java_headers_array_from_http_headers(env, aws_http_message_get_headers(native_request));
    if (jni_headers == NULL) {
        goto done;
    }

    http_request = (*env)->NewObject(
        env,
        http_request_properties.http_request_class,
        http_request_properties.constructor_method_id,
        jni_method,
        jni_uri,
        jni_headers,
        jni_body_stream);

done:

    if (jni_uri != NULL) {
        (*env)->DeleteLocalRef(env, jni_uri);
    }

    if (jni_headers != NULL) {
        (*env)->DeleteLocalRef(env, jni_headers);
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

    (*env)->DeleteLocalRef(env, jni_error_string);
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

done:

    s_cleanup_callback_data(callback_data);
}

static bool s_should_sign_param(const struct aws_byte_cursor *name, void *user_data) {
    (void)name;

    struct s_aws_sign_request_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jstring parameter_name = aws_jni_string_from_cursor(env, name);

    bool result = (*env)->CallBooleanMethod(
        env, callback_data->java_sign_param_predicate, predicate_properties.test_method_id, (jobject)parameter_name);

    (*env)->DeleteLocalRef(env, parameter_name);

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

    jstring region = (jstring)(*env)->GetObjectField(env, java_config, aws_signing_config_properties.region_field_id);
    callback_data->region = aws_jni_new_string_from_jstring(env, region);
    config->region = aws_byte_cursor_from_string(callback_data->region);

    jstring service = (jstring)(*env)->GetObjectField(env, java_config, aws_signing_config_properties.service_field_id);
    callback_data->service = aws_jni_new_string_from_jstring(env, service);
    config->service = aws_byte_cursor_from_string(callback_data->service);

    int64_t epoch_time_millis = (*env)->GetLongField(env, java_config, aws_signing_config_properties.time_field_id);
    aws_date_time_init_epoch_millis(&config->date, (uint64_t)epoch_time_millis);

    jobject sign_param_predicate =
        (*env)->GetObjectField(env, java_config, aws_signing_config_properties.should_sign_parameter_field_id);
    if (sign_param_predicate != NULL) {
        callback_data->java_sign_param_predicate = (*env)->NewGlobalRef(env, sign_param_predicate);
        AWS_FATAL_ASSERT(callback_data->java_sign_param_predicate != NULL);

        config->should_sign_param = s_should_sign_param;
        config->should_sign_param_ud = callback_data;
    }

    config->use_double_uri_encode =
        (*env)->GetBooleanField(env, java_config, aws_signing_config_properties.use_double_uri_encode_field_id);
    config->should_normalize_uri_path =
        (*env)->GetBooleanField(env, java_config, aws_signing_config_properties.should_normalize_uri_path_field_id);
    config->sign_body = (*env)->GetBooleanField(env, java_config, aws_signing_config_properties.sign_body_field_id);

    jobject provider =
        (*env)->GetObjectField(env, java_config, aws_signing_config_properties.credentials_provider_field_id);
    config->credentials_provider =
        (void *)(*env)->CallLongMethod(env, provider, crt_resource_properties.get_native_handle_method_id);

    return AWS_OP_SUCCESS;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_signing_AwsSigner_awsSignerSignRequest(
    JNIEnv *env,
    jclass jni_class,
    jobject java_http_request,
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
        aws_jni_throw_runtime_exception(env, "Failed to allocated sign request callback data");
        s_cleanup_callback_data(callback_data);
        return;
    }

    jstring java_method = (*env)->GetObjectField(env, java_http_request, http_request_properties.method_field_id);
    jstring java_uri = (*env)->GetObjectField(env, java_http_request, http_request_properties.encoded_path_field_id);
    jobjectArray java_headers =
        (*env)->GetObjectField(env, java_http_request, http_request_properties.headers_field_id);
    jobject java_http_request_body_stream =
        (*env)->GetObjectField(env, java_http_request, http_request_properties.body_stream_field_id);

    callback_data->native_request = aws_http_request_new_from_java_http_request(
        env, java_method, java_uri, java_headers, java_http_request_body_stream);
    if (callback_data->native_request == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create native http request from Java HttpRequest");
        s_cleanup_callback_data(callback_data);
        return;
    }

    callback_data->original_message_signable = aws_signable_new_http_request(allocator, callback_data->native_request);
    if (callback_data->original_message_signable == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create signable from http request");
        s_cleanup_callback_data(callback_data);
        return;
    }

    /* Sign the native request */
    if (aws_sign_request_aws(
            allocator,
            callback_data->original_message_signable,
            (struct aws_signing_config_base *)&signing_config,
            s_aws_signing_complete,
            callback_data)) {
        aws_jni_throw_runtime_exception(env, "Failed to initiate signing process for HttpRequest");
        s_cleanup_callback_data(callback_data);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif
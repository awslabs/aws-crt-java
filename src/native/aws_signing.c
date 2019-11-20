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

#include <crt.h>
#include <jni.h>
#include <string.h>

#include <aws/auth/signing.h>
#include <aws/auth/credentials.h>
#include <aws/common/string.h>

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

static struct {
    jclass aws_signing_config_class;
    jfieldID algorithm_field_id;
    jfieldID region_field_id;
    jfieldID service_field_id;
    jfieldID time_field_id;
    jfieldID credentials_provider_field_id;
    jfieldID should_sign_parameter_field_id;
    jfieldID use_double_uri_encode_field_id;
    jfieldID should_normalize_uri_path_field_id;
    jfieldID sign_body_field_id;
} s_aws_signing_config_properties;

static struct {
    jclass predicate_class;
    jmethodID test_method_id;
} s_predicate_properties;

static struct {
    jclass http_request_class;
    jmethodID constructor_method_id;
} s_http_request_properties;

static struct {
    jmethodID get_native_handle_method_id;
} s_crt_resource_properties;

void s_cache_signing_jni_ids(JNIEnv *env) {
    /* AwsSigningConfig */
    jclass aws_signing_config_class =
        (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/signing/AwsSigningConfig");
    AWS_FATAL_ASSERT(aws_signing_config_class);
    s_aws_signing_config_properties.aws_signing_config_class = aws_signing_config_class;

    s_aws_signing_config_properties.algorithm_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "signingAlgorithm", "I");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.algorithm_field_id);

    s_aws_signing_config_properties.region_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "region", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.region_field_id);

    s_aws_signing_config_properties.service_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "service", "Ljava/lang/String;");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.service_field_id);

    s_aws_signing_config_properties.time_field_id = (*env)->GetFieldID(env, aws_signing_config_class, "time", "J");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.time_field_id);

    s_aws_signing_config_properties.credentials_provider_field_id = (*env)->GetFieldID(
        env,
        aws_signing_config_class,
        "credentialsProvider",
        "Lsoftware/amazon/awssdk/crt/auth/credentials/CredentialsProvider;");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.credentials_provider_field_id);

    s_aws_signing_config_properties.should_sign_parameter_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "shouldSignParameter", "Ljava/util/function/Predicate;");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.should_sign_parameter_field_id);

    s_aws_signing_config_properties.use_double_uri_encode_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "useDoubleUriEncode", "Z");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.use_double_uri_encode_field_id);

    s_aws_signing_config_properties.should_normalize_uri_path_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "shouldNormalizeUriPath", "Z");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.should_normalize_uri_path_field_id);

    s_aws_signing_config_properties.sign_body_field_id =
        (*env)->GetFieldID(env, aws_signing_config_class, "signBody", "Z");
    AWS_FATAL_ASSERT(s_aws_signing_config_properties.sign_body_field_id);

    /* Predicate */
    jclass predicate_class = (*env)->FindClass(env, "java/util/function/Predicate");
    AWS_FATAL_ASSERT(predicate_class);
    s_predicate_properties.predicate_class = predicate_class;

    s_predicate_properties.test_method_id = (*env)->GetMethodID(env, predicate_class, "test", "(Ljava/lang/Object;)Z");
    AWS_FATAL_ASSERT(s_predicate_properties.test_method_id);

    /* HttpRequest */
    jclass http_request_class = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpRequest");
    AWS_FATAL_ASSERT(http_request_class);
    s_http_request_properties.http_request_class = http_request_class;

    s_http_request_properties.constructor_method_id = (*env)->GetMethodID(
        env,
        http_request_class,
        "<init>",
        "(Ljava/lang/String;Ljava/lang/String;[Lsoftware/amazon/awssdk/crt/http/HttpHeader;Lsoftware/amazon/awssdk/crt/"
        "http/HttpRequestBodyStream;)V");
    AWS_FATAL_ASSERT(s_http_request_properties.constructor_method_id);

    /* CrtResource */
    jclass crt_resource_class = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtResource");
    AWS_FATAL_ASSERT(crt_resource_class);

    s_crt_resource_properties.get_native_handle_method_id = (*env)->GetMethodID(env, crt_resource_class, "getNativeHandle", "()J");
    AWS_FATAL_ASSERT(s_crt_resource_properties.get_native_handle_method_id);
}

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

static void s_aws_signing_complete(struct aws_signing_result *result, int error_code, void *userdata) {

    struct s_aws_sign_request_callback_data *callback_data = userdata;

    ??;

    s_cleanup_callback_data(callback_data);
}

static bool s_should_sign_param(const struct aws_byte_cursor *name, void *user_data) {
    (void)name;

    struct s_aws_sign_request_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jstring parameter_name = ??;

    bool result = (*env)->CallBooleanMethod(env, callback_data->java_sign_param_predicate, s_predicate_properties.test_method_id, (jobject) parameter_name);

    ??(parameter_name);

    return result;
}

/*
struct aws_signing_config_aws {

    struct aws_credentials_provider *credentials_provider;

};

 */

static int s_build_signing_config(JNIEnv *env, struct s_aws_sign_request_callback_data *callback_data, jobject java_config, struct aws_signing_config_aws *config) {
    (void)callback_data;
    (void)java_config;
    (void)config;

    config->config_type = AWS_SIGNING_CONFIG_AWS;
    config->algorithm = (enum aws_signing_algorithm) (*env)->GetIntField(env, java_config, s_aws_signing_config_properties.algorithm_field_id);

    jstring region = (jstring)(*env)->GetObjectField(env, java_config, s_aws_signing_config_properties.region_field_id);
    callback_data->region = aws_jni_new_string_from_jstring(env, region);
    config->region = aws_byte_cursor_from_string(callback_data->region);

    jstring service = (jstring)(*env)->GetObjectField(env, java_config, s_aws_signing_config_properties.service_field_id);
    callback_data->service = aws_jni_new_string_from_jstring(env, service);
    config->service = aws_byte_cursor_from_string(callback_data->service);

    int64_t epoch_time_millis = (*env)->GetLongField(env, java_config, s_aws_signing_config_properties.time_field_id);
    aws_date_time_init_epoch_millis(&config->date, (uint64_t) epoch_time_millis);

    jobject sign_param_predicate = (*env)->GetObjectField(env, java_config, s_aws_signing_config_properties.should_sign_parameter_field_id);
    if (sign_param_predicate != NULL) {
        callback_data->java_sign_param_predicate = (*env)->NewGlobalRef(env, sign_param_predicate);
        if (callback_data->java_sign_param_predicate == NULL) {
            return AWS_OP_ERR;
        }

        config->should_sign_param = s_should_sign_param;
        config->should_sign_param_ud = callback_data;
    }

    config->use_double_uri_encode = (*env)->GetBooleanField(env, java_config, s_aws_signing_config_properties.use_double_uri_encode_field_id);
    config->should_normalize_uri_path = (*env)->GetBooleanField(env, java_config, s_aws_signing_config_properties.should_normalize_uri_path_field_id);
    config->sign_body = (*env)->GetBooleanField(env, java_config, s_aws_signing_config_properties.sign_body_field_id);

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
    (void)java_http_request;
    (void)java_signing_config;
    (void)java_future;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct s_aws_sign_request_callback_data *callback_data = aws_mem_calloc(allocator, 1, sizeof(struct s_aws_sign_request_callback_data));
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to allocated sign request callback data");
        return;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_future = (*env)->NewGlobalRef(env, java_future);
    callback_data->java_original_request = (*env)->NewGlobalRef(env, java_http_request);

    /* Build a native aws_signing_config_aws object */
    struct aws_signing_config_aws signing_config;
    AWS_ZERO_STRUCT(signing_config);

    if (s_build_signing_config(env, callback_data, java_signing_config, &signing_config)) {
        aws_jni_throw_runtime_exception(env, "Failed to allocated sign request callback data");
        s_cleanup_callback_data(callback_data);
        return;
    }

#ifndef NEVER
    aws_jni_throw_runtime_exception(env, "Not yet implemented");
    s_cleanup_callback_data(callback_data);
#else
    /* Build a native request */
    struct aws_http_message *message = aws_http_message_new(allocator);
    callback_data->native_request = message;
    ??

    /* Wrap the native request in a signable */
    ??

    /* Sign the native request */
    if (aws_sign_request_aws(allocator, signable, (struct aws_signing_config_base *)&signing_config, s_aws_signing_complete, callback_data)) {
        aws_jni_throw_runtime_exception(env, "??");
        s_cleanup_callback_data(callback_data);
    }
#endif
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif
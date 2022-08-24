/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "http_connection_manager.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/auth/credentials.h>
#include <aws/common/clock.h>
#include <aws/common/string.h>
#include <aws/http/connection.h>
#include <aws/http/proxy.h>
#include <aws/io/tls_channel_handler.h>

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

struct aws_credentials_provider_callback_data {
    JavaVM *jvm;
    struct aws_credentials_provider *provider;
    jweak java_crt_credentials_provider;

    jobject jni_delegate_credential_handler;
};

static void s_callback_data_clean_up(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_credentials_provider_callback_data *callback_data) {

    (*env)->DeleteWeakGlobalRef(env, callback_data->java_crt_credentials_provider);
    if (callback_data->jni_delegate_credential_handler != NULL) {
        (*env)->DeleteGlobalRef(env, callback_data->jni_delegate_credential_handler);
    }

    aws_mem_release(allocator, callback_data);
}

static void s_on_shutdown_complete(void *user_data) {
    struct aws_credentials_provider_callback_data *callback_data = user_data;

    AWS_LOGF_DEBUG(AWS_LS_AUTH_CREDENTIALS_PROVIDER, "Credentials providers shutdown complete");

    // Tell the Java credentials providers that shutdown is done.  This lets it release its references.
    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jobject java_crt_credentials_provider = (*env)->NewLocalRef(env, callback_data->java_crt_credentials_provider);
    if (java_crt_credentials_provider != NULL) {
        (*env)->CallVoidMethod(
            env, java_crt_credentials_provider, credentials_provider_properties.on_shutdown_complete_method_id);

        (*env)->DeleteLocalRef(env, java_crt_credentials_provider);
        AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    // We're done with this callback data, clean it up.

    JavaVM *jvm = callback_data->jvm;
    s_callback_data_clean_up(env, allocator, callback_data);

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_StaticCredentialsProvider_staticCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jbyteArray access_key_id,
        jbyteArray secret_access_key,
        jbyteArray session_token) {

    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_static_options options;
    AWS_ZERO_STRUCT(options);
    options.access_key_id = aws_jni_byte_cursor_from_jbyteArray_acquire(env, access_key_id);
    options.secret_access_key = aws_jni_byte_cursor_from_jbyteArray_acquire(env, secret_access_key);
    if (session_token) {
        options.session_token = aws_jni_byte_cursor_from_jbyteArray_acquire(env, session_token);
    }
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_static(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create static credentials provider");
    } else {
        callback_data->provider = provider;
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, access_key_id, options.access_key_id);
    aws_jni_byte_cursor_from_jbyteArray_release(env, secret_access_key, options.secret_access_key);

    if (session_token) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, session_token, options.session_token);
    }

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_DefaultChainCredentialsProvider_defaultChainCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jlong bootstrapHandle) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_chain_default_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_chain_default(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider chain");
    } else {
        callback_data->provider = provider;
    }

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_ProfileCredentialsProvider_profileCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jlong bootstrapHandle,
        jlong tls_context_handle,
        jbyteArray profile_name_override,
        jbyteArray config_file_name_override,
        jbyteArray credentials_file_name_override) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_profile_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;
    options.tls_ctx = (struct aws_tls_ctx *)tls_context_handle;

    if (profile_name_override) {
        options.profile_name_override = aws_jni_byte_cursor_from_jbyteArray_acquire(env, profile_name_override);
    }

    if (config_file_name_override) {
        options.config_file_name_override = aws_jni_byte_cursor_from_jbyteArray_acquire(env, config_file_name_override);
    }

    if (credentials_file_name_override) {
        options.credentials_file_name_override =
            aws_jni_byte_cursor_from_jbyteArray_acquire(env, credentials_file_name_override);
    }

    struct aws_credentials_provider *provider = aws_credentials_provider_new_profile(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create profile credentials provider");
    } else {
        callback_data->provider = provider;
    }

    if (profile_name_override) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, profile_name_override, options.profile_name_override);
    }

    if (config_file_name_override) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, config_file_name_override, options.config_file_name_override);
    }

    if (credentials_file_name_override) {
        aws_jni_byte_cursor_from_jbyteArray_release(
            env, credentials_file_name_override, options.credentials_file_name_override);
    }

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_EcsCredentialsProvider_ecsCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jlong bootstrapHandle,
        jlong tls_context_handle,
        jbyteArray host,
        jbyteArray path_and_query,
        jbyteArray auth_token) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_ecs_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;
    options.tls_ctx = (struct aws_tls_ctx *)tls_context_handle;

    if (host) {
        options.host = aws_jni_byte_cursor_from_jbyteArray_acquire(env, host);
    }

    if (path_and_query) {
        options.path_and_query = aws_jni_byte_cursor_from_jbyteArray_acquire(env, path_and_query);
    }

    if (auth_token) {
        options.auth_token = aws_jni_byte_cursor_from_jbyteArray_acquire(env, auth_token);
    }

    struct aws_credentials_provider *provider = aws_credentials_provider_new_ecs(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create ECS credentials provider");
    } else {
        callback_data->provider = provider;
    }

    if (host) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, host, options.host);
    }

    if (path_and_query) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, path_and_query, options.path_and_query);
    }

    if (auth_token) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, auth_token, options.auth_token);
    }

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_StsCredentialsProvider_stsCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jlong bootstrapHandle,
        jlong tls_context_handle,
        jlong creds_provider,
        jbyteArray role_arn,
        jbyteArray session_name,
        jlong duration_seconds) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_sts_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;
    options.tls_ctx = (struct aws_tls_ctx *)tls_context_handle;

    options.creds_provider = (struct aws_credentials_provider *)creds_provider;

    if (role_arn) {
        options.role_arn = aws_jni_byte_cursor_from_jbyteArray_acquire(env, role_arn);
    }

    if (session_name) {
        options.session_name = aws_jni_byte_cursor_from_jbyteArray_acquire(env, session_name);
    }

    options.duration_seconds =
        (uint16_t)aws_timestamp_convert(duration_seconds, AWS_TIMESTAMP_SECS, AWS_TIMESTAMP_SECS, NULL);

    struct aws_credentials_provider *provider = aws_credentials_provider_new_sts(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create STS credentials provider");
    } else {
        callback_data->provider = provider;
    }

    if (role_arn) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, role_arn, options.role_arn);
    }

    if (session_name) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, session_name, options.session_name);
    }

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_StsWebIdentityCredentialsProvider_stsWebIdentityCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jlong bootstrapHandle,
        jlong tls_context_handle) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_sts_web_identity_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;
    options.tls_ctx = (struct aws_tls_ctx *)tls_context_handle;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_sts_web_identity(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create STS web identity credentials provider");
    } else {
        callback_data->provider = provider;
    }

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_X509CredentialsProvider_x509CredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jlong bootstrap_handle,
        jlong tls_context_handle,
        jbyteArray thing_name,
        jbyteArray role_alias,
        jbyteArray endpoint,
        jint proxy_connection_type,
        jbyteArray jni_proxy_host,
        jint jni_proxy_port,
        jlong jni_proxy_tls_context,
        jint jni_proxy_authorization_type,
        jbyteArray jni_proxy_authorization_username,
        jbyteArray jni_proxy_authorization_password) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_tls_connection_options tls_connection_options;
    AWS_ZERO_STRUCT(tls_connection_options);
    aws_tls_connection_options_init_from_ctx(&tls_connection_options, (struct aws_tls_ctx *)tls_context_handle);

    struct aws_credentials_provider_x509_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrap_handle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;
    options.tls_connection_options = &tls_connection_options;
    options.thing_name = aws_jni_byte_cursor_from_jbyteArray_acquire(env, thing_name);
    options.role_alias = aws_jni_byte_cursor_from_jbyteArray_acquire(env, role_alias);
    options.endpoint = aws_jni_byte_cursor_from_jbyteArray_acquire(env, endpoint);

    struct aws_tls_connection_options proxy_tls_connection_options;
    AWS_ZERO_STRUCT(proxy_tls_connection_options);
    struct aws_http_proxy_options proxy_options;
    AWS_ZERO_STRUCT(proxy_options);

    aws_http_proxy_options_jni_init(
        env,
        &proxy_options,
        proxy_connection_type,
        &proxy_tls_connection_options,
        jni_proxy_host,
        (uint16_t)jni_proxy_port,
        jni_proxy_authorization_username,
        jni_proxy_authorization_password,
        jni_proxy_authorization_type,
        (struct aws_tls_ctx *)jni_proxy_tls_context);

    if (jni_proxy_host != NULL) {
        options.proxy_options = &proxy_options;
    }

    struct aws_credentials_provider *provider = aws_credentials_provider_new_x509(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider chain");
    } else {
        callback_data->provider = provider;
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, thing_name, options.thing_name);
    aws_jni_byte_cursor_from_jbyteArray_release(env, role_alias, options.role_alias);
    aws_jni_byte_cursor_from_jbyteArray_release(env, endpoint, options.endpoint);

    aws_http_proxy_options_jni_clean_up(
        env, &proxy_options, jni_proxy_host, jni_proxy_authorization_username, jni_proxy_authorization_password);

    aws_tls_connection_options_clean_up(&tls_connection_options);

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_CachedCredentialsProvider_cachedCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jint cached_duration_in_seconds,
        jlong native_cached_provider) {

    (void)jni_class;

    if (native_cached_provider == 0) {
        aws_jni_throw_runtime_exception(
            env, "CachedCredentialsProviderials.cachedCredentialsProviderNew: cached provider is null");
        return 0;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_cached_options options;
    AWS_ZERO_STRUCT(options);
    options.refresh_time_in_milliseconds =
        aws_timestamp_convert(cached_duration_in_seconds, AWS_TIMESTAMP_SECS, AWS_TIMESTAMP_MILLIS, NULL);
    options.source = (struct aws_credentials_provider *)native_cached_provider;

    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_cached(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create static credentials provider");
    } else {
        callback_data->provider = provider;
    }

    return (jlong)provider;
}

static int s_credentials_provider_delegate_get_credentials(
    void *delegate_user_data,
    aws_on_get_credentials_callback_fn callback,
    void *callback_user_data) {

    struct aws_credentials_provider_callback_data *callback_data = delegate_user_data;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    int return_value = AWS_OP_ERR;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    // Fetch credentials from java
    jobject java_credentials = (*env)->CallObjectMethod(
        env,
        callback_data->jni_delegate_credential_handler,
        credentials_handler_properties.on_handler_get_credentials_method_id);
    if (aws_jni_check_and_clear_exception(env)) {
        aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
        goto fetch_credentials_failed;
    }

    jbyteArray java_access_key_id =
        (*env)->GetObjectField(env, java_credentials, credentials_properties.access_key_id_field_id);
    jbyteArray java_secret_access_key =
        (*env)->GetObjectField(env, java_credentials, credentials_properties.secret_access_key_field_id);
    jbyteArray java_session_token =
        (*env)->GetObjectField(env, java_credentials, credentials_properties.session_token_field_id);

    /**
     * Construct Anonymous Credentials.
     * Anonymous Credentials are used to skip signing.
     */
    if (java_access_key_id == NULL && java_secret_access_key == NULL) {
        struct aws_credentials *native_credentials = aws_credentials_new_anonymous(allocator);
        callback(native_credentials, AWS_ERROR_SUCCESS, callback_user_data);
        aws_credentials_release(native_credentials);

        return_value = AWS_OP_SUCCESS;
        goto empty_credentials;
    }

    if (java_access_key_id == NULL || java_secret_access_key == NULL) {
        aws_jni_throw_runtime_exception(
            env, "DelegateCredentialProvider - Both accessKeyId and secretAccessKey must be either null or non-null");
        goto empty_credentials;
    }

    struct aws_byte_cursor access_key_id = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_access_key_id);
    struct aws_byte_cursor secret_access_key = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_secret_access_key);
    struct aws_byte_cursor session_token;
    AWS_ZERO_STRUCT(session_token);
    if (java_session_token != NULL) {
        session_token = aws_jni_byte_cursor_from_jbyteArray_acquire(env, java_session_token);
    }
    struct aws_credentials *native_credentials =
        aws_credentials_new(allocator, access_key_id, secret_access_key, session_token, UINT64_MAX);
    if (!native_credentials) {
        aws_jni_throw_runtime_exception(env, "Failed to create native credentials");
        // error has been raised from creating function
        goto done;
    }
    callback(native_credentials, AWS_ERROR_SUCCESS, callback_user_data);
    aws_credentials_release(native_credentials);

    return_value = AWS_OP_SUCCESS;
done:
    aws_jni_byte_cursor_from_jbyteArray_release(env, java_access_key_id, access_key_id);
    aws_jni_byte_cursor_from_jbyteArray_release(env, java_secret_access_key, secret_access_key);
    if (java_session_token != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, java_session_token, session_token);
    }
empty_credentials:
    (*env)->DeleteLocalRef(env, java_access_key_id);
    (*env)->DeleteLocalRef(env, java_secret_access_key);
    (*env)->DeleteLocalRef(env, java_session_token);
fetch_credentials_failed:
    (*env)->DeleteLocalRef(env, java_credentials);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

    return return_value;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_DelegateCredentialsProvider_delegateCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_crt_credentials_provider,
        jobject jni_delegate_credential_handler) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewWeakGlobalRef(env, java_crt_credentials_provider);
    callback_data->jni_delegate_credential_handler = (*env)->NewGlobalRef(env, jni_delegate_credential_handler);

    struct aws_credentials_provider_delegate_options options = {
        .get_credentials = s_credentials_provider_delegate_get_credentials,
        .delegate_user_data = callback_data,
        .shutdown_options =
            {
                .shutdown_callback = s_on_shutdown_complete,
                .shutdown_user_data = callback_data,
            },
    };

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider *provider = aws_credentials_provider_new_delegate(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider chain");
    } else {
        callback_data->provider = provider;
    }

    return (jlong)provider;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_credentials_CredentialsProvider_credentialsProviderDestroy(
    JNIEnv *env,
    jclass jni_cp,
    jobject cp_object,
    jlong cp_addr) {
    (void)jni_cp;
    (void)cp_object;
    struct aws_credentials_provider *provider = (struct aws_credentials_provider *)cp_addr;
    if (!provider) {
        aws_jni_throw_runtime_exception(
            env, "CredentialsProvider.credentialsProviderDestroy: instance should be non-null at destruction time");
        return;
    }

    aws_credentials_provider_release(provider);
}

struct aws_credentials_provider_get_credentials_callback_data {
    JavaVM *jvm;
    struct aws_credentials_provider *provider;
    jobject java_crt_credentials_provider;
    jobject java_credentials_future;
};

static void s_cp_callback_data_clean_up(
    struct aws_credentials_provider_get_credentials_callback_data *callback_data,
    JNIEnv *env) {
    if (callback_data == NULL || env == NULL) {
        return;
    }

    (*env)->DeleteGlobalRef(env, callback_data->java_crt_credentials_provider);
    (*env)->DeleteGlobalRef(env, callback_data->java_credentials_future);

    aws_credentials_provider_release(callback_data->provider);

    // We're done with this callback data, free it.
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static void s_on_get_credentials_callback(struct aws_credentials *credentials, int error_code, void *user_data) {
    (void)error_code;

    struct aws_credentials_provider_get_credentials_callback_data *callback_data = user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jobject java_credentials = NULL;
    jbyteArray access_key_id = NULL;
    jbyteArray secret_access_key = NULL;
    jbyteArray session_token = NULL;

    if (credentials) {
        java_credentials = (*env)->NewObject(
            env, credentials_properties.credentials_class, credentials_properties.constructor_method_id);
        if (java_credentials != NULL) {

            struct aws_byte_cursor access_key_id_cursor = aws_credentials_get_access_key_id(credentials);
            if (access_key_id_cursor.len > 0) {
                access_key_id = aws_jni_byte_array_from_cursor(env, &access_key_id_cursor);
            }

            struct aws_byte_cursor secret_access_key_cursor = aws_credentials_get_secret_access_key(credentials);
            if (secret_access_key_cursor.len > 0) {
                secret_access_key = aws_jni_byte_array_from_cursor(env, &secret_access_key_cursor);
            }

            struct aws_byte_cursor session_token_cursor = aws_credentials_get_session_token(credentials);
            if (session_token_cursor.len > 0) {
                session_token = aws_jni_byte_array_from_cursor(env, &session_token_cursor);
            }

            (*env)->SetObjectField(env, java_credentials, credentials_properties.access_key_id_field_id, access_key_id);
            (*env)->SetObjectField(
                env, java_credentials, credentials_properties.secret_access_key_field_id, secret_access_key);
            if (session_token != NULL) {
                (*env)->SetObjectField(
                    env, java_credentials, credentials_properties.session_token_field_id, session_token);
            }
        }
    }

    (*env)->CallVoidMethod(
        env,
        callback_data->java_crt_credentials_provider,
        credentials_provider_properties.on_get_credentials_complete_method_id,
        callback_data->java_credentials_future,
        java_credentials);

    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));

    if (java_credentials != NULL) {
        if (access_key_id) {
            (*env)->DeleteLocalRef(env, access_key_id);
        }
        if (secret_access_key) {
            (*env)->DeleteLocalRef(env, secret_access_key);
        }
        if (session_token) {
            (*env)->DeleteLocalRef(env, session_token);
        }
        (*env)->DeleteLocalRef(env, java_credentials);
    }

    JavaVM *jvm = callback_data->jvm;
    s_cp_callback_data_clean_up(callback_data, env);

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_credentials_CredentialsProvider_credentialsProviderGetCredentials(
    JNIEnv *env,
    jclass jni_cp,
    jobject java_crt_credentials_provider,
    jobject java_credentials_future,
    jlong native_credentials_provider) {
    (void)jni_cp;
    struct aws_credentials_provider *provider = (struct aws_credentials_provider *)native_credentials_provider;
    if (!provider) {
        aws_jni_throw_runtime_exception(
            env, "CredentialsProvider.credentialsProviderGetCredentials: instance should be non-null");
        return;
    }

    if (java_crt_credentials_provider == NULL || java_credentials_future == NULL) {
        aws_jni_throw_runtime_exception(
            env, "CredentialsProvider.credentialsProviderGetCredentials: called with null parameters");
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_get_credentials_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_get_credentials_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);
    callback_data->java_credentials_future = (*env)->NewGlobalRef(env, java_credentials_future);
    callback_data->provider = provider;

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_credentials_provider_acquire(provider);

    if (aws_credentials_provider_get_credentials(provider, s_on_get_credentials_callback, callback_data)) {
        aws_jni_throw_runtime_exception(env, "CrtCredentialsProvider.credentialsProviderGetCredentials: call failure");
        /* callback will not be invoked on failure, clean up the resource here. */
        s_cp_callback_data_clean_up(callback_data, env);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

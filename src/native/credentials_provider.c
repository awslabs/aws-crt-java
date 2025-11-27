/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "credentials.h"
#include "crt.h"
#include "http_connection_manager.h"
#include "java_class_ids.h"

#include <http_proxy_options.h>
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
    jobject java_crt_credentials_provider;

    jobject jni_delegate_credential_handler;

    /**
     * Right now, all provider bindings share the same basic binding setup, but some providers need some
     * additional state specific to that provider.  Rather than going a full vtable/base/wrapped solution,
     * we just let such providers attach this data here.  They are expected to clean it up before the clean up
     * for this structure is called, hence the fatal assert below.
     */
    void *aux_data;
};

static void s_callback_data_clean_up(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct aws_credentials_provider_callback_data *callback_data) {

    // any provider-specific auxiliary data should have been already cleaned up
    AWS_FATAL_ASSERT(callback_data->aux_data == NULL);

    (*env)->DeleteGlobalRef(env, callback_data->java_crt_credentials_provider);
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
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(callback_data->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    if (callback_data->java_crt_credentials_provider != NULL) {
        (*env)->CallVoidMethod(
            env,
            callback_data->java_crt_credentials_provider,
            credentials_provider_properties.on_shutdown_complete_method_id);
        AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    // We're done with this callback data, clean it up.

    JavaVM *jvm = callback_data->jvm;
    s_callback_data_clean_up(env, allocator, callback_data);
    aws_jni_release_thread_env(jvm, &jvm_env_context);
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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
        jbyteArray jni_proxy_authorization_password,
        jbyteArray jni_no_proxy_hosts) {

    (void)jni_class;
    (void)env;
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
        jni_proxy_port,
        jni_proxy_authorization_username,
        jni_proxy_authorization_password,
        jni_no_proxy_hosts,
        jni_proxy_authorization_type,
        (struct aws_tls_ctx *)jni_proxy_tls_context);

    if (jni_proxy_host != NULL) {
        options.proxy_options = &proxy_options;
    }

    struct aws_credentials_provider *provider = aws_credentials_provider_new_x509(allocator, &options);
    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create X509 credentials provider");
    } else {
        callback_data->provider = provider;
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, thing_name, options.thing_name);
    aws_jni_byte_cursor_from_jbyteArray_release(env, role_alias, options.role_alias);
    aws_jni_byte_cursor_from_jbyteArray_release(env, endpoint, options.endpoint);

    aws_http_proxy_options_jni_clean_up(
        env,
        &proxy_options,
        jni_proxy_host,
        jni_proxy_authorization_username,
        jni_proxy_authorization_password,
        jni_no_proxy_hosts);

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
    aws_cache_jni_ids(env);

    if (native_cached_provider == 0) {
        aws_jni_throw_runtime_exception(
            env, "CachedCredentialsProviderials.cachedCredentialsProviderNew: cached provider is null");
        return 0;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);

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
        aws_jni_throw_runtime_exception(env, "Failed to create cached credentials provider");
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

    int return_value = AWS_OP_ERR;

    /********** JNI ENV ACQUIRE **********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(callback_data->jvm);
    JNIEnv *env = jvm_env_context.env;
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
        goto done;
    }

    struct aws_credentials *native_credentials = aws_credentials_new_from_java_credentials(env, java_credentials);
    if (!native_credentials) {
        aws_jni_throw_runtime_exception(env, "Failed to create native credentials");
        // error has been raised from creating function
        goto done;
    }
    callback(native_credentials, AWS_ERROR_SUCCESS, callback_user_data);
    aws_credentials_release(native_credentials);

    return_value = AWS_OP_SUCCESS;

done:
    (*env)->DeleteLocalRef(env, java_credentials);

    aws_jni_release_thread_env(callback_data->jvm, &jvm_env_context);
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
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, java_crt_credentials_provider);
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
        aws_jni_throw_runtime_exception(env, "Failed to create delegate credentials provider");
    } else {
        callback_data->provider = provider;
    }

    return (jlong)provider;
}

static int s_fill_in_logins(struct aws_array_list *logins, struct aws_byte_cursor marshalled_logins) {
    struct aws_byte_cursor logins_cursor = marshalled_logins;
    uint32_t field_len = 0;

    while (logins_cursor.len > 0) {
        if (!aws_byte_cursor_read_be32(&logins_cursor, &field_len)) {
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }

        struct aws_byte_cursor identity_provider_name = aws_byte_cursor_advance(&logins_cursor, field_len);

        if (!aws_byte_cursor_read_be32(&logins_cursor, &field_len)) {
            return aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        }

        struct aws_byte_cursor identity_provider_token = aws_byte_cursor_advance(&logins_cursor, field_len);

        struct aws_cognito_identity_provider_token_pair login_pair = {
            .identity_provider_name = identity_provider_name,
            .identity_provider_token = identity_provider_token,
        };

        aws_array_list_push_back(logins, &login_pair);
    }

    return AWS_OP_SUCCESS;
}

/*
 * Optional auxiliary provider data used by the Cognito provider binding.  Keeps a reference to the
 * CognitoLoginTokenSource java object that should be used to query dynamic login tokens before each
 * HTTP request to Cognito.
 *
 * We ref count this just to be safe; every in-progress credentials query (technically there should only be at most 1,
 * but to be paranoid we ignore that) keeps a reference to this object (in addition to the reference kept by the
 * generic provider binding via the aux_data field).
 */
struct aws_login_token_source_data {
    struct aws_allocator *allocator;
    struct aws_ref_count ref_count;
    JavaVM *jvm;
    jobject login_token_source;
};

static void s_aws_login_token_source_data_on_zero_ref(void *user_data) {
    struct aws_login_token_source_data *login_token_source_data = user_data;

    AWS_FATAL_ASSERT(login_token_source_data != NULL);
    JavaVM *jvm = login_token_source_data->jvm;

    /********** JNI ENV ACQUIRE **********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env != NULL) {
        if (login_token_source_data->login_token_source != NULL) {
            (*env)->DeleteGlobalRef(env, login_token_source_data->login_token_source);
        }
    }
    aws_jni_release_thread_env(jvm, &jvm_env_context);
    /********** JNI ENV RELEASE **********/

    aws_mem_release(login_token_source_data->allocator, login_token_source_data);
}

static struct aws_login_token_source_data *s_aws_login_token_source_data_acquire(
    struct aws_login_token_source_data *login_token_source_data) {
    if (login_token_source_data != NULL) {
        aws_ref_count_acquire(&login_token_source_data->ref_count);
    }

    return login_token_source_data;
}

static struct aws_login_token_source_data *s_aws_login_token_source_data_release(
    struct aws_login_token_source_data *login_token_source_data) {
    if (login_token_source_data != NULL) {
        aws_ref_count_release(&login_token_source_data->ref_count);
    }

    return NULL;
}

static struct aws_login_token_source_data *s_aws_login_token_source_data_new(
    struct aws_allocator *allocator,
    JNIEnv *env,
    jobject login_token_source) {
    if (login_token_source == NULL) {
        /*
         * Not an error: if no login token source is provided, returning NULL causes us to skip dynamic token
         * sourcing during credentials fetch
         */
        return NULL;
    }

    struct aws_login_token_source_data *login_token_source_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_login_token_source_data));
    login_token_source_data->allocator = allocator;
    login_token_source_data->login_token_source = (*env)->NewGlobalRef(env, login_token_source);
    aws_ref_count_init(
        &login_token_source_data->ref_count, login_token_source_data, s_aws_login_token_source_data_on_zero_ref);

    jint jvmresult = (*env)->GetJavaVM(env, &login_token_source_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    return login_token_source_data;
}

static void s_on_cognito_shutdown_complete(void *user_data) {
    struct aws_credentials_provider_callback_data *callback_data = user_data;
    struct aws_login_token_source_data *login_token_source_data = callback_data->aux_data;

    callback_data->aux_data = s_aws_login_token_source_data_release(login_token_source_data);

    s_on_shutdown_complete(user_data);
}

/* Per-credential-fetch binding data used when there is a cognito login token source configured for the provider */
struct aws_login_token_source_invocation {
    struct aws_allocator *allocator;
    struct aws_login_token_source_data *login_token_source_data; /* strong reference */

    /*
     * This hidden/internal future is what transfers the login token pairs from the future completed by the
     * user to the static callback that continues the cognito credential fetch.  Must be referenced or it might
     * be GCed before the dynamic login tokens are fetched.
     */
    jobject chained_future;

    aws_credentials_provider_cognito_get_token_pairs_completion_fn *completion_callback;
    void *completion_user_data;
};

static struct aws_login_token_source_invocation *aws_login_token_source_invocation_new(
    struct aws_allocator *allocator,
    struct aws_login_token_source_data *login_token_source_data,
    aws_credentials_provider_cognito_get_token_pairs_completion_fn *completion_callback,
    void *completion_user_data) {

    struct aws_login_token_source_invocation *invocation =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_login_token_source_invocation));
    invocation->allocator = allocator;
    invocation->login_token_source_data = s_aws_login_token_source_data_acquire(login_token_source_data);
    invocation->completion_callback = completion_callback;
    invocation->completion_user_data = completion_user_data;

    return invocation;
}

static void s_aws_login_token_source_invocation_destroy(
    struct aws_login_token_source_invocation *invocation,
    JNIEnv *env) {
    if (invocation == NULL) {
        return;
    }

    invocation->login_token_source_data = s_aws_login_token_source_data_release(invocation->login_token_source_data);

    if (invocation->chained_future != NULL) {
        (*env)->DeleteGlobalRef(env, invocation->chained_future);
    }

    aws_mem_release(invocation->allocator, invocation);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_credentials_CognitoCredentialsProvider_completeLoginTokenFetch(
    JNIEnv *env,
    jclass jni_class,
    jlong invocation_handle,
    jbyteArray marshalled_logins,
    jobject ex) {
    (void)jni_class;

    struct aws_login_token_source_invocation *invocation =
        (struct aws_login_token_source_invocation *)invocation_handle;

    struct aws_byte_cursor logins_cursor;
    AWS_ZERO_STRUCT(logins_cursor);

    struct aws_array_list logins;
    aws_array_list_init_dynamic(
        &logins, invocation->allocator, 0, sizeof(struct aws_cognito_identity_provider_token_pair));

    size_t login_count = 0;
    struct aws_cognito_identity_provider_token_pair *login_sequence = NULL;

    int error_code = AWS_ERROR_SUCCESS;
    if (ex != NULL) {
        error_code = AWS_AUTH_CREDENTIALS_PROVIDER_COGNITO_SOURCE_FAILURE;
    }

    if (marshalled_logins != NULL) {
        logins_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, marshalled_logins);
        if (s_fill_in_logins(&logins, logins_cursor)) {
            error_code = aws_last_error();
        } else {
            login_sequence = logins.data;
            login_count = aws_array_list_length(&logins);
        }
    }

    (*invocation->completion_callback)(login_sequence, login_count, error_code, invocation->completion_user_data);

    aws_jni_byte_cursor_from_jbyteArray_release(env, marshalled_logins, logins_cursor);
    aws_array_list_clean_up(&logins);

    s_aws_login_token_source_invocation_destroy(invocation, env);
}

static int s_cognito_get_token_pairs(
    void *get_token_pairs_user_data,
    aws_credentials_provider_cognito_get_token_pairs_completion_fn *completion_callback,
    void *completion_user_data) {

    struct aws_login_token_source_data *login_token_source_data = get_token_pairs_user_data;
    JavaVM *jvm = login_token_source_data->jvm;

    /********** JNI ENV ACQUIRE **********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        return aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_DESTROYED);
    }

    int result = AWS_OP_ERR;
    struct aws_login_token_source_invocation *invocation = aws_login_token_source_invocation_new(
        login_token_source_data->allocator, login_token_source_data, completion_callback, completion_user_data);

    // create the base future that the user must complete with login token pairs
    jobject java_base_future = (*env)->NewObject(
        env,
        completable_future_properties.completable_future_class,
        completable_future_properties.constructor_method_id);
    if ((*env)->ExceptionCheck(env) || java_base_future == NULL) {
        aws_jni_check_and_clear_exception(env);
        aws_raise_error(AWS_AUTH_CREDENTIALS_PROVIDER_COGNITO_SOURCE_FAILURE);
        goto done;
    }

    // create the chained future that invokes the completion callback when the base future is completed either
    // normally or exceptionally
    jobject java_chained_future = (*env)->CallStaticObjectMethod(
        env,
        cognito_credentials_provider_properties.cognito_credentials_provider_class,
        cognito_credentials_provider_properties.create_chained_future_method_id,
        (jlong)invocation,
        java_base_future);
    if ((*env)->ExceptionCheck(env) || java_chained_future == NULL) {
        aws_jni_check_and_clear_exception(env);
        aws_raise_error(AWS_AUTH_CREDENTIALS_PROVIDER_COGNITO_SOURCE_FAILURE);
        goto done;
    }

    invocation->chained_future = (*env)->NewGlobalRef(env, java_chained_future);

    // invoke the login source java API with the base future
    (*env)->CallVoidMethod(
        env,
        login_token_source_data->login_token_source,
        cognito_login_token_source_properties.start_login_token_fetch_method_id,
        java_base_future);
    if ((*env)->ExceptionCheck(env)) {
        aws_jni_check_and_clear_exception(env);
        aws_raise_error(AWS_AUTH_CREDENTIALS_PROVIDER_COGNITO_SOURCE_FAILURE);
        goto done;
    }

    result = AWS_OP_SUCCESS;

done:

    if (result != AWS_OP_SUCCESS) {
        s_aws_login_token_source_invocation_destroy(invocation, env);
    }

    aws_jni_release_thread_env(jvm, &jvm_env_context);
    /********** JNI ENV RELEASE **********/

    return result;
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_auth_credentials_CognitoCredentialsProvider_cognitoCredentialsProviderNew(
    JNIEnv *env,
    jclass jni_class,
    jobject crt_credentials_provider,
    jlong native_bootstrap,
    jlong native_tls_context,
    jstring endpoint,
    jstring identity,
    jstring custom_role_arn,
    jbyteArray marshalled_logins,
    jint proxy_connection_type,
    jbyteArray proxy_host,
    jint proxy_port,
    jlong native_proxy_tls_context,
    jint proxy_authorization_type,
    jbyteArray proxy_authorization_username,
    jbyteArray proxy_authorization_password,
    jbyteArray no_proxy_hosts,
    jobject login_token_source) {

    (void)jni_class;
    (void)env;

    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider *provider = NULL;
    struct aws_credentials_provider_callback_data *callback_data = NULL;

    struct aws_tls_connection_options proxy_tls_connection_options;
    AWS_ZERO_STRUCT(proxy_tls_connection_options);
    struct aws_http_proxy_options proxy_options;
    AWS_ZERO_STRUCT(proxy_options);

    struct aws_byte_cursor endpoint_cursor;
    AWS_ZERO_STRUCT(endpoint_cursor);
    struct aws_byte_cursor identity_cursor;
    AWS_ZERO_STRUCT(identity_cursor);
    struct aws_byte_cursor custom_role_arn_cursor;
    AWS_ZERO_STRUCT(custom_role_arn_cursor);
    struct aws_byte_cursor logins_cursor;
    AWS_ZERO_STRUCT(logins_cursor);

    struct aws_array_list logins;
    aws_array_list_init_dynamic(&logins, allocator, 0, sizeof(struct aws_cognito_identity_provider_token_pair));

    if (endpoint == NULL || identity == NULL) {
        goto done;
    }

    endpoint_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, endpoint);
    identity_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, identity);

    callback_data = aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_callback_data));

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);
    callback_data->java_crt_credentials_provider = (*env)->NewGlobalRef(env, crt_credentials_provider);

    /* If no login token source is provided, this evaluates to NULL */
    callback_data->aux_data = s_aws_login_token_source_data_new(allocator, env, login_token_source);

    struct aws_credentials_provider_cognito_options options = {
        .shutdown_options =
            {
                .shutdown_callback = s_on_cognito_shutdown_complete,
                .shutdown_user_data = callback_data,
            },
        .endpoint = endpoint_cursor,
        .identity = identity_cursor,
        .bootstrap = (void *)native_bootstrap,
        .tls_ctx = (void *)native_tls_context,
    };

    if (callback_data->aux_data != NULL) {
        options.get_token_pairs = s_cognito_get_token_pairs;
        options.get_token_pairs_user_data = callback_data->aux_data;
    }

    if (custom_role_arn != NULL) {
        custom_role_arn_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, custom_role_arn);
        options.custom_role_arn = &custom_role_arn_cursor;
    }

    if (marshalled_logins != NULL) {
        logins_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, marshalled_logins);
        if (s_fill_in_logins(&logins, logins_cursor)) {
            goto done;
        }

        options.logins = logins.data;
        options.login_count = aws_array_list_length(&logins);
    }

    if (proxy_host != NULL) {
        aws_http_proxy_options_jni_init(
            env,
            &proxy_options,
            proxy_connection_type,
            &proxy_tls_connection_options,
            proxy_host,
            proxy_port,
            proxy_authorization_username,
            proxy_authorization_password,
            no_proxy_hosts,
            proxy_authorization_type,
            (struct aws_tls_ctx *)native_proxy_tls_context);

        options.http_proxy_options = &proxy_options;
    }

    provider = aws_credentials_provider_new_cognito(allocator, &options);
    if (provider != NULL) {
        callback_data->provider = provider;
    }

done:

    aws_jni_byte_cursor_from_jstring_release(env, endpoint, endpoint_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, identity, identity_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, custom_role_arn, custom_role_arn_cursor);
    aws_jni_byte_cursor_from_jbyteArray_release(env, marshalled_logins, logins_cursor);

    aws_http_proxy_options_jni_clean_up(
        env, &proxy_options, proxy_host, proxy_authorization_username, proxy_authorization_password, no_proxy_hosts);

    aws_array_list_clean_up(&logins);

    if (provider == NULL) {
        s_callback_data_clean_up(env, allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create native cognito credentials provider");
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
    aws_cache_jni_ids(env);

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
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(callback_data->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jobject java_credentials = NULL;

    if (credentials) {
        java_credentials = aws_java_credentials_from_native_new(env, credentials);
    }

    (*env)->CallVoidMethod(
        env,
        callback_data->java_crt_credentials_provider,
        credentials_provider_properties.on_get_credentials_complete_method_id,
        callback_data->java_credentials_future,
        error_code,
        java_credentials);

    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));

    if (java_credentials != NULL) {
        (*env)->DeleteLocalRef(env, java_credentials);
    }

    JavaVM *jvm = callback_data->jvm;
    s_cp_callback_data_clean_up(callback_data, env);
    aws_jni_release_thread_env(jvm, &jvm_env_context);
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
    aws_cache_jni_ids(env);

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

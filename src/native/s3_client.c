/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "aws_signing.h"
#include "credentials.h"
#include "crt.h"
#include "http_request_utils.h"
#include "java_class_ids.h"
#include "retry_utils.h"
#include <aws/common/string.h>
#include <aws/http/connection.h>
#include <aws/http/proxy.h>
#include <aws/http/request_response.h>
#include <aws/io/channel_bootstrap.h>
#include <aws/io/retry_strategy.h>
#include <aws/io/stream.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/io/uri.h>
#include <aws/s3/s3_client.h>
#include <aws/s3/s3express_credentials_provider.h>
#include <http_proxy_options.h>
#include <http_proxy_options_environment_variable.h>
#include <jni.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'jni_tls_ctx_options *' */
#        pragma warning(disable : 4221)
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

struct s3_client_callback_data {
    JavaVM *jvm;
    jobject java_s3_client;
    struct aws_signing_config_data signing_config_data;
    jobject java_s3express_provider_factory;
};

struct s3_client_make_meta_request_callback_data {
    JavaVM *jvm;
    jobject java_s3_meta_request;
    jobject java_s3_meta_request_response_handler_native_adapter;
    struct aws_input_stream *input_stream;
    struct aws_signing_config_data signing_config_data;
    jthrowable java_exception;
};

static void s_on_s3_client_shutdown_complete_callback(void *user_data);
static void s_on_s3_meta_request_shutdown_complete_callback(void *user_data);

int aws_s3_tcp_keep_alive_options_from_java(
    JNIEnv *env,
    jobject jni_s3_tcp_keep_alive_options,
    struct aws_s3_tcp_keep_alive_options *s3_tcp_keep_alive_options) {

    uint16_t jni_keep_alive_interval_sec = (*env)->GetShortField(
        env, jni_s3_tcp_keep_alive_options, s3_tcp_keep_alive_options_properties.keep_alive_interval_sec_field_id);

    uint16_t jni_keep_alive_timeout_sec = (*env)->GetShortField(
        env, jni_s3_tcp_keep_alive_options, s3_tcp_keep_alive_options_properties.keep_alive_timeout_sec_field_id);

    uint16_t jni_keep_alive_max_failed_probes = (*env)->GetShortField(
        env, jni_s3_tcp_keep_alive_options, s3_tcp_keep_alive_options_properties.keep_alive_max_failed_probes_field_id);

    AWS_ZERO_STRUCT(*s3_tcp_keep_alive_options);

    s3_tcp_keep_alive_options->keep_alive_interval_sec = jni_keep_alive_interval_sec;
    s3_tcp_keep_alive_options->keep_alive_timeout_sec = jni_keep_alive_timeout_sec;
    s3_tcp_keep_alive_options->keep_alive_max_failed_probes = jni_keep_alive_max_failed_probes;

    return AWS_OP_SUCCESS;
}

struct s3_client_s3express_provider_java_impl {
    JavaVM *jvm;
    jobject java_s3express_provider;
};

struct s3_client_s3express_provider_callback_data {
    void *log_id;
    aws_on_get_credentials_callback_fn *get_cred_callback;
    void *get_cred_user_data;
};

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_s3_S3ExpressCredentialsProvider_s3expressCredentialsProviderGetCredentialsCompleted(
        JNIEnv *env,
        jclass jni_class,
        jlong nativeHandler,
        jobject java_credentials) {

    (void)jni_class;
    struct s3_client_s3express_provider_callback_data *callback_data = (void *)nativeHandler;
    struct aws_credentials *native_credentials = NULL;
    int error_code = AWS_ERROR_SUCCESS;

    if (!java_credentials || aws_jni_check_and_clear_exception(env)) {
        /* TODO: a separate error code?? */
        AWS_LOGF_ERROR(
            AWS_LS_S3_META_REQUEST,
            "id=%p: Failed to get S3Express credentials from Java",
            (void *)callback_data->log_id);
        error_code = AWS_ERROR_HTTP_CALLBACK_FAILURE;
        goto done;
    }

    native_credentials = aws_credentials_new_from_java_credentials(env, java_credentials);
    if (!native_credentials) {
        aws_jni_throw_runtime_exception(env, "Failed to create native credentials");
        AWS_LOGF_ERROR(
            AWS_LS_S3_META_REQUEST,
            "id=%p: Failed to create native credentials from Java",
            (void *)callback_data->log_id);
        goto done;
    }
    struct aws_byte_cursor session_token = aws_credentials_get_session_token(native_credentials);

    if (session_token.len == 0) {
        aws_jni_throw_runtime_exception(env, "S3ExpressCredentialsProvider - sessionToken must be non-null");
        error_code = AWS_ERROR_HTTP_CALLBACK_FAILURE;
        aws_credentials_release(native_credentials);
        native_credentials = NULL;
        goto done;
    }

done:
    callback_data->get_cred_callback(native_credentials, error_code, callback_data->get_cred_user_data);
    aws_credentials_release(native_credentials);
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static int s_s3express_get_creds_java(
    struct aws_s3express_credentials_provider *provider,
    const struct aws_credentials *original_credentials,
    const struct aws_credentials_properties_s3express *s3express_properties,
    aws_on_get_credentials_callback_fn callback,
    void *user_data) {

    struct s3_client_s3express_provider_java_impl *impl = provider->impl;
    int result = AWS_OP_ERR;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(impl->jvm);
    if (!env) {
        /* If we can't get an environment, then the JVM is probably shutting down. Don't crash. */
        return AWS_OP_SUCCESS;
    }
    jobject properties_object = NULL;
    jobject original_credentials_object = NULL;

    properties_object = (*env)->NewObject(
        env,
        s3express_credentials_properties_properties.s3express_credentials_properties_class,
        s3express_credentials_properties_properties.constructor_method_id);
    if ((*env)->ExceptionCheck(env) || properties_object == NULL) {
        aws_jni_throw_runtime_exception(
            env,
            "S3ExpressCredentialsProvider.getS3ExpressCredentials: Failed to create S3ExpressCredentialsProperties "
            "object.");
        goto done;
    }
    original_credentials_object = aws_java_credentials_from_native_new(env, original_credentials);
    if ((*env)->ExceptionCheck(env) || original_credentials_object == NULL) {
        aws_jni_throw_runtime_exception(
            env, "S3ExpressCredentialsProvider.getS3ExpressCredentials: Failed to create Credentials object.");
        goto done;
    }

    jstring jni_host = aws_jni_string_from_cursor(env, &s3express_properties->host);
    jstring jni_region = aws_jni_string_from_cursor(env, &s3express_properties->region);
    (*env)->SetObjectField(env, properties_object, s3express_credentials_properties_properties.host_field_id, jni_host);
    (*env)->SetObjectField(
        env, properties_object, s3express_credentials_properties_properties.region_field_id, jni_region);
    (*env)->DeleteLocalRef(env, jni_host);
    (*env)->DeleteLocalRef(env, jni_region);

    struct s3_client_s3express_provider_callback_data *callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct s3_client_s3express_provider_callback_data));

    callback_data->get_cred_callback = callback;
    callback_data->get_cred_user_data = user_data;
    callback_data->log_id = impl->java_s3express_provider;

    /* Invoke the java call */
    (*env)->CallVoidMethod(
        env,
        impl->java_s3express_provider,
        s3express_credentials_provider_properties.getS3ExpressCredentials,
        properties_object,
        original_credentials_object,
        (jlong)callback_data);

    if (aws_jni_check_and_clear_exception(env)) {
        /* Check if any exception raised */
        AWS_LOGF_ERROR(
            AWS_LS_S3_META_REQUEST,
            "id=%p: S3ExpressCredentialsProvider.getS3ExpressCredentials failed",
            (void *)impl->java_s3express_provider);
        aws_mem_release(aws_jni_get_allocator(), callback_data);
        goto done;
    }
    result = AWS_OP_SUCCESS;
done:
    if (properties_object) {
        (*env)->DeleteLocalRef(env, properties_object);
    }
    if (original_credentials_object) {
        (*env)->DeleteLocalRef(env, original_credentials_object);
    }
    aws_jni_release_thread_env(impl->jvm, env);
    /********** JNI ENV RELEASE **********/
    return result;
}

static void s_s3express_destroy_java(struct aws_s3express_credentials_provider *provider) {
    struct s3_client_s3express_provider_java_impl *impl = provider->impl;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(impl->jvm);
    if (!env) {
        /* If we can't get an environment, then the JVM is probably shutting down. Don't crash. */
        return;
    }
    /* Invoke the java call */
    (*env)->CallVoidMethod(
        env, impl->java_s3express_provider, s3express_credentials_provider_properties.destroyProvider);

    (*env)->DeleteGlobalRef(env, impl->java_s3express_provider);

    aws_jni_release_thread_env(impl->jvm, env);
    /********** JNI ENV RELEASE **********/
    /* Once the java call returns, the java resource should be cleaned up already. We can finish up the shutdown
     * process. Clean up the native part. */
    aws_simple_completion_callback *callback = provider->shutdown_complete_callback;
    void *user_data = provider->shutdown_user_data;
    aws_mem_release(provider->allocator, provider);
    callback(user_data);
}

static struct aws_s3express_credentials_provider_vtable s_java_s3express_vtable = {
    .get_credentials = s_s3express_get_creds_java,
    .destroy = s_s3express_destroy_java,
};

struct aws_s3express_credentials_provider *s_s3express_provider_jni_factory(
    struct aws_allocator *allocator,
    struct aws_s3_client *client,
    aws_simple_completion_callback shutdown_complete_callback,
    void *shutdown_user_data,
    void *factory_user_data) {

    (void)client;
    struct s3_client_callback_data *client_data = factory_user_data;

    struct aws_s3express_credentials_provider *provider = NULL;
    struct s3_client_s3express_provider_java_impl *impl = NULL;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(client_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return NULL;
    }

    aws_mem_acquire_many(
        allocator,
        2,
        &provider,
        sizeof(struct aws_s3express_credentials_provider),
        &impl,
        sizeof(struct s3_client_s3express_provider_java_impl));
    /* Call into the java factory to create the java impl */
    AWS_FATAL_ASSERT(client_data->java_s3express_provider_factory != NULL);
    jobject java_s3express_provider = (*env)->CallObjectMethod(
        env,
        client_data->java_s3express_provider_factory,
        s3express_credentials_provider_factory_properties.createS3ExpressCredentialsProvider,
        client_data->java_s3_client);

    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_ERROR(
            AWS_LS_S3_META_REQUEST,
            "id=%p: Failed to create Java S3Express Provider",
            (void *)client_data->java_s3express_provider_factory);
        aws_mem_release(allocator, provider);
        provider = NULL;
        goto done;
    }
    impl->java_s3express_provider = (*env)->NewGlobalRef(env, java_s3express_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &impl->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_s3express_credentials_provider_init_base(provider, allocator, &s_java_s3express_vtable, impl);
    provider->shutdown_complete_callback = shutdown_complete_callback;
    provider->shutdown_user_data = shutdown_user_data;

    /* We are done using the factory, when succeed, clean it up. TODO: we don't have to clean it up here */
    (*env)->DeleteGlobalRef(env, client_data->java_s3express_provider_factory);
    client_data->java_s3express_provider_factory = NULL;

done:
    aws_jni_release_thread_env(client_data->jvm, env);
    /********** JNI ENV RELEASE **********/
    return provider;
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_s3_S3Client_s3ClientNew(
    JNIEnv *env,
    jclass jni_class,
    jobject s3_client_jobject,
    jbyteArray jni_region,
    jlong jni_client_bootstrap,
    jlong jni_tls_ctx,
    jobject java_signing_config,
    jlong part_size_jlong,
    jlong multipart_upload_threshold_jlong,
    jdouble throughput_target_gbps,
    jboolean enable_read_backpressure,
    jlong initial_read_window_jlong,
    int max_connections,
    jobject jni_standard_retry_options,
    jboolean compute_content_md5,
    jint jni_proxy_connection_type,
    jbyteArray jni_proxy_host,
    jint jni_proxy_port,
    jlong jni_proxy_tls_context,
    jint jni_proxy_authorization_type,
    jbyteArray jni_proxy_authorization_username,
    jbyteArray jni_proxy_authorization_password,
    jint jni_environment_variable_proxy_connection_type,
    jlong jni_environment_variable_proxy_tls_connection_options,
    jint jni_environment_variable_type,
    int connect_timeout_ms,
    jobject jni_s3_tcp_keep_alive_options,
    jlong jni_monitoring_throughput_threshold_in_bytes_per_second,
    jint jni_monitoring_failure_interval_in_seconds,
    jboolean enable_s3express,
    jobject java_s3express_provider_factory,
    jlong jni_memory_limit_bytes_jlong) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;

    if (!client_bootstrap) {
        aws_jni_throw_illegal_argument_exception(env, "Invalid Client Bootstrap");
        return (jlong)NULL;
    }

    uint64_t part_size = (uint64_t)part_size_jlong;
    uint64_t multipart_upload_threshold = (uint64_t)multipart_upload_threshold_jlong;

    size_t initial_read_window;
    if (aws_size_t_from_java(env, &initial_read_window, initial_read_window_jlong, "Initial read window")) {
        return (jlong)NULL;
    }

    size_t memory_limit_in_bytes;
    if (aws_size_t_from_java(env, &memory_limit_in_bytes, jni_memory_limit_bytes_jlong, "Initial memory limit in bytes")) {
        return (jlong)NULL;
    }

    struct aws_retry_strategy *retry_strategy = NULL;

    if (jni_standard_retry_options != NULL) {
        struct aws_standard_retry_options retry_options;

        if (aws_standard_retry_options_from_java(env, jni_standard_retry_options, &retry_options)) {
            return (jlong)NULL;
        }

        if (retry_options.backoff_retry_options.el_group == NULL) {
            retry_options.backoff_retry_options.el_group = client_bootstrap->event_loop_group;
        }

        retry_strategy = aws_retry_strategy_new_standard(allocator, &retry_options);

        if (retry_strategy == NULL) {
            aws_jni_throw_runtime_exception(env, "Could not create retry strategy with standard-retry-options");
            return (jlong)NULL;
        }
    }
    struct aws_s3_tcp_keep_alive_options *s3_tcp_keep_alive_options = NULL;

    if (jni_s3_tcp_keep_alive_options != NULL) {
        s3_tcp_keep_alive_options = aws_mem_calloc(allocator, 1, sizeof(struct aws_s3_tcp_keep_alive_options));
        if (aws_s3_tcp_keep_alive_options_from_java(env, jni_s3_tcp_keep_alive_options, s3_tcp_keep_alive_options)) {
            aws_jni_throw_runtime_exception(env, "Could not create s3_tcp_keep_alive_options");
            return (jlong)NULL;
        }
    }

    struct aws_byte_cursor region = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_region);

    struct s3_client_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s3_client_callback_data));

    struct aws_signing_config_aws signing_config;
    struct aws_credentials *anonymous_credentials = NULL;
    AWS_ZERO_STRUCT(signing_config);
    if (java_signing_config != NULL) {
        if (aws_build_signing_config(env, java_signing_config, &callback_data->signing_config_data, &signing_config)) {
            aws_jni_throw_runtime_exception(env, "Invalid signingConfig");
            aws_mem_release(allocator, callback_data);
            return (jlong)NULL;
        }
    } else {
        anonymous_credentials = aws_credentials_new_anonymous(allocator);
        signing_config.credentials = anonymous_credentials;
    }

    if (java_s3express_provider_factory != NULL && enable_s3express) {
        /* Create a JNI factory */
        callback_data->java_s3express_provider_factory = (*env)->NewGlobalRef(env, java_s3express_provider_factory);
    }

    AWS_FATAL_ASSERT(callback_data);
    callback_data->java_s3_client = (*env)->NewGlobalRef(env, s3_client_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_tls_connection_options *tls_options = NULL;
    struct aws_tls_connection_options tls_options_storage;
    AWS_ZERO_STRUCT(tls_options_storage);
    if (jni_tls_ctx) {
        struct aws_tls_ctx *tls_ctx = (void *)jni_tls_ctx;
        tls_options = &tls_options_storage;
        aws_tls_connection_options_init_from_ctx(tls_options, tls_ctx);
    }

    struct aws_s3_client_config client_config = {
        .max_active_connections_override = max_connections,
        .region = region,
        .client_bootstrap = client_bootstrap,
        .tls_connection_options = tls_options,
        .signing_config = &signing_config,
        .part_size = part_size,
        .multipart_upload_threshold = multipart_upload_threshold,
        .throughput_target_gbps = throughput_target_gbps,
        .enable_read_backpressure = enable_read_backpressure,
        .initial_read_window = initial_read_window,
        .retry_strategy = retry_strategy,
        .shutdown_callback = s_on_s3_client_shutdown_complete_callback,
        .shutdown_callback_user_data = callback_data,
        .compute_content_md5 = compute_content_md5 ? AWS_MR_CONTENT_MD5_ENABLED : AWS_MR_CONTENT_MD5_DISABLED,
        .connect_timeout_ms = connect_timeout_ms,
        .tcp_keep_alive_options = s3_tcp_keep_alive_options,
        .enable_s3express = enable_s3express,
        .s3express_provider_override_factory =
            java_s3express_provider_factory ? s_s3express_provider_jni_factory : NULL,
        .factory_user_data = callback_data,
        .memory_limit_in_bytes = memory_limit_in_bytes,
    };

    struct aws_http_connection_monitoring_options monitoring_options;
    AWS_ZERO_STRUCT(monitoring_options);
    if (jni_monitoring_throughput_threshold_in_bytes_per_second >= 0 &&
        jni_monitoring_failure_interval_in_seconds >= 2) {
        monitoring_options.minimum_throughput_bytes_per_second =
            jni_monitoring_throughput_threshold_in_bytes_per_second;
        monitoring_options.allowable_throughput_failure_interval_seconds = jni_monitoring_failure_interval_in_seconds;
        client_config.monitoring_options = &monitoring_options;
    }

    struct aws_http_proxy_options proxy_options;
    AWS_ZERO_STRUCT(proxy_options);

    struct aws_tls_connection_options proxy_tls_conn_options;
    AWS_ZERO_STRUCT(proxy_tls_conn_options);

    aws_http_proxy_options_jni_init(
        env,
        &proxy_options,
        jni_proxy_connection_type,
        &proxy_tls_conn_options,
        jni_proxy_host,
        jni_proxy_port,
        jni_proxy_authorization_username,
        jni_proxy_authorization_password,
        jni_proxy_authorization_type,
        (struct aws_tls_ctx *)jni_proxy_tls_context);

    if (jni_proxy_host != NULL) {
        client_config.proxy_options = &proxy_options;
    }

    struct proxy_env_var_settings proxy_ev_settings;
    AWS_ZERO_STRUCT(proxy_ev_settings);

    aws_http_proxy_environment_variable_setting_jni_init(
        &proxy_ev_settings,
        jni_environment_variable_proxy_connection_type,
        jni_environment_variable_type,
        (struct aws_tls_connection_options *)jni_environment_variable_proxy_tls_connection_options);

    client_config.proxy_ev_settings = &proxy_ev_settings;

    struct aws_s3_client *client = aws_s3_client_new(allocator, &client_config);
    if (!client) {
        aws_jni_throw_runtime_exception(env, "S3Client.aws_s3_client_new: creating aws_s3_client failed");
        /* Clean up stuff */
        aws_signing_config_data_clean_up(&callback_data->signing_config_data, env);
        aws_mem_release(allocator, callback_data);
    }
    aws_credentials_release(anonymous_credentials);

    aws_retry_strategy_release(retry_strategy);

    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_region, region);

    aws_http_proxy_options_jni_clean_up(
        env, &proxy_options, jni_proxy_host, jni_proxy_authorization_username, jni_proxy_authorization_password);

    aws_mem_release(aws_jni_get_allocator(), s3_tcp_keep_alive_options);

    return (jlong)client;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_s3_S3Client_s3ClientDestroy(JNIEnv *env, jclass jni_class, jlong jni_s3_client) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_s3_client *client = (struct aws_s3_client *)jni_s3_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "S3Client.s3_client_clean_up: Invalid/null client");
        return;
    }

    aws_s3_client_release(client);
}

static void s_on_s3_client_shutdown_complete_callback(void *user_data) {
    struct s3_client_callback_data *callback = (struct s3_client_callback_data *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_S3_CLIENT, "S3 Client Shutdown Complete");
    if (callback->java_s3_client != NULL) {
        (*env)->CallVoidMethod(env, callback->java_s3_client, s3_client_properties.onShutdownComplete);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3Client.onShutdownCompete callback",
                (void *)callback->java_s3_client);
        }
    }

    // We're done with this callback data, free it.
    if (callback->java_s3express_provider_factory) {
        /* Clean up factory data if still exist */
        (*env)->DeleteGlobalRef(env, callback->java_s3express_provider_factory);
        callback->java_s3express_provider_factory = NULL;
    }
    (*env)->DeleteGlobalRef(env, callback->java_s3_client);

    aws_signing_config_data_clean_up(&callback->signing_config_data, env);

    aws_jni_release_thread_env(callback->jvm, env);
    /********** JNI ENV RELEASE **********/

    aws_mem_release(aws_jni_get_allocator(), user_data);
}

static int s_on_s3_meta_request_body_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_byte_cursor *body,
    uint64_t range_start,
    void *user_data) {
    (void)body;
    (void)range_start;
    int return_value = AWS_OP_ERR;

    uint64_t range_end = range_start + body->len;

    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    jobject jni_payload = aws_jni_byte_array_from_cursor(env, body);
    if (jni_payload == NULL) {
        /* JVM is out of memory, but native code can still have memory available, handle it and don't crash. */
        aws_jni_check_and_clear_exception(env);
        aws_jni_release_thread_env(callback_data->jvm, env);
        /********** JNI ENV RELEASE **********/
        return aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_OUT_OF_MEMORY);
    }

    jint body_response_result = 0;

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        body_response_result = (*env)->CallIntMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onResponseBody,
            jni_payload,
            range_start,
            range_end);

        if (aws_jni_get_and_clear_exception(env, &(callback_data->java_exception))) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Received exception from S3MetaRequest.onResponseBody callback",
                (void *)meta_request);
            aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
            goto cleanup;
        }

        /* The Java onResponseBody API lets users return a size for auto-incrementing the read window */
        if (body_response_result > 0) {
            aws_s3_meta_request_increment_read_window(meta_request, (uint64_t)body_response_result);
        }
    }
    return_value = AWS_OP_SUCCESS;

cleanup:
    (*env)->DeleteLocalRef(env, jni_payload);

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

    return return_value;
}

static int s_marshal_http_headers_to_buf(const struct aws_http_headers *headers, struct aws_byte_buf *out_headers_buf) {
    /* calculate initial header capacity */
    size_t headers_initial_capacity = 0;
    for (size_t header_index = 0; header_index < aws_http_headers_count(headers); ++header_index) {
        struct aws_http_header header;
        aws_http_headers_get_index(headers, header_index, &header);
        /* aws_marshal_http_headers_array_to_dynamic_buffer() impl drives this calculation */
        headers_initial_capacity += header.name.len + header.value.len + 8;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    if (aws_byte_buf_init(out_headers_buf, allocator, headers_initial_capacity)) {
        return AWS_OP_ERR;
    }

    if (aws_marshal_http_headers_to_dynamic_buffer(out_headers_buf, headers)) {
        aws_byte_buf_clean_up(out_headers_buf);
        return AWS_OP_ERR;
    }

    return AWS_OP_SUCCESS;
}

static int s_on_s3_meta_request_headers_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_http_headers *headers,
    int response_status,
    void *user_data) {
    int return_value = AWS_OP_ERR;
    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return AWS_OP_ERR;
    }

    jobject java_headers_buffer = NULL;
    struct aws_byte_buf headers_buf;
    AWS_ZERO_STRUCT(headers_buf);

    if (s_marshal_http_headers_to_buf(headers, &headers_buf)) {
        goto cleanup;
    }

    java_headers_buffer = aws_jni_direct_byte_buffer_from_raw_ptr(env, headers_buf.buffer, headers_buf.len);
    if (java_headers_buffer == NULL) {
        aws_jni_check_and_clear_exception(env);
        aws_raise_error(AWS_ERROR_JAVA_CRT_JVM_OUT_OF_MEMORY);
        goto cleanup;
    }

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        (*env)->CallVoidMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders,
            response_status,
            java_headers_buffer);

        if (aws_jni_get_and_clear_exception(env, &(callback_data->java_exception))) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Exception thrown from S3MetaRequest.onResponseHeaders callback",
                (void *)meta_request);

            aws_raise_error(AWS_ERROR_HTTP_CALLBACK_FAILURE);
            goto cleanup;
        }
    }
    return_value = AWS_OP_SUCCESS;

cleanup:
    aws_byte_buf_clean_up(&headers_buf);
    if (java_headers_buffer) {
        (*env)->DeleteLocalRef(env, java_headers_buffer);
    }

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/

    return return_value;
}

static void s_on_s3_meta_request_finish_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_s3_meta_request_result *meta_request_result,
    void *user_data) {

    (void)meta_request;

    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        struct aws_byte_buf *error_response_body = meta_request_result->error_response_body;
        struct aws_byte_cursor error_response_cursor;
        AWS_ZERO_STRUCT(error_response_cursor);
        if (error_response_body) {
            error_response_cursor = aws_byte_cursor_from_buf(error_response_body);
        }
        jbyteArray jni_payload = aws_jni_byte_array_from_cursor(env, &error_response_cursor);
        /* Only propagate java_exception if crt error code is callback failure */
        jthrowable java_exception =
            meta_request_result->error_code == AWS_ERROR_HTTP_CALLBACK_FAILURE ? callback_data->java_exception : NULL;

        jobject java_error_headers_buffer = NULL;
        struct aws_byte_buf headers_buf;
        AWS_ZERO_STRUCT(headers_buf);
        if (meta_request_result->error_response_headers) {
            /* Ignore any errors and just report the original error without headers */
            if (s_marshal_http_headers_to_buf(meta_request_result->error_response_headers, &headers_buf) ==
                AWS_OP_SUCCESS) {
                java_error_headers_buffer =
                    aws_jni_direct_byte_buffer_from_raw_ptr(env, headers_buf.buffer, headers_buf.len);
                if (java_error_headers_buffer == NULL) {
                    AWS_LOGF_ERROR(
                        AWS_LS_S3_META_REQUEST,
                        "id=%p: Ignored Exception from "
                        "S3MetaRequest.onFinished.aws_jni_direct_byte_buffer_from_raw_ptr",
                        (void *)meta_request);
                    aws_jni_check_and_clear_exception(env);
                }
            } else {
                AWS_LOGF_ERROR(
                    AWS_LS_S3_META_REQUEST,
                    "id=%p: Ignored Exception from S3MetaRequest.onFinished.s_marshal_http_headers_to_buf",
                    (void *)meta_request);
            }
        }

        (*env)->CallVoidMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onFinished,
            meta_request_result->error_code,
            meta_request_result->response_status,
            jni_payload,
            meta_request_result->validation_algorithm,
            meta_request_result->did_validate,
            java_exception,
            java_error_headers_buffer);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onFinished callback",
                (void *)meta_request);
        }
        if (jni_payload) {
            (*env)->DeleteLocalRef(env, jni_payload);
        }

        aws_byte_buf_clean_up(&headers_buf);
        if (java_error_headers_buffer) {
            (*env)->DeleteLocalRef(env, java_error_headers_buffer);
        }
    }

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/
}

static void s_on_s3_meta_request_progress_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_s3_meta_request_progress *progress,
    void *user_data) {

    (void)meta_request;

    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jobject progress_object = (*env)->NewObject(
        env,
        s3_meta_request_progress_properties.s3_meta_request_progress_class,
        s3_meta_request_progress_properties.s3_meta_request_progress_constructor_method_id);
    if ((*env)->ExceptionCheck(env) || progress_object == NULL) {
        aws_jni_throw_runtime_exception(
            env, "S3MetaRequestResponseHandler.onProgress: Failed to create S3MetaRequestProgress object.");
        goto done;
    }

    (*env)->SetLongField(
        env,
        progress_object,
        s3_meta_request_progress_properties.bytes_transferred_field_id,
        progress->bytes_transferred);
    (*env)->SetLongField(
        env, progress_object, s3_meta_request_progress_properties.content_length_field_id, progress->content_length);

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {

        (*env)->CallVoidMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onProgress,
            progress_object);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onProgress callback",
                (void *)meta_request);
        }
    }

    (*env)->DeleteLocalRef(env, progress_object);

done:

    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/
}

static void s_s3_meta_request_callback_cleanup(
    JNIEnv *env,
    struct s3_client_make_meta_request_callback_data *callback_data) {
    if (callback_data) {
        (*env)->DeleteGlobalRef(env, callback_data->java_s3_meta_request);
        (*env)->DeleteGlobalRef(env, callback_data->java_s3_meta_request_response_handler_native_adapter);
        (*env)->DeleteGlobalRef(env, callback_data->java_exception);
        aws_signing_config_data_clean_up(&callback_data->signing_config_data, env);
        aws_mem_release(aws_jni_get_allocator(), callback_data);
    }
}

static struct aws_s3_meta_request_resume_token *s_native_resume_token_from_java_new(
    JNIEnv *env,
    jobject resume_token_jni) {

    if (resume_token_jni == NULL) {
        return NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();

    jint native_type =
        (*env)->GetIntField(env, resume_token_jni, s3_meta_request_resume_token_properties.native_type_field_id);

    if (native_type != AWS_S3_META_REQUEST_TYPE_PUT_OBJECT) {
        aws_jni_throw_illegal_argument_exception(
            env, "ResumeToken: Operations other than PutObject are not supported for resume.");
        return NULL;
    }

    jlong part_size_jni =
        (*env)->GetLongField(env, resume_token_jni, s3_meta_request_resume_token_properties.part_size_field_id);
    jlong total_num_parts_jni =
        (*env)->GetLongField(env, resume_token_jni, s3_meta_request_resume_token_properties.total_num_parts_field_id);
    jlong num_parts_completed_jni = (*env)->GetLongField(
        env, resume_token_jni, s3_meta_request_resume_token_properties.num_parts_completed_field_id);

    jstring upload_id_jni =
        (*env)->GetObjectField(env, resume_token_jni, s3_meta_request_resume_token_properties.upload_id_field_id);
    if (upload_id_jni == NULL) {
        aws_jni_throw_illegal_argument_exception(env, "ResumeToken: UploadId must not be NULL.");
        return NULL;
    }

    struct aws_string *upload_id = aws_jni_new_string_from_jstring(env, upload_id_jni);

    struct aws_s3_upload_resume_token_options upload_options = {
        .part_size = (uint64_t)part_size_jni,
        .total_num_parts = (size_t)total_num_parts_jni,
        .num_parts_completed = (size_t)num_parts_completed_jni,
        .upload_id = aws_byte_cursor_from_string(upload_id),
    };

    struct aws_s3_meta_request_resume_token *resume_token =
        aws_s3_meta_request_resume_token_new_upload(allocator, &upload_options);
    aws_string_destroy(upload_id);

    return resume_token;
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_s3_S3Client_s3ClientMakeMetaRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_client,
    jobject java_s3_meta_request_jobject,
    jbyteArray jni_region,
    jint meta_request_type,
    jint checksum_location,
    jint checksum_algorithm,
    jboolean validate_response,
    jintArray jni_marshalled_validate_algorithms,
    jbyteArray jni_marshalled_message_data,
    jobject jni_http_request_body_stream,
    jbyteArray jni_request_filepath,
    jobject java_signing_config,
    jobject java_response_handler_jobject,
    jbyteArray jni_endpoint,
    jobject java_resume_token_jobject) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_s3_client *client = (struct aws_s3_client *)jni_s3_client;
    struct aws_byte_cursor request_filepath;
    AWS_ZERO_STRUCT(request_filepath);
    struct aws_s3_meta_request_resume_token *resume_token =
        s_native_resume_token_from_java_new(env, java_resume_token_jobject);
    struct aws_s3_meta_request *meta_request = NULL;
    bool success = false;
    struct aws_byte_cursor region = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_region);
    struct aws_http_message *request_message = NULL;

    struct s3_client_make_meta_request_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s3_client_make_meta_request_callback_data));
    AWS_FATAL_ASSERT(callback_data);
    struct aws_signing_config_aws signing_config;
    AWS_ZERO_STRUCT(signing_config);
    if (java_signing_config != NULL) {
        if (aws_build_signing_config(env, java_signing_config, &callback_data->signing_config_data, &signing_config)) {
            goto done;
        }
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_s3_meta_request = (*env)->NewGlobalRef(env, java_s3_meta_request_jobject);
    AWS_FATAL_ASSERT(callback_data->java_s3_meta_request != NULL);

    callback_data->java_s3_meta_request_response_handler_native_adapter =
        (*env)->NewGlobalRef(env, java_response_handler_jobject);
    AWS_FATAL_ASSERT(callback_data->java_s3_meta_request_response_handler_native_adapter != NULL);

    request_message = aws_http_message_new_request(allocator);
    AWS_FATAL_ASSERT(request_message);

    AWS_FATAL_ASSERT(
        AWS_OP_SUCCESS == aws_apply_java_http_request_changes_to_native_request(
                              env, jni_marshalled_message_data, jni_http_request_body_stream, request_message));

    if (jni_request_filepath) {
        request_filepath = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_request_filepath);
        if (request_filepath.ptr == NULL) {
            goto done;
        }
        if (request_filepath.len == 0) {
            aws_jni_throw_illegal_argument_exception(env, "Request file path cannot be empty");
            goto done;
        }
    }

    struct aws_uri endpoint;
    AWS_ZERO_STRUCT(endpoint);
    if (jni_endpoint != NULL) {
        struct aws_byte_cursor endpoint_str = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_endpoint);
        int uri_parse = aws_uri_init_parse(&endpoint, allocator, &endpoint_str);
        aws_jni_byte_cursor_from_jbyteArray_release(env, jni_endpoint, endpoint_str);
        if (uri_parse) {
            aws_jni_throw_runtime_exception(env, "S3Client.aws_s3_client_make_meta_request: failed to parse endpoint");
            goto done;
        }
    }

    struct aws_s3_checksum_config checksum_config = {
        .location = checksum_location,
        .checksum_algorithm = checksum_algorithm,
        .validate_response_checksum = validate_response,
    };

    struct aws_array_list response_checksum_list;
    AWS_ZERO_STRUCT(response_checksum_list);
    if (jni_marshalled_validate_algorithms != NULL) {
        jint *marshalled_algorithms = (*env)->GetIntArrayElements(env, jni_marshalled_validate_algorithms, NULL);
        const size_t marshalled_len = (*env)->GetArrayLength(env, jni_marshalled_validate_algorithms);
        aws_array_list_init_dynamic(&response_checksum_list, allocator, marshalled_len, sizeof(int));
        for (size_t i = 0; i < marshalled_len; ++i) {
            enum aws_s3_checksum_algorithm algorithm = (int)marshalled_algorithms[i];
            aws_array_list_push_back(&response_checksum_list, &algorithm);
        }
        checksum_config.validate_checksum_algorithms = &response_checksum_list;
    }

    struct aws_s3_meta_request_options meta_request_options = {
        .type = meta_request_type,
        .checksum_config = &checksum_config,
        .message = request_message,
        .send_filepath = request_filepath,
        .user_data = callback_data,
        .signing_config = java_signing_config ? &signing_config : NULL,
        .headers_callback = s_on_s3_meta_request_headers_callback,
        .body_callback = s_on_s3_meta_request_body_callback,
        .finish_callback = s_on_s3_meta_request_finish_callback,
        .progress_callback = s_on_s3_meta_request_progress_callback,
        .shutdown_callback = s_on_s3_meta_request_shutdown_complete_callback,
        .endpoint = jni_endpoint != NULL ? &endpoint : NULL,
        .resume_token = resume_token,
    };

    meta_request = aws_s3_client_make_meta_request(client, &meta_request_options);
    /* We are done using the list, it can be safely cleaned up now. */
    aws_array_list_clean_up(&response_checksum_list);
    if (!meta_request) {
        aws_jni_throw_runtime_exception(
            env, "S3Client.aws_s3_client_make_meta_request: creating aws_s3_meta_request failed");
        goto done;
    }

    success = true;

done:
    aws_s3_meta_request_resume_token_release(resume_token);
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_region, region);
    aws_http_message_release(request_message);
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_request_filepath, request_filepath);
    aws_uri_clean_up(&endpoint);
    if (success) {
        return (jlong)meta_request;
    }
    s_s3_meta_request_callback_cleanup(env, callback_data);
    return (jlong)0;
}

static void s_on_s3_meta_request_shutdown_complete_callback(void *user_data) {
    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    if (callback_data->java_s3_meta_request != NULL) {
        (*env)->CallVoidMethod(env, callback_data->java_s3_meta_request, s3_meta_request_properties.onShutdownComplete);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onShutdownCompete callback",
                (void *)callback_data->java_s3_meta_request);
        }
    }

    // We're done with this callback data, free it.
    JavaVM *jvm = callback_data->jvm;
    s_s3_meta_request_callback_cleanup(env, callback_data);

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_s3_S3MetaRequest_s3MetaRequestDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_meta_request) {
    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_s3_meta_request *meta_request = (struct aws_s3_meta_request *)jni_s3_meta_request;
    if (!meta_request) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        aws_jni_throw_runtime_exception(env, "S3MetaRequest.s3MetaRequestDestroy: Invalid/null meta request");
        return;
    }

    aws_s3_meta_request_release(meta_request);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_s3_S3MetaRequest_s3MetaRequestCancel(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_meta_request) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_s3_meta_request *meta_request = (struct aws_s3_meta_request *)jni_s3_meta_request;
    if (!meta_request) {
        /* It's fine if this particular function does nothing when it's called
         * after CrtResource is closed and the handle is NULL */
        return;
    }

    aws_s3_meta_request_cancel(meta_request);
}

JNIEXPORT jobject JNICALL Java_software_amazon_awssdk_crt_s3_S3MetaRequest_s3MetaRequestPause(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_meta_request) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_s3_meta_request *meta_request = (struct aws_s3_meta_request *)jni_s3_meta_request;
    if (!meta_request) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        aws_jni_throw_illegal_argument_exception(env, "S3MetaRequest.s3MetaRequestPause: Invalid/null meta request");
        return NULL;
    }

    struct aws_s3_meta_request_resume_token *resume_token = NULL;

    if (aws_s3_meta_request_pause(meta_request, &resume_token)) {
        aws_jni_throw_runtime_exception(env, "S3MetaRequest.s3MetaRequestPause: Failed to pause request");
        return NULL;
    }

    jobject resume_token_jni = NULL;
    if (resume_token != NULL) {
        resume_token_jni = (*env)->NewObject(
            env,
            s3_meta_request_resume_token_properties.s3_meta_request_resume_token_class,
            s3_meta_request_resume_token_properties.s3_meta_request_resume_token_constructor_method_id);
        if ((*env)->ExceptionCheck(env) || resume_token_jni == NULL) {
            aws_jni_throw_runtime_exception(env, "S3MetaRequest.s3MetaRequestPause: Failed to create ResumeToken.");
            goto on_done;
        }

        enum aws_s3_meta_request_type type = aws_s3_meta_request_resume_token_type(resume_token);
        if (type != AWS_S3_META_REQUEST_TYPE_PUT_OBJECT) {
            aws_jni_throw_runtime_exception(env, "S3MetaRequest.s3MetaRequestPause: Failed to convert resume token.");
            goto on_done;
        }

        (*env)->SetIntField(env, resume_token_jni, s3_meta_request_resume_token_properties.native_type_field_id, type);
        (*env)->SetLongField(
            env,
            resume_token_jni,
            s3_meta_request_resume_token_properties.part_size_field_id,
            aws_s3_meta_request_resume_token_part_size(resume_token));
        (*env)->SetLongField(
            env,
            resume_token_jni,
            s3_meta_request_resume_token_properties.total_num_parts_field_id,
            aws_s3_meta_request_resume_token_total_num_parts(resume_token));
        (*env)->SetLongField(
            env,
            resume_token_jni,
            s3_meta_request_resume_token_properties.num_parts_completed_field_id,
            aws_s3_meta_request_resume_token_num_parts_completed(resume_token));

        struct aws_byte_cursor upload_id_cur = aws_s3_meta_request_resume_token_upload_id(resume_token);
        jstring upload_id_jni = aws_jni_string_from_cursor(env, &upload_id_cur);
        (*env)->SetObjectField(
            env, resume_token_jni, s3_meta_request_resume_token_properties.upload_id_field_id, upload_id_jni);

        (*env)->DeleteLocalRef(env, upload_id_jni);
    }

on_done:
    aws_s3_meta_request_resume_token_release(resume_token);
    return resume_token_jni;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_s3_S3MetaRequest_s3MetaRequestIncrementReadWindow(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_meta_request,
    jlong increment) {

    (void)jni_class;
    aws_cache_jni_ids(env);

    struct aws_s3_meta_request *meta_request = (struct aws_s3_meta_request *)jni_s3_meta_request;
    if (!meta_request) {
        /* It's fine if this particular function does nothing when it's called
         * after CrtResource is closed and the handle is NULL */
        return;
    }

    if (increment < 0) {
        aws_jni_throw_illegal_argument_exception(
            env, "S3MetaRequest.s3MetaRequestIncrementReadWindow: Number cannot be negative");
        return;
    }

    aws_s3_meta_request_increment_read_window(meta_request, (uint64_t)increment);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

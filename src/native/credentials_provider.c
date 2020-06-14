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
#include "http_connection_manager.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/auth/credentials.h>
#include <aws/common/string.h>
#include <aws/http/connection.h>
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

struct aws_credentials_provider_shutdown_callback_data {
    JavaVM *jvm;
    struct aws_credentials_provider *provider;
    jweak java_crt_credentials_provider;
};

static void s_on_shutdown_complete(void *user_data) {
    struct aws_credentials_provider_shutdown_callback_data *callback_data = user_data;

    AWS_LOGF_DEBUG(AWS_LS_AUTH_CREDENTIALS_PROVIDER, "Credentials providers shutdown complete");

    // Tell the Java credentials providers that shutdown is done.  This lets it release its references.
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jobject java_crt_credentials_provider = (*env)->NewLocalRef(env, callback_data->java_crt_credentials_provider);
    if (java_crt_credentials_provider != NULL) {
        (*env)->CallVoidMethod(
            env, java_crt_credentials_provider, credentials_provider_properties.on_shutdown_complete_method_id);

        (*env)->DeleteLocalRef(env, java_crt_credentials_provider);
        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }

    (*env)->DeleteWeakGlobalRef(env, callback_data->java_crt_credentials_provider);

    // We're done with this callback data, free it.
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback_data);
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

    struct aws_credentials_provider_shutdown_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_shutdown_callback_data));
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
        aws_mem_release(allocator, callback_data);
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
    struct aws_credentials_provider_shutdown_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_shutdown_callback_data));
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
        aws_mem_release(allocator, callback_data);
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider chain");
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
        jbyteArray jni_proxy_host,
        jint jni_proxy_port,
        jlong jni_proxy_tls_context,
        jint jni_proxy_authorization_type,
        jbyteArray jni_proxy_authorization_username,
        jbyteArray jni_proxy_authorization_password) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_shutdown_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_shutdown_callback_data));
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
        aws_mem_release(allocator, callback_data);
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

static void s_on_get_credentials_callback(struct aws_credentials *credentials, int error_code, void *user_data) {
    (void)error_code;
    struct aws_credentials_provider_get_credentials_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jobject java_credentials = NULL;
    jbyteArray access_key_id = NULL;
    jbyteArray secret_access_key = NULL;
    jbyteArray session_token = NULL;

    if (credentials) {
        java_credentials = (*env)->NewObject(
            env, credentials_properties.credentials_class, credentials_properties.constructor_method_id);
        if (java_credentials != NULL) {
            struct aws_byte_cursor access_key_id_cursor = aws_credentials_get_access_key_id(credentials);
            access_key_id = aws_jni_byte_array_from_cursor(env, &access_key_id_cursor);

            struct aws_byte_cursor secret_access_key_cursor = aws_credentials_get_secret_access_key(credentials);
            secret_access_key = aws_jni_byte_array_from_cursor(env, &secret_access_key_cursor);

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

    if (java_credentials != NULL) {
        (*env)->DeleteLocalRef(env, access_key_id);
        (*env)->DeleteLocalRef(env, secret_access_key);
        if (session_token) {
            (*env)->DeleteLocalRef(env, session_token);
        }
        (*env)->DeleteLocalRef(env, java_credentials);
    }

    (*env)->DeleteGlobalRef(env, callback_data->java_crt_credentials_provider);
    (*env)->DeleteGlobalRef(env, callback_data->java_credentials_future);

    aws_credentials_provider_release(callback_data->provider);

    // We're done with this callback data, free it.
    aws_mem_release(aws_jni_get_allocator(), callback_data);
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
        aws_credentials_provider_release(provider);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

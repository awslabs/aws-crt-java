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
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

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

    struct aws_credentials_provider_static_options options;
    AWS_ZERO_STRUCT(options);
    options.access_key_id = aws_jni_byte_cursor_from_jbyteArray_acquire(env, access_key_id);
    options.secret_access_key = aws_jni_byte_cursor_from_jbyteArray_acquire(env, secret_access_key);
    if (session_token) {
        options.session_token = aws_jni_byte_cursor_from_jbyteArray_acquire(env, session_token);
    }

    struct aws_credentials_provider *provider = aws_credentials_provider_new_static(allocator, &options);
    if (provider == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create static credentials provider");
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, access_key_id, options.access_key_id);
    aws_jni_byte_cursor_from_jbyteArray_release(env, secret_access_key, options.secret_access_key);

    if (session_token) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, session_token, options.session_token);
    }

    return (jlong)provider;
}

struct aws_credentials_provider_default_binding {
    JavaVM *jvm;
    jobject java_client_bootstrap;
};

static void s_aws_credentials_provider_default_binding_destroy(JNIEnv *env, struct aws_credentials_provider_default_binding *binding) {
    if (binding == NULL) {
        return;
    }

    if (binding->java_client_bootstrap != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_client_bootstrap);
    }

    // We're done with this binding, free it.
    aws_mem_release(aws_jni_get_allocator(), binding);
}

static struct aws_credentials_provider_default_binding *s_aws_credentials_provider_default_binding_new(JNIEnv *env, jobject java_client_bootstrap) {
    struct aws_credentials_provider_default_binding *binding = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct aws_credentials_provider_default_binding));
    if (binding == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->java_client_bootstrap = (*env)->NewGlobalRef(env, java_client_bootstrap);
    if (binding->java_client_bootstrap == NULL) {
        goto error;
    }

    return binding;

error:

    s_aws_credentials_provider_default_binding_destroy(env, binding);

    return NULL;
}

static void s_credentials_provider_default_on_shutdown_complete(void *user_data) {
    struct aws_credentials_provider_default_binding *binding = user_data;

    AWS_LOGF_DEBUG(AWS_LS_AUTH_CREDENTIALS_PROVIDER, "Default credentials provider shutdown complete");

    JNIEnv *env = aws_jni_get_thread_env(binding->jvm);

    s_aws_credentials_provider_default_binding_destroy(env, binding);
}

JNIEXPORT jlong JNICALL
Java_software_amazon_awssdk_crt_auth_credentials_DefaultChainCredentialsProvider_defaultChainCredentialsProviderNew(
    JNIEnv *env,
    jclass jni_class,
    jobject java_crt_credentials_provider,
    jobject java_client_bootstrap) {

    (void)jni_class;
    (void)env;

    struct aws_credentials_provider_default_binding *binding = s_aws_credentials_provider_default_binding_new(env, java_client_bootstrap);
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider chain binding");
        return (jlong)NULL;
    }

    struct aws_client_bootstrap *bootstrap = (struct aws_client_bootstrap *)(*env)->CallLongMethod(env, java_client_bootstrap, crt_resource_properties.get_native_handle_method_id);
    if (bootstrap == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider - invalid bootstrap");
        return (jlong)NULL;
    }

    struct aws_credentials_provider_chain_default_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = bootstrap;
    options.shutdown_options.shutdown_callback = s_credentials_provider_default_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = binding;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_chain_default(aws_jni_get_allocator(), &options);
    if (provider == NULL) {
        s_aws_credentials_provider_default_binding_destroy(env, binding);
        aws_jni_throw_runtime_exception(env, "Failed to create default credentials provider chain");
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
    jweak java_credentials_future;
};

static void s_aws_credentials_provider_get_credentials_callback_data_destroy(JNIEnv *env, struct aws_credentials_provider_get_credentials_callback_data *callback_data) {
    if (callback_data == NULL) {
        return;
    }

    if (callback_data->java_credentials_future != NULL) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_credentials_future);
    }

    if (callback_data->provider != NULL) {
        aws_credentials_provider_release(callback_data->provider);
    }

    // We're done with this callback data, free it.
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static struct aws_credentials_provider_get_credentials_callback_data *s_aws_credentials_provider_get_credentials_callback_data_new(JNIEnv *env, struct aws_credentials_provider *provider, jobject java_credentials_future) {
    struct aws_credentials_provider_get_credentials_callback_data *callback_data = aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct aws_credentials_provider_get_credentials_callback_data));
    if (callback_data == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_credentials_future = (*env)->NewWeakGlobalRef(env, java_credentials_future);
    if (callback_data->java_credentials_future == NULL) {
        goto error;
    }

    callback_data->provider = provider;
    aws_credentials_provider_acquire(provider);

    return callback_data;

error:

    s_aws_credentials_provider_get_credentials_callback_data_destroy(env, callback_data);

    return NULL;
}

static void s_on_get_credentials_callback(struct aws_credentials *credentials, void *user_data) {
    struct aws_credentials_provider_get_credentials_callback_data *callback_data = user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    bool should_complete_normally = false;
    jobject java_credentials = NULL;
    jobject java_exception = NULL;
    jbyteArray access_key_id = NULL;
    jbyteArray secret_access_key = NULL;
    jbyteArray session_token = NULL;

    jobject java_future = (*env)->NewLocalRef(env, callback_data->java_credentials_future);
    if (java_future == NULL) {
        goto cleanup;
    }

    if (credentials) {
        java_credentials = (*env)->NewObject(
            env, credentials_properties.credentials_class, credentials_properties.constructor_method_id);
        if (java_credentials != NULL) {
            struct aws_byte_cursor access_key_id_cursor = aws_byte_cursor_from_string(credentials->access_key_id);
            access_key_id = aws_jni_byte_array_from_cursor(env, &access_key_id_cursor);
            if (access_key_id == NULL) {
                goto cleanup;
            }

            struct aws_byte_cursor secret_access_key_cursor =
                aws_byte_cursor_from_string(credentials->secret_access_key);
            secret_access_key = aws_jni_byte_array_from_cursor(env, &secret_access_key_cursor);
            if (secret_access_key == NULL) {
                goto cleanup;
            }

            if (credentials->session_token != NULL) {
                struct aws_byte_cursor session_token_cursor = aws_byte_cursor_from_string(credentials->session_token);
                session_token = aws_jni_byte_array_from_cursor(env, &session_token_cursor);
                if (session_token == NULL) {
                    goto cleanup;
                }
            }

            (*env)->SetObjectField(env, java_credentials, credentials_properties.access_key_id_field_id, access_key_id);
            (*env)->SetObjectField(
                env, java_credentials, credentials_properties.secret_access_key_field_id, secret_access_key);
            if (session_token != NULL) {
                (*env)->SetObjectField(
                    env, java_credentials, credentials_properties.session_token_field_id, session_token);
            }

            should_complete_normally = true;
        }
    }

    if (should_complete_normally) {
        (*env)->CallBooleanMethod(
                env,
                java_future,
                completable_future_properties.complete_method_id,
                java_credentials);
    } else {
        java_exception = aws_jni_create_runtime_exception(env, "getCredentials failure");

        (*env)->CallBooleanMethod(
                env,
                java_future,
                completable_future_properties.complete_exceptionally_method_id,
                java_exception);
    }


cleanup:

    if (java_exception != NULL) {
        (*env)->DeleteLocalRef(env, java_exception);
    }

    if (access_key_id != NULL) {
        (*env)->DeleteLocalRef(env, access_key_id);
    }

    if (secret_access_key != NULL) {
        (*env)->DeleteLocalRef(env, secret_access_key);
    }

    if (session_token != NULL) {
        (*env)->DeleteLocalRef(env, session_token);
    }

    if (java_credentials != NULL) {
        (*env)->DeleteLocalRef(env, java_credentials);
    }

    s_aws_credentials_provider_get_credentials_callback_data_destroy(env, callback_data);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_credentials_CredentialsProvider_credentialsProviderGetCredentials(
    JNIEnv *env,
    jclass jni_cp,
    jobject java_credentials_future,
    jlong native_credentials_provider) {
    (void)jni_cp;
    struct aws_credentials_provider *provider = (struct aws_credentials_provider *)native_credentials_provider;
    if (!provider) {
        aws_jni_throw_runtime_exception(
            env, "CredentialsProvider.credentialsProviderGetCredentials: instance should be non-null");
        return;
    }

    if (java_credentials_future == NULL) {
        aws_jni_throw_runtime_exception(
            env, "CredentialsProvider.credentialsProviderGetCredentials: called with null parameters");
        return;
    }

    struct aws_credentials_provider_get_credentials_callback_data *callback_data =
            s_aws_credentials_provider_get_credentials_callback_data_new(env, provider, java_credentials_future);
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(env, "CrtCredentialsProvider.credentialsProviderGetCredentials: failed to create callback binding");
        return;
    }

    if (aws_credentials_provider_get_credentials(provider, s_on_get_credentials_callback, callback_data)) {
        aws_jni_throw_runtime_exception(env, "CrtCredentialsProvider.credentialsProviderGetCredentials: call failure");
        s_aws_credentials_provider_get_credentials_callback_data_destroy(env, callback_data);
    }
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

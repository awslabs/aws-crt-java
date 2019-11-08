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

#include <aws/auth/credentials.h>

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

struct credentials_provider_cleanup_callback_data {
    JavaVM *jvm;
    jlong cp_addr;
    jobject java_credentials_provider;
};

JNIEXPORT jlong JNICALL
Java_software_amazon_awssdk_crt_auth_credentials_provider_StaticCredentialsProviderBuilder_staticCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jstring access_key_id,
        jstring secret_access_key,
        jstring session_token) {

    (void)jni_class;

    struct aws_byte_cursor access_key_id_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, access_key_id);
    struct aws_byte_cursor secret_access_key_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, secret_access_key);
    struct aws_byte_cursor session_token_cursor;
    AWS_ZERO_STRUCT(session_token_cursor);
    if (session_token) {
        session_token_cursor = aws_jni_byte_cursor_from_jstring_acquire(env, session_token);
    }

    struct aws_credentials_provider *provider = aws_credentials_provider_new_static(
            aws_jni_get_allocator(),
            access_key_id_cursor,
            secret_access_key_cursor,
            session_token_cursor);

    aws_jni_byte_cursor_from_jstring_release(env, access_key_id, access_key_id_cursor);
    aws_jni_byte_cursor_from_jstring_release(env, secret_access_key, secret_access_key_cursor);

    if (session_token) {
        aws_jni_byte_cursor_from_jstring_release(env, session_token, session_token_cursor);
    }

    return (jlong)provider;
}


JNIEXPORT jlong JNICALL
Java_software_amazon_awssdk_crt_auth_credentials_provider_DefaultChainCredentialsProviderBuilder_defaultChainCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jlong bootstrapHandle) {

    (void)jni_class;
    (void)env;

    struct aws_credentials_provider_chain_default_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;

    struct aws_credentials_provider *provider =
            aws_credentials_provider_new_chain_default(aws_jni_get_allocator(), &options);

    return (jlong)provider;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_auth_CredentialsProvider_credentialsProviderDestroy(
    JNIEnv *env,
    jclass jni_cp,
    jobject cp_jobject,
    jlong cp_addr) {
    (void)jni_cp;
    struct aws_credentials_provider *provider = (struct aws_credentials_provider *)cp_addr;
    if (!provider) {
        aws_jni_throw_runtime_exception(
                env, "CredentialsProvider.credentialsProviderDestroy: instance should be non-null at clean_up time");
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_cleanup_callback_data *callback_data =
            aws_mem_acquire(allocator, sizeof(struct aws_credentials_provider_cleanup_callback_data));
    callback_data->java_credentials_provider = (*env)->NewGlobalRef(env, cp_jobject);
    callback_data->cp_addr = cp_addr;

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_credentials_provider_clean_up_async(provider, s_credentials_provider_cleanup_completion_callback, callback_data);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif
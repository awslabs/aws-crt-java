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

/* callback methods needed in EventLoopGroup */
static struct { jmethodID onShutdownComplete; } s_credentials_provider_methods;

void s_cache_credentials_provider_methods(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/auth/CredentialsProvider");
    AWS_FATAL_ASSERT(cls);

    s_credentials_provider_methods.onShutdownComplete = (*env)->GetMethodID(env, cls, "onShutdownComplete", "()V");
    AWS_FATAL_ASSERT(s_credentials_provider_methods.onShutdownComplete);
}

struct aws_credentials_provider_shutdown_callback_data {
    JavaVM *jvm;
    struct aws_credentials_provider *provider;
    jobject java_credentials_provider;
};

static void s_on_shutdown_complete(void *user_data) {
    struct aws_credentials_provider_shutdown_callback_data *callback_data = user_data;

    AWS_LOGF_DEBUG(AWS_LS_AUTH_CREDENTIALS_PROVIDER, "Credentials provider shutdown complete");

    // Tell the Java credentials provider that shutdown is done.  This lets it release its references.
    JavaVM *jvm = callback_data->jvm;
    JNIEnv *env = NULL;
    /* fetch the env manually, rather than through the helper which will install an exit callback */
    (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
    (*env)->CallVoidMethod(
        env, callback_data->java_credentials_provider, s_credentials_provider_methods.onShutdownComplete);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    // Remove the ref that was probably keeping the Java event loop group alive.
    (*env)->DeleteGlobalRef(env, callback_data->java_credentials_provider);

    // We're done with this callback data, free it.
    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, callback_data);

    (*jvm)->DetachCurrentThread(jvm);
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_provider_StaticCredentialsProviderBuilder_staticCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_credentials_provider,
        jstring access_key_id,
        jstring secret_access_key,
        jstring session_token) {

    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_credentials_provider_shutdown_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_shutdown_callback_data));
    callback_data->java_credentials_provider = (*env)->NewGlobalRef(env, java_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_static_options options;
    AWS_ZERO_STRUCT(options);
    options.access_key_id = aws_jni_byte_cursor_from_jstring_acquire(env, access_key_id);
    options.secret_access_key = aws_jni_byte_cursor_from_jstring_acquire(env, secret_access_key);
    if (session_token) {
        options.session_token = aws_jni_byte_cursor_from_jstring_acquire(env, session_token);
    }
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_static(allocator, &options);

    aws_jni_byte_cursor_from_jstring_release(env, access_key_id, options.access_key_id);
    aws_jni_byte_cursor_from_jstring_release(env, secret_access_key, options.secret_access_key);

    if (session_token) {
        aws_jni_byte_cursor_from_jstring_release(env, session_token, options.session_token);
    }

    callback_data->provider = provider;

    return (jlong)provider;
}

JNIEXPORT jlong JNICALL
    Java_software_amazon_awssdk_crt_auth_credentials_provider_DefaultChainCredentialsProviderBuilder_defaultChainCredentialsProviderNew(
        JNIEnv *env,
        jclass jni_class,
        jobject java_credentials_provider,
        jlong bootstrapHandle) {

    (void)jni_class;
    (void)env;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_credentials_provider_shutdown_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_credentials_provider_shutdown_callback_data));
    callback_data->java_credentials_provider = (*env)->NewGlobalRef(env, java_credentials_provider);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_credentials_provider_chain_default_options options;
    AWS_ZERO_STRUCT(options);
    options.bootstrap = (struct aws_client_bootstrap *)bootstrapHandle;
    options.shutdown_options.shutdown_callback = s_on_shutdown_complete;
    options.shutdown_options.shutdown_user_data = callback_data;

    struct aws_credentials_provider *provider = aws_credentials_provider_new_chain_default(allocator, &options);

    callback_data->provider = provider;

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
            env, "CredentialsProvider.credentialsProviderDestroy: instance should be non-null at destruction time");
        return;
    }

    aws_credentials_provider_release(provider);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif
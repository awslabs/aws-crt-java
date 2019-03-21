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

#include <jni.h>

#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

#include "crt.h"

/* Have to wrap the native struct so we can manage string lifetime */
struct jni_tls_ctx_options {
    /* Must be first thing in the structure so casts to aws_tls_ctx_options work */
    struct aws_tls_ctx_options options;
    /* these strings get copied from java, so we don't have to pin and track references */
    struct aws_string *ca_file;
    struct aws_string *ca_path;
    struct aws_string *alpn_list;
    struct aws_string *certificate_path;
    struct aws_string *private_key_path;
    struct aws_string *pkcs12_path;
    struct aws_string *pkcs12_password;
};

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

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsNew(JNIEnv *env, jclass jni_class) {
    (void)jni_class;
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct jni_tls_ctx_options *tls =
        (struct jni_tls_ctx_options *)aws_mem_acquire(allocator, sizeof(struct jni_tls_ctx_options));
    if (!tls) {
        aws_jni_throw_runtime_exception(
            env, "TlsContextOptions.tls_ctx_options_new: Unable to allocate new jni_tls_ctx_options");
        return (jlong)NULL;
    }
    AWS_ZERO_STRUCT(*tls);
    aws_tls_ctx_options_init_default_client(&tls->options, aws_jni_get_allocator());
    return (jlong)tls;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls) {
    (void)env;
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (tls->ca_file) {
        aws_string_destroy(tls->ca_file);
    }
    if (tls->ca_path) {
        aws_string_destroy(tls->ca_path);
    }
    if (tls->alpn_list) {
        aws_string_destroy(tls->alpn_list);
    }
    if (tls->certificate_path) {
        aws_string_destroy(tls->certificate_path);
    }
    if (tls->private_key_path) {
        aws_string_destroy(tls->private_key_path);
    }
    if (tls->pkcs12_path) {
        aws_string_destroy(tls->pkcs12_path);
    }
    if (tls->pkcs12_password) {
        aws_string_destroy_secure(tls->pkcs12_password);
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, tls);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsSetMinimumTlsVersion(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jint jni_version) {
    (void)env;
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->options.minimum_tls_version = (enum aws_tls_versions)jni_version;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsOverrideDefaultTrustStoreFromPath(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_ca_file,
    jstring jni_ca_path) {
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    const char *ca_file = NULL;
    const char *ca_path = NULL;
    if (jni_ca_file) {
        tls->ca_file = aws_jni_new_string_from_jstring(env, jni_ca_file);
        ca_file = (const char*)aws_string_bytes(tls->ca_file);
    }
    if (jni_ca_path) {
        tls->ca_path = aws_jni_new_string_from_jstring(env, jni_ca_path);
        ca_path = (const char*)aws_string_bytes(tls->ca_path);
    }
    
    aws_tls_ctx_options_override_default_trust_store_from_path(&tls->options, ca_path, ca_file);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsSetAlpn(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_alpn) {
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->alpn_list = aws_jni_new_string_from_jstring(env, jni_alpn);
    aws_tls_ctx_options_set_alpn_list(&tls->options, (const char *)aws_string_bytes(tls->alpn_list));
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsInitMTLSFromPath(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_certificate_path,
    jstring jni_key_path) {
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (!jni_certificate_path || !jni_key_path) {
        aws_jni_throw_runtime_exception(env, "TlsContextOptions.tlsContextOptionsInitMTLSFromPath: certificatePath and privateKeyPath must be non-null");
        return;
    }

    tls->certificate_path = aws_jni_new_string_from_jstring(env, jni_certificate_path);
    tls->private_key_path = aws_jni_new_string_from_jstring(env, jni_key_path);
    aws_tls_ctx_options_init_client_mtls_from_path(&tls->options, aws_jni_get_allocator(), (const char*)aws_string_bytes(tls->certificate_path), (const char*)aws_string_bytes(tls->private_key_path));
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsInitMTLSPkcs12FromPath(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_pkcs12_path,
    jstring jni_pkcs12_password) {
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (!jni_pkcs12_path || !jni_pkcs12_password) {
        aws_jni_throw_runtime_exception(env, "TlsContextOptions.tlsContextOptionsInitMTLSPkcs12FromPath: pkcs12Path and pkcs12Password must be non-null");
        return;
    }

    tls->pkcs12_path = aws_jni_new_string_from_jstring(env, jni_pkcs12_path);
    tls->pkcs12_password = aws_jni_new_string_from_jstring(env, jni_pkcs12_password);
    struct aws_byte_cursor password = aws_byte_cursor_from_string(tls->pkcs12_password);
    aws_tls_ctx_options_init_client_mtls_pkcs12_from_path(&tls->options, aws_jni_get_allocator(), (const char*)aws_string_bytes(tls->pkcs12_path), &password);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsSetVerifyPeer(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jboolean jni_verify) {
    (void)env;
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->options.verify_peer = jni_verify != 0;
}

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsIsAlpnAvailable(
    JNIEnv *env,
    jclass jni_class) {
    (void)env;
    (void)jni_class;
    return aws_tls_is_alpn_available();
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

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
    struct aws_string *certificate;
    struct aws_string *private_key;
    struct aws_string *ca_root;
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
    struct jni_tls_ctx_options *tls = aws_mem_calloc(allocator, 1, sizeof(struct jni_tls_ctx_options));
    if (!tls) {
        aws_jni_throw_runtime_exception(
            env, "TlsContextOptions.tls_ctx_options_new: Unable to allocate new jni_tls_ctx_options");
        return (jlong)NULL;
    }
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

    aws_string_destroy(tls->ca_file);
    aws_string_destroy(tls->ca_path);
    aws_string_destroy(tls->alpn_list);
    aws_string_destroy(tls->certificate_path);
    aws_string_destroy(tls->private_key_path);
    aws_string_destroy(tls->pkcs12_path);
    aws_string_destroy_secure(tls->pkcs12_password);
    aws_string_destroy_secure(tls->certificate);
    aws_string_destroy_secure(tls->private_key);
    aws_string_destroy_secure(tls->ca_root);

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
        ca_file = (const char *)aws_string_bytes(tls->ca_file);
    }
    if (jni_ca_path) {
        tls->ca_path = aws_jni_new_string_from_jstring(env, jni_ca_path);
        ca_path = (const char *)aws_string_bytes(tls->ca_path);
    }

    if (aws_tls_ctx_options_override_default_trust_store_from_path(&tls->options, ca_path, ca_file)) {
        aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_override_default_trust_store_from_path failed");
    }
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsOverrideDefaultTrustStore(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_ca_root) {
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (!jni_ca_root) {
        aws_jni_throw_runtime_exception(
            env, "TlsContextOptions.tlsContextOptionsOverrideDefaultTrustStore: caRoot must be non-null");
        return;
    }

    tls->ca_root = aws_jni_new_string_from_jstring(env, jni_ca_root);
    struct aws_byte_cursor ca_cursor = aws_byte_cursor_from_string(tls->ca_root);
    if (aws_tls_ctx_options_override_default_trust_store(&tls->options, &ca_cursor)) {
        aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_override_default_trust_store failed");
    }
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
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsSetCipherPreference(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jint jni_cipher_pref) {

    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;

    if (!tls) {
        return;
    }

    if (jni_cipher_pref < 0 || AWS_IO_TLS_CIPHER_PREF_END_RANGE <= jni_cipher_pref) {
        aws_jni_throw_runtime_exception(
            env,
            "TlsContextOptions.tlsContextOptionsSetCipherPreference: TlsCipherPreference is out of range: %d",
            (int)jni_cipher_pref);
        return;
    }

    tls->options.cipher_pref = (enum aws_tls_cipher_pref)jni_cipher_pref;
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
        aws_jni_throw_runtime_exception(
            env,
            "TlsContextOptions.tlsContextOptionsInitMTLSFromPath: certificatePath and privateKeyPath must be non-null");
        return;
    }

    tls->certificate_path = aws_jni_new_string_from_jstring(env, jni_certificate_path);
    tls->private_key_path = aws_jni_new_string_from_jstring(env, jni_key_path);
    aws_tls_ctx_options_init_client_mtls_from_path(
        &tls->options,
        aws_jni_get_allocator(),
        (const char *)aws_string_bytes(tls->certificate_path),
        (const char *)aws_string_bytes(tls->private_key_path));
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsInitMTLS(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_certificate,
    jstring jni_key) {
    (void)jni_class;
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (!jni_certificate || !jni_key) {
        aws_jni_throw_runtime_exception(
            env,
            "TlsContextOptions.tlsContextOptionsInitMTLS: certificate and privateKey must be non-null");
        return;
    }

    tls->certificate = aws_jni_new_string_from_jstring(env, jni_certificate);
    tls->private_key = aws_jni_new_string_from_jstring(env, jni_key);

    struct aws_byte_cursor cert_cursor = aws_byte_cursor_from_string(tls->certificate);
    struct aws_byte_cursor key_cursor = aws_byte_cursor_from_string(tls->private_key);

    aws_tls_ctx_options_init_client_mtls(
        &tls->options,
        aws_jni_get_allocator(),
        &cert_cursor,
        &key_cursor);
}

#if defined(__APPLE__)

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
        aws_jni_throw_runtime_exception(
            env,
            "TlsContextOptions.tlsContextOptionsInitMTLSPkcs12FromPath: pkcs12Path and pkcs12Password must be "
            "non-null");
        return;
    }

    tls->pkcs12_path = aws_jni_new_string_from_jstring(env, jni_pkcs12_path);
    tls->pkcs12_password = aws_jni_new_string_from_jstring(env, jni_pkcs12_password);
    struct aws_byte_cursor password = aws_byte_cursor_from_string(tls->pkcs12_password);
    aws_tls_ctx_options_init_client_mtls_pkcs12_from_path(
        &tls->options, aws_jni_get_allocator(), (const char *)aws_string_bytes(tls->pkcs12_path), &password);
}

#endif /* __APPLE__ */

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

JNIEXPORT
jboolean JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsIsCipherPreferenceSupported(
    JNIEnv *env,
    jclass jni_class,
    jint jni_cipher_pref) {

    (void)env;
    (void)jni_class;

    if (jni_cipher_pref < 0 || AWS_IO_TLS_CIPHER_PREF_END_RANGE <= jni_cipher_pref) {
        aws_jni_throw_runtime_exception(
            env,
            "TlsContextOptions.tlsContextOptionsSetCipherPreference: TlsCipherPreference is out of range: %d",
            (int)jni_cipher_pref);
        return false;
    }

    return aws_tls_is_cipher_pref_supported((enum aws_tls_cipher_pref)jni_cipher_pref);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

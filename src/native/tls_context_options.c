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

static void s_jni_tls_ctx_options_destroy(struct jni_tls_ctx_options *tls) {
    if (tls == NULL) {
        return;
    }

    aws_string_destroy(tls->ca_file);
    aws_string_destroy(tls->ca_path);
    aws_string_destroy(tls->alpn_list);
    aws_string_destroy(tls->certificate_path);
    aws_string_destroy(tls->private_key_path);
    aws_string_destroy(tls->pkcs12_path);
    aws_string_destroy_secure(tls->pkcs12_password);
    aws_string_destroy(tls->certificate);
    aws_string_destroy_secure(tls->private_key);
    aws_string_destroy(tls->ca_root);

    aws_tls_ctx_options_clean_up(&tls->options);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, tls);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsNew(
    JNIEnv *env,
    jclass jni_class,
    jint jni_min_tls_version,
    jint jni_cipher_pref,
    jstring jni_alpn,
    jstring jni_certificate,
    jstring jni_private_key,
    jstring jni_cert_path,
    jstring jni_key_path,
    jstring jni_ca,
    jstring jni_ca_filepath,
    jstring jni_ca_dirpath,
    jboolean jni_verify_peer,
    jstring jni_pkcs12_path,
    jstring jni_pkcs12_password) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct jni_tls_ctx_options *tls = aws_mem_calloc(allocator, 1, sizeof(struct jni_tls_ctx_options));
    AWS_FATAL_ASSERT(tls);
    aws_tls_ctx_options_init_default_client(&tls->options, allocator);

    /* Certs or paths will cause an init, which overwrites other fields, so do those first */
    if (jni_certificate && jni_private_key) {
        tls->certificate = aws_jni_new_string_from_jstring(env, jni_certificate);
        if (!tls->certificate) {
            aws_jni_throw_runtime_exception(env, "failed to get certificate string");
            goto on_error;
        }
        tls->private_key = aws_jni_new_string_from_jstring(env, jni_private_key);
        if (!tls->private_key) {
            aws_jni_throw_runtime_exception(env, "failed to get privateKey string");
            goto on_error;
        }

        struct aws_byte_cursor cert_cursor = aws_byte_cursor_from_string(tls->certificate);
        struct aws_byte_cursor key_cursor = aws_byte_cursor_from_string(tls->private_key);

        if (aws_tls_ctx_options_init_client_mtls(&tls->options, allocator, &cert_cursor, &key_cursor)) {
            aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_init_client_mtls failed");
            goto on_error;
        }
    } else if (jni_cert_path && jni_key_path) {
        tls->certificate_path = aws_jni_new_string_from_jstring(env, jni_cert_path);
        if (!tls->certificate_path) {
            aws_jni_throw_runtime_exception(env, "failed to get certificatePath string");
            goto on_error;
        }
        tls->private_key_path = aws_jni_new_string_from_jstring(env, jni_key_path);
        if (!tls->private_key_path) {
            aws_jni_throw_runtime_exception(env, "failed to get privateKeyPath string");
            goto on_error;
        }

        if (aws_tls_ctx_options_init_client_mtls_from_path(
                &tls->options,
                allocator,
                aws_string_c_str(tls->certificate_path),
                aws_string_c_str(tls->private_key_path))) {
            aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_init_client_mtls_from_path failed");
            goto on_error;
        }
    }

    if (jni_ca) {
        tls->ca_root = aws_jni_new_string_from_jstring(env, jni_ca);
        if (!tls->ca_root) {
            aws_jni_throw_runtime_exception(env, "failed to get caRoot string");
            goto on_error;
        }
        struct aws_byte_cursor ca_cursor = aws_byte_cursor_from_string(tls->ca_root);
        if (aws_tls_ctx_options_override_default_trust_store(&tls->options, &ca_cursor)) {
            aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_override_default_trust_store failed");
            goto on_error;
        }
    } else if (jni_ca_filepath || jni_ca_dirpath) {
        const char *ca_file = NULL;
        const char *ca_path = NULL;
        if (jni_ca_filepath) {
            tls->ca_file = aws_jni_new_string_from_jstring(env, jni_ca_filepath);
            if (!tls->ca_file) {
                aws_jni_throw_runtime_exception(env, "failed to get caFile string");
                goto on_error;
            }

            ca_file = aws_string_c_str(tls->ca_file);
        }
        if (jni_ca_dirpath) {
            tls->ca_path = aws_jni_new_string_from_jstring(env, jni_ca_dirpath);
            if (!tls->ca_path) {
                aws_jni_throw_runtime_exception(env, "failed to get caPath string");
                goto on_error;
            }

            ca_path = aws_string_c_str(tls->ca_path);
        }

        if (aws_tls_ctx_options_override_default_trust_store_from_path(&tls->options, ca_path, ca_file)) {
            aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_override_default_trust_store_from_path failed");
            goto on_error;
        }
    }

#if defined(__APPLE__)
    if (jni_pkcs12_path && jni_pkcs12_password) {
        tls->pkcs12_path = aws_jni_new_string_from_jstring(env, jni_pkcs12_path);
        if (!tls->pkcs12_path) {
            aws_jni_throw_runtime_exception(env, "failed to get pkcs12Path string");
            goto on_error;
        }
        tls->pkcs12_password = aws_jni_new_string_from_jstring(env, jni_pkcs12_password);
        if (!tls->pkcs12_password) {
            aws_jni_throw_runtime_exception(env, "failed to get pkcs12Password string");
            goto on_error;
        }

        struct aws_byte_cursor password = aws_byte_cursor_from_string(tls->pkcs12_password);
        if (aws_tls_ctx_options_init_client_mtls_pkcs12_from_path(
                &tls->options, allocator, aws_string_c_str(tls->pkcs12_path), &password)) {
            aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_init_client_mtls_pkcs12_from_path failed");
            goto on_error;
        }
    }
#endif
    (void)jni_pkcs12_path;
    (void)jni_pkcs12_password;

    /* apply the rest of the non-init settings */
    tls->options.minimum_tls_version = (enum aws_tls_versions)jni_min_tls_version;
    tls->options.cipher_pref = (enum aws_tls_cipher_pref)jni_cipher_pref;
    tls->options.verify_peer = jni_verify_peer != 0;

    if (jni_alpn) {
        tls->alpn_list = aws_jni_new_string_from_jstring(env, jni_alpn);
        if (!tls->alpn_list) {
            aws_jni_throw_runtime_exception(env, "failed to get alpnList string");
            goto on_error;
        }

        if (aws_tls_ctx_options_set_alpn_list(&tls->options, aws_string_c_str(tls->alpn_list))) {
            aws_jni_throw_runtime_exception(env, "aws_tls_ctx_options_set_alpn_list failed");
            goto on_error;
        }
    }

    return (jlong)tls;

on_error:

    s_jni_tls_ctx_options_destroy(tls);

    return (jlong)0;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_io_TlsContextOptions_tlsContextOptionsDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls) {
    (void)env;
    (void)jni_class;

    s_jni_tls_ctx_options_destroy((struct jni_tls_ctx_options *)jni_tls);
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

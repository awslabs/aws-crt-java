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
#   ifdef __clang__
#       pragma clang diagnostic push
#       pragma clang diagnostic ignored "-Wpointer-to-int-cast"
#   else
#       pragma GCC diagnostic push
#       pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#   endif
#endif

JNIEXPORT
jlong JNICALL
    Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1new(JNIEnv *env, jclass jni_tls_ctx_options) {
    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct jni_tls_ctx_options *tls =
        (struct jni_tls_ctx_options *)aws_mem_acquire(allocator, sizeof(struct jni_tls_ctx_options));
    if (!tls) {
        aws_jni_throw_runtime_exception(
            env, "TlsContextOptions.tls_ctx_options_new: Unable to allocate new jni_tls_ctx_options");
        return (jlong)NULL;
    }
    AWS_ZERO_STRUCT(*tls);
    aws_tls_ctx_options_init_default_client(&tls->options);
    return (jlong)tls;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1clean_1up(
    JNIEnv *env,
    jclass jni_tls_ctx_options,
    jlong jni_tls) {
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
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1minimum_1tls_1version(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jint jni_version) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->options.minimum_tls_version = (enum aws_tls_versions)jni_version;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1ca_1file(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_ca_file) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (!jni_ca_file) {
        return;
    }

    tls->ca_file = aws_jni_new_string_from_jstring(env, jni_ca_file);
    tls->options.ca_file = (const char *)aws_string_bytes(tls->ca_file);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1ca_1path(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_ca_path) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    if (!jni_ca_path) {
        return;
    }

    tls->ca_path = aws_jni_new_string_from_jstring(env, jni_ca_path);
    tls->options.ca_path = (const char *)aws_string_bytes(tls->ca_path);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1alpn(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_alpn) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->alpn_list = aws_jni_new_string_from_jstring(env, jni_alpn);
    tls->options.alpn_list = (const char *)aws_string_bytes(tls->alpn_list);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1certificate_1path(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_certificate_path) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->certificate_path = aws_jni_new_string_from_jstring(env, jni_certificate_path);
    tls->options.certificate_path = (const char *)aws_string_bytes(tls->certificate_path);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1private_1key_1path(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_key_path) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->private_key_path = aws_jni_new_string_from_jstring(env, jni_key_path);
    tls->options.private_key_path = (const char *)aws_string_bytes(tls->private_key_path);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1pkcs12_1path(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_pkcs12_path) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->pkcs12_path = aws_jni_new_string_from_jstring(env, jni_pkcs12_path);
    tls->options.pkcs12_path = (const char *)aws_string_bytes(tls->pkcs12_path);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1pkcs12_1password(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jstring jni_pkcs12_password) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->pkcs12_password = aws_jni_new_string_from_jstring(env, jni_pkcs12_password);
    tls->options.pkcs12_password = (const char *)aws_string_bytes(tls->pkcs12_password);
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1set_1verify_1peer(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_tls,
    jboolean jni_verify) {
    struct jni_tls_ctx_options *tls = (struct jni_tls_ctx_options *)jni_tls;
    if (!tls) {
        return;
    }

    tls->options.verify_peer = jni_verify != 0;
}

JNIEXPORT
jboolean JNICALL
    Java_software_amazon_awssdk_crt_TlsContextOptions_tls_1options_1is_1alpn_1available(JNIEnv *env, jclass jni_class) {
    return aws_tls_is_alpn_available();
}

#if UINTPTR_MAX == 0xffffffff
#    ifdef __clang__
#        pragma clang diagnostic pop
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

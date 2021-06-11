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

#include <aws/auth/credentials.h>
#include <aws/cal/ecc.h>

#include "credentials.h"
#include "crt.h"

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
void JNICALL
    Java_software_amazon_awssdk_crt_cal_EccKeyPair_eccKeyPairRelease(JNIEnv *env, jclass jni_ekp, jlong ekp_addr) {
    (void)env;
    (void)jni_ekp;
    struct aws_ecc_key_pair *key_pair = (struct aws_ecc_key_pair *)ekp_addr;

    aws_ecc_key_pair_release(key_pair);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_cal_EccKeyPair_eccKeyPairNewFromCredentials(
    JNIEnv *env,
    jclass jni_class,
    jobject credentials,
    jint curve) {

    (void)jni_class;

    struct aws_credentials *native_credentials = aws_credentials_new_from_java_credentials(env, credentials);
    if (native_credentials == NULL) {
        return (jlong)0;
    }

    enum aws_ecc_curve_name curve_name = curve;

    struct aws_ecc_key_pair *key_pair = NULL;

    switch (curve_name) {
        case AWS_CAL_ECDSA_P256:
            key_pair =
                aws_ecc_key_pair_new_ecdsa_p256_key_from_aws_credentials(aws_jni_get_allocator(), native_credentials);
            break;

        default:
            break;
    }

    aws_credentials_release(native_credentials);

    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));

    return (jlong)key_pair;
}

#define SIGNATURE_SIZE_OVERESTIMATE 128

JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_cal_EccKeyPair_eccKeyPairSignMessage(
    JNIEnv *env,
    jclass jni_class,
    jlong ekp_addr,
    jbyteArray message) {

    (void)jni_class;

    struct aws_ecc_key_pair *key_pair = (struct aws_ecc_key_pair *)ekp_addr;

    struct aws_byte_buf signature_buffer;
    AWS_ZERO_STRUCT(signature_buffer);

    if (aws_byte_buf_init(&signature_buffer, aws_jni_get_allocator(), SIGNATURE_SIZE_OVERESTIMATE)) {
        aws_jni_throw_runtime_exception(env, "EccKeyPair.eccKeyPairSignMessage: failed to initialize signature buffer");
        return NULL;
    }

    struct aws_byte_cursor message_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, message);
    if (message_cursor.ptr == NULL) {
        aws_jni_throw_runtime_exception(env, "EccKeyPair.eccKeyPairSignMessage: failed to pin message bytes");
        return NULL;
    }

    jbyteArray signature = NULL;
    if (aws_ecc_key_pair_sign_message(key_pair, &message_cursor, &signature_buffer)) {
        aws_jni_throw_runtime_exception(env, "EccKeyPair.eccKeyPairSignMessage: failed to sign message");
    } else {
        struct aws_byte_cursor signature_cursor = aws_byte_cursor_from_buf(&signature_buffer);
        signature = aws_jni_byte_array_from_cursor(env, &signature_cursor);
    }

    aws_jni_byte_cursor_from_jbyteArray_release(env, message, message_cursor);
    aws_byte_buf_clean_up(&signature_buffer);

    return signature;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

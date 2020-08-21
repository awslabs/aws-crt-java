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

JNIEXPORT
jbyteArray JNICALL Java_software_amazon_awssdk_crt_cal_EccKeyPair_eccKeyPairGetPrivateKeyS(
    JNIEnv *env,
    jclass jni_class,
    jlong ekp_addr) {

    (void)env;
    (void)jni_class;

    struct aws_ecc_key_pair *key_pair = (struct aws_ecc_key_pair *)ekp_addr;

    struct aws_byte_cursor private_key_cursor;
    AWS_ZERO_STRUCT(private_key_cursor);

    aws_ecc_key_pair_get_private_key(key_pair, &private_key_cursor);
    if (private_key_cursor.len == 0) {
        return NULL;
    }

    return aws_jni_byte_array_from_cursor(env, &private_key_cursor);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

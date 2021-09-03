/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/io/pkcs11.h>

#include "crt.h"

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_io_Pkcs11Lib_pkcs11LibNew(
    JNIEnv *env,
    jclass jni_class,
    jstring jni_path,
    jboolean jni_omit_initialize) {

    (void)jni_class;

    struct aws_pkcs11_lib *pkcs11_lib = NULL;
    struct aws_pkcs11_lib_options options;
    AWS_ZERO_STRUCT(options);

    /* read jni args into C options */

    options.filename = aws_jni_byte_cursor_from_jstring_acquire(env, jni_path);
    if (options.filename.ptr == NULL) {
        goto cleanup;
    }

    options.omit_initialize = jni_omit_initialize != 0;

    /* create aws_pkcs11_lib */
    pkcs11_lib = aws_pkcs11_lib_new(aws_jni_get_allocator(), &options);
    if (pkcs11_lib == NULL) {
        aws_jni_throw_runtime_exception(env, "Pkcs11Lib() failed.");
        goto cleanup;
    }

cleanup:
    /* clean up, whether or not we were successful */
    aws_jni_byte_cursor_from_jstring_release(env, jni_path, options.filename);

    return (jlong)pkcs11_lib;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_io_Pkcs11Lib_pkcs11LibRelease(JNIEnv *env, jclass jni_class, jlong jni_pkcs11_lib) {

    (void)env;
    (void)jni_class;

    struct aws_pkcs11_lib *pkcs11_lib = (struct aws_pkcs11_lib *)jni_pkcs11_lib;
    aws_pkcs11_lib_release(pkcs11_lib);
}

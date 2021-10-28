/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/io/pkcs11.h>

#include "crt.h"

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'aws_pkcs11_lib *' */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_io_Pkcs11Lib_pkcs11LibNew(
    JNIEnv *env,
    jclass jni_class,
    jstring jni_filename,
    jint jni_initialize_finalize_behavior) {

    (void)jni_class;

    struct aws_pkcs11_lib *pkcs11_lib = NULL;
    struct aws_pkcs11_lib_options options;
    AWS_ZERO_STRUCT(options);

    /* read jni args into C options */

    /* filename is required in Java binding
     * (it's optional in C because user could link their PKCS#11 lib statically,
     * but that's not happening in Java) */
    options.filename = aws_jni_byte_cursor_from_jstring_acquire(env, jni_filename);
    if (options.filename.ptr == NULL) {
        goto cleanup;
    }

    options.initialize_finalize_behavior = jni_initialize_finalize_behavior;

    /* create aws_pkcs11_lib */
    pkcs11_lib = aws_pkcs11_lib_new(aws_jni_get_allocator(), &options);
    if (pkcs11_lib == NULL) {
        aws_jni_throw_runtime_exception(env, "Pkcs11Lib() failed.");
        goto cleanup;
    }

cleanup:
    /* clean up, whether or not we were successful */
    aws_jni_byte_cursor_from_jstring_release(env, jni_filename, options.filename);

    return (jlong)pkcs11_lib;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_io_Pkcs11Lib_pkcs11LibRelease(JNIEnv *env, jclass jni_class, jlong jni_pkcs11_lib) {

    (void)env;
    (void)jni_class;

    struct aws_pkcs11_lib *pkcs11_lib = (struct aws_pkcs11_lib *)jni_pkcs11_lib;
    aws_pkcs11_lib_release(pkcs11_lib);
}

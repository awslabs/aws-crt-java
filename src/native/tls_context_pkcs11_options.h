#ifndef AWS_JNI_CRT_TLS_CONTEXT_PKCS11_OPTIONS_H
#define AWS_JNI_CRT_TLS_CONTEXT_PKCS11_OPTIONS_H
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

struct aws_tls_ctx_pkcs11_options;

/* Create a aws_tls_ctx_pkcs11_options from a TlsContextPkcs11Options java object.
 * All values are copied out of the Java object and stored in this allocation,
 * there's no need to keep the java object around.
 * This MUST be destroyed via aws_pkcs11_tls_options_from_java_destroy().
 * Returns NULL and throws a java exception if something goes wrong. */
struct aws_tls_ctx_pkcs11_options *aws_tls_ctx_pkcs11_options_from_java_new(JNIEnv *env, jobject options_jni);

void aws_tls_ctx_pkcs11_options_from_java_destroy(struct aws_tls_ctx_pkcs11_options *options);

#endif /* AWS_JNI_CRT_TLS_CONTEXT_PKCS11_OPTIONS_H */

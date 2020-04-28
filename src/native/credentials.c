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

#include "crt.h"
#include "java_class_ids.h"

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

struct aws_credentials *aws_credentials_new_from_java_credentials(JNIEnv *env, jobject java_credentials) {
    if (java_credentials == NULL) {
        return NULL;
    }

    jbyteArray access_key_id =
        (*env)->GetObjectField(env, java_credentials, credentials_properties.access_key_id_field_id);
    jbyteArray secret_access_key =
        (*env)->GetObjectField(env, java_credentials, credentials_properties.secret_access_key_field_id);
    jbyteArray session_token =
        (*env)->GetObjectField(env, java_credentials, credentials_properties.session_token_field_id);

    if (access_key_id == NULL || secret_access_key == NULL) {
        return NULL;
    }

    struct aws_credentials *credentials = NULL;

    struct aws_byte_cursor access_key_id_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, access_key_id);
    struct aws_byte_cursor secret_access_key_cursor =
        aws_jni_byte_cursor_from_jbyteArray_acquire(env, secret_access_key);

    struct aws_byte_cursor session_token_cursor;
    AWS_ZERO_STRUCT(session_token_cursor);
    if (session_token != NULL) {
        session_token_cursor = aws_jni_byte_cursor_from_jbyteArray_acquire(env, session_token);
    }

    credentials = aws_credentials_new(
        aws_jni_get_allocator(), access_key_id_cursor, secret_access_key_cursor, session_token_cursor, UINT64_MAX);

    aws_jni_byte_cursor_from_jbyteArray_release(env, access_key_id, access_key_id_cursor);
    aws_jni_byte_cursor_from_jbyteArray_release(env, secret_access_key, secret_access_key_cursor);
    if (session_token != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, session_token, session_token_cursor);
    }

    return credentials;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

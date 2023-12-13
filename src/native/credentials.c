/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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

    jlong expiration_timepoint_secs =
        (*env)->GetLongField(env, java_credentials, credentials_properties.expiration_field_id);

    if (access_key_id == NULL && secret_access_key == NULL) {
        return aws_credentials_new_anonymous(aws_jni_get_allocator());
    }

    if (access_key_id == NULL || secret_access_key == NULL) {
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        aws_jni_throw_illegal_argument_exception(
            env,
            "Aws_credentials_new_from_java_credentials: Both access_key_id and secret_access_key must be either null "
            "or non-null.");
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
        aws_jni_get_allocator(),
        access_key_id_cursor,
        secret_access_key_cursor,
        session_token_cursor,
        (uint64_t)expiration_timepoint_secs);

    aws_jni_byte_cursor_from_jbyteArray_release(env, access_key_id, access_key_id_cursor);
    aws_jni_byte_cursor_from_jbyteArray_release(env, secret_access_key, secret_access_key_cursor);
    if (session_token != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, session_token, session_token_cursor);
    }

   (*env)->DeleteLocalRef(env, access_key_id);
   (*env)->DeleteLocalRef(env, secret_access_key);
   (*env)->DeleteLocalRef(env, session_token);

    return credentials;
}

jobject aws_java_credentials_from_native_new(JNIEnv *env, const struct aws_credentials *credentials) {

    jobject java_credentials = NULL;
    jbyteArray access_key_id = NULL;
    jbyteArray secret_access_key = NULL;
    jbyteArray session_token = NULL;
    java_credentials =
        (*env)->NewObject(env, credentials_properties.credentials_class, credentials_properties.constructor_method_id);
    if (java_credentials != NULL) {

        struct aws_byte_cursor access_key_id_cursor = aws_credentials_get_access_key_id(credentials);
        if (access_key_id_cursor.len > 0) {
            access_key_id = aws_jni_byte_array_from_cursor(env, &access_key_id_cursor);
        }

        struct aws_byte_cursor secret_access_key_cursor = aws_credentials_get_secret_access_key(credentials);
        if (secret_access_key_cursor.len > 0) {
            secret_access_key = aws_jni_byte_array_from_cursor(env, &secret_access_key_cursor);
        }

        struct aws_byte_cursor session_token_cursor = aws_credentials_get_session_token(credentials);
        if (session_token_cursor.len > 0) {
            session_token = aws_jni_byte_array_from_cursor(env, &session_token_cursor);
        }

        (*env)->SetObjectField(env, java_credentials, credentials_properties.access_key_id_field_id, access_key_id);
        (*env)->SetObjectField(
            env, java_credentials, credentials_properties.secret_access_key_field_id, secret_access_key);
        if (session_token != NULL) {
            (*env)->SetObjectField(env, java_credentials, credentials_properties.session_token_field_id, session_token);
        }
        (*env)->SetLongField(
            env,
            java_credentials,
            credentials_properties.expiration_field_id,
            (jlong)aws_credentials_get_expiration_timepoint_seconds(credentials));
    } else {
        return NULL;
    }

    if (access_key_id) {
        (*env)->DeleteLocalRef(env, access_key_id);
    }
    if (secret_access_key) {
        (*env)->DeleteLocalRef(env, secret_access_key);
    }
    if (session_token) {
        (*env)->DeleteLocalRef(env, session_token);
    }

    return java_credentials;
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

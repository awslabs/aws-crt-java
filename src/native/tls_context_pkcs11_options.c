/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "tls_context_pkcs11_options.h"

#include "crt.h"
#include "java_class_ids.h"

#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

/* Contains aws_tls_ctx_pkcs11_options, plus values copied from
 * the TlsContextPkcs11Options java object */
struct aws_tls_ctx_pkcs11_options_binding {
    struct aws_tls_ctx_pkcs11_options options;

    struct aws_string *user_pin;
    struct aws_string *token_label;
    struct aws_string *private_key_object_label;
    struct aws_string *cert_file_path;
    struct aws_string *cert_file_contents;

    uint32_t slot_id;
};

void aws_tls_ctx_pkcs11_options_from_java_destroy(struct aws_tls_ctx_pkcs11_options *options) {
    if (options == NULL) {
        return;
    }

    struct aws_tls_ctx_pkcs11_options_binding *binding =
        AWS_CONTAINER_OF(options, struct aws_tls_ctx_pkcs11_options_binding, options);

    aws_string_destroy_secure(binding->user_pin);
    aws_string_destroy(binding->token_label);
    aws_string_destroy(binding->private_key_object_label);
    aws_string_destroy(binding->cert_file_path);
    aws_string_destroy(binding->cert_file_contents);

    aws_mem_release(aws_jni_get_allocator(), binding);
}

/* Helper for processing optional strings.
 * If false is returned then a java exception has occurred */
static bool s_read_optional_string(
    JNIEnv *env,
    jobject options_jni,
    jfieldID jstring_field_id,
    struct aws_string **out_string,
    struct aws_byte_cursor *out_cursor) {

    /* Check the field in TlsContextPkcs11Options.
     * If it's NULL then we're all done, the user didn't set that optional string */
    jstring field = (*env)->GetObjectField(env, options_jni, jstring_field_id);
    if (field == NULL) {
        return true;
    }

    struct aws_string *value = aws_jni_new_string_from_jstring(env, field);
    if (value == NULL) {
        return false;
    }

    *out_string = value;
    *out_cursor = aws_byte_cursor_from_string(value);
    return true;
}

struct aws_tls_ctx_pkcs11_options *aws_tls_ctx_pkcs11_options_from_java_new(JNIEnv *env, jobject options_jni) {
    struct aws_tls_ctx_pkcs11_options_binding *binding =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct aws_tls_ctx_pkcs11_options_binding));

    /* pkcs11_lib is required */
    jobject pkcs11_lib_jni = (*env)->GetObjectField(env, options_jni, tls_context_pkcs11_options_properties.pkcs11Lib);
    if (pkcs11_lib_jni == NULL) {
        aws_jni_throw_null_pointer_exception(env, "Pkcs11Lib is null");
        goto error;
    }
    binding->options.pkcs11_lib =
        (void *)(*env)->CallLongMethod(env, pkcs11_lib_jni, crt_resource_properties.get_native_handle_method_id);
    if (binding->options.pkcs11_lib == NULL) {
        aws_jni_throw_null_pointer_exception(env, "Pkcs11Lib.getNativeHandle() returned null");
        goto error;
    }

    /* user_pin is optional String */
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.userPin,
            &binding->user_pin,
            &binding->options.user_pin)) {
        goto error;
    }

    /* slot_id is optional Integer */
    jobject slot_id_jni = (*env)->GetObjectField(env, options_jni, tls_context_pkcs11_options_properties.slotId);
    if (slot_id_jni != NULL) {
        jint slot_id_value = (*env)->CallIntMethod(env, slot_id_jni, integer_properties.int_value_method_id);
        if ((*env)->ExceptionCheck(env)) {
            goto error;
        }
        if (slot_id_value < 0) {
            aws_jni_throw_illegal_argument_exception(env, "PKCS#11 slot ID cannot be negative");
            goto error;
        }
        binding->slot_id = (uint32_t)slot_id_value;
        binding->options.slot_id = &binding->slot_id;
    }

    /* token_label is optional String */
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.tokenLabel,
            &binding->token_label,
            &binding->options.token_label)) {
        goto error;
    }

    /* private_key_object_label is optional String */
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.privateKeyObjectLabel,
            &binding->private_key_object_label,
            &binding->options.private_key_object_label)) {
        goto error;
    }

    /* cert_file_path is optional String */
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.certificateFilePath,
            &binding->cert_file_path,
            &binding->options.cert_file_path)) {
        goto error;
    }

    /* binding->cert_file_contents is optional String */
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.certificateFileContents,
            &binding->cert_file_contents,
            &binding->options.cert_file_contents)) {
        goto error;
    }

    /* success! */
    return &binding->options;

error:
    aws_tls_ctx_pkcs11_options_from_java_destroy(&binding->options);
    return NULL;
}

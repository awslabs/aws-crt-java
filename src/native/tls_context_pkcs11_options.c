/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "tls_context_pkcs11_options.h"

#include "crt.h"
#include "java_class_ids.h"

#include <aws/common/string.h>
#include <aws/io/pkcs11.h>
#include <aws/io/tls_channel_handler.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to pointer */
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

/* Contains aws_tls_ctx_pkcs11_options, plus values copied from
 * the TlsContextPkcs11Options java object */
struct aws_tls_ctx_pkcs11_options_binding {
    struct aws_tls_ctx_pkcs11_options options;

    struct aws_string *user_pin;
    struct aws_string *token_label;
    struct aws_string *private_key_object_label;
    struct aws_string *cert_file_path;
    struct aws_string *cert_file_contents;

    uint64_t slot_id;
};

void aws_tls_ctx_pkcs11_options_from_java_destroy(struct aws_tls_ctx_pkcs11_options *options) {
    if (options == NULL) {
        return;
    }

    struct aws_tls_ctx_pkcs11_options_binding *binding =
        AWS_CONTAINER_OF(options, struct aws_tls_ctx_pkcs11_options_binding, options);

    aws_pkcs11_lib_release(binding->options.pkcs11_lib);
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

    jlong pkcs11_lib_handle =
        (*env)->CallLongMethod(env, pkcs11_lib_jni, crt_resource_properties.get_native_handle_method_id);
    if (pkcs11_lib_handle == 0) {
        aws_jni_throw_null_pointer_exception(env, "Pkcs11Lib.getNativeHandle() returned null");
        goto error;
    }

    /* don't forget ref-counting to keep C object alive */
    binding->options.pkcs11_lib = aws_pkcs11_lib_acquire((void *)pkcs11_lib_handle);

    /* user_pin is optional String */
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.userPin,
            &binding->user_pin,
            &binding->options.user_pin)) {
        goto error;
    }

    /* slot_id is optional Long */
    jobject slot_id_jni = (*env)->GetObjectField(env, options_jni, tls_context_pkcs11_options_properties.slotId);
    if (slot_id_jni != NULL) {
        jlong slot_id_value = (*env)->CallLongMethod(env, slot_id_jni, boxed_long_properties.long_value_method_id);
        if ((*env)->ExceptionCheck(env)) {
            goto error;
        }
        binding->slot_id = (uint64_t)slot_id_value;
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

    /**
     * cert_file_path is optional String path to a cert file,
     * which we convert to the file contents right away
     */
    struct aws_byte_cursor cert_file_path_cursor;
    if (!s_read_optional_string(
            env,
            options_jni,
            tls_context_pkcs11_options_properties.certificateFilePath,
            &binding->cert_file_path,
            &cert_file_path_cursor)) {
        goto error;
    }
    if (binding->cert_file_path) {
        struct aws_allocator *allocator = aws_jni_get_allocator();
        struct aws_byte_buf tmp_cert_buf;
        if (aws_byte_buf_init(&tmp_cert_buf, allocator, 0) != AWS_OP_SUCCESS) {
            goto error;
        }
        int op = aws_byte_buf_init_from_file(&tmp_cert_buf, allocator, aws_string_c_str(binding->cert_file_path));
        if (op != AWS_OP_SUCCESS) {
            aws_byte_buf_clean_up(&tmp_cert_buf);
            goto error;
        }
        binding->cert_file_contents = aws_string_new_from_buf(allocator, &tmp_cert_buf);
        binding->options.cert_file_contents = aws_byte_cursor_from_string(binding->cert_file_contents);
        aws_byte_buf_clean_up(&tmp_cert_buf);
    }

    /* binding->cert_file_contents is optional String */
    if (!binding->cert_file_contents) {
        if (!s_read_optional_string(
                env,
                options_jni,
                tls_context_pkcs11_options_properties.certificateFileContents,
                &binding->cert_file_contents,
                &binding->options.cert_file_contents)) {
            goto error;
        }
    }

    /* success! */
    return &binding->options;

error:
    aws_tls_ctx_pkcs11_options_from_java_destroy(&binding->options);
    return NULL;
}

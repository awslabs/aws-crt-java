/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "crt.h"
#include "java_class_ids.h"
#include <aws/common/file.h>
#include <aws/common/string.h>

struct directory_traversal_callback_ctx {
    JNIEnv *env;
    jobject handler;
};

static bool s_on_directory_entry(const struct aws_directory_entry *entry, void *user_data) {

    struct directory_traversal_callback_ctx *ctx = user_data;

    /* this callback is synchronous, therefore no need to use aws_jni_get_thread_env() */
    JNIEnv *env = ctx->env;

    jobject directory_entry_object = (*env)->NewObject(
        env,
        directory_entry_properties.directory_entry_class,
        directory_entry_properties.directory_entry_constructor_method_id);
    if ((*env)->ExceptionCheck(env) || directory_entry_object == NULL) {
        return false;
    }

    /* aws_jni_string_from_cursor() does not return NULL */
    jstring path = aws_jni_string_from_cursor(env, &entry->path);
    jstring relativePath = aws_jni_string_from_cursor(env, &entry->relative_path);

    (*env)->SetObjectField(env, directory_entry_object, directory_entry_properties.path_field_id, path);
    (*env)->SetObjectField(
        env, directory_entry_object, directory_entry_properties.relative_path_field_id, relativePath);
    (*env)->SetBooleanField(
        env,
        directory_entry_object,
        directory_entry_properties.is_directory_field_id,
        (entry->file_type & AWS_FILE_TYPE_DIRECTORY) != 0);
    (*env)->SetBooleanField(
        env,
        directory_entry_object,
        directory_entry_properties.is_symlink_field_id,
        (entry->file_type & AWS_FILE_TYPE_SYM_LINK) != 0);
    (*env)->SetBooleanField(
        env,
        directory_entry_object,
        directory_entry_properties.is_file_field_id,
        (entry->file_type & AWS_FILE_TYPE_FILE) != 0);
    (*env)->SetLongField(
        env, directory_entry_object, directory_entry_properties.file_size_field_id, (jlong)entry->file_size);

    jboolean callback_result = (*env)->CallBooleanMethod(
        env, ctx->handler, directory_traversal_handler_properties.on_directory_entry_method_id, directory_entry_object);

    if ((*env)->ExceptionCheck(env)) {
        /* If an exception is thrown by the user callback, the traversal is cancelled.
           Cancelling the iteration either via the user callback returning false or throwing, results
           in DirectoryTraversal.traverse() throwing an exception to notify user about incomplete results. */
        callback_result = JNI_FALSE;
    }

    /* clean-up */
    (*env)->DeleteLocalRef(env, directory_entry_object);

    if (path != NULL) {
        (*env)->DeleteLocalRef(env, path);
    }

    if (relativePath != NULL) {
        (*env)->DeleteLocalRef(env, relativePath);
    }

    return (bool)callback_result;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_DirectoryTraversal_crtTraverse(
    JNIEnv *env,
    jclass jni_class,
    jstring path,
    jboolean recursive,
    jobject handler) {
    (void)jni_class;

    struct aws_string *path_str = aws_jni_new_string_from_jstring(env, path);
    if (path_str == NULL) {
        aws_jni_throw_runtime_exception(env, "failed to get path string");
        return;
    }

    struct directory_traversal_callback_ctx ctx = {
        .env = env,
        .handler = handler,
    };

    struct aws_allocator *allocator = aws_jni_get_allocator();
    if (aws_directory_traverse(allocator, path_str, (bool)recursive, s_on_directory_entry, &ctx)) {
        /* If there's already a Java exception being thrown from the callback, then we don't need to throw another */
        if (!(*env)->ExceptionCheck(env)) {
            aws_jni_throw_runtime_exception(env, "Directory traversal failed");
        }
    }

    aws_string_destroy(path_str);
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "crt.h"
#include "java_class_ids.h"
#include "retry_utils.h"
#include <aws/common/string.h>
#include <aws/common/file.h>
#include <jni.h>

/* on 32-bit platforms, casting pointers to longs throws a warning we don't need */
#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(push)
#        pragma warning(disable : 4305) /* 'type cast': truncation from 'jlong' to 'jni_tls_ctx_options *' */
#        pragma warning(disable : 4221)
#    else
#        pragma GCC diagnostic push
#        pragma GCC diagnostic ignored "-Wpointer-to-int-cast"
#        pragma GCC diagnostic ignored "-Wint-to-pointer-cast"
#    endif
#endif

struct directory_traversal_callback_ctx {
    JNIEnv *env;
    jobject handler;
};

static bool s_on_directory_entry(const struct aws_directory_entry *entry, void *user_data) {

    struct directory_traversal_callback_ctx *ctx = user_data;
    JNIEnv* env = ctx->env;

    jstring path = aws_jni_string_from_cursor(env, &entry->path);
    jstring relativePath = aws_jni_string_from_cursor(env, &entry->relative_path);

    jboolean callback_result = (*env)->CallBooleanMethod(env,
        ctx->handler,
        directory_traversal_handler_properties.on_directory_entry_method_id,
        path,
        relativePath,
        (jboolean) entry->file_type & AWS_FILE_TYPE_DIRECTORY,
        (jboolean) entry->file_type & AWS_FILE_TYPE_SYM_LINK,
        (jboolean) entry->file_type & AWS_FILE_TYPE_FILE,
        (jlong) entry->file_size);

    return (bool) callback_result;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_DirectoryTraversal_traverse(
    JNIEnv *env,
    jclass jni_class,
    jstring path,
    jboolean recursive,
    jobject handler) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();

    const char *path_c_str = (*env)->GetStringUTFChars(env, path, NULL);

    struct aws_string *path_str = aws_string_new_from_c_str(allocator, path_c_str);
    (*env)->ReleaseStringUTFChars(env, path, path_c_str);

    struct directory_traversal_callback_ctx ctx = {
        .env = env,
        .handler = handler
    };

    if (aws_directory_traverse(allocator, path_str, (bool) recursive, s_on_directory_entry, &ctx)) {
        /* TODO: throw exception */
    }

    aws_string_destroy(path_str);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

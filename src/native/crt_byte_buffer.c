/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

#include <aws/common/common.h>
#include <aws/common/string.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/io/io.h>
#include <aws/io/logging.h>

#include <jni.h>

#include "crt.h"
#include "crt_byte_buffer.h"

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

static struct {
    jclass class;
    jmethodID constructor;
    jmethodID getBuffer;
} s_crt_byte_buffer;

void s_cache_crt_byte_buffer(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/io/CrtByteBuffer");
    AWS_FATAL_ASSERT(cls);
    s_crt_byte_buffer.class = (*env)->NewGlobalRef(env, cls);

    s_crt_byte_buffer.constructor = (*env)->GetMethodID(env, cls, "<init>", "(Ljava/nio/ByteBuffer;J)V");
    AWS_FATAL_ASSERT(s_crt_byte_buffer.constructor);

    s_crt_byte_buffer.getBuffer = (*env)->GetMethodID(env, cls, "getBuffer", "()Ljava/nio/ByteBuffer;");
    AWS_FATAL_ASSERT(s_crt_byte_buffer.getBuffer);
}

jobject aws_crt_byte_buffer_get_direct_buffer(JNIEnv *env, jobject crtBuffer) {
    // The the DirectByteBuffer from the CrtByteBuffer
    jobject jByteBuffer = (*env)->CallObjectMethod(env, crtBuffer, s_crt_byte_buffer.getBuffer);
    return jByteBuffer;
}

JNIEXPORT jobject JNICALL
    Java_software_amazon_awssdk_crt_io_CrtByteBuffer_newCrtByteBuffer(JNIEnv *env, jclass jni_class, jint capacity) {

    (void)jni_class;

    if (capacity <= 0) {
        aws_jni_throw_runtime_exception(env, "CrtByteBuffer.newCrtByteBuffer: Capacity must be >= 0");
        return (jobject)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    void *native_buf = aws_mem_calloc(allocator, 1, capacity);

    if (native_buf == NULL) {
        aws_jni_throw_runtime_exception(env, "CRT.newDirectByteBuffer: Allocation Failed");
        return (jobject)NULL;
    }

    jobject directByteBuffer = aws_jni_direct_byte_buffer_from_raw_ptr(env, native_buf, (size_t)capacity);

    AWS_FATAL_ASSERT(s_crt_byte_buffer.class);
    AWS_FATAL_ASSERT(s_crt_byte_buffer.constructor);

    jobject crtByteBuffer = (*env)->NewObject(
        env, s_crt_byte_buffer.class, s_crt_byte_buffer.constructor, directByteBuffer, (jlong)native_buf);

    return crtByteBuffer;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_io_CrtByteBuffer_zeroCrtByteBuffer(
    JNIEnv *env,
    jclass jni_class,
    jlong ptr,
    jint capacity) {

    (void)env;
    (void)jni_class;

    void *native_buf = (void *)ptr;

    memset(native_buf, 0, (size_t)capacity);
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_io_CrtByteBuffer_releaseCrtByteBuffer(JNIEnv *env, jclass jni_class, jlong ptr) {

    (void)jni_class;

    void *native_buf = (void *)ptr;

    if (native_buf == NULL) {
        aws_jni_throw_runtime_exception(env, "CrtByteBuffer.releaseCrtByteBuffer: pointer must not be null");
        return;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, native_buf);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

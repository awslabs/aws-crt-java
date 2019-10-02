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

#ifndef AWS_JNI_CRT_H
#define AWS_JNI_CRT_H

#include <aws/common/byte_buf.h>
#include <aws/common/common.h>
#include <jni.h>

struct aws_allocator *aws_jni_get_allocator();

/*******************************************************************************
 * aws_jni_throw_runtime_exception - throws a crt.CrtRuntimeException with the
 * supplied message, sprintf formatted. Control WILL return from this function,
 * so after calling it, make sure to clean up any native resources before exiting
 * the calling JNIEXPORT function.
 ******************************************************************************/
void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...);

/*******************************************************************************
 * aws_java_byte_array_new - Creates a new Java byte[]
 ******************************************************************************/
jbyteArray aws_java_byte_array_new(JNIEnv *env, size_t size);

/*******************************************************************************
 * aws_copy_java_byte_array_to_native_array - Copies from a Java byte[] to a Native byte array.
 * Returns false if ArrayIndexOutOfBoundsException occurred.
 ******************************************************************************/
bool aws_copy_java_byte_array_to_native_array(JNIEnv *env, jbyteArray src, uint8_t *dst, size_t amount);

/*******************************************************************************
 * aws_copy_java_byte_array_to_native_array - Copies from a Native byte array to a Java byte[]
 * Returns false if ArrayIndexOutOfBoundsException occurred.
 ******************************************************************************/
bool aws_copy_native_array_to_java_byte_array(JNIEnv *env, jbyteArray dst, uint8_t *src, size_t amount);

/*******************************************************************************
 * aws_java_byte_array_to_java_byte_buffer - Creates a Java ByteBuffer Object from a Java byte[]
 ******************************************************************************/
jobject aws_java_byte_array_to_java_byte_buffer(JNIEnv *env, jbyteArray jArray);

/*******************************************************************************
 * aws_jni_byte_cursor_from_jbyteArray - Creates an aws_byte_cursor from a jbyteArray.
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray(JNIEnv *env, jbyteArray array);

/*******************************************************************************
 * aws_jni_byte_cursor_from_jbyteArray - Creates an aws_byte_cursor from a jbyteArray.
 ******************************************************************************/
jbyteArray aws_jni_byte_array_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * jni_byte_buffer_copy_from_cursor - Creates a Java ByteBuffer from a native aws_byte_cursor
 ******************************************************************************/
jobject aws_jni_byte_buffer_copy_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * aws_jni_native_byte_buf_from_java_direct_byte_buf - Populates a aws_byte_buf from a Java DirectByteBuffer
 ******************************************************************************/
void aws_jni_native_byte_buf_from_java_direct_byte_buf(JNIEnv *env, jobject directBuf, struct aws_byte_buf *dst);

/*******************************************************************************
 * aws_jni_direct_byte_buffer_from_raw_ptr - Creates a Java Direct ByteBuffer from raw pointer and length
 ******************************************************************************/
jobject aws_jni_direct_byte_buffer_from_raw_ptr(JNIEnv *env, const void *dst, size_t capacity);

/*******************************************************************************
 * aws_jni_direct_byte_buffer_from_byte_buf - Creates a Java DirectByteBuffer from a native aws_byte_buf
 ******************************************************************************/
jobject aws_jni_direct_byte_buffer_from_byte_buf(JNIEnv *env, const struct aws_byte_buf *dst);

/*******************************************************************************
 * aws_jni_byte_buffer_get_position - Gets the Read/Write Position of a ByteBuffer
 ******************************************************************************/
int aws_jni_byte_buffer_get_position(JNIEnv *env, jobject java_byte_buffer);

/*******************************************************************************
 * aws_jni_byte_buffer_set_position - Sets the Read/Write Position of a ByteBuffer
 ******************************************************************************/
void aws_jni_byte_buffer_set_position(JNIEnv *env, jobject jByteBuf, jint position);

/*******************************************************************************
 * aws_jni_byte_buffer_set_limit - Sets the Read/Write Limit of a ByteBuffer
 ******************************************************************************/
void aws_jni_byte_buffer_set_limit(JNIEnv *env, jobject jByteBuf, jint limit);

/*******************************************************************************
 * aws_jni_byte_cursor_from_jstring - Creates an aws_byte_cursor from the UTF-8
 * characters extracted from the supplied jstring. The string value is null-terminated.
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jstring(JNIEnv *env, jstring str);

/*******************************************************************************
 * aws_jni_byte_cursor_from_direct_byte_buffer - Creates an aws_byte_cursor from the
 * direct byte buffer. Note that the buffer is not reference pinned, so the cursor
 * is only valid for the current JNI call
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_direct_byte_buffer(JNIEnv *env, jobject byte_buffer);

/*******************************************************************************
 * aws_jni_new_string_from_jstring - Creates a new aws_string from the UTF-8
 * characters extracted from the supplied jstring. The string must be destroyed
 * via aws_string_destroy or aws_string_destroy_secure
 ******************************************************************************/
struct aws_string *aws_jni_new_string_from_jstring(JNIEnv *env, jstring str);

/*******************************************************************************
 * aws_jni_get_thread_env - Gets the JNIEnv for the current thread from the VM,
 * attaching the env if necessary
 ******************************************************************************/
JNIEnv *aws_jni_get_thread_env(JavaVM *jvm);

#endif /* AWS_JNI_CRT_H */

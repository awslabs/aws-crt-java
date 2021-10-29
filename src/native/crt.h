/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_H
#define AWS_JNI_CRT_H

#include <aws/common/byte_buf.h>
#include <aws/common/common.h>
#include <aws/common/logging.h>

#include <jni.h>

#define AWS_CRT_JAVA_PACKAGE_ID 9

enum aws_java_crt_log_subject {
    AWS_LS_JAVA_CRT_GENERAL = AWS_LOG_SUBJECT_BEGIN_RANGE(AWS_CRT_JAVA_PACKAGE_ID),

    AWS_LS_JAVA_CRT_LAST = AWS_LOG_SUBJECT_END_RANGE(AWS_CRT_JAVA_PACKAGE_ID)
};

struct aws_allocator *aws_jni_get_allocator(void);

/*******************************************************************************
 * aws_jni_throw_runtime_exception - throws a crt.CrtRuntimeException with the
 * supplied message, sprintf formatted. Control WILL return from this function,
 * so after calling it, make sure to clean up any native resources before exiting
 * the calling JNIEXPORT function.
 ******************************************************************************/
void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...);

/*******************************************************************************
 * Throws java NullPointerException
 ******************************************************************************/
void aws_jni_throw_null_pointer_exception(JNIEnv *env, const char *msg, ...);

/*******************************************************************************
 * Throws java IllegalArgumentException
 ******************************************************************************/
void aws_jni_throw_illegal_argument_exception(JNIEnv *env, const char *msg, ...);

/*******************************************************************************
 * Checks whether or not an exception is pending on the stack and clears it.
 * If an exception was pending, it is cleared.
 *
 * @return true if an exception was pending, false otherwise. If it returns true
 * the pending exception was cleared.
 ******************************************************************************/
bool aws_jni_check_and_clear_exception(JNIEnv *env);

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
 * aws_jni_byte_cursor_from_jbyteArray - Creates an aws_byte_cursor from a jbyteArray.
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray(JNIEnv *env, jbyteArray array);

/*******************************************************************************
 * aws_jni_byte_array_from_cursor - Creates a jbyteArray from a aws_byte_cursor.
 ******************************************************************************/
jbyteArray aws_jni_byte_array_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * jni_byte_buffer_copy_from_cursor - Creates a Java ByteBuffer from a native aws_byte_cursor
 ******************************************************************************/
jobject aws_jni_byte_buffer_copy_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * aws_jni_string_from_cursor - Creates a Java String from a cursor.
 ******************************************************************************/
jstring aws_jni_string_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * aws_jni_native_byte_buf_from_java_direct_byte_buf - Populates a aws_byte_buf from a Java DirectByteBuffer
 ******************************************************************************/
void aws_jni_native_byte_buf_from_java_direct_byte_buf(JNIEnv *env, jobject directBuf, struct aws_byte_buf *dst);

/*******************************************************************************
 * aws_jni_direct_byte_buffer_from_raw_ptr - Creates a Java Direct ByteBuffer from raw pointer and length
 ******************************************************************************/
jobject aws_jni_direct_byte_buffer_from_raw_ptr(JNIEnv *env, const void *dst, size_t capacity);

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
 * aws_jni_byte_cursor_from_jstring_acquire - Creates an aws_byte_cursor from the UTF-8
 * characters extracted from the supplied jstring. The string value is null-terminated.
 * The aws_byte_cursor MUST be given to aws_jni_byte_cursor_from jstring_release() when
 * it's no longer needed, or it will leak.
 *
 * If there is an error, the returned aws_byte_cursor.ptr will be NULL and
 * and a java exception is being thrown (OutOfMemoryError or NullPointerException)
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jstring_acquire(JNIEnv *env, jstring str);

/********************************************************************************
 * aws_jni_byte_cursor_from_jstring_release - Releases the string back to the JVM
 ********************************************************************************/
void aws_jni_byte_cursor_from_jstring_release(JNIEnv *env, jstring str, struct aws_byte_cursor cur);

/*******************************************************************************
 * aws_jni_byte_cursor_from_jbyteArray_acquire - Creates an aws_byte_cursor from the
 * bytes extracted from the supplied jbyteArray.
 * The aws_byte_cursor MUST be given to aws_jni_byte_cursor_from jstring_release() when
 * it's no longer needed, or it will leak.
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray_acquire(JNIEnv *env, jbyteArray str);

/********************************************************************************
 * aws_jni_byte_cursor_from_jbyteArray_release - Releases the array back to the JVM
 ********************************************************************************/
void aws_jni_byte_cursor_from_jbyteArray_release(JNIEnv *env, jbyteArray str, struct aws_byte_cursor cur);

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
 * aws_jni_new_crt_exception_from_error_code - Creates a new jobject from the aws
 * error code, which is the type of software/amazon/awssdk/crt/CrtRuntimeException.
 * Reference of the jobject needed to be cleaned up after use.
 ******************************************************************************/
jobject aws_jni_new_crt_exception_from_error_code(JNIEnv *env, int error_code);

/*******************************************************************************
 * aws_jni_get_thread_env - Gets the JNIEnv for the current thread from the VM,
 * attaching the env if necessary
 ******************************************************************************/
JNIEnv *aws_jni_get_thread_env(JavaVM *jvm);

#endif /* AWS_JNI_CRT_H */

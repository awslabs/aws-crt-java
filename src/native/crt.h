/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_H
#define AWS_JNI_CRT_H

#include <aws/common/byte_buf.h>
#include <aws/common/common.h>
#include <aws/common/logging.h>
#include <aws/common/string.h>

#include <jni.h>

#define AWS_CRT_JAVA_PACKAGE_ID 9

enum aws_java_crt_log_subject {
    AWS_LS_JAVA_CRT_GENERAL = AWS_LOG_SUBJECT_BEGIN_RANGE(AWS_CRT_JAVA_PACKAGE_ID),
    AWS_LS_JAVA_CRT_RESOURCE,
    AWS_LS_JAVA_CRT_S3,

    AWS_LS_JAVA_CRT_LAST = AWS_LOG_SUBJECT_END_RANGE(AWS_CRT_JAVA_PACKAGE_ID),
};

enum aws_java_crt_error {
    AWS_ERROR_JAVA_CRT_JVM_DESTROYED = AWS_ERROR_ENUM_BEGIN_RANGE(AWS_CRT_JAVA_PACKAGE_ID),
    AWS_ERROR_JAVA_CRT_JVM_OUT_OF_MEMORY,

    AWS_ERROR_JAVA_CRT_END_RANGE = AWS_ERROR_ENUM_END_RANGE(AWS_CRT_JAVA_PACKAGE_ID),
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
 * Set a size_t based on a jlong.
 * If conversion fails, a java IllegalArgumentException is thrown like
 * "{errmsg_prefix} cannot be negative" and AWS_OP_ERR is returned.
 ******************************************************************************/
int aws_size_t_from_java(JNIEnv *env, size_t *out_size, jlong java_long, const char *errmsg_prefix);

/*******************************************************************************
 * aws_jni_byte_array_from_cursor - Creates a jbyteArray from a aws_byte_cursor.
 ******************************************************************************/
jbyteArray aws_jni_byte_array_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * aws_jni_string_from_cursor - Creates a Java String from a cursor.
 * This function never returns NULL. It produces a fatal assertion if the allocator is out of memory.
 ******************************************************************************/
jstring aws_jni_string_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data);

/*******************************************************************************
 * aws_jni_string_from_cursor - Creates a Java String from a string.
 * This function never returns NULL. It produces a fatal assertion if the allocator is out of memory.
 ******************************************************************************/
jstring aws_jni_string_from_string(JNIEnv *env, const struct aws_string *string);

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
 *
 * If there is an error, the returned aws_byte_cursor.ptr will be NULL and
 * and a java exception is being thrown.
 ******************************************************************************/
struct aws_byte_cursor aws_jni_byte_cursor_from_jbyteArray_acquire(JNIEnv *env, jbyteArray str);

/********************************************************************************
 * aws_jni_byte_cursor_from_jbyteArray_release - Releases the array back to the JVM
 ********************************************************************************/
void aws_jni_byte_cursor_from_jbyteArray_release(JNIEnv *env, jbyteArray str, struct aws_byte_cursor cur);

/*******************************************************************************
 * aws_jni_new_string_from_jstring - Creates a new aws_string from the UTF-8
 * characters extracted from the supplied jstring. The string must be destroyed
 * via aws_string_destroy or aws_string_destroy_secure
 ******************************************************************************/
struct aws_string *aws_jni_new_string_from_jstring(JNIEnv *env, jstring str);

/*******************************************************************************
 * aws_jni_acquire_thread_env - Acquires the JNIEnv for the current thread from the VM,
 * attaching the env if necessary.  aws_jni_release_thread_env() must be called once
 * the caller is through with the environment.
 ******************************************************************************/
JNIEnv *aws_jni_acquire_thread_env(JavaVM *jvm);

/*******************************************************************************
 * aws_jni_release_thread_env - Releases an acquired JNIEnv for the current thread.  Every successfully
 * acquired JNIEnv must be released exactly once.  Internally, all this does is release the reader
 * lock on the set of valid JVMs.
 ******************************************************************************/
void aws_jni_release_thread_env(JavaVM *jvm, JNIEnv *env);

/*******************************************************************************
 * aws_jni_new_crt_exception_from_error_code - Creates a new jobject from the aws
 * error code, which is the type of software/amazon/awssdk/crt/CrtRuntimeException.
 * Reference of the jobject needed to be cleaned up after use.
 ******************************************************************************/
jobject aws_jni_new_crt_exception_from_error_code(JNIEnv *env, int error_code);

#endif /* AWS_JNI_CRT_H */

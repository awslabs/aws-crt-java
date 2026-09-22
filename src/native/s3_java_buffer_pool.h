#ifndef AWS_CRT_JAVA_S3_JAVA_BUFFER_POOL_H
#define AWS_CRT_JAVA_S3_JAVA_BUFFER_POOL_H

#include <aws/s3/s3_buffer_pool.h>
#include <jni.h>

/*
 * Passed via aws_s3_client_config_options.buffer_pool_user_data to
 * aws_s3_java_buffer_pool_factory. Caller allocates on the stack;
 * the factory copies fields into pool state before returning, so
 * the struct memory does not need to outlive the factory call.
 *
 * `java_pool_global` ownership transfers to the factory on success.
 * On failure (factory returns NULL), the caller retains ownership
 * and must DeleteGlobalRef.
 */
struct aws_s3_java_buffer_pool_factory_data {
    JavaVM *jvm;
    jobject java_pool_global;
};

/*
 * Factory function compatible with aws_s3_buffer_pool_factory_fn
 * (declared in aws-c-s3/include/aws/s3/s3_buffer_pool.h).
 *
 * Wired into aws_s3_client_config_options.buffer_pool_factory_fn
 * when the Java caller has attached a non-null pool to
 * S3ClientOptions via withDirectByteBufferPool(...).
 *
 * `user_data` is the global JNI ref to the S3DirectBufferPool Java
 * object. The factory takes ownership of that ref for the lifetime
 * of the pool — releasing it in the pool's destroy path.
 *
 * Returns NULL and raises an aws_error on failure (e.g. JNI ref
 * allocation, method ID lookup). aws-c-s3 falls back to the default
 * pool in that case (per its existing behavior when the factory
 * returns NULL).
 */
struct aws_s3_buffer_pool *aws_s3_java_buffer_pool_factory(
    struct aws_allocator *allocator,
    struct aws_s3_buffer_pool_config config,
    void *user_data);

#endif /* AWS_CRT_JAVA_S3_JAVA_BUFFER_POOL_H */

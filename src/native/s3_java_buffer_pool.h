/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#ifndef AWS_JNI_CRT_S3_JAVA_BUFFER_POOL_H
#define AWS_JNI_CRT_S3_JAVA_BUFFER_POOL_H

#include <aws/s3/s3_buffer_pool.h>
#include <jni.h>

/*
 * Passed via aws_s3_client_config_options.buffer_pool_user_data to
 * aws_s3_java_buffer_pool_factory. Caller allocates on the stack; it
 * must stay alive until the caller has read `out_pool` after
 * aws_s3_client_new returns.
 *
 * The factory takes ownership of `java_pool_global` once its argument
 * checks pass, and clears the field. If the field is still set after
 * the call, the caller still owns the reference and must
 * DeleteGlobalRef it.
 */
struct aws_s3_java_buffer_pool_factory_data {
    JavaVM *jvm;
    jobject java_pool_global;
    /* Out: the created pool, set by the factory on success. Not a ref; the
     * client owns it. s3ClientNew publishes it to the Java pool so meta
     * requests can call aws_s3_java_buffer_pool_drain. */
    struct aws_s3_buffer_pool *out_pool;
};

/*
 * Factory function compatible with aws_s3_buffer_pool_factory_fn
 * (declared in aws-c-s3/include/aws/s3/s3_buffer_pool.h).
 *
 * Wired into aws_s3_client_config_options.buffer_pool_factory_fn
 * when S3ClientOptions.withDirectBufferPoolOptions(...) is set; the
 * Java S3Client creates the pool and passes it to s3ClientNew.
 *
 * `user_data` points to an aws_s3_java_buffer_pool_factory_data struct
 * containing the JVM pointer and the JNI global ref to the
 * S3DirectBufferPool Java object. The factory takes ownership of the
 * global ref (clearing the caller's field) and releases it in the
 * pool's destroy path, or on its own failure paths.
 *
 * Returns NULL and raises an aws_error on failure (e.g. missing JVM
 * or pool ref, slot-size mismatch). aws-c-s3 then fails client
 * creation; there is no fallback to the default pool, so an explicit
 * opt-in never silently degrades.
 */
struct aws_s3_buffer_pool *aws_s3_java_buffer_pool_factory(
    struct aws_allocator *allocator,
    struct aws_s3_buffer_pool_config config,
    void *user_data);

/*
 * Drops pending reservations that aws-c-s3 has already cancelled or paused,
 * and serves waiting ones that now fit. Called from each meta request's
 * finish callback: aws-c-s3 completes a cancelled request's pending futures
 * without calling into the pool, so without this a reservation queued
 * behind a cancelled one could wait until an unrelated reserve or release.
 * `pool` MUST be a pool made by aws_s3_java_buffer_pool_factory, and the
 * caller MUST hold a ref on it. Takes the pool's pending_lock; MUST NOT be
 * called while holding it.
 */
void aws_s3_java_buffer_pool_drain(struct aws_s3_buffer_pool *pool);

#endif /* AWS_JNI_CRT_S3_JAVA_BUFFER_POOL_H */

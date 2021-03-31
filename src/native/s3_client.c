/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#include "crt.h"
#include "http_request_utils.h"
#include "java_class_ids.h"
#include <aws/http/request_response.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/s3/s3_client.h>
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

struct s3_client_callback_data {
    JavaVM *jvm;
    jobject java_s3_client;
};

struct s3_client_make_meta_request_callback_data {
    JavaVM *jvm;
    jobject java_s3_meta_request;
    jobject java_s3_meta_request_response_handler_native_adapter;
};

static void s_on_s3_client_shutdown_complete_callback(void *user_data);
static void s_on_s3_meta_request_shutdown_complete_callback(void *user_data);

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_s3_S3Client_s3ClientNew(
    JNIEnv *env,
    jclass jni_class,
    jobject s3_client_jobject,
    jbyteArray jni_region,
    jbyteArray jni_endpoint,
    jlong jni_client_bootstrap,
    jlong jni_tls_ctx,
    jlong jni_credentials_provider,
    jlong part_size,
    jdouble throughput_target_gbps,
    int max_connections) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();

    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;

    if (!client_bootstrap) {
        aws_jni_throw_runtime_exception(env, "Invalid Client Bootstrap");
        return (jlong)NULL;
    }

    struct aws_credentials_provider *credentials_provider = (struct aws_credentials_provider *)jni_credentials_provider;
    if (!credentials_provider) {
        aws_jni_throw_runtime_exception(env, "Invalid Credentials Provider");
        return (jlong)NULL;
    }

    struct aws_byte_cursor region = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_region);

    struct s3_client_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct s3_client_callback_data));
    AWS_FATAL_ASSERT(callback_data);
    callback_data->java_s3_client = (*env)->NewGlobalRef(env, s3_client_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_signing_config_aws signing_config;
    aws_s3_init_default_signing_config(&signing_config, region, credentials_provider);

    struct aws_tls_connection_options *tls_options = NULL;
    struct aws_tls_connection_options tls_options_storage;
    AWS_ZERO_STRUCT(tls_options_storage);
    if (jni_tls_ctx) {
        struct aws_tls_ctx *tls_ctx = (void *)jni_tls_ctx;
        tls_options = &tls_options_storage;
        aws_tls_connection_options_init_from_ctx(tls_options, tls_ctx);
        struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_endpoint);
        aws_tls_connection_options_set_server_name(tls_options, allocator, &endpoint);
        aws_jni_byte_cursor_from_jbyteArray_release(env, jni_endpoint, endpoint);
    }

    struct aws_s3_client_config client_config = {
        .max_active_connections_override = max_connections,
        .region = region,
        .client_bootstrap = client_bootstrap,
        .tls_connection_options = tls_options,
        .signing_config = NULL,
        .part_size = (size_t)part_size,
        .throughput_target_gbps = throughput_target_gbps,
        .shutdown_callback = s_on_s3_client_shutdown_complete_callback,
        .shutdown_callback_user_data = callback_data,
    };
    client_config.signing_config = &signing_config;

    struct aws_s3_client *client = aws_s3_client_new(allocator, &client_config);
    if (!client) {
        aws_jni_throw_runtime_exception(env, "S3Client.aws_s3_client_new: creating aws_s3_client failed");
        goto clean_up;
    }

clean_up:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_region, region);

    return (jlong)client;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_s3_S3Client_s3ClientDestroy(JNIEnv *env, jclass jni_class, jlong jni_s3_client) {
    (void)jni_class;
    struct aws_s3_client *client = (struct aws_s3_client *)jni_s3_client;
    if (!client) {
        aws_jni_throw_runtime_exception(env, "S3Client.s3_client_clean_up: Invalid/null client");
        return;
    }

    aws_s3_client_release(client);
}

static void s_on_s3_client_shutdown_complete_callback(void *user_data) {
    struct s3_client_callback_data *callback = (struct s3_client_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    AWS_LOGF_DEBUG(AWS_LS_S3_CLIENT, "S3 Client Shutdown Complete");
    if (callback->java_s3_client != NULL) {
        (*env)->CallVoidMethod(env, callback->java_s3_client, s3_client_properties.onShutdownComplete);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3Client.onShutdownCompete callback",
                (void *)callback->java_s3_client);
        }
    }

    // We're done with this callback data, free it.
    (*env)->DeleteGlobalRef(env, callback->java_s3_client);
    aws_mem_release(aws_jni_get_allocator(), user_data);
}

static int s_on_s3_meta_request_body_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_byte_cursor *body,
    uint64_t range_start,
    void *user_data) {
    (void)body;
    (void)range_start;
    int return_value = AWS_OP_ERR;

    uint64_t range_end = range_start + body->len;

    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    jobject jni_payload = aws_jni_direct_byte_buffer_from_raw_ptr(env, body->ptr, body->len);
    jint body_response_result = 0;

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        body_response_result = (*env)->CallIntMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onResponseBody,
            jni_payload,
            range_start,
            range_end);
        (void)body_response_result;

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onResponseBody callback",
                (void *)meta_request);
            goto cleanup;
        }
    }
    return_value = AWS_OP_SUCCESS;

cleanup:
    (*env)->DeleteLocalRef(env, jni_payload);

    return return_value;
}

static int s_on_s3_meta_request_headers_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_http_headers *headers,
    int response_status,
    void *user_data) {
    int return_value = AWS_OP_ERR;
    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);
    struct aws_allocator *allocator = aws_jni_get_allocator();
    /* calculate initial header capacity */
    size_t headers_initial_capacity = 0;
    for (size_t header_index = 0; header_index < aws_http_headers_count(headers); ++header_index) {
        struct aws_http_header header;
        aws_http_headers_get_index(headers, header_index, &header);
        /* aws_marshal_http_headers_to_dynamic_buffer() impl drives this calculation */
        headers_initial_capacity += header.name.len + header.value.len + 8;
    }

    struct aws_byte_buf headers_buf;
    AWS_ZERO_STRUCT(headers_buf);
    if (aws_byte_buf_init(&headers_buf, allocator, headers_initial_capacity)) {
        return AWS_OP_ERR;
    }

    for (size_t header_index = 0; header_index < aws_http_headers_count(headers); ++header_index) {
        struct aws_http_header header;
        aws_http_headers_get_index(headers, header_index, &header);
        aws_marshal_http_headers_to_dynamic_buffer(&headers_buf, &header, 1);
    }
    jobject java_headers_buffer = aws_jni_direct_byte_buffer_from_raw_ptr(env, headers_buf.buffer, headers_buf.len);

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        (*env)->CallVoidMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders,
            response_status,
            java_headers_buffer);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Exception thrown from S3MetaRequest.onResponseHeaders callback",
                (void *)meta_request);
            goto cleanup;
        }
    }
    return_value = AWS_OP_SUCCESS;

cleanup:
    aws_byte_buf_clean_up(&headers_buf);
    if (java_headers_buffer) {
        (*env)->DeleteLocalRef(env, java_headers_buffer);
    }

    return return_value;
}

static void s_on_s3_meta_request_finish_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_s3_meta_request_result *meta_request_result,
    void *user_data) {

    (void)meta_request;

    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        (*env)->CallVoidMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onFinished,
            meta_request_result->error_code);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onFinished callback",
                (void *)meta_request);
        }
    }
}

static void s_s3_meta_request_callback_cleanup(
    JNIEnv *env,
    struct s3_client_make_meta_request_callback_data *callback_data) {
    if (callback_data) {
        (*env)->DeleteGlobalRef(env, callback_data->java_s3_meta_request);
        (*env)->DeleteGlobalRef(env, callback_data->java_s3_meta_request_response_handler_native_adapter);
        aws_mem_release(aws_jni_get_allocator(), callback_data);
    }
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_s3_S3Client_s3ClientMakeMetaRequest(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_client,
    jobject java_s3_meta_request_jobject,
    jint meta_request_type,
    jbyteArray jni_marshalled_message_data,
    jobject jni_http_request_body_stream,
    jobject java_response_handler_jobject) {
    (void)jni_class;

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_s3_client *client = (struct aws_s3_client *)jni_s3_client;

    struct s3_client_make_meta_request_callback_data *callback_data =
        aws_mem_acquire(allocator, sizeof(struct s3_client_make_meta_request_callback_data));
    AWS_FATAL_ASSERT(callback_data);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->java_s3_meta_request = (*env)->NewGlobalRef(env, java_s3_meta_request_jobject);
    AWS_FATAL_ASSERT(callback_data->java_s3_meta_request != NULL);

    callback_data->java_s3_meta_request_response_handler_native_adapter =
        (*env)->NewGlobalRef(env, java_response_handler_jobject);
    AWS_FATAL_ASSERT(callback_data->java_s3_meta_request_response_handler_native_adapter != NULL);

    struct aws_http_message *request_message = aws_http_message_new_request(allocator);
    AWS_FATAL_ASSERT(request_message);

    AWS_FATAL_ASSERT(
        AWS_OP_SUCCESS == aws_apply_java_http_request_changes_to_native_request(
                              env, jni_marshalled_message_data, jni_http_request_body_stream, request_message));

    struct aws_s3_meta_request_options meta_request_options = {
        .type = meta_request_type,
        .message = request_message,
        .user_data = callback_data,
        .headers_callback = s_on_s3_meta_request_headers_callback,
        .body_callback = s_on_s3_meta_request_body_callback,
        .finish_callback = s_on_s3_meta_request_finish_callback,
        .shutdown_callback = s_on_s3_meta_request_shutdown_complete_callback};

    struct aws_s3_meta_request *meta_request = aws_s3_client_make_meta_request(client, &meta_request_options);

    if (!meta_request) {
        aws_jni_throw_runtime_exception(
            env, "S3Client.aws_s3_client_make_meta_request: creating aws_s3_meta_request failed");
        goto error_cleanup;
    }

    aws_http_message_release(request_message);
    return (jlong)meta_request;

error_cleanup:
    aws_http_message_release(request_message);
    s_s3_meta_request_callback_cleanup(env, callback_data);
    return (jlong)0;
}

static void s_on_s3_meta_request_shutdown_complete_callback(void *user_data) {
    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    if (callback_data->java_s3_meta_request != NULL) {
        (*env)->CallVoidMethod(env, callback_data->java_s3_meta_request, s3_meta_request_properties.onShutdownComplete);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onShutdownCompete callback",
                (void *)callback_data->java_s3_meta_request);
        }
    }

    // We're done with this callback data, free it.
    s_s3_meta_request_callback_cleanup(env, callback_data);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_s3_S3MetaRequest_s3MetaRequestDestroy(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_s3_meta_request) {
    (void)jni_class;

    struct aws_s3_meta_request *meta_request = (struct aws_s3_meta_request *)jni_s3_meta_request;
    if (!meta_request) {
        aws_jni_throw_runtime_exception(env, "S3Client.s3_client_clean_up: Invalid/null client");
        return;
    }

    aws_s3_meta_request_release(meta_request);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

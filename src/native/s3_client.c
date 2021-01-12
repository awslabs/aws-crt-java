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
    jdouble throughput_target_gbps) {
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

    struct s3_client_callback_data *callback_data = aws_mem_acquire(allocator, sizeof(struct s3_client_callback_data));
    AWS_FATAL_ASSERT(callback_data);
    callback_data->java_s3_client = (*env)->NewGlobalRef(env, s3_client_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_signing_config_aws signing_config = {0};
    aws_s3_init_default_signing_config(&signing_config, region, credentials_provider);

    struct aws_tls_connection_options *tls_options = NULL;
    struct aws_tls_connection_options tls_options_storage = {0};
    if (jni_tls_ctx) {
        struct aws_tls_ctx *tls_ctx = (void *)jni_tls_ctx;
        tls_options = &tls_options_storage;
        aws_tls_connection_options_init_from_ctx(tls_options, tls_ctx);
        struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_endpoint);
        aws_tls_connection_options_set_server_name(tls_options, allocator, &endpoint);
        aws_jni_byte_cursor_from_jbyteArray_release(env, jni_endpoint, endpoint);
    }

    struct aws_s3_client_config client_config = {.region = region,
                                                 .client_bootstrap = client_bootstrap,
                                                 .tls_connection_options = tls_options,
                                                 .signing_config = NULL,
                                                 .part_size = part_size,
                                                 .throughput_target_gbps = throughput_target_gbps,
                                                 .shutdown_callback = s_on_s3_client_shutdown_complete_callback,
                                                 .shutdown_callback_user_data = callback_data};
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

static void s_on_s3_meta_request_body_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_byte_cursor *body,
    uint64_t range_start,
    void *user_data) {

    (void)meta_request;
    (void)body;
    (void)range_start;
    (void)user_data;

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
        }
    }

    (*env)->DeleteLocalRef(env, jni_payload);
}

/* only used in S3 client for now, but has generic JNI/HTTP utility */
static jobjectArray s_aws_java_http_headers_from_native(JNIEnv *env, const struct aws_http_headers *headers) {
    const size_t header_count = aws_http_headers_count(headers);
    jobjectArray ret = (jobjectArray)(*env)->NewObjectArray(env, header_count,
        http_header_properties.http_header_class,
        (void *)NULL);

    for (size_t index = 0; index < header_count; index += 1) {
        struct aws_http_header header;
        aws_http_headers_get_index(headers, index, &header);
        jbyteArray header_name = aws_jni_byte_array_from_cursor(env, &header.name);
        jbyteArray header_value = aws_jni_byte_array_from_cursor(env, &header.value);

        jobject java_http_header = (*env)->NewObject(env,
            http_header_properties.http_header_class,
            http_header_properties.constructor_method_id,
               header_name, header_value);

        (*env)->SetObjectArrayElement(env, ret, index, java_http_header);
    }

    return (ret);
}

static void s_on_s3_meta_request_headers_callback(
    struct aws_s3_meta_request *meta_request,
    const struct aws_http_headers *headers,
    int response_status,
    void *user_data) {
    (void)meta_request;

    struct s3_client_make_meta_request_callback_data *callback_data =
        (struct s3_client_make_meta_request_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    if (callback_data->java_s3_meta_request_response_handler_native_adapter != NULL) {
        jobjectArray java_headers_array = s_aws_java_http_headers_from_native(env, headers);

        /* size_t header_length = aws_array_list_length(&headers->array_list);
        printf("Headers length: %zu", header_length); */

        AWS_LOGF_TRACE(
                AWS_LS_S3_META_REQUEST,
                "id=%p: invoking S3MetaRequest.onResponseHeaders callback. Code: %d",
                (void *)meta_request, response_status);

        (*env)->CallVoidMethod(
            env,
            callback_data->java_s3_meta_request_response_handler_native_adapter,
            s3_meta_request_response_handler_native_adapter_properties.onResponseHeaders,
            response_status, java_headers_array);

        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_META_REQUEST,
                "id=%p: Ignored Exception from S3MetaRequest.onResponseHeaders callback",
                (void *)meta_request);
        }
    }
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
    callback_data->java_s3_meta_request_response_handler_native_adapter =
        (*env)->NewGlobalRef(env, java_response_handler_jobject);

    AWS_FATAL_ASSERT(callback_data->java_s3_meta_request != NULL);

    callback_data->java_s3_meta_request_response_handler_native_adapter =
        (*env)->NewGlobalRef(env, java_response_handler_jobject);

    AWS_FATAL_ASSERT(callback_data->java_s3_meta_request_response_handler_native_adapter != NULL);

    struct aws_http_message *request_message = aws_http_message_new_request(allocator);
    if (request_message == NULL) {
        /* TODO */
    }

    if (aws_apply_java_http_request_changes_to_native_request(
            env, jni_marshalled_message_data, jni_http_request_body_stream, request_message)) {
        /* TODO */
    }

    struct aws_s3_meta_request_options meta_request_options = {
        .type = meta_request_type,
        .message = request_message,
        .user_data = callback_data,
        .headers_callback = s_on_s3_meta_request_headers_callback,
        .body_callback = s_on_s3_meta_request_body_callback,
        .finish_callback = s_on_s3_meta_request_finish_callback,
        .shutdown_callback = s_on_s3_meta_request_shutdown_complete_callback};

    struct aws_s3_meta_request *meta_request = aws_s3_client_make_meta_request(client, &meta_request_options);

    aws_http_message_release(request_message);

    if (!meta_request) {
        aws_jni_throw_runtime_exception(
            env, "S3Client.aws_s3_client_make_meta_request: creating aws_s3_meta_request failed");
    }

    return (jlong)meta_request;
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
    (*env)->DeleteGlobalRef(env, callback_data->java_s3_meta_request);
    (*env)->DeleteGlobalRef(env, callback_data->java_s3_meta_request_response_handler_native_adapter);
    aws_mem_release(aws_jni_get_allocator(), callback_data);
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

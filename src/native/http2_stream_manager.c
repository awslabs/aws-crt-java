/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "http_connection_manager.h"
#include "http_request_response.h"
#include "http_request_utils.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>

#include <aws/common/condition_variable.h>
#include <aws/common/string.h>

#include <aws/io/channel_bootstrap.h>
#include <aws/io/event_loop.h>
#include <aws/io/logging.h>
#include <aws/io/socket.h>
#include <aws/io/tls_channel_handler.h>

#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/http/http2_stream_manager.h>
#include <aws/http/proxy.h>

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

/*
 * Stream manager binding, persists across the lifetime of the native object.
 */
struct aws_http2_stream_manager_binding {
    JavaVM *jvm;
    jweak java_http2_stream_manager;
    struct aws_http2_stream_manager *stream_manager;
};

static void s_destroy_manager_binding(struct aws_http2_stream_manager_binding *binding, JNIEnv *env) {
    if (binding == NULL) {
        return;
    }
    if (binding->java_http2_stream_manager != NULL) {
        (*env)->DeleteWeakGlobalRef(env, binding->java_http2_stream_manager);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static void s_on_stream_manager_shutdown_complete_callback(void *user_data) {

    struct aws_http2_stream_manager_binding *binding = (struct aws_http2_stream_manager_binding *)user_data;
    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);

    AWS_LOGF_DEBUG(AWS_LS_HTTP_STREAM_MANAGER, "Java Stream Manager Shutdown Complete");
    jobject java_http2_stream_manager = (*env)->NewLocalRef(env, binding->java_http2_stream_manager);
    if (java_http2_stream_manager != NULL) {
        (*env)->CallVoidMethod(env, java_http2_stream_manager, http2_stream_manager_properties.onShutdownComplete);

        /* If exception raised from Java callback, but we already closed the stream manager, just move on */
        aws_jni_check_and_clear_exception(env);

        (*env)->DeleteLocalRef(env, java_http2_stream_manager);
    }

    /* We're done with this wrapper, free it. */
    s_destroy_manager_binding(binding, env);
    aws_jni_release_thread_env(binding->jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_http_Http2StreamManager_http2StreamManagerNew(
    JNIEnv *env,
    jclass jni_class,
    jobject stream_manager_jobject,
    jlong jni_client_bootstrap,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jlongArray java_marshalled_settings,
    jbyteArray jni_endpoint,
    jint jni_port,
    jint jni_proxy_connection_type,
    jbyteArray jni_proxy_host,
    jint jni_proxy_port,
    jlong jni_proxy_tls_context,
    jint jni_proxy_authorization_type,
    jbyteArray jni_proxy_authorization_username,
    jbyteArray jni_proxy_authorization_password,
    jboolean jni_manual_window_management,
    jlong jni_monitoring_throughput_threshold_in_bytes_per_second,
    jint jni_monitoring_failure_interval_in_seconds,
    jint jni_max_conns,
    jint jni_ideal_concurrent_streams_per_connection,
    jint jni_max_concurrent_streams_per_connection) {

    (void)jni_class;

    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;
    struct aws_socket_options *socket_options = (struct aws_socket_options *)jni_socket_options;
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_ctx;
    struct aws_http2_stream_manager_binding *binding = NULL;
    struct aws_allocator *allocator = aws_jni_get_allocator();

    if (!client_bootstrap) {
        aws_jni_throw_illegal_argument_exception(env, "ClientBootstrap can't be null");
        return (jlong)NULL;
    }

    if (!socket_options) {
        aws_jni_throw_illegal_argument_exception(env, "SocketOptions can't be null");
        return (jlong)NULL;
    }

    const size_t marshalled_len = (*env)->GetArrayLength(env, java_marshalled_settings);
    AWS_ASSERT(marshalled_len % 2 == 0);

    size_t num_initial_settings = marshalled_len / 2;
    struct aws_http2_setting *initial_settings =
        num_initial_settings ? aws_mem_calloc(allocator, num_initial_settings, sizeof(struct aws_http2_setting)) : NULL;

    jlong *marshalled_settings = (*env)->GetLongArrayElements(env, java_marshalled_settings, NULL);
    for (size_t i = 0; i < num_initial_settings; i++) {
        jlong id = marshalled_settings[i * 2];
        initial_settings[i].id = (uint32_t)id;
        jlong value = marshalled_settings[i * 2 + 1];
        /* We checked the value can fit into uint32_t in Java already */
        initial_settings[i].value = (uint32_t)value;
    }

    struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_endpoint);

    if (jni_port <= 0 || 65535 < jni_port) {
        aws_jni_throw_illegal_argument_exception(env, "Port must be between 1 and 65535");
        goto cleanup;
    }

    if (jni_max_conns <= 0) {
        aws_jni_throw_illegal_argument_exception(env, "Max Connections must be > 0");
        goto cleanup;
    }

    uint16_t port = (uint16_t)jni_port;

    int use_tls = (jni_tls_ctx != 0);

    struct aws_tls_connection_options tls_conn_options;
    AWS_ZERO_STRUCT(tls_conn_options);

    if (use_tls) {
        aws_tls_connection_options_init_from_ctx(&tls_conn_options, tls_ctx);
        aws_tls_connection_options_set_server_name(&tls_conn_options, allocator, &endpoint);
    }

    binding = aws_mem_calloc(allocator, 1, sizeof(struct aws_http2_stream_manager_binding));
    AWS_FATAL_ASSERT(binding);
    binding->java_http2_stream_manager = (*env)->NewWeakGlobalRef(env, stream_manager_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_http2_stream_manager_options manager_options;
    AWS_ZERO_STRUCT(manager_options);

    manager_options.bootstrap = client_bootstrap;
    manager_options.initial_settings_array = initial_settings;
    manager_options.num_initial_settings = num_initial_settings;

    manager_options.socket_options = socket_options;
    manager_options.tls_connection_options = NULL;
    manager_options.host = endpoint;
    manager_options.port = port;
    manager_options.shutdown_complete_callback = &s_on_stream_manager_shutdown_complete_callback;
    manager_options.shutdown_complete_user_data = binding;
    manager_options.monitoring_options = NULL;
    /* TODO: this variable needs to be renamed in aws-c-http. Come back and change it next revision. */
    manager_options.enable_read_back_pressure = jni_manual_window_management;

    manager_options.max_connections = (size_t)jni_max_conns;
    manager_options.ideal_concurrent_streams_per_connection = (size_t)jni_ideal_concurrent_streams_per_connection;
    manager_options.max_concurrent_streams_per_connection = (size_t)jni_max_concurrent_streams_per_connection;

    if (use_tls) {
        manager_options.tls_connection_options = &tls_conn_options;
    }

    struct aws_http_connection_monitoring_options monitoring_options;
    AWS_ZERO_STRUCT(monitoring_options);
    if (jni_monitoring_throughput_threshold_in_bytes_per_second >= 0 &&
        jni_monitoring_failure_interval_in_seconds >= 2) {
        monitoring_options.minimum_throughput_bytes_per_second =
            jni_monitoring_throughput_threshold_in_bytes_per_second;
        monitoring_options.allowable_throughput_failure_interval_seconds = jni_monitoring_failure_interval_in_seconds;

        manager_options.monitoring_options = &monitoring_options;
    }

    struct aws_http_proxy_options proxy_options;
    AWS_ZERO_STRUCT(proxy_options);

    struct aws_tls_connection_options proxy_tls_conn_options;
    AWS_ZERO_STRUCT(proxy_tls_conn_options);

    aws_http_proxy_options_jni_init(
        env,
        &proxy_options,
        jni_proxy_connection_type,
        &proxy_tls_conn_options,
        jni_proxy_host,
        (uint16_t)jni_proxy_port,
        jni_proxy_authorization_username,
        jni_proxy_authorization_password,
        jni_proxy_authorization_type,
        (struct aws_tls_ctx *)jni_proxy_tls_context);

    if (jni_proxy_host != NULL) {
        manager_options.proxy_options = &proxy_options;
    }

    binding->stream_manager = aws_http2_stream_manager_new(allocator, &manager_options);
    if (binding->stream_manager == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to create stream manager: %s", aws_error_str(aws_last_error()));
    }

    aws_http_proxy_options_jni_clean_up(
        env, &proxy_options, jni_proxy_host, jni_proxy_authorization_username, jni_proxy_authorization_password);

    if (use_tls) {
        aws_tls_connection_options_clean_up(&tls_conn_options);
    }

cleanup:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_endpoint, endpoint);

    if (binding->stream_manager == NULL) {
        s_destroy_manager_binding(binding, env);
        binding = NULL;
    }

    return (jlong)binding;
}

/*
 * Stream manager binding, persists across the lifetime of the native object.
 */
struct aws_sm_acquire_stream_callback_data {
    JavaVM *jvm;
    struct http_stream_binding *stream_binding;
    jobject java_async_callback;
};

static void s_cleanup_sm_acquire_stream_callback_data(
    struct aws_sm_acquire_stream_callback_data *callback_data,
    JNIEnv *env) {

    if (callback_data->java_async_callback) {
        (*env)->DeleteGlobalRef(env, callback_data->java_async_callback);
    }
    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static struct aws_sm_acquire_stream_callback_data *s_new_sm_acquire_stream_callback_data(
    JNIEnv *env,
    struct aws_allocator *allocator,
    struct http_stream_binding *stream_binding,
    jobject async_callback) {
    struct aws_sm_acquire_stream_callback_data *callback_data =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_sm_acquire_stream_callback_data));

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    AWS_FATAL_ASSERT(jvmresult == 0);
    callback_data->java_async_callback = async_callback ? (*env)->NewGlobalRef(env, async_callback) : NULL;
    AWS_FATAL_ASSERT(callback_data->java_async_callback != NULL);
    callback_data->stream_binding = stream_binding;

    return callback_data;
}

static void s_on_stream_acquired(struct aws_http_stream *stream, int error_code, void *user_data) {
    struct aws_sm_acquire_stream_callback_data *callback_data = user_data;
    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(callback_data->jvm);
    if (error_code) {
        jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, error_code);
        (*env)->CallVoidMethod(
            env, callback_data->java_async_callback, async_callback_properties.on_failure, crt_exception);
        (*env)->DeleteLocalRef(env, crt_exception);
        aws_http_stream_binding_destroy(env, callback_data->stream_binding);
    } else {
        callback_data->stream_binding->native_stream = stream;
        jobject j_http_stream =
            aws_java_http_stream_from_native_new(env, callback_data->stream_binding, AWS_HTTP_VERSION_2);
        if (!j_http_stream) {
            jobject crt_exception = aws_jni_new_crt_exception_from_error_code(env, aws_last_error());
            (*env)->CallVoidMethod(
                env, callback_data->java_async_callback, async_callback_properties.on_failure, crt_exception);
            (*env)->DeleteLocalRef(env, crt_exception);
            aws_http_stream_binding_destroy(env, callback_data->stream_binding);
        } else {
            /* Stream is activated once we acquired from the Stream Manager */
            aws_atomic_store_int(&callback_data->stream_binding->activated, 1);
            callback_data->stream_binding->java_http_stream_base = (*env)->NewGlobalRef(env, j_http_stream);
            (*env)->CallVoidMethod(
                env,
                callback_data->java_async_callback,
                async_callback_properties.on_success_with_object,
                callback_data->stream_binding->java_http_stream_base);
            (*env)->DeleteLocalRef(env, j_http_stream);
        }
    }
    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
    s_cleanup_sm_acquire_stream_callback_data(callback_data, env);
    aws_jni_release_thread_env(callback_data->jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_Http2StreamManager_http2StreamManagerAcquireStream(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_stream_manager,
    jbyteArray marshalled_request,
    jobject jni_http_request_body_stream,
    jobject jni_http_response_callback_handler,
    jobject java_async_callback) {
    (void)jni_class;
    struct aws_http2_stream_manager_binding *sm_binding = (struct aws_http2_stream_manager_binding *)jni_stream_manager;
    struct aws_http2_stream_manager *stream_manager = sm_binding->stream_manager;

    if (!stream_manager) {
        aws_jni_throw_illegal_argument_exception(env, "Stream Manager can't be null");
        return;
    }

    if (!jni_http_response_callback_handler) {
        aws_jni_throw_illegal_argument_exception(
            env, "Http2StreamManager.acquireStream: Invalid jni_http_response_callback_handler");
        return;
    }
    if (!java_async_callback) {
        aws_jni_throw_illegal_argument_exception(env, "Http2StreamManager.acquireStream: Invalid async callback");
        return;
    }

    struct http_stream_binding *stream_binding = aws_http_stream_binding_alloc(env, jni_http_response_callback_handler);
    if (!stream_binding) {
        /* Exception already thrown */
        return;
    }

    stream_binding->native_request =
        aws_http_request_new_from_java_http_request(env, marshalled_request, jni_http_request_body_stream);
    if (stream_binding->native_request == NULL) {
        /* Exception already thrown */
        aws_http_stream_binding_destroy(env, stream_binding);
        return;
    }

    struct aws_http_make_request_options request_options = {
        .self_size = sizeof(request_options),
        .request = stream_binding->native_request,
        /* Set Callbacks */
        .on_response_headers = aws_java_http_stream_on_incoming_headers_fn,
        .on_response_header_block_done = aws_java_http_stream_on_incoming_header_block_done_fn,
        .on_response_body = aws_java_http_stream_on_incoming_body_fn,
        .on_complete = aws_java_http_stream_on_stream_complete_fn,
        .user_data = stream_binding,
    };

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_sm_acquire_stream_callback_data *callback_data =
        s_new_sm_acquire_stream_callback_data(env, allocator, stream_binding, java_async_callback);

    struct aws_http2_stream_manager_acquire_stream_options acquire_options = {
        .options = &request_options,
        .callback = s_on_stream_acquired,
        .user_data = callback_data,
    };

    aws_http2_stream_manager_acquire_stream(sm_binding->stream_manager, &acquire_options);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_Http2StreamManager_http2StreamManagerRelease(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_stream_manager) {
    (void)jni_class;

    struct aws_http2_stream_manager_binding *sm_binding = (struct aws_http2_stream_manager_binding *)jni_stream_manager;
    struct aws_http2_stream_manager *stream_manager = sm_binding->stream_manager;

    if (!stream_manager) {
        aws_jni_throw_runtime_exception(env, "Stream Manager can't be null");
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION, "Releasing StreamManager: id: %p", (void *)stream_manager);
    aws_http2_stream_manager_release(stream_manager);
}

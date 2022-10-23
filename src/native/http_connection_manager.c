/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include "crt.h"
#include "java_class_ids.h"

#include <jni.h>
#include <string.h>
#include <http_proxy_options.h>

#include <aws/common/condition_variable.h>
#include <aws/common/string.h>

#include <aws/io/channel_bootstrap.h>
#include <aws/io/event_loop.h>
#include <aws/io/logging.h>
#include <aws/io/socket.h>
#include <aws/io/tls_channel_handler.h>

#include <aws/http/connection.h>
#include <aws/http/connection_manager.h>
#include <aws/http/http.h>
#include <aws/http/proxy.h>

#include "http_connection_manager.h"

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
 * Connection manager binding, persists across the lifetime of the native object.
 */
struct http_connection_manager_binding {
    JavaVM *jvm;
    jweak java_http_conn_manager;
    struct aws_http_connection_manager *manager;
};

static void s_destroy_manager_binding(struct http_connection_manager_binding *binding, JNIEnv *env) {
    if (binding == NULL || env == NULL) {
        return;
    }

    if (binding->java_http_conn_manager != NULL) {
        (*env)->DeleteWeakGlobalRef(env, binding->java_http_conn_manager);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static void s_on_http_conn_manager_shutdown_complete_callback(void *user_data) {

    struct http_connection_manager_binding *binding = (struct http_connection_manager_binding *)user_data;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION_MANAGER, "ConnManager Shutdown Complete");
    jobject java_http_conn_manager = (*env)->NewLocalRef(env, binding->java_http_conn_manager);
    if (java_http_conn_manager != NULL) {
        (*env)->CallVoidMethod(
            env, java_http_conn_manager, http_client_connection_manager_properties.onShutdownComplete);

        AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));
        (*env)->DeleteLocalRef(env, java_http_conn_manager);
    }

    // We're done with this wrapper, free it.
    JavaVM *jvm = binding->jvm;
    s_destroy_manager_binding(binding, env);

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerNew(
    JNIEnv *env,
    jclass jni_class,
    jobject conn_manager_jobject,
    jlong jni_client_bootstrap,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jlong jni_tls_connection_options,
    jint jni_window_size,
    jbyteArray jni_endpoint,
    jint jni_port,
    jint jni_max_conns,
    jint jni_proxy_connection_type,
    jbyteArray jni_proxy_host,
    jint jni_proxy_port,
    jlong jni_proxy_tls_context,
    jint jni_proxy_authorization_type,
    jbyteArray jni_proxy_authorization_username,
    jbyteArray jni_proxy_authorization_password,
    jboolean jni_manual_window_management,
    jlong jni_max_connection_idle_in_milliseconds,
    jlong jni_monitoring_throughput_threshold_in_bytes_per_second,
    jint jni_monitoring_failure_interval_in_seconds,
    jint jni_expected_protocol_version) {

    (void)jni_class;
    (void)jni_expected_protocol_version;

    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;
    struct aws_socket_options *socket_options = (struct aws_socket_options *)jni_socket_options;
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_ctx;
    struct aws_tls_connection_options *tls_connection_options =
        (struct aws_tls_connection_options *)jni_tls_connection_options;
    struct http_connection_manager_binding *binding = NULL;

    if (!client_bootstrap) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap can't be null");
        return (jlong)NULL;
    }

    if (!socket_options) {
        aws_jni_throw_runtime_exception(env, "SocketOptions can't be null");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jbyteArray_acquire(env, jni_endpoint);

    if (jni_port <= 0 || 65535 < jni_port) {
        aws_jni_throw_runtime_exception(env, "Port must be between 1 and 65535");
        goto cleanup;
    }

    if (jni_window_size <= 0) {
        aws_jni_throw_runtime_exception(env, "Window Size must be > 0");
        goto cleanup;
    }

    if (jni_max_conns <= 0) {
        aws_jni_throw_runtime_exception(env, "Max Connections must be > 0");
        goto cleanup;
    }

    uint16_t port = (uint16_t)jni_port;

    bool new_tls_conn_opts = (jni_tls_ctx != 0 && !tls_connection_options);

    struct aws_tls_connection_options tls_conn_options;
    AWS_ZERO_STRUCT(tls_conn_options);
    if (new_tls_conn_opts) {
        aws_tls_connection_options_init_from_ctx(&tls_conn_options, tls_ctx);
        aws_tls_connection_options_set_server_name(&tls_conn_options, allocator, &endpoint);
        tls_connection_options = &tls_conn_options;
    }

    binding = aws_mem_calloc(allocator, 1, sizeof(struct http_connection_manager_binding));
    AWS_FATAL_ASSERT(binding);
    binding->java_http_conn_manager = (*env)->NewWeakGlobalRef(env, conn_manager_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_http_connection_manager_options manager_options;
    AWS_ZERO_STRUCT(manager_options);

    manager_options.bootstrap = client_bootstrap;
    manager_options.initial_window_size = (size_t)jni_window_size;
    manager_options.socket_options = socket_options;
    manager_options.tls_connection_options = tls_connection_options;
    manager_options.host = endpoint;
    manager_options.port = port;
    manager_options.max_connections = (size_t)jni_max_conns;
    manager_options.shutdown_complete_callback = &s_on_http_conn_manager_shutdown_complete_callback;
    manager_options.shutdown_complete_user_data = binding;
    manager_options.monitoring_options = NULL;
    /* TODO: this variable needs to be renamed in aws-c-http. Come back and change it next revision. */
    manager_options.enable_read_back_pressure = jni_manual_window_management;
    manager_options.max_connection_idle_in_milliseconds = jni_max_connection_idle_in_milliseconds;

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

    binding->manager = aws_http_connection_manager_new(allocator, &manager_options);
    if (binding->manager == NULL) {
        aws_jni_throw_runtime_exception(
            env, "Failed to create connection manager: %s", aws_error_str(aws_last_error()));
    }

    aws_http_proxy_options_jni_clean_up(
        env, &proxy_options, jni_proxy_host, jni_proxy_authorization_username, jni_proxy_authorization_password);

    if (new_tls_conn_opts) {
        aws_tls_connection_options_clean_up(&tls_conn_options);
    }

cleanup:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_endpoint, endpoint);

    if (binding->manager == NULL) {
        s_destroy_manager_binding(binding, env);
        binding = NULL;
    }

    return (jlong)binding;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerRelease(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_conn_manager_binding) {

    (void)jni_class;

    struct http_connection_manager_binding *binding =
        (struct http_connection_manager_binding *)jni_conn_manager_binding;
    struct aws_http_connection_manager *conn_manager = binding->manager;

    if (!conn_manager) {
        aws_jni_throw_runtime_exception(env, "Connection Manager can't be null");
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION, "Releasing ConnManager: id: %p", (void *)conn_manager);
    aws_http_connection_manager_release(conn_manager);
}

/********************************************************************************************************************/

static void s_destroy_connection_binding(struct aws_http_connection_binding *binding, JNIEnv *env) {
    if (binding == NULL || env == NULL) {
        return;
    }

    if (binding->java_acquire_connection_future != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_acquire_connection_future);
    }

    if (binding->manager != NULL && binding->connection != NULL) {
        aws_http_connection_manager_release_connection(binding->manager, binding->connection);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static void s_on_http_conn_acquisition_callback(
    struct aws_http_connection *connection,
    int error_code,
    void *user_data) {

    struct aws_http_connection_binding *binding = (struct aws_http_connection_binding *)user_data;
    binding->connection = connection;

    /********** JNI ENV ACQUIRE **********/
    JNIEnv *env = aws_jni_acquire_thread_env(binding->jvm);
    if (env == NULL) {
        /* If we can't get an environment, then the JVM is probably shutting down.  Don't crash. */
        return;
    }

    jint jni_error_code = (jint)error_code;

    AWS_LOGF_DEBUG(
        AWS_LS_HTTP_CONNECTION,
        "ConnManager Acquired Conn: conn: %p, manager: %p, err_code: %d,  err_str: %s",
        (void *)connection,
        (void *)binding->manager,
        error_code,
        aws_error_str(error_code));

    (*env)->CallStaticVoidMethod(
        env,
        http_client_connection_properties.http_client_connection_class,
        http_client_connection_properties.on_connection_acquired_method_id,
        binding->java_acquire_connection_future,
        (jlong)binding,
        jni_error_code);

    AWS_FATAL_ASSERT(!aws_jni_check_and_clear_exception(env));

    JavaVM *jvm = binding->jvm;
    if (error_code) {
        s_destroy_connection_binding(binding, env);
    }

    aws_jni_release_thread_env(jvm, env);
    /********** JNI ENV RELEASE **********/
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerAcquireConnection(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_conn_manager_binding,
        jobject acquire_future) {

    (void)jni_class;

    struct http_connection_manager_binding *manager_binding =
        (struct http_connection_manager_binding *)jni_conn_manager_binding;
    struct aws_http_connection_manager *conn_manager = manager_binding->manager;

    if (!conn_manager) {
        aws_jni_throw_runtime_exception(env, "Connection Manager can't be null");
        return;
    }

    jobject future_ref = (*env)->NewGlobalRef(env, acquire_future);
    if (future_ref == NULL) {
        aws_jni_throw_runtime_exception(
            env, "httpClientConnectionManagerAcquireConnection: failed to obtain ref to future");
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION, "Requesting a new connection from conn_manager: %p", (void *)conn_manager);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_http_connection_binding *connection_binding =
        aws_mem_calloc(allocator, 1, sizeof(struct aws_http_connection_binding));
    connection_binding->java_acquire_connection_future = future_ref;
    connection_binding->manager = conn_manager;

    jint jvmresult = (*env)->GetJavaVM(env, &connection_binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_http_connection_manager_acquire_connection(
        conn_manager, &s_on_http_conn_acquisition_callback, (void *)connection_binding);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnection_httpClientConnectionReleaseManaged(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection_binding) {

    (void)jni_class;

    struct aws_http_connection_binding *binding = (struct aws_http_connection_binding *)jni_connection_binding;

    struct aws_http_connection_manager *conn_manager = binding->manager;
    struct aws_http_connection *conn = binding->connection;

    if (!conn_manager) {
        aws_jni_throw_runtime_exception(env, "Connection Manager can't be null");
        return;
    }

    if (!conn) {
        aws_jni_throw_runtime_exception(env, "Connection can't be null");
        return;
    }

    AWS_LOGF_DEBUG(
        AWS_LS_HTTP_CONNECTION,
        "ConnManager Releasing Conn: manager: %p, conn: %p",
        (void *)conn_manager,
        (void *)conn);

    s_destroy_connection_binding(binding, env);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

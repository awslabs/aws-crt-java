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

#include "crt.h"
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
#include <aws/http/connection_manager.h>
#include <aws/http/http.h>

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

struct http_conn_manager_callback_data {
    JavaVM *jvm;
    jweak java_http_conn_manager;
};

static void s_on_http_conn_manager_shutdown_complete_callback(void *user_data) {

    struct http_conn_manager_callback_data *callback = (struct http_conn_manager_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION_MANAGER, "ConnManager Shutdown Complete");
    jobject java_http_conn_manager = (*env)->NewLocalRef(env, callback->java_http_conn_manager);
    if (java_http_conn_manager != NULL) {
        (*env)->CallVoidMethod(
            env, java_http_conn_manager, http_client_connection_manager_properties.onShutdownComplete);

        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
        (*env)->DeleteLocalRef(env, java_http_conn_manager);
    }

    // We're done with this callback data, free it.
    (*env)->DeleteWeakGlobalRef(env, callback->java_http_conn_manager);
    aws_mem_release(aws_jni_get_allocator(), user_data);
}

void aws_http_proxy_options_jni_init(
    JNIEnv *env,
    struct aws_http_proxy_options *options,
    struct aws_tls_connection_options *tls_options,
    jbyteArray proxy_host,
    uint16_t proxy_port,
    jbyteArray proxy_authorization_username,
    jbyteArray proxy_authorization_password,
    int proxy_authorization_type,
    struct aws_tls_ctx *proxy_tls_ctx) {

    struct aws_allocator *allocator = aws_jni_get_allocator();

    options->port = proxy_port;
    options->auth_type = proxy_authorization_type;

    if (proxy_host != NULL) {
        options->host = aws_jni_byte_cursor_from_jbyteArray_acquire(env, proxy_host);
    }

    if (proxy_authorization_username != NULL) {
        options->auth_username = aws_jni_byte_cursor_from_jbyteArray_acquire(env, proxy_authorization_username);
    }

    if (proxy_authorization_password != NULL) {
        options->auth_password = aws_jni_byte_cursor_from_jbyteArray_acquire(env, proxy_authorization_password);
    }

    if (proxy_tls_ctx != NULL) {
        aws_tls_connection_options_init_from_ctx(tls_options, proxy_tls_ctx);
        aws_tls_connection_options_set_server_name(tls_options, allocator, &options->host);
        options->tls_options = tls_options;
    }
}

void aws_http_proxy_options_jni_clean_up(
    JNIEnv *env,
    struct aws_http_proxy_options *options,
    jbyteArray proxy_host,
    jbyteArray proxy_authorization_username,
    jbyteArray proxy_authorization_password) {

    if (options->host.ptr != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, proxy_host, options->host);
    }

    if (options->auth_username.ptr != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, proxy_authorization_username, options->auth_username);
    }

    if (options->auth_password.ptr != NULL) {
        aws_jni_byte_cursor_from_jbyteArray_release(env, proxy_authorization_password, options->auth_password);
    }

    if (options->tls_options != NULL) {
        aws_tls_connection_options_clean_up(options->tls_options);
    }
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerNew(
    JNIEnv *env,
    jclass jni_class,
    jobject conn_manager_jobject,
    jlong jni_client_bootstrap,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jint jni_window_size,
    jbyteArray jni_endpoint,
    jint jni_port,
    jint jni_max_conns,
    jbyteArray jni_proxy_host,
    jint jni_proxy_port,
    jlong jni_proxy_tls_context,
    jint jni_proxy_authorization_type,
    jbyteArray jni_proxy_authorization_username,
    jbyteArray jni_proxy_authorization_password,
    jboolean jni_manual_window_management,
    jlong jni_max_connection_idle_in_milliseconds,
    jlong jni_monitoring_throughput_threshold_in_bytes_per_second,
    jint jni_monitoring_failure_interval_in_seconds) {

    (void)jni_class;

    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;
    struct aws_socket_options *socket_options = (struct aws_socket_options *)jni_socket_options;
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_ctx;
    struct aws_http_connection_manager *conn_manager = NULL;

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

    int use_tls = (jni_tls_ctx != 0);

    struct aws_tls_connection_options tls_conn_options;
    AWS_ZERO_STRUCT(tls_conn_options);

    if (use_tls) {
        aws_tls_connection_options_init_from_ctx(&tls_conn_options, tls_ctx);
        aws_tls_connection_options_set_server_name(&tls_conn_options, allocator, &endpoint);
    }

    struct http_conn_manager_callback_data *callback_data =
        aws_mem_acquire(allocator, sizeof(struct http_conn_manager_callback_data));
    AWS_FATAL_ASSERT(callback_data);
    callback_data->java_http_conn_manager = (*env)->NewWeakGlobalRef(env, conn_manager_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    struct aws_http_connection_manager_options manager_options;
    AWS_ZERO_STRUCT(manager_options);

    manager_options.bootstrap = client_bootstrap;
    manager_options.initial_window_size = (size_t)jni_window_size;
    manager_options.socket_options = socket_options;
    manager_options.tls_connection_options = NULL;
    manager_options.host = endpoint;
    manager_options.port = port;
    manager_options.max_connections = (size_t)jni_max_conns;
    manager_options.shutdown_complete_callback = &s_on_http_conn_manager_shutdown_complete_callback;
    manager_options.shutdown_complete_user_data = callback_data;
    manager_options.monitoring_options = NULL;
    /* TODO: this variable needs to be renamed in aws-c-http. Come back and change it next revision. */
    manager_options.enable_read_back_pressure = jni_manual_window_management;
    manager_options.max_connection_idle_in_milliseconds = jni_max_connection_idle_in_milliseconds;

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

    conn_manager = aws_http_connection_manager_new(allocator, &manager_options);

    aws_http_proxy_options_jni_clean_up(
        env, &proxy_options, jni_proxy_host, jni_proxy_authorization_username, jni_proxy_authorization_password);

    if (use_tls) {
        aws_tls_connection_options_clean_up(&tls_conn_options);
    }

cleanup:
    aws_jni_byte_cursor_from_jbyteArray_release(env, jni_endpoint, endpoint);

    return (jlong)conn_manager;
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerRelease(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_conn_manager) {

    (void)jni_class;

    struct aws_http_connection_manager *conn_manager = (struct aws_http_connection_manager *)jni_conn_manager;

    if (!conn_manager) {
        aws_jni_throw_runtime_exception(env, "Connection Manager can't be null");
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION, "Releasing ConnManager: id: %p", (void *)jni_conn_manager);
    aws_http_connection_manager_release(conn_manager);
}

static void s_on_http_conn_acquisition_callback(
    struct aws_http_connection *connection,
    int error_code,
    void *user_data) {

    struct http_conn_manager_callback_data *callback = (struct http_conn_manager_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);
    jlong jni_connection = (jlong)connection;
    jint jni_error_code = (jint)error_code;

    AWS_LOGF_DEBUG(
        AWS_LS_HTTP_CONNECTION,
        "ConnManager Acquired Conn: conn: %p, err_code: %d,  err_str: %s",
        (void *)connection,
        error_code,
        aws_error_str(error_code));

    jobject java_http_conn_manager = (*env)->NewLocalRef(env, callback->java_http_conn_manager);
    if (java_http_conn_manager != NULL) {
        (*env)->CallVoidMethod(
            env,
            java_http_conn_manager,
            http_client_connection_manager_properties.onConnectionAcquired,
            jni_connection,
            jni_error_code);

        (*env)->DeleteLocalRef(env, java_http_conn_manager);

        AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
    }

    // We're done with this callback data, free it.
    (*env)->DeleteWeakGlobalRef(env, callback->java_http_conn_manager);
    aws_mem_release(aws_jni_get_allocator(), user_data);
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerAcquireConnection(
        JNIEnv *env,
        jclass jni_class,
        jobject conn_manager_jobject,
        jlong jni_conn_manager) {

    (void)jni_class;

    struct aws_http_connection_manager *conn_manager = (struct aws_http_connection_manager *)jni_conn_manager;

    if (!conn_manager) {
        aws_jni_throw_runtime_exception(env, "Connection Manager can't be null");
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION, "Requesting a new connection from conn_manager: %p", (void *)conn_manager);

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct http_conn_manager_callback_data *callback_data =
        aws_mem_acquire(allocator, sizeof(struct http_conn_manager_callback_data));
    callback_data->java_http_conn_manager = (*env)->NewWeakGlobalRef(env, conn_manager_jobject);

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    aws_http_connection_manager_acquire_connection(
        conn_manager, &s_on_http_conn_acquisition_callback, (void *)callback_data);
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerReleaseConnection(
        JNIEnv *env,
        jclass jni_class,
        jlong jni_conn_manager,
        jlong jni_conn) {

    (void)jni_class;

    struct aws_http_connection_manager *conn_manager = (struct aws_http_connection_manager *)jni_conn_manager;
    struct aws_http_connection *conn = (struct aws_http_connection *)jni_conn;

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

    aws_http_connection_manager_release_connection(conn_manager, conn);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

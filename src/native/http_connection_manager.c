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
#include <aws/common/mutex.h>
#include <aws/common/string.h>
#include <aws/common/thread.h>

#include <aws/io/channel.h>
#include <aws/io/channel_bootstrap.h>
#include <aws/io/event_loop.h>
#include <aws/io/host_resolver.h>
#include <aws/io/logging.h>
#include <aws/io/socket.h>
#include <aws/io/socket_channel_handler.h>
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

struct http_conn_manager_binding {
    JavaVM *jvm;
    jweak java_http_conn_manager;
    jobject java_client_bootstrap;
    jobject java_tls_context;
    jobject java_proxy_tls_context;
};

static void s_aws_http_conn_manager_binding_destroy(struct http_conn_manager_binding *binding) {
    if (binding == NULL) {
        return;
    }

    JNIEnv *env = aws_jni_get_thread_env(binding->jvm);

    if (binding->java_http_conn_manager != NULL) {
        (*env)->DeleteWeakGlobalRef(env, binding->java_http_conn_manager);
    }

    if (binding->java_client_bootstrap != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_client_bootstrap);
    }

    if (binding->java_tls_context != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_tls_context);
    }

    if (binding->java_proxy_tls_context != NULL) {
        (*env)->DeleteGlobalRef(env, binding->java_proxy_tls_context);
    }

    aws_mem_release(aws_jni_get_allocator(), binding);
}

static struct http_conn_manager_binding *s_aws_http_conn_manager_binding_new(
    JNIEnv *env,
    jobject java_conn_manager,
    jobject client_bootstrap,
    jobject tls_context,
    jobject proxy_tls_context) {
    struct http_conn_manager_binding *binding =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct http_conn_manager_binding));
    if (binding == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &binding->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    binding->java_http_conn_manager = (*env)->NewWeakGlobalRef(env, java_conn_manager);
    if (binding->java_http_conn_manager == NULL) {
        goto on_error;
    }

    binding->java_client_bootstrap = (*env)->NewGlobalRef(env, client_bootstrap);
    if (binding->java_client_bootstrap == NULL) {
        goto on_error;
    }

    if (tls_context != NULL) {
        binding->java_tls_context = (*env)->NewGlobalRef(env, tls_context);
        if (binding->java_tls_context == NULL) {
            goto on_error;
        }
    }

    if (proxy_tls_context != NULL) {
        binding->java_proxy_tls_context = (*env)->NewGlobalRef(env, proxy_tls_context);
        if (binding->java_proxy_tls_context == NULL) {
            goto on_error;
        }
    }

    return binding;

on_error:

    s_aws_http_conn_manager_binding_destroy(binding);
    return NULL;
}

static void s_on_http_conn_manager_shutdown_complete_callback(void *user_data) {

    struct http_conn_manager_binding *binding = (struct http_conn_manager_binding *)user_data;

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION_MANAGER, "ConnManager Shutdown Complete");

    // We're done with the binding, free it.
    s_aws_http_conn_manager_binding_destroy(binding);
}

JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerNew(
    JNIEnv *env,
    jclass jni_class,
    jobject conn_manager_jobject,
    jobject java_client_bootstrap,
    jlong jni_socket_options,
    jobject java_tls_ctx,
    jint jni_window_size,
    jstring jni_endpoint,
    jint jni_port,
    jint jni_max_conns,
    jstring jni_proxy_host,
    jint jni_proxy_port,
    jobject java_proxy_tls_context,
    jint jni_proxy_authorization_type,
    jstring jni_proxy_authorization_username,
    jstring jni_proxy_authorization_password) {

    (void)jni_class;

    struct http_conn_manager_binding *binding = NULL;
    struct aws_client_bootstrap *client_bootstrap = NULL;
    struct aws_socket_options *socket_options = NULL;
    struct aws_tls_ctx *tls_ctx = NULL;
    struct aws_tls_ctx *proxy_tls_ctx = NULL;
    struct aws_http_connection_manager *conn_manager = NULL;
    struct aws_byte_cursor endpoint = {0};
    struct aws_tls_connection_options tls_conn_options = {0};
    struct aws_byte_cursor proxy_host = {0};
    struct aws_byte_cursor proxy_authorization_username = {0};
    struct aws_byte_cursor proxy_authorization_password = {0};
    struct aws_tls_connection_options proxy_tls_conn_options = {0};

    if (java_client_bootstrap == NULL) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap can't be null");
        return (jlong)NULL;
    }
    client_bootstrap = (struct aws_client_bootstrap *)(*env)->CallLongMethod(
        env, java_client_bootstrap, crt_resource_properties.get_native_handle_method_id);
    if (client_bootstrap == NULL) {
        aws_jni_throw_runtime_exception(env, "Java ClientBootstrap is not bound to a native client bootstrap");
        return (jlong)NULL;
    }

    socket_options = (struct aws_socket_options *)jni_socket_options;
    if (!socket_options) {
        aws_jni_throw_runtime_exception(env, "SocketOptions can't be null");
        return (jlong)NULL;
    }

    if (java_tls_ctx != NULL) {
        tls_ctx = (struct aws_tls_ctx *)(*env)->CallLongMethod(
            env, java_tls_ctx, crt_resource_properties.get_native_handle_method_id);
        if (tls_ctx == NULL) {
            aws_jni_throw_runtime_exception(env, "Java Tls Context is not bound to a native tls context");
            return (jlong)NULL;
        }
    }

    if (java_proxy_tls_context != NULL) {
        proxy_tls_ctx = (struct aws_tls_ctx *)(*env)->CallLongMethod(
            env, java_proxy_tls_context, crt_resource_properties.get_native_handle_method_id);
        if (proxy_tls_ctx == NULL) {
            aws_jni_throw_runtime_exception(env, "Java Proxy Tls Context is not bound to a native tls context");
            return (jlong)NULL;
        }
    }

    if (jni_port <= 0 || 65535 < jni_port) {
        aws_jni_throw_runtime_exception(env, "Port must be between 1 and 65535");
        return (jlong)NULL;
    }

    if (jni_window_size <= 0) {
        aws_jni_throw_runtime_exception(env, "Window Size must be > 0");
        return (jlong)NULL;
    }

    if (jni_max_conns <= 0) {
        aws_jni_throw_runtime_exception(env, "Max Connections must be > 0");
        return (jlong)NULL;
    }

    /*
     * This is where we start performing operations that must be undone on error.  From now on, failures must jump to
     * cleanup.
     */
    struct aws_allocator *allocator = aws_jni_get_allocator();
    endpoint = aws_jni_byte_cursor_from_jstring_acquire(env, jni_endpoint);

    uint16_t port = (uint16_t)jni_port;

    bool use_tls = (tls_ctx != 0);
    if (use_tls) {
        aws_tls_connection_options_init_from_ctx(&tls_conn_options, tls_ctx);
        aws_tls_connection_options_set_server_name(&tls_conn_options, allocator, &endpoint);
    }

    binding = s_aws_http_conn_manager_binding_new(
        env, conn_manager_jobject, java_client_bootstrap, java_tls_ctx, java_proxy_tls_context);
    if (binding == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to build binding object");
        goto cleanup;
    }

    struct aws_http_connection_manager_options manager_options = {0};
    manager_options.bootstrap = client_bootstrap;
    manager_options.initial_window_size = (size_t)jni_window_size;
    manager_options.socket_options = socket_options;
    manager_options.tls_connection_options = NULL;
    manager_options.host = endpoint;
    manager_options.port = port;
    manager_options.max_connections = (size_t)jni_max_conns;
    manager_options.shutdown_complete_callback = &s_on_http_conn_manager_shutdown_complete_callback;
    manager_options.shutdown_complete_user_data = binding;

    if (use_tls) {
        manager_options.tls_connection_options = &tls_conn_options;
    }

    struct aws_http_proxy_options proxy_options;
    AWS_ZERO_STRUCT(proxy_options);

    if (jni_proxy_host != NULL) {
        proxy_host = aws_jni_byte_cursor_from_jstring_acquire(env, jni_proxy_host);
        if (proxy_host.ptr == NULL) {
            goto cleanup;
        }
    }

    if (jni_proxy_authorization_username != NULL) {
        proxy_authorization_username = aws_jni_byte_cursor_from_jstring_acquire(env, jni_proxy_authorization_username);
        if (proxy_authorization_username.ptr == NULL) {
            goto cleanup;
        }
    }

    if (jni_proxy_authorization_password != NULL) {
        proxy_authorization_password = aws_jni_byte_cursor_from_jstring_acquire(env, jni_proxy_authorization_password);
        if (proxy_authorization_password.ptr == NULL) {
            goto cleanup;
        }
    }

    if (jni_proxy_host != NULL) {
        proxy_options.host = proxy_host;
        proxy_options.port = (uint16_t)jni_proxy_port;
        proxy_options.auth_type = jni_proxy_authorization_type;
        proxy_options.auth_username = proxy_authorization_username;
        proxy_options.auth_password = proxy_authorization_password;

        if (proxy_tls_ctx != 0) {
            aws_tls_connection_options_init_from_ctx(&proxy_tls_conn_options, proxy_tls_ctx);
            aws_tls_connection_options_set_server_name(&proxy_tls_conn_options, allocator, &proxy_options.host);
            proxy_options.tls_options = &proxy_tls_conn_options;
        }

        manager_options.proxy_options = &proxy_options;
    }

    conn_manager = aws_http_connection_manager_new(allocator, &manager_options);

cleanup:

    if (conn_manager == NULL) {
        s_aws_http_conn_manager_binding_destroy(binding);
    }

    if (proxy_host.ptr != NULL) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_proxy_host, proxy_host);
    }

    if (proxy_authorization_username.ptr != NULL) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_proxy_authorization_username, proxy_authorization_username);
    }

    if (proxy_authorization_password.ptr != NULL) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_proxy_authorization_password, proxy_authorization_password);
    }

    aws_tls_connection_options_clean_up(&tls_conn_options);
    aws_tls_connection_options_clean_up(&proxy_tls_conn_options);

    if (endpoint.ptr != NULL) {
        aws_jni_byte_cursor_from_jstring_release(env, jni_endpoint, endpoint);
    }

    return (jlong)conn_manager;
}

JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerRelease(
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

struct http_conn_manager_acquire_connection_callback_data {
    JavaVM *jvm;
    jweak java_http_conn_manager;
    jweak java_completable_future;
    struct aws_http_connection_manager *connection_manager;
};

static void s_http_conn_manager_acquire_connection_callback_data_destroy(
    struct http_conn_manager_acquire_connection_callback_data *callback_data) {
    if (callback_data == NULL) {
        return;
    }

    JNIEnv *env = aws_jni_get_thread_env(callback_data->jvm);

    if (callback_data->java_http_conn_manager) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_http_conn_manager);
    }

    if (callback_data->java_completable_future) {
        (*env)->DeleteWeakGlobalRef(env, callback_data->java_completable_future);
    }

    aws_mem_release(aws_jni_get_allocator(), callback_data);
}

static struct http_conn_manager_acquire_connection_callback_data *
    s_http_conn_manager_acquire_connection_callback_data_new(
        JNIEnv *env,
        jobject java_http_connection_manager,
        jlong jni_conn_manager,
        jobject java_completable_future) {
    struct http_conn_manager_acquire_connection_callback_data *callback_data =
        aws_mem_calloc(aws_jni_get_allocator(), 1, sizeof(struct http_conn_manager_acquire_connection_callback_data));
    if (callback_data == NULL) {
        return NULL;
    }

    jint jvmresult = (*env)->GetJavaVM(env, &callback_data->jvm);
    (void)jvmresult;
    AWS_FATAL_ASSERT(jvmresult == 0);

    callback_data->connection_manager = (struct aws_http_connection_manager *)jni_conn_manager;

    callback_data->java_http_conn_manager = (*env)->NewWeakGlobalRef(env, java_http_connection_manager);
    if (callback_data->java_http_conn_manager == NULL) {
        goto on_error;
    }

    callback_data->java_completable_future = (*env)->NewWeakGlobalRef(env, java_completable_future);
    if (callback_data->java_completable_future == NULL) {
        goto on_error;
    }

    return callback_data;

on_error:

    s_http_conn_manager_acquire_connection_callback_data_destroy(callback_data);

    return NULL;
}

static void s_on_http_conn_acquisition_callback(
    struct aws_http_connection *connection,
    int error_code,
    void *user_data) {

    struct http_conn_manager_acquire_connection_callback_data *callback =
        (struct http_conn_manager_acquire_connection_callback_data *)user_data;
    JNIEnv *env = aws_jni_get_thread_env(callback->jvm);

    AWS_LOGF_DEBUG(
        AWS_LS_HTTP_CONNECTION,
        "ConnManager Acquired Conn: conn: %p, err_code: %d,  err_str: %s",
        (void *)connection,
        error_code,
        aws_error_str(error_code));

    jobject java_http_conn_manager = (*env)->NewLocalRef(env, callback->java_http_conn_manager);
    jobject java_completable_future = (*env)->NewLocalRef(env, callback->java_completable_future);
    struct aws_http_connection_manager *manager = callback->connection_manager;

    /*
     * Assume failure unless everything is non-null and we create the java connection successfully.
     */
    bool is_failure = true;

    if (connection != NULL && java_http_conn_manager != NULL && java_completable_future != NULL) {
        jobject http_connection = (*env)->NewObject(
            env,
            http_client_connection_properties.http_client_connection_class,
            http_client_connection_properties.constructor,
            java_http_conn_manager,
            (jlong)connection);
        if (http_connection != NULL) {
            (*env)->CallVoidMethod(
                env, java_completable_future, completable_future_properties.complete_method_id, http_connection);
            is_failure = false;
        }
    }

    if (is_failure) {
        if (connection != NULL) {
            aws_http_connection_manager_release_connection(manager, connection);
        }

        if (java_completable_future != NULL) {
            int ec = (error_code == AWS_ERROR_SUCCESS) ? AWS_ERROR_UNKNOWN : error_code;
            jobject http_exception = (*env)->NewObject(
                env, http_exception_properties.http_exception_class, http_exception_properties.constructor, (jint)ec);

            // null or non-null, we need to fail the future
            (*env)->CallVoidMethod(
                env,
                java_completable_future,
                completable_future_properties.complete_exceptionally_method_id,
                http_exception);
        }
    }

    if (java_http_conn_manager != NULL) {
        (*env)->DeleteLocalRef(env, java_http_conn_manager);
    }

    if (java_completable_future != NULL) {
        (*env)->DeleteLocalRef(env, java_completable_future);
    }

    // We're done with this callback data, free it.
    s_http_conn_manager_acquire_connection_callback_data_destroy(callback);
}

JNIEXPORT void JNICALL
    Java_software_amazon_awssdk_crt_http_HttpClientConnectionManager_httpClientConnectionManagerAcquireConnection(
        JNIEnv *env,
        jclass jni_class,
        jobject conn_manager_jobject,
        jobject completable_future,
        jlong jni_conn_manager) {

    (void)jni_class;

    struct aws_http_connection_manager *conn_manager = (struct aws_http_connection_manager *)jni_conn_manager;

    if (!conn_manager) {
        aws_jni_throw_runtime_exception(env, "Connection Manager can't be null");
        return;
    }

    AWS_LOGF_DEBUG(AWS_LS_HTTP_CONNECTION, "Requesting a new connection from conn_manager: %p", (void *)conn_manager);

    struct http_conn_manager_acquire_connection_callback_data *callback_data =
        s_http_conn_manager_acquire_connection_callback_data_new(
            env, conn_manager_jobject, jni_conn_manager, completable_future);
    if (callback_data == NULL) {
        aws_jni_throw_runtime_exception(env, "Failed to build acquisition callback binding");
        return;
    }

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

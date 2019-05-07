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

#include <crt.h>
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
#include <aws/http/http.h>
#include <aws/http/request_response.h>

/*******************************************************************************
 * JNI class field/method maps
 ******************************************************************************/

/* methods of HttpConnection.AsyncCallback */
static struct {
    jmethodID on_success;
    jmethodID on_failure;
} s_async_callback = {0};

void s_cache_http_async_callback(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/AsyncCallback");
    assert(cls);
    s_async_callback.on_success = (*env)->GetMethodID(env, cls, "onSuccess", "()V");
    assert(s_async_callback.on_success);
    s_async_callback.on_failure = (*env)->GetMethodID(env, cls, "onFailure", "(Ljava/lang/Throwable;)V");
    assert(s_async_callback.on_failure);
}

/* methods of HttpConnection */
static struct {
    jmethodID on_connection_complete;
    jmethodID on_connection_shutdown;
} s_http_connection;

void s_cache_http_connection(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "software/amazon/awssdk/crt/http/HttpConnection");
    assert(cls);
    s_http_connection.on_connection_complete = (*env)->GetMethodID(env, cls, "onConnectionComplete", "(I)V");
    assert(s_http_connection.on_connection_complete);

    s_http_connection.on_connection_shutdown = (*env)->GetMethodID(env, cls, "onConnectionShutdown", "(I)V");
    assert(s_http_connection.on_connection_shutdown);
}

/*******************************************************************************
 * http_jni_connection - represents an aws_http_connection to Java
 ******************************************************************************/
struct http_jni_connection {
    struct aws_http_connection *native_http_conn;
    struct aws_socket_options *socket_options;
    struct aws_tls_connection_options *tls_options;

    JavaVM *jvm;
    jobject java_http_conn; /* The Java HttpConnection instance */
};

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

static void s_on_http_conn_setup(struct aws_http_connection *connection, int error_code, void *user_data) {
    struct http_jni_connection *http_jni_conn = (struct http_jni_connection *)user_data;
    assert(http_jni_conn);

    // Save the native pointer
    http_jni_conn->native_http_conn = connection;

    assert(s_http_connection.on_connection_complete);
    assert(http_jni_conn->java_http_conn);
    // Call the Java Object's "onComplete" callback
    if (http_jni_conn->java_http_conn) {
        JNIEnv *env = aws_jni_get_thread_env(http_jni_conn->jvm);
        (*env)->CallVoidMethod(
            env, http_jni_conn->java_http_conn, s_http_connection.on_connection_complete, error_code);
    }
}

static void s_on_http_conn_shutdown(struct aws_http_connection *connection, int error_code, void *user_data) {
    struct http_jni_connection *http_jni_conn = (struct http_jni_connection *)user_data;

    // Call the Java Object's "onShutdown" callback
    if (http_jni_conn->java_http_conn) {
        JNIEnv *env = aws_jni_get_thread_env(http_jni_conn->jvm);
        (*env)->CallVoidMethod(
            env, http_jni_conn->java_http_conn, s_http_connection.on_connection_shutdown, error_code);
    }
}

/**
 * Create a new aws_http_request_options struct with default values
 */
JNIEXPORT long JNICALL Java_software_amazon_awssdk_crt_http_HttpConnection_httpConnectionNew(
    JNIEnv *env,
    jclass jni_class,
    jobject http_conn_jobject,
    jlong jni_client_bootstrap,
    jlong jni_socket_options,
    jlong jni_tls_ctx,
    jstring jni_endpoint,
    jint jni_port) {

    struct aws_client_bootstrap *client_bootstrap = (struct aws_client_bootstrap *)jni_client_bootstrap;
    struct aws_socket_options *socket_options = (struct aws_socket_options *)jni_socket_options;
    struct aws_tls_ctx *tls_ctx = (struct aws_tls_ctx *)jni_tls_ctx;

    if (!client_bootstrap) {
        aws_jni_throw_runtime_exception(env, "ClientBootstrap can't be null");
        return (jlong)NULL;
    }

    if (!socket_options) {
        aws_jni_throw_runtime_exception(env, "SocketOptions can't be null");
        return (jlong)NULL;
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    struct aws_byte_cursor endpoint = aws_jni_byte_cursor_from_jstring(env, jni_endpoint);

    if (jni_port <= 0 || 65535 < jni_port) {
        aws_jni_throw_runtime_exception(env, "Port must be between 1 and 65535");
        return (jlong)NULL;
    }

    uint16_t port = (uint16_t)jni_port;

    int use_tls = (jni_tls_ctx != 0);

    struct aws_tls_connection_options tls_conn_options = {0};

    if (use_tls) {
        aws_tls_connection_options_init_from_ctx(&tls_conn_options, tls_ctx);
        aws_tls_connection_options_set_server_name(&tls_conn_options, allocator, &endpoint);
    }

    /* any error after this point needs to jump to error_cleanup */
    struct http_jni_connection *http_jni_conn = aws_mem_acquire(allocator, sizeof(struct http_jni_connection));
    if (!http_jni_conn) {
        aws_jni_throw_runtime_exception(env, "Out of memory allocating JNI connection");
        goto error_cleanup;
    }

    // Create a new reference to the HttpConnection Object.
    http_jni_conn->java_http_conn = (*env)->NewGlobalRef(env, http_conn_jobject);

    // GetJavaVM() reference doesn't need a NewGlobalRef() call since it's global by default
    jint jvmresult = (*env)->GetJavaVM(env, &http_jni_conn->jvm);
    (void)jvmresult;
    assert(jvmresult == 0);

    struct aws_http_client_connection_options http_options = AWS_HTTP_CLIENT_CONNECTION_OPTIONS_INIT;
    http_options.self_size = sizeof(struct aws_http_client_connection_options);
    http_options.allocator = allocator;
    http_options.bootstrap = client_bootstrap;
    http_options.host_name = endpoint;
    http_options.port = port;
    http_options.socket_options = socket_options;
    http_options.tls_options = NULL;
    http_options.user_data = http_jni_conn;
    http_options.on_setup = s_on_http_conn_setup;
    http_options.on_shutdown = s_on_http_conn_shutdown;

    if (use_tls) {
        http_options.tls_options = &tls_conn_options;
    }

    int rc = aws_http_client_connect(&http_options);

    if (use_tls) {
        aws_tls_connection_options_clean_up(&tls_conn_options);
    }

    if (rc != AWS_OP_SUCCESS) {
        aws_jni_throw_runtime_exception(env, "There was an error calling aws_http_client_connect()");
    }

    return (jlong)http_jni_conn;

error_cleanup:
    if (http_jni_conn) {
        aws_mem_release(allocator, http_jni_conn);
    }

    return (jlong)NULL;
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpConnection_httpConnectionShutdown(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {

    struct http_jni_connection *http_jni_conn = (struct http_jni_connection *)jni_connection;
    aws_http_connection_close(http_jni_conn->native_http_conn);
}

JNIEXPORT void JNICALL Java_software_amazon_awssdk_crt_http_HttpConnection_httpConnectionRelease(
    JNIEnv *env,
    jclass jni_class,
    jlong jni_connection) {

    struct http_jni_connection *http_jni_conn = (struct http_jni_connection *)jni_connection;
    if (http_jni_conn->java_http_conn) {
        // Delete our reference to the HttpConnection Object from the JVM.
        (*env)->DeleteGlobalRef(env, http_jni_conn->java_http_conn);
        http_jni_conn->java_http_conn = NULL;
    }
    if (http_jni_conn->native_http_conn) {
        aws_http_connection_release(http_jni_conn->native_http_conn);
    }

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mem_release(allocator, http_jni_conn);
}

#if UINTPTR_MAX == 0xffffffff
#    if defined(_MSC_VER)
#        pragma warning(pop)
#    else
#        pragma GCC diagnostic pop
#    endif
#endif

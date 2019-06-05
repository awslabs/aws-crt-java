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

#include <aws/http/connection.h>
#include <aws/http/http.h>

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

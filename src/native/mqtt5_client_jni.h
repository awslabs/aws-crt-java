/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
#ifndef AWS_JNI_CLIENT_H
#define AWS_JNI_CLIENT_H

struct aws_mqtt5_client_java_jni {
    struct aws_mqtt5_client *client;
    jobject jni_client;
    JavaVM *jvm;

    struct aws_tls_connection_options tls_options;
    struct aws_tls_connection_options http_proxy_tls_options;

    jobject jni_publish_events;
    jobject jni_lifecycle_events;
};

#endif /* AWS_JNI_CLIENT_H */

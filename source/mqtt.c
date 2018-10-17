/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
#include <jni.h>
#include <crt.h>

#include <aws/common/condition_variable.h>
#include <aws/common/mutex.h>
#include <aws/common/string.h>
#include <aws/common/thread.h>
#include <aws/io/channel.h>
#include <aws/io/channel_bootstrap.h>
#include <aws/io/event_loop.h>
#include <aws/io/host_resolver.h>
#include <aws/io/socket.h>
#include <aws/io/socket_channel_handler.h>
#include <aws/io/tls_channel_handler.h>
#include <string.h>

JNIEXPORT void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1connect(JNIEnv *env, jobject jni_mqtt, jstring jni_host_name, jshort jni_port, jobject jni_params) {
    struct aws_byte_cursor host_name = aws_jni_byte_cursor_from_jstring(env, jni_host_name);
    (void)host_name;
}

JNIEXPORT void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1disconnect(JNIEnv *env, jobject jni_mqtt) {

}

JNIEXPORT void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1subscribe(JNIEnv *env, jobject jni_mqtt) {

}

JNIEXPORT void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1unsubscribe(JNIEnv *env, jobject jni_mqtt) {

}

JNIEXPORT void JNICALL Java_com_amazon_aws_MQTTClient_mqtt_1publish(JNIEnv *env, jobject jni_mqtt) {

}

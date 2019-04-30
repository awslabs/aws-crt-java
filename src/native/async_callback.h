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

#ifndef AWS_JNI_CRT_ASYNC_CALLBACK_H
#define AWS_JNI_CRT_ASYNC_CALLBACK_H

#include <jni.h>

/* methods of AsyncCallback */
struct crt_async_callback {
    jmethodID on_success;
    jmethodID on_failure;
};

extern struct crt_async_callback g_async_callback;

void s_cache_async_callback(JNIEnv *env);

#endif /* AWS_JNI_CRT_ASYNC_CALLBACK_H */

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

#include <aws/common/string.h>
#include <aws/io/tls_channel_handler.h>

#include "crt.h"
#include "java_class_ids.h"
#include "tls_context_pkcs11_options.h"

// TODO - document
struct custom_key_op_handler {
    JavaVM *jvm;
    jobject jni_handler;

    // TODO - add reference counting here?
    // TODO - need to add callback when no longer used.
};
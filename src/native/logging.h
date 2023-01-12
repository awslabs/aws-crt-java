#ifndef AWS_JNI_LOGGING_H
#define AWS_JNI_LOGGING_H

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <jni.h>

/*******************************************************************************
 * aws_jni_cleanup_logging - cleans up the native logger; invoked on atexit
 ******************************************************************************/
void aws_jni_cleanup_logging(void);

#endif /* AWS_JNI_LOGGING_H */

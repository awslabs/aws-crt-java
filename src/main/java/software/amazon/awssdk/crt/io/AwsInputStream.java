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
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;

/**
 * This class wraps an aws_input_stream native resource.  Java InputStream is not an appropriate
 * abstraction for what we need (arbitrarily seekable).
 */
public class AwsInputStream extends CrtResource {

    public AwsInputStream() {}

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Cleans up the stream's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            awsInputStreamDestroy(getNativeHandle());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void awsInputStreamDestroy(long stream_handler);
};

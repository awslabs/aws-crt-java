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

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtResource;

/**
 * An HttpStream represents a single Http Request/Response pair within a HttpClientConnection, and wraps the native resources
 * from the aws-c-http library.
 *
 * Can be used to update the Window size, or to abort the stream early in the middle of sending/receiving Http Bodies.
 */
public class HttpStream extends CrtResource {

    /* Native code will call this constructor during HttpClientConnection.makeRequest() */
    protected HttpStream(long ptr) {
        acquireNativeHandle(ptr);
    }

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
            httpStreamRelease(getNativeHandle());
        }
    }

    /**
     * Opens the Sliding Read/Write Window by the number of bytes passed as an argument for this HttpStream.
     *
     * This function should only be called if the user application previously returned less than the length of the input
     * ByteBuffer from a onResponseBody() call in a HttpStreamResponseHandler, and should be &lt;= to the total number of
     * un-acked bytes.
     *
     * @param windowSize How many bytes to increment the sliding window by.
     */
    public void incrementWindow(int windowSize) {
        if (windowSize < 0) {
            throw new IllegalArgumentException("windowSize must be >= 0. Actual value: " + windowSize);
        }
        if (!isNull()) {
            httpStreamIncrementWindow(getNativeHandle(), windowSize);
        }
    }

    /**
     * Activates the client stream.
     */
    public void activate() {
        if (!isNull()) {
            httpStreamActivate(getNativeHandle(), this);
        }
    }

    /**
     * Retrieves the Http Response Status Code
     * @return The Http Response Status Code
     */
    public int getResponseStatusCode() {
        if (!isNull()) {
            return httpStreamGetResponseStatusCode(getNativeHandle());
        }
        throw new IllegalStateException("Can't get Status Code on Closed Stream");
    }

    private static native void httpStreamRelease(long http_stream);
    private static native void httpStreamIncrementWindow(long http_stream, int window_size);
    private static native void httpStreamActivate(long http_stream, HttpStream streamObj);
    private static native int  httpStreamGetResponseStatusCode(long http_stream);
}

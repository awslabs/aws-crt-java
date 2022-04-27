/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * An base class represents a single Http Request/Response for both HTTP/1.1 and
 * HTTP/2 and wraps the native resources from the aws-c-http library.
 *
 * Can be used to update the Window size, or to abort the stream early in the
 * middle of sending/receiving Http Bodies.
 */
public class HttpStreamBase extends CrtResource {

    /*
     * Native code will call this constructor during
     * HttpClientConnection.makeRequest()
     */
    protected HttpStreamBase(long ptr) {
        acquireNativeHandle(ptr);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the
     * native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    /**
     * Cleans up the stream's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            httpStreamBaseRelease(getNativeHandle());
        }
    }

    /*******************************************************************************
     * Shared method
     ******************************************************************************/
    /**
     * Opens the Sliding Read/Write Window by the number of bytes passed as an
     * argument for this HttpStream.
     *
     * This function should only be called if the user application previously
     * returned less than the length of the input
     * ByteBuffer from a onResponseBody() call in a HttpStreamResponseHandler, and
     * should be &lt;= to the total number of
     * un-acked bytes.
     *
     * @param windowSize How many bytes to increment the sliding window by.
     */
    public void incrementWindow(int windowSize) {
        if (windowSize < 0) {
            throw new IllegalArgumentException("windowSize must be >= 0. Actual value: " + windowSize);
        }
        if (!isNull()) {
            httpStreamBaseIncrementWindow(getNativeHandle(), windowSize);
        }
    }

    /**
     * Activates the client stream.
     */
    public void activate() {
        if (!isNull()) {
            httpStreamBaseActivate(getNativeHandle(), this);
        }
    }

    /**
     * Retrieves the Http Response Status Code
     *
     * @return The Http Response Status Code
     */
    public int getResponseStatusCode() {
        if (!isNull()) {
            return httpStreamBaseGetResponseStatusCode(getNativeHandle());
        }
        throw new IllegalStateException("Can't get Status Code on Closed Stream");
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native void httpStreamBaseRelease(long http_stream);

    private static native void httpStreamBaseIncrementWindow(long http_stream, int window_size);

    private static native void httpStreamBaseActivate(long http_stream, HttpStreamBase streamObj);

    private static native int httpStreamBaseGetResponseStatusCode(long http_stream);
}

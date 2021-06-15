/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpStream;

import static software.amazon.awssdk.crt.CRT.awsLastError;

/**
 * This class wraps aws-c-http to provide the basic HTTP request/response functionality via the AWS Common Runtime.
 *
 * HttpClientConnection represents a single connection to a HTTP service endpoint.
 *
 * This class is not thread safe and should not be called from different threads.
 */
public class HttpClientConnection extends CrtResource {

    private final HttpClientConnectionManager manager;
    private long nativeManager;

    protected HttpClientConnection(HttpClientConnectionManager manager, long connection) {
        this(manager, connection, manager.getNativeHandle());
    }

    protected HttpClientConnection(HttpClientConnectionManager manager, long connection, long nativeManager) {
        acquireNativeHandle(connection);
        addReferenceTo(manager);
        this.manager = manager;
        this.nativeManager = nativeManager;
    }

    /**
     * Schedules an HttpRequest on the Native EventLoop for this HttpClientConnection.
     *
     * @param request The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native EventLoop
     * @throws CrtRuntimeException if stream creation fails
     * @return The HttpStream that represents this Request/Response Pair. It can be closed at any time during the
     *          request/response, but must be closed by the user thread making this request when it's done.
     */
    public HttpStream makeRequest(HttpRequest request, HttpStreamResponseHandler streamHandler) throws CrtRuntimeException {
        if (isNull()) {
            throw new IllegalStateException("HttpClientConnection has been closed, can't make requests on it.");
        }

        HttpStream stream = httpClientConnectionMakeRequest(getNativeHandle(),
            request.marshalForJni(),
            request.getBodyStream(),
            new HttpStreamResponseHandlerNativeAdapter(streamHandler));
        if (stream == null || stream.isNull()) {
            throw new CrtRuntimeException(awsLastError());
        }

        return stream;
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Releases this HttpClientConnection back into the Connection Pool, and allows another Request to acquire this connection.
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()){
            manager.releaseConnectionPointer(getNativeHandle(), nativeManager);
        }
    }

    /**
     * @deprecated Given the strong coupling between connection and manager, it doesn't make sense to ever close the
     * http connection.  If you're shutting down, give it back to the manager and close the manager.
     */
    @Deprecated
    public void shutdown() {
        httpClientConnectionShutdown(getNativeHandle());
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native HttpStream httpClientConnectionMakeRequest(long connection,
                                                                     byte[] marshalledRequest,
                                                                     HttpRequestBodyStream bodyStream,
                                                                     HttpStreamResponseHandlerNativeAdapter responseHandler) throws CrtRuntimeException;

    private static native void httpClientConnectionShutdown(long connection) throws CrtRuntimeException;
}

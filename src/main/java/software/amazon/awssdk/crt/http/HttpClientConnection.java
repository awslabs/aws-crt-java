/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;

import software.amazon.awssdk.crt.CRT;
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

    protected HttpClientConnection(long connectionBinding) {
        acquireNativeHandle(connectionBinding);
    }

    /**
     * Schedules an HttpRequest on the Native EventLoop for this HttpClientConnection specific to HTTP/1.1 connection.
     *
     * @param request The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native EventLoop
     * @throws CrtRuntimeException if stream creation fails
     * @return The HttpStream that represents this Request/Response Pair. It can be closed at any time during the
     *          request/response, but must be closed by the user thread making this request when it's done.
     */
    public HttpStream makeRequest(HttpRequest request, HttpStreamResponseHandler streamHandler)
            throws CrtRuntimeException {
        if (isNull()) {
            throw new IllegalStateException("HttpClientConnection has been closed, can't make requests on it.");
        }
        if (getVersion() == HttpVersion.HTTP_2) {
            throw new IllegalArgumentException("HTTP/1 only method called on an HTTP/2 connection.");
        }
        HttpStreamBase stream = httpClientConnectionMakeRequest(getNativeHandle(),
                request.marshalForJni(),
                request.getBodyStream(),
                new HttpStreamResponseHandlerNativeAdapter(streamHandler));

        return (HttpStream)stream;
    }

    /**
     * Schedules an HttpRequestBase on the Native EventLoop for this HttpClientConnection applies to both HTTP/2 and HTTP/1.1 connection.
     *
     * @param request The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native EventLoop
     * @throws CrtRuntimeException if stream creation fails
     * @return The HttpStream that represents this Request/Response Pair. It can be closed at any time during the
     *          request/response, but must be closed by the user thread making this request when it's done.
     */
    public HttpStreamBase makeRequest(HttpRequestBase request, HttpStreamBaseResponseHandler streamHandler) throws CrtRuntimeException {
        if (isNull()) {
            throw new IllegalStateException("HttpClientConnection has been closed, can't make requests on it.");
        }
        HttpStreamBase stream = httpClientConnectionMakeRequest(getNativeHandle(),
                request.marshalForJni(),
                request.getBodyStream(),
                new HttpStreamResponseHandlerNativeAdapter(streamHandler));

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
            httpClientConnectionReleaseManaged(getNativeHandle());
        }
    }

    /**
     * Shuts down the underlying http connection.  Even if this function is called, you still need to properly close
     * the connection as well in order to release the native resources.
     */
    public void shutdown() {
        httpClientConnectionShutdown(getNativeHandle());
    }

    public HttpVersion getVersion() {
        short version = httpClientConnectionGetVersion(getNativeHandle());
        return HttpVersion.getEnumValueFromInteger((int) version);
    };

    /** Called from Native when a new connection is acquired **/
    private static void onConnectionAcquired(CompletableFuture<HttpClientConnection> acquireFuture, long nativeConnectionBinding, int errorCode) {
        if (errorCode != CRT.AWS_CRT_SUCCESS) {
            acquireFuture.completeExceptionally(new HttpException(errorCode));
            return;
        }
        if (HttpVersion.getEnumValueFromInteger(
                (int) httpClientConnectionGetVersion(nativeConnectionBinding)) == HttpVersion.HTTP_2) {
            HttpClientConnection h2Conn = new Http2ClientConnection(nativeConnectionBinding);
            if (!acquireFuture.complete(h2Conn)) {
                // future was already completed/cancelled, return it immediately to the pool to not leak it
                h2Conn.close();
            }
        } else {
            HttpClientConnection conn = new HttpClientConnection(nativeConnectionBinding);
            if (!acquireFuture.complete(conn)) {
                // future was already completed/cancelled, return it immediately to the pool to not leak it
                conn.close();
            }
        }
    }

    /**
     * Certain exceptions thrown by this HTTP API are from invalid boundary conditions
     * that, if the request isn't altered, will never succeed. This function returns
     * false if the exception is caused by such a condition.
     * <p>
     * It does not mean the request that generated the error SHOULD be retried:
     * only that as far as this client is concerned, the request might,
     * possibly succeed with a subsequent attempt.
     *
     * @param exception, an exception thrown by the CRT HTTP API--for any reason.
     * @return true if the error that generated the exception makes sense for a retry, and
     * false otherwise.
     */
    public static boolean isErrorRetryable(HttpException exception) {
        // why take an exception rather than an error code directly?
        // to give us breathing room for changing our mind later about how we convey
        // retry information on the exceptions we throw.
        return isErrorRetryable(exception.getErrorCode());
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native HttpStreamBase httpClientConnectionMakeRequest(long connectionBinding,
                                                                     byte[] marshalledRequest,
                                                                     HttpRequestBodyStream bodyStream,
                                                                     HttpStreamResponseHandlerNativeAdapter responseHandler) throws CrtRuntimeException;

    private static native void httpClientConnectionShutdown(long connectionBinding) throws CrtRuntimeException;

    private static native void httpClientConnectionReleaseManaged(long connectionBinding) throws CrtRuntimeException;
    private static native short httpClientConnectionGetVersion(long connectionBinding) throws CrtRuntimeException;

    private static native boolean isErrorRetryable(int errorCode);
}

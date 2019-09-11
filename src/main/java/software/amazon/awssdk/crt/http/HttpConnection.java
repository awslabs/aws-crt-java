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
import software.amazon.awssdk.crt.CrtRuntimeException;


/**
 * This class wraps aws-c-http to provide the basic HTTP request/response functionality via the AWS Common Runtime.
 *
 * HttpConnection represents a single connection to a HTTP service endpoint.
 *
 * This class is not thread safe and should not be called from different threads.
 */
public class HttpConnection extends CrtResource {

    private final HttpConnectionPoolManager manager;

    protected HttpConnection(HttpConnectionPoolManager manager, long connection) {
        acquire(connection);
        this.manager = addReferenceTo(manager);
    }

    /**
     * Schedules an HttpRequest on the Native EventLoop for this HttpConnection.
     *
     * @param request The Request to make to the Server.
     * @param reqOptions The Http Request Options
     * @param streamHandler The Stream Handler to be called from the Native EventLoop
     * @throws CrtRuntimeException
     * @return The HttpStream that represents this Request/Response Pair. It can be closed at any time during the
     *          request/response, but must be closed by the user thread making this request when it's done.
     */
    public HttpStream makeRequest(HttpRequest request, HttpRequestOptions reqOptions, CrtHttpStreamHandler streamHandler) throws CrtRuntimeException {
        if (isNull()) {
            throw new IllegalStateException("HttpConnection has been closed, can't make requests on it.");
        }

        if (reqOptions.getBodyBufferSize() > manager.getWindowSize()) {
            throw new IllegalArgumentException("Response Body Buffer can't be > than Window Size");
        }

        HttpStream stream = httpConnectionMakeRequest(native_ptr(),
                reqOptions.getBodyBufferSize(),
                request.getMethod(),
                request.getEncodedPath(),
                request.getHeaders(),
                streamHandler);

        if (stream == null || stream.isNull()) {
            throw new IllegalStateException("HttpStream is null");
        }

        return stream;
    }

    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Releases this HttpConnection back into the Connection Pool, and allows another Request to acquire this connection.
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()){
            manager.releaseConnectionPointer(native_ptr());
        }
    }


    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native HttpStream httpConnectionMakeRequest(long connection,
                                                               int respBodyBufSize,
                                                               String method,
                                                               String uri,
                                                               HttpHeader[] headers,
                                                               CrtHttpStreamHandler crtHttpStreamHandler) throws CrtRuntimeException;
}

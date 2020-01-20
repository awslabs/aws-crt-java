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

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpStream;

/**
 * This class wraps aws-c-http to provide the basic HTTP request/response functionality via the AWS Common Runtime.
 *
 * HttpClientConnection represents a single connection to a HTTP service endpoint.
 *
 * This class is not thread safe and should not be called from different threads.
 */
public class HttpClientConnection extends CrtResource {

    private final HttpClientConnectionManager manager;

    protected HttpClientConnection(HttpClientConnectionManager manager, long connection) {
        acquireNativeHandle(connection, (x)->manager.releaseConnectionPointer(connection));
        this.manager = manager;
    }

    /**
     * Schedules an HttpRequest on the Native EventLoop for this HttpClientConnection.
     *
     * @param request The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native EventLoop
     * @throws CrtRuntimeException
     * @return The HttpStream that represents this Request/Response Pair. It can be closed at any time during the
     *          request/response, but must be closed by the user thread making this request when it's done.
     */
    public CompletableFuture<HttpStream> makeRequest(HttpRequest request, HttpStreamResponseHandler streamHandler) throws CrtRuntimeException {
        if (isNull()) {
            throw new IllegalStateException("HttpClientConnection has been closed, can't make requests on it.");
        }

        CompletableFuture<HttpStream> streamFuture = new CompletableFuture<>();

            try {
                HttpStream stream = httpClientConnectionMakeRequest(this,
                    getNativeHandle(),
                    request.getMethod(),
                    request.getEncodedPath(),
                    request.getHeadersAsArray(),
                    request.getBodyStream(),
                    streamHandler);
                if (stream == null || stream.isNull()) {
                    streamFuture.completeExceptionally(new RuntimeException("HttpStream creation failed"));
                }
                streamFuture.complete(stream);
            } catch (Exception e) {
                streamFuture.completeExceptionally(e);
            }

        return streamFuture;
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native HttpStream httpClientConnectionMakeRequest(HttpClientConnection thisObj,
                                                                     long connectionHandle,
                                                                     String method,
                                                                     String uri,
                                                                     HttpHeader[] headers,
                                                                     HttpRequestBodyStream bodyStream,
                                                                     HttpStreamResponseHandler responseHandler) throws CrtRuntimeException;
}

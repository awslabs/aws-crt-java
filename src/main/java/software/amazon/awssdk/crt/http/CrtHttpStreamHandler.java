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

import java.nio.ByteBuffer;

/**
 * Interface that Native code knows how to call when handling Http Request/Responses
 *
 * Maps 1-1 to the Native Http API here: https://github.com/awslabs/aws-c-http/blob/master/include/aws/http/request_response.h
 */
public interface CrtHttpStreamHandler {

    /**
     * Called from Native when new Http Headers have been received.
     * Note that this function may be called multiple times as HTTP headers are received.
     *
     * @param stream The HttpStream object
     * @param responseStatusCode The HTTP Response Status Code
     * @param nextHeaders The headers received in the latest IO event.
     */
    void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType, HttpHeader[] nextHeaders);

    /**
     * Called from Native once all HTTP Headers are processed. Will not be called if there are no Http Headers in the
     * response. Guaranteed to be called exactly once if there is at least 1 Header.
     *
     * @param stream The HttpStream object
     * @param blockType The type of the header block, corresponds to {@link HttpHeaderBlock}
     */
    default void onResponseHeadersDone(HttpStream stream, int blockType) {
        /* Optional Callback, do nothing by default */
    }

    /**
     * Called when new Response Body bytes have been received. Note that this function may be called multiple times over
     * the lifetime of an HttpClientConnection as bytes are received.
     *
     * Users must read all data from bodyBytesIn before returning. If "bodyBytesIn.remaining() > 0" after this method
     * returns, then Native will assume there was a processing failure and abort the connection.
     *
     * Do NOT keep a reference to this ByteBuffer past the lifetime of this function call. The CommonRuntime reserves
     * the right to use DirectByteBuffers pointing to memory that only lives as long as the function call.
     *
     * Sliding Window:
     * The Native HttpClientConnection EventLoop will keep sending data until the end of the sliding Window is reached.
     * The user application is responsible for setting the initial Window size appropriately when creating the
     * HttpClientConnection, and for incrementing the sliding window appropriately throughout the lifetime of the HttpStream.
     *
     * For more info, see:
     *  - https://en.wikipedia.org/wiki/Sliding_window_protocol
     *
     * @param bodyBytesIn The HTTP Body Bytes received in the last IO Event.
     * @return The number of bytes to move the sliding window by. Repeatedly returning zero will eventually cause the
     *          sliding window to fill up and data to stop flowing until the user slides the window back open.
     */
    default int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
        /* Optional Callback, ignore incoming response body by default unless user wants to capture it. */
        return bodyBytesIn.length;
    }

    /**
     * Called from Native when the Response has completed.
     * @param stream
     * @param errorCode
     */
    void onResponseComplete(HttpStream stream, int errorCode);

    /**
     * Called from Native when the Http Request has a Body (Eg PUT/POST requests).
     * Note that this function may be called many times as Native sends the Request Body.
     *
     * Do NOT keep a reference to this ByteBuffer past the lifetime of this function call. The CommonRuntime reserves
     * the right to use DirectByteBuffers pointing to memory that only lives as long as the function call.
     *
     * @param stream The HttpStream for this Request/Response Pair
     * @param bodyBytesOut The Buffer to write the Request Body Bytes to.
     * @return True if Request body is complete, false otherwise.
     */
    default boolean sendRequestBody(HttpStream stream, ByteBuffer bodyBytesOut) {
        /* Optional Callback, return empty request body by default unless user wants to return one. */
        return true;
    }

}
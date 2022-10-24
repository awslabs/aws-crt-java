/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

/**
 * Interface that Native code knows how to call when handling Http Responses for HTTP/1.1 only.
 * You can use HttpStreamBaseResponseHandler instead to adapt both HTTP/1.1 and HTTP/2
 *
 * Maps 1-1 to the Native Http API here: https://github.com/awslabs/aws-c-http/blob/master/include/aws/http/request_response.h
 */
public interface HttpStreamResponseHandler {

    /**
     * Called from Native when new Http Headers have been received.
     * Note that this function may be called multiple times as HTTP headers are received.
     *
     * @param stream The HttpStream object
     * @param responseStatusCode The HTTP Response Status Code
     * @param blockType The HTTP header block type
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
     * <p>
     * Note that if {@link HttpClientConnectionManagerOptions#withManualWindowManagement} was set true,
     * you must manage the flow-control window.
     * The flow-control window shrinks as you receive body data via this callback.
     * Whenever the flow-control window reaches zero, data will stop downloading.
     * To keep data flowing, you must increment the window by returning a number
     * from this method, or by calling {@link HttpStreamBase#incrementWindow}.
     *
     * @param stream The HTTP Stream the body was delivered to
     * @param bodyBytesIn The HTTP Body Bytes received in the last IO Event.
     * @return The number of bytes to increment the window by
     *          (calling {@link HttpStreamBase#incrementWindow} has the same effect).
     *          This value is ignored if "manual window management" is disabled.
     * @see HttpClientConnectionManagerOptions#withManualWindowManagement
     */
    default int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
        /* Optional Callback, ignore incoming response body by default unless user wants to capture it. */
        return bodyBytesIn.length;
    }

    /**
     * Called from Native when the Response has completed.
     * @param stream completed stream
     * @param errorCode resultant errorCode for the response
     */
    void onResponseComplete(HttpStream stream, int errorCode);

}

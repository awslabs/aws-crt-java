/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.nio.ByteBuffer;

/**
 * Response handler implementation used by the native http layer
 */
class HttpStreamResponseHandlerNativeAdapter {
    private HttpStreamResponseHandler responseHandler;
    private HttpStreamBaseResponseHandler responseBaseHandler;

    HttpStreamResponseHandlerNativeAdapter(HttpStreamResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        this.responseBaseHandler = null;
    }

    HttpStreamResponseHandlerNativeAdapter(HttpStreamBaseResponseHandler responseBaseHandler) {
        this.responseBaseHandler = responseBaseHandler;
        this.responseHandler = null;
    }

    void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType, ByteBuffer headersBlob) {
        HttpHeader[] headersArray = HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob);
        if (this.responseBaseHandler != null) {
            responseBaseHandler.onResponseHeaders(stream, responseStatusCode, blockType, headersArray);
        } else {
            responseHandler.onResponseHeaders((HttpStream) stream, responseStatusCode, blockType, headersArray);
        }
    }

    void onResponseHeadersDone(HttpStreamBase stream, int blockType) {
        if (this.responseBaseHandler != null) {
            responseBaseHandler.onResponseHeadersDone(stream, blockType);
        } else {
            responseHandler.onResponseHeadersDone((HttpStream) stream, blockType);
        }
    }

    int onResponseBody(HttpStreamBase stream, byte[] body) {
        if (this.responseBaseHandler != null) {
            return responseBaseHandler.onResponseBody(stream, body);
        } else {
            return responseHandler.onResponseBody((HttpStream) stream, body);
        }
    }

    void onResponseComplete(HttpStreamBase stream, int errorCode) {
        if (this.responseBaseHandler != null) {
            responseBaseHandler.onResponseComplete(stream, errorCode);
        } else {
            responseHandler.onResponseComplete((HttpStream) stream, errorCode);
        }
    }
}

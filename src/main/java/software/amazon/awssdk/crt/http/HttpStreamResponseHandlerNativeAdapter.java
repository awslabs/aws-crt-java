/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.nio.ByteBuffer;

class HttpStreamResponseHandlerNativeAdapter {
    private HttpStreamResponseHandler responseHandler;

    HttpStreamResponseHandlerNativeAdapter(HttpStreamResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType, ByteBuffer headersBlob) {
        HttpHeader[] headersArray = HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob);
        responseHandler.onResponseHeaders(stream, responseStatusCode, blockType, headersArray);
    }

    void onResponseHeadersDone(HttpStream stream, int blockType) {
        responseHandler.onResponseHeadersDone(stream, blockType);
    }

    int onResponseBody(HttpStream stream, ByteBuffer bodyBytesIn) {
        byte[] body = new byte[bodyBytesIn.limit()];
        bodyBytesIn.get(body);
        return responseHandler.onResponseBody(stream, body);
    }

    void onResponseComplete(HttpStream stream, int errorCode) {
        responseHandler.onResponseComplete(stream, errorCode);
    }
}

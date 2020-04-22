/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import java.util.ArrayList;
import java.util.List;

class HttpStreamResponseHandlerNativeAdapter {
    private HttpStreamResponseHandler responseHandler;

    HttpStreamResponseHandlerNativeAdapter(HttpStreamResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType, ByteBuffer headersBlob) {
        List<HttpHeader> headers = new ArrayList<>(16);

        while(headersBlob.hasRemaining()) {
            int nameLen = headersBlob.getInt();
            byte[] nameBuf = new byte[nameLen];
            headersBlob.get(nameBuf);
            int valLen = headersBlob.getInt();
            byte[] valueBuf = new byte[valLen];
            headersBlob.get(valueBuf);
            headers.add(new HttpHeader(nameBuf, valueBuf));
        }

        HttpHeader[] headersArray = new HttpHeader[headers.size()];
        responseHandler.onResponseHeaders(stream, responseStatusCode, blockType, headers.toArray(headersArray));
    }

    void onResponseHeadersDone(HttpStream stream, int blockType) {
        responseHandler.onResponseHeadersDone(stream, blockType);
    }

    int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
        return responseHandler.onResponseBody(stream, bodyBytesIn);
    }

    void onResponseComplete(HttpStream stream, int errorCode) {
        responseHandler.onResponseComplete(stream, errorCode);
    }
}

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

/**
 * This class provides access to Http Request handling options.
 */
public class HttpRequestOptions {
    public static final int DEFAULT_RESP_BODY_BUFFER_SIZE = 1024 * 1024; // 1 MB

    private int bodyBufferSize = DEFAULT_RESP_BODY_BUFFER_SIZE;

    public HttpRequestOptions() {
    }

    public int getBodyBufferSize() {
        return bodyBufferSize;
    }

    public void setBodyBufferSize(int bodyBufferSize) {
        this.bodyBufferSize = bodyBufferSize;
    }

    public HttpRequestOptions withBodyBufferSize(int bodyBufferSize) {
        setBodyBufferSize(bodyBufferSize);
        return this;
    }
}

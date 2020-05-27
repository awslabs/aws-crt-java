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
 * Interface that Native code knows how to call when handling Http Request bodies
 *
 */
public interface HttpRequestBodyStream {


    /**
     * Called from Native when the Http Request has a Body (Eg PUT/POST requests).
     * Note that this function may be called many times as Native sends the Request Body.
     *
     * Do NOT keep a reference to this ByteBuffer past the lifetime of this function call. The CommonRuntime reserves
     * the right to use DirectByteBuffers pointing to memory that only lives as long as the function call.
     *
     * @param bodyBytesOut The Buffer to write the Request Body Bytes to.
     * @return True if Request body is complete, false otherwise.
     */
    default boolean sendRequestBody(ByteBuffer bodyBytesOut) {
        /* Optional Callback, return empty request body by default unless user wants to return one. */
        return true;
    }

    /**
     * Called from native when the processing needs the stream to rewind itself back to its beginning.
     * If the stream does not support rewinding or the rewind fails, false should be returned
     *
     * Signing requires a rewindable stream, but basic http does not.
     *
     * @return True if the stream was successfully rewound, false otherwise.
     */
    default boolean resetPosition() { return false; }

}

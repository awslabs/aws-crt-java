/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.crt.CRT;

public class Uri {
    static {
        new CRT();
    };

    public static String appendEncodingUriPath(String buffer, String cursor) {
        return new String(appendEncodingUriPath(buffer.getBytes(), cursor.getBytes()), StandardCharsets.UTF_8);
    }

    public static String appendEncodingUriParam(String buffer, String cursor) {
        return new String(appendEncodingUriParam(buffer.getBytes(), cursor.getBytes()), StandardCharsets.UTF_8);
    }

    public static String appendDecodingUri(String buffer, String cursor) {
        return new String(appendDecodingUri(buffer.getBytes(), cursor.getBytes()), StandardCharsets.UTF_8);
    }

    private static native byte[] appendEncodingUriPath(byte[] buffer, byte[] cursor);

    private static native byte[] appendEncodingUriParam(byte[] buffer, byte[] cursor);

    private static native byte[] appendDecodingUri(byte[] buffer, byte[] cursor);
}

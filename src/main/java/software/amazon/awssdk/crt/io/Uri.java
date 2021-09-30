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

    public static String appendEncodingUriPath(String encoded, String path) {
        return new String(appendEncodingUriPath(encoded.getBytes(), path.getBytes()), StandardCharsets.UTF_8);
    }

    public static String appendEncodingUriPath(String path) {
        return appendEncodingUriPath("", path);
    }

    public static String appendEncodingUriParam(String encoded, String param) {
        return new String(appendEncodingUriParam(encoded.getBytes(), param.getBytes()), StandardCharsets.UTF_8);
    }

    public static String appendEncodingUriParam(String param) {
        return appendEncodingUriParam("", param);
    }

    public static String appendDecodingUri(String base, String encoded) {
        return new String(appendDecodingUri(base.getBytes(), encoded.getBytes()), StandardCharsets.UTF_8);
    }

    public static String appendDecodingUri(String encoded) {
        return appendDecodingUri("", encoded);
    }

    private static native byte[] appendEncodingUriPath(byte[] encoding, byte[] path);

    private static native byte[] appendEncodingUriParam(byte[] encoding, byte[] param);

    private static native byte[] appendDecodingUri(byte[] base, byte[] encoded);
}

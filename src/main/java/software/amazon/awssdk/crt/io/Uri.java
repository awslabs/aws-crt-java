/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.crt.CRT;

/**
 * Wrapper around an http URI
 */
public class Uri {
    static {
        new CRT();
    };

    /**
     * Returns a concatenation of an encoded base, and the URI path encoding of a
     * string. This is the modified version of rfc3986 used by sigv4 signing.
     *
     * @param encoded The encoded original path.
     * @param path    The path to be encoded and appended to the original path
     *
     * @return concatenation
     */
    public static String appendEncodingUriPath(String encoded, String path) {
        return new String(
                appendEncodingUriPath(encoded.getBytes(StandardCharsets.UTF_8), path.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    /**
     * Returns the URI path encoding of a string. This is the modified version of
     * rfc3986 used by sigv4 signing.
     *
     * @param path The path to be encoded
     *
     * @return encoded path
     */
    public static String encodeUriPath(String path) {
        return appendEncodingUriPath("", path);
    }

    /**
     * Returns a concatenation of an encoded base, and the URI query param encoding
     * (passthrough alnum + '-' '_' '~' '.') of a UTF-8 string. For example, reading
     * "a b_c" would write "a%20b_c".
     *
     * @param encoded The encoded original param.
     * @param param   The param to be encoded and appended to the original param
     *
     * @return concatenation
     */
    public static String appendEncodingUriParam(String encoded, String param) {
        return new String(appendEncodingUriParam(encoded.getBytes(StandardCharsets.UTF_8),
                param.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * Returns the URI query param encoding (passthrough alnum + '-' '_' '~' '.') of
     * a UTF-8 string. For example, reading "a b_c" would write "a%20b_c".
     *
     * @param param The param to be encoded and appended to the original param
     *
     * @return encoded param
     */
    public static String encodeUriParam(String param) {
        return appendEncodingUriParam("", param);
    }

    /**
     * Returns a concatenation of a decoded base, and the URI decoding of a UTF-8
     * string, replacing %xx escapes by their single byte equivalent. For example,
     * reading "a%20b_c" would write "a b_c".
     *
     * @param base    The decoded base URI.
     * @param encoded The encoded URI to be decoded and appended to the base URI.
     *
     * @return concatenation
     */
    public static String appendDecodingUri(String base, String encoded) {
        return new String(
                appendDecodingUri(base.getBytes(StandardCharsets.UTF_8), encoded.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    /**
     * Returns the URI decoding of a UTF-8 string, replacing %xx escapes by their
     * single byte equivalent. For example, reading "a%20b_c" would write "a b_c".
     *
     * @param encoded The encoded URI to be decoded.
     *
     * @return decoded URI
     */
    public static String decodeUri(String encoded) {
        return appendDecodingUri("", encoded);
    }

    private static native byte[] appendEncodingUriPath(byte[] encoded, byte[] path);

    private static native byte[] appendEncodingUriParam(byte[] encoded, byte[] param);

    private static native byte[] appendDecodingUri(byte[] base, byte[] encoded);
}

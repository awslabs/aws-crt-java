/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.signing;

import software.amazon.awssdk.crt.http.HttpRequest;

/**
 * Internal utility/testing functions for verifying sigv4a signatures.
 */
public class AwsSigningUtils {

    public static boolean verifySigv4aEcdsaSignature(HttpRequest request, String expectedCanonicalRequest, AwsSigningConfig config, byte[] hexEncodedSignature, String verifierPubX, String verifierPubY) {
        return awsSigningUtilsVerifyEcdsaSignature(request, request.marshalForJni(), expectedCanonicalRequest, config, hexEncodedSignature, verifierPubX, verifierPubY);
    }

    public static boolean verifyRawSha256EcdsaSignature(byte[] stringToSign, byte[] hexEncodedSignature, String verifierPubX, String verifierPubY) {
        return awsSigningUtilsVerifyRawSha256EcdsaSignature(stringToSign, hexEncodedSignature, verifierPubX, verifierPubY);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native boolean awsSigningUtilsVerifyEcdsaSignature(
            HttpRequest request,
            byte[] marshalledRequest,
            String expectedCanonicalRequest,
            AwsSigningConfig config,
            byte[] hexEncodedSignature,
            String verifierPubX,
            String verifiedPubY);

    private static native boolean awsSigningUtilsVerifyRawSha256EcdsaSignature(
            byte[] stringToSign,
            byte[] hexEncodedSignature,
            String verifierPubX,
            String verifiedPubY);
}

/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

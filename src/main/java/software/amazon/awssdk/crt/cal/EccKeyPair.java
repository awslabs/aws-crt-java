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
package software.amazon.awssdk.crt.cal;

import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.CleanableCrtResource;

import java.util.HashMap;
import java.util.Map;

/**
 * This class puts an opaque wrapper around aws_ecc_key_pair from aws-c-cal.  Currently, it is only intended to be
 * cached and returned to native code by a signing invocation.
 *
 * If there's a compelling reason, we can add accessors and conversions to/from Java's KeyPair.
 */
public final class EccKeyPair extends CleanableCrtResource {

    /**
     * Enum for supported ECC curves
     * Needs to stay in sync with aws_ecc_curve_name
     */
    public enum AwsEccCurve {
        /** Nist standard P256 elliptic curve */
        AWS_ECDSA_P256(0),

        /** Nist standard P384 elliptic curve */
        AWS_ECDSA_P384(1);

        /**
         * Constructs a Java enum value from a native enum value as an integer
         * @param nativeValue native enum value
         */
        AwsEccCurve(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        /**
         * Gets the native enum value as an integer that is associated with this Java enum value
         * @return this value's associated native enum value
         */
        public int getNativeValue() { return nativeValue; }

        /**
         * Creates a Java enum value from a native enum value as an integer
         * @param value native enum value
         * @return the corresponding Java enum value
         */
        public static AwsEccCurve getEnumValueFromInteger(int value) {
            AwsEccCurve enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal ecc curve name value");
        }

        private static Map<Integer, AwsEccCurve> buildEnumMapping() {
            Map<Integer, AwsEccCurve> enumMapping = new HashMap<Integer, AwsEccCurve>();
            enumMapping.put(AWS_ECDSA_P256.getNativeValue(), AWS_ECDSA_P256);
            enumMapping.put(AWS_ECDSA_P384.getNativeValue(), AWS_ECDSA_P384);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsEccCurve> enumMapping = buildEnumMapping();
    }

    /**
     * Creates a new ecc key pair.  Only called from native at the moment.
     *
     * @param nativeHandle handle to the native ecc key pair object
     */
    private EccKeyPair(long nativeHandle) {
        acquireNativeHandle(nativeHandle, EccKeyPair::eccKeyPairRelease);
    }

    /**
     * Derives the associated ECC key from a pair of AWS credentials according to the sigv4a ecc key
     * derivation specification.
     *
     * @param credentials AWS credentials to derive the associated key for
     * @param curve ECC curve to use (only P256 is currently supported)
     * @return derived ecc key pair associated with the AWS credentials
     */
    static public EccKeyPair newDeriveFromCredentials(Credentials credentials, AwsEccCurve curve) {
        long nativeHandle = eccKeyPairNewFromCredentials(credentials, curve.getNativeValue());
        if (nativeHandle != 0) {
            return new EccKeyPair(nativeHandle);
        }

        return null;
    }

    /**
     * Sign a message using the ECC key pair via ECDSA
     * @param message message to sign
     * @return the ECDSA signature of the message
     */
    public byte[] signMessage(byte[] message) {
        return eccKeyPairSignMessage(getNativeHandle(), message);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long eccKeyPairNewFromCredentials(Credentials credentials, int curve);
    private static native void eccKeyPairRelease(long ecc_key_pair);

    private static native byte[] eccKeyPairSignMessage(long ecc_key_pair, byte[] message);
};

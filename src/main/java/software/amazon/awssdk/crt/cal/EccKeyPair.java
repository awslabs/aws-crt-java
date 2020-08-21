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
import software.amazon.awssdk.crt.CrtResource;

import java.util.HashMap;
import java.util.Map;

/**
 * This class puts an opaque wrapper around aws_ecc_key_pair from aws-c-cal.  Currently, it is only intended to be
 * cached and returned to native code by a signing invocation.
 *
 * If there's a compelling reason, we can add accessors and conversions to/from Java's KeyPair.
 */
public final class EccKeyPair extends CrtResource {

    /* Needs to stay in sync with aws_ecc_curve_name */
    public enum AwsEccCurve {
        AWS_ECDSA_P256(0),
        AWS_ECDSA_P384(1);

        AwsEccCurve(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

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
     */
    private EccKeyPair(long nativeHandle) {
        acquireNativeHandle(nativeHandle);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Releases the instance's reference to the underlying native key pair
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            eccKeyPairRelease(getNativeHandle());
        }
    }

    static public EccKeyPair newDeriveFromCredentials(Credentials credentials, AwsEccCurve curve) {
        long nativeHandle = eccKeyPairNewFromCredentials(credentials, curve.getNativeValue());
        if (nativeHandle != 0) {
            return new EccKeyPair(nativeHandle);
        }

        return null;
    }

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

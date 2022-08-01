/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.HashMap;
import java.util.Map;

/**
 * The signature of a TLS private key operation. Any custom private key operation handlers are expected to perform
 * operations on the input TLS data using the correct signature algorithm or fail the operation.
 */
public enum TlsSignatureAlgorithm {
    UNKNOWN(0), RSA(1), ECDSA(2);

    static Map<Integer, TlsSignatureAlgorithm> buildEnumMapping() {
        Map<Integer, TlsSignatureAlgorithm> enumMapping = new HashMap<Integer, TlsSignatureAlgorithm>();
        for (TlsSignatureAlgorithm i : TlsSignatureAlgorithm.values()) {
            enumMapping.put(i.nativeValue, i);
        }
        return enumMapping;
    }

    public static TlsSignatureAlgorithm getEnumValueFromInteger(int value) {
        TlsSignatureAlgorithm enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }
        throw new RuntimeException("Illegal TlsKeyOperation.TlsSignatureAlgorithm");
    }

    TlsSignatureAlgorithm(int nativeValue) {
        this.nativeValue = nativeValue;
    }

    public int getNativeValue() {
        return nativeValue;
    }

    int nativeValue;
    static Map<Integer, TlsSignatureAlgorithm> enumMapping = buildEnumMapping();
}

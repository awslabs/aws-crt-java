/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.HashMap;
import java.util.Map;

/**
 * The hash algorithm of a TLS private key operation. Any custom private key operation handlers are expected to perform
 * operations on the input TLS data using the correct hash algorithm or fail the operation.
 */
public enum TlsHashAlgorithm {
    UNKNOWN(0), SHA1(1), SHA224(2), SHA256(3), SHA384(4), SHA512(5);

    static Map<Integer, TlsHashAlgorithm> buildEnumMapping() {
        Map<Integer, TlsHashAlgorithm> enumMapping = new HashMap<Integer, TlsHashAlgorithm>();
        for (TlsHashAlgorithm i : TlsHashAlgorithm.values()) {
            enumMapping.put(i.nativeValue, i);
        }
        return enumMapping;
    }

    public static TlsHashAlgorithm getEnumValueFromInteger(int value) {
        TlsHashAlgorithm enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }
        throw new RuntimeException("Illegal TlsKeyOperation.TlsHashAlgorithm");
    }

    TlsHashAlgorithm(int nativeValue) {
        this.nativeValue = nativeValue;
    }

    public int getNativeValue() {
        return nativeValue;
    }

    int nativeValue;
    static Map<Integer, TlsHashAlgorithm> enumMapping = buildEnumMapping();
}

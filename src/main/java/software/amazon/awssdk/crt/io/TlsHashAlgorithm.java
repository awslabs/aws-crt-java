/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.HashMap;
import java.util.Map;

public enum TlsHashAlgorithm {
    SHA1(1), SHA224(2), SHA256(3), SHA384(4), SHA512(5);

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

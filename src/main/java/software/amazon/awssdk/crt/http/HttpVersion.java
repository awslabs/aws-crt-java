/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.Map;
import java.util.HashMap;

public enum HttpVersion {

    UNKNOWN(0),
    HTTP_1_0(1),
    HTTP_1_1(2),
    HTTP_2(3);

    private int value;
    private static Map<Integer, HttpVersion> enumMapping = buildEnumMapping();

    HttpVersion(int value) {
        this.value = value;
    }

    public static HttpVersion getEnumValueFromInteger(int value) {
        HttpVersion enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }

        throw new RuntimeException("Illegal signature type value in signing configuration");
    }

    private static Map<Integer, HttpVersion> buildEnumMapping() {
        Map<Integer, HttpVersion> enumMapping = new HashMap<Integer, HttpVersion>();
        enumMapping.put(UNKNOWN.getValue(), UNKNOWN);
        enumMapping.put(HTTP_1_0.getValue(), HTTP_1_0);
        enumMapping.put(HTTP_1_1.getValue(), HTTP_1_1);
        enumMapping.put(HTTP_2.getValue(), HTTP_2);

        return enumMapping;
    }

    public int getValue() {
        return value;
    }
}

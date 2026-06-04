/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.internal;

/**
 * @internal
 * A key-value pair for IoT SDK metrics metadata.
 * Metadata entries are appended to the MQTT CONNECT packet username field
 * as part of the Metadata query parameter.
 */
public class IoTMetricsMetadata {
    private String key;
    private String value;

    public IoTMetricsMetadata(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
}

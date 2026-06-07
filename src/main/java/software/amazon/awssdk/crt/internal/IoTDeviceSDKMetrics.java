/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.internal;

import java.util.List;

/**
 * @internal
 * IoT Device SDK Metrics Structure. Not for external usage.
 */
public class IoTDeviceSDKMetrics {
    private String libraryName;
    private List<IoTMetricsMetadata> metadataEntries;

    public IoTDeviceSDKMetrics() {
        this.libraryName = "IoTDeviceSDK/Java";
    }

    public IoTDeviceSDKMetrics(String libraryName, List<IoTMetricsMetadata> metadataEntries) {
        this.libraryName = libraryName;
        this.metadataEntries = metadataEntries;
    }

    public String getLibraryName() { return libraryName; }

    /**
    * Sets the SDK library name (e.g., "IoTDeviceSDK/Java").
    *
    * @param libraryName the library name to report in metrics
    */
    public void setLibraryName(String libraryName) { this.libraryName = libraryName; }

    public List<IoTMetricsMetadata> getMetadataEntries() { return metadataEntries; }

    /**
    * Sets the metadata entries to include in the MQTT CONNECT packet username field.
    *
    * @param metadataEntries list of key-value metadata pairs, or null for none
    */
    public void setMetadataEntries(List<IoTMetricsMetadata> metadataEntries) { this.metadataEntries = metadataEntries; }
}

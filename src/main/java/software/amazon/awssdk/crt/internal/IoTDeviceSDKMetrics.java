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
    public void setLibraryName(String libraryName) { this.libraryName = libraryName; }

    public List<IoTMetricsMetadata> getMetadataEntries() { return metadataEntries; }
    public void setMetadataEntries(List<IoTMetricsMetadata> metadataEntries) { this.metadataEntries = metadataEntries; }
}

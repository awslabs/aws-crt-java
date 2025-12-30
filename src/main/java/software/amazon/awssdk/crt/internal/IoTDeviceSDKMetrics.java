/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.internal;

/**
 * @internal
 * IoT Device SDK Metrics Structure
 */
public class IoTDeviceSDKMetrics {
    private String libraryName;

    public IoTDeviceSDKMetrics() {
        this.libraryName = "IoTDeviceSDK/Java";
    }

    public String getLibraryName() {
        return libraryName;
    }
}
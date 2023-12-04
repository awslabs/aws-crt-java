/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

public class S3ExpressCredentialsProperties {
    private String hostValue;
    private String region;

    public S3ExpressCredentialsProperties withHostValue (String hostValue) {
        this.hostValue = hostValue;
        return this;
    }

    public String getHostValue () {
        return this.hostValue;
    }

    public S3ExpressCredentialsProperties withRegion (String region) {
        this.region = region;
        return this;
    }

    public String getRegion () {
        return this.region;
    }
}

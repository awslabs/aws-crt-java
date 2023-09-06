/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

// Encapsulates any platform-specific configuration for tests
// TODO remove this. We are going to be using System Properties for tests
public class CrtTestContext {
    // Trust store PEM blob
    public byte[] trustStore = null;
    // IoT Thing certificate for testing
    public byte[] iotClientCertificate = null;
    // IoT Thing private key for testing
    public byte[] iotClientPrivateKey = null;
    // IoT Thing ecc certificate for testing
    public byte[] iotClientECCCertificate = null;
    // IoT Thing ecc private key for testing
    public byte[] iotClientECCPrivateKey = null;
    // IoT ATS endpoint for testing
    public String iotEndpoint = null;
    // IoT CA Root
    public byte[] iotCARoot = null;
}

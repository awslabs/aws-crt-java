/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.test;

// Encapsulates any platform-specific configuration for tests
public class CrtTestContext {
    // Trust store PEM blob
    public byte[] trustStore = null;
    // IoT Thing certificate for testing
    public byte[] iotClientCertificate = null;
    // IoT Thing private key for testing
    public byte[] iotClientPrivateKey = null;
    // IoT ATS endpoint for testing
    public String iotEndpoint = null;
    // IoT CA Root
    public byte[] iotCARoot = null;
}
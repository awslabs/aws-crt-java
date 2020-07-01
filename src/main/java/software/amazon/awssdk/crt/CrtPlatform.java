/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt;

import software.amazon.awssdk.crt.utils.PackageInfo;

public abstract class CrtPlatform {
    // Called before any native code is loaded, just to configure the JVM
    public void jvmInit() {}

    // Gets the package version. If not overridden, the Java Package that the
    // CRT class is in will be read for version info.
    public PackageInfo.Version getVersion() {
        return null;
    }

    // Get the OS identifier, used to determine platform and to load the
    // JNI library
    public String getOSIdentifier() {
        return System.getProperty("os.name");
    }

    // Get the architecture, used to determine platform and to load the
    // JNI library
    public String getArchIdentifier() {
        return System.getProperty("os.arch");
    }

    // Called before every JUnit test
    public void testSetup(Object context) {}
    
    // Called after every JUnit test
    public void testTearDown(Object context) {}
}

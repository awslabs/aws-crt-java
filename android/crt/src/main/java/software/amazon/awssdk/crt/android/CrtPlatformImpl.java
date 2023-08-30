
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.android;

import software.amazon.awssdk.crt.BuildConfig;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.utils.PackageInfo;

public class CrtPlatformImpl extends CrtPlatform {
    public String getOSIdentifier() {
        System.out.println("Android TEST: CrtPlatformImpl.getOSIdentifier() called\n");
        return "android";
    }

    public PackageInfo.Version getVersion() {
        System.out.println("Android TEST: CrtPlatformImpl.getVersion() from android->crt->main called\n");
        return new PackageInfo.Version(BuildConfig.VERSION_NAME);
    }
}
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import org.junit.Assert;
import software.amazon.awssdk.crt.utils.PackageInfo;

public class PackageInfoTest extends CrtTestFixture  {
    public PackageInfoTest() {}
    
    @Test
    public void testPackageInfo() {
        PackageInfo pkgInfo = new PackageInfo();
        Assert.assertNotEquals("UNKNOWN", pkgInfo.version.toString());
        Assert.assertEquals(0, pkgInfo.version.major);
        Assert.assertEquals(0, pkgInfo.version.minor);
        Assert.assertEquals(0, pkgInfo.version.patch);
        Assert.assertEquals("UNITTEST", pkgInfo.version.tag);
    }
};

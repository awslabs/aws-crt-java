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

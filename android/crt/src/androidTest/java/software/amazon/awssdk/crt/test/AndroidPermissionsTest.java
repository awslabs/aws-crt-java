/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.Rule;

public final class AndroidPermissionsTest {
    @Rule
    public GrantPermissionRule runtimePermissions = GrantPermissionRule.grant(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE);

    @Test
    public void verifyPermissions() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getContext();
        Assert.assertNotNull(ctx);
        Assert.assertEquals(PackageManager.PERMISSION_GRANTED, ContextCompat.checkSelfPermission(ctx, Manifest.permission.INTERNET));
        Assert.assertEquals(PackageManager.PERMISSION_GRANTED, ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_NETWORK_STATE));
    }
}

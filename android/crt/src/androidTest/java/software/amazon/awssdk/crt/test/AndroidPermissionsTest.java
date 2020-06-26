/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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

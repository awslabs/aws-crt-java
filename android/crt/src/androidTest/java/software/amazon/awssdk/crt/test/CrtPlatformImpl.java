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
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.CrtRuntimeException;


public class CrtPlatformImpl extends CrtPlatform {
    public void jvmInit() {
        // Ensure that android JUnitTestRunner test arguments get turned into system properties
        Bundle testArgs = InstrumentationRegistry.getArguments();
        if (testArgs != null) {
            final Set<String> keys = testArgs.keySet();
            for (final String key : keys) {
                if (key.startsWith("aws.crt.")) {
                    System.setProperty(key, testArgs.getString(key));
                }
            }
        }
    }

    public void testSetup(Object context) {
        CrtTestContext ctx = (CrtTestContext) context;
        AssetManager assets = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets();
        try (InputStream trustStoreStream = assets.open("ca-certificates.crt");) {
            ctx.trustStore = new byte[trustStoreStream.available()];
            trustStoreStream.read(ctx.trustStore);
        } catch (IOException ex) {
            throw new CrtRuntimeException(ex.toString());
        }
    }

    public void testTearDown(Object context) {

    }
}


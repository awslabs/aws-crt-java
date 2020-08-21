/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test.android;

import android.Manifest;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import software.amazon.awssdk.crt.test.CrtTestContext;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.utils.PackageInfo;

// Overrides just for testing
public class CrtPlatformImpl extends software.amazon.awssdk.crt.android.CrtPlatformImpl {
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

    public PackageInfo.Version getVersion() {
        return new PackageInfo.Version("0.0.0-UNITTEST");
    }

    private static byte[] assetContents(String filename) {
        AssetManager assets = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets();
        try (InputStream assetStream = assets.open(filename);) {
            byte[] contents = new byte[assetStream.available()];
            assetStream.read(contents);
            return contents;
        } catch (IOException ex) {
            throw new CrtRuntimeException(ex.toString());
        }
    }

    public void testSetup(Object context) {
        CrtTestContext ctx = (CrtTestContext) context;
        ctx.trustStore = assetContents("ca-certificates.crt");
        ctx.iotClientCertificate = assetContents("certificate.pem");
        ctx.iotClientPrivateKey = assetContents("privatekey.pem");
        ctx.iotEndpoint = new String(assetContents("endpoint.txt")).trim();
        ctx.iotCARoot = assetContents("AmazonRootCA1.pem");
    }

    public void testTearDown(Object context) {

    }
}


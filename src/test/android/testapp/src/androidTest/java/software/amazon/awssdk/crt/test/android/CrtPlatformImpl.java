/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

 package software.amazon.awssdk.crt.test.android;

 import android.content.res.AssetManager;
 import android.os.Bundle;

 import androidx.test.platform.app.InstrumentationRegistry;

 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Set;

 import software.amazon.awssdk.crt.test.CrtTestContext;
 import software.amazon.awssdk.crt.utils.PackageInfo;

 // Overrides just for testing
 public class CrtPlatformImpl extends software.amazon.awssdk.crt.android.CrtPlatformImpl {
     public void jvmInit() {
        System.out.println("Android TEST: CrtPlatformImpl.jvmInit() src.test.android.testapp.src.androidTest.java Started");
         // Ensure that android JUnitTestRunner test arguments get turned into system properties
         Bundle testArgs = InstrumentationRegistry.getArguments();
         if (testArgs != null) {
            System.out.println("Android TEST: CrtPlatformImpl.jvmInit() testArgs != null");
             final Set<String> keys = testArgs.keySet();
             for (final String key : keys) {
                 if (key.startsWith("aws.crt.")) {
                     System.setProperty(key, testArgs.getString(key));
                     System.out.println("Android TEST: CrtPlatformImpl.jvmInit() setting key:" + key + " to value:" + testArgs.getString(key));
                 }
             }
         }
         else {
            System.out.println("Android TEST: CrtPlatformImpl.jvmInit() testArgs == null");
         }
         System.out.println("Android TEST: CrtPlatformImpl.jvmInit() Completed");
     }

     public PackageInfo.Version getVersion() {
        System.out.println("Android TEST: CrtPlatformImpl.getVersion() from testapp->androidTest hit");
         return new PackageInfo.Version("0.0.0-UNITTEST");
     }

     private static byte[] assetContents(String filename) {
        System.out.println("Android TEST: CrtPlatformImpl.AssetContents(" + filename +") Started");
         AssetManager assets = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets();
         try (InputStream assetStream = assets.open(filename);) {
             byte[] contents = new byte[assetStream.available()];
             assetStream.read(contents);
             System.out.println("Android TEST: CrtPlatformImpl.AssetContents() byte[] being returned");
             return contents;
         } catch (IOException ex) {
            System.out.println("Android TEST: CrtPlatformImpl.AssetContents() null being returned");
             return null;
         }
     }

     public void testSetup(Object context) {
        System.out.println("Android TEST: CrtPlatformImpl.testSetup() Started");
         CrtTestContext ctx = (CrtTestContext) context;
         ctx.trustStore = assetContents("ca-certificates.crt");
         ctx.iotClientCertificate = assetContents("certificate.pem");
         ctx.iotClientPrivateKey = assetContents("privatekey.pem");
         ctx.iotClientECCPrivateKey = assetContents("ecc_privatekey.pem");
         ctx.iotClientECCCertificate = assetContents("ecc_certificate.pem");
         byte[] endpoint = assetContents("endpoint.txt");
         if (endpoint != null) {
             ctx.iotEndpoint = new String(endpoint).trim();
         }
         ctx.iotCARoot = assetContents("AmazonRootCA1.pem");
         System.out.println("Android TEST: CrtPlatformImpl.testSetup() Completed");
     }

     public void testTearDown(Object context) {
        System.out.println("Android TEST: CrtPlatformImpl.testTearDown() Started");
        System.out.println("Android TEST: CrtPlatformImpl.testTearDown() Completed");
     }
 }


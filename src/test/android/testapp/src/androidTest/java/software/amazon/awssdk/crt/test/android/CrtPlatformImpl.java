/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

 package software.amazon.awssdk.crt.test.android;

 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.content.Context;
 import android.os.Bundle;

 import androidx.test.platform.app.InstrumentationRegistry;

 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Set;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FileOutputStream;
 import java.io.BufferedWriter;

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
    // private static byte[] assetContents(String filename) {
    //    System.out.println("Android TEST: CrtPlatformImpl.AssetContents(" + filename +") Started");
    //     AssetManager assets = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets();
    //     try (InputStream assetStream = assets.open(filename);) {
    //         byte[] contents = new byte[assetStream.available()];
    //         assetStream.read(contents);
    //         System.out.println("Android TEST: CrtPlatformImpl.AssetContents() byte[] being returned");
    //         return contents;
    //     } catch (IOException ex) {
    //        System.out.println("Android TEST: CrtPlatformImpl.AssetContents() null being returned");
    //         return null;
    //     }
    // }

    // Attempts to set a System property to the contents of a file in the assets folder
    private void SetPropertyFromFile(String propertyName, String fileName){
        System.out.println("Android TEST: CrtPlatformImpl.SetPropertyFromFile("+ propertyName +", " + fileName + ") Started");
        AssetManager assets = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getAssets();
        try (InputStream assetStream = assets.open(fileName);) {
            byte[] contents = new byte[assetStream.available()];
            assetStream.read(contents);
            System.setProperty(propertyName, new String(contents).trim());
        } catch (IOException ex){
            // File didn't exist or was unreadable
        }
    }

    // Attempts to create a cached file from a file in the assets folder and set a System property pointing to the created file
    private void SetPropertyToFileLocation(String propertyName, String fileName){
        System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation(" + propertyName + ", " + fileName + ") Started");
        Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Resources testRes = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
        AssetManager assets = testRes.getAssets();

        String cachedName = testContext.getExternalCacheDir().getAbsolutePath() + "/" + fileName;
        System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation cachedName set to" + cachedName);

        try (InputStream assetStream = assets.open(fileName);
            FileOutputStream cachedRes = new FileOutputStream(cachedName)){
                System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation file:" + fileName + " opened and cachedRes created");
                byte[] data = new byte[assetStream.available()];
                assetStream.read(data);
                cachedRes.write(data);
                System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation cachedRes written to");
                System.setProperty(propertyName, cachedName);
                // assetStream.transferTo(cachedRes);
            } catch (IOException ex) {
                System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation(" + propertyName + ", " + fileName + ") IOException: " + ex.toString());
            }

            /*
        try (InputStream assetStream = assets.open(fileName);){
            byte[] contents = new byte[assetStream.available()];
            assetStream.read(contents);

            System.out.println("Android TEST: File file coming up");
            // Files in the assets folder are compressed and need to be saved to a cache before being used
            File file = new File(InstrumentationRegistry.getInstrumentation().getContext().getCacheDir(), fileName);
            System.out.println("Android TEST: FileWriter fw coming up using file.getAbsoluteFile:" + file.getAbsoluteFile());
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            System.out.println("Android TEST: BufferedWriter bw coming up");
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(new String(contents).trim());
            bw.close();
            System.out.println("Android TEST: setting property " + propertyName + " to " + file.getAbsolutePath() + fileName);
            System.setProperty(propertyName, file.getAbsolutePath() + fileName);
        } catch (IOException ex){
            System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation exception encountered");
            // File didn't exist or was unreadable
        }
        */
        System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation(" + propertyName + ", " + fileName + ") Completed");
    }

    public void testSetup(Object context) {
        System.out.println("Android TEST: CrtPlatformImpl.testSetup() Started");

        // Set System properties using files from the assets folder for use by tests
        SetPropertyToFileLocation("AWS_TEST_MQTT311_IOT_CORE_RSA_CERT", "pubSubCertificate.pem");
        SetPropertyToFileLocation("AWS_TEST_MQTT311_IOT_CORE_RSA_KEY", "pubSubPrivatekey.pem");
        SetPropertyFromFile("AWS_TEST_MQTT311_IOT_CORE_HOST", "endpoint.txt");

        System.out.println("Android TEST: CrtPlatformImpl.testSetup() Completed");
    }

    public void testTearDown(Object context) {
        System.out.println("Android TEST: CrtPlatformImpl.testTearDown() Started");
        System.out.println("Android TEST: CrtPlatformImpl.testTearDown() Completed");
    }
 }


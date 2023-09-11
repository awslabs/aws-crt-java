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

    // Attempts to set a System property to the contents of a file in the assets folder
    private void SetPropertyFromFile(String propertyName, String fileName){
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
        Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Resources testRes = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
        AssetManager assets = testRes.getAssets();

        String cachedName = testContext.getExternalCacheDir().getAbsolutePath() + "/" + fileName;

        try (InputStream assetStream = assets.open(fileName);
            FileOutputStream cachedRes = new FileOutputStream(cachedName)){
                byte[] data = new byte[assetStream.available()];
                assetStream.read(data);
                cachedRes.write(data);
                System.setProperty(propertyName, cachedName);
                // assetStream.transferTo(cachedRes);
            } catch (IOException ex) {
                System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation(" + propertyName + ", " + fileName + ") IOException: " + ex.toString());
            }
    }

    public void testSetup(Object context) {
        //Indicate system properties are set for future tests
        System.setProperty("are.test.properties.setup", "true");

        // Set System properties using files from the assets folder for use by tests
        SetPropertyToFileLocation("AWS_TEST_MQTT311_IOT_CORE_RSA_CERT", "cert.pem");
        SetPropertyToFileLocation("AWS_TEST_MQTT311_IOT_CORE_RSA_KEY", "key.pem");
        SetPropertyFromFile("AWS_TEST_MQTT311_IOT_CORE_HOST", "unit-test-endpoint.txt");
    }

    public void testTearDown(Object context) {
    }
 }


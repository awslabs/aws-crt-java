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

    // Used to avoid exception when the property being copied is null (not set)
    private void SetPropertyFromProperty(String propertyName, String propertyToCopy){
        if (System.getProperty(propertyToCopy) != null) {
            System.setProperty(propertyName, System.getProperty(propertyToCopy));
        } else {
            System.out.println("Android TEST: CrtPlatformImpl.SetPropertyFromProperty() Property:" + propertyToCopy + " is null")
        }
    }

    // Set a System property to the contents of a file in the assets folder
    private void SetPropertyFromFile(String propertyName, String fileName){
        AssetManager assets = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getAssets();
        try (InputStream assetStream = assets.open(fileName);) {
            byte[] contents = new byte[assetStream.available()];
            assetStream.read(contents);
            System.setProperty(propertyName, new String(contents).trim());
        } catch (IOException ex){
            System.out.println("Android TEST: CrtPlatformImpl.SetPropertyFromFile(" + propertyName + ", " + fileName + ") IOException: " + ex.toString());
        }
    }

    // Attempts to create a cached file from a file in the assets folder and sets a System property pointing to the created file
    // CRT cannot directly access files in the assets folder of an Android app as they are compressed
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
            } catch (IOException ex) {
                System.out.println("Android TEST: CrtPlatformImpl.SetPropertyToFileLocation(" + propertyName + ", " + fileName + ") IOException: " + ex.toString());
            }
    }

    public void testSetup(Object context) {
        // Any new system properties that are required by CI Device Farm tests must be added to android_file_creation.py
        // as well as configured here.

        //Indicate system properties are set for future tests
        System.setProperty("are.test.properties.setup", "true");

        System.setProperty("AWS_TEST_IS_CI", "True");

        SetPropertyFromFile("AWS_TEST_MQTT5_IOT_CORE_HOST", "AWS_TEST_MQTT5_IOT_CORE_HOST.txt");
        System.setProperty("AWS_TEST_MQTT5_IOT_CORE_REGION", "us-east-1");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_IOT_CORE_RSA_CERT", "AWS_TEST_MQTT5_IOT_CORE_RSA_CERT.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_IOT_CORE_RSA_KEY", "AWS_TEST_MQTT5_IOT_CORE_RSA_KEY.txt");
        System.setProperty("AWS_TEST_MQTT5_COGNITO_ENDPOINT", "cognito-identity.us-east-1.amazonaws.com");
        SetPropertyFromFile("AWS_TEST_MQTT5_COGNITO_IDENTITY", "AWS_TEST_MQTT5_COGNITO_IDENTITY.txt");
        SetPropertyFromFile("AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT", "AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_IOT_CORE_X509_CA", "AWS_TEST_MQTT5_IOT_CORE_X509_CA.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_IOT_CORE_X509_KEY", "AWS_TEST_MQTT5_IOT_CORE_X509_KEY.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_IOT_CORE_X509_CERT", "AWS_TEST_MQTT5_IOT_CORE_X509_CERT.txt");
        System.setProperty("AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS", "X509IntegrationTestRoleAlias");
        System.setProperty("AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME", "X509IntegrationTestThing");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT", "AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY", "AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY.txt");

        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_HOST","AWS_TEST_MQTT5_IOT_CORE_HOST");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_RSA_CERT", "AWS_TEST_MQTT5_IOT_CORE_RSA_CERT");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_RSA_KEY", "AWS_TEST_MQTT5_IOT_CORE_RSA_KEY");
        System.setProperty("AWS_TEST_MQTT311_IOT_CORE_REGION", "us-east-1");
        System.setProperty("AWS_TEST_MQTT311_COGNITO_ENDPOINT", "cognito-identity.us-east-1.amazonaws.com");
        SetPropertyFromProperty("AWS_TEST_MQTT311_COGNITO_IDENTITY", "AWS_TEST_MQTT5_COGNITO_IDENTITY");
        SetPropertyToFileLocation("AWS_TEST_MQTT311_IOT_CORE_ECC_CERT", "AWS_TEST_MQTT311_IOT_CORE_ECC_CERT.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT311_IOT_CORE_ECC_KEY", "AWS_TEST_MQTT311_IOT_CORE_ECC_KEY.txt");
        SetPropertyToFileLocation("AWS_TEST_MQTT311_ROOT_CA", "AWS_TEST_MQTT311_ROOT_CA.txt");
        SetPropertyFromProperty("AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT", "AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT");
        SetPropertyFromProperty("AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY", "AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_X509_CERT", "AWS_TEST_MQTT5_IOT_CORE_X509_CERT");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_X509_KEY", "AWS_TEST_MQTT5_IOT_CORE_X509_KEY");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_X509_ENDPOINT", "AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_X509_ROLE_ALIAS", "AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS");
        SetPropertyFromProperty("AWS_TEST_MQTT311_IOT_CORE_X509_THING_NAME", "AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME");

        // THESE ARE SET USING aws sts assume-role with a role-arn and role-session and must be cycled.
        SetPropertyFromFile("AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY", "AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY.txt");
        SetPropertyFromFile("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY", "AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY.txt");
        SetPropertyFromFile("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN", "AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN.txt");

        SetPropertyFromProperty("AWS_TEST_MQTT311_ROLE_CREDENTIAL_ACCESS_KEY", "AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY");
        SetPropertyFromProperty("AWS_TEST_MQTT311_ROLE_CREDENTIAL_SECRET_ACCESS_KEY", "AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
        SetPropertyFromProperty("AWS_TEST_MQTT311_ROLE_CREDENTIAL_SESSION_TOKEN", "AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN");
    }

    public void testTearDown(Object context) {
    }
 }


/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.amazonaws.test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.test.CrtTestContext;
import software.amazon.awssdk.crt.test.CrtMemoryLeakDetector;
import software.amazon.awssdk.crt.test.CrtTestFixture;
import java.nio.charset.StandardCharsets;
import java.io.File;

import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;

/**
 * Stub for getting Aws credentials from environment for running CI tests
 */
public class AwsClientTestFixture extends CrtTestFixture {

    @BeforeClass
    public static void enableLog() {
        Log.initLoggingToFile(Log.LogLevel.Error, "log.txt");
    }

    @Before
    public void traceLogForSingleTest() {
        if  (System.getProperty("aws.crt.aws_trace_log_per_test") != null) {
            File logsFile = new File("log.txt");
            logsFile.delete();
            Log.initLoggingToFile(Log.LogLevel.Trace, "log.txt");
        }
    }

    @After
    public void tearDown() {
        CrtResource.waitForNoResources();
        if (CRT.getOSIdentifier() != "android") {
            try {
                Runtime.getRuntime().gc();
                CrtMemoryLeakDetector.nativeMemoryLeakCheck();
            } catch (Exception e) {
                throw new RuntimeException("Memory leak from native resource detected!");
            }
        }
    }

    protected static boolean areAwsCredentialsAvailable() {
        return true;
    }

    /**
     * Temporary implementation for local testing
     */
    protected static CredentialsProvider getTestCredentialsProvider() {
        DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder();
        return builder.build();

    }
}

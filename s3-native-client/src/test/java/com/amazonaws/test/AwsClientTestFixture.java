/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.amazonaws.test;

import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import java.nio.charset.StandardCharsets;

/**
 * Stub for getting Aws credentials from environment for running CI tests
 */
public class AwsClientTestFixture {
    protected static boolean areAwsCredentialsAvailable() {
        return System.getProperty("aws_access_key_id") != null && !System.getProperty("aws_access_key_id").equals("");
    }
    
    /**
     * Temporary implementation for local testing
     */
    protected static CredentialsProvider getTestCredentialsProvider() {
        final String awsAccessKeyId = System.getProperty("crt.aws_access_key_id");
        final String awsSecretAccessKey = System.getProperty("crt.aws_secret_access_key");
        final String awsSessionToken = System.getProperty("crt.aws_session_token");

        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = 
                new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(awsAccessKeyId.getBytes(StandardCharsets.UTF_8))
                .withSecretAccessKey(awsSecretAccessKey.getBytes(StandardCharsets.UTF_8));
        if (awsSessionToken != null && !awsSessionToken.equals("")) {
            builder.withSessionToken(awsSessionToken.getBytes(StandardCharsets.UTF_8));
        }
        return builder.build();
    }
}

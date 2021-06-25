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
        return (System.getProperty("crt.aws_access_key_id") != null
                && !System.getProperty("crt.aws_access_key_id").equals(""))
                || (System.getenv("AWS_ACCESS_KEY_ID") != null && !System.getenv("AWS_ACCESS_KEY_ID").equals(""));
    }

    /**
     * Temporary implementation for local testing
     */
    protected static CredentialsProvider getTestCredentialsProvider() {
        final String awsAccessKeyId;
        final String awsSecretAccessKey;
        final String awsSessionToken;
        if (System.getProperty("crt.aws_access_key_id") != null) {
            awsAccessKeyId = System.getProperty("crt.aws_access_key_id");
            awsSecretAccessKey = System.getProperty("crt.aws_secret_access_key");
            awsSessionToken = System.getProperty("crt.aws_session_token");
        } else {
            awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
            awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
            awsSessionToken = null;
        }

        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(awsAccessKeyId.getBytes(StandardCharsets.UTF_8))
                .withSecretAccessKey(awsSecretAccessKey.getBytes(StandardCharsets.UTF_8));
        if (awsSessionToken != null && !awsSessionToken.equals("")) {
            builder.withSessionToken(awsSessionToken.getBytes(StandardCharsets.UTF_8));
        }
        return builder.build();

    }
}

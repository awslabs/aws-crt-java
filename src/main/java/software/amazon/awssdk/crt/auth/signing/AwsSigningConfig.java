/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package software.amazon.awssdk.crt.auth.signing;

import java.time.Instant;
import java.util.function.Predicate;

import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;

/**
 * A class representing
 */
public class AwsSigningConfig {

    public enum AwsSigningAlgorithm {
        SIGV4_HEADER(0),
        SIGV4_QUERY_PARAM(1);

        AwsSigningAlgorithm(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        int getNativeValue() { return nativeValue; }

        int nativeValue;
    }

    int signingAlgorithm = AwsSigningAlgorithm.SIGV4_HEADER.getNativeValue();

    String region;

    String service;

    long time = Instant.now().toEpochMilli();

    CredentialsProvider credentialsProvider;

    Predicate<String> shouldSignParameter;

    boolean useDoubleUriEncode = true;
    boolean shouldNormalizeUriPath = true;
    boolean signBody = false;

    public AwsSigningConfig() {}

    public void setSigningAlgorithm(AwsSigningAlgorithm algorithm) { this.signingAlgorithm = algorithm.getNativeValue(); }
    public AwsSigningAlgorithm getSigningAlgorithm() {
        switch(signingAlgorithm) {
            case 0:
                return AwsSigningAlgorithm.SIGV4_HEADER;

            case 1:
                return AwsSigningAlgorithm.SIGV4_QUERY_PARAM;

            default:
                throw new RuntimeException("Illegal signing algorithm value in signing configuration");
        }
    }

    public void setRegion(String region) { this.region = region; }
    public String getRegion() { return region; }

    public void setService(String service) { this.service = service; }
    public String getService() { return service; }

    public void setTime(Instant time) { this.time = time.toEpochMilli(); }
    public Instant getTime() { return Instant.ofEpochMilli(time); }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) { this.credentialsProvider = credentialsProvider; }
    public CredentialsProvider getCredentialsProvider() { return credentialsProvider; }

    public void setShouldSignParameter(Predicate<String> shouldSignParameter) { this.shouldSignParameter = shouldSignParameter; }
    public Predicate<String> getShouldSignParameter() { return shouldSignParameter; }

    public void setUseDoubleUriEncode(boolean useDoubleUriEncode) { this.useDoubleUriEncode = useDoubleUriEncode; }
    public boolean getUseDoubleUriEncode() { return useDoubleUriEncode; }

    public void setShouldNormalizeUriPath(boolean shouldNormalizeUriPath) { this.shouldNormalizeUriPath = shouldNormalizeUriPath; }
    public boolean getShouldNormalizeUriPath() { return shouldNormalizeUriPath; }

    public void setSignBody(boolean signBody) { this.signBody = signBody; }
    public boolean getSignBody() { return signBody; }
}




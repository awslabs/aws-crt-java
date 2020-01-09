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
import java.util.HashMap;
import java.util.Map;

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

        public int getNativeValue() { return nativeValue; }

        public static AwsSigningAlgorithm getEnumValueFromInteger(int value) {
            AwsSigningAlgorithm enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal signing algorithm value in signing configuration");
        }

        private static Map<Integer, AwsSigningAlgorithm> buildEnumMapping() {
            Map<Integer, AwsSigningAlgorithm> enumMapping = new HashMap<Integer, AwsSigningAlgorithm>();
            enumMapping.put(SIGV4_HEADER.getNativeValue(), SIGV4_HEADER);
            enumMapping.put(SIGV4_QUERY_PARAM.getNativeValue(), SIGV4_QUERY_PARAM);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsSigningAlgorithm> enumMapping = buildEnumMapping();
    }

    public enum AwsBodySigningConfigType {
        AWS_BODY_SIGNING_OFF(0),
        AWS_BODY_SIGNING_ON(1),
        AWS_BODY_SIGNING_UNSIGNED_PAYLOAD(2);

        AwsBodySigningConfigType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

        public static AwsBodySigningConfigType getEnumValueFromInteger(int value) {
            AwsBodySigningConfigType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal body signing config type value in signing configuration");
        }

        private static Map<Integer, AwsBodySigningConfigType> buildEnumMapping() {
            Map<Integer, AwsBodySigningConfigType> enumMapping = new HashMap<Integer, AwsBodySigningConfigType>();
            enumMapping.put(AWS_BODY_SIGNING_OFF.getNativeValue(), AWS_BODY_SIGNING_OFF);
            enumMapping.put(AWS_BODY_SIGNING_ON.getNativeValue(), AWS_BODY_SIGNING_ON);
            enumMapping.put(AWS_BODY_SIGNING_UNSIGNED_PAYLOAD.getNativeValue(), AWS_BODY_SIGNING_UNSIGNED_PAYLOAD);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsBodySigningConfigType> enumMapping = buildEnumMapping();
    }

    private int signingAlgorithm = AwsSigningAlgorithm.SIGV4_HEADER.getNativeValue();
    private String region;
    private String service;
    private long time = Instant.now().toEpochMilli();
    private CredentialsProvider credentialsProvider;
    private Predicate<String> shouldSignParameter;
    private boolean useDoubleUriEncode = true;
    private boolean shouldNormalizeUriPath = true;
    private int signBody = AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF.getNativeValue();

    public AwsSigningConfig() {}

    public AwsSigningConfig clone() {
        try (AwsSigningConfig clone = new AwsSigningConfig()) {

            clone.setSigningAlgorithm(getSigningAlgorithm());
            clone.setRegion(getRegion());
            clone.setService(getService());
            clone.setTime(getTime());
            clone.setCredentialsProvider(getCredentialsProvider());
            clone.setShouldSignParameter(getShouldSignParameter());
            clone.setUseDoubleUriEncode(getUseDoubleUriEncode());
            clone.setShouldNormalizeUriPath(getShouldNormalizeUriPath());
            clone.setSignBody(getSignBody());

            return clone;
        }
    }

    public void setSigningAlgorithm(AwsSigningAlgorithm algorithm) { this.signingAlgorithm = algorithm.getNativeValue(); }
    public AwsSigningAlgorithm getSigningAlgorithm() {
        return AwsSigningAlgorithm.getEnumValueFromInteger(signingAlgorithm);
    }

    public void setRegion(String region) { this.region = region; }
    public String getRegion() { return region; }

    public void setService(String service) { this.service = service; }
    public String getService() { return service; }

    public void setTime(Instant time) { this.time = time.toEpochMilli(); }
    public Instant getTime() { return Instant.ofEpochMilli(time); }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        swapReferenceTo(this.credentialsProvider, credentialsProvider);
        this.credentialsProvider = credentialsProvider;
    }

    public CredentialsProvider getCredentialsProvider() { return credentialsProvider; }

    public void setShouldSignParameter(Predicate<String> shouldSignParameter) { this.shouldSignParameter = shouldSignParameter; }
    public Predicate<String> getShouldSignParameter() { return shouldSignParameter; }

    public void setUseDoubleUriEncode(boolean useDoubleUriEncode) { this.useDoubleUriEncode = useDoubleUriEncode; }
    public boolean getUseDoubleUriEncode() { return useDoubleUriEncode; }

    public void setShouldNormalizeUriPath(boolean shouldNormalizeUriPath) { this.shouldNormalizeUriPath = shouldNormalizeUriPath; }
    public boolean getShouldNormalizeUriPath() { return shouldNormalizeUriPath; }

    public void setSignBody(AwsBodySigningConfigType signBody) { this.signBody = signBody.getNativeValue(); }
    public AwsBodySigningConfigType getSignBody() { return AwsBodySigningConfigType.getEnumValueFromInteger(signBody); }
}




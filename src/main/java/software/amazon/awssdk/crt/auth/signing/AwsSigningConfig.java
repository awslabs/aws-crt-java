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

import java.util.function.Predicate;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.CrtResource;

/**
 * A class representing
 */
public class AwsSigningConfig extends CrtResource {

    public enum AwsSigningAlgorithm {
        SIGV4(0);

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
            enumMapping.put(SIGV4.getNativeValue(), SIGV4);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsSigningAlgorithm> enumMapping = buildEnumMapping();
    }

    public enum AwsRequestSigningTransform {
        HEADER(0),
        QUERY_PARAM(1);

        AwsRequestSigningTransform(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

        public static AwsRequestSigningTransform getEnumValueFromInteger(int value) {
            AwsRequestSigningTransform enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal request signing transform value in signing configuration");
        }

        private static Map<Integer, AwsRequestSigningTransform> buildEnumMapping() {
            Map<Integer, AwsRequestSigningTransform> enumMapping = new HashMap<Integer, AwsRequestSigningTransform>();
            enumMapping.put(HEADER.getNativeValue(), HEADER);
            enumMapping.put(QUERY_PARAM.getNativeValue(), QUERY_PARAM);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsRequestSigningTransform> enumMapping = buildEnumMapping();
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

    private int algorithm = AwsSigningAlgorithm.SIGV4.getNativeValue();
    private int transform = AwsRequestSigningTransform.HEADER.getNativeValue();
    private String region;
    private String service;
    private long time = System.currentTimeMillis();
    private CredentialsProvider credentialsProvider;
    private Credentials credentials;
    private Predicate<String> shouldSignParameter;
    private boolean useDoubleUriEncode = true;
    private boolean shouldNormalizeUriPath = true;
    private int signBody = AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF.getNativeValue();
    private long expirationInSeconds = 0;

    public AwsSigningConfig() {}

    public AwsSigningConfig clone() {
        try (AwsSigningConfig clone = new AwsSigningConfig()) {

            clone.setAlgorithm(getAlgorithm());
            clone.setTransform(getTransform());
            clone.setRegion(getRegion());
            clone.setService(getService());
            clone.setTime(getTime());
            clone.setCredentialsProvider(getCredentialsProvider());
            clone.setCredentials(getCredentials());
            clone.setShouldSignParameter(getShouldSignParameter());
            clone.setUseDoubleUriEncode(getUseDoubleUriEncode());
            clone.setShouldNormalizeUriPath(getShouldNormalizeUriPath());
            clone.setSignBody(getSignBody());
            clone.setExpirationInSeconds(getExpirationInSeconds());

            // success, bump up the ref count so we can escape the try-with-resources block
            clone.addRef();
            return clone;
        }
    }

    /**
     * Required override method that must begin the release process of the acquired native handle
     */
    @Override
    protected void releaseNativeHandle() {}

    /**
     * Override that determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources with asynchronous shutdown processes should override this with false, and establish a callback from native code that
     * invokes releaseReferences() when the asynchronous shutdown process has completed.  See HttpClientConnectionManager for an example.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    public void setAlgorithm(AwsSigningAlgorithm algorithm) { this.algorithm = algorithm.getNativeValue(); }
    public AwsSigningAlgorithm getAlgorithm() {
        return AwsSigningAlgorithm.getEnumValueFromInteger(algorithm);
    }

    public void setTransform(AwsRequestSigningTransform transform) { this.transform = transform.getNativeValue(); }
    public AwsRequestSigningTransform getTransform() {
        return AwsRequestSigningTransform.getEnumValueFromInteger(transform);
    }

    public void setRegion(String region) { this.region = region; }
    public String getRegion() { return region; }

    public void setService(String service) { this.service = service; }
    public String getService() { return service; }

    public void setTime(long time) { this.time = time; }
    public long getTime() { return this.time; }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        swapReferenceTo(this.credentialsProvider, credentialsProvider);
        this.credentialsProvider = credentialsProvider;
    }

    public CredentialsProvider getCredentialsProvider() { return credentialsProvider; }

    public void setCredentials(Credentials credentials) { this.credentials = credentials; }
    public Credentials getCredentials() { return credentials; }

    public void setShouldSignParameter(Predicate<String> shouldSignParameter) { this.shouldSignParameter = shouldSignParameter; }
    public Predicate<String> getShouldSignParameter() { return shouldSignParameter; }

    public void setUseDoubleUriEncode(boolean useDoubleUriEncode) { this.useDoubleUriEncode = useDoubleUriEncode; }
    public boolean getUseDoubleUriEncode() { return useDoubleUriEncode; }

    public void setShouldNormalizeUriPath(boolean shouldNormalizeUriPath) { this.shouldNormalizeUriPath = shouldNormalizeUriPath; }
    public boolean getShouldNormalizeUriPath() { return shouldNormalizeUriPath; }

    public void setSignBody(AwsBodySigningConfigType signBody) { this.signBody = signBody.getNativeValue(); }
    public AwsBodySigningConfigType getSignBody() { return AwsBodySigningConfigType.getEnumValueFromInteger(signBody); }

    public void setExpirationInSeconds(long expirationInSeconds) { this.expirationInSeconds = expirationInSeconds; }
    public long getExpirationInSeconds() { return expirationInSeconds; }
}




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

    public enum AwsSignatureType {
        HTTP_REQUEST_VIA_HEADERS(0),
        HTTP_REQUEST_VIA_QUERY_PARAMS(1),
        HTTP_REQUEST_CHUNK(2),
        HTTP_REQUEST_EVENT(3);

        AwsSignatureType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

        public static AwsSignatureType getEnumValueFromInteger(int value) {
            AwsSignatureType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal signature type value in signing configuration");
        }

        private static Map<Integer, AwsSignatureType> buildEnumMapping() {
            Map<Integer, AwsSignatureType> enumMapping = new HashMap<Integer, AwsSignatureType>();
            enumMapping.put(HTTP_REQUEST_VIA_HEADERS.getNativeValue(), HTTP_REQUEST_VIA_HEADERS);
            enumMapping.put(HTTP_REQUEST_VIA_QUERY_PARAMS.getNativeValue(), HTTP_REQUEST_VIA_QUERY_PARAMS);
            enumMapping.put(HTTP_REQUEST_CHUNK.getNativeValue(), HTTP_REQUEST_CHUNK);
            enumMapping.put(HTTP_REQUEST_EVENT.getNativeValue(), HTTP_REQUEST_EVENT);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsSignatureType> enumMapping = buildEnumMapping();
    }

    public enum AwsSignedBodyValueType {
        EMPTY(0),
        PAYLOAD(1),
        UNSIGNED_PAYLOAD(2),
        STREAMING_AWS4_HMAC_SHA256_PAYLOAD(3),
        STREAMING_AWS4_HMAC_SHA256_EVENTS(4);

        AwsSignedBodyValueType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

        public static AwsSignedBodyValueType getEnumValueFromInteger(int value) {
            AwsSignedBodyValueType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal signed body value type value in signing configuration");
        }

        private static Map<Integer, AwsSignedBodyValueType> buildEnumMapping() {
            Map<Integer, AwsSignedBodyValueType> enumMapping = new HashMap<Integer, AwsSignedBodyValueType>();
            enumMapping.put(EMPTY.getNativeValue(), EMPTY);
            enumMapping.put(PAYLOAD.getNativeValue(), PAYLOAD);
            enumMapping.put(UNSIGNED_PAYLOAD.getNativeValue(), UNSIGNED_PAYLOAD);
            enumMapping.put(STREAMING_AWS4_HMAC_SHA256_PAYLOAD.getNativeValue(), STREAMING_AWS4_HMAC_SHA256_PAYLOAD);
            enumMapping.put(STREAMING_AWS4_HMAC_SHA256_EVENTS.getNativeValue(), STREAMING_AWS4_HMAC_SHA256_EVENTS);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsSignedBodyValueType> enumMapping = buildEnumMapping();
    }

    public enum AwsSignedBodyHeaderType {
        NONE(0),
        X_AMZ_CONTENT_SHA256(1);

        AwsSignedBodyHeaderType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

        public static AwsSignedBodyHeaderType getEnumValueFromInteger(int value) {
            AwsSignedBodyHeaderType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal signed body header value in signing configuration");
        }

        private static Map<Integer, AwsSignedBodyHeaderType> buildEnumMapping() {
            Map<Integer, AwsSignedBodyHeaderType> enumMapping = new HashMap<Integer, AwsSignedBodyHeaderType>();
            enumMapping.put(NONE.getNativeValue(), NONE);
            enumMapping.put(X_AMZ_CONTENT_SHA256.getNativeValue(), X_AMZ_CONTENT_SHA256);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsSignedBodyHeaderType> enumMapping = buildEnumMapping();
    }

    private int algorithm = AwsSigningAlgorithm.SIGV4.getNativeValue();
    private int signatureType = AwsSignatureType.HTTP_REQUEST_VIA_HEADERS.getNativeValue();
    private String region;
    private String service;
    private long time = System.currentTimeMillis();
    private CredentialsProvider credentialsProvider;
    private Credentials credentials;
    private Predicate<String> shouldSignHeader;
    private boolean useDoubleUriEncode = true;
    private boolean shouldNormalizeUriPath = true;
    private boolean omitSessionToken = false;
    private int signedBodyValue = AwsSignedBodyValueType.PAYLOAD.getNativeValue();
    private int signedBodyHeader = AwsSignedBodyHeaderType.NONE.getNativeValue();
    private long expirationInSeconds = 0;

    public AwsSigningConfig() {}

    public AwsSigningConfig clone() {
        try (AwsSigningConfig clone = new AwsSigningConfig()) {

            clone.setAlgorithm(getAlgorithm());
            clone.setSignatureType(getSignatureType());
            clone.setRegion(getRegion());
            clone.setService(getService());
            clone.setTime(getTime());
            clone.setCredentialsProvider(getCredentialsProvider());
            clone.setCredentials(getCredentials());
            clone.setShouldSignHeader(getShouldSignHeader());
            clone.setUseDoubleUriEncode(getUseDoubleUriEncode());
            clone.setShouldNormalizeUriPath(getShouldNormalizeUriPath());
            clone.setOmitSessionToken(getOmitSessionToken());
            clone.setSignedBodyValue(getSignedBodyValue());
            clone.setSignedBodyHeader(getSignedBodyHeader());
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

    public void setSignatureType(AwsSignatureType signatureType) { this.signatureType = signatureType.getNativeValue(); }
    public AwsSignatureType getSignatureType() {
        return AwsSignatureType.getEnumValueFromInteger(signatureType);
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

    public void setShouldSignHeader(Predicate<String> shouldSignHeader) { this.shouldSignHeader = shouldSignHeader; }
    public Predicate<String> getShouldSignHeader() { return shouldSignHeader; }

    public void setUseDoubleUriEncode(boolean useDoubleUriEncode) { this.useDoubleUriEncode = useDoubleUriEncode; }
    public boolean getUseDoubleUriEncode() { return useDoubleUriEncode; }

    public void setShouldNormalizeUriPath(boolean shouldNormalizeUriPath) { this.shouldNormalizeUriPath = shouldNormalizeUriPath; }
    public boolean getShouldNormalizeUriPath() { return shouldNormalizeUriPath; }

    public void setOmitSessionToken(boolean omitSessionToken) { this.omitSessionToken = omitSessionToken; }
    public boolean getOmitSessionToken() { return omitSessionToken; }

    public void setSignedBodyValue(AwsSignedBodyValueType signedBodyValue) { this.signedBodyValue = signedBodyValue.getNativeValue(); }
    public AwsSignedBodyValueType getSignedBodyValue() { return AwsSignedBodyValueType.getEnumValueFromInteger(signedBodyValue); }

    public void setSignedBodyHeader(AwsSignedBodyHeaderType signedBodyHeader) { this.signedBodyHeader = signedBodyHeader.getNativeValue(); }
    public AwsSignedBodyHeaderType getSignedBodyHeader() { return AwsSignedBodyHeaderType.getEnumValueFromInteger(signedBodyHeader); }

    public void setExpirationInSeconds(long expirationInSeconds) { this.expirationInSeconds = expirationInSeconds; }
    public long getExpirationInSeconds() { return expirationInSeconds; }
}




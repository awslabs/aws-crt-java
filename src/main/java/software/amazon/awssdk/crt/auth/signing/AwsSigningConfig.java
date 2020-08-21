/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.auth.signing;

import java.util.function.Predicate;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.cal.EccKeyPair;
import software.amazon.awssdk.crt.CrtResource;

/**
 * A class representing
 */
public class AwsSigningConfig extends CrtResource {

    public enum AwsSigningAlgorithm {
        SIGV4(0),
        SIGV4_ASYMMETRIC(1);

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
            enumMapping.put(SIGV4_ASYMMETRIC.getNativeValue(), SIGV4_ASYMMETRIC);

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

    public class AwsSignedBodyValue {
        public static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
        public static final String STREAMING_AWS4_HMAC_SHA256_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";
        public static final String STREAMING_AWS4_HMAC_SHA256_EVENTS = "STREAMING-AWS4-HMAC-SHA256-EVENTS";
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
    private String signedBodyValue = null;
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

    public void setSignedBodyValue(String signedBodyValue) {
        if (signedBodyValue != null && signedBodyValue.isEmpty()) {
            throw new IllegalArgumentException("Signed Body Value must be null or non-empty string.");
        }
        this.signedBodyValue = signedBodyValue;
    }

    public String getSignedBodyValue() { return signedBodyValue; }

    public void setSignedBodyHeader(AwsSignedBodyHeaderType signedBodyHeader) { this.signedBodyHeader = signedBodyHeader.getNativeValue(); }
    public AwsSignedBodyHeaderType getSignedBodyHeader() { return AwsSignedBodyHeaderType.getEnumValueFromInteger(signedBodyHeader); }

    public void setExpirationInSeconds(long expirationInSeconds) { this.expirationInSeconds = expirationInSeconds; }
    public long getExpirationInSeconds() { return expirationInSeconds; }
}




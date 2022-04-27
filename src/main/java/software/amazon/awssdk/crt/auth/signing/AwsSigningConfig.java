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

/**
 * A class representing configuration related to signing something "signable" (an http request, a body chunk, a
 * stream event) via an AWS signing process.
 */
public class AwsSigningConfig implements AutoCloseable {

    /* For backwards compatibility only */
    public void close() {}

    /**
     * What version of the AWS signing process should we use.
     */
    public enum AwsSigningAlgorithm {

        /** Standard AWS Sigv4 signing, based on AWS credentials and symmetric secrets */
        SIGV4(0),

        /** AWS Sigv4a signing, based on ECDSA signatures */
        SIGV4_ASYMMETRIC(1);

        /**
         * Constructs a Java enum value from the associated native enum value
         * @param nativeValue native enum value
         */
        AwsSigningAlgorithm(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        /**
         * Trivial Java Enum value to native enum value conversion function
         * @return integer associated with this enum value
         */
        public int getNativeValue() { return nativeValue; }

        /**
         * Creates a Java enum value from a native enum value as an integer
         * @param value native enum value
         * @return the corresponding Java enum value
         */
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

    /**
     * What sort of signature should be computed from the signable?
     */
    public enum AwsSignatureType {

        /**
         * A signature for a full http request should be computed, with header updates applied to the signing result.
         */
        HTTP_REQUEST_VIA_HEADERS(0),

        /**
         * A signature for a full http request should be computed, with query param updates applied to the signing result.
         */
        HTTP_REQUEST_VIA_QUERY_PARAMS(1),

        /**
         * Compute a signature for a payload chunk.
         */
        HTTP_REQUEST_CHUNK(2),

        /**
         * Compute a signature for an event stream event.
         *
         * This option is not yet supported.
         */
        HTTP_REQUEST_EVENT(3),

        /**
         * Compute a signature for a payloads trailing headers.
         */
        HTTP_REQUEST_TRAILING_HEADERS(6);

        /**
         * Constructs a Java enum value from a native enum value as an integer
         * @param nativeValue native enum value
         */
        AwsSignatureType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        /**
         * Gets the native enum value as an integer that is associated with this Java enum value
         * @return this value's associated native enum value
         */
        public int getNativeValue() { return nativeValue; }

        /**
         * Creates a Java enum value from a native enum value as an integer
         * @param value native enum value
         * @return the corresponding Java enum value
         */
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
            enumMapping.put(HTTP_REQUEST_TRAILING_HEADERS.getNativeValue(), HTTP_REQUEST_TRAILING_HEADERS);

            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, AwsSignatureType> enumMapping = buildEnumMapping();
    }

    /**
     * A set of string constants for various canonical request payload values.  If signed body header type is not NONE
     * then the value will also be reflected in X-Amz-Content-Sha256
     */
    public class AwsSignedBodyValue {
        public static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
        public static final String STREAMING_AWS4_HMAC_SHA256_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";
        public static final String STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD = "STREAMING-AWS4-ECDSA-P256-SHA256-PAYLOAD";
        public static final String STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD_TRAILER = "STREAMING-AWS4-ECDSA-P256-SHA256-PAYLOAD-TRAILER";
        public static final String STREAMING_AWS4_HMAC_SHA256_EVENTS = "STREAMING-AWS4-HMAC-SHA256-EVENTS";
    }

    /**
     * Controls if signing adds a header containing the canonical request's body value
     */
    public enum AwsSignedBodyHeaderType {
        /** Do not add any signing information about the body to the signed request */
        NONE(0),

        /** Add the 'X-Amz-Content-Sha256' header to the signed request */
        X_AMZ_CONTENT_SHA256(1);

        /**
         * Constructs a Java enum value from a native enum value as an integer
         * @param nativeValue native enum value
         */
        AwsSignedBodyHeaderType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        /**
         * Gets the native enum value as an integer that is associated with this Java enum value
         * @return this value's associated native enum value
         */
        public int getNativeValue() { return nativeValue; }

        /**
         * Creates a Java enum value from a native enum value as an integer
         * @param value native enum value
         * @return the corresponding Java enum value
         */
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

    /**
     * Default constructor
     */
    public AwsSigningConfig() {}

    /**
     * Creates a new signing configuration from this one.
     * @return a clone of this signing configuration
     */
    public AwsSigningConfig clone() {
        AwsSigningConfig clone = new AwsSigningConfig();

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

        return clone;
    }


    /**
     * Sets what version of the AWS signing process should be used
     * @param algorithm desired version of the AWS signing process
     */
    public void setAlgorithm(AwsSigningAlgorithm algorithm) { this.algorithm = algorithm.getNativeValue(); }

    /**
     * Gets what version of the AWS signing procecss will be used
     * @return what version of the AWS signing procecss will be used
     */
    public AwsSigningAlgorithm getAlgorithm() {
        return AwsSigningAlgorithm.getEnumValueFromInteger(algorithm);
    }

    /**
     * Sets what sort of signature should be computed
     * @param signatureType what kind of signature to compute
     */
    public void setSignatureType(AwsSignatureType signatureType) { this.signatureType = signatureType.getNativeValue(); }

    /**
     * Gets what kind of signature will be computed
     * @return what kind of signature will be computed
     */
    public AwsSignatureType getSignatureType() {
        return AwsSignatureType.getEnumValueFromInteger(signatureType);
    }

    /**
     * Sets what to use for region when signing.  Depending on the algorithm, this may not be an actual region name
     * and so no validation is done on this parameter.  In sigv4a, this value is used for the "region-set" concept.
     * @param region region value to use when signing
     */
    public void setRegion(String region) { this.region = region; }

    /**
     * Gets what will be used for the region or region-set concept during signing.
     * @return what will be used for the region or region-set concept during signing
     */
    public String getRegion() { return region; }

    /**
     * Sets what service signing name to use.
     * @param service signing name of the service that this signing calculation should use
     */
    public void setService(String service) { this.service = service; }

    /**
     * Gets what service signing name will be used
     * @return what service signing name will be used
     */
    public String getService() { return service; }

    /**
     * Sets the point in time that signing should be relative to.  Not Instant for Android API level support reasons.
     * Additionally, for http requests, X-Amz-Date will be added to the request using this time point.
     * @param time point in time, as milliseconds since epoch, that signing should be relative to
     */
    public void setTime(long time) { this.time = time; }

    /**
     * Gets the point in time (in milliseconds since epoch) that signing will be done relative to
     * @return the point in time (in milliseconds since epoch) that signing will be done relative to
     */
    public long getTime() { return this.time; }

    /**
     * Sets the provider to use to source credentials from before signing.
     * @param credentialsProvider provider to retrieve credentials from prior to signing
     */
    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Gets the provider to source credentials from before signing
     * @return the provider to source credentials from before signing
     */
    public CredentialsProvider getCredentialsProvider() { return credentialsProvider; }

    /**
     * Sets the credentials to use for signing.  Overrides the provider setting if non-null.
     * @param credentials credentials to use for signing
     */
    public void setCredentials(Credentials credentials) { this.credentials = credentials; }

    /**
     * Gets the credentials to use for signing.
     * @return credentials to use for signing
     */
    public Credentials getCredentials() { return credentials; }

    /**
     * Sets a header-name signing predicate filter.  Headers that do not pass the filter will not be signed.
     * @param shouldSignHeader header-name signing predicate filter
     */
    public void setShouldSignHeader(Predicate<String> shouldSignHeader) { this.shouldSignHeader = shouldSignHeader; }

    /**
     * Gets the header-name signing predicate filter to use
     * @return the header-name signing predicate filter to use
     */
    public Predicate<String> getShouldSignHeader() { return shouldSignHeader; }

    /**
     * Sets whether or not signing should uri encode urls as part of canonical request construction.
     * We assume the uri will be encoded once in preparation for transmission.  Certain services
     * do not decode before checking signature, requiring us to actually double-encode the uri in the canonical
     * request in order to pass a signature check.
     * @param useDoubleUriEncode should signing uri encode urls in the canonical request
     */
    public void setUseDoubleUriEncode(boolean useDoubleUriEncode) { this.useDoubleUriEncode = useDoubleUriEncode; }

    /**
     * Gets whether or not signing will uri encode urls during canonical request construction
     * @return whether or not signing will uri encode urls during canonical request construction
     */
    public boolean getUseDoubleUriEncode() { return useDoubleUriEncode; }

    /**
     * Sets whether or not the uri path should be normalized during canonical request construction
     * @param shouldNormalizeUriPath whether or not the uri path should be normalized during canonical request construction
     */
    public void setShouldNormalizeUriPath(boolean shouldNormalizeUriPath) { this.shouldNormalizeUriPath = shouldNormalizeUriPath; }

    /**
     * Gets whether or not the uri path should be normalized during canonical request construction
     * @return whether or not the uri path should be normalized during canonical request construction
     */
    public boolean getShouldNormalizeUriPath() { return shouldNormalizeUriPath; }

    /**
     * Sets whether or not X-Amz-Session-Token should be added to the canonical request when signing with session
     * credentials.
     *
     * "X-Amz-Security-Token" is added during signing, as a header or
     * query param, when credentials have a session token.
     * If false (the default), this parameter is included in the canonical request.
     * If true, this parameter is still added, but omitted from the canonical request.
     *
     * @param omitSessionToken whether or not X-Amz-Session-Token should be added to the canonical request when signing with session
     *                         credentials
     */
    public void setOmitSessionToken(boolean omitSessionToken) { this.omitSessionToken = omitSessionToken; }

    /**
     * Gets whether or not X-Amz-Session-Token should be added to the canonical request when signing with session
     * credentials.
     * @return whether or not X-Amz-Session-Token should be added to the canonical request when signing with session
     * credentials
     */
    public boolean getOmitSessionToken() { return omitSessionToken; }

    /**
     * Sets the payload hash override value to use in canonical request construction.  If the signed body header type is
     * not set to null, then the designated header will also take on this value.  If this value is NULL, then the signer
     * will compute the SHA256 of the body stream and use that instead.
     * @param signedBodyValue payload hash override value to use in canonical request construction
     */
    public void setSignedBodyValue(String signedBodyValue) {
        if (signedBodyValue != null && signedBodyValue.isEmpty()) {
            throw new IllegalArgumentException("Signed Body Value must be null or non-empty string.");
        }
        this.signedBodyValue = signedBodyValue;
    }

    /**
     * Gets the payload hash override to use in canonical request construction.
     * @return the payload hash override to use in canonical request construction
     */
    public String getSignedBodyValue() { return signedBodyValue; }

    /**
     * Sets what signed body header should hold the payload hash (or override value).
     * @param signedBodyHeader what signed body header should hold the payload hash (or override value)
     */
    public void setSignedBodyHeader(AwsSignedBodyHeaderType signedBodyHeader) { this.signedBodyHeader = signedBodyHeader.getNativeValue(); }

    /**
     * Gets what signed body header should hold the payload hash (or override value).
     * @return what signed body header should hold the payload hash (or override value)
     */
    public AwsSignedBodyHeaderType getSignedBodyHeader() { return AwsSignedBodyHeaderType.getEnumValueFromInteger(signedBodyHeader); }

    /**
     * Sets the expiration time in seconds when using query param signing (pre-signed url).  The appropriate query param
     * will be added to the URL when building the canonical and signed requests.
     * @param expirationInSeconds time in seconds that a pre-signed url will be valid for
     */
    public void setExpirationInSeconds(long expirationInSeconds) { this.expirationInSeconds = expirationInSeconds; }

    /**
     * Gets the expiration time in seconds to use when signing to make a pre-signed url.
     * @return the expiration time in seconds for a pre-signed url
     */
    public long getExpirationInSeconds() { return expirationInSeconds; }
}




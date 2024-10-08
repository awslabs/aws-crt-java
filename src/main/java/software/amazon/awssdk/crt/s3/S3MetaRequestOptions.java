/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class S3MetaRequestOptions {

    /**
     * A Meta Request represents a group of generated requests that are being done on behalf of the
     * original request. For example, one large GetObject request can be transformed into a series
     * of ranged GetObject requests that are executed in parallel to improve throughput.
     *
     * The MetaRequestType is a hint of transformation to be applied.
     */
    public enum MetaRequestType {
        /**
         * The Default meta request type sends any request to S3 as-is (with no transformation). For example,
         * it can be used to pass a CreateBucket request.
         */
        DEFAULT(0),

        /**
         * The GetObject request will be split into a series of ranged GetObject requests that are
         * executed in parallel to improve throughput, when possible.
         */
        GET_OBJECT(1),

        /**
         * The PutObject request will be split into MultiPart uploads that are executed in parallel
         * to improve throughput, when possible.
         */
        PUT_OBJECT(2),

        /**
         * The CopyObject meta request performs a multi-part copy using multiple S3 UploadPartCopy requests
         * in parallel, or bypasses a CopyObject request to S3 if the object size is not large enough for
         * a multipart upload.
         */
        COPY_OBJECT(3);

        MetaRequestType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() {
            return nativeValue;
        }

        public static MetaRequestType getEnumValueFromInteger(int value) {
            MetaRequestType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Invalid S3 Meta Request type");
        }

        private static Map<Integer, MetaRequestType> buildEnumMapping() {
            Map<Integer, MetaRequestType> enumMapping = new HashMap<Integer, MetaRequestType>();
            enumMapping.put(DEFAULT.getNativeValue(), DEFAULT);
            enumMapping.put(GET_OBJECT.getNativeValue(), GET_OBJECT);
            enumMapping.put(PUT_OBJECT.getNativeValue(), PUT_OBJECT);
            enumMapping.put(COPY_OBJECT.getNativeValue(), COPY_OBJECT);
            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, MetaRequestType> enumMapping = buildEnumMapping();
    }

    private MetaRequestType metaRequestType;
    private String operationName;
    private ChecksumConfig checksumConfig;
    private HttpRequest httpRequest;
    private Path requestFilePath;
    private Path responseFilePath;
    private ResponseFileOption responseFileOption = ResponseFileOption.CREATE_OR_REPLACE;
    private long responseFilePosition = 0;
    private boolean responseFileDeleteOnFailure = false;
    private S3MetaRequestResponseHandler responseHandler;
    private CredentialsProvider credentialsProvider;
    private AwsSigningConfig signingConfig;
    private URI endpoint;
    private ResumeToken resumeToken;
    private Long objectSizeHint;

    public S3MetaRequestOptions withMetaRequestType(MetaRequestType metaRequestType) {
        this.metaRequestType = metaRequestType;
        return this;
    }

    public MetaRequestType getMetaRequestType() {
        return metaRequestType;
    }

    /**
     * The S3 operation name (eg: "CreateBucket"),
     * this MUST be set for {@link MetaRequestType#DEFAULT},
     * it is ignored for other meta request types since the operation is implicit.
     *
     * See <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html">
     * S3 API documentation</a> for the canonical list of names.
     *
     * This name is used to fill out details in metrics and error reports.
     * It also drives some operation-specific behavior.
     * If you pass the wrong name, you risk getting the wrong behavior.
     *
     * For example, every operation except "GetObject" has its response checked
     * for error, even if the HTTP status-code was 200 OK
     * (see <a href=https://repost.aws/knowledge-center/s3-resolve-200-internalerror>knowledge center</a>).
     * If you used the {@link MetaRequestType#DEFAULT DEFAULT} type to do
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html">GetObject</a>,
     * but mis-named it "Download", and the object looked like XML with an error code,
     * then the meta-request would fail. You risk logging the full response body,
     * and leaking sensitive data.
     * @param operationName the operation name for this {@link MetaRequestType#DEFAULT} meta request
     * @return this
     */
    public S3MetaRequestOptions withOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    public String getOperationName() {
        return operationName;
    }

    /**
     * The config related to checksum used for the meta request. See {@link ChecksumConfig} for details.
     * @param checksumConfig The checksum config used for the meta request
     * @return this
     */
    public S3MetaRequestOptions withChecksumConfig(ChecksumConfig checksumConfig) {
        this.checksumConfig = checksumConfig;
        return this;
    }

    public ChecksumConfig getChecksumConfig() {
        return this.checksumConfig;
    }

    /**
     * @deprecated Please use {@link #withChecksumConfig(ChecksumConfig)} instead.
     * Specify the checksum algorithm to use use for put requests, if unset defaults to NONE and no checksum will be calculated.
     * The location of the checksum will be default to trailer.
     *
     * @param checksumAlgorithm the checksum algorithm to use use for put requests
     * @return this
     */
    public S3MetaRequestOptions withChecksumAlgorithm(ChecksumAlgorithm checksumAlgorithm) {
        ChecksumConfig config = new ChecksumConfig().withChecksumAlgorithm(checksumAlgorithm).withChecksumLocation(ChecksumConfig.ChecksumLocation.TRAILER);
        this.checksumConfig = config;
        return this;
    }

    /**
     * @deprecated
     * @return the checksum algorithm to use use for put requests
     */
    public ChecksumAlgorithm getChecksumAlgorithm() {
        return this.checksumConfig.getChecksumAlgorithm();
    }

    /**
     * @deprecated  Please use {@link #withChecksumConfig(ChecksumConfig)} instead.
     * validateChecksum defaults to false, if set to true, it will cause the client to compare a streamed
     * calculation of the objects checksum to a remotely stored checksum assigned to the object if one exists.
     * The checksumValidated field passed in parameter of the finished callback will inform
     * the user if validation ocurred. A mismatch will result in a AWS_ERROR_S3_RESPONSE_CHECKSUM_MISMATCH error
     *
     * @param validateChecksum Validate the checksum of response if server provides.
     * @return this
     */
    public S3MetaRequestOptions withValidateChecksum(boolean validateChecksum) {
        ChecksumConfig config = new ChecksumConfig().withValidateChecksum(validateChecksum);
        this.checksumConfig = config;
        return this;
    }

    /**
     * @deprecated
     * @return Validate the checksum of response if server provides.
     */
    public boolean getValidateChecksum() {
        return checksumConfig.getValidateChecksum();
    }

    /**
     * Set the initial HTTP request.
     *
     * Note: When uploading a file, you can get better performance by setting
     * {@link withRequestFilePath} instead of setting a body stream on the HttpRequest.
     * (If both are set, the file path is used and body stream is ignored)
     *
     * @param httpRequest initial HTTP request message.
     * @return this
     */
    public S3MetaRequestOptions withHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * If set, this file is sent as the request's body, and the {@link withHttpRequest} body stream is ignored.
     *
     * This can give better upload performance than sending data using the body stream.
     *
     * @param requestFilePath path to file to send as the request's body.
     * @return this
     */
    public S3MetaRequestOptions withRequestFilePath(Path requestFilePath) {
        this.requestFilePath = requestFilePath;
        return this;
    }

    public Path getRequestFilePath() {
        return requestFilePath;
    }

    public S3MetaRequestOptions withResponseHandler(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public S3MetaRequestResponseHandler getResponseHandler() {
        return responseHandler;
    }

    /**
     * @deprecated Please use {@link #withSigningConfig(AwsSigningConfig)} instead.
     * The credentials provider will be used to create the signing Config to override the client level config.
     * The client config will be used.
     *
     * @param credentialsProvider provide credentials for signing.
     * @return this
     */
    public S3MetaRequestOptions withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    /**
     * The configuration related to signing used by S3 client. It will override the client level configuration if provided.
     * `AwsSigningConfig.getDefaultS3SigningConfig(region, credentialsProvider);` can be used as helper to create the default configuration to be used for S3.
     *
     * If not set, the client configuration will be used.
     * If set:
     *  - All fields are optional. The credentials will be resolve from client if not set.
     *  - S3 Client will derive the right config for signing process based on this.
     *
     * Notes:
     * - For SIGV4_S3EXPRESS, S3 client will use the credentials in the config to derive the S3Express
     *      credentials that are used in the signing process.
     * - Client may make modifications to signing config before passing it on to signer.
     *
     * @param signingConfig configuration related to signing via an AWS signing process.
     * @return this
     */
    public S3MetaRequestOptions withSigningConfig(AwsSigningConfig signingConfig) {
        this.signingConfig = signingConfig;
        return this;
    }

    public AwsSigningConfig getSigningConfig() {
        return signingConfig;
    }

    public S3MetaRequestOptions withEndpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public S3MetaRequestOptions withResumeToken(ResumeToken resumeToken) {
        this.resumeToken = resumeToken;
        return this;
    }

    public ResumeToken getResumeToken() {
        return resumeToken;
    }

    /*
     * (Optional)
     * Total object size hint, in bytes.
     * The optimal strategy for downloading a file depends on its size.
     * Set this hint to help the S3 client choose the best strategy for this particular file.
     * This is just used as an estimate, so it's okay to provide an approximate value if the exact size is unknown.
     */
    public S3MetaRequestOptions withObjectSizeHint(Long objectSizeHint) {
        this.objectSizeHint = objectSizeHint;
        return this;
    }

    public Long getObjectSizeHint() {
        return objectSizeHint;
    }

    public enum ResponseFileOption {
        /**
         * Create a new file if it doesn't exist, otherwise replace the existing file.
         */
        CREATE_OR_REPLACE(0),

        /**
         * Always create a new file. If the file already exists,
         * AWS_ERROR_S3_RECV_FILE_EXISTS will be raised.
         */
        CREATE_NEW(1),

        /**
         * Create a new file if it doesn't exist, otherwise append to the existing file.
         */
        CREATE_OR_APPEND(2),

        /**
         * Write to an existing file at the specified position, defined by the
         * {@link withHttpRequest}.
         * If the file does not exist, AWS_ERROR_S3_RECV_FILE_NOT_EXISTS will be raised.
         * If {@link withHttpRequest} is not configured, start overwriting data at the
         * beginning of the file (byte 0).
         */
        WRITE_TO_POSITION(3);

        ResponseFileOption(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() {
            return nativeValue;
        }

        public static ResponseFileOption getEnumValueFromInteger(int value) {
            ResponseFileOption enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Invalid S3 ResponseFileOption");
        }

        private static Map<Integer, ResponseFileOption> buildEnumMapping() {
            Map<Integer, ResponseFileOption> enumMapping = new HashMap<Integer, ResponseFileOption>();
            enumMapping.put(CREATE_OR_REPLACE.getNativeValue(), CREATE_OR_REPLACE);
            enumMapping.put(CREATE_NEW.getNativeValue(), CREATE_NEW);
            enumMapping.put(CREATE_OR_APPEND.getNativeValue(), CREATE_OR_APPEND);
            enumMapping.put(WRITE_TO_POSITION.getNativeValue(), WRITE_TO_POSITION);
            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, ResponseFileOption> enumMapping = buildEnumMapping();
    }

    /**
     * If set, this file will be used to write the response body to a file.
     * And the {@link HttpStreamResponseHandler#onResponseBody} will not be invoked.
     * {@link withResponseFileOption} configures the write behavior.
     *
     * @param responseFilePath path to file to write response body to.
     * @return this
     */
    public S3MetaRequestOptions withResponseFilePath(Path responseFilePath) {
        this.responseFilePath = responseFilePath;
        return this;
    }

    public Path getResponseFilePath() {
        return responseFilePath;
    }

    /**
     * Sets the option for how to handle the response file when downloading an
     * object from S3.
     * This option is only applicable when {@link withResponseFilePath} is set.
     *
     * By default, the option is set to
     * {@link ResponseFileOption#CREATE_OR_REPLACE}.
     *
     * @param responseFileOption The option for handling the response file.
     * @return this
     */
    public S3MetaRequestOptions withResponseFileOption(ResponseFileOption responseFileOption) {
        this.responseFileOption = responseFileOption;
        return this;
    }

    public ResponseFileOption getResponseFileOption() {
        return responseFileOption;
    }

    /**
     * Sets the position to start writing to the response file.
     * This option is only applicable when {@link withResponseFileOption} is set
     * to {@link ResponseFileOption#WRITE_TO_POSITION}.
     *
     * @param responseFilePosition The position to start writing to the response
     *                             file.
     * @return this
     */
    public S3MetaRequestOptions withResponseFilePosition(long responseFilePosition) {
        this.responseFilePosition = responseFilePosition;
        return this;
    }

    public long getResponseFilePosition() {
        return responseFilePosition;
    }

    /**
     * Sets whether to delete the response file on failure when downloading an
     * object from S3.
     * This option is only applicable when a response file path is set.
     *
     * @param responseFileDeleteOnFailure True to delete the response file on
     *                                    failure,
     *                                    False to leave it as-is.
     * @return this
     */
    public S3MetaRequestOptions withResponseFileDeleteOnFailure(boolean responseFileDeleteOnFailure) {
        this.responseFileDeleteOnFailure = responseFileDeleteOnFailure;
        return this;
    }
    public boolean getResponseFileDeleteOnFailure() {
        return responseFileDeleteOnFailure;
    }
}

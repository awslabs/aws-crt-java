package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;

import java.net.URI;
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
    private ChecksumAlgorithm checksumAlgorithm;
    private boolean validateChecksum;
    private HttpRequest httpRequest;
    private S3MetaRequestResponseHandler responseHandler;
    private CredentialsProvider credentialsProvider;
    private URI endpoint;

    public S3MetaRequestOptions withMetaRequestType(MetaRequestType metaRequestType) {
        this.metaRequestType = metaRequestType;
        return this;
    }

    public MetaRequestType getMetaRequestType() {
        return metaRequestType;
    }

    /*
     * Specify the checksum algorithm to use use for put requests, if unset defaults to NONE and no checksum will be calculated.
     */
    public S3MetaRequestOptions withChecksumAlgorithm(ChecksumAlgorithm checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
        return this;
    }

    public ChecksumAlgorithm getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    /*
     * validateChecksum defaults to false, if set to true, it will cause the client to compare a streamed 
     * calculation of the objects checksum to a remotely stored checksum assigned to the object if one exists.
     * The checksumValidated field passed in parameter of the finished callback will inform
     * the user if validation occured. A mismatch will result in a AWS_ERROR_S3_RESPONSE_CHECKSUM_MISMATCH error
     */
    public S3MetaRequestOptions withValidateChecksum(boolean validateChecksum) {
        this.validateChecksum = validateChecksum;
        return this;
    }

    public boolean getValidateChecksum() {
        return validateChecksum;
    }

    public S3MetaRequestOptions withHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public S3MetaRequestOptions withResponseHandler(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public S3MetaRequestResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public S3MetaRequestOptions withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public S3MetaRequestOptions withEndpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public URI getEndpoint() {
        return endpoint;
    }
}

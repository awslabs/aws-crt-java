package software.amazon.awssdk.crt.s3;

public class S3FinishedResponseContext {
    private final int errorCode;
    private final int responseStatus;
    private final byte[] errorPayload;
    private final ChecksumAlgorithm checksumAlgorithm;
    private final boolean didValidateChecksum;

    /*
     * errorCode The CRT error code
     * responseStatus statusCode of the HTTP response
     * errorPayload body of the error response. Can be null if the request completed successfully
     * checksumAlgorithm, the algorith used to validate the Body, None if not validated
     * didValidateChecksum which is true if the response was validated.
     */
    S3FinishedResponseContext(final int errorCode, final int responseStatus, final byte[] errorPayload, final ChecksumAlgorithm checksumAlgorithm, final boolean didValidateChecksum) {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.errorPayload = errorPayload;
        this.checksumAlgorithm = checksumAlgorithm;
        this.didValidateChecksum = didValidateChecksum;
    }

    public int getErrorCode () {
        return this.errorCode;
    }

    /*
     * If the request didn't receive a response due to a connection 
     * failure or some othe issue the response status will be 0.
     */
    public int getResponseStatus () {
        return this.responseStatus;
    }

    /*
     * In the case of a failed http response get the payload of the response.
     */
    public byte[] getErrorPayload () {
        return this.errorPayload;
    }

    /*
     * if no checksum is found, or the request finished with an error the Algorithm will be None,
     * otherwise the algorithm will correspond to the one attached to the object when uploaded.
     */
    public ChecksumAlgorithm getChecksumAlgorithm () {
        return this.checksumAlgorithm;
    }
    public boolean isChecksumValidated () {
        return this.didValidateChecksum;
    }
}

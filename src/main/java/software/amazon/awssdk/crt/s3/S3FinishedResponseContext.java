package software.amazon.awssdk.crt.s3;

public class S3FinishedResponseContext {
    private final int errorCode;
    private final int responseStatus;
    private final byte[] errorPayload;
    private final ChecksumAlgorithm checksumAlgorithm;
    private final boolean didValidateChecksum;

    S3FinishedResponseContext(final int errorCode, final int responseStatus, final byte[] errorPayload, final ChecksumAlgorithm checksumAlgorithm, final boolean didValidateChecksum) {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.errorPayload = errorPayload;
        this.checksumAlgorithm = checksumAlgorithm;
        this.didValidateChecksum = didValidateChecksum;
    }

    // replace errorCode with s3runtime exception
    public int getErrorCode () {
        return this.errorCode;
    }
    public int getResponseStatus () {
        return this.responseStatus;
    }
    public byte[] getErrorPayload () {
        return this.errorPayload;
    }
    public ChecksumAlgorithm getChecksumAlgorithm () {
        return this.checksumAlgorithm;
    }
    public boolean isChecksumValidated () {
        return this.didValidateChecksum;
    }
}

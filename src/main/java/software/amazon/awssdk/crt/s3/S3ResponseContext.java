package software.amazon.awssdk.crt.s3;

public class S3ResponseContext {
    public final int errorCode;
    public final int responseStatus;
    public final byte[] errorPayload;
    public final S3ChecksumAlgorithm checksumAlgorithm;
    public final boolean didValidateChecksum;

    public S3ResponseContext(final int errorCode, final int responseStatus, final byte[] errorPayload, final S3ChecksumAlgorithm checksumAlgorithm, final boolean didValidateChecksum) {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.errorPayload = errorPayload;
        this.checksumAlgorithm = checksumAlgorithm;
        this.didValidateChecksum = didValidateChecksum;
    }
}

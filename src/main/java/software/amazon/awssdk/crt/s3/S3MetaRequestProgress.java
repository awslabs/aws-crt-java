package software.amazon.awssdk.crt.s3;

/**
 * Information about the meta request progress.
 */
public class S3MetaRequestProgress {

    private long bytesTransferred;
    private long contentLength;

    /**
     * @param bytesTransferred bytes transferred since the previous progress update
     * @return this progress object
     */
    public S3MetaRequestProgress withBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
        return this;
    }

    /**
     * @return bytes transferred since the previous progress update
     */
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * @param contentLength length of the entire meta request operation
     * @return this progress object
     */
    public S3MetaRequestProgress withContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    /**
     *
     * @return length of the entire meta request operation
     */
    public long getContentLength() {
        return contentLength;
    }
}

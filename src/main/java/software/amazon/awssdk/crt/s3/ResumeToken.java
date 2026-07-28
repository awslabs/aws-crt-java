/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

public class ResumeToken {

    static public class PutResumeTokenBuilder {
        private long partSize;
        private long totalNumParts;
        private long numPartsCompleted;
        private String uploadId;

        /**
         * Default constructor
         */
        public PutResumeTokenBuilder() {}

        /**
         * @param partSize part size used for operation
         * @return this resume token object
         */
        public PutResumeTokenBuilder withPartSize(long partSize) {
            this.partSize = partSize;
            return this;
        }

        /**
         * @param totalNumParts total num parts in operation
         * @return this resume token object
         */
        public PutResumeTokenBuilder withTotalNumParts(long totalNumParts) {
            this.totalNumParts = totalNumParts;
            return this;
        }

        /**
         * @param numPartsCompleted number of parts completed
         * @return this resume token object
         */
        public PutResumeTokenBuilder withNumPartsCompleted(long numPartsCompleted) {
            this.numPartsCompleted = numPartsCompleted;
            return this;
        }

        /**
         * @param uploadId upload Id
         * @return this resume token object
         */
        public PutResumeTokenBuilder withUploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public ResumeToken build() {
            return new ResumeToken(this);
        }
    };

    private int nativeType;
    private long partSize;
    private long totalNumParts;
    private long numPartsCompleted;
    private String uploadId;

    /* Download specific fields, populated by native code. */
    private String etag;
    private String versionId;
    private String s3ObjectLastModified;
    private long objectSize;
    private long objectRangeStart;
    private long objectRangeEnd;
    private long continuesDownloadedBytes;
    private long totalDownloadedBytes;
    private long fileLastModifiedEpochNs;

    public ResumeToken(PutResumeTokenBuilder builder) {
        this.nativeType = S3MetaRequestOptions.MetaRequestType.PUT_OBJECT.getNativeValue();
        this.partSize = builder.partSize;
        this.totalNumParts = builder.totalNumParts;
        this.numPartsCompleted = builder.numPartsCompleted;
        this.uploadId = builder.uploadId;
    }
    /**
     * Default constructor
     */
    private ResumeToken() {}

    /******
     * Common Fields.
     ******/

    /**
     * @return type of resume token
     */
    public S3MetaRequestOptions.MetaRequestType getType() {
        return S3MetaRequestOptions.MetaRequestType.getEnumValueFromInteger(nativeType);
    }


    /**
     * @return part size
     */
    public long getPartSize() {
        return partSize;
    }

    /**
     * @return total number of parts
     */
    public long getTotalNumParts() {
        return totalNumParts;
    }

    /**
     * @return number of parts completed
     */
    public long getNumPartsCompleted() {
        return numPartsCompleted;
    }

    /******
     * Upload Specific fields.
     ******/
    /**
     * @return upload Id
     */
    public String getUploadId() {
        if (getType() != S3MetaRequestOptions.MetaRequestType.PUT_OBJECT) {
            throw new IllegalArgumentException("ResumeToken - upload id is only defined for Put Object Resume tokens");
        }

        return uploadId;
    }

    /******
     * Download Specific fields.
     ******/

    private void validateDownloadToken(String field) {
        if (getType() != S3MetaRequestOptions.MetaRequestType.GET_OBJECT) {
            throw new IllegalArgumentException(
                    "ResumeToken - " + field + " is only defined for Get Object Resume tokens");
        }
    }

    /**
     * ETag of the S3 object being downloaded, captured from the first response.
     * May be null/empty if the download was paused before the first response arrived.
     *
     * @return etag of the object
     */
    public String getEtag() {
        validateDownloadToken("etag");
        return etag;
    }

    /**
     * Version ID of the S3 object being downloaded.
     * Optional: null/empty when the bucket is unversioned or the version id was not captured.
     *
     * @return version id of the object
     */
    public String getVersionId() {
        validateDownloadToken("version id");
        return versionId;
    }

    /**
     * Last-Modified of the S3 object being downloaded, in HTTP-date format
     * (RFC 9110 5.6.7, "Wed, 09 Oct 2024 22:28:00 GMT"), captured from the first
     * response. The exact string in the response header.
     * Optional: null/empty if the value was not captured before the pause.
     *
     * @return Last-Modified of the object as an HTTP-date string
     */
    public String getS3ObjectLastModified() {
        validateDownloadToken("s3 object last modified");
        return s3ObjectLastModified;
    }

    /**
     * Total size of the S3 object being downloaded, regardless of any Range header
     * on the request (for a ranged download this is larger than the range being
     * fetched). 0 if the download was paused before the object size was discovered.
     *
     * @return total object size in bytes
     */
    public long getObjectSize() {
        validateDownloadToken("object size");
        return objectSize;
    }

    /**
     * Absolute byte offset in the object where the download's range starts.
     * 0 for a download without a Range header.
     *
     * @return range start offset in bytes
     */
    public long getObjectRangeStart() {
        validateDownloadToken("object range start");
        return objectRangeStart;
    }

    /**
     * Absolute byte offset in the object where the download's range ends (inclusive).
     * For a download without a Range header this is object size - 1.
     * 0 if the download was paused before the object size was discovered.
     *
     * @return range end offset in bytes (inclusive)
     */
    public long getObjectRangeEnd() {
        validateDownloadToken("object range end");
        return objectRangeEnd;
    }

    /**
     * Number of bytes downloaded contiguously from the start of the range, with no
     * gaps. Everything before this offset (relative to the object range start) has
     * been downloaded.
     *
     * @return contiguously downloaded bytes
     */
    public long getContinuesDownloadedBytes() {
        validateDownloadToken("continues downloaded bytes");
        return continuesDownloadedBytes;
    }

    /**
     * Total number of bytes downloaded before the pause. May be greater than
     * {@link #getContinuesDownloadedBytes()} when parts completed out of order,
     * leaving gaps. Equals it when delivery was strictly in order.
     *
     * @return total downloaded bytes
     */
    public long getTotalDownloadedBytes() {
        validateDownloadToken("total downloaded bytes");
        return totalDownloadedBytes;
    }

    /**
     * Last-modified time of the local receive file (nanoseconds since the Unix
     * epoch), captured after the file handle was closed during the pause.
     * Only set when the download was writing to a file; 0 for downloads that
     * deliver via body callback or when the timestamp could not be queried.
     *
     * @return local receive file last-modified time in epoch nanoseconds
     */
    public long getFileLastModifiedEpochNs() {
        validateDownloadToken("file last modified epoch ns");
        return fileLastModifiedEpochNs;
    }
}

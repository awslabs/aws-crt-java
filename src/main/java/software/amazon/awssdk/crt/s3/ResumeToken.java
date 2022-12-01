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

        ResumeToken build() {
            return new ResumeToken(this);
        } 
    };

    private int nativeType;
    private long partSize;
    private long totalNumParts;
    private long numPartsCompleted;
    private String uploadId;

    public ResumeToken(PutResumeTokenBuilder builder) {
        this.nativeType = S3MetaRequestOptions.MetaRequestType.PUT_OBJECT.getNativeValue();
        this.partSize = builder.partSize;
        this.totalNumParts = builder.totalNumParts;
        this.numPartsCompleted = builder.numPartsCompleted;
        this.uploadId = builder.uploadId;
    }

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
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListPartsOutput {
    /**
     * <p>If the bucket has a lifecycle rule configured with an action to abort incomplete
     *          multipart uploads and the prefix in the lifecycle rule matches the object name in the
     *          request, then the response includes this header indicating when the initiated multipart
     *          upload will become eligible for abort operation. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html#mpu-abort-incomplete-mpu-lifecycle-config">Aborting
     *             Incomplete Multipart Uploads Using a Bucket Lifecycle Policy</a>.</p>
     *
     *          <p>The response will also include the <code>x-amz-abort-rule-id</code> header that will
     *          provide the ID of the lifecycle configuration rule that defines this action.</p>
     */
    Instant abortDate;

    /**
     * <p>This header is returned along with the <code>x-amz-abort-date</code> header. It
     *          identifies applicable lifecycle configuration rule that defines the action to abort
     *          incomplete multipart uploads.</p>
     */
    String abortRuleId;

    /**
     * <p>The name of the bucket to which the multipart upload was initiated.</p>
     */
    String bucket;

    /**
     * <p>Object key for which the multipart upload was initiated.</p>
     */
    String key;

    /**
     * <p>Upload ID identifying the multipart upload whose parts are being listed.</p>
     */
    String uploadId;

    /**
     * <p>When a list is truncated, this element specifies the last part in the list, as well as
     *          the value to use for the part-number-marker request parameter in a subsequent
     *          request.</p>
     */
    String partNumberMarker;

    /**
     * <p>When a list is truncated, this element specifies the last part in the list, as well as
     *          the value to use for the part-number-marker request parameter in a subsequent
     *          request.</p>
     */
    String nextPartNumberMarker;

    /**
     * <p>Maximum number of parts that were allowed in the response.</p>
     */
    Integer maxParts;

    /**
     * <p> Indicates whether the returned list of parts is truncated. A true value indicates that
     *          the list was truncated. A list can be truncated if the number of parts exceeds the limit
     *          returned in the MaxParts element.</p>
     */
    Boolean isTruncated;

    /**
     * <p> Container for elements related to a particular part. A response can contain zero or
     *          more <code>Part</code> elements.</p>
     */
    List<Part> parts;

    /**
     * <p>Container element that identifies who initiated the multipart upload. If the initiator
     *          is an AWS account, this element provides the same information as the <code>Owner</code>
     *          element. If the initiator is an IAM User, this element provides the user ARN and display
     *          name.</p>
     */
    Initiator initiator;

    /**
     * <p> Container element that identifies the object owner, after the object is created. If
     *          multipart upload is initiated by an IAM user, this element provides the parent account ID
     *          and display name.</p>
     */
    Owner owner;

    /**
     * <p>Class of storage (STANDARD or REDUCED_REDUNDANCY) used to store the uploaded
     *          object.</p>
     */
    StorageClass storageClass;

    RequestCharged requestCharged;

    ListPartsOutput() {
        this.abortDate = null;
        this.abortRuleId = "";
        this.bucket = "";
        this.key = "";
        this.uploadId = "";
        this.partNumberMarker = "";
        this.nextPartNumberMarker = "";
        this.maxParts = null;
        this.isTruncated = null;
        this.parts = null;
        this.initiator = null;
        this.owner = null;
        this.storageClass = null;
        this.requestCharged = null;
    }

    protected ListPartsOutput(BuilderImpl builder) {
        this.abortDate = builder.abortDate;
        this.abortRuleId = builder.abortRuleId;
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.uploadId = builder.uploadId;
        this.partNumberMarker = builder.partNumberMarker;
        this.nextPartNumberMarker = builder.nextPartNumberMarker;
        this.maxParts = builder.maxParts;
        this.isTruncated = builder.isTruncated;
        this.parts = builder.parts;
        this.initiator = builder.initiator;
        this.owner = builder.owner;
        this.storageClass = builder.storageClass;
        this.requestCharged = builder.requestCharged;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListPartsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListPartsOutput);
    }

    public Instant abortDate() {
        return abortDate;
    }

    public String abortRuleId() {
        return abortRuleId;
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public String uploadId() {
        return uploadId;
    }

    public String partNumberMarker() {
        return partNumberMarker;
    }

    public String nextPartNumberMarker() {
        return nextPartNumberMarker;
    }

    public Integer maxParts() {
        return maxParts;
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public List<Part> parts() {
        return parts;
    }

    public Initiator initiator() {
        return initiator;
    }

    public Owner owner() {
        return owner;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setAbortDate(final Instant abortDate) {
        this.abortDate = abortDate;
    }

    public void setAbortRuleId(final String abortRuleId) {
        this.abortRuleId = abortRuleId;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public void setPartNumberMarker(final String partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }

    public void setNextPartNumberMarker(final String nextPartNumberMarker) {
        this.nextPartNumberMarker = nextPartNumberMarker;
    }

    public void setMaxParts(final Integer maxParts) {
        this.maxParts = maxParts;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public void setParts(final List<Part> parts) {
        this.parts = parts;
    }

    public void setInitiator(final Initiator initiator) {
        this.initiator = initiator;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public interface Builder {
        Builder abortDate(Instant abortDate);

        Builder abortRuleId(String abortRuleId);

        Builder bucket(String bucket);

        Builder key(String key);

        Builder uploadId(String uploadId);

        Builder partNumberMarker(String partNumberMarker);

        Builder nextPartNumberMarker(String nextPartNumberMarker);

        Builder maxParts(Integer maxParts);

        Builder isTruncated(Boolean isTruncated);

        Builder parts(List<Part> parts);

        Builder initiator(Initiator initiator);

        Builder owner(Owner owner);

        Builder storageClass(StorageClass storageClass);

        Builder requestCharged(RequestCharged requestCharged);

        ListPartsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>If the bucket has a lifecycle rule configured with an action to abort incomplete
         *          multipart uploads and the prefix in the lifecycle rule matches the object name in the
         *          request, then the response includes this header indicating when the initiated multipart
         *          upload will become eligible for abort operation. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html#mpu-abort-incomplete-mpu-lifecycle-config">Aborting
         *             Incomplete Multipart Uploads Using a Bucket Lifecycle Policy</a>.</p>
         *
         *          <p>The response will also include the <code>x-amz-abort-rule-id</code> header that will
         *          provide the ID of the lifecycle configuration rule that defines this action.</p>
         */
        Instant abortDate;

        /**
         * <p>This header is returned along with the <code>x-amz-abort-date</code> header. It
         *          identifies applicable lifecycle configuration rule that defines the action to abort
         *          incomplete multipart uploads.</p>
         */
        String abortRuleId;

        /**
         * <p>The name of the bucket to which the multipart upload was initiated.</p>
         */
        String bucket;

        /**
         * <p>Object key for which the multipart upload was initiated.</p>
         */
        String key;

        /**
         * <p>Upload ID identifying the multipart upload whose parts are being listed.</p>
         */
        String uploadId;

        /**
         * <p>When a list is truncated, this element specifies the last part in the list, as well as
         *          the value to use for the part-number-marker request parameter in a subsequent
         *          request.</p>
         */
        String partNumberMarker;

        /**
         * <p>When a list is truncated, this element specifies the last part in the list, as well as
         *          the value to use for the part-number-marker request parameter in a subsequent
         *          request.</p>
         */
        String nextPartNumberMarker;

        /**
         * <p>Maximum number of parts that were allowed in the response.</p>
         */
        Integer maxParts;

        /**
         * <p> Indicates whether the returned list of parts is truncated. A true value indicates that
         *          the list was truncated. A list can be truncated if the number of parts exceeds the limit
         *          returned in the MaxParts element.</p>
         */
        Boolean isTruncated;

        /**
         * <p> Container for elements related to a particular part. A response can contain zero or
         *          more <code>Part</code> elements.</p>
         */
        List<Part> parts;

        /**
         * <p>Container element that identifies who initiated the multipart upload. If the initiator
         *          is an AWS account, this element provides the same information as the <code>Owner</code>
         *          element. If the initiator is an IAM User, this element provides the user ARN and display
         *          name.</p>
         */
        Initiator initiator;

        /**
         * <p> Container element that identifies the object owner, after the object is created. If
         *          multipart upload is initiated by an IAM user, this element provides the parent account ID
         *          and display name.</p>
         */
        Owner owner;

        /**
         * <p>Class of storage (STANDARD or REDUCED_REDUNDANCY) used to store the uploaded
         *          object.</p>
         */
        StorageClass storageClass;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListPartsOutput model) {
            abortDate(model.abortDate);
            abortRuleId(model.abortRuleId);
            bucket(model.bucket);
            key(model.key);
            uploadId(model.uploadId);
            partNumberMarker(model.partNumberMarker);
            nextPartNumberMarker(model.nextPartNumberMarker);
            maxParts(model.maxParts);
            isTruncated(model.isTruncated);
            parts(model.parts);
            initiator(model.initiator);
            owner(model.owner);
            storageClass(model.storageClass);
            requestCharged(model.requestCharged);
        }

        public ListPartsOutput build() {
            return new ListPartsOutput(this);
        }

        public final Builder abortDate(Instant abortDate) {
            this.abortDate = abortDate;
            return this;
        }

        public final Builder abortRuleId(String abortRuleId) {
            this.abortRuleId = abortRuleId;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public final Builder partNumberMarker(String partNumberMarker) {
            this.partNumberMarker = partNumberMarker;
            return this;
        }

        public final Builder nextPartNumberMarker(String nextPartNumberMarker) {
            this.nextPartNumberMarker = nextPartNumberMarker;
            return this;
        }

        public final Builder maxParts(Integer maxParts) {
            this.maxParts = maxParts;
            return this;
        }

        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        public final Builder parts(List<Part> parts) {
            this.parts = parts;
            return this;
        }

        public final Builder initiator(Initiator initiator) {
            this.initiator = initiator;
            return this;
        }

        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(BuilderImpl.class);
        }

        @Override
        public boolean equals(Object rhs) {
            if (rhs == null) return false;
            return (rhs instanceof BuilderImpl);
        }

        public Instant abortDate() {
            return abortDate;
        }

        public String abortRuleId() {
            return abortRuleId;
        }

        public String bucket() {
            return bucket;
        }

        public String key() {
            return key;
        }

        public String uploadId() {
            return uploadId;
        }

        public String partNumberMarker() {
            return partNumberMarker;
        }

        public String nextPartNumberMarker() {
            return nextPartNumberMarker;
        }

        public Integer maxParts() {
            return maxParts;
        }

        public Boolean isTruncated() {
            return isTruncated;
        }

        public List<Part> parts() {
            return parts;
        }

        public Initiator initiator() {
            return initiator;
        }

        public Owner owner() {
            return owner;
        }

        public StorageClass storageClass() {
            return storageClass;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }

        public void setAbortDate(final Instant abortDate) {
            this.abortDate = abortDate;
        }

        public void setAbortRuleId(final String abortRuleId) {
            this.abortRuleId = abortRuleId;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setUploadId(final String uploadId) {
            this.uploadId = uploadId;
        }

        public void setPartNumberMarker(final String partNumberMarker) {
            this.partNumberMarker = partNumberMarker;
        }

        public void setNextPartNumberMarker(final String nextPartNumberMarker) {
            this.nextPartNumberMarker = nextPartNumberMarker;
        }

        public void setMaxParts(final Integer maxParts) {
            this.maxParts = maxParts;
        }

        public void setIsTruncated(final Boolean isTruncated) {
            this.isTruncated = isTruncated;
        }

        public void setParts(final List<Part> parts) {
            this.parts = parts;
        }

        public void setInitiator(final Initiator initiator) {
            this.initiator = initiator;
        }

        public void setOwner(final Owner owner) {
            this.owner = owner;
        }

        public void setStorageClass(final StorageClass storageClass) {
            this.storageClass = storageClass;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }
    }
}

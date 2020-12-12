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
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListPartsOutput {
    private Instant abortDate;

    private String abortRuleId;

    private String bucket;

    private String key;

    private String uploadId;

    private String partNumberMarker;

    private String nextPartNumberMarker;

    private Integer maxParts;

    private Boolean isTruncated;

    private List<Part> parts;

    private Initiator initiator;

    private Owner owner;

    private StorageClass storageClass;

    private RequestCharged requestCharged;

    private ListPartsOutput() {
        this.abortDate = null;
        this.abortRuleId = null;
        this.bucket = null;
        this.key = null;
        this.uploadId = null;
        this.partNumberMarker = null;
        this.nextPartNumberMarker = null;
        this.maxParts = null;
        this.isTruncated = null;
        this.parts = null;
        this.initiator = null;
        this.owner = null;
        this.storageClass = null;
        this.requestCharged = null;
    }

    private ListPartsOutput(Builder builder) {
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

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setAbortDate(final Instant abortDate) {
        this.abortDate = abortDate;
    }

    public String abortRuleId() {
        return abortRuleId;
    }

    public void setAbortRuleId(final String abortRuleId) {
        this.abortRuleId = abortRuleId;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String uploadId() {
        return uploadId;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public String partNumberMarker() {
        return partNumberMarker;
    }

    public void setPartNumberMarker(final String partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }

    public String nextPartNumberMarker() {
        return nextPartNumberMarker;
    }

    public void setNextPartNumberMarker(final String nextPartNumberMarker) {
        this.nextPartNumberMarker = nextPartNumberMarker;
    }

    public Integer maxParts() {
        return maxParts;
    }

    public void setMaxParts(final Integer maxParts) {
        this.maxParts = maxParts;
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public List<Part> parts() {
        return parts;
    }

    public void setParts(final List<Part> parts) {
        this.parts = parts;
    }

    public Initiator initiator() {
        return initiator;
    }

    public void setInitiator(final Initiator initiator) {
        this.initiator = initiator;
    }

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    static final class Builder {
        private Instant abortDate;

        private String abortRuleId;

        private String bucket;

        private String key;

        private String uploadId;

        private String partNumberMarker;

        private String nextPartNumberMarker;

        private Integer maxParts;

        private Boolean isTruncated;

        private List<Part> parts;

        private Initiator initiator;

        private Owner owner;

        private StorageClass storageClass;

        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(ListPartsOutput model) {
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
            return new com.amazonaws.s3.model.ListPartsOutput(this);
        }

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
        public final Builder abortDate(Instant abortDate) {
            this.abortDate = abortDate;
            return this;
        }

        /**
         * <p>This header is returned along with the <code>x-amz-abort-date</code> header. It
         *          identifies applicable lifecycle configuration rule that defines the action to abort
         *          incomplete multipart uploads.</p>
         */
        public final Builder abortRuleId(String abortRuleId) {
            this.abortRuleId = abortRuleId;
            return this;
        }

        /**
         * <p>The name of the bucket to which the multipart upload was initiated.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Object key for which the multipart upload was initiated.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Upload ID identifying the multipart upload whose parts are being listed.</p>
         */
        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        /**
         * <p>When a list is truncated, this element specifies the last part in the list, as well as
         *          the value to use for the part-number-marker request parameter in a subsequent
         *          request.</p>
         */
        public final Builder partNumberMarker(String partNumberMarker) {
            this.partNumberMarker = partNumberMarker;
            return this;
        }

        /**
         * <p>When a list is truncated, this element specifies the last part in the list, as well as
         *          the value to use for the part-number-marker request parameter in a subsequent
         *          request.</p>
         */
        public final Builder nextPartNumberMarker(String nextPartNumberMarker) {
            this.nextPartNumberMarker = nextPartNumberMarker;
            return this;
        }

        /**
         * <p>Maximum number of parts that were allowed in the response.</p>
         */
        public final Builder maxParts(Integer maxParts) {
            this.maxParts = maxParts;
            return this;
        }

        /**
         * <p> Indicates whether the returned list of parts is truncated. A true value indicates that
         *          the list was truncated. A list can be truncated if the number of parts exceeds the limit
         *          returned in the MaxParts element.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p> Container for elements related to a particular part. A response can contain zero or
         *          more <code>Part</code> elements.</p>
         */
        public final Builder parts(List<Part> parts) {
            this.parts = parts;
            return this;
        }

        /**
         * <p>Container element that identifies who initiated the multipart upload. If the initiator
         *          is an AWS account, this element provides the same information as the <code>Owner</code>
         *          element. If the initiator is an IAM User, this element provides the user ARN and display
         *          name.</p>
         */
        public final Builder initiator(Initiator initiator) {
            this.initiator = initiator;
            return this;
        }

        /**
         * <p> Container element that identifies the object owner, after the object is created. If
         *          multipart upload is initiated by an IAM user, this element provides the parent account ID
         *          and display name.</p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        /**
         * <p>Class of storage (STANDARD or REDUCED_REDUNDANCY) used to store the uploaded
         *          object.</p>
         */
        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }
    }
}

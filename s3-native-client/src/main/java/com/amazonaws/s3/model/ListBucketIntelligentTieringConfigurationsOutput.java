// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketIntelligentTieringConfigurationsOutput {
    private Boolean isTruncated;

    private String continuationToken;

    private String nextContinuationToken;

    private List<IntelligentTieringConfiguration> intelligentTieringConfigurationList;

    private ListBucketIntelligentTieringConfigurationsOutput() {
        this.isTruncated = null;
        this.continuationToken = null;
        this.nextContinuationToken = null;
        this.intelligentTieringConfigurationList = null;
    }

    private ListBucketIntelligentTieringConfigurationsOutput(Builder builder) {
        this.isTruncated = builder.isTruncated;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.intelligentTieringConfigurationList = builder.intelligentTieringConfigurationList;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketIntelligentTieringConfigurationsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketIntelligentTieringConfigurationsOutput);
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public String continuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public String nextContinuationToken() {
        return nextContinuationToken;
    }

    public void setNextContinuationToken(final String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }

    public List<IntelligentTieringConfiguration> intelligentTieringConfigurationList() {
        return intelligentTieringConfigurationList;
    }

    public void setIntelligentTieringConfigurationList(
            final List<IntelligentTieringConfiguration> intelligentTieringConfigurationList) {
        this.intelligentTieringConfigurationList = intelligentTieringConfigurationList;
    }

    static final class Builder {
        private Boolean isTruncated;

        private String continuationToken;

        private String nextContinuationToken;

        private List<IntelligentTieringConfiguration> intelligentTieringConfigurationList;

        private Builder() {
        }

        private Builder(ListBucketIntelligentTieringConfigurationsOutput model) {
            isTruncated(model.isTruncated);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            intelligentTieringConfigurationList(model.intelligentTieringConfigurationList);
        }

        public ListBucketIntelligentTieringConfigurationsOutput build() {
            return new com.amazonaws.s3.model.ListBucketIntelligentTieringConfigurationsOutput(this);
        }

        /**
         * <p>Indicates whether the returned list of analytics configurations is complete. A value of
         *          true indicates that the list is not complete and the NextContinuationToken will be provided
         *          for a subsequent request.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        /**
         * <p>The ContinuationToken that represents a placeholder from where this request should
         *          begin.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        /**
         * <p>The marker used to continue this inventory configuration listing. Use the
         *             <code>NextContinuationToken</code> from this response to continue the listing in a
         *          subsequent request. The continuation token is an opaque value that Amazon S3 understands.</p>
         */
        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
            return this;
        }

        /**
         * <p>The list of S3 Intelligent-Tiering configurations for a bucket.</p>
         */
        public final Builder intelligentTieringConfigurationList(
                List<IntelligentTieringConfiguration> intelligentTieringConfigurationList) {
            this.intelligentTieringConfigurationList = intelligentTieringConfigurationList;
            return this;
        }
    }
}

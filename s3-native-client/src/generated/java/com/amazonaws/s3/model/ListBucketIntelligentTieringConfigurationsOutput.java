// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketIntelligentTieringConfigurationsOutput {
    /**
     * <p>Indicates whether the returned list of analytics configurations is complete. A value of
     *          true indicates that the list is not complete and the NextContinuationToken will be provided
     *          for a subsequent request.</p>
     */
    Boolean isTruncated;

    /**
     * <p>The ContinuationToken that represents a placeholder from where this request should
     *          begin.</p>
     */
    String continuationToken;

    /**
     * <p>The marker used to continue this inventory configuration listing. Use the
     *             <code>NextContinuationToken</code> from this response to continue the listing in a
     *          subsequent request. The continuation token is an opaque value that Amazon S3 understands.</p>
     */
    String nextContinuationToken;

    /**
     * <p>The list of S3 Intelligent-Tiering configurations for a bucket.</p>
     */
    List<IntelligentTieringConfiguration> intelligentTieringConfigurationList;

    ListBucketIntelligentTieringConfigurationsOutput() {
        this.isTruncated = null;
        this.continuationToken = "";
        this.nextContinuationToken = "";
        this.intelligentTieringConfigurationList = null;
    }

    protected ListBucketIntelligentTieringConfigurationsOutput(BuilderImpl builder) {
        this.isTruncated = builder.isTruncated;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.intelligentTieringConfigurationList = builder.intelligentTieringConfigurationList;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String continuationToken() {
        return continuationToken;
    }

    public String nextContinuationToken() {
        return nextContinuationToken;
    }

    public List<IntelligentTieringConfiguration> intelligentTieringConfigurationList() {
        return intelligentTieringConfigurationList;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public void setNextContinuationToken(final String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }

    public void setIntelligentTieringConfigurationList(
            final List<IntelligentTieringConfiguration> intelligentTieringConfigurationList) {
        this.intelligentTieringConfigurationList = intelligentTieringConfigurationList;
    }

    public interface Builder {
        Builder isTruncated(Boolean isTruncated);

        Builder continuationToken(String continuationToken);

        Builder nextContinuationToken(String nextContinuationToken);

        Builder intelligentTieringConfigurationList(
                List<IntelligentTieringConfiguration> intelligentTieringConfigurationList);

        ListBucketIntelligentTieringConfigurationsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether the returned list of analytics configurations is complete. A value of
         *          true indicates that the list is not complete and the NextContinuationToken will be provided
         *          for a subsequent request.</p>
         */
        Boolean isTruncated;

        /**
         * <p>The ContinuationToken that represents a placeholder from where this request should
         *          begin.</p>
         */
        String continuationToken;

        /**
         * <p>The marker used to continue this inventory configuration listing. Use the
         *             <code>NextContinuationToken</code> from this response to continue the listing in a
         *          subsequent request. The continuation token is an opaque value that Amazon S3 understands.</p>
         */
        String nextContinuationToken;

        /**
         * <p>The list of S3 Intelligent-Tiering configurations for a bucket.</p>
         */
        List<IntelligentTieringConfiguration> intelligentTieringConfigurationList;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketIntelligentTieringConfigurationsOutput model) {
            isTruncated(model.isTruncated);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            intelligentTieringConfigurationList(model.intelligentTieringConfigurationList);
        }

        public ListBucketIntelligentTieringConfigurationsOutput build() {
            return new ListBucketIntelligentTieringConfigurationsOutput(this);
        }

        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
            return this;
        }

        public final Builder intelligentTieringConfigurationList(
                List<IntelligentTieringConfiguration> intelligentTieringConfigurationList) {
            this.intelligentTieringConfigurationList = intelligentTieringConfigurationList;
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

        public Boolean isTruncated() {
            return isTruncated;
        }

        public String continuationToken() {
            return continuationToken;
        }

        public String nextContinuationToken() {
            return nextContinuationToken;
        }

        public List<IntelligentTieringConfiguration> intelligentTieringConfigurationList() {
            return intelligentTieringConfigurationList;
        }

        public void setIsTruncated(final Boolean isTruncated) {
            this.isTruncated = isTruncated;
        }

        public void setContinuationToken(final String continuationToken) {
            this.continuationToken = continuationToken;
        }

        public void setNextContinuationToken(final String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
        }

        public void setIntelligentTieringConfigurationList(
                final List<IntelligentTieringConfiguration> intelligentTieringConfigurationList) {
            this.intelligentTieringConfigurationList = intelligentTieringConfigurationList;
        }
    }
}

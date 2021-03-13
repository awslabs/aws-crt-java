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
public class ListBucketAnalyticsConfigurationsOutput {
    /**
     * <p>Indicates whether the returned list of analytics configurations is complete. A value of
     *          true indicates that the list is not complete and the NextContinuationToken will be provided
     *          for a subsequent request.</p>
     */
    Boolean isTruncated;

    /**
     * <p>The marker that is used as a starting point for this analytics configuration list
     *          response. This value is present if it was sent in the request.</p>
     */
    String continuationToken;

    /**
     * <p>
     *             <code>NextContinuationToken</code> is sent when <code>isTruncated</code> is true, which
     *          indicates that there are more analytics configurations to list. The next request must
     *          include this <code>NextContinuationToken</code>. The token is obfuscated and is not a
     *          usable value.</p>
     */
    String nextContinuationToken;

    /**
     * <p>The list of analytics configurations for a bucket.</p>
     */
    List<AnalyticsConfiguration> analyticsConfigurationList;

    ListBucketAnalyticsConfigurationsOutput() {
        this.isTruncated = null;
        this.continuationToken = "";
        this.nextContinuationToken = "";
        this.analyticsConfigurationList = null;
    }

    protected ListBucketAnalyticsConfigurationsOutput(BuilderImpl builder) {
        this.isTruncated = builder.isTruncated;
        this.continuationToken = builder.continuationToken;
        this.nextContinuationToken = builder.nextContinuationToken;
        this.analyticsConfigurationList = builder.analyticsConfigurationList;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketAnalyticsConfigurationsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketAnalyticsConfigurationsOutput);
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

    public List<AnalyticsConfiguration> analyticsConfigurationList() {
        return analyticsConfigurationList;
    }

    public interface Builder {
        Builder isTruncated(Boolean isTruncated);

        Builder continuationToken(String continuationToken);

        Builder nextContinuationToken(String nextContinuationToken);

        Builder analyticsConfigurationList(List<AnalyticsConfiguration> analyticsConfigurationList);

        ListBucketAnalyticsConfigurationsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether the returned list of analytics configurations is complete. A value of
         *          true indicates that the list is not complete and the NextContinuationToken will be provided
         *          for a subsequent request.</p>
         */
        Boolean isTruncated;

        /**
         * <p>The marker that is used as a starting point for this analytics configuration list
         *          response. This value is present if it was sent in the request.</p>
         */
        String continuationToken;

        /**
         * <p>
         *             <code>NextContinuationToken</code> is sent when <code>isTruncated</code> is true, which
         *          indicates that there are more analytics configurations to list. The next request must
         *          include this <code>NextContinuationToken</code>. The token is obfuscated and is not a
         *          usable value.</p>
         */
        String nextContinuationToken;

        /**
         * <p>The list of analytics configurations for a bucket.</p>
         */
        List<AnalyticsConfiguration> analyticsConfigurationList;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketAnalyticsConfigurationsOutput model) {
            isTruncated(model.isTruncated);
            continuationToken(model.continuationToken);
            nextContinuationToken(model.nextContinuationToken);
            analyticsConfigurationList(model.analyticsConfigurationList);
        }

        public ListBucketAnalyticsConfigurationsOutput build() {
            return new ListBucketAnalyticsConfigurationsOutput(this);
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

        public final Builder analyticsConfigurationList(
                List<AnalyticsConfiguration> analyticsConfigurationList) {
            this.analyticsConfigurationList = analyticsConfigurationList;
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

        public List<AnalyticsConfiguration> analyticsConfigurationList() {
            return analyticsConfigurationList;
        }
    }
}

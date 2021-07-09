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
public class ListBucketInventoryConfigurationsOutput {
    /**
     * <p>If sent in the request, the marker that is used as a starting point for this inventory
     *          configuration list response.</p>
     */
    String continuationToken;

    /**
     * <p>The list of inventory configurations for a bucket.</p>
     */
    List<InventoryConfiguration> inventoryConfigurationList;

    /**
     * <p>Tells whether the returned list of inventory configurations is complete. A value of true
     *          indicates that the list is not complete and the NextContinuationToken is provided for a
     *          subsequent request.</p>
     */
    Boolean isTruncated;

    /**
     * <p>The marker used to continue this inventory configuration listing. Use the
     *             <code>NextContinuationToken</code> from this response to continue the listing in a
     *          subsequent request. The continuation token is an opaque value that Amazon S3 understands.</p>
     */
    String nextContinuationToken;

    ListBucketInventoryConfigurationsOutput() {
        this.continuationToken = "";
        this.inventoryConfigurationList = null;
        this.isTruncated = null;
        this.nextContinuationToken = "";
    }

    protected ListBucketInventoryConfigurationsOutput(BuilderImpl builder) {
        this.continuationToken = builder.continuationToken;
        this.inventoryConfigurationList = builder.inventoryConfigurationList;
        this.isTruncated = builder.isTruncated;
        this.nextContinuationToken = builder.nextContinuationToken;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketInventoryConfigurationsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketInventoryConfigurationsOutput);
    }

    public String continuationToken() {
        return continuationToken;
    }

    public List<InventoryConfiguration> inventoryConfigurationList() {
        return inventoryConfigurationList;
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public String nextContinuationToken() {
        return nextContinuationToken;
    }

    public interface Builder {
        Builder continuationToken(String continuationToken);

        Builder inventoryConfigurationList(List<InventoryConfiguration> inventoryConfigurationList);

        Builder isTruncated(Boolean isTruncated);

        Builder nextContinuationToken(String nextContinuationToken);

        ListBucketInventoryConfigurationsOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>If sent in the request, the marker that is used as a starting point for this inventory
         *          configuration list response.</p>
         */
        String continuationToken;

        /**
         * <p>The list of inventory configurations for a bucket.</p>
         */
        List<InventoryConfiguration> inventoryConfigurationList;

        /**
         * <p>Tells whether the returned list of inventory configurations is complete. A value of true
         *          indicates that the list is not complete and the NextContinuationToken is provided for a
         *          subsequent request.</p>
         */
        Boolean isTruncated;

        /**
         * <p>The marker used to continue this inventory configuration listing. Use the
         *             <code>NextContinuationToken</code> from this response to continue the listing in a
         *          subsequent request. The continuation token is an opaque value that Amazon S3 understands.</p>
         */
        String nextContinuationToken;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketInventoryConfigurationsOutput model) {
            continuationToken(model.continuationToken);
            inventoryConfigurationList(model.inventoryConfigurationList);
            isTruncated(model.isTruncated);
            nextContinuationToken(model.nextContinuationToken);
        }

        public ListBucketInventoryConfigurationsOutput build() {
            return new ListBucketInventoryConfigurationsOutput(this);
        }

        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        public final Builder inventoryConfigurationList(
                List<InventoryConfiguration> inventoryConfigurationList) {
            this.inventoryConfigurationList = inventoryConfigurationList;
            return this;
        }

        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
            return this;
        }

        public final Builder nextContinuationToken(String nextContinuationToken) {
            this.nextContinuationToken = nextContinuationToken;
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

        public String continuationToken() {
            return continuationToken;
        }

        public List<InventoryConfiguration> inventoryConfigurationList() {
            return inventoryConfigurationList;
        }

        public Boolean isTruncated() {
            return isTruncated;
        }

        public String nextContinuationToken() {
            return nextContinuationToken;
        }
    }
}

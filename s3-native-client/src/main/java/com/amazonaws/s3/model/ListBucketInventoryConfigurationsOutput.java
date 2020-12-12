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
public class ListBucketInventoryConfigurationsOutput {
    private String continuationToken;

    private List<InventoryConfiguration> inventoryConfigurationList;

    private Boolean isTruncated;

    private String nextContinuationToken;

    private ListBucketInventoryConfigurationsOutput() {
        this.continuationToken = null;
        this.inventoryConfigurationList = null;
        this.isTruncated = null;
        this.nextContinuationToken = null;
    }

    private ListBucketInventoryConfigurationsOutput(Builder builder) {
        this.continuationToken = builder.continuationToken;
        this.inventoryConfigurationList = builder.inventoryConfigurationList;
        this.isTruncated = builder.isTruncated;
        this.nextContinuationToken = builder.nextContinuationToken;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public List<InventoryConfiguration> inventoryConfigurationList() {
        return inventoryConfigurationList;
    }

    public void setInventoryConfigurationList(
            final List<InventoryConfiguration> inventoryConfigurationList) {
        this.inventoryConfigurationList = inventoryConfigurationList;
    }

    public Boolean isTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(final Boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public String nextContinuationToken() {
        return nextContinuationToken;
    }

    public void setNextContinuationToken(final String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }

    static final class Builder {
        private String continuationToken;

        private List<InventoryConfiguration> inventoryConfigurationList;

        private Boolean isTruncated;

        private String nextContinuationToken;

        private Builder() {
        }

        private Builder(ListBucketInventoryConfigurationsOutput model) {
            continuationToken(model.continuationToken);
            inventoryConfigurationList(model.inventoryConfigurationList);
            isTruncated(model.isTruncated);
            nextContinuationToken(model.nextContinuationToken);
        }

        public ListBucketInventoryConfigurationsOutput build() {
            return new com.amazonaws.s3.model.ListBucketInventoryConfigurationsOutput(this);
        }

        /**
         * <p>If sent in the request, the marker that is used as a starting point for this inventory
         *          configuration list response.</p>
         */
        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
            return this;
        }

        /**
         * <p>The list of inventory configurations for a bucket.</p>
         */
        public final Builder inventoryConfigurationList(
                List<InventoryConfiguration> inventoryConfigurationList) {
            this.inventoryConfigurationList = inventoryConfigurationList;
            return this;
        }

        /**
         * <p>Tells whether the returned list of inventory configurations is complete. A value of true
         *          indicates that the list is not complete and the NextContinuationToken is provided for a
         *          subsequent request.</p>
         */
        public final Builder isTruncated(Boolean isTruncated) {
            this.isTruncated = isTruncated;
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
    }
}

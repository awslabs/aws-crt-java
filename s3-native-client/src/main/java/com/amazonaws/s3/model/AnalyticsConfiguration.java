// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsConfiguration {
    private String id;

    private AnalyticsFilter filter;

    private StorageClassAnalysis storageClassAnalysis;

    private AnalyticsConfiguration() {
        this.id = null;
        this.filter = null;
        this.storageClassAnalysis = null;
    }

    private AnalyticsConfiguration(Builder builder) {
        this.id = builder.id;
        this.filter = builder.filter;
        this.storageClassAnalysis = builder.storageClassAnalysis;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(AnalyticsConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AnalyticsConfiguration);
    }

    public String id() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public AnalyticsFilter filter() {
        return filter;
    }

    public void setFilter(final AnalyticsFilter filter) {
        this.filter = filter;
    }

    public StorageClassAnalysis storageClassAnalysis() {
        return storageClassAnalysis;
    }

    public void setStorageClassAnalysis(final StorageClassAnalysis storageClassAnalysis) {
        this.storageClassAnalysis = storageClassAnalysis;
    }

    static final class Builder {
        private String id;

        private AnalyticsFilter filter;

        private StorageClassAnalysis storageClassAnalysis;

        private Builder() {
        }

        private Builder(AnalyticsConfiguration model) {
            id(model.id);
            filter(model.filter);
            storageClassAnalysis(model.storageClassAnalysis);
        }

        public AnalyticsConfiguration build() {
            return new com.amazonaws.s3.model.AnalyticsConfiguration(this);
        }

        /**
         * <p>The ID that identifies the analytics configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>The filter used to describe a set of objects for analyses. A filter must have exactly
         *          one prefix, one tag, or one conjunction (AnalyticsAndOperator). If no filter is provided,
         *          all objects will be considered in any analysis.</p>
         */
        public final Builder filter(AnalyticsFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * <p> Contains data related to access patterns to be collected and made available to analyze
         *          the tradeoffs between different storage classes. </p>
         */
        public final Builder storageClassAnalysis(StorageClassAnalysis storageClassAnalysis) {
            this.storageClassAnalysis = storageClassAnalysis;
            return this;
        }
    }
}

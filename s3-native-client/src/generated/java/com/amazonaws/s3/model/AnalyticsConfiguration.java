// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsConfiguration {
    /**
     * <p>The ID that identifies the analytics configuration.</p>
     */
    String id;

    /**
     * <p>The filter used to describe a set of objects for analyses. A filter must have exactly
     *          one prefix, one tag, or one conjunction (AnalyticsAndOperator). If no filter is provided,
     *          all objects will be considered in any analysis.</p>
     */
    AnalyticsFilter filter;

    /**
     * <p> Contains data related to access patterns to be collected and made available to analyze
     *          the tradeoffs between different storage classes. </p>
     */
    StorageClassAnalysis storageClassAnalysis;

    AnalyticsConfiguration() {
        this.id = "";
        this.filter = null;
        this.storageClassAnalysis = null;
    }

    protected AnalyticsConfiguration(BuilderImpl builder) {
        this.id = builder.id;
        this.filter = builder.filter;
        this.storageClassAnalysis = builder.storageClassAnalysis;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public AnalyticsFilter filter() {
        return filter;
    }

    public StorageClassAnalysis storageClassAnalysis() {
        return storageClassAnalysis;
    }

    public interface Builder {
        Builder id(String id);

        Builder filter(AnalyticsFilter filter);

        Builder storageClassAnalysis(StorageClassAnalysis storageClassAnalysis);

        AnalyticsConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The ID that identifies the analytics configuration.</p>
         */
        String id;

        /**
         * <p>The filter used to describe a set of objects for analyses. A filter must have exactly
         *          one prefix, one tag, or one conjunction (AnalyticsAndOperator). If no filter is provided,
         *          all objects will be considered in any analysis.</p>
         */
        AnalyticsFilter filter;

        /**
         * <p> Contains data related to access patterns to be collected and made available to analyze
         *          the tradeoffs between different storage classes. </p>
         */
        StorageClassAnalysis storageClassAnalysis;

        protected BuilderImpl() {
        }

        private BuilderImpl(AnalyticsConfiguration model) {
            id(model.id);
            filter(model.filter);
            storageClassAnalysis(model.storageClassAnalysis);
        }

        public AnalyticsConfiguration build() {
            return new AnalyticsConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder filter(AnalyticsFilter filter) {
            this.filter = filter;
            return this;
        }

        public final Builder storageClassAnalysis(StorageClassAnalysis storageClassAnalysis) {
            this.storageClassAnalysis = storageClassAnalysis;
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

        public String id() {
            return id;
        }

        public AnalyticsFilter filter() {
            return filter;
        }

        public StorageClassAnalysis storageClassAnalysis() {
            return storageClassAnalysis;
        }
    }
}

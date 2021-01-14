// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MetricsConfiguration {
    /**
     * <p>The ID used to identify the metrics configuration.</p>
     */
    String id;

    /**
     * <p>Specifies a metrics configuration filter. The metrics configuration will only include
     *          objects that meet the filter's criteria. A filter must be a prefix, a tag, or a conjunction
     *          (MetricsAndOperator).</p>
     */
    MetricsFilter filter;

    MetricsConfiguration() {
        this.id = "";
        this.filter = null;
    }

    protected MetricsConfiguration(BuilderImpl builder) {
        this.id = builder.id;
        this.filter = builder.filter;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(MetricsConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof MetricsConfiguration);
    }

    public String id() {
        return id;
    }

    public MetricsFilter filter() {
        return filter;
    }

    public interface Builder {
        Builder id(String id);

        Builder filter(MetricsFilter filter);

        MetricsConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The ID used to identify the metrics configuration.</p>
         */
        String id;

        /**
         * <p>Specifies a metrics configuration filter. The metrics configuration will only include
         *          objects that meet the filter's criteria. A filter must be a prefix, a tag, or a conjunction
         *          (MetricsAndOperator).</p>
         */
        MetricsFilter filter;

        protected BuilderImpl() {
        }

        private BuilderImpl(MetricsConfiguration model) {
            id(model.id);
            filter(model.filter);
        }

        public MetricsConfiguration build() {
            return new MetricsConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder filter(MetricsFilter filter) {
            this.filter = filter;
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

        public MetricsFilter filter() {
            return filter;
        }
    }
}

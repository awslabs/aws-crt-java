// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MetricsConfiguration {
    private String id;

    private MetricsFilter filter;

    private MetricsConfiguration() {
        this.id = null;
        this.filter = null;
    }

    private MetricsConfiguration(Builder builder) {
        this.id = builder.id;
        this.filter = builder.filter;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setId(final String id) {
        this.id = id;
    }

    public MetricsFilter filter() {
        return filter;
    }

    public void setFilter(final MetricsFilter filter) {
        this.filter = filter;
    }

    static final class Builder {
        private String id;

        private MetricsFilter filter;

        private Builder() {
        }

        private Builder(MetricsConfiguration model) {
            id(model.id);
            filter(model.filter);
        }

        public MetricsConfiguration build() {
            return new com.amazonaws.s3.model.MetricsConfiguration(this);
        }

        /**
         * <p>The ID used to identify the metrics configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>Specifies a metrics configuration filter. The metrics configuration will only include
         *          objects that meet the filter's criteria. A filter must be a prefix, a tag, or a conjunction
         *          (MetricsAndOperator).</p>
         */
        public final Builder filter(MetricsFilter filter) {
            this.filter = filter;
            return this;
        }
    }
}

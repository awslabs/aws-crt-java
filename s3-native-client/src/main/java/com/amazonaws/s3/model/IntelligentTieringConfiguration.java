// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class IntelligentTieringConfiguration {
    private String id;

    private IntelligentTieringFilter filter;

    private IntelligentTieringStatus status;

    private List<Tiering> tierings;

    private IntelligentTieringConfiguration() {
        this.id = null;
        this.filter = null;
        this.status = null;
        this.tierings = null;
    }

    private IntelligentTieringConfiguration(Builder builder) {
        this.id = builder.id;
        this.filter = builder.filter;
        this.status = builder.status;
        this.tierings = builder.tierings;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IntelligentTieringConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof IntelligentTieringConfiguration);
    }

    public String id() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public IntelligentTieringFilter filter() {
        return filter;
    }

    public void setFilter(final IntelligentTieringFilter filter) {
        this.filter = filter;
    }

    public IntelligentTieringStatus status() {
        return status;
    }

    public void setStatus(final IntelligentTieringStatus status) {
        this.status = status;
    }

    public List<Tiering> tierings() {
        return tierings;
    }

    public void setTierings(final List<Tiering> tierings) {
        this.tierings = tierings;
    }

    static final class Builder {
        private String id;

        private IntelligentTieringFilter filter;

        private IntelligentTieringStatus status;

        private List<Tiering> tierings;

        private Builder() {
        }

        private Builder(IntelligentTieringConfiguration model) {
            id(model.id);
            filter(model.filter);
            status(model.status);
            tierings(model.tierings);
        }

        public IntelligentTieringConfiguration build() {
            return new com.amazonaws.s3.model.IntelligentTieringConfiguration(this);
        }

        /**
         * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>Specifies a bucket filter. The configuration only includes objects that meet the
         *          filter's criteria.</p>
         */
        public final Builder filter(IntelligentTieringFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * <p>Specifies the status of the configuration.</p>
         */
        public final Builder status(IntelligentTieringStatus status) {
            this.status = status;
            return this;
        }

        /**
         * <p>Specifies the S3 Intelligent-Tiering storage class tier of the configuration.</p>
         */
        public final Builder tierings(List<Tiering> tierings) {
            this.tierings = tierings;
            return this;
        }
    }
}

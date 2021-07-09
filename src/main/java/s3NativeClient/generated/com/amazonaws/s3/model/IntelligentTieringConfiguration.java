// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class IntelligentTieringConfiguration {
    /**
     * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
     */
    String id;

    /**
     * <p>Specifies a bucket filter. The configuration only includes objects that meet the
     *          filter's criteria.</p>
     */
    IntelligentTieringFilter filter;

    /**
     * <p>Specifies the status of the configuration.</p>
     */
    IntelligentTieringStatus status;

    /**
     * <p>Specifies the S3 Intelligent-Tiering storage class tier of the configuration.</p>
     */
    List<Tiering> tierings;

    IntelligentTieringConfiguration() {
        this.id = "";
        this.filter = null;
        this.status = null;
        this.tierings = null;
    }

    protected IntelligentTieringConfiguration(BuilderImpl builder) {
        this.id = builder.id;
        this.filter = builder.filter;
        this.status = builder.status;
        this.tierings = builder.tierings;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public IntelligentTieringFilter filter() {
        return filter;
    }

    public IntelligentTieringStatus status() {
        return status;
    }

    public List<Tiering> tierings() {
        return tierings;
    }

    public interface Builder {
        Builder id(String id);

        Builder filter(IntelligentTieringFilter filter);

        Builder status(IntelligentTieringStatus status);

        Builder tierings(List<Tiering> tierings);

        IntelligentTieringConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
         */
        String id;

        /**
         * <p>Specifies a bucket filter. The configuration only includes objects that meet the
         *          filter's criteria.</p>
         */
        IntelligentTieringFilter filter;

        /**
         * <p>Specifies the status of the configuration.</p>
         */
        IntelligentTieringStatus status;

        /**
         * <p>Specifies the S3 Intelligent-Tiering storage class tier of the configuration.</p>
         */
        List<Tiering> tierings;

        protected BuilderImpl() {
        }

        private BuilderImpl(IntelligentTieringConfiguration model) {
            id(model.id);
            filter(model.filter);
            status(model.status);
            tierings(model.tierings);
        }

        public IntelligentTieringConfiguration build() {
            return new IntelligentTieringConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder filter(IntelligentTieringFilter filter) {
            this.filter = filter;
            return this;
        }

        public final Builder status(IntelligentTieringStatus status) {
            this.status = status;
            return this;
        }

        public final Builder tierings(List<Tiering> tierings) {
            this.tierings = tierings;
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

        public IntelligentTieringFilter filter() {
            return filter;
        }

        public IntelligentTieringStatus status() {
            return status;
        }

        public List<Tiering> tierings() {
            return tierings;
        }
    }
}

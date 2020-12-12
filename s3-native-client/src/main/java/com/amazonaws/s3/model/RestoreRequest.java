// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RestoreRequest {
    private Integer days;

    private GlacierJobParameters glacierJobParameters;

    private RestoreRequestType type;

    private Tier tier;

    private String description;

    private SelectParameters selectParameters;

    private OutputLocation outputLocation;

    private RestoreRequest() {
        this.days = null;
        this.glacierJobParameters = null;
        this.type = null;
        this.tier = null;
        this.description = null;
        this.selectParameters = null;
        this.outputLocation = null;
    }

    private RestoreRequest(Builder builder) {
        this.days = builder.days;
        this.glacierJobParameters = builder.glacierJobParameters;
        this.type = builder.type;
        this.tier = builder.tier;
        this.description = builder.description;
        this.selectParameters = builder.selectParameters;
        this.outputLocation = builder.outputLocation;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(RestoreRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof RestoreRequest);
    }

    public Integer days() {
        return days;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public GlacierJobParameters glacierJobParameters() {
        return glacierJobParameters;
    }

    public void setGlacierJobParameters(final GlacierJobParameters glacierJobParameters) {
        this.glacierJobParameters = glacierJobParameters;
    }

    public RestoreRequestType type() {
        return type;
    }

    public void setType(final RestoreRequestType type) {
        this.type = type;
    }

    public Tier tier() {
        return tier;
    }

    public void setTier(final Tier tier) {
        this.tier = tier;
    }

    public String description() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public SelectParameters selectParameters() {
        return selectParameters;
    }

    public void setSelectParameters(final SelectParameters selectParameters) {
        this.selectParameters = selectParameters;
    }

    public OutputLocation outputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(final OutputLocation outputLocation) {
        this.outputLocation = outputLocation;
    }

    static final class Builder {
        private Integer days;

        private GlacierJobParameters glacierJobParameters;

        private RestoreRequestType type;

        private Tier tier;

        private String description;

        private SelectParameters selectParameters;

        private OutputLocation outputLocation;

        private Builder() {
        }

        private Builder(RestoreRequest model) {
            days(model.days);
            glacierJobParameters(model.glacierJobParameters);
            type(model.type);
            tier(model.tier);
            description(model.description);
            selectParameters(model.selectParameters);
            outputLocation(model.outputLocation);
        }

        public RestoreRequest build() {
            return new com.amazonaws.s3.model.RestoreRequest(this);
        }

        /**
         * <p>Lifetime of the active copy in days. Do not use with restores that specify
         *             <code>OutputLocation</code>.</p>
         *          <p>The Days element is required for regular restores, and must not be provided for select
         *          requests.</p>
         */
        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        /**
         * <p>S3 Glacier related parameters pertaining to this job. Do not use with restores that
         *          specify <code>OutputLocation</code>.</p>
         */
        public final Builder glacierJobParameters(GlacierJobParameters glacierJobParameters) {
            this.glacierJobParameters = glacierJobParameters;
            return this;
        }

        /**
         * <p>Type of restore request.</p>
         */
        public final Builder type(RestoreRequestType type) {
            this.type = type;
            return this;
        }

        /**
         * <p>Retrieval tier at which the restore will be processed.</p>
         */
        public final Builder tier(Tier tier) {
            this.tier = tier;
            return this;
        }

        /**
         * <p>The optional description for the job.</p>
         */
        public final Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * <p>Describes the parameters for Select job types.</p>
         */
        public final Builder selectParameters(SelectParameters selectParameters) {
            this.selectParameters = selectParameters;
            return this;
        }

        /**
         * <p>Describes the location where the restore job's output is stored.</p>
         */
        public final Builder outputLocation(OutputLocation outputLocation) {
            this.outputLocation = outputLocation;
            return this;
        }
    }
}

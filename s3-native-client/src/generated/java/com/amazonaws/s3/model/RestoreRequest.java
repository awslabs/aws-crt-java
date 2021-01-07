// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RestoreRequest {
    /**
     * <p>Lifetime of the active copy in days. Do not use with restores that specify
     *             <code>OutputLocation</code>.</p>
     *          <p>The Days element is required for regular restores, and must not be provided for select
     *          requests.</p>
     */
    Integer days;

    /**
     * <p>S3 Glacier related parameters pertaining to this job. Do not use with restores that
     *          specify <code>OutputLocation</code>.</p>
     */
    GlacierJobParameters glacierJobParameters;

    /**
     * <p>Type of restore request.</p>
     */
    RestoreRequestType type;

    /**
     * <p>Retrieval tier at which the restore will be processed.</p>
     */
    Tier tier;

    /**
     * <p>The optional description for the job.</p>
     */
    String description;

    /**
     * <p>Describes the parameters for Select job types.</p>
     */
    SelectParameters selectParameters;

    /**
     * <p>Describes the location where the restore job's output is stored.</p>
     */
    OutputLocation outputLocation;

    RestoreRequest() {
        this.days = null;
        this.glacierJobParameters = null;
        this.type = null;
        this.tier = null;
        this.description = "";
        this.selectParameters = null;
        this.outputLocation = null;
    }

    protected RestoreRequest(BuilderImpl builder) {
        this.days = builder.days;
        this.glacierJobParameters = builder.glacierJobParameters;
        this.type = builder.type;
        this.tier = builder.tier;
        this.description = builder.description;
        this.selectParameters = builder.selectParameters;
        this.outputLocation = builder.outputLocation;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public GlacierJobParameters glacierJobParameters() {
        return glacierJobParameters;
    }

    public RestoreRequestType type() {
        return type;
    }

    public Tier tier() {
        return tier;
    }

    public String description() {
        return description;
    }

    public SelectParameters selectParameters() {
        return selectParameters;
    }

    public OutputLocation outputLocation() {
        return outputLocation;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public void setGlacierJobParameters(final GlacierJobParameters glacierJobParameters) {
        this.glacierJobParameters = glacierJobParameters;
    }

    public void setType(final RestoreRequestType type) {
        this.type = type;
    }

    public void setTier(final Tier tier) {
        this.tier = tier;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setSelectParameters(final SelectParameters selectParameters) {
        this.selectParameters = selectParameters;
    }

    public void setOutputLocation(final OutputLocation outputLocation) {
        this.outputLocation = outputLocation;
    }

    public interface Builder {
        Builder days(Integer days);

        Builder glacierJobParameters(GlacierJobParameters glacierJobParameters);

        Builder type(RestoreRequestType type);

        Builder tier(Tier tier);

        Builder description(String description);

        Builder selectParameters(SelectParameters selectParameters);

        Builder outputLocation(OutputLocation outputLocation);

        RestoreRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Lifetime of the active copy in days. Do not use with restores that specify
         *             <code>OutputLocation</code>.</p>
         *          <p>The Days element is required for regular restores, and must not be provided for select
         *          requests.</p>
         */
        Integer days;

        /**
         * <p>S3 Glacier related parameters pertaining to this job. Do not use with restores that
         *          specify <code>OutputLocation</code>.</p>
         */
        GlacierJobParameters glacierJobParameters;

        /**
         * <p>Type of restore request.</p>
         */
        RestoreRequestType type;

        /**
         * <p>Retrieval tier at which the restore will be processed.</p>
         */
        Tier tier;

        /**
         * <p>The optional description for the job.</p>
         */
        String description;

        /**
         * <p>Describes the parameters for Select job types.</p>
         */
        SelectParameters selectParameters;

        /**
         * <p>Describes the location where the restore job's output is stored.</p>
         */
        OutputLocation outputLocation;

        protected BuilderImpl() {
        }

        private BuilderImpl(RestoreRequest model) {
            days(model.days);
            glacierJobParameters(model.glacierJobParameters);
            type(model.type);
            tier(model.tier);
            description(model.description);
            selectParameters(model.selectParameters);
            outputLocation(model.outputLocation);
        }

        public RestoreRequest build() {
            return new RestoreRequest(this);
        }

        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        public final Builder glacierJobParameters(GlacierJobParameters glacierJobParameters) {
            this.glacierJobParameters = glacierJobParameters;
            return this;
        }

        public final Builder type(RestoreRequestType type) {
            this.type = type;
            return this;
        }

        public final Builder tier(Tier tier) {
            this.tier = tier;
            return this;
        }

        public final Builder description(String description) {
            this.description = description;
            return this;
        }

        public final Builder selectParameters(SelectParameters selectParameters) {
            this.selectParameters = selectParameters;
            return this;
        }

        public final Builder outputLocation(OutputLocation outputLocation) {
            this.outputLocation = outputLocation;
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

        public Integer days() {
            return days;
        }

        public GlacierJobParameters glacierJobParameters() {
            return glacierJobParameters;
        }

        public RestoreRequestType type() {
            return type;
        }

        public Tier tier() {
            return tier;
        }

        public String description() {
            return description;
        }

        public SelectParameters selectParameters() {
            return selectParameters;
        }

        public OutputLocation outputLocation() {
            return outputLocation;
        }

        public void setDays(final Integer days) {
            this.days = days;
        }

        public void setGlacierJobParameters(final GlacierJobParameters glacierJobParameters) {
            this.glacierJobParameters = glacierJobParameters;
        }

        public void setType(final RestoreRequestType type) {
            this.type = type;
        }

        public void setTier(final Tier tier) {
            this.tier = tier;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setSelectParameters(final SelectParameters selectParameters) {
            this.selectParameters = selectParameters;
        }

        public void setOutputLocation(final OutputLocation outputLocation) {
            this.outputLocation = outputLocation;
        }
    }
}

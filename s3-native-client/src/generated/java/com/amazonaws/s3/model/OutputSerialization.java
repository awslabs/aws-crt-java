// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OutputSerialization {
    /**
     * <p>Describes the serialization of CSV-encoded Select results.</p>
     */
    CSVOutput cSV;

    /**
     * <p>Specifies JSON as request's output serialization format.</p>
     */
    JSONOutput jSON;

    OutputSerialization() {
        this.cSV = null;
        this.jSON = null;
    }

    protected OutputSerialization(BuilderImpl builder) {
        this.cSV = builder.cSV;
        this.jSON = builder.jSON;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(OutputSerialization.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof OutputSerialization);
    }

    public CSVOutput cSV() {
        return cSV;
    }

    public JSONOutput jSON() {
        return jSON;
    }

    public void setCSV(final CSVOutput cSV) {
        this.cSV = cSV;
    }

    public void setJSON(final JSONOutput jSON) {
        this.jSON = jSON;
    }

    public interface Builder {
        Builder cSV(CSVOutput cSV);

        Builder jSON(JSONOutput jSON);

        OutputSerialization build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Describes the serialization of CSV-encoded Select results.</p>
         */
        CSVOutput cSV;

        /**
         * <p>Specifies JSON as request's output serialization format.</p>
         */
        JSONOutput jSON;

        protected BuilderImpl() {
        }

        private BuilderImpl(OutputSerialization model) {
            cSV(model.cSV);
            jSON(model.jSON);
        }

        public OutputSerialization build() {
            return new OutputSerialization(this);
        }

        public final Builder cSV(CSVOutput cSV) {
            this.cSV = cSV;
            return this;
        }

        public final Builder jSON(JSONOutput jSON) {
            this.jSON = jSON;
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

        public CSVOutput cSV() {
            return cSV;
        }

        public JSONOutput jSON() {
            return jSON;
        }

        public void setCSV(final CSVOutput cSV) {
            this.cSV = cSV;
        }

        public void setJSON(final JSONOutput jSON) {
            this.jSON = jSON;
        }
    }
}

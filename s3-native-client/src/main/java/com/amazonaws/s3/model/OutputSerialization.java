// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OutputSerialization {
    private CSVOutput cSV;

    private JSONOutput jSON;

    private OutputSerialization() {
        this.cSV = null;
        this.jSON = null;
    }

    private OutputSerialization(Builder builder) {
        this.cSV = builder.cSV;
        this.jSON = builder.jSON;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setCSV(final CSVOutput cSV) {
        this.cSV = cSV;
    }

    public JSONOutput jSON() {
        return jSON;
    }

    public void setJSON(final JSONOutput jSON) {
        this.jSON = jSON;
    }

    static final class Builder {
        private CSVOutput cSV;

        private JSONOutput jSON;

        private Builder() {
        }

        private Builder(OutputSerialization model) {
            cSV(model.cSV);
            jSON(model.jSON);
        }

        public OutputSerialization build() {
            return new com.amazonaws.s3.model.OutputSerialization(this);
        }

        /**
         * <p>Describes the serialization of CSV-encoded Select results.</p>
         */
        public final Builder cSV(CSVOutput cSV) {
            this.cSV = cSV;
            return this;
        }

        /**
         * <p>Specifies JSON as request's output serialization format.</p>
         */
        public final Builder jSON(JSONOutput jSON) {
            this.jSON = jSON;
            return this;
        }
    }
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class JSONOutput {
    private String recordDelimiter;

    private JSONOutput() {
        this.recordDelimiter = null;
    }

    private JSONOutput(Builder builder) {
        this.recordDelimiter = builder.recordDelimiter;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(JSONOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof JSONOutput);
    }

    public String recordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(final String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    static final class Builder {
        private String recordDelimiter;

        private Builder() {
        }

        private Builder(JSONOutput model) {
            recordDelimiter(model.recordDelimiter);
        }

        public JSONOutput build() {
            return new com.amazonaws.s3.model.JSONOutput(this);
        }

        /**
         * <p>The value used to separate individual records in the output. If no value is specified,
         *          Amazon S3 uses a newline character ('\n').</p>
         */
        public final Builder recordDelimiter(String recordDelimiter) {
            this.recordDelimiter = recordDelimiter;
            return this;
        }
    }
}

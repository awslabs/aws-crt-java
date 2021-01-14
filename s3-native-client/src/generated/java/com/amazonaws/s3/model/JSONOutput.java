// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class JSONOutput {
    /**
     * <p>The value used to separate individual records in the output. If no value is specified,
     *          Amazon S3 uses a newline character ('\n').</p>
     */
    String recordDelimiter;

    JSONOutput() {
        this.recordDelimiter = "";
    }

    protected JSONOutput(BuilderImpl builder) {
        this.recordDelimiter = builder.recordDelimiter;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder recordDelimiter(String recordDelimiter);

        JSONOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The value used to separate individual records in the output. If no value is specified,
         *          Amazon S3 uses a newline character ('\n').</p>
         */
        String recordDelimiter;

        protected BuilderImpl() {
        }

        private BuilderImpl(JSONOutput model) {
            recordDelimiter(model.recordDelimiter);
        }

        public JSONOutput build() {
            return new JSONOutput(this);
        }

        public final Builder recordDelimiter(String recordDelimiter) {
            this.recordDelimiter = recordDelimiter;
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

        public String recordDelimiter() {
            return recordDelimiter;
        }
    }
}

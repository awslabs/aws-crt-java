// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CommonPrefix {
    /**
     * <p>Container for the specified common prefix.</p>
     */
    String prefix;

    CommonPrefix() {
        this.prefix = "";
    }

    protected CommonPrefix(BuilderImpl builder) {
        this.prefix = builder.prefix;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CommonPrefix.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CommonPrefix);
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public interface Builder {
        Builder prefix(String prefix);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for the specified common prefix.</p>
         */
        String prefix;

        protected BuilderImpl() {
        }

        private BuilderImpl(CommonPrefix model) {
            prefix(model.prefix);
        }

        public CommonPrefix build() {
            return new CommonPrefix(this);
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
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

        public String prefix() {
            return prefix;
        }

        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }
    }
}

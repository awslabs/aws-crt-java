// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Tagging {
    /**
     * <p>A collection for a set of tags</p>
     */
    List<Tag> tagSet;

    Tagging() {
        this.tagSet = null;
    }

    protected Tagging(BuilderImpl builder) {
        this.tagSet = builder.tagSet;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Tagging.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Tagging);
    }

    public List<Tag> tagSet() {
        return tagSet;
    }

    public void setTagSet(final List<Tag> tagSet) {
        this.tagSet = tagSet;
    }

    public interface Builder {
        Builder tagSet(List<Tag> tagSet);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A collection for a set of tags</p>
         */
        List<Tag> tagSet;

        protected BuilderImpl() {
        }

        private BuilderImpl(Tagging model) {
            tagSet(model.tagSet);
        }

        public Tagging build() {
            return new Tagging(this);
        }

        public final Builder tagSet(List<Tag> tagSet) {
            this.tagSet = tagSet;
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

        public List<Tag> tagSet() {
            return tagSet;
        }

        public void setTagSet(final List<Tag> tagSet) {
            this.tagSet = tagSet;
        }
    }
}

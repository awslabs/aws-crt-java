// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Tagging {
    private List<Tag> tagSet;

    private Tagging() {
        this.tagSet = null;
    }

    private Tagging(Builder builder) {
        this.tagSet = builder.tagSet;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private List<Tag> tagSet;

        private Builder() {
        }

        private Builder(Tagging model) {
            tagSet(model.tagSet);
        }

        public Tagging build() {
            return new com.amazonaws.s3.model.Tagging(this);
        }

        /**
         * <p>A collection for a set of tags</p>
         */
        public final Builder tagSet(List<Tag> tagSet) {
            this.tagSet = tagSet;
            return this;
        }
    }
}

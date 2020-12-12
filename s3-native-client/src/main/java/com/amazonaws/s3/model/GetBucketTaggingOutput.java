// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketTaggingOutput {
    private List<Tag> tagSet;

    private GetBucketTaggingOutput() {
        this.tagSet = null;
    }

    private GetBucketTaggingOutput(Builder builder) {
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
        return Objects.hash(GetBucketTaggingOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketTaggingOutput);
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

        private Builder(GetBucketTaggingOutput model) {
            tagSet(model.tagSet);
        }

        public GetBucketTaggingOutput build() {
            return new com.amazonaws.s3.model.GetBucketTaggingOutput(this);
        }

        /**
         * <p>Contains the tag set.</p>
         */
        public final Builder tagSet(List<Tag> tagSet) {
            this.tagSet = tagSet;
            return this;
        }
    }
}

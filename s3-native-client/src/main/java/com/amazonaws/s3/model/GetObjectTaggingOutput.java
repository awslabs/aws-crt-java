// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectTaggingOutput {
    private String versionId;

    private List<Tag> tagSet;

    private GetObjectTaggingOutput() {
        this.versionId = null;
        this.tagSet = null;
    }

    private GetObjectTaggingOutput(Builder builder) {
        this.versionId = builder.versionId;
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
        return Objects.hash(GetObjectTaggingOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectTaggingOutput);
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public List<Tag> tagSet() {
        return tagSet;
    }

    public void setTagSet(final List<Tag> tagSet) {
        this.tagSet = tagSet;
    }

    static final class Builder {
        private String versionId;

        private List<Tag> tagSet;

        private Builder() {
        }

        private Builder(GetObjectTaggingOutput model) {
            versionId(model.versionId);
            tagSet(model.tagSet);
        }

        public GetObjectTaggingOutput build() {
            return new com.amazonaws.s3.model.GetObjectTaggingOutput(this);
        }

        /**
         * <p>The versionId of the object for which you got the tagging information.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
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

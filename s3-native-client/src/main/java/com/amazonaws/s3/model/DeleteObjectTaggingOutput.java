// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectTaggingOutput {
    private String versionId;

    private DeleteObjectTaggingOutput() {
        this.versionId = null;
    }

    private DeleteObjectTaggingOutput(Builder builder) {
        this.versionId = builder.versionId;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteObjectTaggingOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteObjectTaggingOutput);
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    static final class Builder {
        private String versionId;

        private Builder() {
        }

        private Builder(DeleteObjectTaggingOutput model) {
            versionId(model.versionId);
        }

        public DeleteObjectTaggingOutput build() {
            return new com.amazonaws.s3.model.DeleteObjectTaggingOutput(this);
        }

        /**
         * <p>The versionId of the object the tag-set was removed from.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }
    }
}

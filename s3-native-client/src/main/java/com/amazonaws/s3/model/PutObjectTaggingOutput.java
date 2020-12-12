// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectTaggingOutput {
    private String versionId;

    private PutObjectTaggingOutput() {
        this.versionId = null;
    }

    private PutObjectTaggingOutput(Builder builder) {
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
        return Objects.hash(PutObjectTaggingOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectTaggingOutput);
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

        private Builder(PutObjectTaggingOutput model) {
            versionId(model.versionId);
        }

        public PutObjectTaggingOutput build() {
            return new com.amazonaws.s3.model.PutObjectTaggingOutput(this);
        }

        /**
         * <p>The versionId of the object the tag-set was added to.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }
    }
}

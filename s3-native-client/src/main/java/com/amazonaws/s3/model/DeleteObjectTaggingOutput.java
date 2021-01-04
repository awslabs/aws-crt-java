// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectTaggingOutput {
    /**
     * <p>The versionId of the object the tag-set was removed from.</p>
     */
    String versionId;

    DeleteObjectTaggingOutput() {
        this.versionId = "";
    }

    protected DeleteObjectTaggingOutput(BuilderImpl builder) {
        this.versionId = builder.versionId;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder versionId(String versionId);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The versionId of the object the tag-set was removed from.</p>
         */
        String versionId;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteObjectTaggingOutput model) {
            versionId(model.versionId);
        }

        public DeleteObjectTaggingOutput build() {
            return new DeleteObjectTaggingOutput(this);
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
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

        public String versionId() {
            return versionId;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }
    }
}

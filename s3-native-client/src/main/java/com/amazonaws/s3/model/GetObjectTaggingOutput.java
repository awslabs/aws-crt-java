// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectTaggingOutput {
    /**
     * <p>The versionId of the object for which you got the tagging information.</p>
     */
    String versionId;

    /**
     * <p>Contains the tag set.</p>
     */
    List<Tag> tagSet;

    GetObjectTaggingOutput() {
        this.versionId = "";
        this.tagSet = null;
    }

    protected GetObjectTaggingOutput(BuilderImpl builder) {
        this.versionId = builder.versionId;
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

    public List<Tag> tagSet() {
        return tagSet;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public void setTagSet(final List<Tag> tagSet) {
        this.tagSet = tagSet;
    }

    public interface Builder {
        Builder versionId(String versionId);

        Builder tagSet(List<Tag> tagSet);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The versionId of the object for which you got the tagging information.</p>
         */
        String versionId;

        /**
         * <p>Contains the tag set.</p>
         */
        List<Tag> tagSet;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectTaggingOutput model) {
            versionId(model.versionId);
            tagSet(model.tagSet);
        }

        public GetObjectTaggingOutput build() {
            return new GetObjectTaggingOutput(this);
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
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

        public String versionId() {
            return versionId;
        }

        public List<Tag> tagSet() {
            return tagSet;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }

        public void setTagSet(final List<Tag> tagSet) {
            this.tagSet = tagSet;
        }
    }
}

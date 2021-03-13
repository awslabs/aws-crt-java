// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectTaggingOutput {
    /**
     * <p>The versionId of the object the tag-set was added to.</p>
     */
    String versionId;

    PutObjectTaggingOutput() {
        this.versionId = "";
    }

    protected PutObjectTaggingOutput(BuilderImpl builder) {
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

    public interface Builder {
        Builder versionId(String versionId);

        PutObjectTaggingOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The versionId of the object the tag-set was added to.</p>
         */
        String versionId;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectTaggingOutput model) {
            versionId(model.versionId);
        }

        public PutObjectTaggingOutput build() {
            return new PutObjectTaggingOutput(this);
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
    }
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectIdentifier {
    /**
     * <p>Key name of the object to delete.</p>
     */
    String key;

    /**
     * <p>VersionId for the specific version of the object to delete.</p>
     */
    String versionId;

    ObjectIdentifier() {
        this.key = "";
        this.versionId = "";
    }

    protected ObjectIdentifier(BuilderImpl builder) {
        this.key = builder.key;
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
        return Objects.hash(ObjectIdentifier.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectIdentifier);
    }

    public String key() {
        return key;
    }

    public String versionId() {
        return versionId;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public interface Builder {
        Builder key(String key);

        Builder versionId(String versionId);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Key name of the object to delete.</p>
         */
        String key;

        /**
         * <p>VersionId for the specific version of the object to delete.</p>
         */
        String versionId;

        protected BuilderImpl() {
        }

        private BuilderImpl(ObjectIdentifier model) {
            key(model.key);
            versionId(model.versionId);
        }

        public ObjectIdentifier build() {
            return new ObjectIdentifier(this);
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
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

        public String key() {
            return key;
        }

        public String versionId() {
            return versionId;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }
    }
}

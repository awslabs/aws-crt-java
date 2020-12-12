// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectIdentifier {
    private String key;

    private String versionId;

    private ObjectIdentifier() {
        this.key = null;
        this.versionId = null;
    }

    private ObjectIdentifier(Builder builder) {
        this.key = builder.key;
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

    public void setKey(final String key) {
        this.key = key;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    static final class Builder {
        private String key;

        private String versionId;

        private Builder() {
        }

        private Builder(ObjectIdentifier model) {
            key(model.key);
            versionId(model.versionId);
        }

        public ObjectIdentifier build() {
            return new com.amazonaws.s3.model.ObjectIdentifier(this);
        }

        /**
         * <p>Key name of the object to delete.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>VersionId for the specific version of the object to delete.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }
    }
}

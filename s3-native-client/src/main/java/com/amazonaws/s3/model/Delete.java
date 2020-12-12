// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Delete {
    private List<ObjectIdentifier> objects;

    private Boolean quiet;

    private Delete() {
        this.objects = null;
        this.quiet = null;
    }

    private Delete(Builder builder) {
        this.objects = builder.objects;
        this.quiet = builder.quiet;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Delete.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Delete);
    }

    public List<ObjectIdentifier> objects() {
        return objects;
    }

    public void setObjects(final List<ObjectIdentifier> objects) {
        this.objects = objects;
    }

    public Boolean quiet() {
        return quiet;
    }

    public void setQuiet(final Boolean quiet) {
        this.quiet = quiet;
    }

    static final class Builder {
        private List<ObjectIdentifier> objects;

        private Boolean quiet;

        private Builder() {
        }

        private Builder(Delete model) {
            objects(model.objects);
            quiet(model.quiet);
        }

        public Delete build() {
            return new com.amazonaws.s3.model.Delete(this);
        }

        /**
         * <p>The objects to delete.</p>
         */
        public final Builder objects(List<ObjectIdentifier> objects) {
            this.objects = objects;
            return this;
        }

        /**
         * <p>Element to enable quiet mode for the request. When you add this element, you must set
         *          its value to true.</p>
         */
        public final Builder quiet(Boolean quiet) {
            this.quiet = quiet;
            return this;
        }
    }
}

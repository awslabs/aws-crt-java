// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Delete {
    /**
     * <p>The objects to delete.</p>
     */
    List<ObjectIdentifier> objects;

    /**
     * <p>Element to enable quiet mode for the request. When you add this element, you must set
     *          its value to true.</p>
     */
    Boolean quiet;

    Delete() {
        this.objects = null;
        this.quiet = null;
    }

    protected Delete(BuilderImpl builder) {
        this.objects = builder.objects;
        this.quiet = builder.quiet;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Boolean quiet() {
        return quiet;
    }

    public void setObjects(final List<ObjectIdentifier> objects) {
        this.objects = objects;
    }

    public void setQuiet(final Boolean quiet) {
        this.quiet = quiet;
    }

    public interface Builder {
        Builder objects(List<ObjectIdentifier> objects);

        Builder quiet(Boolean quiet);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The objects to delete.</p>
         */
        List<ObjectIdentifier> objects;

        /**
         * <p>Element to enable quiet mode for the request. When you add this element, you must set
         *          its value to true.</p>
         */
        Boolean quiet;

        protected BuilderImpl() {
        }

        private BuilderImpl(Delete model) {
            objects(model.objects);
            quiet(model.quiet);
        }

        public Delete build() {
            return new Delete(this);
        }

        public final Builder objects(List<ObjectIdentifier> objects) {
            this.objects = objects;
            return this;
        }

        public final Builder quiet(Boolean quiet) {
            this.quiet = quiet;
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

        public List<ObjectIdentifier> objects() {
            return objects;
        }

        public Boolean quiet() {
            return quiet;
        }

        public void setObjects(final List<ObjectIdentifier> objects) {
            this.objects = objects;
        }

        public void setQuiet(final Boolean quiet) {
            this.quiet = quiet;
        }
    }
}

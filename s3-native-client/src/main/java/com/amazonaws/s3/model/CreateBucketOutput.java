// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CreateBucketOutput {
    /**
     * <p>Specifies the Region where the bucket will be created. If you are creating a bucket on
     *          the US East (N. Virginia) Region (us-east-1), you do not need to specify the
     *          location.</p>
     */
    String location;

    CreateBucketOutput() {
        this.location = "";
    }

    protected CreateBucketOutput(BuilderImpl builder) {
        this.location = builder.location;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CreateBucketOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CreateBucketOutput);
    }

    public String location() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public interface Builder {
        Builder location(String location);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the Region where the bucket will be created. If you are creating a bucket on
         *          the US East (N. Virginia) Region (us-east-1), you do not need to specify the
         *          location.</p>
         */
        String location;

        protected BuilderImpl() {
        }

        private BuilderImpl(CreateBucketOutput model) {
            location(model.location);
        }

        public CreateBucketOutput build() {
            return new CreateBucketOutput(this);
        }

        public final Builder location(String location) {
            this.location = location;
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

        public String location() {
            return location;
        }

        public void setLocation(final String location) {
            this.location = location;
        }
    }
}

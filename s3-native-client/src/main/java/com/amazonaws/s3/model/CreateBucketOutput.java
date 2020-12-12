// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CreateBucketOutput {
    private String location;

    private CreateBucketOutput() {
        this.location = null;
    }

    private CreateBucketOutput(Builder builder) {
        this.location = builder.location;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private String location;

        private Builder() {
        }

        private Builder(CreateBucketOutput model) {
            location(model.location);
        }

        public CreateBucketOutput build() {
            return new com.amazonaws.s3.model.CreateBucketOutput(this);
        }

        /**
         * <p>Specifies the Region where the bucket will be created. If you are creating a bucket on
         *          the US East (N. Virginia) Region (us-east-1), you do not need to specify the
         *          location.</p>
         */
        public final Builder location(String location) {
            this.location = location;
            return this;
        }
    }
}

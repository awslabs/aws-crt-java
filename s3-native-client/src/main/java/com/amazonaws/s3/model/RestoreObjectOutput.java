// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RestoreObjectOutput {
    private RequestCharged requestCharged;

    private String restoreOutputPath;

    private RestoreObjectOutput() {
        this.requestCharged = null;
        this.restoreOutputPath = null;
    }

    private RestoreObjectOutput(Builder builder) {
        this.requestCharged = builder.requestCharged;
        this.restoreOutputPath = builder.restoreOutputPath;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(RestoreObjectOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof RestoreObjectOutput);
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public String restoreOutputPath() {
        return restoreOutputPath;
    }

    public void setRestoreOutputPath(final String restoreOutputPath) {
        this.restoreOutputPath = restoreOutputPath;
    }

    static final class Builder {
        private RequestCharged requestCharged;

        private String restoreOutputPath;

        private Builder() {
        }

        private Builder(RestoreObjectOutput model) {
            requestCharged(model.requestCharged);
            restoreOutputPath(model.restoreOutputPath);
        }

        public RestoreObjectOutput build() {
            return new com.amazonaws.s3.model.RestoreObjectOutput(this);
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        /**
         * <p>Indicates the path in the provided S3 output location where Select results will be
         *          restored to.</p>
         */
        public final Builder restoreOutputPath(String restoreOutputPath) {
            this.restoreOutputPath = restoreOutputPath;
            return this;
        }
    }
}

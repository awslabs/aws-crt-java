// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RestoreObjectOutput {
    RequestCharged requestCharged;

    /**
     * <p>Indicates the path in the provided S3 output location where Select results will be
     *          restored to.</p>
     */
    String restoreOutputPath;

    RestoreObjectOutput() {
        this.requestCharged = null;
        this.restoreOutputPath = "";
    }

    protected RestoreObjectOutput(BuilderImpl builder) {
        this.requestCharged = builder.requestCharged;
        this.restoreOutputPath = builder.restoreOutputPath;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String restoreOutputPath() {
        return restoreOutputPath;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public void setRestoreOutputPath(final String restoreOutputPath) {
        this.restoreOutputPath = restoreOutputPath;
    }

    public interface Builder {
        Builder requestCharged(RequestCharged requestCharged);

        Builder restoreOutputPath(String restoreOutputPath);

        RestoreObjectOutput build();
    }

    protected static class BuilderImpl implements Builder {
        RequestCharged requestCharged;

        /**
         * <p>Indicates the path in the provided S3 output location where Select results will be
         *          restored to.</p>
         */
        String restoreOutputPath;

        protected BuilderImpl() {
        }

        private BuilderImpl(RestoreObjectOutput model) {
            requestCharged(model.requestCharged);
            restoreOutputPath(model.restoreOutputPath);
        }

        public RestoreObjectOutput build() {
            return new RestoreObjectOutput(this);
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        public final Builder restoreOutputPath(String restoreOutputPath) {
            this.restoreOutputPath = restoreOutputPath;
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

        public RequestCharged requestCharged() {
            return requestCharged;
        }

        public String restoreOutputPath() {
            return restoreOutputPath;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }

        public void setRestoreOutputPath(final String restoreOutputPath) {
            this.restoreOutputPath = restoreOutputPath;
        }
    }
}

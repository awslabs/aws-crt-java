// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RequestProgress {
    private Boolean enabled;

    private RequestProgress() {
        this.enabled = null;
    }

    private RequestProgress(Builder builder) {
        this.enabled = builder.enabled;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(RequestProgress.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof RequestProgress);
    }

    public Boolean enabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    static final class Builder {
        private Boolean enabled;

        private Builder() {
        }

        private Builder(RequestProgress model) {
            enabled(model.enabled);
        }

        public RequestProgress build() {
            return new com.amazonaws.s3.model.RequestProgress(this);
        }

        /**
         * <p>Specifies whether periodic QueryProgress frames should be sent. Valid values: TRUE,
         *          FALSE. Default value: FALSE.</p>
         */
        public final Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }
    }
}

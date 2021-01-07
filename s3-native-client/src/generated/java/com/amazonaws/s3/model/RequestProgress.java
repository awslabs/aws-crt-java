// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RequestProgress {
    /**
     * <p>Specifies whether periodic QueryProgress frames should be sent. Valid values: TRUE,
     *          FALSE. Default value: FALSE.</p>
     */
    Boolean enabled;

    RequestProgress() {
        this.enabled = null;
    }

    protected RequestProgress(BuilderImpl builder) {
        this.enabled = builder.enabled;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder enabled(Boolean enabled);

        RequestProgress build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies whether periodic QueryProgress frames should be sent. Valid values: TRUE,
         *          FALSE. Default value: FALSE.</p>
         */
        Boolean enabled;

        protected BuilderImpl() {
        }

        private BuilderImpl(RequestProgress model) {
            enabled(model.enabled);
        }

        public RequestProgress build() {
            return new RequestProgress(this);
        }

        public final Builder enabled(Boolean enabled) {
            this.enabled = enabled;
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

        public Boolean enabled() {
            return enabled;
        }

        public void setEnabled(final Boolean enabled) {
            this.enabled = enabled;
        }
    }
}

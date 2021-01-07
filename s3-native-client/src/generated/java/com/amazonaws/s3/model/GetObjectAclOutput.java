// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectAclOutput {
    /**
     * <p> Container for the bucket owner's display name and ID.</p>
     */
    Owner owner;

    /**
     * <p>A list of grants.</p>
     */
    List<Grant> grants;

    RequestCharged requestCharged;

    GetObjectAclOutput() {
        this.owner = null;
        this.grants = null;
        this.requestCharged = null;
    }

    protected GetObjectAclOutput(BuilderImpl builder) {
        this.owner = builder.owner;
        this.grants = builder.grants;
        this.requestCharged = builder.requestCharged;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetObjectAclOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectAclOutput);
    }

    public Owner owner() {
        return owner;
    }

    public List<Grant> grants() {
        return grants;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public void setGrants(final List<Grant> grants) {
        this.grants = grants;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public interface Builder {
        Builder owner(Owner owner);

        Builder grants(List<Grant> grants);

        Builder requestCharged(RequestCharged requestCharged);

        GetObjectAclOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p> Container for the bucket owner's display name and ID.</p>
         */
        Owner owner;

        /**
         * <p>A list of grants.</p>
         */
        List<Grant> grants;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectAclOutput model) {
            owner(model.owner);
            grants(model.grants);
            requestCharged(model.requestCharged);
        }

        public GetObjectAclOutput build() {
            return new GetObjectAclOutput(this);
        }

        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public final Builder grants(List<Grant> grants) {
            this.grants = grants;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
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

        public Owner owner() {
            return owner;
        }

        public List<Grant> grants() {
            return grants;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }

        public void setOwner(final Owner owner) {
            this.owner = owner;
        }

        public void setGrants(final List<Grant> grants) {
            this.grants = grants;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }
    }
}

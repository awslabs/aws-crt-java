// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectTorrentOutput {
    /**
     * <p>A Bencoded dictionary as defined by the BitTorrent specification</p>
     */
    byte[] body;

    RequestCharged requestCharged;

    GetObjectTorrentOutput() {
        this.body = null;
        this.requestCharged = null;
    }

    protected GetObjectTorrentOutput(BuilderImpl builder) {
        this.body = builder.body;
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
        return Objects.hash(GetObjectTorrentOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectTorrentOutput);
    }

    public byte[] body() {
        return body;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public interface Builder {
        Builder body(byte[] body);

        Builder requestCharged(RequestCharged requestCharged);

        GetObjectTorrentOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A Bencoded dictionary as defined by the BitTorrent specification</p>
         */
        byte[] body;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectTorrentOutput model) {
            body(model.body);
            requestCharged(model.requestCharged);
        }

        public GetObjectTorrentOutput build() {
            return new GetObjectTorrentOutput(this);
        }

        public final Builder body(byte[] body) {
            this.body = body;
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

        public byte[] body() {
            return body;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }
    }
}

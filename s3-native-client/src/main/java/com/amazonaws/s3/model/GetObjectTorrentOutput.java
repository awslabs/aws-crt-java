// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectTorrentOutput {
    private byte[] body;

    private RequestCharged requestCharged;

    private GetObjectTorrentOutput() {
        this.body = null;
        this.requestCharged = null;
    }

    private GetObjectTorrentOutput(Builder builder) {
        this.body = builder.body;
        this.requestCharged = builder.requestCharged;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBody(final byte[] body) {
        this.body = body;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    static final class Builder {
        private byte[] body;

        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(GetObjectTorrentOutput model) {
            body(model.body);
            requestCharged(model.requestCharged);
        }

        public GetObjectTorrentOutput build() {
            return new com.amazonaws.s3.model.GetObjectTorrentOutput(this);
        }

        /**
         * <p>A Bencoded dictionary as defined by the BitTorrent specification</p>
         */
        public final Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }
    }
}

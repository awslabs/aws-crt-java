// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.UnionGenerator")
public class SelectObjectContentEventStream {
    private RecordsEvent records;

    private StatsEvent stats;

    private ProgressEvent progress;

    private ContinuationEvent cont;

    private EndEvent end;

    private SelectObjectContentEventStream(Builder builder) {
        this.records = builder.records;
        this.stats = builder.stats;
        this.progress = builder.progress;
        this.cont = builder.cont;
        this.end = builder.end;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SelectObjectContentEventStream.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SelectObjectContentEventStream);
    }

    public RecordsEvent records() {
        return records;
    }

    public StatsEvent stats() {
        return stats;
    }

    public ProgressEvent progress() {
        return progress;
    }

    public ContinuationEvent cont() {
        return cont;
    }

    public EndEvent end() {
        return end;
    }

    static final class Builder {
        private RecordsEvent records;

        private StatsEvent stats;

        private ProgressEvent progress;

        private ContinuationEvent cont;

        private EndEvent end;

        private Builder() {
        }

        private Builder(SelectObjectContentEventStream model) {
            records(model.records);
            stats(model.stats);
            progress(model.progress);
            cont(model.cont);
            end(model.end);
        }

        public SelectObjectContentEventStream build() {
            return new com.amazonaws.s3.model.SelectObjectContentEventStream(this);
        }

        /**
         * <p>The Records Event.</p>
         */
        public final Builder records(RecordsEvent records) {
            this.records = records;
            return this;
        }

        /**
         * <p>The Stats Event.</p>
         */
        public final Builder stats(StatsEvent stats) {
            this.stats = stats;
            return this;
        }

        /**
         * <p>The Progress Event.</p>
         */
        public final Builder progress(ProgressEvent progress) {
            this.progress = progress;
            return this;
        }

        /**
         * <p>The Continuation Event.</p>
         */
        public final Builder cont(ContinuationEvent cont) {
            this.cont = cont;
            return this;
        }

        /**
         * <p>The End Event.</p>
         */
        public final Builder end(EndEvent end) {
            this.end = end;
            return this;
        }
    }
}

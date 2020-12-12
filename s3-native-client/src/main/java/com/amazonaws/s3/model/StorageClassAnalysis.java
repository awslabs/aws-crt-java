// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class StorageClassAnalysis {
    private StorageClassAnalysisDataExport dataExport;

    private StorageClassAnalysis() {
        this.dataExport = null;
    }

    private StorageClassAnalysis(Builder builder) {
        this.dataExport = builder.dataExport;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(StorageClassAnalysis.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof StorageClassAnalysis);
    }

    public StorageClassAnalysisDataExport dataExport() {
        return dataExport;
    }

    public void setDataExport(final StorageClassAnalysisDataExport dataExport) {
        this.dataExport = dataExport;
    }

    static final class Builder {
        private StorageClassAnalysisDataExport dataExport;

        private Builder() {
        }

        private Builder(StorageClassAnalysis model) {
            dataExport(model.dataExport);
        }

        public StorageClassAnalysis build() {
            return new com.amazonaws.s3.model.StorageClassAnalysis(this);
        }

        /**
         * <p>Specifies how data related to the storage class analysis for an Amazon S3 bucket should be
         *          exported.</p>
         */
        public final Builder dataExport(StorageClassAnalysisDataExport dataExport) {
            this.dataExport = dataExport;
            return this;
        }
    }
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class StorageClassAnalysis {
    /**
     * <p>Specifies how data related to the storage class analysis for an Amazon S3 bucket should be
     *          exported.</p>
     */
    StorageClassAnalysisDataExport dataExport;

    StorageClassAnalysis() {
        this.dataExport = null;
    }

    protected StorageClassAnalysis(BuilderImpl builder) {
        this.dataExport = builder.dataExport;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder dataExport(StorageClassAnalysisDataExport dataExport);

        StorageClassAnalysis build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies how data related to the storage class analysis for an Amazon S3 bucket should be
         *          exported.</p>
         */
        StorageClassAnalysisDataExport dataExport;

        protected BuilderImpl() {
        }

        private BuilderImpl(StorageClassAnalysis model) {
            dataExport(model.dataExport);
        }

        public StorageClassAnalysis build() {
            return new StorageClassAnalysis(this);
        }

        public final Builder dataExport(StorageClassAnalysisDataExport dataExport) {
            this.dataExport = dataExport;
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

        public StorageClassAnalysisDataExport dataExport() {
            return dataExport;
        }

        public void setDataExport(final StorageClassAnalysisDataExport dataExport) {
            this.dataExport = dataExport;
        }
    }
}

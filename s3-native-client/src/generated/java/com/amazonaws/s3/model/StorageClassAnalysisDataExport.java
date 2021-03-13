// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class StorageClassAnalysisDataExport {
    /**
     * <p>The version of the output schema to use when exporting data. Must be
     *          <code>V_1</code>.</p>
     */
    StorageClassAnalysisSchemaVersion outputSchemaVersion;

    /**
     * <p>The place to store the data for an analysis.</p>
     */
    AnalyticsExportDestination destination;

    StorageClassAnalysisDataExport() {
        this.outputSchemaVersion = null;
        this.destination = null;
    }

    protected StorageClassAnalysisDataExport(BuilderImpl builder) {
        this.outputSchemaVersion = builder.outputSchemaVersion;
        this.destination = builder.destination;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(StorageClassAnalysisDataExport.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof StorageClassAnalysisDataExport);
    }

    public StorageClassAnalysisSchemaVersion outputSchemaVersion() {
        return outputSchemaVersion;
    }

    public AnalyticsExportDestination destination() {
        return destination;
    }

    public interface Builder {
        Builder outputSchemaVersion(StorageClassAnalysisSchemaVersion outputSchemaVersion);

        Builder destination(AnalyticsExportDestination destination);

        StorageClassAnalysisDataExport build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The version of the output schema to use when exporting data. Must be
         *          <code>V_1</code>.</p>
         */
        StorageClassAnalysisSchemaVersion outputSchemaVersion;

        /**
         * <p>The place to store the data for an analysis.</p>
         */
        AnalyticsExportDestination destination;

        protected BuilderImpl() {
        }

        private BuilderImpl(StorageClassAnalysisDataExport model) {
            outputSchemaVersion(model.outputSchemaVersion);
            destination(model.destination);
        }

        public StorageClassAnalysisDataExport build() {
            return new StorageClassAnalysisDataExport(this);
        }

        public final Builder outputSchemaVersion(
                StorageClassAnalysisSchemaVersion outputSchemaVersion) {
            this.outputSchemaVersion = outputSchemaVersion;
            return this;
        }

        public final Builder destination(AnalyticsExportDestination destination) {
            this.destination = destination;
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

        public StorageClassAnalysisSchemaVersion outputSchemaVersion() {
            return outputSchemaVersion;
        }

        public AnalyticsExportDestination destination() {
            return destination;
        }
    }
}

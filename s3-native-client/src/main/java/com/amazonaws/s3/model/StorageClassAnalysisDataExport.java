// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class StorageClassAnalysisDataExport {
    private StorageClassAnalysisSchemaVersion outputSchemaVersion;

    private AnalyticsExportDestination destination;

    private StorageClassAnalysisDataExport() {
        this.outputSchemaVersion = null;
        this.destination = null;
    }

    private StorageClassAnalysisDataExport(Builder builder) {
        this.outputSchemaVersion = builder.outputSchemaVersion;
        this.destination = builder.destination;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setOutputSchemaVersion(
            final StorageClassAnalysisSchemaVersion outputSchemaVersion) {
        this.outputSchemaVersion = outputSchemaVersion;
    }

    public AnalyticsExportDestination destination() {
        return destination;
    }

    public void setDestination(final AnalyticsExportDestination destination) {
        this.destination = destination;
    }

    static final class Builder {
        private StorageClassAnalysisSchemaVersion outputSchemaVersion;

        private AnalyticsExportDestination destination;

        private Builder() {
        }

        private Builder(StorageClassAnalysisDataExport model) {
            outputSchemaVersion(model.outputSchemaVersion);
            destination(model.destination);
        }

        public StorageClassAnalysisDataExport build() {
            return new com.amazonaws.s3.model.StorageClassAnalysisDataExport(this);
        }

        /**
         * <p>The version of the output schema to use when exporting data. Must be
         *          <code>V_1</code>.</p>
         */
        public final Builder outputSchemaVersion(
                StorageClassAnalysisSchemaVersion outputSchemaVersion) {
            this.outputSchemaVersion = outputSchemaVersion;
            return this;
        }

        /**
         * <p>The place to store the data for an analysis.</p>
         */
        public final Builder destination(AnalyticsExportDestination destination) {
            this.destination = destination;
            return this;
        }
    }
}

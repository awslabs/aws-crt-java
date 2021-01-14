// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InputSerialization {
    /**
     * <p>Describes the serialization of a CSV-encoded object.</p>
     */
    CSVInput cSV;

    /**
     * <p>Specifies object's compression format. Valid values: NONE, GZIP, BZIP2. Default Value:
     *          NONE.</p>
     */
    CompressionType compressionType;

    /**
     * <p>Specifies JSON as object's input serialization format.</p>
     */
    JSONInput jSON;

    /**
     * <p>Specifies Parquet as object's input serialization format.</p>
     */
    ParquetInput parquet;

    InputSerialization() {
        this.cSV = null;
        this.compressionType = null;
        this.jSON = null;
        this.parquet = null;
    }

    protected InputSerialization(BuilderImpl builder) {
        this.cSV = builder.cSV;
        this.compressionType = builder.compressionType;
        this.jSON = builder.jSON;
        this.parquet = builder.parquet;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(InputSerialization.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InputSerialization);
    }

    public CSVInput cSV() {
        return cSV;
    }

    public CompressionType compressionType() {
        return compressionType;
    }

    public JSONInput jSON() {
        return jSON;
    }

    public ParquetInput parquet() {
        return parquet;
    }

    public interface Builder {
        Builder cSV(CSVInput cSV);

        Builder compressionType(CompressionType compressionType);

        Builder jSON(JSONInput jSON);

        Builder parquet(ParquetInput parquet);

        InputSerialization build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Describes the serialization of a CSV-encoded object.</p>
         */
        CSVInput cSV;

        /**
         * <p>Specifies object's compression format. Valid values: NONE, GZIP, BZIP2. Default Value:
         *          NONE.</p>
         */
        CompressionType compressionType;

        /**
         * <p>Specifies JSON as object's input serialization format.</p>
         */
        JSONInput jSON;

        /**
         * <p>Specifies Parquet as object's input serialization format.</p>
         */
        ParquetInput parquet;

        protected BuilderImpl() {
        }

        private BuilderImpl(InputSerialization model) {
            cSV(model.cSV);
            compressionType(model.compressionType);
            jSON(model.jSON);
            parquet(model.parquet);
        }

        public InputSerialization build() {
            return new InputSerialization(this);
        }

        public final Builder cSV(CSVInput cSV) {
            this.cSV = cSV;
            return this;
        }

        public final Builder compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        public final Builder jSON(JSONInput jSON) {
            this.jSON = jSON;
            return this;
        }

        public final Builder parquet(ParquetInput parquet) {
            this.parquet = parquet;
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

        public CSVInput cSV() {
            return cSV;
        }

        public CompressionType compressionType() {
            return compressionType;
        }

        public JSONInput jSON() {
            return jSON;
        }

        public ParquetInput parquet() {
            return parquet;
        }
    }
}

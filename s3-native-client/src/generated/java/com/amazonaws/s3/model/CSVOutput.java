// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CSVOutput {
    /**
     * <p>Indicates whether to use quotation marks around output fields. </p>
     *          <ul>
     *             <li>
     *                <p>
     *                   <code>ALWAYS</code>: Always use quotation marks for output fields.</p>
     *             </li>
     *             <li>
     *                <p>
     *                   <code>ASNEEDED</code>: Use quotation marks for output fields when needed.</p>
     *             </li>
     *          </ul>
     */
    QuoteFields quoteFields;

    /**
     * <p>The single character used for escaping the quote character inside an already escaped
     *          value.</p>
     */
    String quoteEscapeCharacter;

    /**
     * <p>A single character used to separate individual records in the output. Instead of the
     *          default value, you can specify an arbitrary delimiter.</p>
     */
    String recordDelimiter;

    /**
     * <p>The value used to separate individual fields in a record. You can specify an arbitrary
     *          delimiter.</p>
     */
    String fieldDelimiter;

    /**
     * <p>A single character used for escaping when the field delimiter is part of the value. For
     *          example, if the value is <code>a, b</code>, Amazon S3 wraps this field value in quotation marks,
     *          as follows: <code>" a , b "</code>.</p>
     */
    String quoteCharacter;

    CSVOutput() {
        this.quoteFields = null;
        this.quoteEscapeCharacter = "";
        this.recordDelimiter = "";
        this.fieldDelimiter = "";
        this.quoteCharacter = "";
    }

    protected CSVOutput(BuilderImpl builder) {
        this.quoteFields = builder.quoteFields;
        this.quoteEscapeCharacter = builder.quoteEscapeCharacter;
        this.recordDelimiter = builder.recordDelimiter;
        this.fieldDelimiter = builder.fieldDelimiter;
        this.quoteCharacter = builder.quoteCharacter;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CSVOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CSVOutput);
    }

    public QuoteFields quoteFields() {
        return quoteFields;
    }

    public String quoteEscapeCharacter() {
        return quoteEscapeCharacter;
    }

    public String recordDelimiter() {
        return recordDelimiter;
    }

    public String fieldDelimiter() {
        return fieldDelimiter;
    }

    public String quoteCharacter() {
        return quoteCharacter;
    }

    public void setQuoteFields(final QuoteFields quoteFields) {
        this.quoteFields = quoteFields;
    }

    public void setQuoteEscapeCharacter(final String quoteEscapeCharacter) {
        this.quoteEscapeCharacter = quoteEscapeCharacter;
    }

    public void setRecordDelimiter(final String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public void setFieldDelimiter(final String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public void setQuoteCharacter(final String quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public interface Builder {
        Builder quoteFields(QuoteFields quoteFields);

        Builder quoteEscapeCharacter(String quoteEscapeCharacter);

        Builder recordDelimiter(String recordDelimiter);

        Builder fieldDelimiter(String fieldDelimiter);

        Builder quoteCharacter(String quoteCharacter);

        CSVOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether to use quotation marks around output fields. </p>
         *          <ul>
         *             <li>
         *                <p>
         *                   <code>ALWAYS</code>: Always use quotation marks for output fields.</p>
         *             </li>
         *             <li>
         *                <p>
         *                   <code>ASNEEDED</code>: Use quotation marks for output fields when needed.</p>
         *             </li>
         *          </ul>
         */
        QuoteFields quoteFields;

        /**
         * <p>The single character used for escaping the quote character inside an already escaped
         *          value.</p>
         */
        String quoteEscapeCharacter;

        /**
         * <p>A single character used to separate individual records in the output. Instead of the
         *          default value, you can specify an arbitrary delimiter.</p>
         */
        String recordDelimiter;

        /**
         * <p>The value used to separate individual fields in a record. You can specify an arbitrary
         *          delimiter.</p>
         */
        String fieldDelimiter;

        /**
         * <p>A single character used for escaping when the field delimiter is part of the value. For
         *          example, if the value is <code>a, b</code>, Amazon S3 wraps this field value in quotation marks,
         *          as follows: <code>" a , b "</code>.</p>
         */
        String quoteCharacter;

        protected BuilderImpl() {
        }

        private BuilderImpl(CSVOutput model) {
            quoteFields(model.quoteFields);
            quoteEscapeCharacter(model.quoteEscapeCharacter);
            recordDelimiter(model.recordDelimiter);
            fieldDelimiter(model.fieldDelimiter);
            quoteCharacter(model.quoteCharacter);
        }

        public CSVOutput build() {
            return new CSVOutput(this);
        }

        public final Builder quoteFields(QuoteFields quoteFields) {
            this.quoteFields = quoteFields;
            return this;
        }

        public final Builder quoteEscapeCharacter(String quoteEscapeCharacter) {
            this.quoteEscapeCharacter = quoteEscapeCharacter;
            return this;
        }

        public final Builder recordDelimiter(String recordDelimiter) {
            this.recordDelimiter = recordDelimiter;
            return this;
        }

        public final Builder fieldDelimiter(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
            return this;
        }

        public final Builder quoteCharacter(String quoteCharacter) {
            this.quoteCharacter = quoteCharacter;
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

        public QuoteFields quoteFields() {
            return quoteFields;
        }

        public String quoteEscapeCharacter() {
            return quoteEscapeCharacter;
        }

        public String recordDelimiter() {
            return recordDelimiter;
        }

        public String fieldDelimiter() {
            return fieldDelimiter;
        }

        public String quoteCharacter() {
            return quoteCharacter;
        }

        public void setQuoteFields(final QuoteFields quoteFields) {
            this.quoteFields = quoteFields;
        }

        public void setQuoteEscapeCharacter(final String quoteEscapeCharacter) {
            this.quoteEscapeCharacter = quoteEscapeCharacter;
        }

        public void setRecordDelimiter(final String recordDelimiter) {
            this.recordDelimiter = recordDelimiter;
        }

        public void setFieldDelimiter(final String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
        }

        public void setQuoteCharacter(final String quoteCharacter) {
            this.quoteCharacter = quoteCharacter;
        }
    }
}

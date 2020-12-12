// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CSVOutput {
    private QuoteFields quoteFields;

    private String quoteEscapeCharacter;

    private String recordDelimiter;

    private String fieldDelimiter;

    private String quoteCharacter;

    private CSVOutput() {
        this.quoteFields = null;
        this.quoteEscapeCharacter = null;
        this.recordDelimiter = null;
        this.fieldDelimiter = null;
        this.quoteCharacter = null;
    }

    private CSVOutput(Builder builder) {
        this.quoteFields = builder.quoteFields;
        this.quoteEscapeCharacter = builder.quoteEscapeCharacter;
        this.recordDelimiter = builder.recordDelimiter;
        this.fieldDelimiter = builder.fieldDelimiter;
        this.quoteCharacter = builder.quoteCharacter;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setQuoteFields(final QuoteFields quoteFields) {
        this.quoteFields = quoteFields;
    }

    public String quoteEscapeCharacter() {
        return quoteEscapeCharacter;
    }

    public void setQuoteEscapeCharacter(final String quoteEscapeCharacter) {
        this.quoteEscapeCharacter = quoteEscapeCharacter;
    }

    public String recordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(final String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public String fieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(final String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public String quoteCharacter() {
        return quoteCharacter;
    }

    public void setQuoteCharacter(final String quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    static final class Builder {
        private QuoteFields quoteFields;

        private String quoteEscapeCharacter;

        private String recordDelimiter;

        private String fieldDelimiter;

        private String quoteCharacter;

        private Builder() {
        }

        private Builder(CSVOutput model) {
            quoteFields(model.quoteFields);
            quoteEscapeCharacter(model.quoteEscapeCharacter);
            recordDelimiter(model.recordDelimiter);
            fieldDelimiter(model.fieldDelimiter);
            quoteCharacter(model.quoteCharacter);
        }

        public CSVOutput build() {
            return new com.amazonaws.s3.model.CSVOutput(this);
        }

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
        public final Builder quoteFields(QuoteFields quoteFields) {
            this.quoteFields = quoteFields;
            return this;
        }

        /**
         * <p>The single character used for escaping the quote character inside an already escaped
         *          value.</p>
         */
        public final Builder quoteEscapeCharacter(String quoteEscapeCharacter) {
            this.quoteEscapeCharacter = quoteEscapeCharacter;
            return this;
        }

        /**
         * <p>A single character used to separate individual records in the output. Instead of the
         *          default value, you can specify an arbitrary delimiter.</p>
         */
        public final Builder recordDelimiter(String recordDelimiter) {
            this.recordDelimiter = recordDelimiter;
            return this;
        }

        /**
         * <p>The value used to separate individual fields in a record. You can specify an arbitrary
         *          delimiter.</p>
         */
        public final Builder fieldDelimiter(String fieldDelimiter) {
            this.fieldDelimiter = fieldDelimiter;
            return this;
        }

        /**
         * <p>A single character used for escaping when the field delimiter is part of the value. For
         *          example, if the value is <code>a, b</code>, Amazon S3 wraps this field value in quotation marks,
         *          as follows: <code>" a , b "</code>.</p>
         */
        public final Builder quoteCharacter(String quoteCharacter) {
            this.quoteCharacter = quoteCharacter;
            return this;
        }
    }
}

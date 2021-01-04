// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CSVInput {
    /**
     * <p>Describes the first line of input. Valid values are:</p>
     *          <ul>
     *             <li>
     *                <p>
     *                   <code>NONE</code>: First line is not a header.</p>
     *             </li>
     *             <li>
     *                <p>
     *                   <code>IGNORE</code>: First line is a header, but you can't use the header values
     *                to indicate the column in an expression. You can use column position (such as _1, _2,
     *                …) to indicate the column (<code>SELECT s._1 FROM OBJECT s</code>).</p>
     *             </li>
     *             <li>
     *                <p>
     *                   <code>Use</code>: First line is a header, and you can use the header value to
     *                identify a column in an expression (<code>SELECT "name" FROM OBJECT</code>). </p>
     *             </li>
     *          </ul>
     */
    FileHeaderInfo fileHeaderInfo;

    /**
     * <p>A single character used to indicate that a row should be ignored when the character is
     *          present at the start of that row. You can specify any character to indicate a comment
     *          line.</p>
     */
    String comments;

    /**
     * <p>A single character used for escaping the quotation mark character inside an already
     *          escaped value. For example, the value """ a , b """ is parsed as " a , b ".</p>
     */
    String quoteEscapeCharacter;

    /**
     * <p>A single character used to separate individual records in the input. Instead of the
     *          default value, you can specify an arbitrary delimiter.</p>
     */
    String recordDelimiter;

    /**
     * <p>A single character used to separate individual fields in a record. You can specify an
     *          arbitrary delimiter.</p>
     */
    String fieldDelimiter;

    /**
     * <p>A single character used for escaping when the field delimiter is part of the value. For
     *          example, if the value is <code>a, b</code>, Amazon S3 wraps this field value in quotation marks,
     *          as follows: <code>" a , b "</code>.</p>
     *          <p>Type: String</p>
     *          <p>Default: <code>"</code>
     *          </p>
     *          <p>Ancestors: <code>CSV</code>
     *          </p>
     */
    String quoteCharacter;

    /**
     * <p>Specifies that CSV field values may contain quoted record delimiters and such records
     *          should be allowed. Default value is FALSE. Setting this value to TRUE may lower
     *          performance.</p>
     */
    Boolean allowQuotedRecordDelimiter;

    CSVInput() {
        this.fileHeaderInfo = null;
        this.comments = "";
        this.quoteEscapeCharacter = "";
        this.recordDelimiter = "";
        this.fieldDelimiter = "";
        this.quoteCharacter = "";
        this.allowQuotedRecordDelimiter = null;
    }

    protected CSVInput(BuilderImpl builder) {
        this.fileHeaderInfo = builder.fileHeaderInfo;
        this.comments = builder.comments;
        this.quoteEscapeCharacter = builder.quoteEscapeCharacter;
        this.recordDelimiter = builder.recordDelimiter;
        this.fieldDelimiter = builder.fieldDelimiter;
        this.quoteCharacter = builder.quoteCharacter;
        this.allowQuotedRecordDelimiter = builder.allowQuotedRecordDelimiter;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CSVInput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CSVInput);
    }

    public FileHeaderInfo fileHeaderInfo() {
        return fileHeaderInfo;
    }

    public String comments() {
        return comments;
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

    public Boolean allowQuotedRecordDelimiter() {
        return allowQuotedRecordDelimiter;
    }

    public void setFileHeaderInfo(final FileHeaderInfo fileHeaderInfo) {
        this.fileHeaderInfo = fileHeaderInfo;
    }

    public void setComments(final String comments) {
        this.comments = comments;
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

    public void setAllowQuotedRecordDelimiter(final Boolean allowQuotedRecordDelimiter) {
        this.allowQuotedRecordDelimiter = allowQuotedRecordDelimiter;
    }

    public interface Builder {
        Builder fileHeaderInfo(FileHeaderInfo fileHeaderInfo);

        Builder comments(String comments);

        Builder quoteEscapeCharacter(String quoteEscapeCharacter);

        Builder recordDelimiter(String recordDelimiter);

        Builder fieldDelimiter(String fieldDelimiter);

        Builder quoteCharacter(String quoteCharacter);

        Builder allowQuotedRecordDelimiter(Boolean allowQuotedRecordDelimiter);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Describes the first line of input. Valid values are:</p>
         *          <ul>
         *             <li>
         *                <p>
         *                   <code>NONE</code>: First line is not a header.</p>
         *             </li>
         *             <li>
         *                <p>
         *                   <code>IGNORE</code>: First line is a header, but you can't use the header values
         *                to indicate the column in an expression. You can use column position (such as _1, _2,
         *                …) to indicate the column (<code>SELECT s._1 FROM OBJECT s</code>).</p>
         *             </li>
         *             <li>
         *                <p>
         *                   <code>Use</code>: First line is a header, and you can use the header value to
         *                identify a column in an expression (<code>SELECT "name" FROM OBJECT</code>). </p>
         *             </li>
         *          </ul>
         */
        FileHeaderInfo fileHeaderInfo;

        /**
         * <p>A single character used to indicate that a row should be ignored when the character is
         *          present at the start of that row. You can specify any character to indicate a comment
         *          line.</p>
         */
        String comments;

        /**
         * <p>A single character used for escaping the quotation mark character inside an already
         *          escaped value. For example, the value """ a , b """ is parsed as " a , b ".</p>
         */
        String quoteEscapeCharacter;

        /**
         * <p>A single character used to separate individual records in the input. Instead of the
         *          default value, you can specify an arbitrary delimiter.</p>
         */
        String recordDelimiter;

        /**
         * <p>A single character used to separate individual fields in a record. You can specify an
         *          arbitrary delimiter.</p>
         */
        String fieldDelimiter;

        /**
         * <p>A single character used for escaping when the field delimiter is part of the value. For
         *          example, if the value is <code>a, b</code>, Amazon S3 wraps this field value in quotation marks,
         *          as follows: <code>" a , b "</code>.</p>
         *          <p>Type: String</p>
         *          <p>Default: <code>"</code>
         *          </p>
         *          <p>Ancestors: <code>CSV</code>
         *          </p>
         */
        String quoteCharacter;

        /**
         * <p>Specifies that CSV field values may contain quoted record delimiters and such records
         *          should be allowed. Default value is FALSE. Setting this value to TRUE may lower
         *          performance.</p>
         */
        Boolean allowQuotedRecordDelimiter;

        protected BuilderImpl() {
        }

        private BuilderImpl(CSVInput model) {
            fileHeaderInfo(model.fileHeaderInfo);
            comments(model.comments);
            quoteEscapeCharacter(model.quoteEscapeCharacter);
            recordDelimiter(model.recordDelimiter);
            fieldDelimiter(model.fieldDelimiter);
            quoteCharacter(model.quoteCharacter);
            allowQuotedRecordDelimiter(model.allowQuotedRecordDelimiter);
        }

        public CSVInput build() {
            return new CSVInput(this);
        }

        public final Builder fileHeaderInfo(FileHeaderInfo fileHeaderInfo) {
            this.fileHeaderInfo = fileHeaderInfo;
            return this;
        }

        public final Builder comments(String comments) {
            this.comments = comments;
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

        public final Builder allowQuotedRecordDelimiter(Boolean allowQuotedRecordDelimiter) {
            this.allowQuotedRecordDelimiter = allowQuotedRecordDelimiter;
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

        public FileHeaderInfo fileHeaderInfo() {
            return fileHeaderInfo;
        }

        public String comments() {
            return comments;
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

        public Boolean allowQuotedRecordDelimiter() {
            return allowQuotedRecordDelimiter;
        }

        public void setFileHeaderInfo(final FileHeaderInfo fileHeaderInfo) {
            this.fileHeaderInfo = fileHeaderInfo;
        }

        public void setComments(final String comments) {
            this.comments = comments;
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

        public void setAllowQuotedRecordDelimiter(final Boolean allowQuotedRecordDelimiter) {
            this.allowQuotedRecordDelimiter = allowQuotedRecordDelimiter;
        }
    }
}

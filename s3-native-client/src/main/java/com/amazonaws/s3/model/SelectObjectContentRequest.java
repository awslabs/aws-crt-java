// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SelectObjectContentRequest {
    private String bucket;

    private String key;

    private String sSECustomerAlgorithm;

    private String sSECustomerKey;

    private String sSECustomerKeyMD5;

    private String expression;

    private ExpressionType expressionType;

    private RequestProgress requestProgress;

    private InputSerialization inputSerialization;

    private OutputSerialization outputSerialization;

    private ScanRange scanRange;

    private String expectedBucketOwner;

    private SelectObjectContentRequest() {
        this.bucket = null;
        this.key = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKey = null;
        this.sSECustomerKeyMD5 = null;
        this.expression = null;
        this.expressionType = null;
        this.requestProgress = null;
        this.inputSerialization = null;
        this.outputSerialization = null;
        this.scanRange = null;
        this.expectedBucketOwner = null;
    }

    private SelectObjectContentRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKey = builder.sSECustomerKey;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.expression = builder.expression;
        this.expressionType = builder.expressionType;
        this.requestProgress = builder.requestProgress;
        this.inputSerialization = builder.inputSerialization;
        this.outputSerialization = builder.outputSerialization;
        this.scanRange = builder.scanRange;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SelectObjectContentRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SelectObjectContentRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
        this.sSECustomerAlgorithm = sSECustomerAlgorithm;
    }

    public String sSECustomerKey() {
        return sSECustomerKey;
    }

    public void setSSECustomerKey(final String sSECustomerKey) {
        this.sSECustomerKey = sSECustomerKey;
    }

    public String sSECustomerKeyMD5() {
        return sSECustomerKeyMD5;
    }

    public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
        this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
    }

    public String expression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public ExpressionType expressionType() {
        return expressionType;
    }

    public void setExpressionType(final ExpressionType expressionType) {
        this.expressionType = expressionType;
    }

    public RequestProgress requestProgress() {
        return requestProgress;
    }

    public void setRequestProgress(final RequestProgress requestProgress) {
        this.requestProgress = requestProgress;
    }

    public InputSerialization inputSerialization() {
        return inputSerialization;
    }

    public void setInputSerialization(final InputSerialization inputSerialization) {
        this.inputSerialization = inputSerialization;
    }

    public OutputSerialization outputSerialization() {
        return outputSerialization;
    }

    public void setOutputSerialization(final OutputSerialization outputSerialization) {
        this.outputSerialization = outputSerialization;
    }

    public ScanRange scanRange() {
        return scanRange;
    }

    public void setScanRange(final ScanRange scanRange) {
        this.scanRange = scanRange;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String key;

        private String sSECustomerAlgorithm;

        private String sSECustomerKey;

        private String sSECustomerKeyMD5;

        private String expression;

        private ExpressionType expressionType;

        private RequestProgress requestProgress;

        private InputSerialization inputSerialization;

        private OutputSerialization outputSerialization;

        private ScanRange scanRange;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(SelectObjectContentRequest model) {
            bucket(model.bucket);
            key(model.key);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKey(model.sSECustomerKey);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            expression(model.expression);
            expressionType(model.expressionType);
            requestProgress(model.requestProgress);
            inputSerialization(model.inputSerialization);
            outputSerialization(model.outputSerialization);
            scanRange(model.scanRange);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public SelectObjectContentRequest build() {
            return new com.amazonaws.s3.model.SelectObjectContentRequest(this);
        }

        /**
         * <p>The S3 bucket.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The object key.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>The SSE Algorithm used to encrypt the object. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ServerSideEncryptionCustomerKeys.html">Server-Side Encryption (Using Customer-Provided Encryption Keys</a>. </p>
         */
        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
            return this;
        }

        /**
         * <p>The SSE Customer Key. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ServerSideEncryptionCustomerKeys.html">Server-Side Encryption
         *             (Using Customer-Provided Encryption Keys</a>. </p>
         */
        public final Builder sSECustomerKey(String sSECustomerKey) {
            this.sSECustomerKey = sSECustomerKey;
            return this;
        }

        /**
         * <p>The SSE Customer Key MD5. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ServerSideEncryptionCustomerKeys.html">Server-Side Encryption
         *             (Using Customer-Provided Encryption Keys</a>. </p>
         */
        public final Builder sSECustomerKeyMD5(String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
            return this;
        }

        /**
         * <p>The expression that is used to query the object.</p>
         */
        public final Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        /**
         * <p>The type of the provided expression (for example, SQL).</p>
         */
        public final Builder expressionType(ExpressionType expressionType) {
            this.expressionType = expressionType;
            return this;
        }

        /**
         * <p>Specifies if periodic request progress information should be enabled.</p>
         */
        public final Builder requestProgress(RequestProgress requestProgress) {
            this.requestProgress = requestProgress;
            return this;
        }

        /**
         * <p>Describes the format of the data in the object that is being queried.</p>
         */
        public final Builder inputSerialization(InputSerialization inputSerialization) {
            this.inputSerialization = inputSerialization;
            return this;
        }

        /**
         * <p>Describes the format of the data that you want Amazon S3 to return in response.</p>
         */
        public final Builder outputSerialization(OutputSerialization outputSerialization) {
            this.outputSerialization = outputSerialization;
            return this;
        }

        /**
         * <p>Specifies the byte range of the object to get the records from. A record is processed
         *          when its first byte is contained by the range. This parameter is optional, but when
         *          specified, it must not be empty. See RFC 2616, Section 14.35.1 about how to specify the
         *          start and end of the range.</p>
         *          <p>
         *             <code>ScanRange</code>may be used in the following ways:</p>
         *          <ul>
         *             <li>
         *                <p>
         *                   <code><scanrange><start>50</start><end>100</end></scanrange></code>
         *                - process only the records starting between the bytes 50 and 100 (inclusive, counting
         *                from zero)</p>
         *             </li>
         *             <li>
         *                <p>
         *                   <code><scanrange><start>50</start></scanrange></code> -
         *                process only the records starting after the byte 50</p>
         *             </li>
         *             <li>
         *                <p>
         *                   <code><scanrange><end>50</end></scanrange></code> -
         *                process only the records within the last 50 bytes of the file.</p>
         *             </li>
         *          </ul>
         */
        public final Builder scanRange(ScanRange scanRange) {
            this.scanRange = scanRange;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}

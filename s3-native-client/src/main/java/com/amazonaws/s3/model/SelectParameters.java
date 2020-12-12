// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SelectParameters {
    private InputSerialization inputSerialization;

    private ExpressionType expressionType;

    private String expression;

    private OutputSerialization outputSerialization;

    private SelectParameters() {
        this.inputSerialization = null;
        this.expressionType = null;
        this.expression = null;
        this.outputSerialization = null;
    }

    private SelectParameters(Builder builder) {
        this.inputSerialization = builder.inputSerialization;
        this.expressionType = builder.expressionType;
        this.expression = builder.expression;
        this.outputSerialization = builder.outputSerialization;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SelectParameters.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SelectParameters);
    }

    public InputSerialization inputSerialization() {
        return inputSerialization;
    }

    public void setInputSerialization(final InputSerialization inputSerialization) {
        this.inputSerialization = inputSerialization;
    }

    public ExpressionType expressionType() {
        return expressionType;
    }

    public void setExpressionType(final ExpressionType expressionType) {
        this.expressionType = expressionType;
    }

    public String expression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public OutputSerialization outputSerialization() {
        return outputSerialization;
    }

    public void setOutputSerialization(final OutputSerialization outputSerialization) {
        this.outputSerialization = outputSerialization;
    }

    static final class Builder {
        private InputSerialization inputSerialization;

        private ExpressionType expressionType;

        private String expression;

        private OutputSerialization outputSerialization;

        private Builder() {
        }

        private Builder(SelectParameters model) {
            inputSerialization(model.inputSerialization);
            expressionType(model.expressionType);
            expression(model.expression);
            outputSerialization(model.outputSerialization);
        }

        public SelectParameters build() {
            return new com.amazonaws.s3.model.SelectParameters(this);
        }

        /**
         * <p>Describes the serialization format of the object.</p>
         */
        public final Builder inputSerialization(InputSerialization inputSerialization) {
            this.inputSerialization = inputSerialization;
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
         * <p>The expression that is used to query the object.</p>
         */
        public final Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        /**
         * <p>Describes how the results of the Select job are serialized.</p>
         */
        public final Builder outputSerialization(OutputSerialization outputSerialization) {
            this.outputSerialization = outputSerialization;
            return this;
        }
    }
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SelectParameters {
    /**
     * <p>Describes the serialization format of the object.</p>
     */
    InputSerialization inputSerialization;

    /**
     * <p>The type of the provided expression (for example, SQL).</p>
     */
    ExpressionType expressionType;

    /**
     * <p>The expression that is used to query the object.</p>
     */
    String expression;

    /**
     * <p>Describes how the results of the Select job are serialized.</p>
     */
    OutputSerialization outputSerialization;

    SelectParameters() {
        this.inputSerialization = null;
        this.expressionType = null;
        this.expression = "";
        this.outputSerialization = null;
    }

    protected SelectParameters(BuilderImpl builder) {
        this.inputSerialization = builder.inputSerialization;
        this.expressionType = builder.expressionType;
        this.expression = builder.expression;
        this.outputSerialization = builder.outputSerialization;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public ExpressionType expressionType() {
        return expressionType;
    }

    public String expression() {
        return expression;
    }

    public OutputSerialization outputSerialization() {
        return outputSerialization;
    }

    public void setInputSerialization(final InputSerialization inputSerialization) {
        this.inputSerialization = inputSerialization;
    }

    public void setExpressionType(final ExpressionType expressionType) {
        this.expressionType = expressionType;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public void setOutputSerialization(final OutputSerialization outputSerialization) {
        this.outputSerialization = outputSerialization;
    }

    public interface Builder {
        Builder inputSerialization(InputSerialization inputSerialization);

        Builder expressionType(ExpressionType expressionType);

        Builder expression(String expression);

        Builder outputSerialization(OutputSerialization outputSerialization);

        SelectParameters build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Describes the serialization format of the object.</p>
         */
        InputSerialization inputSerialization;

        /**
         * <p>The type of the provided expression (for example, SQL).</p>
         */
        ExpressionType expressionType;

        /**
         * <p>The expression that is used to query the object.</p>
         */
        String expression;

        /**
         * <p>Describes how the results of the Select job are serialized.</p>
         */
        OutputSerialization outputSerialization;

        protected BuilderImpl() {
        }

        private BuilderImpl(SelectParameters model) {
            inputSerialization(model.inputSerialization);
            expressionType(model.expressionType);
            expression(model.expression);
            outputSerialization(model.outputSerialization);
        }

        public SelectParameters build() {
            return new SelectParameters(this);
        }

        public final Builder inputSerialization(InputSerialization inputSerialization) {
            this.inputSerialization = inputSerialization;
            return this;
        }

        public final Builder expressionType(ExpressionType expressionType) {
            this.expressionType = expressionType;
            return this;
        }

        public final Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public final Builder outputSerialization(OutputSerialization outputSerialization) {
            this.outputSerialization = outputSerialization;
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

        public InputSerialization inputSerialization() {
            return inputSerialization;
        }

        public ExpressionType expressionType() {
            return expressionType;
        }

        public String expression() {
            return expression;
        }

        public OutputSerialization outputSerialization() {
            return outputSerialization;
        }

        public void setInputSerialization(final InputSerialization inputSerialization) {
            this.inputSerialization = inputSerialization;
        }

        public void setExpressionType(final ExpressionType expressionType) {
            this.expressionType = expressionType;
        }

        public void setExpression(final String expression) {
            this.expression = expression;
        }

        public void setOutputSerialization(final OutputSerialization outputSerialization) {
            this.outputSerialization = outputSerialization;
        }
    }
}

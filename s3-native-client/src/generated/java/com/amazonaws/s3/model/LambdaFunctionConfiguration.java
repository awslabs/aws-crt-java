// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class LambdaFunctionConfiguration {
    String id;

    /**
     * <p>The Amazon Resource Name (ARN) of the AWS Lambda function that Amazon S3 invokes when the
     *          specified event type occurs.</p>
     */
    String lambdaFunctionArn;

    /**
     * <p>The Amazon S3 bucket event for which to invoke the AWS Lambda function. For more information,
     *          see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Supported
     *             Event Types</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    List<Event> events;

    NotificationConfigurationFilter filter;

    LambdaFunctionConfiguration() {
        this.id = "";
        this.lambdaFunctionArn = "";
        this.events = null;
        this.filter = null;
    }

    protected LambdaFunctionConfiguration(BuilderImpl builder) {
        this.id = builder.id;
        this.lambdaFunctionArn = builder.lambdaFunctionArn;
        this.events = builder.events;
        this.filter = builder.filter;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(LambdaFunctionConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LambdaFunctionConfiguration);
    }

    public String id() {
        return id;
    }

    public String lambdaFunctionArn() {
        return lambdaFunctionArn;
    }

    public List<Event> events() {
        return events;
    }

    public NotificationConfigurationFilter filter() {
        return filter;
    }

    public interface Builder {
        Builder id(String id);

        Builder lambdaFunctionArn(String lambdaFunctionArn);

        Builder events(List<Event> events);

        Builder filter(NotificationConfigurationFilter filter);

        LambdaFunctionConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        String id;

        /**
         * <p>The Amazon Resource Name (ARN) of the AWS Lambda function that Amazon S3 invokes when the
         *          specified event type occurs.</p>
         */
        String lambdaFunctionArn;

        /**
         * <p>The Amazon S3 bucket event for which to invoke the AWS Lambda function. For more information,
         *          see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Supported
         *             Event Types</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        List<Event> events;

        NotificationConfigurationFilter filter;

        protected BuilderImpl() {
        }

        private BuilderImpl(LambdaFunctionConfiguration model) {
            id(model.id);
            lambdaFunctionArn(model.lambdaFunctionArn);
            events(model.events);
            filter(model.filter);
        }

        public LambdaFunctionConfiguration build() {
            return new LambdaFunctionConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder lambdaFunctionArn(String lambdaFunctionArn) {
            this.lambdaFunctionArn = lambdaFunctionArn;
            return this;
        }

        public final Builder events(List<Event> events) {
            this.events = events;
            return this;
        }

        public final Builder filter(NotificationConfigurationFilter filter) {
            this.filter = filter;
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

        public String id() {
            return id;
        }

        public String lambdaFunctionArn() {
            return lambdaFunctionArn;
        }

        public List<Event> events() {
            return events;
        }

        public NotificationConfigurationFilter filter() {
            return filter;
        }
    }
}

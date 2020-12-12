// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class LambdaFunctionConfiguration {
    private String id;

    private String lambdaFunctionArn;

    private List<Event> events;

    private NotificationConfigurationFilter filter;

    private LambdaFunctionConfiguration() {
        this.id = null;
        this.lambdaFunctionArn = null;
        this.events = null;
        this.filter = null;
    }

    private LambdaFunctionConfiguration(Builder builder) {
        this.id = builder.id;
        this.lambdaFunctionArn = builder.lambdaFunctionArn;
        this.events = builder.events;
        this.filter = builder.filter;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setId(final String id) {
        this.id = id;
    }

    public String lambdaFunctionArn() {
        return lambdaFunctionArn;
    }

    public void setLambdaFunctionArn(final String lambdaFunctionArn) {
        this.lambdaFunctionArn = lambdaFunctionArn;
    }

    public List<Event> events() {
        return events;
    }

    public void setEvents(final List<Event> events) {
        this.events = events;
    }

    public NotificationConfigurationFilter filter() {
        return filter;
    }

    public void setFilter(final NotificationConfigurationFilter filter) {
        this.filter = filter;
    }

    static final class Builder {
        private String id;

        private String lambdaFunctionArn;

        private List<Event> events;

        private NotificationConfigurationFilter filter;

        private Builder() {
        }

        private Builder(LambdaFunctionConfiguration model) {
            id(model.id);
            lambdaFunctionArn(model.lambdaFunctionArn);
            events(model.events);
            filter(model.filter);
        }

        public LambdaFunctionConfiguration build() {
            return new com.amazonaws.s3.model.LambdaFunctionConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>The Amazon Resource Name (ARN) of the AWS Lambda function that Amazon S3 invokes when the
         *          specified event type occurs.</p>
         */
        public final Builder lambdaFunctionArn(String lambdaFunctionArn) {
            this.lambdaFunctionArn = lambdaFunctionArn;
            return this;
        }

        /**
         * <p>The Amazon S3 bucket event for which to invoke the AWS Lambda function. For more information,
         *          see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Supported
         *             Event Types</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder events(List<Event> events) {
            this.events = events;
            return this;
        }

        public final Builder filter(NotificationConfigurationFilter filter) {
            this.filter = filter;
            return this;
        }
    }
}

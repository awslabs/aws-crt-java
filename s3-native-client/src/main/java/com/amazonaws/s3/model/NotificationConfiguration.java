// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NotificationConfiguration {
    private List<TopicConfiguration> topicConfigurations;

    private List<QueueConfiguration> queueConfigurations;

    private List<LambdaFunctionConfiguration> lambdaFunctionConfigurations;

    private NotificationConfiguration() {
        this.topicConfigurations = null;
        this.queueConfigurations = null;
        this.lambdaFunctionConfigurations = null;
    }

    private NotificationConfiguration(Builder builder) {
        this.topicConfigurations = builder.topicConfigurations;
        this.queueConfigurations = builder.queueConfigurations;
        this.lambdaFunctionConfigurations = builder.lambdaFunctionConfigurations;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(NotificationConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof NotificationConfiguration);
    }

    public List<TopicConfiguration> topicConfigurations() {
        return topicConfigurations;
    }

    public void setTopicConfigurations(final List<TopicConfiguration> topicConfigurations) {
        this.topicConfigurations = topicConfigurations;
    }

    public List<QueueConfiguration> queueConfigurations() {
        return queueConfigurations;
    }

    public void setQueueConfigurations(final List<QueueConfiguration> queueConfigurations) {
        this.queueConfigurations = queueConfigurations;
    }

    public List<LambdaFunctionConfiguration> lambdaFunctionConfigurations() {
        return lambdaFunctionConfigurations;
    }

    public void setLambdaFunctionConfigurations(
            final List<LambdaFunctionConfiguration> lambdaFunctionConfigurations) {
        this.lambdaFunctionConfigurations = lambdaFunctionConfigurations;
    }

    static final class Builder {
        private List<TopicConfiguration> topicConfigurations;

        private List<QueueConfiguration> queueConfigurations;

        private List<LambdaFunctionConfiguration> lambdaFunctionConfigurations;

        private Builder() {
        }

        private Builder(NotificationConfiguration model) {
            topicConfigurations(model.topicConfigurations);
            queueConfigurations(model.queueConfigurations);
            lambdaFunctionConfigurations(model.lambdaFunctionConfigurations);
        }

        public NotificationConfiguration build() {
            return new com.amazonaws.s3.model.NotificationConfiguration(this);
        }

        /**
         * <p>The topic to which notifications are sent and the events for which notifications are
         *          generated.</p>
         */
        public final Builder topicConfigurations(List<TopicConfiguration> topicConfigurations) {
            this.topicConfigurations = topicConfigurations;
            return this;
        }

        /**
         * <p>The Amazon Simple Queue Service queues to publish messages to and the events for which
         *          to publish messages.</p>
         */
        public final Builder queueConfigurations(List<QueueConfiguration> queueConfigurations) {
            this.queueConfigurations = queueConfigurations;
            return this;
        }

        /**
         * <p>Describes the AWS Lambda functions to invoke and the events for which to invoke
         *          them.</p>
         */
        public final Builder lambdaFunctionConfigurations(
                List<LambdaFunctionConfiguration> lambdaFunctionConfigurations) {
            this.lambdaFunctionConfigurations = lambdaFunctionConfigurations;
            return this;
        }
    }
}

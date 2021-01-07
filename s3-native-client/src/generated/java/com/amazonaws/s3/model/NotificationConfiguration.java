// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NotificationConfiguration {
    /**
     * <p>The topic to which notifications are sent and the events for which notifications are
     *          generated.</p>
     */
    List<TopicConfiguration> topicConfigurations;

    /**
     * <p>The Amazon Simple Queue Service queues to publish messages to and the events for which
     *          to publish messages.</p>
     */
    List<QueueConfiguration> queueConfigurations;

    /**
     * <p>Describes the AWS Lambda functions to invoke and the events for which to invoke
     *          them.</p>
     */
    List<LambdaFunctionConfiguration> lambdaFunctionConfigurations;

    NotificationConfiguration() {
        this.topicConfigurations = null;
        this.queueConfigurations = null;
        this.lambdaFunctionConfigurations = null;
    }

    protected NotificationConfiguration(BuilderImpl builder) {
        this.topicConfigurations = builder.topicConfigurations;
        this.queueConfigurations = builder.queueConfigurations;
        this.lambdaFunctionConfigurations = builder.lambdaFunctionConfigurations;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public List<QueueConfiguration> queueConfigurations() {
        return queueConfigurations;
    }

    public List<LambdaFunctionConfiguration> lambdaFunctionConfigurations() {
        return lambdaFunctionConfigurations;
    }

    public void setTopicConfigurations(final List<TopicConfiguration> topicConfigurations) {
        this.topicConfigurations = topicConfigurations;
    }

    public void setQueueConfigurations(final List<QueueConfiguration> queueConfigurations) {
        this.queueConfigurations = queueConfigurations;
    }

    public void setLambdaFunctionConfigurations(
            final List<LambdaFunctionConfiguration> lambdaFunctionConfigurations) {
        this.lambdaFunctionConfigurations = lambdaFunctionConfigurations;
    }

    public interface Builder {
        Builder topicConfigurations(List<TopicConfiguration> topicConfigurations);

        Builder queueConfigurations(List<QueueConfiguration> queueConfigurations);

        Builder lambdaFunctionConfigurations(
                List<LambdaFunctionConfiguration> lambdaFunctionConfigurations);

        NotificationConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The topic to which notifications are sent and the events for which notifications are
         *          generated.</p>
         */
        List<TopicConfiguration> topicConfigurations;

        /**
         * <p>The Amazon Simple Queue Service queues to publish messages to and the events for which
         *          to publish messages.</p>
         */
        List<QueueConfiguration> queueConfigurations;

        /**
         * <p>Describes the AWS Lambda functions to invoke and the events for which to invoke
         *          them.</p>
         */
        List<LambdaFunctionConfiguration> lambdaFunctionConfigurations;

        protected BuilderImpl() {
        }

        private BuilderImpl(NotificationConfiguration model) {
            topicConfigurations(model.topicConfigurations);
            queueConfigurations(model.queueConfigurations);
            lambdaFunctionConfigurations(model.lambdaFunctionConfigurations);
        }

        public NotificationConfiguration build() {
            return new NotificationConfiguration(this);
        }

        public final Builder topicConfigurations(List<TopicConfiguration> topicConfigurations) {
            this.topicConfigurations = topicConfigurations;
            return this;
        }

        public final Builder queueConfigurations(List<QueueConfiguration> queueConfigurations) {
            this.queueConfigurations = queueConfigurations;
            return this;
        }

        public final Builder lambdaFunctionConfigurations(
                List<LambdaFunctionConfiguration> lambdaFunctionConfigurations) {
            this.lambdaFunctionConfigurations = lambdaFunctionConfigurations;
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

        public List<TopicConfiguration> topicConfigurations() {
            return topicConfigurations;
        }

        public List<QueueConfiguration> queueConfigurations() {
            return queueConfigurations;
        }

        public List<LambdaFunctionConfiguration> lambdaFunctionConfigurations() {
            return lambdaFunctionConfigurations;
        }

        public void setTopicConfigurations(final List<TopicConfiguration> topicConfigurations) {
            this.topicConfigurations = topicConfigurations;
        }

        public void setQueueConfigurations(final List<QueueConfiguration> queueConfigurations) {
            this.queueConfigurations = queueConfigurations;
        }

        public void setLambdaFunctionConfigurations(
                final List<LambdaFunctionConfiguration> lambdaFunctionConfigurations) {
            this.lambdaFunctionConfigurations = lambdaFunctionConfigurations;
        }
    }
}

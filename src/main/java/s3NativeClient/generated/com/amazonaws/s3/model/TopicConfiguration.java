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
public class TopicConfiguration {
    String id;

    /**
     * <p>The Amazon Resource Name (ARN) of the Amazon SNS topic to which Amazon S3 publishes a message
     *          when it detects events of the specified type.</p>
     */
    String topicArn;

    /**
     * <p>The Amazon S3 bucket event about which to send notifications. For more information, see
     *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Supported
     *             Event Types</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    List<Event> events;

    NotificationConfigurationFilter filter;

    TopicConfiguration() {
        this.id = "";
        this.topicArn = "";
        this.events = null;
        this.filter = null;
    }

    protected TopicConfiguration(BuilderImpl builder) {
        this.id = builder.id;
        this.topicArn = builder.topicArn;
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
        return Objects.hash(TopicConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof TopicConfiguration);
    }

    public String id() {
        return id;
    }

    public String topicArn() {
        return topicArn;
    }

    public List<Event> events() {
        return events;
    }

    public NotificationConfigurationFilter filter() {
        return filter;
    }

    public interface Builder {
        Builder id(String id);

        Builder topicArn(String topicArn);

        Builder events(List<Event> events);

        Builder filter(NotificationConfigurationFilter filter);

        TopicConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        String id;

        /**
         * <p>The Amazon Resource Name (ARN) of the Amazon SNS topic to which Amazon S3 publishes a message
         *          when it detects events of the specified type.</p>
         */
        String topicArn;

        /**
         * <p>The Amazon S3 bucket event about which to send notifications. For more information, see
         *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Supported
         *             Event Types</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        List<Event> events;

        NotificationConfigurationFilter filter;

        protected BuilderImpl() {
        }

        private BuilderImpl(TopicConfiguration model) {
            id(model.id);
            topicArn(model.topicArn);
            events(model.events);
            filter(model.filter);
        }

        public TopicConfiguration build() {
            return new TopicConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder topicArn(String topicArn) {
            this.topicArn = topicArn;
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

        public String topicArn() {
            return topicArn;
        }

        public List<Event> events() {
            return events;
        }

        public NotificationConfigurationFilter filter() {
            return filter;
        }
    }
}

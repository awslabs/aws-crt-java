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
public class TopicConfiguration {
    private String id;

    private String topicArn;

    private List<Event> events;

    private NotificationConfigurationFilter filter;

    private TopicConfiguration() {
        this.id = null;
        this.topicArn = null;
        this.events = null;
        this.filter = null;
    }

    private TopicConfiguration(Builder builder) {
        this.id = builder.id;
        this.topicArn = builder.topicArn;
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

    public void setId(final String id) {
        this.id = id;
    }

    public String topicArn() {
        return topicArn;
    }

    public void setTopicArn(final String topicArn) {
        this.topicArn = topicArn;
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

        private String topicArn;

        private List<Event> events;

        private NotificationConfigurationFilter filter;

        private Builder() {
        }

        private Builder(TopicConfiguration model) {
            id(model.id);
            topicArn(model.topicArn);
            events(model.events);
            filter(model.filter);
        }

        public TopicConfiguration build() {
            return new com.amazonaws.s3.model.TopicConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>The Amazon Resource Name (ARN) of the Amazon SNS topic to which Amazon S3 publishes a message
         *          when it detects events of the specified type.</p>
         */
        public final Builder topicArn(String topicArn) {
            this.topicArn = topicArn;
            return this;
        }

        /**
         * <p>The Amazon S3 bucket event about which to send notifications. For more information, see
         *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Supported
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

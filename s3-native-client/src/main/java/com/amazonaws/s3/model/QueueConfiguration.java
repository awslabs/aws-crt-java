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
public class QueueConfiguration {
    private String id;

    private String queueArn;

    private List<Event> events;

    private NotificationConfigurationFilter filter;

    private QueueConfiguration() {
        this.id = null;
        this.queueArn = null;
        this.events = null;
        this.filter = null;
    }

    private QueueConfiguration(Builder builder) {
        this.id = builder.id;
        this.queueArn = builder.queueArn;
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
        return Objects.hash(QueueConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof QueueConfiguration);
    }

    public String id() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String queueArn() {
        return queueArn;
    }

    public void setQueueArn(final String queueArn) {
        this.queueArn = queueArn;
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

        private String queueArn;

        private List<Event> events;

        private NotificationConfigurationFilter filter;

        private Builder() {
        }

        private Builder(QueueConfiguration model) {
            id(model.id);
            queueArn(model.queueArn);
            events(model.events);
            filter(model.filter);
        }

        public QueueConfiguration build() {
            return new com.amazonaws.s3.model.QueueConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>The Amazon Resource Name (ARN) of the Amazon SQS queue to which Amazon S3 publishes a message
         *          when it detects events of the specified type.</p>
         */
        public final Builder queueArn(String queueArn) {
            this.queueArn = queueArn;
            return this;
        }

        /**
         * <p>A collection of bucket events for which to send notifications</p>
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

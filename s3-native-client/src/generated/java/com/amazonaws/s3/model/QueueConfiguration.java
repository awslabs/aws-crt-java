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
public class QueueConfiguration {
    String id;

    /**
     * <p>The Amazon Resource Name (ARN) of the Amazon SQS queue to which Amazon S3 publishes a message
     *          when it detects events of the specified type.</p>
     */
    String queueArn;

    /**
     * <p>A collection of bucket events for which to send notifications</p>
     */
    List<Event> events;

    NotificationConfigurationFilter filter;

    QueueConfiguration() {
        this.id = "";
        this.queueArn = "";
        this.events = null;
        this.filter = null;
    }

    protected QueueConfiguration(BuilderImpl builder) {
        this.id = builder.id;
        this.queueArn = builder.queueArn;
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

    public String queueArn() {
        return queueArn;
    }

    public List<Event> events() {
        return events;
    }

    public NotificationConfigurationFilter filter() {
        return filter;
    }

    public interface Builder {
        Builder id(String id);

        Builder queueArn(String queueArn);

        Builder events(List<Event> events);

        Builder filter(NotificationConfigurationFilter filter);

        QueueConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        String id;

        /**
         * <p>The Amazon Resource Name (ARN) of the Amazon SQS queue to which Amazon S3 publishes a message
         *          when it detects events of the specified type.</p>
         */
        String queueArn;

        /**
         * <p>A collection of bucket events for which to send notifications</p>
         */
        List<Event> events;

        NotificationConfigurationFilter filter;

        protected BuilderImpl() {
        }

        private BuilderImpl(QueueConfiguration model) {
            id(model.id);
            queueArn(model.queueArn);
            events(model.events);
            filter(model.filter);
        }

        public QueueConfiguration build() {
            return new QueueConfiguration(this);
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder queueArn(String queueArn) {
            this.queueArn = queueArn;
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

        public String queueArn() {
            return queueArn;
        }

        public List<Event> events() {
            return events;
        }

        public NotificationConfigurationFilter filter() {
            return filter;
        }
    }
}

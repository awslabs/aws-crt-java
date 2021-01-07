// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketNotificationConfigurationRequest {
    /**
     * <p>The name of the bucket.</p>
     */
    String bucket;

    NotificationConfiguration notificationConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketNotificationConfigurationRequest() {
        this.bucket = "";
        this.notificationConfiguration = null;
        this.expectedBucketOwner = "";
    }

    protected PutBucketNotificationConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.notificationConfiguration = builder.notificationConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutBucketNotificationConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketNotificationConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public NotificationConfiguration notificationConfiguration() {
        return notificationConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setNotificationConfiguration(
            final NotificationConfiguration notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder notificationConfiguration(NotificationConfiguration notificationConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);

        PutBucketNotificationConfigurationRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket.</p>
         */
        String bucket;

        NotificationConfiguration notificationConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketNotificationConfigurationRequest model) {
            bucket(model.bucket);
            notificationConfiguration(model.notificationConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketNotificationConfigurationRequest build() {
            return new PutBucketNotificationConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder notificationConfiguration(
                NotificationConfiguration notificationConfiguration) {
            this.notificationConfiguration = notificationConfiguration;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
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

        public String bucket() {
            return bucket;
        }

        public NotificationConfiguration notificationConfiguration() {
            return notificationConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setNotificationConfiguration(
                final NotificationConfiguration notificationConfiguration) {
            this.notificationConfiguration = notificationConfiguration;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}

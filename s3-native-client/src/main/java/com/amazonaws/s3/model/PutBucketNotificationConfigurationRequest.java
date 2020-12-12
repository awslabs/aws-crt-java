// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketNotificationConfigurationRequest {
    private String bucket;

    private NotificationConfiguration notificationConfiguration;

    private String expectedBucketOwner;

    private PutBucketNotificationConfigurationRequest() {
        this.bucket = null;
        this.notificationConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketNotificationConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.notificationConfiguration = builder.notificationConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public NotificationConfiguration notificationConfiguration() {
        return notificationConfiguration;
    }

    public void setNotificationConfiguration(
            final NotificationConfiguration notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private NotificationConfiguration notificationConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketNotificationConfigurationRequest model) {
            bucket(model.bucket);
            notificationConfiguration(model.notificationConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketNotificationConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutBucketNotificationConfigurationRequest(this);
        }

        /**
         * <p>The name of the bucket.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder notificationConfiguration(
                NotificationConfiguration notificationConfiguration) {
            this.notificationConfiguration = notificationConfiguration;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}

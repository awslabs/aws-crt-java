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
public class LoggingEnabled {
    private String targetBucket;

    private List<TargetGrant> targetGrants;

    private String targetPrefix;

    private LoggingEnabled() {
        this.targetBucket = null;
        this.targetGrants = null;
        this.targetPrefix = null;
    }

    private LoggingEnabled(Builder builder) {
        this.targetBucket = builder.targetBucket;
        this.targetGrants = builder.targetGrants;
        this.targetPrefix = builder.targetPrefix;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LoggingEnabled.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LoggingEnabled);
    }

    public String targetBucket() {
        return targetBucket;
    }

    public void setTargetBucket(final String targetBucket) {
        this.targetBucket = targetBucket;
    }

    public List<TargetGrant> targetGrants() {
        return targetGrants;
    }

    public void setTargetGrants(final List<TargetGrant> targetGrants) {
        this.targetGrants = targetGrants;
    }

    public String targetPrefix() {
        return targetPrefix;
    }

    public void setTargetPrefix(final String targetPrefix) {
        this.targetPrefix = targetPrefix;
    }

    static final class Builder {
        private String targetBucket;

        private List<TargetGrant> targetGrants;

        private String targetPrefix;

        private Builder() {
        }

        private Builder(LoggingEnabled model) {
            targetBucket(model.targetBucket);
            targetGrants(model.targetGrants);
            targetPrefix(model.targetPrefix);
        }

        public LoggingEnabled build() {
            return new com.amazonaws.s3.model.LoggingEnabled(this);
        }

        /**
         * <p>Specifies the bucket where you want Amazon S3 to store server access logs. You can have your
         *          logs delivered to any bucket that you own, including the same bucket that is being logged.
         *          You can also configure multiple buckets to deliver their logs to the same target bucket. In
         *          this case, you should choose a different <code>TargetPrefix</code> for each source bucket
         *          so that the delivered log files can be distinguished by key.</p>
         */
        public final Builder targetBucket(String targetBucket) {
            this.targetBucket = targetBucket;
            return this;
        }

        /**
         * <p>Container for granting information.</p>
         */
        public final Builder targetGrants(List<TargetGrant> targetGrants) {
            this.targetGrants = targetGrants;
            return this;
        }

        /**
         * <p>A prefix for all log object keys. If you store log files from multiple Amazon S3 buckets in a
         *          single bucket, you can use a prefix to distinguish which log files came from which
         *          bucket.</p>
         */
        public final Builder targetPrefix(String targetPrefix) {
            this.targetPrefix = targetPrefix;
            return this;
        }
    }
}

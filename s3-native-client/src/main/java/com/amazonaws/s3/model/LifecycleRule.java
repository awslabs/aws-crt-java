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
public class LifecycleRule {
    private LifecycleExpiration expiration;

    private String iD;

    private String prefix;

    private LifecycleRuleFilter filter;

    private ExpirationStatus status;

    private List<Transition> transitions;

    private List<NoncurrentVersionTransition> noncurrentVersionTransitions;

    private NoncurrentVersionExpiration noncurrentVersionExpiration;

    private AbortIncompleteMultipartUpload abortIncompleteMultipartUpload;

    private LifecycleRule() {
        this.expiration = null;
        this.iD = null;
        this.prefix = null;
        this.filter = null;
        this.status = null;
        this.transitions = null;
        this.noncurrentVersionTransitions = null;
        this.noncurrentVersionExpiration = null;
        this.abortIncompleteMultipartUpload = null;
    }

    private LifecycleRule(Builder builder) {
        this.expiration = builder.expiration;
        this.iD = builder.iD;
        this.prefix = builder.prefix;
        this.filter = builder.filter;
        this.status = builder.status;
        this.transitions = builder.transitions;
        this.noncurrentVersionTransitions = builder.noncurrentVersionTransitions;
        this.noncurrentVersionExpiration = builder.noncurrentVersionExpiration;
        this.abortIncompleteMultipartUpload = builder.abortIncompleteMultipartUpload;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LifecycleRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LifecycleRule);
    }

    public LifecycleExpiration expiration() {
        return expiration;
    }

    public void setExpiration(final LifecycleExpiration expiration) {
        this.expiration = expiration;
    }

    public String iD() {
        return iD;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public LifecycleRuleFilter filter() {
        return filter;
    }

    public void setFilter(final LifecycleRuleFilter filter) {
        this.filter = filter;
    }

    public ExpirationStatus status() {
        return status;
    }

    public void setStatus(final ExpirationStatus status) {
        this.status = status;
    }

    public List<Transition> transitions() {
        return transitions;
    }

    public void setTransitions(final List<Transition> transitions) {
        this.transitions = transitions;
    }

    public List<NoncurrentVersionTransition> noncurrentVersionTransitions() {
        return noncurrentVersionTransitions;
    }

    public void setNoncurrentVersionTransitions(
            final List<NoncurrentVersionTransition> noncurrentVersionTransitions) {
        this.noncurrentVersionTransitions = noncurrentVersionTransitions;
    }

    public NoncurrentVersionExpiration noncurrentVersionExpiration() {
        return noncurrentVersionExpiration;
    }

    public void setNoncurrentVersionExpiration(
            final NoncurrentVersionExpiration noncurrentVersionExpiration) {
        this.noncurrentVersionExpiration = noncurrentVersionExpiration;
    }

    public AbortIncompleteMultipartUpload abortIncompleteMultipartUpload() {
        return abortIncompleteMultipartUpload;
    }

    public void setAbortIncompleteMultipartUpload(
            final AbortIncompleteMultipartUpload abortIncompleteMultipartUpload) {
        this.abortIncompleteMultipartUpload = abortIncompleteMultipartUpload;
    }

    static final class Builder {
        private LifecycleExpiration expiration;

        private String iD;

        private String prefix;

        private LifecycleRuleFilter filter;

        private ExpirationStatus status;

        private List<Transition> transitions;

        private List<NoncurrentVersionTransition> noncurrentVersionTransitions;

        private NoncurrentVersionExpiration noncurrentVersionExpiration;

        private AbortIncompleteMultipartUpload abortIncompleteMultipartUpload;

        private Builder() {
        }

        private Builder(LifecycleRule model) {
            expiration(model.expiration);
            iD(model.iD);
            prefix(model.prefix);
            filter(model.filter);
            status(model.status);
            transitions(model.transitions);
            noncurrentVersionTransitions(model.noncurrentVersionTransitions);
            noncurrentVersionExpiration(model.noncurrentVersionExpiration);
            abortIncompleteMultipartUpload(model.abortIncompleteMultipartUpload);
        }

        public LifecycleRule build() {
            return new com.amazonaws.s3.model.LifecycleRule(this);
        }

        /**
         * <p>Specifies the expiration for the lifecycle of the object in the form of date, days and,
         *          whether the object has a delete marker.</p>
         */
        public final Builder expiration(LifecycleExpiration expiration) {
            this.expiration = expiration;
            return this;
        }

        /**
         * <p>Unique identifier for the rule. The value cannot be longer than 255 characters.</p>
         */
        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        /**
         * <p>Prefix identifying one or more objects to which the rule applies. This is
         *          No longer used; use <code>Filter</code> instead.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder filter(LifecycleRuleFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * <p>If 'Enabled', the rule is currently being applied. If 'Disabled', the rule is not
         *          currently being applied.</p>
         */
        public final Builder status(ExpirationStatus status) {
            this.status = status;
            return this;
        }

        /**
         * <p>Specifies when an Amazon S3 object transitions to a specified storage class.</p>
         */
        public final Builder transitions(List<Transition> transitions) {
            this.transitions = transitions;
            return this;
        }

        /**
         * <p> Specifies the transition rule for the lifecycle rule that describes when noncurrent
         *          objects transition to a specific storage class. If your bucket is versioning-enabled (or
         *          versioning is suspended), you can set this action to request that Amazon S3 transition
         *          noncurrent object versions to a specific storage class at a set period in the object's
         *          lifetime. </p>
         */
        public final Builder noncurrentVersionTransitions(
                List<NoncurrentVersionTransition> noncurrentVersionTransitions) {
            this.noncurrentVersionTransitions = noncurrentVersionTransitions;
            return this;
        }

        public final Builder noncurrentVersionExpiration(
                NoncurrentVersionExpiration noncurrentVersionExpiration) {
            this.noncurrentVersionExpiration = noncurrentVersionExpiration;
            return this;
        }

        public final Builder abortIncompleteMultipartUpload(
                AbortIncompleteMultipartUpload abortIncompleteMultipartUpload) {
            this.abortIncompleteMultipartUpload = abortIncompleteMultipartUpload;
            return this;
        }
    }
}

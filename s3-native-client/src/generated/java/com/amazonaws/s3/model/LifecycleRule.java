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
public class LifecycleRule {
    /**
     * <p>Specifies the expiration for the lifecycle of the object in the form of date, days and,
     *          whether the object has a delete marker.</p>
     */
    LifecycleExpiration expiration;

    /**
     * <p>Unique identifier for the rule. The value cannot be longer than 255 characters.</p>
     */
    String iD;

    /**
     * <p>Prefix identifying one or more objects to which the rule applies. This is
     *          No longer used; use <code>Filter</code> instead.</p>
     */
    String prefix;

    LifecycleRuleFilter filter;

    /**
     * <p>If 'Enabled', the rule is currently being applied. If 'Disabled', the rule is not
     *          currently being applied.</p>
     */
    ExpirationStatus status;

    /**
     * <p>Specifies when an Amazon S3 object transitions to a specified storage class.</p>
     */
    List<Transition> transitions;

    /**
     * <p> Specifies the transition rule for the lifecycle rule that describes when noncurrent
     *          objects transition to a specific storage class. If your bucket is versioning-enabled (or
     *          versioning is suspended), you can set this action to request that Amazon S3 transition
     *          noncurrent object versions to a specific storage class at a set period in the object's
     *          lifetime. </p>
     */
    List<NoncurrentVersionTransition> noncurrentVersionTransitions;

    NoncurrentVersionExpiration noncurrentVersionExpiration;

    AbortIncompleteMultipartUpload abortIncompleteMultipartUpload;

    LifecycleRule() {
        this.expiration = null;
        this.iD = "";
        this.prefix = "";
        this.filter = null;
        this.status = null;
        this.transitions = null;
        this.noncurrentVersionTransitions = null;
        this.noncurrentVersionExpiration = null;
        this.abortIncompleteMultipartUpload = null;
    }

    protected LifecycleRule(BuilderImpl builder) {
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

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String iD() {
        return iD;
    }

    public String prefix() {
        return prefix;
    }

    public LifecycleRuleFilter filter() {
        return filter;
    }

    public ExpirationStatus status() {
        return status;
    }

    public List<Transition> transitions() {
        return transitions;
    }

    public List<NoncurrentVersionTransition> noncurrentVersionTransitions() {
        return noncurrentVersionTransitions;
    }

    public NoncurrentVersionExpiration noncurrentVersionExpiration() {
        return noncurrentVersionExpiration;
    }

    public AbortIncompleteMultipartUpload abortIncompleteMultipartUpload() {
        return abortIncompleteMultipartUpload;
    }

    public interface Builder {
        Builder expiration(LifecycleExpiration expiration);

        Builder iD(String iD);

        Builder prefix(String prefix);

        Builder filter(LifecycleRuleFilter filter);

        Builder status(ExpirationStatus status);

        Builder transitions(List<Transition> transitions);

        Builder noncurrentVersionTransitions(
                List<NoncurrentVersionTransition> noncurrentVersionTransitions);

        Builder noncurrentVersionExpiration(
                NoncurrentVersionExpiration noncurrentVersionExpiration);

        Builder abortIncompleteMultipartUpload(
                AbortIncompleteMultipartUpload abortIncompleteMultipartUpload);

        LifecycleRule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the expiration for the lifecycle of the object in the form of date, days and,
         *          whether the object has a delete marker.</p>
         */
        LifecycleExpiration expiration;

        /**
         * <p>Unique identifier for the rule. The value cannot be longer than 255 characters.</p>
         */
        String iD;

        /**
         * <p>Prefix identifying one or more objects to which the rule applies. This is
         *          No longer used; use <code>Filter</code> instead.</p>
         */
        String prefix;

        LifecycleRuleFilter filter;

        /**
         * <p>If 'Enabled', the rule is currently being applied. If 'Disabled', the rule is not
         *          currently being applied.</p>
         */
        ExpirationStatus status;

        /**
         * <p>Specifies when an Amazon S3 object transitions to a specified storage class.</p>
         */
        List<Transition> transitions;

        /**
         * <p> Specifies the transition rule for the lifecycle rule that describes when noncurrent
         *          objects transition to a specific storage class. If your bucket is versioning-enabled (or
         *          versioning is suspended), you can set this action to request that Amazon S3 transition
         *          noncurrent object versions to a specific storage class at a set period in the object's
         *          lifetime. </p>
         */
        List<NoncurrentVersionTransition> noncurrentVersionTransitions;

        NoncurrentVersionExpiration noncurrentVersionExpiration;

        AbortIncompleteMultipartUpload abortIncompleteMultipartUpload;

        protected BuilderImpl() {
        }

        private BuilderImpl(LifecycleRule model) {
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
            return new LifecycleRule(this);
        }

        public final Builder expiration(LifecycleExpiration expiration) {
            this.expiration = expiration;
            return this;
        }

        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder filter(LifecycleRuleFilter filter) {
            this.filter = filter;
            return this;
        }

        public final Builder status(ExpirationStatus status) {
            this.status = status;
            return this;
        }

        public final Builder transitions(List<Transition> transitions) {
            this.transitions = transitions;
            return this;
        }

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

        @Override
        public int hashCode() {
            return Objects.hash(BuilderImpl.class);
        }

        @Override
        public boolean equals(Object rhs) {
            if (rhs == null) return false;
            return (rhs instanceof BuilderImpl);
        }

        public LifecycleExpiration expiration() {
            return expiration;
        }

        public String iD() {
            return iD;
        }

        public String prefix() {
            return prefix;
        }

        public LifecycleRuleFilter filter() {
            return filter;
        }

        public ExpirationStatus status() {
            return status;
        }

        public List<Transition> transitions() {
            return transitions;
        }

        public List<NoncurrentVersionTransition> noncurrentVersionTransitions() {
            return noncurrentVersionTransitions;
        }

        public NoncurrentVersionExpiration noncurrentVersionExpiration() {
            return noncurrentVersionExpiration;
        }

        public AbortIncompleteMultipartUpload abortIncompleteMultipartUpload() {
            return abortIncompleteMultipartUpload;
        }
    }
}

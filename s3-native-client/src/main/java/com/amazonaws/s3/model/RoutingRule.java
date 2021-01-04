// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RoutingRule {
    /**
     * <p>A container for describing a condition that must be met for the specified redirect to
     *          apply. For example, 1. If request is for pages in the <code>/docs</code> folder, redirect
     *          to the <code>/documents</code> folder. 2. If request results in HTTP error 4xx, redirect
     *          request to another host where you might process the error.</p>
     */
    Condition condition;

    /**
     * <p>Container for redirect information. You can redirect requests to another host, to
     *          another page, or with another protocol. In the event of an error, you can specify a
     *          different error code to return.</p>
     */
    Redirect redirect;

    RoutingRule() {
        this.condition = null;
        this.redirect = null;
    }

    protected RoutingRule(BuilderImpl builder) {
        this.condition = builder.condition;
        this.redirect = builder.redirect;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(RoutingRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof RoutingRule);
    }

    public Condition condition() {
        return condition;
    }

    public Redirect redirect() {
        return redirect;
    }

    public void setCondition(final Condition condition) {
        this.condition = condition;
    }

    public void setRedirect(final Redirect redirect) {
        this.redirect = redirect;
    }

    public interface Builder {
        Builder condition(Condition condition);

        Builder redirect(Redirect redirect);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A container for describing a condition that must be met for the specified redirect to
         *          apply. For example, 1. If request is for pages in the <code>/docs</code> folder, redirect
         *          to the <code>/documents</code> folder. 2. If request results in HTTP error 4xx, redirect
         *          request to another host where you might process the error.</p>
         */
        Condition condition;

        /**
         * <p>Container for redirect information. You can redirect requests to another host, to
         *          another page, or with another protocol. In the event of an error, you can specify a
         *          different error code to return.</p>
         */
        Redirect redirect;

        protected BuilderImpl() {
        }

        private BuilderImpl(RoutingRule model) {
            condition(model.condition);
            redirect(model.redirect);
        }

        public RoutingRule build() {
            return new RoutingRule(this);
        }

        public final Builder condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public final Builder redirect(Redirect redirect) {
            this.redirect = redirect;
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

        public Condition condition() {
            return condition;
        }

        public Redirect redirect() {
            return redirect;
        }

        public void setCondition(final Condition condition) {
            this.condition = condition;
        }

        public void setRedirect(final Redirect redirect) {
            this.redirect = redirect;
        }
    }
}

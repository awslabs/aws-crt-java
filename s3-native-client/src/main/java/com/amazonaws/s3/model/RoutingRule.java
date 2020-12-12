// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RoutingRule {
    private Condition condition;

    private Redirect redirect;

    private RoutingRule() {
        this.condition = null;
        this.redirect = null;
    }

    private RoutingRule(Builder builder) {
        this.condition = builder.condition;
        this.redirect = builder.redirect;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setCondition(final Condition condition) {
        this.condition = condition;
    }

    public Redirect redirect() {
        return redirect;
    }

    public void setRedirect(final Redirect redirect) {
        this.redirect = redirect;
    }

    static final class Builder {
        private Condition condition;

        private Redirect redirect;

        private Builder() {
        }

        private Builder(RoutingRule model) {
            condition(model.condition);
            redirect(model.redirect);
        }

        public RoutingRule build() {
            return new com.amazonaws.s3.model.RoutingRule(this);
        }

        /**
         * <p>A container for describing a condition that must be met for the specified redirect to
         *          apply. For example, 1. If request is for pages in the <code>/docs</code> folder, redirect
         *          to the <code>/documents</code> folder. 2. If request results in HTTP error 4xx, redirect
         *          request to another host where you might process the error.</p>
         */
        public final Builder condition(Condition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * <p>Container for redirect information. You can redirect requests to another host, to
         *          another page, or with another protocol. In the event of an error, you can specify a
         *          different error code to return.</p>
         */
        public final Builder redirect(Redirect redirect) {
            this.redirect = redirect;
            return this;
        }
    }
}

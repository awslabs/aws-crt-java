// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class WebsiteConfiguration {
    private ErrorDocument errorDocument;

    private IndexDocument indexDocument;

    private RedirectAllRequestsTo redirectAllRequestsTo;

    private List<RoutingRule> routingRules;

    private WebsiteConfiguration() {
        this.errorDocument = null;
        this.indexDocument = null;
        this.redirectAllRequestsTo = null;
        this.routingRules = null;
    }

    private WebsiteConfiguration(Builder builder) {
        this.errorDocument = builder.errorDocument;
        this.indexDocument = builder.indexDocument;
        this.redirectAllRequestsTo = builder.redirectAllRequestsTo;
        this.routingRules = builder.routingRules;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(WebsiteConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof WebsiteConfiguration);
    }

    public ErrorDocument errorDocument() {
        return errorDocument;
    }

    public void setErrorDocument(final ErrorDocument errorDocument) {
        this.errorDocument = errorDocument;
    }

    public IndexDocument indexDocument() {
        return indexDocument;
    }

    public void setIndexDocument(final IndexDocument indexDocument) {
        this.indexDocument = indexDocument;
    }

    public RedirectAllRequestsTo redirectAllRequestsTo() {
        return redirectAllRequestsTo;
    }

    public void setRedirectAllRequestsTo(final RedirectAllRequestsTo redirectAllRequestsTo) {
        this.redirectAllRequestsTo = redirectAllRequestsTo;
    }

    public List<RoutingRule> routingRules() {
        return routingRules;
    }

    public void setRoutingRules(final List<RoutingRule> routingRules) {
        this.routingRules = routingRules;
    }

    static final class Builder {
        private ErrorDocument errorDocument;

        private IndexDocument indexDocument;

        private RedirectAllRequestsTo redirectAllRequestsTo;

        private List<RoutingRule> routingRules;

        private Builder() {
        }

        private Builder(WebsiteConfiguration model) {
            errorDocument(model.errorDocument);
            indexDocument(model.indexDocument);
            redirectAllRequestsTo(model.redirectAllRequestsTo);
            routingRules(model.routingRules);
        }

        public WebsiteConfiguration build() {
            return new com.amazonaws.s3.model.WebsiteConfiguration(this);
        }

        /**
         * <p>The name of the error document for the website.</p>
         */
        public final Builder errorDocument(ErrorDocument errorDocument) {
            this.errorDocument = errorDocument;
            return this;
        }

        /**
         * <p>The name of the index document for the website.</p>
         */
        public final Builder indexDocument(IndexDocument indexDocument) {
            this.indexDocument = indexDocument;
            return this;
        }

        /**
         * <p>The redirect behavior for every request to this bucket's website endpoint.</p>
         *          <important>
         *             <p>If you specify this property, you can't specify any other property.</p>
         *          </important>
         */
        public final Builder redirectAllRequestsTo(RedirectAllRequestsTo redirectAllRequestsTo) {
            this.redirectAllRequestsTo = redirectAllRequestsTo;
            return this;
        }

        /**
         * <p>Rules that define when a redirect is applied and the redirect behavior.</p>
         */
        public final Builder routingRules(List<RoutingRule> routingRules) {
            this.routingRules = routingRules;
            return this;
        }
    }
}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class WebsiteConfiguration {
    /**
     * <p>The name of the error document for the website.</p>
     */
    ErrorDocument errorDocument;

    /**
     * <p>The name of the index document for the website.</p>
     */
    IndexDocument indexDocument;

    /**
     * <p>The redirect behavior for every request to this bucket's website endpoint.</p>
     *          <important>
     *             <p>If you specify this property, you can't specify any other property.</p>
     *          </important>
     */
    RedirectAllRequestsTo redirectAllRequestsTo;

    /**
     * <p>Rules that define when a redirect is applied and the redirect behavior.</p>
     */
    List<RoutingRule> routingRules;

    WebsiteConfiguration() {
        this.errorDocument = null;
        this.indexDocument = null;
        this.redirectAllRequestsTo = null;
        this.routingRules = null;
    }

    protected WebsiteConfiguration(BuilderImpl builder) {
        this.errorDocument = builder.errorDocument;
        this.indexDocument = builder.indexDocument;
        this.redirectAllRequestsTo = builder.redirectAllRequestsTo;
        this.routingRules = builder.routingRules;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public IndexDocument indexDocument() {
        return indexDocument;
    }

    public RedirectAllRequestsTo redirectAllRequestsTo() {
        return redirectAllRequestsTo;
    }

    public List<RoutingRule> routingRules() {
        return routingRules;
    }

    public interface Builder {
        Builder errorDocument(ErrorDocument errorDocument);

        Builder indexDocument(IndexDocument indexDocument);

        Builder redirectAllRequestsTo(RedirectAllRequestsTo redirectAllRequestsTo);

        Builder routingRules(List<RoutingRule> routingRules);

        WebsiteConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the error document for the website.</p>
         */
        ErrorDocument errorDocument;

        /**
         * <p>The name of the index document for the website.</p>
         */
        IndexDocument indexDocument;

        /**
         * <p>The redirect behavior for every request to this bucket's website endpoint.</p>
         *          <important>
         *             <p>If you specify this property, you can't specify any other property.</p>
         *          </important>
         */
        RedirectAllRequestsTo redirectAllRequestsTo;

        /**
         * <p>Rules that define when a redirect is applied and the redirect behavior.</p>
         */
        List<RoutingRule> routingRules;

        protected BuilderImpl() {
        }

        private BuilderImpl(WebsiteConfiguration model) {
            errorDocument(model.errorDocument);
            indexDocument(model.indexDocument);
            redirectAllRequestsTo(model.redirectAllRequestsTo);
            routingRules(model.routingRules);
        }

        public WebsiteConfiguration build() {
            return new WebsiteConfiguration(this);
        }

        public final Builder errorDocument(ErrorDocument errorDocument) {
            this.errorDocument = errorDocument;
            return this;
        }

        public final Builder indexDocument(IndexDocument indexDocument) {
            this.indexDocument = indexDocument;
            return this;
        }

        public final Builder redirectAllRequestsTo(RedirectAllRequestsTo redirectAllRequestsTo) {
            this.redirectAllRequestsTo = redirectAllRequestsTo;
            return this;
        }

        public final Builder routingRules(List<RoutingRule> routingRules) {
            this.routingRules = routingRules;
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

        public ErrorDocument errorDocument() {
            return errorDocument;
        }

        public IndexDocument indexDocument() {
            return indexDocument;
        }

        public RedirectAllRequestsTo redirectAllRequestsTo() {
            return redirectAllRequestsTo;
        }

        public List<RoutingRule> routingRules() {
            return routingRules;
        }
    }
}

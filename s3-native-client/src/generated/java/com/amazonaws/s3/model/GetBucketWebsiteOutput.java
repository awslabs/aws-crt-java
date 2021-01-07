// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketWebsiteOutput {
    /**
     * <p>Specifies the redirect behavior of all requests to a website endpoint of an Amazon S3
     *          bucket.</p>
     */
    RedirectAllRequestsTo redirectAllRequestsTo;

    /**
     * <p>The name of the index document for the website (for example
     *          <code>index.html</code>).</p>
     */
    IndexDocument indexDocument;

    /**
     * <p>The object key name of the website error document to use for 4XX class errors.</p>
     */
    ErrorDocument errorDocument;

    /**
     * <p>Rules that define when a redirect is applied and the redirect behavior.</p>
     */
    List<RoutingRule> routingRules;

    GetBucketWebsiteOutput() {
        this.redirectAllRequestsTo = null;
        this.indexDocument = null;
        this.errorDocument = null;
        this.routingRules = null;
    }

    protected GetBucketWebsiteOutput(BuilderImpl builder) {
        this.redirectAllRequestsTo = builder.redirectAllRequestsTo;
        this.indexDocument = builder.indexDocument;
        this.errorDocument = builder.errorDocument;
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
        return Objects.hash(GetBucketWebsiteOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketWebsiteOutput);
    }

    public RedirectAllRequestsTo redirectAllRequestsTo() {
        return redirectAllRequestsTo;
    }

    public IndexDocument indexDocument() {
        return indexDocument;
    }

    public ErrorDocument errorDocument() {
        return errorDocument;
    }

    public List<RoutingRule> routingRules() {
        return routingRules;
    }

    public void setRedirectAllRequestsTo(final RedirectAllRequestsTo redirectAllRequestsTo) {
        this.redirectAllRequestsTo = redirectAllRequestsTo;
    }

    public void setIndexDocument(final IndexDocument indexDocument) {
        this.indexDocument = indexDocument;
    }

    public void setErrorDocument(final ErrorDocument errorDocument) {
        this.errorDocument = errorDocument;
    }

    public void setRoutingRules(final List<RoutingRule> routingRules) {
        this.routingRules = routingRules;
    }

    public interface Builder {
        Builder redirectAllRequestsTo(RedirectAllRequestsTo redirectAllRequestsTo);

        Builder indexDocument(IndexDocument indexDocument);

        Builder errorDocument(ErrorDocument errorDocument);

        Builder routingRules(List<RoutingRule> routingRules);

        GetBucketWebsiteOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the redirect behavior of all requests to a website endpoint of an Amazon S3
         *          bucket.</p>
         */
        RedirectAllRequestsTo redirectAllRequestsTo;

        /**
         * <p>The name of the index document for the website (for example
         *          <code>index.html</code>).</p>
         */
        IndexDocument indexDocument;

        /**
         * <p>The object key name of the website error document to use for 4XX class errors.</p>
         */
        ErrorDocument errorDocument;

        /**
         * <p>Rules that define when a redirect is applied and the redirect behavior.</p>
         */
        List<RoutingRule> routingRules;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketWebsiteOutput model) {
            redirectAllRequestsTo(model.redirectAllRequestsTo);
            indexDocument(model.indexDocument);
            errorDocument(model.errorDocument);
            routingRules(model.routingRules);
        }

        public GetBucketWebsiteOutput build() {
            return new GetBucketWebsiteOutput(this);
        }

        public final Builder redirectAllRequestsTo(RedirectAllRequestsTo redirectAllRequestsTo) {
            this.redirectAllRequestsTo = redirectAllRequestsTo;
            return this;
        }

        public final Builder indexDocument(IndexDocument indexDocument) {
            this.indexDocument = indexDocument;
            return this;
        }

        public final Builder errorDocument(ErrorDocument errorDocument) {
            this.errorDocument = errorDocument;
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

        public RedirectAllRequestsTo redirectAllRequestsTo() {
            return redirectAllRequestsTo;
        }

        public IndexDocument indexDocument() {
            return indexDocument;
        }

        public ErrorDocument errorDocument() {
            return errorDocument;
        }

        public List<RoutingRule> routingRules() {
            return routingRules;
        }

        public void setRedirectAllRequestsTo(final RedirectAllRequestsTo redirectAllRequestsTo) {
            this.redirectAllRequestsTo = redirectAllRequestsTo;
        }

        public void setIndexDocument(final IndexDocument indexDocument) {
            this.indexDocument = indexDocument;
        }

        public void setErrorDocument(final ErrorDocument errorDocument) {
            this.errorDocument = errorDocument;
        }

        public void setRoutingRules(final List<RoutingRule> routingRules) {
            this.routingRules = routingRules;
        }
    }
}

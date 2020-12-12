// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketWebsiteOutput {
    private RedirectAllRequestsTo redirectAllRequestsTo;

    private IndexDocument indexDocument;

    private ErrorDocument errorDocument;

    private List<RoutingRule> routingRules;

    private GetBucketWebsiteOutput() {
        this.redirectAllRequestsTo = null;
        this.indexDocument = null;
        this.errorDocument = null;
        this.routingRules = null;
    }

    private GetBucketWebsiteOutput(Builder builder) {
        this.redirectAllRequestsTo = builder.redirectAllRequestsTo;
        this.indexDocument = builder.indexDocument;
        this.errorDocument = builder.errorDocument;
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

    public void setRedirectAllRequestsTo(final RedirectAllRequestsTo redirectAllRequestsTo) {
        this.redirectAllRequestsTo = redirectAllRequestsTo;
    }

    public IndexDocument indexDocument() {
        return indexDocument;
    }

    public void setIndexDocument(final IndexDocument indexDocument) {
        this.indexDocument = indexDocument;
    }

    public ErrorDocument errorDocument() {
        return errorDocument;
    }

    public void setErrorDocument(final ErrorDocument errorDocument) {
        this.errorDocument = errorDocument;
    }

    public List<RoutingRule> routingRules() {
        return routingRules;
    }

    public void setRoutingRules(final List<RoutingRule> routingRules) {
        this.routingRules = routingRules;
    }

    static final class Builder {
        private RedirectAllRequestsTo redirectAllRequestsTo;

        private IndexDocument indexDocument;

        private ErrorDocument errorDocument;

        private List<RoutingRule> routingRules;

        private Builder() {
        }

        private Builder(GetBucketWebsiteOutput model) {
            redirectAllRequestsTo(model.redirectAllRequestsTo);
            indexDocument(model.indexDocument);
            errorDocument(model.errorDocument);
            routingRules(model.routingRules);
        }

        public GetBucketWebsiteOutput build() {
            return new com.amazonaws.s3.model.GetBucketWebsiteOutput(this);
        }

        /**
         * <p>Specifies the redirect behavior of all requests to a website endpoint of an Amazon S3
         *          bucket.</p>
         */
        public final Builder redirectAllRequestsTo(RedirectAllRequestsTo redirectAllRequestsTo) {
            this.redirectAllRequestsTo = redirectAllRequestsTo;
            return this;
        }

        /**
         * <p>The name of the index document for the website (for example
         *          <code>index.html</code>).</p>
         */
        public final Builder indexDocument(IndexDocument indexDocument) {
            this.indexDocument = indexDocument;
            return this;
        }

        /**
         * <p>The object key name of the website error document to use for 4XX class errors.</p>
         */
        public final Builder errorDocument(ErrorDocument errorDocument) {
            this.errorDocument = errorDocument;
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

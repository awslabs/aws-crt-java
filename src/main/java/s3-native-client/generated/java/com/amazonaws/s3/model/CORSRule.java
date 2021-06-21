// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CORSRule {
    /**
     * <p>Headers that are specified in the <code>Access-Control-Request-Headers</code> header.
     *          These headers are allowed in a preflight OPTIONS request. In response to any preflight
     *          OPTIONS request, Amazon S3 returns any requested headers that are allowed.</p>
     */
    List<String> allowedHeaders;

    /**
     * <p>An HTTP method that you allow the origin to execute. Valid values are <code>GET</code>,
     *             <code>PUT</code>, <code>HEAD</code>, <code>POST</code>, and <code>DELETE</code>.</p>
     */
    List<String> allowedMethods;

    /**
     * <p>One or more origins you want customers to be able to access the bucket from.</p>
     */
    List<String> allowedOrigins;

    /**
     * <p>One or more headers in the response that you want customers to be able to access from
     *          their applications (for example, from a JavaScript <code>XMLHttpRequest</code>
     *          object).</p>
     */
    List<String> exposeHeaders;

    /**
     * <p>The time in seconds that your browser is to cache the preflight response for the
     *          specified resource.</p>
     */
    Integer maxAgeSeconds;

    CORSRule() {
        this.allowedHeaders = null;
        this.allowedMethods = null;
        this.allowedOrigins = null;
        this.exposeHeaders = null;
        this.maxAgeSeconds = null;
    }

    protected CORSRule(BuilderImpl builder) {
        this.allowedHeaders = builder.allowedHeaders;
        this.allowedMethods = builder.allowedMethods;
        this.allowedOrigins = builder.allowedOrigins;
        this.exposeHeaders = builder.exposeHeaders;
        this.maxAgeSeconds = builder.maxAgeSeconds;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CORSRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CORSRule);
    }

    public List<String> allowedHeaders() {
        return allowedHeaders;
    }

    public List<String> allowedMethods() {
        return allowedMethods;
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public List<String> exposeHeaders() {
        return exposeHeaders;
    }

    public Integer maxAgeSeconds() {
        return maxAgeSeconds;
    }

    public interface Builder {
        Builder allowedHeaders(List<String> allowedHeaders);

        Builder allowedMethods(List<String> allowedMethods);

        Builder allowedOrigins(List<String> allowedOrigins);

        Builder exposeHeaders(List<String> exposeHeaders);

        Builder maxAgeSeconds(Integer maxAgeSeconds);

        CORSRule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Headers that are specified in the <code>Access-Control-Request-Headers</code> header.
         *          These headers are allowed in a preflight OPTIONS request. In response to any preflight
         *          OPTIONS request, Amazon S3 returns any requested headers that are allowed.</p>
         */
        List<String> allowedHeaders;

        /**
         * <p>An HTTP method that you allow the origin to execute. Valid values are <code>GET</code>,
         *             <code>PUT</code>, <code>HEAD</code>, <code>POST</code>, and <code>DELETE</code>.</p>
         */
        List<String> allowedMethods;

        /**
         * <p>One or more origins you want customers to be able to access the bucket from.</p>
         */
        List<String> allowedOrigins;

        /**
         * <p>One or more headers in the response that you want customers to be able to access from
         *          their applications (for example, from a JavaScript <code>XMLHttpRequest</code>
         *          object).</p>
         */
        List<String> exposeHeaders;

        /**
         * <p>The time in seconds that your browser is to cache the preflight response for the
         *          specified resource.</p>
         */
        Integer maxAgeSeconds;

        protected BuilderImpl() {
        }

        private BuilderImpl(CORSRule model) {
            allowedHeaders(model.allowedHeaders);
            allowedMethods(model.allowedMethods);
            allowedOrigins(model.allowedOrigins);
            exposeHeaders(model.exposeHeaders);
            maxAgeSeconds(model.maxAgeSeconds);
        }

        public CORSRule build() {
            return new CORSRule(this);
        }

        public final Builder allowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
            return this;
        }

        public final Builder allowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
            return this;
        }

        public final Builder allowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
            return this;
        }

        public final Builder exposeHeaders(List<String> exposeHeaders) {
            this.exposeHeaders = exposeHeaders;
            return this;
        }

        public final Builder maxAgeSeconds(Integer maxAgeSeconds) {
            this.maxAgeSeconds = maxAgeSeconds;
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

        public List<String> allowedHeaders() {
            return allowedHeaders;
        }

        public List<String> allowedMethods() {
            return allowedMethods;
        }

        public List<String> allowedOrigins() {
            return allowedOrigins;
        }

        public List<String> exposeHeaders() {
            return exposeHeaders;
        }

        public Integer maxAgeSeconds() {
            return maxAgeSeconds;
        }
    }
}

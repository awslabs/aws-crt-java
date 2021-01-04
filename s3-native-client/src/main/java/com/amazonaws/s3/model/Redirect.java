// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Redirect {
    /**
     * <p>The host name to use in the redirect request.</p>
     */
    String hostName;

    /**
     * <p>The HTTP redirect code to use on the response. Not required if one of the siblings is
     *          present.</p>
     */
    String httpRedirectCode;

    /**
     * <p>Protocol to use when redirecting requests. The default is the protocol that is used in
     *          the original request.</p>
     */
    Protocol protocol;

    /**
     * <p>The object key prefix to use in the redirect request. For example, to redirect requests
     *          for all pages with prefix <code>docs/</code> (objects in the <code>docs/</code> folder) to
     *             <code>documents/</code>, you can set a condition block with <code>KeyPrefixEquals</code>
     *          set to <code>docs/</code> and in the Redirect set <code>ReplaceKeyPrefixWith</code> to
     *             <code>/documents</code>. Not required if one of the siblings is present. Can be present
     *          only if <code>ReplaceKeyWith</code> is not provided.</p>
     */
    String replaceKeyPrefixWith;

    /**
     * <p>The specific object key to use in the redirect request. For example, redirect request to
     *             <code>error.html</code>. Not required if one of the siblings is present. Can be present
     *          only if <code>ReplaceKeyPrefixWith</code> is not provided.</p>
     */
    String replaceKeyWith;

    Redirect() {
        this.hostName = "";
        this.httpRedirectCode = "";
        this.protocol = null;
        this.replaceKeyPrefixWith = "";
        this.replaceKeyWith = "";
    }

    protected Redirect(BuilderImpl builder) {
        this.hostName = builder.hostName;
        this.httpRedirectCode = builder.httpRedirectCode;
        this.protocol = builder.protocol;
        this.replaceKeyPrefixWith = builder.replaceKeyPrefixWith;
        this.replaceKeyWith = builder.replaceKeyWith;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Redirect.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Redirect);
    }

    public String hostName() {
        return hostName;
    }

    public String httpRedirectCode() {
        return httpRedirectCode;
    }

    public Protocol protocol() {
        return protocol;
    }

    public String replaceKeyPrefixWith() {
        return replaceKeyPrefixWith;
    }

    public String replaceKeyWith() {
        return replaceKeyWith;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setHttpRedirectCode(final String httpRedirectCode) {
        this.httpRedirectCode = httpRedirectCode;
    }

    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }

    public void setReplaceKeyPrefixWith(final String replaceKeyPrefixWith) {
        this.replaceKeyPrefixWith = replaceKeyPrefixWith;
    }

    public void setReplaceKeyWith(final String replaceKeyWith) {
        this.replaceKeyWith = replaceKeyWith;
    }

    public interface Builder {
        Builder hostName(String hostName);

        Builder httpRedirectCode(String httpRedirectCode);

        Builder protocol(Protocol protocol);

        Builder replaceKeyPrefixWith(String replaceKeyPrefixWith);

        Builder replaceKeyWith(String replaceKeyWith);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The host name to use in the redirect request.</p>
         */
        String hostName;

        /**
         * <p>The HTTP redirect code to use on the response. Not required if one of the siblings is
         *          present.</p>
         */
        String httpRedirectCode;

        /**
         * <p>Protocol to use when redirecting requests. The default is the protocol that is used in
         *          the original request.</p>
         */
        Protocol protocol;

        /**
         * <p>The object key prefix to use in the redirect request. For example, to redirect requests
         *          for all pages with prefix <code>docs/</code> (objects in the <code>docs/</code> folder) to
         *             <code>documents/</code>, you can set a condition block with <code>KeyPrefixEquals</code>
         *          set to <code>docs/</code> and in the Redirect set <code>ReplaceKeyPrefixWith</code> to
         *             <code>/documents</code>. Not required if one of the siblings is present. Can be present
         *          only if <code>ReplaceKeyWith</code> is not provided.</p>
         */
        String replaceKeyPrefixWith;

        /**
         * <p>The specific object key to use in the redirect request. For example, redirect request to
         *             <code>error.html</code>. Not required if one of the siblings is present. Can be present
         *          only if <code>ReplaceKeyPrefixWith</code> is not provided.</p>
         */
        String replaceKeyWith;

        protected BuilderImpl() {
        }

        private BuilderImpl(Redirect model) {
            hostName(model.hostName);
            httpRedirectCode(model.httpRedirectCode);
            protocol(model.protocol);
            replaceKeyPrefixWith(model.replaceKeyPrefixWith);
            replaceKeyWith(model.replaceKeyWith);
        }

        public Redirect build() {
            return new Redirect(this);
        }

        public final Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public final Builder httpRedirectCode(String httpRedirectCode) {
            this.httpRedirectCode = httpRedirectCode;
            return this;
        }

        public final Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public final Builder replaceKeyPrefixWith(String replaceKeyPrefixWith) {
            this.replaceKeyPrefixWith = replaceKeyPrefixWith;
            return this;
        }

        public final Builder replaceKeyWith(String replaceKeyWith) {
            this.replaceKeyWith = replaceKeyWith;
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

        public String hostName() {
            return hostName;
        }

        public String httpRedirectCode() {
            return httpRedirectCode;
        }

        public Protocol protocol() {
            return protocol;
        }

        public String replaceKeyPrefixWith() {
            return replaceKeyPrefixWith;
        }

        public String replaceKeyWith() {
            return replaceKeyWith;
        }

        public void setHostName(final String hostName) {
            this.hostName = hostName;
        }

        public void setHttpRedirectCode(final String httpRedirectCode) {
            this.httpRedirectCode = httpRedirectCode;
        }

        public void setProtocol(final Protocol protocol) {
            this.protocol = protocol;
        }

        public void setReplaceKeyPrefixWith(final String replaceKeyPrefixWith) {
            this.replaceKeyPrefixWith = replaceKeyPrefixWith;
        }

        public void setReplaceKeyWith(final String replaceKeyWith) {
            this.replaceKeyWith = replaceKeyWith;
        }
    }
}

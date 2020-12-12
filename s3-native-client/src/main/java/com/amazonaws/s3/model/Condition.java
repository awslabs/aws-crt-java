// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Condition {
    private String httpErrorCodeReturnedEquals;

    private String keyPrefixEquals;

    private Condition() {
        this.httpErrorCodeReturnedEquals = null;
        this.keyPrefixEquals = null;
    }

    private Condition(Builder builder) {
        this.httpErrorCodeReturnedEquals = builder.httpErrorCodeReturnedEquals;
        this.keyPrefixEquals = builder.keyPrefixEquals;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Condition.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Condition);
    }

    public String httpErrorCodeReturnedEquals() {
        return httpErrorCodeReturnedEquals;
    }

    public void setHttpErrorCodeReturnedEquals(final String httpErrorCodeReturnedEquals) {
        this.httpErrorCodeReturnedEquals = httpErrorCodeReturnedEquals;
    }

    public String keyPrefixEquals() {
        return keyPrefixEquals;
    }

    public void setKeyPrefixEquals(final String keyPrefixEquals) {
        this.keyPrefixEquals = keyPrefixEquals;
    }

    static final class Builder {
        private String httpErrorCodeReturnedEquals;

        private String keyPrefixEquals;

        private Builder() {
        }

        private Builder(Condition model) {
            httpErrorCodeReturnedEquals(model.httpErrorCodeReturnedEquals);
            keyPrefixEquals(model.keyPrefixEquals);
        }

        public Condition build() {
            return new com.amazonaws.s3.model.Condition(this);
        }

        /**
         * <p>The HTTP error code when the redirect is applied. In the event of an error, if the error
         *          code equals this value, then the specified redirect is applied. Required when parent
         *          element <code>Condition</code> is specified and sibling <code>KeyPrefixEquals</code> is not
         *          specified. If both are specified, then both must be true for the redirect to be
         *          applied.</p>
         */
        public final Builder httpErrorCodeReturnedEquals(String httpErrorCodeReturnedEquals) {
            this.httpErrorCodeReturnedEquals = httpErrorCodeReturnedEquals;
            return this;
        }

        /**
         * <p>The object key name prefix when the redirect is applied. For example, to redirect
         *          requests for <code>ExamplePage.html</code>, the key prefix will be
         *             <code>ExamplePage.html</code>. To redirect request for all pages with the prefix
         *             <code>docs/</code>, the key prefix will be <code>/docs</code>, which identifies all
         *          objects in the <code>docs/</code> folder. Required when the parent element
         *             <code>Condition</code> is specified and sibling <code>HttpErrorCodeReturnedEquals</code>
         *          is not specified. If both conditions are specified, both must be true for the redirect to
         *          be applied.</p>
         */
        public final Builder keyPrefixEquals(String keyPrefixEquals) {
            this.keyPrefixEquals = keyPrefixEquals;
            return this;
        }
    }
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.auth.credentials;

/**
 * A credentials provider that sources credentials from a custom synchronous
 * callback
 */
public class DelegateCredentialsProvider extends CredentialsProvider {

    /**
     * A simple builder class for a delegate credentials provider and its options
     */
    static public class DelegateCredentialsProviderBuilder {

        private DelegateCredentialsHandler handler;

        /**
         * Default constructor
         */
        public DelegateCredentialsProviderBuilder() {
        }

        /**
         * Sets the delegate this provider should use for sourcing credentials
         * @param handler credentials-sourcing delegate
         * @return this builder object
         */
        public DelegateCredentialsProviderBuilder withHandler(DelegateCredentialsHandler handler) {
            this.handler = handler;

            return this;
        }

        DelegateCredentialsHandler getHandler() {
            return handler;
        }

        /**
         * Builds a new delegate credentials provider using the builder's configuration
         * @return a new delegate credentials provider
         */
        public DelegateCredentialsProvider build() {
            return new DelegateCredentialsProvider(this);
        }
    }

    private DelegateCredentialsProvider(DelegateCredentialsProviderBuilder builder) {
        super();
        DelegateCredentialsHandler handler = builder.getHandler();

        long nativeHandle = delegateCredentialsProviderNew(this, handler);
        acquireNativeHandle(nativeHandle);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long delegateCredentialsProviderNew(DelegateCredentialsProvider thisObj,
            DelegateCredentialsHandler handler);

}

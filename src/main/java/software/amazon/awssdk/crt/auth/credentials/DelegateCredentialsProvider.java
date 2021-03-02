package software.amazon.awssdk.crt.auth.credentials;

/**
 * A credentials provider that sources credentials from a custom synchronous
 * callback
 */
public class DelegateCredentialsProvider extends CredentialsProvider {

    /**
     * A simple builder class for a static credentials provider and its options
     */
    static public class DelegateCredentialsProviderBuilder {

        private DelegateCredentialsHandler handler;

        public DelegateCredentialsProviderBuilder() {
        }

        public DelegateCredentialsProviderBuilder withHandler(DelegateCredentialsHandler handler) {
            this.handler = handler;

            return this;
        }

        DelegateCredentialsHandler getHandler() {
            return handler;
        }

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

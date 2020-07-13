package software.amazon.awssdk.crt.auth.credentials;

public class CachedCredentialsProvider extends CredentialsProvider {

    private CredentialsProvider cachedProvider;

    /**
     * A simple builder class for a cached credentials provider and its options
     */
    static public class CachedCredentialsProviderBuilder {

        private int cachingDurationInSeconds;
        private CredentialsProvider cachedProvider;

        public CachedCredentialsProviderBuilder() {}

        public CachedCredentialsProviderBuilder withCachingDurationInSeconds(int cachingDurationInSeconds) {
            this.cachingDurationInSeconds = cachingDurationInSeconds;

            return this;
        }

        int getCachingDurationInSeconds() { return cachingDurationInSeconds; }

        public CachedCredentialsProviderBuilder withCachedProvider(CredentialsProvider cachedProvider) {
            this.cachedProvider = cachedProvider;

            return this;
        }

        CredentialsProvider getCachedProvider() { return cachedProvider; }

        public CachedCredentialsProvider build() {
            return new CachedCredentialsProvider(this);
        }
    }

    private CachedCredentialsProvider(CachedCredentialsProviderBuilder builder) {
        super();

        cachedProvider = builder.getCachedProvider();
        addReferenceTo(cachedProvider);

        long nativeHandle = cachedCredentialsProviderNew(this, builder.getCachingDurationInSeconds(), cachedProvider.getNativeHandle());
        acquireNativeHandle(nativeHandle);
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long cachedCredentialsProviderNew(CachedCredentialsProvider thisObj, int cachingDurationInSeconds, long cachedProvider);
}

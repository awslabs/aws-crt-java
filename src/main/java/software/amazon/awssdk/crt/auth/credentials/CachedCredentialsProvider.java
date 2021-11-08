package software.amazon.awssdk.crt.auth.credentials;

/**
 * A credentials provider that adds caching to another credentials provider via decoration
 */
public class CachedCredentialsProvider extends CredentialsProvider {

    private CredentialsProvider cachedProvider;

    /**
     * A simple builder class for a cached credentials provider and its options
     */
    static public class CachedCredentialsProviderBuilder {

        private int cachingDurationInSeconds;
        private CredentialsProvider cachedProvider;

        /**
         * Default constructor
         */
        public CachedCredentialsProviderBuilder() {}

        /**
         * Sets the maximum caching duration for any credentials sourced through this provider.  Depending on the
         * wrapped provider's configuration, credentials may be sourced with shorter durations.
         * @param cachingDurationInSeconds maximum caching duration in seconds of sourced credentials
         * @return the provider builder
         */
        public CachedCredentialsProviderBuilder withCachingDurationInSeconds(int cachingDurationInSeconds) {
            this.cachingDurationInSeconds = cachingDurationInSeconds;

            return this;
        }

        int getCachingDurationInSeconds() { return cachingDurationInSeconds; }

        /**
         * Sets the credentials provider to cache results from
         * @param cachedProvider credentials provider to cache results from
         * @return the provider builder
         */
        public CachedCredentialsProviderBuilder withCachedProvider(CredentialsProvider cachedProvider) {
            this.cachedProvider = cachedProvider;

            return this;
        }

        CredentialsProvider getCachedProvider() { return cachedProvider; }

        /**
         * Builds a new caching credentials provider
         * @return the new credentials provider
         */
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

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.io.*;

public class RetryOptionsTest extends CrtTestFixture {

    public RetryOptionsTest() {
    }

    @Test
    public void testExponentialBackoffRetryOptions() {
        try(EventLoopGroup elg = new EventLoopGroup(0,1)) {
            final long backoffScaleFactorMS = 10;
            final ExponentialBackoffRetryOptions.JitterMode jitterMode = ExponentialBackoffRetryOptions.JitterMode.Decorrelated;
            final long maxRetries = 42;

            ExponentialBackoffRetryOptions.Builder builder = new ExponentialBackoffRetryOptions.Builder();
            builder.withEventLoopGroup(elg);
            builder.withBackoffScaleFactorMS(backoffScaleFactorMS);
            builder.withJitterMode(jitterMode);
            builder.withMaxRetries(maxRetries);

            Assume.assumeTrue(builder.getEventLoopGroup() == elg);
            Assume.assumeTrue(builder.getBackoffScaleFactorMS() == backoffScaleFactorMS);
            Assume.assumeTrue(builder.getJitterMode() == jitterMode);
            Assume.assumeTrue(builder.getMaxRetries() == maxRetries);

            try (ExponentialBackoffRetryOptions backoffRetryOptions = builder.build()) {

            }
        }
    }

    @Test
    public void testStandardRetryOptions() {
        try(ExponentialBackoffRetryOptions backoffOptions = new ExponentialBackoffRetryOptions.Builder().build()) {
            final long initialBucketCapcity = 42l;

            StandardRetryOptions.Builder builder = new StandardRetryOptions.Builder();
            builder.withBackoffRetryOptions(backoffOptions);
            builder.withInitialBucketCapcity(initialBucketCapcity);

            Assume.assumeTrue(builder.getBackoffRetryOptions() == backoffOptions);
            Assume.assumeTrue(builder.getInitialBucketCapacity() == initialBucketCapcity);

            try (StandardRetryOptions retryOptions = builder.build()) {

            }
        }
    }
}

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.CrtRuntimeException;;

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
    public void testInvalidExponentialBackoffRetryOptions() {
        Log.initLoggingToStdout(Log.LogLevel.Debug);

        try(EventLoopGroup elg = new EventLoopGroup(0,1)) {

            try (ExponentialBackoffRetryOptions backoffRetryOptions = new ExponentialBackoffRetryOptions.Builder().withEventLoopGroup(elg).build()) {

            }

            try (ExponentialBackoffRetryOptions backoffRetryOptions = new ExponentialBackoffRetryOptions.Builder().withEventLoopGroup(elg).withBackoffScaleFactorMS(-1).build()) {

            } catch(CrtRuntimeException e) {
                Assume.assumeTrue(e.errorName.equals("AWS_ERROR_INVALID_ARGUMENT"));
            }

            try (ExponentialBackoffRetryOptions backoffRetryOptions = new ExponentialBackoffRetryOptions.Builder().withEventLoopGroup(elg).withMaxRetries(-1).build()) {

            } catch(CrtRuntimeException e) {
                Assume.assumeTrue(e.errorName.equals("AWS_ERROR_INVALID_ARGUMENT"));
            }
        }
    }

    @Test
    public void testStandardRetryOptions() {
        try(ExponentialBackoffRetryOptions backoffOptions = new ExponentialBackoffRetryOptions.Builder().build()) {
            final long initialBucketCapacity = 42l;

            StandardRetryOptions.Builder builder = new StandardRetryOptions.Builder();
            builder.withBackoffRetryOptions(backoffOptions);
            builder.withInitialBucketCapacity(initialBucketCapacity);

            Assume.assumeTrue(builder.getBackoffRetryOptions() == backoffOptions);
            Assume.assumeTrue(builder.getInitialBucketCapacity() == initialBucketCapacity);

            try (StandardRetryOptions retryOptions = builder.build()) {

            }
        }
    }

    @Test
    public void testInvalidStandardRetryOptions() {
        try(ExponentialBackoffRetryOptions backoffOptions = new ExponentialBackoffRetryOptions.Builder().build()) {
            final long initialBucketCapacity = 42l;

            StandardRetryOptions.Builder builder = new StandardRetryOptions.Builder();
            builder.withInitialBucketCapacity(initialBucketCapacity);

            Assume.assumeTrue(builder.getBackoffRetryOptions() == backoffOptions);
            Assume.assumeTrue(builder.getInitialBucketCapacity() == initialBucketCapacity);

            try (StandardRetryOptions retryOptions = new StandardRetryOptions.Builder().withBackoffRetryOptions(backoffOptions).build()) {

            }

            try (StandardRetryOptions retryOptions = new StandardRetryOptions.Builder().withBackoffRetryOptions(backoffOptions).withInitialBucketCapacity(-1).build()) {

            } catch(CrtRuntimeException e) {
                Assume.assumeTrue(e.errorName.equals("AWS_ERROR_INVALID_ARGUMENT"));
            }
        }
    }

    @Test
    public void testStandardRetryOptions_NoBackoffOptions() {
        final long initialBucketCapacity = 42l;

        StandardRetryOptions.Builder builder = new StandardRetryOptions.Builder();
        builder.withInitialBucketCapacity(initialBucketCapacity);

        Assume.assumeTrue(builder.getBackoffRetryOptions() == null);
        Assume.assumeTrue(builder.getInitialBucketCapacity() == initialBucketCapacity);

        try (StandardRetryOptions retryOptions = builder.build()) {

        }
    }
}

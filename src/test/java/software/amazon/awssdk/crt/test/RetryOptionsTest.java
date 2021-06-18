package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.CrtRuntimeException;;

public class RetryOptionsTest extends CrtTestFixture {

    public class CopyToNativeCallback {
        public void onCopiedToNative(long nativeHandle) {
        }
    }

    public RetryOptionsTest() {
    }

    @Test
    public void testExponentialBackoffRetryOptions() {
        try(EventLoopGroup elg = new EventLoopGroup(0,1)) {
            final long scaleFactorMS = 123l;
            final long maxRetries = 456;
            final ExponentialBackoffRetryOptions.JitterMode jitterMode = ExponentialBackoffRetryOptions.JitterMode.Decorrelated;

            final ExponentialBackoffRetryOptions retryOptions = new ExponentialBackoffRetryOptions()
                    .withEventLoopGroup(elg)
                    .withBackoffScaleFactorMS(scaleFactorMS)
                    .withJitterMode(jitterMode)
                    .withMaxRetries(maxRetries);

            Assume.assumeTrue(retryOptions.getEventLoopGroup() == elg);
            Assume.assumeTrue(retryOptions.getBackoffScaleFactorMS() == scaleFactorMS);
            Assume.assumeTrue(retryOptions.getJitterMode() == jitterMode);
            Assume.assumeTrue(retryOptions.getMaxRetries() == maxRetries);

            copyExponentialBackoffRetryOptionsToNative(retryOptions, new CopyToNativeCallback() {
                @Override
                public void onCopiedToNative(long nativeHandle) {
                    Assume.assumeTrue(retryOptions.compareToNative(nativeHandle));
                }
            });
        }
    }

    private boolean copyExponentialBackoffRetryOptionsExpectError(ExponentialBackoffRetryOptions retryOptions) {
        boolean exceptionCaught = false;

        try {
            copyExponentialBackoffRetryOptionsToNative(retryOptions, null);
        } catch(CrtRuntimeException e) {
            exceptionCaught = true;
        }

        return exceptionCaught;
    }

    @Test
    public void testExponentialBackoffRetryOptionsInvalidValues() {
        ExponentialBackoffRetryOptions retryOptions = new ExponentialBackoffRetryOptions();

        /* Should not throw any exceptions. */
        copyExponentialBackoffRetryOptionsToNative(retryOptions, null);

        retryOptions = new ExponentialBackoffRetryOptions().withMaxRetries(-1);
        Assume.assumeTrue( copyExponentialBackoffRetryOptionsExpectError(retryOptions) );

        retryOptions = new ExponentialBackoffRetryOptions().withBackoffScaleFactorMS(-1);
        Assume.assumeTrue( copyExponentialBackoffRetryOptionsExpectError(retryOptions) );

        retryOptions = new ExponentialBackoffRetryOptions().withBackoffScaleFactorMS(Long.MAX_VALUE);
        Assume.assumeTrue( copyExponentialBackoffRetryOptionsExpectError(retryOptions) );
    }

    @Test
    public void testStandardRetryOptions() {
        try(EventLoopGroup elg = new EventLoopGroup(0,1)) {
            final ExponentialBackoffRetryOptions backoffOptions = new ExponentialBackoffRetryOptions()
                    .withBackoffScaleFactorMS(123)
                    .withJitterMode(ExponentialBackoffRetryOptions.JitterMode.Decorrelated)
                    .withMaxRetries(456);

            final long initialBucketCapacity = 100;

            final StandardRetryOptions retryOptions = new StandardRetryOptions()
                    .withBackoffRetryOptions(backoffOptions)
                    .withInitialBucketCapacity(initialBucketCapacity);

            Assume.assumeTrue(retryOptions.getBackoffRetryOptions() == backoffOptions);
            Assume.assumeTrue(retryOptions.getInitialBucketCapacity() == initialBucketCapacity);

            copyStandardRetryOptionsToNative(retryOptions, new CopyToNativeCallback() {
                @Override
                public void onCopiedToNative(long nativeHandle) {
                    Assume.assumeTrue(retryOptions.compareToNative(nativeHandle));
                }
            });
        }
    }

    private boolean copyStandardRetryOptionsToNativeExpectError(StandardRetryOptions retryOptions) {
        boolean exceptionCaught = false;

        try {
            copyStandardRetryOptionsToNative(retryOptions, null);
        } catch(CrtRuntimeException e) {
            exceptionCaught = true;
        }

        return exceptionCaught;
    }

    @Test
    public void testStandardRetryOptionsInvalidValues() {
        StandardRetryOptions retryOptions = new StandardRetryOptions();

        /* Should not throw any exceptions. */
        copyStandardRetryOptionsToNative(retryOptions, null);

        retryOptions = new StandardRetryOptions().withInitialBucketCapacity(-1);
        Assume.assumeTrue( copyStandardRetryOptionsToNativeExpectError(retryOptions) );
    }

    @Test
    public void testNativeRetryFunctions() {
        Log.initLoggingToStdout(Log.LogLevel.Info);

        nativeTestCopyToNative();

        Assume.assumeTrue(true);
    }

    @Test
    public void testNativeOptionsCompare() {
        Log.initLoggingToStdout(Log.LogLevel.Info);

        nativeTestOptionsCompare();

        Assume.assumeTrue(true);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void copyExponentialBackoffRetryOptionsToNative(ExponentialBackoffRetryOptions retryOptions, CopyToNativeCallback callback);

    private static native void copyStandardRetryOptionsToNative(StandardRetryOptions retryOptions, CopyToNativeCallback callback);

    private static native void nativeTestCopyToNative();

    private static native void nativeTestOptionsCompare();
}

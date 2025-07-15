/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import java.lang.Runnable;
import java.lang.Thread;
import java.util.function.Function;

public class TestUtils {
    static public void doRetryableTest(Runnable testFunction, Function<Exception, Boolean> shouldRetryPredicate, int maxAttempts, int sleepTimeMillis) throws Exception {
        int attempt = 0;
        while (attempt < maxAttempts) {
            ++attempt;

            try {
                testFunction.run();
                return;
            } catch (Exception ex) {
                if (!shouldRetryPredicate.apply(ex)) {
                    throw ex;
                }
            }

            sleepForAtLeastMilliseconds(sleepTimeMillis);
        }

        throw new Exception("Retryable network test exceeded the maximum allowed attempts without succeeding");
    }

    static public Boolean isRetryableTimeout(Exception ex) {
        String exceptionMsg = ex.toString();
        return exceptionMsg.contains("socket operation timed out") ||
                exceptionMsg.contains("tls negotiation timeout") ||
                exceptionMsg.contains("socket connection refused");
    }

    static public void sleepForAtLeastMilliseconds(long sleepMilliseconds) throws InterruptedException {
        long startTimeNanoseconds = System.nanoTime();
        long sleepNanoseconds = sleepMilliseconds * 1_000_000L;
        long remainingMilliseconds = sleepMilliseconds;

        while (remainingMilliseconds > 0) {
            Thread.sleep(remainingMilliseconds);
            long elapsedNanoseconds = System.nanoTime() - startTimeNanoseconds;
            remainingMilliseconds = (sleepNanoseconds - elapsedNanoseconds + 1) / 1_000_000L;
        }
    }
}

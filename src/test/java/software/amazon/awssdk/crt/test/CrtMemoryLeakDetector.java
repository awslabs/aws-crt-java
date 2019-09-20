package software.amazon.awssdk.crt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Assert;


/**
 * Checks that the CRT doesn't have any major memory leaks. Probably won't detect very small leaks but will likely find
 * obvious large ones.
 */
public class CrtMemoryLeakDetector {
    // Allow up to 512 byte increase in memory usage between CRT Test Runs
    private final static int DEFAULT_ALLOWED_MEDIAN_MEMORY_BYTE_DELTA = 512;
    private final static int DEFAULT_NUM_LEAK_TEST_ITERATIONS = 20;

    private static long getEstimatedMemoryInUse() {
        long estimatedMemInUse = Long.MAX_VALUE;

        for (int i = 0; i < 10; i++) {
            // Force a Java Garbage Collection before measuring to reduce noise in measurement
            System.gc();

            // Take the minimum of several measurements to reduce noise
            estimatedMemInUse = Long.min(estimatedMemInUse, (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        }

        return estimatedMemInUse;
    }

    public static void leakCheck(Callable<Void> fn) throws Exception {
        leakCheck(DEFAULT_NUM_LEAK_TEST_ITERATIONS, DEFAULT_ALLOWED_MEDIAN_MEMORY_BYTE_DELTA, fn);
    }

    public static void leakCheck(int numIterations, int maxLeakage, Callable<Void> fn) throws Exception {
        List<Long> memoryUsedMeasurements = new ArrayList<>();

        for (int i = 0; i < numIterations; i++) {
            fn.call();
            memoryUsedMeasurements.add(getEstimatedMemoryInUse());
        }

        List<Long> memUseDeltas = new ArrayList<>();
        for (int i = 0; i < memoryUsedMeasurements.size() - 1; i++) {
            long prev = memoryUsedMeasurements.get(i);
            long curr = memoryUsedMeasurements.get(i + 1);
            long delta = (curr - prev);
            memUseDeltas.add(delta);
        }

        // Sort from smallest to largest
        memUseDeltas.sort(null);

        // Get the median delta
        long p50MemoryUsageDelta = memUseDeltas.get(memUseDeltas.size() / 2);

        if (p50MemoryUsageDelta > maxLeakage) {
            Assert.fail("Potential Memory Leak! Memory usage Deltas: " + memUseDeltas.toString());
        }
    }
}

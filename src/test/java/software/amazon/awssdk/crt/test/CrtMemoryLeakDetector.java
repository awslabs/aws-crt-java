package software.amazon.awssdk.crt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Assert;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.Log;

/**
 * Checks that the CRT doesn't have any major memory leaks. Probably won't
 * detect very small leaks but will likely find obvious large ones.
 */
public class CrtMemoryLeakDetector {
    // Allow up to 512 byte increase in memory usage between CRT Test Runs
    private final static int DEFAULT_ALLOWED_MEDIAN_MEMORY_BYTE_DELTA = 1024;
    private final static int DEFAULT_NUM_LEAK_TEST_ITERATIONS = 20;

    private static long getNativeMemoryInUse() {
        long nativeMemory = CRT.nativeMemory();
        Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtGeneral, String.format("Native MemUsage: %d", nativeMemory));
        return nativeMemory;
    }

    private static long getJvmMemoryInUse() {

        Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtGeneral, "Checking JVM Memory Usage");

        long estimatedMemInUse = Long.MAX_VALUE;

        for (int i = 0; i < 10; i++) {
            // Force a Java Garbage Collection before measuring to reduce noise in
            // measurement
            System.gc();

            // Take the minimum of several measurements to reduce noise
            estimatedMemInUse = Long.min(estimatedMemInUse,
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        }

        Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtGeneral, String.format("JVM MemUsage: %d", estimatedMemInUse));

        return estimatedMemInUse;
    }

    public static void leakCheck(Callable<Void> fn) throws Exception {
        leakCheck(DEFAULT_NUM_LEAK_TEST_ITERATIONS, DEFAULT_ALLOWED_MEDIAN_MEMORY_BYTE_DELTA, fn);
    }

    public static void leakCheck(int numIterations, int maxLeakage, Callable<Void> fn) throws Exception {
        List<Long> jvmSamples = new ArrayList<>();
        List<Long> nativeSamples = new ArrayList<>();
        numIterations = Math.max(2, numIterations); // There need to be at least 2 iterations to get deltas

        for (int i = 0; i < numIterations; i++) {
            fn.call();
            jvmSamples.add(getJvmMemoryInUse());
            nativeSamples.add(getNativeMemoryInUse());
        }

        List<Long> jvmDeltas = getDeltas(jvmSamples);
        // Get the median delta
        long p50MemoryUsageDelta = jvmDeltas.get(jvmDeltas.size() / 2);
        if (p50MemoryUsageDelta > maxLeakage) {
            Assert.fail(
                    String.format("Potential Java Memory Leak!\nMemory usage Deltas: %s\nMeasurements: %s\nDiff: %d",
                            jvmDeltas.toString(), jvmSamples.toString(), p50MemoryUsageDelta - maxLeakage));
        }
        List<Long> nativeDeltas = getDeltas(nativeSamples);
        // Get the median delta
        p50MemoryUsageDelta = nativeDeltas.get(nativeDeltas.size() / 2);
        if (p50MemoryUsageDelta > maxLeakage) {
            Assert.fail(
                    String.format("Potential Native Memory Leak!\nMemory usage Deltas: %s\nMeasurements: %s\nDiff: %d",
                            nativeDeltas.toString(), nativeSamples.toString(), p50MemoryUsageDelta - maxLeakage));
        }
    }

    private static List<Long> getDeltas(List<Long> samples) {
        List<Long> memUseDeltas = new ArrayList<>();
        for (int i = 0; i < samples.size() - 1; i++) {
            long prev = samples.get(i);
            long curr = samples.get(i + 1);
            long delta = (curr - prev);
            memUseDeltas.add(delta);
        }

        // Sort from smallest to largest
        memUseDeltas.sort(null);
        return memUseDeltas;
    }
}

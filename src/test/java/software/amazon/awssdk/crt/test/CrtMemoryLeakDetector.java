package software.amazon.awssdk.crt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.Assert;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
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

        Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtGeneral,
                String.format("JVM MemUsage: %d", estimatedMemInUse));

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

        // Get the median deltas
        List<Long> jvmDeltas = getDeltas(jvmSamples);
        long medianJvmDelta = jvmDeltas.get(jvmDeltas.size() / 2);
        List<Long> nativeDeltas = getDeltas(nativeSamples);
        long medianNativeDelta = nativeDeltas.get(nativeDeltas.size() / 2);

        String output = "";
        if (medianJvmDelta > maxLeakage) {
            output += "Potential Java Memory Leak!\n";
        }
        if (medianNativeDelta > maxLeakage) {
            output += "Potential Native Memory Leak!\n";
        }

        final List<String> resources = new ArrayList<>();
        CrtResource.collectNativeResources((resource) -> {
            resources.add(resource);
        });
        if (resources.size() > 0) {
            output += resources.toString();
        }

        if (output.length() > 0) {
            Assert.fail(String.format(
                    "%s\nJVM Usage Deltas: %s\nJVM Samples: %s\nNative Usage Deltas: %s\nNative Samples: %s\n", output,
                    jvmDeltas.toString(), jvmSamples.toString(), nativeDeltas.toString(), nativeSamples.toString()));
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

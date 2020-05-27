package software.amazon.awssdk.crt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;

/**
 * Checks that the CRT doesn't have any major memory leaks. Probably won't
 * detect very small leaks but will likely find obvious large ones.
 */
public class CrtMemoryLeakDetector extends CrtTestFixture  {
    static {
        new CRT(); // force the CRT to load before doing anything
    };
    
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
        leakCheck(DEFAULT_NUM_LEAK_TEST_ITERATIONS, expectedFixedGrowth(), fn);
    }

    public static void leakCheck(int numIterations, int maxLeakage, Callable<Void> fn) throws Exception {
        List<Long> jvmSamples = new ArrayList<>();
        List<Long> nativeSamples = new ArrayList<>();
        numIterations = Math.max(2, numIterations); // There need to be at least 2 iterations to get deltas

        getJvmMemoryInUse(); // force a few GCs to get a good baseline

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
            output += String.join("\n", resources);
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

    @Test
    public void testLeakDetectorSerial() throws Exception {
        leakCheck(20, 64, () -> {
            Thread.sleep(1);
            return null;
        });
    }

    private static int FIXED_EXECUTOR_GROWTH = 0;
    public static int expectedFixedGrowth() {
        if (FIXED_EXECUTOR_GROWTH == 0) {
            determineBaselineGrowth();
        }
        return FIXED_EXECUTOR_GROWTH;
    }

    private static void determineBaselineGrowth() {

        getJvmMemoryInUse(); // force a few GCs to get a good baseline

        List<Long> jvmSamples = new ArrayList<>();

        Callable<Void> fn = () -> {
            final ExecutorService threadPool = Executors.newFixedThreadPool(32);            
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int idx = 0; idx < DEFAULT_NUM_LEAK_TEST_ITERATIONS; ++idx) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                futures.add(future);
                final int thisIdx = idx;
                threadPool.execute(() -> {
                    try {
                        Thread.sleep(1);
                    } catch (Exception ex) {
                        // no op
                    } finally {
                        future.complete(null);
                    }
                });
            }
            for (CompletableFuture f : futures) {
                f.join();
            }
            return null;
        };

        for (int i = 0; i < DEFAULT_NUM_LEAK_TEST_ITERATIONS; ++i) {
            try {
                fn.call();
                jvmSamples.add(getJvmMemoryInUse());
            } catch (Exception ex) {
            }
        }

        // Get the median deltas
        List<Long> jvmDeltas = getDeltas(jvmSamples);
        long medianJvmDelta = jvmDeltas.get(jvmDeltas.size() / 2);
        FIXED_EXECUTOR_GROWTH = (int) medianJvmDelta;
    }

    private static void runViaThreadPool(int numThreads) throws Exception {
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        leakCheck(20, expectedFixedGrowth(), () -> {
            for (int idx = 0; idx < 100; ++idx) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                futures.add(future);
                threadPool.execute(() -> {
                    try {
                        Thread.sleep(1);
                    } catch (Exception ex) {
                        // no op
                    } finally {
                        future.complete(null);
                    }
                });
            }
            
            for (CompletableFuture f : futures) {
                f.join();
            }
            return null;
        });
    }

    @Test
    public void testLeakDetectorParallel_2() throws Exception {
        runViaThreadPool(2);
    }

    @Test
    public void testLeakDetectorParallel_8() throws Exception {
        runViaThreadPool(8);
    }

    @Test
    public void testLeakDetectorParallel_32() throws Exception {
        runViaThreadPool(32);
    }
}

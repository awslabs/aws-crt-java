/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import software.amazon.awssdk.crt.Log;

/**
 * Package-private utility that forces synchronous release of a
 * {@link ByteBuffer#allocateDirect direct ByteBuffer}'s off-heap
 * memory, matching Netty's
 * {@code io.netty.util.internal.PlatformDependent0#freeDirectBufferPrivileged}
 * behavior.
 *
 * <h2>Why this exists</h2>
 * A {@code DirectByteBuffer} wraps a native allocation obtained from
 * {@code Unsafe.allocateMemory} (which routes through {@code malloc}
 * and, for allocations above glibc's {@code MMAP_THRESHOLD} of 128
 * KiB, through {@code mmap}). The JVM tracks direct memory in an
 * internal {@code Bits.reservedMemory} counter that is checked
 * against {@code -XX:MaxDirectMemorySize} on every subsequent
 * {@code allocateDirect} call.
 *
 * <p>Normally, direct memory is released only when the DBB becomes
 * phantom-reachable AND the JVM decides to run GC AND the JVM's
 * Cleaner processes the phantom reference — which can be seconds
 * to minutes later on a quiet JVM. On the release path, three
 * things happen:</p>
 * <ol>
 *   <li>{@code Unsafe.freeMemory} runs, calling {@code free()}</li>
 *   <li>The JVM's {@code reservedMemory} counter decrements</li>
 *   <li>For {@code mmap}'d blocks, {@code munmap} runs and RSS drops</li>
 * </ol>
 *
 * <p>For a pool that trims DirectByteBuffers during idle periods,
 * "eventually via GC" is not good enough:</p>
 * <ul>
 *   <li><b>Correctness:</b> a customer running with a tight
 *       {@code -XX:MaxDirectMemorySize} can see spurious
 *       {@code OutOfMemoryError: Direct buffer memory} on re-warm
 *       because the JVM still counts the un-cleaned DBBs against
 *       the cap.</li>
 *   <li><b>Observability:</b> {@code jcmd VM.native_memory} and
 *       process RSS both remain high after trim, defeating the
 *       operator's ability to reason about pool sizing.</li>
 * </ul>
 *
 * <p>This class forces {@code Unsafe.freeMemory} to run synchronously
 * at trim time, so the effect matches the native pool's
 * {@code aws_mem_release}: microseconds, not "next GC pass".</p>
 *
 * <h2>Portability</h2>
 * Two dispatch paths, resolved once at class load:
 * <ol>
 *   <li><b>Java 9+ preferred:</b> {@code sun.misc.Unsafe#invokeCleaner}
 *       is a public API since Java 9 for exactly this purpose. No
 *       reflection into non-exported packages required.</li>
 *   <li><b>Java 8 fallback:</b> reflect into
 *       {@code sun.misc.Cleaner} via
 *       {@code ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean()}.
 *       This is the historical mechanism Netty has shipped since
 *       version 4.x and is well-vetted in production.</li>
 * </ol>
 *
 * <p>On JVMs that expose neither (rare — some hardened enterprise
 * JVMs, IBM J9 with specific security policies), we log a WARN
 * once and fall back to plain reference-drop (GC will eventually
 * clean up the memory). The pool's trim behavior degrades from
 * "immediate" to "eventual" but does not fail.</p>
 *
 * <h2>Safety</h2>
 * After {@link #free} returns, the buffer's memory has been returned
 * to the OS. Any subsequent read or write to that DirectByteBuffer
 * (via {@code get}, {@code put}, or a cached native address) is a
 * use-after-free at the native level and will crash or corrupt
 * data. Callers MUST ensure no other reference to the buffer is
 * used after this call. The DBZ pool enforces this by nulling
 * {@code slots[i]} and {@code slotAddresses[i]} under the growth
 * lock immediately before invoking this helper.
 *
 * <p>This class is package-private on purpose: it is intended only
 * for use by {@link S3DirectBufferPool}. The Cleaner-invocation
 * pattern is a footgun for arbitrary callers who may not have
 * exclusive ownership of the buffer.</p>
 */
final class DirectBufferCleaner {

    /** Marker for the "unsupported" dispatch mode. */
    private static final int MODE_UNSUPPORTED = 0;
    /** Java 9+: {@code sun.misc.Unsafe.invokeCleaner(ByteBuffer)} */
    private static final int MODE_INVOKE_CLEANER = 1;
    /** Java 8: reflect {@code ((DirectBuffer) buffer).cleaner().clean()}. */
    private static final int MODE_LEGACY_CLEANER = 2;

    /** Resolved at class-load. Never changes after. */
    private static final int MODE;

    /** Java 9+ path: cached {@code sun.misc.Unsafe} instance. */
    private static final Object UNSAFE_INSTANCE;
    /** Java 9+ path: cached {@code invokeCleaner(ByteBuffer)} method. */
    private static final Method INVOKE_CLEANER_METHOD;

    /** Java 8 path: cached {@code DirectBuffer.cleaner()} method. */
    private static final Method LEGACY_CLEANER_METHOD;
    /** Java 8 path: cached {@code Cleaner.clean()} method. */
    private static final Method LEGACY_CLEAN_METHOD;

    static {
        int mode = MODE_UNSUPPORTED;
        Object unsafeInstance = null;
        Method invokeCleaner = null;
        Method legacyCleaner = null;
        Method legacyClean = null;

        // Try Java 9+ Unsafe.invokeCleaner(ByteBuffer). This is a
        // public API since Java 9 explicitly for freeing direct
        // ByteBuffers. Preferred because it does not require
        // reflecting into non-exported internal packages, so it
        // works cleanly under the module system without needing
        // --add-opens flags.
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafeInstance = theUnsafe.get(null);
            invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            mode = MODE_INVOKE_CLEANER;
        } catch (Throwable t) {
            unsafeInstance = null;
            invokeCleaner = null;
            // Fall through — we'll try the Java 8 legacy path next.
        }

        // Try Java 8 legacy path if the Java 9+ path was not
        // available. On Java 9+ this reflection typically works too
        // but produces "illegal reflective access" warnings, so we
        // strongly prefer invokeCleaner where available.
        if (mode == MODE_UNSUPPORTED) {
            try {
                Class<?> directBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
                legacyCleaner = directBufferClass.getMethod("cleaner");
                // The Cleaner return type differs across JDK
                // versions (sun.misc.Cleaner on 8, jdk.internal.ref.Cleaner
                // on 9+ if reachable). Both expose a public clean()
                // method, so we resolve it dynamically off a real
                // instance rather than by class name. That instance
                // resolution happens in free() below via the returned
                // cleaner's runtime class.
                //
                // Get a probe buffer to resolve the clean() method
                // signature once. Allocate a tiny 1-byte DBB so the
                // native cost is negligible; free it immediately via
                // the resolved cleaner.
                ByteBuffer probe = ByteBuffer.allocateDirect(1);
                Object cleanerObj = legacyCleaner.invoke(probe);
                if (cleanerObj != null) {
                    legacyClean = cleanerObj.getClass().getMethod("clean");
                    legacyClean.setAccessible(true);
                    // Sanity: exercise it once on the probe so we
                    // fail fast at class load rather than at first
                    // trim call.
                    legacyClean.invoke(cleanerObj);
                    mode = MODE_LEGACY_CLEANER;
                }
                // If cleanerObj was null, some JVMs don't attach a
                // cleaner to trivial DBBs. Leave mode as UNSUPPORTED
                // — the fallback is "let GC handle it later".
            } catch (Throwable t) {
                legacyCleaner = null;
                legacyClean = null;
                // mode stays UNSUPPORTED
            }
        }

        MODE = mode;
        UNSAFE_INSTANCE = unsafeInstance;
        INVOKE_CLEANER_METHOD = invokeCleaner;
        LEGACY_CLEANER_METHOD = legacyCleaner;
        LEGACY_CLEAN_METHOD = legacyClean;

        if (MODE == MODE_UNSUPPORTED) {
            // Log once at class load so the operator sees the
            // degradation in server startup rather than as
            // mysteriously-lagging RSS.
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: neither sun.misc.Unsafe.invokeCleaner nor "
              + "sun.nio.ch.DirectBuffer.cleaner() is available on this JVM. "
              + "Pool trim will null references but rely on GC + Cleaner for "
              + "actual native-memory release. RSS drop and MaxDirectMemorySize "
              + "accounting will lag trim events by one or more GC cycles.");
        }
    }

    /** Not instantiable. */
    private DirectBufferCleaner() {}

    /**
     * Forces synchronous release of {@code buffer}'s off-heap
     * memory. After this call returns:
     * <ul>
     *   <li>{@code Unsafe.freeMemory} has completed synchronously
     *       (native memory returned to allocator; for mmap'd blocks,
     *       {@code munmap} has run and pages are back with the OS)</li>
     *   <li>The JVM's internal {@code Bits.reservedMemory} counter
     *       has decremented, so a subsequent {@code allocateDirect}
     *       will not fail spuriously against
     *       {@code -XX:MaxDirectMemorySize}</li>
     * </ul>
     *
     * <p>Idempotent: invoking {@code free} more than once on the
     * same buffer is a no-op after the first call (the JVM's
     * cleaner sets an internal flag so redundant calls do not
     * double-free).</p>
     *
     * <p>Silently no-ops if {@code buffer} is null, non-direct, or
     * the class-load-time platform detection concluded that no
     * synchronous release mechanism is available (see class-level
     * WARN log at startup).</p>
     *
     * <p><b>USE-AFTER-FREE HAZARD:</b> callers MUST ensure no other
     * code path reads or writes the buffer, or a cached native
     * address of it, after this call returns. See class Javadoc.</p>
     *
     * @param buffer the direct ByteBuffer to release; may be
     *               null or non-direct (no-op in both cases)
     */
    static void free(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect()) {
            return;
        }

        try {
            switch (MODE) {
                case MODE_INVOKE_CLEANER:
                    // Preferred Java 9+ path. Public API. No
                    // reflective-access warnings; works under
                    // strict module boundaries.
                    INVOKE_CLEANER_METHOD.invoke(UNSAFE_INSTANCE, buffer);
                    return;
                case MODE_LEGACY_CLEANER:
                    // Java 8 fallback. Uses sun.misc.Cleaner via
                    // reflection. Older mechanism; works on
                    // pre-module-system JVMs (and on Java 9+ when
                    // the caller has opened sun.nio.ch).
                    Object cleaner = LEGACY_CLEANER_METHOD.invoke(buffer);
                    if (cleaner != null) {
                        LEGACY_CLEAN_METHOD.invoke(cleaner);
                    }
                    return;
                case MODE_UNSUPPORTED:
                default:
                    // No mechanism available. Silent no-op — the
                    // reference-drop the caller already performed
                    // will let GC clean up eventually. WARN was
                    // logged at class load.
                    return;
            }
        } catch (Throwable t) {
            // Not fatal: the buffer will still be reclaimed by GC
            // eventually. Log a WARN with the specific failure so
            // operators can diagnose if this becomes a chronic issue
            // (e.g. custom SecurityManager blocking reflection).
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: DirectBufferCleaner.free failed; "
              + "falling back to GC-driven release. Exception: " + t);
        }
    }
}

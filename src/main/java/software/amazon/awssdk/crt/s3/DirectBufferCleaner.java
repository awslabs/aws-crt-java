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
 * memory during trim.
 * 
 * A DirectByteBuffer's off-heap memory is only released when GC
 * runs its cleaner. This class forces the release synchronously.
 * We handle Java 9+ and Java 8. If we detect a JVM that doesn't
 * expose manual release, we fall back on GC collection.
 */
final class DirectBufferCleaner {

    /** Release mechanism selected once at class load. */
    private enum Mode { UNSUPPORTED, INVOKE_CLEANER, LEGACY_CLEANER }

    /** Resolved at class-load. Never changes after. */
    private static final Mode MODE;

    /** Java 9+ path: cached {@code sun.misc.Unsafe} instance. */
    private static final Object UNSAFE_INSTANCE;
    /** Java 9+ path: cached {@code invokeCleaner(ByteBuffer)} method. */
    private static final Method INVOKE_CLEANER_METHOD;
    /** Java 8 path: cached {@code DirectBuffer.cleaner()} method. */
    private static final Method LEGACY_CLEANER_METHOD;
    /** Java 8 path: cached {@code Cleaner.clean()} method. */
    private static final Method LEGACY_CLEAN_METHOD;

    static {
        Mode mode = Mode.UNSUPPORTED;
        Object unsafeInstance = null;
        Method invokeCleaner = null;
        Method legacyCleaner = null;
        Method legacyClean = null;

        // Java 9+: Unsafe.invokeCleaner(ByteBuffer)
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafeInstance = theUnsafe.get(null);
            invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
            mode = Mode.INVOKE_CLEANER;
        } catch (Throwable t) {
            unsafeInstance = null;
            invokeCleaner = null;
        }

        // Java 8 fallback: ((DirectBuffer)buf).cleaner().clean()
        if (mode == Mode.UNSUPPORTED) {
            try {
                Class<?> directBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
                legacyCleaner = directBufferClass.getMethod("cleaner");
                ByteBuffer probe = ByteBuffer.allocateDirect(1);
                Object cleanerObj = legacyCleaner.invoke(probe);
                if (cleanerObj != null) {
                    legacyClean = cleanerObj.getClass().getMethod("clean");
                    legacyClean.setAccessible(true);
                    legacyClean.invoke(cleanerObj);
                    mode = Mode.LEGACY_CLEANER;
                }
            } catch (Throwable t) {
                legacyCleaner = null;
                legacyClean = null;
            }
        }

        MODE = mode;
        UNSAFE_INSTANCE = unsafeInstance;
        INVOKE_CLEANER_METHOD = invokeCleaner;
        LEGACY_CLEANER_METHOD = legacyCleaner;
        LEGACY_CLEAN_METHOD = legacyClean;

        if (MODE == Mode.UNSUPPORTED) {
            // Log once at class load so the operator sees the
            // degradation in server startup rather than as
            // mysteriously-lagging RSS.
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: neither sun.misc.Unsafe.invokeCleaner nor "
              + "sun.nio.ch.DirectBuffer.cleaner() is available on this JVM. "
              + "Pool trim will null references and rely on GC + Cleaner for "
              + "actual native-memory release. RSS drop and MaxDirectMemorySize "
              + "accounting will lag trim events by one or more GC cycles.");
        }
    }

    /** Not instantiable. */
    private DirectBufferCleaner() {}

    /**
     * Forces synchronous release of buffer's off-heap
     * memory. After this call returns memory has been freed. Outside
     * of the {@code UNSUPPORTED} case or a failure, there is nothing
     * remaining to GC.
     *
     * <p>Idempotent: invoking {@code free} more than once on the
     * same buffer is a no-op after the first call.</p>
     *
     * <p><b>USE-AFTER-FREE HAZARD:</b> callers MUST ensure no other
     * code path reads or writes the buffer, or a cached native
     * address of it, after this call returns.</p>
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
                case INVOKE_CLEANER:
                    INVOKE_CLEANER_METHOD.invoke(UNSAFE_INSTANCE, buffer);
                    return;
                case LEGACY_CLEANER:
                    Object cleaner = LEGACY_CLEANER_METHOD.invoke(buffer);
                    if (cleaner != null) {
                        LEGACY_CLEAN_METHOD.invoke(cleaner);
                    }
                    return;
                case UNSUPPORTED:
                default:
                    // The reference-drop the caller already performed
                    // will let GC clean up eventually. Related GC cleanup 
                    // WARN was logged at class load.
                    return;
            }
        } catch (Throwable t) {
            // Not fatal. The buffer will still be reclaimed by GC
            // eventually. Logging a WARN with the specific failure in
            // case this is a problem that needs tracking.
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: DirectBufferCleaner.free failed; "
              + "falling back to GC-driven release. Exception: " + t);
        }
    }
}

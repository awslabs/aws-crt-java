/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

import software.amazon.awssdk.crt.Log;

/**
 * One chunk of an S3 response body, read directly from the client's direct
 * buffer pool memory (see {@link S3DirectBufferPoolOptions}) with no copy.
 * Delivered to handlers that override
 * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}.
 * The chunk starts at that callback's {@code objectRangeStart}; its length
 * is {@code asByteBuffer().remaining()}.
 *
 * <p>Exception: for a GetObject that sets a part number, the native client
 * reads the body into its own temporary memory rather than the pool. Its
 * data is then copied once into a separate buffer that this object owns;
 * that buffer works the same way but is not pool memory, so it does not
 * count toward the pool's ceiling or the JVM direct memory limit.</p>
 *
 * <p>The buffer holds its memory until {@link #close()}, so you can keep it
 * after the callback returns, pass it to another thread, or hold it past
 * client shutdown. Every buffer MUST be closed, on every path including
 * exceptions: until it is closed its memory can't be reused, and once the
 * pool runs out, every request on the client waits. Holding a buffer open
 * is not flow control; to pause a download, use read backpressure (see
 * the {@code onResponseBody} overload). Once a buffer is closed, the
 * {@link ByteBuffer} from {@link #asByteBuffer()} must NOT be read. Keep a
 * reference to the {@code S3BorrowedBuffer} itself for as long as you use
 * that view: a buffer that is no longer referenced can be recovered by
 * garbage collection, which frees the memory under the view.</p>
 *
 * <p>{@link #close()} can be called any number of times.
 * {@link #toByteArray()} copies the data to a heap {@code byte[]} and
 * closes the buffer; it works once and later calls throw. Both are safe to
 * call from any thread. {@link #asByteBuffer()} returns one shared view;
 * use {@link ByteBuffer#duplicate()} if it may be read from more than one
 * place or thread.</p>
 *
 * <p>A buffer that is never closed is recovered only when it is
 * garbage-collected, after an unbounded delay, and is reported as a leak.
 * The {@code aws.crt.s3.leakdetection} system property controls the
 * report:</p>
 * <ul>
 *   <li>{@code simple} (default): logs warnings, and captures the
 *       allocation stack trace for 1 in 128 buffers.</li>
 *   <li>{@code paranoid}: captures a stack trace for every buffer. Useful
 *       for tracking down a leak, but costs more.</li>
 *   <li>{@code disabled}: no warnings.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <p><b>Reading the data during the callback.</b> The buffer is closed
 * before the callback returns, so nothing from it (the buffer or its
 * {@link ByteBuffer} view) may be kept and used afterwards.</p>
 * <pre>{@code
 * public int onResponseBody(S3BorrowedBuffer buffer, long objectRangeStart, long objectRangeEnd) {
 *     try (S3BorrowedBuffer b = buffer) {  // closes the buffer when the block exits, even on an exception
 *         ByteBuffer data = b.asByteBuffer();
 *         // Read the chunk from data here, for example by writing it to a
 *         // channel or updating a checksum.
 *     }
 *     // Grows the read window by this chunk's length, so the download keeps
 *     // going when read backpressure is enabled (ignored otherwise).
 *     return (int) (objectRangeEnd - objectRangeStart);
 * }
 * }</pre>
 *
 * <p><b>Keeping the buffer after the callback.</b> Hand it to whatever
 * reads it later, and have that code close it. If the hand-off fails, the
 * callback still owns the buffer and must close it.</p>
 * <pre>{@code
 * public int onResponseBody(S3BorrowedBuffer buffer, long objectRangeStart, long objectRangeEnd) {
 *     try {
 *         // The task now owns the buffer and closes it when done.
 *         executor.execute(() -> {
 *             try (S3BorrowedBuffer b = buffer) {  // closes the buffer when the block exits, even on an exception
 *                 ByteBuffer data = b.asByteBuffer();
 *                 // Read the chunk from data here.
 *             }
 *         });
 *     } catch (RuntimeException e) {
 *         buffer.close();  // not handed off, so close it here
 *         throw e;
 *     }
 *     // Grows the read window now, before the task has read the data. With
 *     // read backpressure, return 0 instead and call
 *     // S3MetaRequest.incrementReadWindow from the task once it is done.
 *     return (int) (objectRangeEnd - objectRangeStart);
 * }
 * }</pre>
 */
public final class S3BorrowedBuffer implements AutoCloseable {

    /**
     * Atomically guards the open-to-closed transition: exactly one caller wins
     * the CAS. A shared static updater over a plain int field avoids one
     * wrapper allocation per buffer on the per-chunk hot path.
     */
    private static final AtomicIntegerFieldUpdater<S3BorrowedBuffer> CLOSED_UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(S3BorrowedBuffer.class, "closed");

    /** 0 = open, 1 = closed. Compared-and-set by {@link #close()} and {@link #toByteArray()}. */
    private volatile int closed = 0;

    /**
     * Direct view over the leased pool memory, sliced to the response body chunk
     * length. The one shared instance returned by {@link #asByteBuffer()}.
     */
    private final ByteBuffer directView;

    /**
     * Registered release action. Invoked exactly once, either by {@link #close()}
     * (customer control) or by the cleaner daemon thread if the buffer is
     * garbage-collected without close() (it becomes phantom-reachable; see the
     * release-mechanism section below).
     */
    private final ReleaseAction release;

    /**
     * Created only by native code ({@code s_on_s3_meta_request_body_callback_borrowed} in
     * {@code src/native/s3_client.c}). Not part of the public API.
     *
     * @param ticketPtr raw native buffer-ticket address (a pool ticket, or
     *                  an owned copy of a body that had none); carries one
     *                  ticket ref owned by this object
     * @param directView direct byte buffer view sliced to the response body length,
     *                  over the ticket's memory (leased pool memory, or the
     *                  owned copy)
     */
    S3BorrowedBuffer(long ticketPtr, ByteBuffer directView) {
        this.directView = directView;

        // Sampled allocation trace (every buffer at PARANOID). Stored on the
        // ReleaseAction, not this object, so it survives GC of this buffer.
        Throwable allocationTrace = null;
        if (Leaks.LEVEL == LeakDetection.PARANOID
            || (Leaks.LEVEL == LeakDetection.SIMPLE
                && (Leaks.SAMPLE_COUNTER.getAndIncrement() & Leaks.SAMPLE_MASK) == 0)) {
            allocationTrace = new Throwable(
                "S3BorrowedBuffer allocation site (captured for leak detection)");
        }
        this.release = new ReleaseAction(this, ticketPtr, allocationTrace);
        GcFallback.LIVE.add(this.release);
    }

    /**
     * Returns a {@link ByteBuffer} view over the underlying pool memory. The
     * returned buffer is a shared reference. Do not mutate its position or
     * limit if other threads may be reading concurrently. Use
     * {@link ByteBuffer#duplicate()} or {@link ByteBuffer#slice()} for a
     * private view.
     *
     * <p>The returned buffer is a DIRECT buffer over pool memory, not a
     * heap buffer: it has no backing array ({@link ByteBuffer#hasArray()}
     * is {@code false}, and {@link ByteBuffer#array()} throws
     * {@link UnsupportedOperationException}). Read it through the
     * {@code ByteBuffer} API ({@code get}, bulk {@code get(byte[])},
     * channel writes). If you need a {@code byte[]}, or bytes that outlive
     * this buffer, use {@link #toByteArray()} instead.</p>
     *
     * <p>The memory under this view, and under any duplicate or slice of
     * it, is valid only while this {@code S3BorrowedBuffer} is open and
     * still referenced. Once it is closed (or recovered by garbage
     * collection), the memory may be reused for other data: reading the
     * view then returns unrelated bytes rather than failing, and writing
     * to it corrupts another download or upload.</p>
     *
     * @return a direct {@link ByteBuffer} sliced to the response body length
     * @throws IllegalStateException if this borrowed buffer has been closed
     */
    public ByteBuffer asByteBuffer() {
        if (closed != 0) {
            throw new IllegalStateException("S3BorrowedBuffer has been closed");
        }
        return directView;
    }

    /**
     * Copies the buffer contents into a new heap {@code byte[]} (safe to hold
     * indefinitely) and closes this borrowed buffer, returning its pool
     * memory so the client can reuse it for further parts of this or other
     * requests. Subsequent {@link #asByteBuffer()} or {@code toByteArray()}
     * calls throw.
     *
     * @return a heap byte[] copy of the buffer contents
     * @throws IllegalStateException if this borrowed buffer has been closed
     */
    public byte[] toByteArray() {
        // Win the close CAS first so a concurrent close() cannot release the
        // pool memory mid-copy. The loser no-ops. GC fallback cannot fire mid-copy
        // because this call keeps `this` reachable until performRelease().
        if (!CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            throw new IllegalStateException("S3BorrowedBuffer has been closed");
        }
        try {
            // duplicate() so we do not mutate the shared view's position
            ByteBuffer dup = directView.duplicate();
            byte[] copy = new byte[dup.remaining()];
            dup.get(copy);
            return copy;
        } finally {
            // this call won the Compare and Set, so it owns the release
            performRelease();
        }
    }

    /**
     * Releases the pool memory so the client can reuse it for further parts
     * of this or other requests. Idempotent, safe to call multiple times from
     * any thread. The first call performs the release; subsequent calls are
     * no-ops.
     *
     * <p>After {@code close()} returns, the {@link ByteBuffer} previously
     * returned from {@link #asByteBuffer()} MUST NOT be read. The underlying
     * pool memory may be reused by another request.</p>
     */
    @Override
    public void close() {
        if (CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            performRelease();
        }
    }

    /**
     * Shared release steps for {@link #close()} and {@link #toByteArray()}.
     * Precondition: caller won the {@code closed} 0-to-1 CAS. Releases
     * the ticket (ReleaseAction.run() is CAS-idempotent), removes from
     * LIVE, and clears the phantom ref so a closed buffer never reports
     * as a leak.
     */
    private void performRelease() {
        release.run();
        GcFallback.LIVE.remove(release);
        release.clear();
    }

    /* ==================================================================== */
    /* Release mechanism + GC fallback for unclosed buffers                 */
    /* ==================================================================== */

    /*
     * Why this exists: customers will sometimes forget close(). Without a
     * fallback each forgotten buffer pins its pool memory forever, and a hard-capped
     * pool stalls silently. Each buffer registers a phantom reference the JVM
     * enqueues after GC; a daemon thread releases the ticket. This is a safety
     * net, not a lifecycle strategy, so leaks are also reported (see the
     * leak-detection section below).
     */

    /**
     * GC-fallback state, initialized on first use (initialization-on-demand
     * holder). It must NOT live in this class's own static initializer: JNI
     * ID caching at library load ({@code GetMethodID} in java_class_ids.c)
     * initializes S3BorrowedBuffer in every process, so a static block here
     * would start the cleaner thread even when DBZ is never used. The
     * constructor is the first touch, so the thread starts with the first
     * borrowed buffer.
     */
    private static final class GcFallback {
        /** Receives phantom refs for buffers GC'd without close; drained by the daemon cleaner thread. */
        static final ReferenceQueue<S3BorrowedBuffer> RELEASE_QUEUE = new ReferenceQueue<>();

        /** Keeps ReleaseActions strongly reachable so the phantom refs can enqueue; pruned on release. */
        static final Set<ReleaseAction> LIVE = Collections.newSetFromMap(new ConcurrentHashMap<>());

        static {
            Thread t = new Thread(S3BorrowedBuffer::cleanerLoop, "aws-crt-s3-borrowed-buffer-cleaner");
            t.setDaemon(true);
            t.start();
        }

        private GcFallback() {}
    }

    /**
     * Per-buffer cleanup action, doubling as the phantom reference for the
     * GC fallback. Kept strongly reachable via {@code GcFallback.LIVE} until released.
     * MUST NOT reference the enclosing buffer; a strong ref to the referent would
     * prevent the phantom reference from ever enqueueing.
     */
    private static final class ReleaseAction extends PhantomReference<S3BorrowedBuffer> {

        /** Same idiom as CLOSED_UPDATER: one shared static, no per-buffer wrapper allocation. */
        private static final AtomicIntegerFieldUpdater<ReleaseAction> RELEASED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ReleaseAction.class, "released");

        /** 0 = not released, 1 = released. Compared-and-set once by {@link #run()}. */
        @SuppressWarnings("unused") // accessed only via RELEASED_UPDATER reflection
        private volatile int released = 0;

        private final long ticketPtr;

        /** Sampled allocation trace for leak reports; null when not sampled. */
        private final Throwable allocationTrace;

        ReleaseAction(S3BorrowedBuffer referent, long ticketPtr, Throwable allocationTrace) {
            super(referent, GcFallback.RELEASE_QUEUE);
            this.ticketPtr = ticketPtr;
            this.allocationTrace = allocationTrace;
        }

        /**
         * Releases the ticket exactly once; later calls are no-ops.
         *
         * @return true if THIS call performed the release. The cleaner loop
         *         uses this to distinguish a genuine leak from a benign duplicate
         */
        boolean run() {
            if (RELEASED_UPDATER.compareAndSet(this, 0, 1)) {
                try {
                    nativeReleaseTicket(ticketPtr);
                } catch (Throwable t) {
                    // Swallow: this action may be called from the cleaner
                    // daemon thread, and a throw there would kill the
                    // thread and orphan future release actions.
                    Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                        "S3BorrowedBuffer: nativeReleaseTicket threw during release: " + t);
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Daemon loop draining {@code GcFallback.RELEASE_QUEUE}: the GC fallback for
     * buffers never closed. A properly closed buffer can never arrive here
     * (close() clears the phantom ref while still strongly reachable), so
     * every arrival whose run() performs the release is by definition a leak.
     */
    private static void cleanerLoop() {
        while (true) {
            try {
                ReleaseAction ra = (ReleaseAction) GcFallback.RELEASE_QUEUE.remove();
                boolean thisCallReleased = ra.run();
                GcFallback.LIVE.remove(ra);
                ra.clear();
                if (thisCallReleased && Leaks.LEVEL != LeakDetection.DISABLED) {
                    reportLeak(ra);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                // Never let the cleaner thread die; log and continue.
                Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                    "S3BorrowedBuffer cleaner loop swallowed exception: " + t);
            }
        }
    }

    /**
     * Releases this object's reference on the native buffer ticket; see the
     * native implementation in s3_client.c for what the last release frees.
     */
    private static native void nativeReleaseTicket(long ticketPtr);

    /* ==================================================================== */
    /* Leak detection (diagnostics only; no effect on buffer lifetime)      */
    /* ==================================================================== */

    /** Leak-report level: DISABLED, SIMPLE (sample traces 1-in-128), PARANOID (trace every buffer). */
    private enum LeakDetection { DISABLED, SIMPLE, PARANOID }

    /**
     * Leak-detection state, initialized on first use for the same reason as
     * {@code GcFallback}: the system property is read, and the counters
     * allocated, only once a borrowed buffer exists.
     */
    private static final class Leaks {
        /** From aws.crt.s3.leakdetection at first use. Unrecognized values -> SIMPLE (a typo must not disable reporting). */
        static final LeakDetection LEVEL;

        static {
            String prop = System.getProperty("aws.crt.s3.leakdetection", "simple").trim();
            if (prop.equalsIgnoreCase("disabled")) {
                LEVEL = LeakDetection.DISABLED;
            } else if (prop.equalsIgnoreCase("paranoid")) {
                LEVEL = LeakDetection.PARANOID;
            } else {
                LEVEL = LeakDetection.SIMPLE;
            }
        }

        /** SIMPLE-mode sampling: every 128th buffer captures an allocation trace. */
        static final AtomicLong SAMPLE_COUNTER = new AtomicLong();
        static final long SAMPLE_MASK = 127; // 1 in 128

        /** Running total of detected leaks, included in every report. */
        static final AtomicLong LEAK_COUNT = new AtomicLong();

        /** Dedup of reported allocation sites (frame hash). Bounded; when full, new sites still report. */
        static final Set<Integer> REPORTED_LEAK_SITES = Collections.newSetFromMap(new ConcurrentHashMap<>());
        static final int MAX_REPORTED_LEAK_SITES = 1024;

        /** Ensures the "untraced leaks occurred" summary WARN logs exactly once. */
        static final AtomicBoolean UNTRACED_LEAK_REPORTED = new AtomicBoolean();

        private Leaks() {}
    }

    /** Shared opener for both leak WARN variants below. */
    private static final String LEAK_WARNING_PREAMBLE =
        "S3BorrowedBuffer LEAK detected: a borrowed buffer was garbage-collected without close(). "
      + "The pool memory was recovered by the GC fallback, but the delay is unbounded and can stall "
      + "downloads via pool exhaustion. close() every S3BorrowedBuffer.";

    /**
     * WARNs for a detected leak. Traced leaks log once per unique allocation
     * site with the creation stack; untraced leaks log one summary ever
     * (pointing at the paranoid level) and are counted thereafter.
     */
    private static void reportLeak(ReleaseAction ra) {
        long totalLeaks = Leaks.LEAK_COUNT.incrementAndGet();

        if (ra.allocationTrace == null) {
            // Untraced leak: one summary WARN, ever. A recurring leak
            // site will eventually hit the 1-in-128 sample and produce
            // a traced report below.
            if (Leaks.UNTRACED_LEAK_REPORTED.compareAndSet(false, true)) {
                Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                    LEAK_WARNING_PREAMBLE
                  + " This buffer's allocation "
                  + "site was not sampled; set -Daws.crt.s3.leakdetection=paranoid to capture a stack trace "
                  + "for every buffer. Further untraced-leak warnings are suppressed. (total leaks so far: "
                  + totalLeaks + ")");
            }
            return;
        }

        // Traced leak: dedup on the allocation site so one leaky loop
        // doesn't flood the logs.
        StackTraceElement[] frames = ra.allocationTrace.getStackTrace();
        // Hash collisions may suppress distinct sites, accepted for a best-effort diagnostic.
        Integer siteHash = Arrays.hashCode(frames);
        if (Leaks.REPORTED_LEAK_SITES.contains(siteHash)) {
            return;
        }
        if (Leaks.REPORTED_LEAK_SITES.size() < Leaks.MAX_REPORTED_LEAK_SITES) {
            Leaks.REPORTED_LEAK_SITES.add(siteHash);
        }
        // If the registry is full we fall through and report anyway.
        // Duplicate warnings beat silence.

        StringWriter sw = new StringWriter();
        ra.allocationTrace.printStackTrace(new PrintWriter(sw));
        Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
            LEAK_WARNING_PREAMBLE
          + " (total leaks so far: "
          + totalLeaks + ") Allocation site:\n" + sw);
    }

}

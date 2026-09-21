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
 * A borrowed view into an S3 response body chunk backed by pooled
 * direct-buffer memory. Delivered to handlers that opt in to zero-copy
 * delivery by overriding
 * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}.
 *
 * <p>The buffer keeps its pool slot alive until {@link #close()}, so it may
 * be held across async boundaries (reactive publishers, async file writes)
 * and even past client shutdown. Every buffer MUST be closed: after
 * {@code close()} the slot may be recycled to another request, so the
 * {@link ByteBuffer} from {@link #asByteBuffer()} must NOT be read
 * afterwards. Unclosed buffers are recovered on GC and reported as leaks
 * per the {@code aws.crt.s3.leakdetection} system property
 * ({@code disabled} | {@code simple}, the default | {@code paranoid}).</p>
 *
 * <p>{@link #close()} and {@link #toByteArray()} (copy to heap, then close)
 * are idempotent and safe from any thread. {@link #asByteBuffer()} returns a
 * shared view — use {@link ByteBuffer#duplicate()} for independent
 * position/limit.</p>
 */
public final class S3BorrowedBuffer implements AutoCloseable {

    /** Leak-report level: DISABLED, SIMPLE (sample traces 1-in-128), PARANOID (trace every buffer). */
    private enum LeakDetection { DISABLED, SIMPLE, PARANOID }

    /** From aws.crt.s3.leakdetection at class load. Unrecognized values -> SIMPLE (a typo must not disable reporting). */
    private static final LeakDetection LEAK_DETECTION_LEVEL;

    static {
        String prop = System.getProperty("aws.crt.s3.leakdetection", "simple").trim();
        if (prop.equalsIgnoreCase("disabled")) {
            LEAK_DETECTION_LEVEL = LeakDetection.DISABLED;
        } else if (prop.equalsIgnoreCase("paranoid")) {
            LEAK_DETECTION_LEVEL = LeakDetection.PARANOID;
        } else {
            LEAK_DETECTION_LEVEL = LeakDetection.SIMPLE;
        }
    }

    /** SIMPLE-mode sampling: every 128th buffer captures an allocation trace. */
    private static final AtomicLong SAMPLE_COUNTER = new AtomicLong();
    private static final long SAMPLE_MASK = 127; // 1 in 128

    /** Running total of detected leaks, included in every report. */
    private static final AtomicLong LEAK_COUNT = new AtomicLong();

    /** Dedup of reported allocation sites (frame hash). Bounded; when full, new sites still report. */
    private static final Set<Integer> REPORTED_LEAK_SITES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final int MAX_REPORTED_LEAK_SITES = 1024;

    /** Ensures the "untraced leaks occurred" summary WARN logs exactly once. */
    private static final AtomicBoolean UNTRACED_LEAK_REPORTED = new AtomicBoolean();

    /** Receives phantom refs for buffers GC'd without close; drained by the daemon cleaner thread. */
    private static final ReferenceQueue<S3BorrowedBuffer> RELEASE_QUEUE = new ReferenceQueue<>();

    /** Keeps ReleaseActions strongly reachable so the phantom refs can enqueue; pruned on release. */
    private static final Set<ReleaseAction> LIVE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        Thread t = new Thread(S3BorrowedBuffer::cleanerLoop, "aws-crt-s3-borrowed-buffer-cleaner");
        t.setDaemon(true);
        t.start();
    }

    private static final AtomicIntegerFieldUpdater<S3BorrowedBuffer> CLOSED_UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(S3BorrowedBuffer.class, "closed");

    /** Raw pointer to the underlying aws_s3_buffer_ticket. Valid until nativeReleaseTicket is called. */
    private final long ticketPtr;

    /**
     * Direct view over pool slot memory, sliced to the response body chunk length.
     * Shared across all callers of {@link #asByteBuffer()}; use
     * {@link ByteBuffer#duplicate()} or {@link ByteBuffer#slice()} if you need
     * an independent position/limit.
     */
    private final ByteBuffer view;

    /**
     * Registered release action. Invoked exactly once — either by {@link #close()}
     * (customer control) or by the cleaner daemon thread when this buffer
     * becomes phantom-reachable.
     */
    private final ReleaseAction release;

    /** 0 = open, 1 = closed. CAS'd by {@link #close()}. */
    @SuppressWarnings("unused") // read by CLOSED_UPDATER
    private volatile int closed = 0;

    /**
     * Called only from native ({@code s_on_s3_meta_request_body_callback_borrowed} in
     * {@code src/native/s3_client.c}). Not part of the public API.
     *
     * @param ticketPtr raw {@code struct aws_s3_buffer_ticket *} address; native
     *                  holds one ref count for us; nativeReleaseTicket decrements it
     * @param view      direct byte buffer view sliced to the response body length,
     *                  addressed against the ticket's slot memory
     */
    S3BorrowedBuffer(long ticketPtr, ByteBuffer view) {
        this.ticketPtr = ticketPtr;
        this.view = view;
        // Leak detection: sampled allocation trace (all buffers at PARANOID).
        // The trace travels with the ReleaseAction — NOT this object — so it
        // survives to the leak report after this buffer has been GC'd.
        Throwable allocationTrace = null;
        if (LEAK_DETECTION_LEVEL == LeakDetection.PARANOID
            || (LEAK_DETECTION_LEVEL == LeakDetection.SIMPLE
                && (SAMPLE_COUNTER.getAndIncrement() & SAMPLE_MASK) == 0)) {
            allocationTrace = new Throwable(
                "S3BorrowedBuffer allocation site (captured for leak detection)");
        }
        this.release = new ReleaseAction(this, ticketPtr, allocationTrace);
        LIVE.add(this.release);
    }

    /**
     * Returns a {@link ByteBuffer} view over the underlying pool slot. The
     * returned buffer is a shared reference — do not mutate its position or
     * limit if other threads may be reading concurrently. Use
     * {@link ByteBuffer#duplicate()} or {@link ByteBuffer#slice()} for a
     * private view.
     *
     * @return a direct {@link ByteBuffer} sliced to the response body length
     * @throws IllegalStateException if this borrowed buffer has been closed
     */
    public ByteBuffer asByteBuffer() {
        if (closed != 0) {
            throw new IllegalStateException("S3BorrowedBuffer has been closed");
        }
        return view;
    }

    /**
     * Copies the buffer contents into a new heap {@code byte[]} (safe to hold
     * indefinitely) and closes this borrowed buffer — subsequent
     * {@link #asByteBuffer()} or {@code toByteArray()} calls throw.
     *
     * @return a heap byte[] copy of the buffer contents
     * @throws IllegalStateException if this borrowed buffer has been closed
     */
    public byte[] toByteArray() {
        // Win the close CAS FIRST so a concurrent close() (or the GC
        // fallback) cannot release the pool slot while we are still copying
        // from it — the losing caller becomes a no-op, and the native
        // release below runs strictly after the copy completes.
        if (!CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            throw new IllegalStateException("S3BorrowedBuffer has been closed");
        }
        try {
            // duplicate() so we do not mutate the shared view's position
            ByteBuffer dup = view.duplicate();
            byte[] copy = new byte[dup.remaining()];
            dup.get(copy);
            return copy;
        } finally {
            // Same post-CAS steps as close(); this call owns the transition.
            release.run();
            LIVE.remove(release);
            release.clear();
        }
    }

    /**
     * Releases the pool slot. Idempotent — safe to call multiple times from
     * any thread. The first call performs the release; subsequent calls are
     * no-ops.
     *
     * <p>After {@code close()} returns, the {@link ByteBuffer} previously
     * returned from {@link #asByteBuffer()} MUST NOT be read — the underlying
     * slot may be reused by another concurrent meta-request.</p>
     */
    @Override
    public void close() {
        if (CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            // Run the release synchronously. The cleaner thread may still
            // pull this from the queue later; ReleaseAction.run() is
            // idempotent so a duplicate call is a no-op.
            release.run();
            LIVE.remove(release);
            release.clear();  // clear the phantom reference so it doesn't enqueue
        }
    }

    /**
     * Per-buffer cleanup action, doubling as the phantom reference for the
     * GC fallback. Kept strongly reachable via {@link #LIVE} until released.
     * MUST NOT reference the enclosing buffer — a strong ref to the referent
     * would prevent the phantom reference from ever enqueueing.
     */
    private static final class ReleaseAction extends PhantomReference<S3BorrowedBuffer> {
        private final long ticketPtr;
        private final AtomicBoolean released = new AtomicBoolean(false);

        /** Sampled allocation trace for leak reports; null when not sampled. */
        private final Throwable allocationTrace;

        ReleaseAction(S3BorrowedBuffer referent, long ticketPtr, Throwable allocationTrace) {
            super(referent, RELEASE_QUEUE);
            this.ticketPtr = ticketPtr;
            this.allocationTrace = allocationTrace;
        }

        /**
         * Releases the ticket exactly once; later calls are no-ops.
         *
         * @return true if THIS call performed the release — the cleaner loop
         *         uses this to distinguish a genuine leak from a benign duplicate
         */
        boolean run() {
            if (released.compareAndSet(false, true)) {
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
     * Daemon loop draining {@link #RELEASE_QUEUE}: the GC fallback for
     * buffers never closed. A properly closed buffer can never arrive here
     * (close() clears the phantom ref while still strongly reachable), so
     * every arrival whose run() performs the release is by definition a leak.
     */
    private static void cleanerLoop() {
        while (true) {
            try {
                ReleaseAction ra = (ReleaseAction) RELEASE_QUEUE.remove();
                boolean thisCallReleased = ra.run();
                LIVE.remove(ra);
                ra.clear();
                if (thisCallReleased && LEAK_DETECTION_LEVEL != LeakDetection.DISABLED) {
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
     * WARNs for a detected leak. Traced leaks log once per unique allocation
     * site with the creation stack; untraced leaks log one summary ever
     * (pointing at the paranoid level) and are counted thereafter.
     */
    private static void reportLeak(ReleaseAction ra) {
        long totalLeaks = LEAK_COUNT.incrementAndGet();

        if (ra.allocationTrace == null) {
            // Untraced leak: one summary WARN, ever. A recurring leak
            // site will eventually hit the 1-in-128 sample and produce
            // a traced report below.
            if (UNTRACED_LEAK_REPORTED.compareAndSet(false, true)) {
                Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                    "S3BorrowedBuffer LEAK detected: a borrowed buffer was garbage-collected without close(). "
                  + "The pool slot was recovered by the GC fallback, but the delay is unbounded and can stall "
                  + "downloads via pool exhaustion — close() every S3BorrowedBuffer. This buffer's allocation "
                  + "site was not sampled; set -Daws.crt.s3.leakdetection=paranoid to capture a stack trace "
                  + "for every buffer. Further untraced-leak warnings are suppressed. (total leaks so far: "
                  + totalLeaks + ")");
            }
            return;
        }

        // Traced leak: dedup on the allocation site so one leaky loop
        // doesn't flood the logs.
        StackTraceElement[] frames = ra.allocationTrace.getStackTrace();
        // Hash collisions may suppress distinct sites — accepted for a best-effort diagnostic.
        Integer siteHash = Arrays.hashCode(frames);
        if (REPORTED_LEAK_SITES.contains(siteHash)) {
            return;
        }
        if (REPORTED_LEAK_SITES.size() < MAX_REPORTED_LEAK_SITES) {
            REPORTED_LEAK_SITES.add(siteHash);
        }
        // If the registry is full we fall through and report anyway —
        // duplicate warnings beat silence.

        StringWriter sw = new StringWriter();
        ra.allocationTrace.printStackTrace(new PrintWriter(sw));
        Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
            "S3BorrowedBuffer LEAK detected: a borrowed buffer was garbage-collected without close(). "
          + "The pool slot was recovered by the GC fallback, but the delay is unbounded and can stall "
          + "downloads via pool exhaustion — close() every S3BorrowedBuffer. (total leaks so far: "
          + totalLeaks + ") Allocation site:\n" + sw);
    }

    /**
     * Releases one reference on the aws_s3_buffer_ticket at the given pointer.
     * When the last reference drops the slot returns to the pool. Safe to
     * call after JVM shutdown has begun; a null/zero pointer is a no-op.
     */
    private static native void nativeReleaseTicket(long ticketPtr);
}

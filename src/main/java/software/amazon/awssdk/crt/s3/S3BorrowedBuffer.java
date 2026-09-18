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
 * A borrowed view into an S3 response body chunk backed by pooled direct-buffer memory.
 *
 * <p>Delivered to handlers that opt in to zero-copy delivery by overriding
 * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}.
 * The borrowed buffer keeps the underlying pool slot alive until {@link #close()}
 * is called, letting the customer hold the buffer across async boundaries
 * (e.g. queuing into a reactive publisher, writing to
 * {@code AsynchronousFileChannel}).</p>
 *
 * <h3>Lifetime contract</h3>
 * <ul>
 *   <li>The {@link ByteBuffer} returned by {@link #asByteBuffer()} is a direct
 *       view over pool memory. It is valid until {@link #close()} is called
 *       on this {@code S3BorrowedBuffer}.</li>
 *   <li>After {@link #close()} the pool slot may be recycled to another
 *       meta-request. Do NOT retain the {@code ByteBuffer} reference past
 *       {@link #close()}; reading it afterwards may return stale or
 *       corrupted bytes.</li>
 *   <li>Every acquired {@code S3BorrowedBuffer} MUST be closed by the customer.
 *       Failure to close leaks the pool slot; sustained leaks exhaust the pool
 *       and stall further downloads. A phantom-reference-based fallback
 *       releases the slot on GC, but relying on GC for pool health is not
 *       a supported pattern.</li>
 *   <li>{@link #close()} is idempotent. Multiple calls are safe and cheap.</li>
 *   <li>{@link #toByteArray()} copies the buffer contents to a heap {@code byte[]}
 *       and closes automatically. Use this when a long-lived heap copy is
 *       preferable to holding the pool slot.</li>
 * </ul>
 *
 * <h3>Thread safety</h3>
 * <p>{@link #close()} and {@link #toByteArray()} are safe to call from any
 * thread. {@link #asByteBuffer()} is also thread-safe, but callers must NOT
 * mutate the position/limit of the returned {@link ByteBuffer} while
 * concurrently reading from it on another thread — use
 * {@link ByteBuffer#duplicate()} or {@link ByteBuffer#slice()} for defensive
 * per-thread views.</p>
 *
 * <h3>Leak detection</h3>
 * <p>A buffer GC'd without {@link #close()} is a leak: the slot is
 * recovered by the GC fallback, but only after an unbounded delay that
 * can exhaust the pool. Leaks are reported per the
 * {@code aws.crt.s3.leakdetection} system property: {@code disabled},
 * {@code simple} (default — WARN on every leak, allocation trace sampled
 * 1-in-128), {@code paranoid} (trace every buffer; for tests). Reports
 * are deduplicated per allocation site.</p>
 */
public final class S3BorrowedBuffer implements AutoCloseable {

    /** Leak-detection level: no reporting. */
    private static final int LEAK_DETECTION_DISABLED = 0;
    /** Leak-detection level: report all leaks; sample traces 1-in-128. */
    private static final int LEAK_DETECTION_SIMPLE = 1;
    /** Leak-detection level: report all leaks; trace every buffer. */
    private static final int LEAK_DETECTION_PARANOID = 2;

    /**
     * Resolved once at class load from the {@code aws.crt.s3.leakdetection}
     * system property. Unrecognized values fall back to SIMPLE (fail-safe:
     * a typo should not silently disable leak reporting).
     */
    private static final int LEAK_DETECTION_LEVEL;

    static {
        String prop = System.getProperty("aws.crt.s3.leakdetection", "simple").trim();
        if (prop.equalsIgnoreCase("disabled")) {
            LEAK_DETECTION_LEVEL = LEAK_DETECTION_DISABLED;
        } else if (prop.equalsIgnoreCase("paranoid")) {
            LEAK_DETECTION_LEVEL = LEAK_DETECTION_PARANOID;
        } else {
            LEAK_DETECTION_LEVEL = LEAK_DETECTION_SIMPLE;
        }
    }

    /**
     * Sampling counter for SIMPLE mode: buffer #0, #128, #256, ... capture
     * an allocation trace. Power-of-two mask keeps the hot-path cost to one
     * atomic increment and a bitwise AND.
     */
    private static final AtomicLong SAMPLE_COUNTER = new AtomicLong();
    private static final long SAMPLE_MASK = 127; // 1 in 128

    /** Running total of detected leaks, included in every report. */
    private static final AtomicLong LEAK_COUNT = new AtomicLong();

    /**
     * Dedup registry of allocation sites already reported (hash of the
     * captured stack frames). Bounded so a pathological application cannot
     * grow it without limit; once full, new sites are still reported (we
     * prefer duplicate warnings over silence).
     */
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
        // Leak detection: capture an allocation stack trace for a sampled
        // subset of buffers (all of them at PARANOID). The trace travels
        // with the ReleaseAction — NOT with this object — so it survives
        // to the leak report after this buffer has been GC'd. Cost when
        // sampled: one Throwable fill-in (~1-10 us); when not sampled:
        // one atomic increment.
        Throwable allocationTrace = null;
        if (LEAK_DETECTION_LEVEL == LEAK_DETECTION_PARANOID
            || (LEAK_DETECTION_LEVEL == LEAK_DETECTION_SIMPLE
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
     * Copies the borrowed buffer contents into a new heap {@code byte[]} and
     * closes this borrowed buffer. The returned array is safe to hold
     * indefinitely.
     *
     * <p>Equivalent to reading through {@link #asByteBuffer()} into a fresh
     * {@code byte[]} of size {@code asByteBuffer().remaining()} and then
     * calling {@link #close()}, but avoids position mutation on the shared
     * view.</p>
     *
     * <p>Because this method closes the buffer, any subsequent
     * {@link #asByteBuffer()} or {@code toByteArray()} call throws
     * {@link IllegalStateException}.</p>
     *
     * @return a heap byte[] containing a copy of the buffer contents
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
     * Cleanup action attached to each borrowed buffer as a phantom reference.
     * Kept strongly reachable via {@link #LIVE} until its release runs.
     * Holds only primitive state (the ticket pointer) — must not reference
     * the enclosing {@code S3BorrowedBuffer} instance or the phantom
     * reference will never see it become unreachable.
     */
    private static final class ReleaseAction extends PhantomReference<S3BorrowedBuffer> {
        private final long ticketPtr;
        private final AtomicBoolean released = new AtomicBoolean(false);

        /**
         * Allocation stack trace captured at construction when this buffer
         * was sampled for leak detection; {@code null} when not sampled.
         * Deliberately does NOT reference the buffer itself (a strong ref
         * from the action to the referent would prevent the phantom
         * reference from ever being enqueued).
         */
        private final Throwable allocationTrace;

        ReleaseAction(S3BorrowedBuffer referent, long ticketPtr, Throwable allocationTrace) {
            super(referent, RELEASE_QUEUE);
            this.ticketPtr = ticketPtr;
            this.allocationTrace = allocationTrace;
        }

        /**
         * Release the ticket exactly once. Called synchronously from
         * {@link S3BorrowedBuffer#close()} or asynchronously from the
         * cleaner thread. Second and subsequent calls are no-ops.
         *
         * @return {@code true} if THIS call performed the release,
         *         {@code false} if it had already been released. The
         *         cleaner loop uses this to distinguish a genuine leak
         *         (GC-path release) from a benign duplicate.
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
     * Daemon-thread loop: drains {@link #RELEASE_QUEUE}, invoking each
     * enqueued {@link ReleaseAction}. Handles GC-fallback release for
     * borrowed buffers that were never closed explicitly.
     *
     * <p>Leak detection: a properly closed buffer never arrives here —
     * {@link #close()} clears the phantom reference while the buffer is
     * still strongly reachable, so it can never be enqueued. Any action
     * dequeued below whose {@code run()} actually performs the release
     * is therefore a leak (buffer GC'd without close), and is reported
     * per the {@code aws.crt.s3.leakdetection} level.</p>
     */
    private static void cleanerLoop() {
        while (true) {
            try {
                ReleaseAction ra = (ReleaseAction) RELEASE_QUEUE.remove();
                boolean thisCallReleased = ra.run();
                LIVE.remove(ra);
                ra.clear();
                if (thisCallReleased && LEAK_DETECTION_LEVEL != LEAK_DETECTION_DISABLED) {
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
     * Emits a WARN for a detected leak, deduplicated per allocation site.
     *
     * <p>Traced leaks (this buffer was sampled at construction) log once
     * per unique allocation site with the full creation stack trace.
     * Untraced leaks log a single summary the first time, directing the
     * operator to the {@code paranoid} level; subsequent untraced leaks
     * are counted but not logged (the running count appears in every
     * traced report).</p>
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

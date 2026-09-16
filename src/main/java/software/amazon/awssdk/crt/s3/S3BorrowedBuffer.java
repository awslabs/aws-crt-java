/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import software.amazon.awssdk.crt.Log;

/**
 * A borrowed view into an S3 response body chunk backed by DBZ pool memory.
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
 */
public final class S3BorrowedBuffer implements AutoCloseable {

    /**
     * Reference queue that receives PhantomRef notifications when
     * {@code S3BorrowedBuffer} instances become phantom-reachable
     * (i.e. GC'd without explicit close). A single daemon thread drains
     * this queue and releases the associated tickets.
     */
    private static final ReferenceQueue<S3BorrowedBuffer> RELEASE_QUEUE = new ReferenceQueue<>();

    /**
     * Strong references to pending release actions. Without this set, the
     * PhantomReference objects themselves would be GC'd before their
     * referents, breaking the notification. Entries are removed by the
     * cleaner thread after the release runs, or by {@link #close()} when
     * the customer closes explicitly.
     */
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
     * Called only from native ({@code s_on_body_ex_dbz} in
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
        this.release = new ReleaseAction(this, ticketPtr);
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
     * @return a heap byte[] containing a copy of the buffer contents
     * @throws IllegalStateException if this borrowed buffer has been closed
     */
    public byte[] toByteArray() {
        if (closed != 0) {
            throw new IllegalStateException("S3BorrowedBuffer has been closed");
        }
        // duplicate() so we do not mutate the shared view's position
        ByteBuffer dup = view.duplicate();
        byte[] copy = new byte[dup.remaining()];
        dup.get(copy);
        close();
        return copy;
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

        ReleaseAction(S3BorrowedBuffer referent, long ticketPtr) {
            super(referent, RELEASE_QUEUE);
            this.ticketPtr = ticketPtr;
        }

        /**
         * Release the ticket exactly once. Called synchronously from
         * {@link S3BorrowedBuffer#close()} or asynchronously from the
         * cleaner thread. Second and subsequent calls are no-ops.
         */
        void run() {
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
            }
        }
    }

    /**
     * Daemon-thread loop: drains {@link #RELEASE_QUEUE}, invoking each
     * enqueued {@link ReleaseAction}. Handles GC-fallback release for
     * borrowed buffers that were never closed explicitly.
     */
    private static void cleanerLoop() {
        while (true) {
            try {
                ReleaseAction ra = (ReleaseAction) RELEASE_QUEUE.remove();
                ra.run();
                LIVE.remove(ra);
                ra.clear();
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
     * Releases one reference on the aws_s3_buffer_ticket at the given pointer.
     * When the last reference drops the slot returns to the DBZ pool. Safe to
     * call after JVM shutdown has begun; a null/zero pointer is a no-op.
     */
    private static native void nativeReleaseTicket(long ticketPtr);
}

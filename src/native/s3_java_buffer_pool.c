/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

/*
 * Java-backed implementation of the aws_s3_buffer_pool vtable.
 *
 * PURPOSE
 * -------
 * Provide aws-c-s3 with a buffer pool whose tickets, when claimed,
 * expose memory that is simultaneously native-addressable (so the
 * HTTP receive path can memcpy into it directly) and Java-visible
 * (so the body callback can deliver it as a ByteBuffer slice without
 * an extra JNI copy).
 *
 * The pool's memory is owned by the S3DirectBufferPool Java object:
 * blocks of contiguous part-sized slots, plus dedicated buffers for
 * reservations larger than a slot run (see that class). Each
 * ticket leases one slot, a contiguous run of slots, or a dedicated
 * buffer. When the ticket is released, the lease returns to the Java
 * pool.
 *
 * LIFETIME INVARIANTS
 * -------------------
 * 1. The Java pool object outlives every ticket. This is enforced by
 *    EACH TICKET holding a refcount on the native pool (acquired in
 *    s_try_acquire_locked, released at the end of s_java_ticket_destroy).
 *    The native pool state pins the Java pool object via a JNI global
 *    ref, so as long as any ticket is alive (including S3BorrowedBuffer
 *    tickets the customer holds past client shutdown), the pool state,
 *    the Java pool object, and its memory all remain valid.
 *
 * 2. A lease's memory address (cached in the ticket) is stable from
 *    acquire (tryAcquire) until its release runs.
 *    DirectByteBuffer memory is off-heap and NEVER moved by GC, and the
 *    Java pool never frees memory under a live lease, so the cached
 *    address remains valid until release.
 *
 * 3. The aws_byte_buf returned from s_java_ticket_claim has
 *    .allocator == NULL. This is essential. aws-c-s3 appends body
 *    chunks via aws_byte_buf_append (static, no realloc) on
 *    pool-managed buffers. If we set a non-NULL allocator, the
 *    append path could call aws_byte_buf_append_dynamic, which
 *    would aws_mem_acquire a fresh buffer and silently abandon
 *    the lease's memory, orphaning the lease and corrupting future
 *    leases when that memory is reused.
 *
 * 4. NON-BLOCKING RESERVE PATH. s_java_pool_reserve is called on the
 *    aws-c-s3 client's event-loop thread. It MUST NOT block. When
 *    the Java pool reports exhaustion, we push an unresolved future
 *    onto pending_reserves and return immediately. The future is
 *    resolved later by the drain: on a lease release, or when the next
 *    reserve or a finishing meta request prunes a cancelled entry
 *    (aws_s3_java_buffer_pool_drain). This mirrors the default
 *    pool's pending_reserves pattern.
 *
 * WARNING: Body callbacks invoked downstream of claim() see a
 *          cursor pointing into Java DirectByteBuffer memory. The
 *          ticket's release path is the ONLY safe point to mark
 *          that memory reusable. Triggering release while the SDK
 *          subscriber still has a reference produces silent data
 *          corruption. Consumers that retain the buffer past the
 *          callback must hold the ticket (S3BorrowedBuffer) so
 *          release fires only after consumption.
 *
 * DESIGN NOTE: can_block reservations
 * -----------------------------------
 * can_block=true is set only by aws-c-s3's async-write upload path
 * (aws_s3_meta_request_write); the s3_buffer_pool.h contract forbids
 * indefinite deferral. Our deferral resolves only when a held slot is
 * released independently of the waiter's progress: true for download
 * parts (the network completes them), false for async-write buffered
 * tickets (held until the customer writes more data). N uploads holding
 * all N slots while the app awaits a pending write future is a permanent
 * deadlock. The default pool escapes via over-limit forced buffers; this
 * pool keeps a hard cap, so exhausted+can_block fails loudly. Unreachable
 * today (aws-crt-java does not expose async write); it is a tripwire.
 * Revisit with a bounded-overflow design if that binding is added.
 */

#include "s3_java_buffer_pool.h"
#include "crt.h"            /* aws_jni_acquire_thread_env, aws_jni_check_and_clear_exception */
#include "java_class_ids.h" /* s3_direct_buffer_pool_properties */

#include <aws/common/mutex.h>
#include <aws/common/ref_count.h>
#include <aws/io/future.h>

/* ------------------------------------------------------------------ */
/* Internal state structs.                                            */
/* ------------------------------------------------------------------ */

struct java_pool_state {
    /* Allocator used for our own state allocations (NOT for pool memory;
     * that's owned by the Java pool object). */
    struct aws_allocator *allocator;

    /* JavaVM* captured at factory time. Used to attach threads (which may
     * not have a JNIEnv*) when calling back into the Java pool. */
    JavaVM *jvm;

    /* Global JNI reference to the S3DirectBufferPool Java object.
     * Owned by this state; released in s_java_pool_destroy. */
    jobject java_pool_global;

    /* Per-slot size; mirrors S3DirectBufferPool.partSize(). */
    size_t part_size;

    /*
     * Pending reserve futures, FIFO. Each entry holds an acquired ref on a
     * not-yet-resolved aws_future_s3_buffer_ticket and the original
     * reserve_meta. See preamble invariant #4 for when it is drained.
     *
     * GUARDED BY pending_lock. pending_lock is also held around every
     * reserve attempt, release, and trim (including their JNI calls), so
     * an attempt-then-pend can never interleave with a release-then-drain
     * (which would strand the pended future with capacity free). Lock
     * order: pending_lock, then the Java pool's lock. Futures are always
     * resolved after unlocking: setting a ticket on a future aws-c-s3 has
     * already cancelled destroys the ticket inside set_result_by_move, and that
     * runs s_java_ticket_destroy, which takes pending_lock.
     *
     * Strict FIFO for anything that consumes capacity: while a reservation
     * is pending, later ones queue behind it. This is what keeps in-order
     * downloads deadlock-free: each meta request reserves its parts in
     * order, so a part held for in-order delivery always has every earlier
     * part already served and is only waiting on network I/O, never on
     * the queue. The Java pool frees dedicated buffers on release, so idle
     * memory never holds capacity a waiting reservation needs.
     */
    struct aws_linked_list pending_reserves;
    struct aws_mutex pending_lock;

    /* The polymorphic header that aws-c-s3 dispatches against.
     * MUST be embedded so &state->pool gives a valid
     * aws_s3_buffer_pool* and the vtable lookup works.   */
    struct aws_s3_buffer_pool pool;
};

/* Pending-reserve list node. One per outstanding-but-unresolved reserve
 * future. Built by s_java_pool_reserve when the pool is exhausted (or
 * others are already waiting); consumed by the drain. */
struct java_pending_reserve {
    struct aws_linked_list_node node;
    struct aws_future_s3_buffer_ticket *future;
    struct aws_s3_buffer_pool_reserve_meta meta;
    /* Outcome filled in while draining under pending_lock, applied after
     * unlocking: a ticket on success, else an error code. */
    struct aws_s3_buffer_ticket *ticket;
    int error_code;
};

struct java_ticket_state {
    /* Issuing pool; the ticket holds a ref on it (preamble invariant #1). */
    struct java_pool_state *pool_state;

    /* Java lease handle: a slot run or a dedicated buffer. Returned via
     * S3DirectBufferPool.release in s_java_ticket_destroy. */
    jlong handle;

    /* Cached native address of the lease's memory. Stable from acquire to
     * release (preamble invariant #2). */
    void *lease_addr;

    /* Bytes claim() exposes: the reserved size (the lease may be larger). */
    size_t capacity;

    /* The polymorphic header. Same embedding rationale as above. */
    struct aws_s3_buffer_ticket ticket;
};

/* s_try_acquire_locked result: the pool is currently exhausted, so the
 * caller pends. Matches S3DirectBufferPool.EXHAUSTED. Distinct from
 * AWS_OP_SUCCESS and from the (positive) aws error codes it also returns. */
static const int s_exhausted = -1;

/* ------------------------------------------------------------------ */
/* Forward declarations.                                              */
/* ------------------------------------------------------------------ */

static struct aws_future_s3_buffer_ticket *s_java_pool_reserve(
    struct aws_s3_buffer_pool *pool,
    struct aws_s3_buffer_pool_reserve_meta meta);
static void s_java_pool_trim(struct aws_s3_buffer_pool *pool);
static uint64_t s_java_pool_derive_aligned_buffer_size(struct aws_s3_buffer_pool *pool, uint64_t size);
static void s_java_pool_destroy(void *user_data);

static struct aws_byte_buf s_java_ticket_claim(struct aws_s3_buffer_ticket *t);
static void s_java_ticket_destroy(void *user_data);

static int s_try_acquire_locked(
    struct java_pool_state *ps,
    JNIEnv *env,
    size_t size,
    struct aws_s3_buffer_ticket **out_ticket);

static struct aws_s3_buffer_pool_vtable s_java_pool_vtable = {
    .reserve = s_java_pool_reserve,
    .trim = s_java_pool_trim,
    .derive_aligned_buffer_size = s_java_pool_derive_aligned_buffer_size,
    /* acquire/release left NULL: default ref_count behavior. add_special_size /
     * release_special_size left NULL: contiguous slot runs serve sizes up to
     * a few parts, and larger ones get a dedicated buffer per reservation. */
};

static struct aws_s3_buffer_ticket_vtable s_java_ticket_vtable = {
    .claim = s_java_ticket_claim,
    /* acquire/release left NULL (as above). */
};

/* ------------------------------------------------------------------ */
/* Pending queue.                                                     */
/* ------------------------------------------------------------------ */

/*
 * Drops every entry whose future aws-c-s3 already completed (a cancelled or
 * paused meta request sets an error on its pending futures), wherever it
 * sits in the queue, so it never holds back live reservations. Caller holds
 * pending_lock; needs no JNIEnv. Returns true if any entry was dropped.
 */
static bool s_prune_done_locked(struct java_pool_state *ps, struct aws_linked_list *out_resolved) {
    bool pruned = false;
    struct aws_linked_list_node *node = aws_linked_list_begin(&ps->pending_reserves);
    while (node != aws_linked_list_end(&ps->pending_reserves)) {
        struct aws_linked_list_node *next = aws_linked_list_next(node);
        struct java_pending_reserve *pending = AWS_CONTAINER_OF(node, struct java_pending_reserve, node);
        if (aws_future_s3_buffer_ticket_is_done(pending->future)) {
            /* s_resolve_pending_list's set on a done future is a no-op. */
            pending->error_code = AWS_ERROR_S3_CANCELED;
            aws_linked_list_remove(node);
            aws_linked_list_push_back(out_resolved, node);
            pruned = true;
        }
        node = next;
    }
    return pruned;
}

/*
 * Prunes completed entries, then serves live ones in strict FIFO order from
 * the head, stopping at the first that still cannot be served (see
 * java_pool_state.pending_reserves for why). Caller holds pending_lock and
 * a JNIEnv; resolved entries move to out_resolved.
 *
 * Serving only helps if room appeared or the head changed. Growth up to the
 * ceiling already happens inside tryAcquire (including freeing idle blocks
 * that are in the way), so EXHAUSTED means the pool cannot grow either, and
 * room under the ceiling only appears on a release, which passes
 * always_serve; trim frees nothing a waiting reservation can use. Otherwise
 * serve only if the prune dropped an entry: the head was already retried by
 * the last release's drain, so retrying it again would be a JNI call that
 * cannot succeed.
 */
static void s_drain_pending_locked(
    struct java_pool_state *ps,
    JNIEnv *env,
    bool always_serve,
    struct aws_linked_list *out_resolved) {

    if (!s_prune_done_locked(ps, out_resolved) && !always_serve) {
        return;
    }

    while (!aws_linked_list_empty(&ps->pending_reserves)) {
        struct java_pending_reserve *head =
            AWS_CONTAINER_OF(aws_linked_list_front(&ps->pending_reserves), struct java_pending_reserve, node);
        struct aws_s3_buffer_ticket *ticket = NULL;
        int result = s_try_acquire_locked(ps, env, head->meta.size, &ticket);
        if (result == s_exhausted) {
            break; /* wait for the next release */
        }
        if (result == AWS_OP_SUCCESS) {
            head->ticket = ticket;
        } else {
            head->error_code = result;
        }
        aws_linked_list_pop_front(&ps->pending_reserves);
        aws_linked_list_push_back(out_resolved, &head->node);
    }
}

/* Applies drained outcomes. MUST be called without pending_lock held. */
static void s_resolve_pending_list(struct java_pool_state *ps, struct aws_linked_list *resolved) {
    while (!aws_linked_list_empty(resolved)) {
        struct java_pending_reserve *pending =
            AWS_CONTAINER_OF(aws_linked_list_pop_front(resolved), struct java_pending_reserve, node);
        if (pending->ticket != NULL) {
            aws_future_s3_buffer_ticket_set_result_by_move(pending->future, &pending->ticket);
        } else {
            aws_future_s3_buffer_ticket_set_error(pending->future, pending->error_code);
        }
        /* Release the future-acquire we did when we pended. */
        aws_future_s3_buffer_ticket_release(pending->future);
        aws_mem_release(ps->allocator, pending);
    }
}

void aws_s3_java_buffer_pool_drain(struct aws_s3_buffer_pool *pool) {
    AWS_FATAL_ASSERT(pool != NULL && pool->vtable == &s_java_pool_vtable);
    struct java_pool_state *ps = pool->impl;

    struct aws_linked_list resolved;
    aws_linked_list_init(&resolved);

    /* Fast path: serving can only help if this pass drops a cancelled entry
     * (see s_drain_pending_locked). Prune without a JNIEnv; in the common
     * case nothing is dropped and no JNI call is made. */
    aws_mutex_lock(&ps->pending_lock);
    bool pruned = s_prune_done_locked(ps, &resolved);
    aws_mutex_unlock(&ps->pending_lock);

    if (pruned) {
        /* Re-locking is safe: draining is idempotent, so a reserve or
         * release in between does no harm. The JNIEnv is acquired before
         * pending_lock, as everywhere else. always_serve: the first pass
         * dropped an entry, so the head may have changed. */
        /******** JNI ENV ACQUIRE ********/
        struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
        JNIEnv *env = jvm_env_context.env;
        if (env != NULL) { /* NULL: JVM shutting down; nothing to serve. */
            aws_mutex_lock(&ps->pending_lock);
            s_drain_pending_locked(ps, env, true /*always_serve*/, &resolved);
            aws_mutex_unlock(&ps->pending_lock);
            aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
        }
        /******** JNI ENV RELEASE ********/
    }

    /* Always resolve: pruned entries hold a future ref and their node. */
    s_resolve_pending_list(ps, &resolved);
}

/* ------------------------------------------------------------------ */
/* TICKET vtable.                                                     */
/* ------------------------------------------------------------------ */

/*
 * Invoked by aws-c-s3 when it first needs the buffer (lazy claim). The
 * returned buffer has .allocator == NULL (preamble invariant #3). claim()
 * may run more than once per ticket; each call returns a buffer over the
 * same memory, since lease_addr never moves.
 */
static struct aws_byte_buf s_java_ticket_claim(struct aws_s3_buffer_ticket *t) {
    struct java_ticket_state *ts = t->impl;
    return aws_byte_buf_from_empty_array(ts->lease_addr, ts->capacity);
}

/*
 * Ticket refcount hit zero: the safe point to dispose of its lease. Under
 * pending_lock the lease returns to the Java pool and pending reservations
 * are retried. See the preamble WARNING for why early release corrupts data.
 */
static void s_java_ticket_destroy(void *user_data) {
    struct java_ticket_state *ts = user_data;
    struct java_pool_state *ps = ts->pool_state;

    struct aws_linked_list resolved;
    aws_linked_list_init(&resolved);

    /******** JNI ENV ACQUIRE ********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        AWS_LOGF_WARN(AWS_LS_S3_CLIENT, "S3DirectBufferPool: could not release a buffer; JVM shutting down");
    } else {
        aws_mutex_lock(&ps->pending_lock);
        (*env)->CallVoidMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.release, ts->handle);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_WARN(
                AWS_LS_S3_CLIENT,
                "S3DirectBufferPool: release threw an exception (defensive not-leased check); "
                "the buffer may leak from Java-side tracking");
        }
        s_drain_pending_locked(ps, env, true /*always_serve*/, &resolved);
        aws_mutex_unlock(&ps->pending_lock);
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
    }
    /******** JNI ENV RELEASE ********/

    s_resolve_pending_list(ps, &resolved);

    aws_mem_release(ps->allocator, ts);

    /* LAST: drop the ticket's pool ref. May trigger s_java_pool_destroy
     * (frees ps), so it MUST follow every use of ps above. */
    aws_s3_buffer_pool_release(&ps->pool);
}

/* ------------------------------------------------------------------ */
/* POOL vtable.                                                       */
/* ------------------------------------------------------------------ */

/*
 * One non-blocking acquire attempt from the Java pool (a slot, a
 * contiguous slot run, or a dedicated buffer; see S3DirectBufferPool).
 * Caller holds pending_lock and a JNIEnv.
 *
 * Returns AWS_OP_SUCCESS with *out_ticket set; s_exhausted when the pool is
 * currently exhausted (caller pends); or an aws error code when the
 * request can never be served or a JNI call failed (caller fails it).
 */
static int s_try_acquire_locked(
    struct java_pool_state *ps,
    JNIEnv *env,
    size_t size,
    struct aws_s3_buffer_ticket **out_ticket) {

    *out_ticket = NULL;

    /* MUST NOT block; see S3DirectBufferPool#tryAcquire. */
    jlong handle =
        (*env)->CallLongMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.tryAcquire, (jlong)size);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: acquire of %zu bytes threw an exception (most likely OutOfMemoryError from "
            "allocateDirect, or a closed pool). Failing reservation",
            size);
        return AWS_ERROR_S3_BUFFER_ALLOCATION_FAILED;
    }
    if (handle == s_exhausted) {
        return s_exhausted;
    }
    if (handle < 0) {
        /* Never servable; the Java pool logged the reason. */
        return AWS_ERROR_S3_PART_SIZE_EXCEEDS_MEMORY_LIMIT;
    }

    jlong addr =
        (*env)->CallLongMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.leaseAddress, handle);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: leaseAddress threw (defensive check); returning the lease and failing the "
            "reservation");
        (*env)->CallVoidMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.release, handle);
        aws_jni_check_and_clear_exception(env);
        return AWS_ERROR_INVALID_STATE;
    }

    struct java_ticket_state *ts = aws_mem_calloc(ps->allocator, 1, sizeof(struct java_ticket_state));
    aws_s3_buffer_pool_acquire(&ps->pool); /* the ticket's pool ref (preamble invariant #1) */
    ts->pool_state = ps;
    ts->handle = handle;
    ts->lease_addr = (void *)(uintptr_t)addr;
    ts->capacity = size;
    ts->ticket.vtable = &s_java_ticket_vtable;
    ts->ticket.impl = ts;
    aws_ref_count_init(&ts->ticket.ref_count, ts, s_java_ticket_destroy);

    *out_ticket = &ts->ticket;
    return AWS_OP_SUCCESS;
}

/*
 * Reserve a buffer ticket. NON-BLOCKING (preamble invariant #4). Outcomes:
 *   (a) served now: resolve the future synchronously;
 *   (b) exhausted, or earlier reservations still pending (strict FIFO):
 *       pend the future;
 *   (c) exhausted and can_block: fail loudly (preamble DESIGN NOTE);
 *   (d) never servable (for example larger than the pool ceiling): fail
 *       with the reason logged by the Java pool.
 */
static struct aws_future_s3_buffer_ticket *s_java_pool_reserve(
    struct aws_s3_buffer_pool *pool,
    struct aws_s3_buffer_pool_reserve_meta meta) {

    struct java_pool_state *ps = pool->impl;
    struct aws_future_s3_buffer_ticket *future = aws_future_s3_buffer_ticket_new(ps->allocator);

    /******** JNI ENV ACQUIRE ********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_INVALID_STATE);
        return future;
    }

    struct aws_s3_buffer_ticket *ticket = NULL;
    bool pended = false;
    struct aws_linked_list resolved;
    aws_linked_list_init(&resolved);

    aws_mutex_lock(&ps->pending_lock);
    /* Drop cancelled entries (serving the rest only if one was dropped);
     * then strict FIFO: never jump reservations that are still waiting. */
    s_drain_pending_locked(ps, env, false /*always_serve*/, &resolved);
    int result =
        aws_linked_list_empty(&ps->pending_reserves) ? s_try_acquire_locked(ps, env, meta.size, &ticket) : s_exhausted;
    if (result == s_exhausted && !meta.can_block) {
        /* Pend; resolved by the drain. The event loop returns immediately. */
        struct java_pending_reserve *pending = aws_mem_calloc(ps->allocator, 1, sizeof(struct java_pending_reserve));
        pending->meta = meta;
        pending->future = future;
        /* Ref for the time the future sits on the pending list. Released
         * when resolved (or when the pool is destroyed with entries). */
        aws_future_s3_buffer_ticket_acquire(pending->future);
        aws_linked_list_push_back(&ps->pending_reserves, &pending->node);
        pended = true;
    }
    aws_mutex_unlock(&ps->pending_lock);
    aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
    /******** JNI ENV RELEASE ********/

    s_resolve_pending_list(ps, &resolved);

    if (pended) {
        return future;
    }
    if (result == AWS_OP_SUCCESS) {
        /* future holds a ref via set_result; aws-c-s3 owns the ticket once
         * it pops it. */
        aws_future_s3_buffer_ticket_set_result_by_move(future, &ticket);
        return future;
    }
    if (result == s_exhausted) {
        /* Exhausted + can_block: deferring risks deadlock; see "DESIGN
         * NOTE: can_block" in the preamble. Fail loudly. */
        AWS_LOGF_ERROR(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: blocking reservation (can_block=true, async-write path) requested while the "
            "pool is exhausted. S3DirectBufferPool does not grant over-limit forced buffers and cannot safely "
            "defer blocking reservations (deadlock risk). Failing the reservation. Use the default native "
            "buffer pool for async-write uploads, or size the pool for the expected concurrency.");
        aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_S3_BUFFER_ALLOCATION_FAILED);
        return future;
    }
    aws_future_s3_buffer_ticket_set_error(future, result);
    return future;
}

/*
 * Rounds a buffer size up to a whole number of slots, like the default
 * pool's chunk alignment. aws-c-s3 applies it to automatic download range
 * sizes and adjusted upload part sizes, so they fill slot runs exactly.
 */
static uint64_t s_java_pool_derive_aligned_buffer_size(struct aws_s3_buffer_pool *pool, uint64_t size) {
    struct java_pool_state *ps = pool->impl;
    uint64_t slots = size / ps->part_size;
    if (size % ps->part_size != 0) {
        ++slots;
    }
    return slots * ps->part_size;
}

/*
 * Idleness-gated trim (aws-c-s3 calls this only when
 * num_requests_in_flight == 0). Delegates to S3DirectBufferPool.trim(),
 * which frees direct memory synchronously. Best-effort: exceptions are
 * logged, never propagated.
 */
static void s_java_pool_trim(struct aws_s3_buffer_pool *pool) {
    struct java_pool_state *ps = pool->impl;

    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        /* JVM shutting down; nothing to do. */
        return;
    }

    aws_mutex_lock(&ps->pending_lock);
    (*env)->CallVoidMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.trim);
    bool trim_threw = aws_jni_check_and_clear_exception(env);
    aws_mutex_unlock(&ps->pending_lock);
    if (trim_threw) {
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: trim() threw an exception; unused blocks stay allocated until the next trim "
            "or close");
    }

    aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
}

/*
 * Invoked via aws_ref_count when the pool's refcount reaches zero:
 * after the owning aws_s3_client is destroyed AND every outstanding
 * ticket (including customer-held S3BorrowedBuffers) has released.
 * Releases our global JNI ref and frees state. The Java pool was created
 * by, and is closed by, the owning S3Client (at shutdown complete); this
 * only drops the native claim on it.
 *
 * Under normal teardown, pending_reserves is empty: a meta request cannot
 * finish while one of its reservations is pending (aws-c-s3 cancels them
 * first), and the pool outlives every meta request. Leftover entries are
 * failed defensively so their futures are not leaked. No lock is needed:
 * at refcount zero nothing else can reach the state.
 */
static void s_java_pool_destroy(void *user_data) {
    struct java_pool_state *ps = user_data;

    struct aws_linked_list resolved;
    aws_linked_list_init(&resolved);
    aws_linked_list_swap_contents(&resolved, &ps->pending_reserves);
    for (struct aws_linked_list_node *node = aws_linked_list_begin(&resolved); node != aws_linked_list_end(&resolved);
         node = aws_linked_list_next(node)) {
        AWS_CONTAINER_OF(node, struct java_pending_reserve, node)->error_code = AWS_ERROR_S3_CANCELED;
    }
    s_resolve_pending_list(ps, &resolved);
    aws_mutex_clean_up(&ps->pending_lock);

    /******** JNI ENV ACQUIRE ********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env != NULL) {
        (*env)->DeleteGlobalRef(env, ps->java_pool_global);
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
    }
    /* If env is NULL, JVM is shutting down. Global refs are released
     * automatically as part of VM teardown. */
    /******** JNI ENV RELEASE ********/

    aws_mem_release(ps->allocator, ps);
}

/* ------------------------------------------------------------------ */
/* FACTORY (registered into client config).                           */
/* ------------------------------------------------------------------ */

/*
 * Wired into aws_s3_client_config_options.buffer_pool_factory_fn by
 * s3ClientNew (s3_client.c) when S3Client created a pool. `user_data` is an
 * aws_s3_java_buffer_pool_factory_data.
 *
 * Once the argument checks pass, the factory owns the JNI global ref (it
 * clears the caller's field): the pool state holds it until
 * s_java_pool_destroy, and every later failure path deletes it before
 * returning NULL.
 */
struct aws_s3_buffer_pool *aws_s3_java_buffer_pool_factory(
    struct aws_allocator *allocator,
    struct aws_s3_buffer_pool_config config,
    void *user_data) {

    struct aws_s3_java_buffer_pool_factory_data *factory_data =
        (struct aws_s3_java_buffer_pool_factory_data *)user_data;
    if (factory_data == NULL || factory_data->java_pool_global == NULL || factory_data->jvm == NULL) {
        AWS_LOGF_ERROR(
            AWS_LS_S3_CLIENT, "S3DirectBufferPool factory invoked with NULL user_data or missing JVM/pool ref");
        aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
        return NULL;
    }

    jobject java_pool_global = factory_data->java_pool_global;
    JavaVM *jvm = factory_data->jvm;
    factory_data->java_pool_global = NULL; /* ownership taken */

    struct java_pool_state *ps = aws_mem_calloc(allocator, 1, sizeof(struct java_pool_state));
    ps->allocator = allocator;
    ps->java_pool_global = java_pool_global;
    ps->jvm = jvm;
    /* config.memory_limit is ignored: the Java pool enforces its own ceiling. */
    ps->part_size = config.part_size;

    aws_linked_list_init(&ps->pending_reserves);
    if (aws_mutex_init(&ps->pending_lock) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_S3_CLIENT, "S3DirectBufferPool factory: failed to init pending_lock");
        aws_mem_release(allocator, ps);
        goto error_release_global_ref;
    }

    /* Validate slot size against this client (fail-fast at client
     * creation). Tickets report capacity = config.part_size (the client's
     * resolved part size) over slots the Java pool allocated at its own
     * partSize(). If the client's is larger, aws-c-s3 would write past the
     * end of the slot allocation (native heap corruption); if smaller,
     * slots are silently underused. S3Client builds the pool from the part
     * size it resolves, so a mismatch means the Java and native part-size
     * resolution have diverged; require exact equality. */
    {
        struct aws_jvm_env_context validate_env = aws_jni_acquire_thread_env(ps->jvm);
        JNIEnv *env = validate_env.env;
        if (env == NULL) {
            goto error_clean_ps;
        }
        jint java_slot_size =
            (*env)->CallIntMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.partSize);
        bool threw = aws_jni_check_and_clear_exception(env);
        aws_jni_release_thread_env(ps->jvm, &validate_env);
        if (threw) {
            goto error_clean_ps;
        }
        if ((size_t)java_slot_size != config.part_size) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_CLIENT,
                "S3DirectBufferPool slot size (%d bytes) does not match the client's effective part size "
                "(%zu bytes). Java and native part-size resolution have diverged. Failing client creation.",
                (int)java_slot_size,
                config.part_size);
            goto error_clean_ps;
        }
    }

    ps->pool.vtable = &s_java_pool_vtable;
    ps->pool.impl = ps;
    aws_ref_count_init(&ps->pool.ref_count, ps, s_java_pool_destroy);
    factory_data->out_pool = &ps->pool;

    AWS_LOGF_INFO(
        AWS_LS_S3_CLIENT, "S3DirectBufferPool factory: pool=%p part_size=%zu", (void *)&ps->pool, ps->part_size);

    return &ps->pool;

error_clean_ps:
    aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    aws_mutex_clean_up(&ps->pending_lock);
    aws_mem_release(allocator, ps);

error_release_global_ref:
    /* ps is freed or was never initialized; we still own the global ref. */
    {
        struct aws_jvm_env_context cleanup_env = aws_jni_acquire_thread_env(jvm);
        if (cleanup_env.env != NULL) {
            (*cleanup_env.env)->DeleteGlobalRef(cleanup_env.env, java_pool_global);
            aws_jni_release_thread_env(jvm, &cleanup_env);
        }
        /* If env is NULL, the JVM is shutting down and reclaims the ref. */
    }
    return NULL;
}

/* ------------------------------------------------------------------ */
/* JNI helper: nativeGetDirectBufferAddress                           */
/* ------------------------------------------------------------------ */

/* Called by S3DirectBufferPool when it allocates a block or a dedicated
 * buffer, to cache the buffer's native address (stable: invariant #2). */
JNIEXPORT jlong JNICALL Java_software_amazon_awssdk_crt_s3_S3DirectBufferPool_nativeGetDirectBufferAddress(
    JNIEnv *env,
    jclass cls,
    jobject dbb) {
    (void)cls;
    if (dbb == NULL) {
        return 0;
    }
    return (jlong)(intptr_t)(*env)->GetDirectBufferAddress(env, dbb);
}

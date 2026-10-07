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
 *    the Java pool reports exhaustion (an acquire returns -1), we
 *    push an unresolved future onto pending_reserves and return
 *    immediately. The future is resolved later by the drain (on a lease
 *    release, the next reserve, or a meta request finishing), mirroring the
 *    default pool's pending_reserves pattern. Reserve, release, trim, and
 *    drain all run under pending_lock (see java_pool_state.pending_reserves).
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
#include "crt.h"            /* aws_jni_get_thread_env, AWS_LOGF_*, etc. */
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

    /* Method IDs for S3DirectBufferPool's JNI back-call surface are in
     * s3_direct_buffer_pool_properties (java_class_ids.c). */

    /* Per-slot size; mirrors S3DirectBufferPool.partSize(). */
    size_t part_size;

    /*
     * Pending reserve futures, FIFO. Each entry holds an acquired ref on a
     * not-yet-resolved aws_future_s3_buffer_ticket and the original
     * reserve_meta. Drained when a lease is released and at the start of
     * every reserve; pruned when any meta request finishes, and drained
     * then only if a cancelled entry was dropped
     * (aws_s3_java_buffer_pool_drain). Entries whose future aws-c-s3
     * already completed (a cancelled or paused meta request sets an error
     * on its pending futures) are dropped by the drain wherever they sit, so they never
     * hold back live reservations.
     *
     * GUARDED BY pending_lock. pending_lock is also held around every
     * reserve attempt, release, trim, and request finish (including their
     * JNI calls), so an attempt-then-pend can never interleave with a
     * release-then-drain (which would strand the pended future with
     * capacity free). Lock order: pending_lock, then the Java pool's lock.
     * Futures are always resolved after unlocking: their callbacks run
     * synchronously.
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
    /* Issuing pool. Each ticket holds a refcount on it (acquired in
     * s_try_acquire_locked, released at the end of s_java_ticket_destroy);
     * see LIFETIME INVARIANTS #1 in the file preamble. */
    struct java_pool_state *pool_state;

    /* Java lease handle: a slot run or a dedicated buffer. Returned via
     * S3DirectBufferPool.release in s_java_ticket_destroy. */
    jlong handle;

    /* Cached native address of the lease's memory. Stable from acquire to
     * release (see invariant #2 above). */
    void *lease_addr;

    /* Bytes claim() exposes: the reserved size (the lease may be larger). */
    size_t capacity;

    /* The polymorphic header. Same embedding rationale as above. */
    struct aws_s3_buffer_ticket ticket;
};

/* ------------------------------------------------------------------ */
/* Forward declarations of vtable functions.                          */
/* ------------------------------------------------------------------ */

static struct aws_future_s3_buffer_ticket *s_java_pool_reserve(
    struct aws_s3_buffer_pool *pool,
    struct aws_s3_buffer_pool_reserve_meta meta);
static void s_java_pool_trim(struct aws_s3_buffer_pool *pool);
static uint64_t s_java_pool_derive_aligned_buffer_size(struct aws_s3_buffer_pool *pool, uint64_t size);
static void s_java_pool_destroy(void *user_data);

static struct aws_byte_buf s_java_ticket_claim(struct aws_s3_buffer_ticket *t);
static void s_java_ticket_destroy(void *user_data);

/* Helper forward declarations. */
static int s_try_acquire_locked(
    struct java_pool_state *ps,
    JNIEnv *env,
    size_t size,
    struct aws_s3_buffer_ticket **out_ticket);
static bool s_prune_done_locked(struct java_pool_state *ps, struct aws_linked_list *out_resolved);
static void s_drain_pending_locked(struct java_pool_state *ps, JNIEnv *env, struct aws_linked_list *out_resolved);
static void s_resolve_pending_list(struct java_pool_state *ps, struct aws_linked_list *resolved);

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
/* TICKET vtable.                                                     */
/* ------------------------------------------------------------------ */

/*
 * Invoked by aws-c-s3 when it first needs the buffer (lazy claim).
 *
 * Returns an aws_byte_buf pointing at the lease's native memory with
 * .allocator == NULL, so no realloc will be attempted (invariant #3 in the
 * file preamble).
 *
 * IMPORTANT: claim() may run more than once per ticket (the default pool
 * returns the same buffer on repeated claims); each call returns a buffer
 * over the same memory, since lease_addr never moves.
 */
static struct aws_byte_buf s_java_ticket_claim(struct aws_s3_buffer_ticket *t) {
    struct java_ticket_state *ts = t->impl;
    return aws_byte_buf_from_empty_array(ts->lease_addr, ts->capacity);
}

/*
 * Ticket refcount hit zero: the safe point to dispose of its lease. Under
 * pending_lock the lease returns to the Java pool and pending reservations
 * are retried (see s_drain_pending_locked). Futures are resolved after
 * unlocking. See the preamble WARNING for why early release corrupts data.
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
        s_drain_pending_locked(ps, env, &resolved);
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
 * Retries pending reservations. Called on every ticket release (capacity
 * may have freed) and at the start of every reserve; a meta request finish
 * calls it only if the prune dropped a cancelled entry (see
 * aws_s3_java_buffer_pool_drain). Caller holds pending_lock and a JNIEnv;
 * served, failed, or already-completed entries move to out_resolved.
 *
 * 1. Prunes completed entries (s_prune_done_locked).
 * 2. Serves live entries in strict FIFO order from the head, stopping at the
 *    first that still cannot be served (see java_pool_state.pending_reserves
 *    for why).
 */
static void s_drain_pending_locked(struct java_pool_state *ps, JNIEnv *env, struct aws_linked_list *out_resolved) {
    s_prune_done_locked(ps, out_resolved);

    while (!aws_linked_list_empty(&ps->pending_reserves)) {
        struct java_pending_reserve *head =
            AWS_CONTAINER_OF(aws_linked_list_front(&ps->pending_reserves), struct java_pending_reserve, node);
        struct aws_s3_buffer_ticket *ticket = NULL;
        int result = s_try_acquire_locked(ps, env, head->meta.size, &ticket);
        if (result == -1) {
            break; /* still exhausted: wait for the next release */
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

void aws_s3_java_buffer_pool_drain(struct aws_s3_buffer_pool *pool) {
    AWS_FATAL_ASSERT(pool != NULL && pool->vtable == &s_java_pool_vtable);
    struct java_pool_state *ps = pool->impl;

    struct aws_linked_list resolved;
    aws_linked_list_init(&resolved);

    /* Fast path. A finish frees no capacity (ticket releases already drain),
     * so serving can only help if this pass drops a cancelled entry. Prune
     * without a JNIEnv; in the common case nothing is dropped and no JNI
     * call is made. */
    aws_mutex_lock(&ps->pending_lock);
    bool pruned = s_prune_done_locked(ps, &resolved);
    aws_mutex_unlock(&ps->pending_lock);

    if (pruned) {
        /* Re-locking is safe: draining is idempotent, so a reserve or
         * release in between does no harm. The JNIEnv is acquired before
         * pending_lock, as everywhere else. */
        /******** JNI ENV ACQUIRE ********/
        struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
        JNIEnv *env = jvm_env_context.env;
        if (env != NULL) { /* NULL: JVM shutting down; nothing to serve. */
            aws_mutex_lock(&ps->pending_lock);
            s_drain_pending_locked(ps, env, &resolved);
            aws_mutex_unlock(&ps->pending_lock);
            aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
        }
        /******** JNI ENV RELEASE ********/
    }

    /* Always resolve: pruned entries hold a future ref and their node. */
    s_resolve_pending_list(ps, &resolved);
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

/* ------------------------------------------------------------------ */
/* POOL vtable.                                                       */
/* ------------------------------------------------------------------ */

/*
 * One non-blocking acquire attempt from the Java pool (a slot, a
 * contiguous slot run, or a dedicated buffer; see S3DirectBufferPool).
 * Caller holds pending_lock and a JNIEnv.
 *
 * Returns AWS_OP_SUCCESS with *out_ticket set; -1 when the pool is
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
        /* Most likely OutOfMemoryError from allocateDirect, or
         * IllegalStateException from a closed pool. */
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: acquire of %zu bytes threw an exception (most likely OutOfMemoryError from "
            "allocateDirect, or a closed pool). Failing reservation",
            size);
        return AWS_ERROR_S3_BUFFER_ALLOCATION_FAILED;
    }
    if (handle == -1) {
        return -1;
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

    /* Never returns NULL; aws_mem_calloc aborts on OOM. */
    struct java_ticket_state *ts = aws_mem_calloc(ps->allocator, 1, sizeof(struct java_ticket_state));
    /* Pool ref (see LIFETIME INVARIANTS #1 in the preamble). */
    aws_s3_buffer_pool_acquire(&ps->pool);
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
 * Reserve a buffer ticket. NON-BLOCKING (preamble invariant #4).
 * Outcomes: (a) served now: resolve the future synchronously; (b)
 * exhausted, or earlier reservations still pending (strict FIFO): pend the
 * future;
 * (c) exhausted+can_block: fail loudly (preamble DESIGN NOTE); (d) never
 * servable (for example larger than the pool ceiling): fail with the
 * reason logged by the Java pool.
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
    /* Drop cancelled entries and serve any waiting ones that now fit, so a
     * reservation that aws-c-s3 already cancelled never holds back this one.
     * Then strict FIFO: never jump reservations that are still waiting. */
    s_drain_pending_locked(ps, env, &resolved);
    int result = aws_linked_list_empty(&ps->pending_reserves) ? s_try_acquire_locked(ps, env, meta.size, &ticket) : -1;
    if (result == -1 && !meta.can_block) {
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
    if (result == -1) {
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
 * which frees direct memory synchronously (see that method's Javadoc).
 * Best-effort: exceptions are caught and logged, never propagated.
 */
static void s_java_pool_trim(struct aws_s3_buffer_pool *pool) {
    struct java_pool_state *ps = pool->impl;

    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        /* JVM shutting down; nothing to do. Memory is reclaimed as part of
         * VM teardown. */
        return;
    }

    aws_mutex_lock(&ps->pending_lock);
    (*env)->CallVoidMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.trim);
    bool trim_threw = aws_jni_check_and_clear_exception(env);
    aws_mutex_unlock(&ps->pending_lock);
    if (trim_threw) {
        /* trim() is defensive against a closed pool, so an exception here
         * is unexpected. Log for diagnosis but do not propagate; trim is
         * fire-and-forget. */
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: trim() threw an exception; some direct memory "
            "may remain pinned until the next GC pass");
    }

    aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
}

/*
 * Invoked via aws_ref_count when the pool's refcount reaches zero:
 * after the owning aws_s3_client is destroyed AND every outstanding
 * ticket (including customer-held S3BorrowedBuffers) has released.
 * Fails any remaining pending-reserve futures, releases our global
 * JNI ref, and frees state. The Java pool was created by, and is
 * closed by, the owning S3Client (at shutdown complete); this only
 * drops the native claim on it.
 *
 * Under normal teardown, pending_reserves is empty: a meta request cannot
 * finish while one of its reservations is pending (aws-c-s3 cancels them
 * first), and the pool outlives every meta request. We handle non-empty
 * defensively to avoid leaking unresolved futures.
 */
static void s_java_pool_destroy(void *user_data) {
    struct java_pool_state *ps = user_data;

    /* Fail any leftover pending reserves before tearing down the mutex.
     * Collect under the lock, resolve after unlocking (future callbacks
     * run synchronously). Each entry holds a future ref we acquired in
     * s_java_pool_reserve; s_resolve_pending_list drops it. */
    struct aws_linked_list resolved;
    aws_linked_list_init(&resolved);
    aws_mutex_lock(&ps->pending_lock);
    while (!aws_linked_list_empty(&ps->pending_reserves)) {
        struct aws_linked_list_node *node = aws_linked_list_pop_front(&ps->pending_reserves);
        struct java_pending_reserve *pending = AWS_CONTAINER_OF(node, struct java_pending_reserve, node);
        pending->ticket = NULL;
        pending->error_code = AWS_ERROR_S3_CANCELED;
        aws_linked_list_push_back(&resolved, node);
    }
    aws_mutex_unlock(&ps->pending_lock);
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
 * Wired into aws_s3_client_config_options.buffer_pool_factory_fn
 * by s3ClientNew (s3_client.c) when S3Client created a pool.
 *
 * `user_data` points to an aws_s3_java_buffer_pool_factory_data (JVM
 * pointer + JNI global ref to the S3DirectBufferPool Java object). The
 * factory takes ownership of the global ref; from here the pool state
 * holds it until the pool is destroyed.
 *
 * Failure paths after ref-ownership transfer MUST release the global
 * ref before returning NULL, otherwise the Java pool object stays
 * pinned for the JVM's lifetime. We acquire the JavaVM* up front so
 * every error path has a JNIEnv* available for DeleteGlobalRef.
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

    /* STEP 1: Take ownership of the JNI global ref: clear the caller's
     * pointer so their cleanup path (in s3_client.c after
     * aws_s3_client_new) knows the ref has been consumed. From here
     * on, this factory is responsible for releasing the ref: on
     * success via s_java_pool_destroy, on failure via
     * error_release_global_ref below. */
    factory_data->java_pool_global = NULL;

    /* STEP 2: Allocate state. aws_mem_calloc aborts on OOM (see
     * AWS_PANIC_OOM in aws-c-common), so no NULL check needed here.
     * From here on, any error must goto error_release_global_ref
     * to clean up the JNI global ref. */
    struct java_pool_state *ps = aws_mem_calloc(allocator, 1, sizeof(struct java_pool_state));
    ps->allocator = allocator;
    ps->java_pool_global = java_pool_global;
    ps->jvm = jvm;
    ps->part_size = config.part_size;

    /* STEP 3: Initialize the pending-reserves list and its guarding
     * mutex. Both are required for the async backpressure path in
     * s_java_pool_reserve and s_java_ticket_destroy. */
    aws_linked_list_init(&ps->pending_reserves);
    if (aws_mutex_init(&ps->pending_lock) != AWS_OP_SUCCESS) {
        AWS_LOGF_ERROR(AWS_LS_S3_CLIENT, "S3DirectBufferPool factory: failed to init pending_lock");
        aws_mem_release(allocator, ps);
        goto error_release_global_ref;
    }

    /* STEP 4: Validate slot size against this client (fail-fast at
     * client creation). Tickets report capacity = config.part_size (the
     * client's resolved part size) over slots the Java pool allocated at
     * its own partSize(). If the client's is larger, aws-c-s3 would write
     * past the end of the slot allocation (native heap corruption); if
     * smaller, slots are silently underused. S3Client builds the pool
     * from the part size it resolves, so a mismatch means the Java and
     * native part-size resolution have diverged; require exact equality.
     *
     * No single-client guard is needed: S3Client creates the pool for
     * its own use and it is not reachable by customers. */
    {
        struct aws_jvm_env_context validate_env = aws_jni_acquire_thread_env(ps->jvm);
        JNIEnv *env = validate_env.env;
        if (env == NULL) {
            goto error_clean_ps;
        }

        jint java_slot_size =
            (*env)->CallIntMethod(env, ps->java_pool_global, s3_direct_buffer_pool_properties.partSize);
        if (aws_jni_check_and_clear_exception(env)) {
            aws_jni_release_thread_env(ps->jvm, &validate_env);
            goto error_clean_ps;
        }
        if ((size_t)java_slot_size != config.part_size) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_CLIENT,
                "S3DirectBufferPool slot size (%d bytes) does not match the client's effective part size "
                "(%zu bytes). Java and native part-size resolution have diverged. Failing client creation.",
                (int)java_slot_size,
                config.part_size);
            aws_jni_release_thread_env(ps->jvm, &validate_env);
            goto error_clean_ps;
        }
        aws_jni_release_thread_env(ps->jvm, &validate_env);
    }

    /* STEP 5: Wire vtable and ref_count. Pool is now valid; the
     * global ref is owned by ps and will be released in
     * s_java_pool_destroy. */
    ps->pool.vtable = &s_java_pool_vtable;
    ps->pool.impl = ps;
    aws_ref_count_init(&ps->pool.ref_count, ps, s_java_pool_destroy);
    factory_data->out_pool = &ps->pool;

    AWS_LOGF_INFO(
        AWS_LS_S3_CLIENT, "S3DirectBufferPool factory: pool=%p part_size=%zu", (void *)&ps->pool, ps->part_size);

    return &ps->pool;

error_clean_ps:
    /* Failure after ps + mutex were initialized (validation step). */
    aws_raise_error(AWS_ERROR_INVALID_ARGUMENT);
    aws_mutex_clean_up(&ps->pending_lock);
    aws_mem_release(allocator, ps);

error_release_global_ref:
    /* Failure path: ps either was never allocated or has been freed.
     * The global ref is still owned by us; delete it before
     * returning NULL. */
    {
        struct aws_jvm_env_context cleanup_env = aws_jni_acquire_thread_env(jvm);
        if (cleanup_env.env != NULL) {
            (*cleanup_env.env)->DeleteGlobalRef(cleanup_env.env, java_pool_global);
            aws_jni_release_thread_env(jvm, &cleanup_env);
        }
        /* If env is NULL here, JVM is shutting down. The global ref
         * will be reclaimed when the JVM exits. */
    }
    return NULL;
}

/* ------------------------------------------------------------------ */
/* JNI helper: nativeGetDirectBufferAddress                           */
/* ------------------------------------------------------------------ */

/*
 * Called from S3DirectBufferPool's Java constructor / growth path to
 * cache each slot's raw native address. Direct buffers are off-heap
 * and not relocated by GC, so the address is stable for the buffer's
 * lifetime.
 */
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

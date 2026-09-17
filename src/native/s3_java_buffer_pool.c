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
 * The pool is backed by a fixed array of DirectByteBuffer slots
 * owned by the S3DirectBufferPool Java object. Each ticket leases
 * one slot. When the ticket is released, the slot returns to the
 * Java pool's free queue.
 *
 * LIFETIME INVARIANTS
 * -------------------
 * 1. The Java pool object outlives every ticket. This is enforced by
 *    EACH TICKET holding a refcount on the native pool (acquired in
 *    s_build_java_ticket, released at the end of s_java_ticket_destroy).
 *    The native pool state pins the Java pool object via a JNI global
 *    ref, so as long as any ticket is alive — including Phase 2
 *    S3BorrowedBuffer tickets the customer holds past client shutdown
 *    — the pool state, the Java pool object, and its slot memory all
 *    remain valid. (Pre-Phase-2 this was enforced by tickets being
 *    scoped to in-flight meta requests on a client that held the pool
 *    ref; customer-held borrowed buffers broke that assumption.)
 *
 * 2. A slot's underlying memory address (cached in the ticket) is
 *    stable from tryAcquireSlot() until releaseSlot() runs (or until
 *    the slot is handed to a pending future from s_java_ticket_destroy).
 *    DirectByteBuffer memory is off-heap and NEVER moved by GC, so
 *    the cached address remains valid as long as the Java pool holds
 *    its strong reference to the slot.
 *
 * 3. The aws_byte_buf returned from s_java_ticket_claim has
 *    .allocator == NULL. This is essential — aws-c-s3 appends body
 *    chunks via aws_byte_buf_append (static, no realloc) on
 *    pool-managed buffers. If we set a non-NULL allocator, the
 *    append path could call aws_byte_buf_append_dynamic, which
 *    would aws_mem_acquire a fresh buffer and silently abandon
 *    the slot — orphaning the slot lease and corrupting future
 *    leases when the slot index is reused.
 *
 * 4. NON-BLOCKING RESERVE PATH. s_java_pool_reserve is called on the
 *    aws-c-s3 client's event-loop thread. It MUST NOT block. When
 *    the Java pool reports exhaustion (tryAcquireSlot returns -1),
 *    we push an unresolved future onto pending_reserves and return
 *    immediately. The future is resolved later from s_java_ticket_destroy
 *    when a slot is released — mirroring the default pool's
 *    pending_reserves pattern.
 *
 * WARNING: Body callbacks invoked downstream of claim() see a
 *          cursor pointing into Java DirectByteBuffer memory. The
 *          ticket's release path is the ONLY safe point to mark
 *          that memory reusable. Triggering release while the SDK
 *          subscriber still has a reference produces silent data
 *          corruption. See "Lifetime Implementation Mechanic"
 *          below for the SDK-side wiring that ensures release
 *          fires only after subscriber consumption.
 */

#include "s3_java_buffer_pool.h"
#include "crt.h"            /* aws_jni_get_thread_env, AWS_LOGF_*, etc. */
#include "java_class_ids.h" /* s3_direct_buffer_pool_properties (Layer 4) */

#include <aws/common/mutex.h>
#include <aws/common/ref_count.h>
#include <aws/io/future.h>

/* ------------------------------------------------------------------ */
/* Internal state structs.                                            */
/* ------------------------------------------------------------------ */

struct java_pool_state {
    /* Allocator used for our own state allocations (NOT for slot
     * memory; that's owned by the Java DBZ pool). */
    struct aws_allocator *allocator;

    /* JavaVM* captured at factory time. Used to attach the receive
     * thread (which may not have a JNIEnv*) when invoking the back
     * callbacks below. */
    JavaVM *jvm;

    /* Global JNI reference to the S3DirectBufferPool Java object.
     * Owned by this state — released in s_java_pool_destroy. */
    jobject java_pool_global;

    /* Cached method IDs. Resolved once at factory time so the hot
     * path can CallXxxMethod without GetMethodID round-trips. */
    jmethodID mid_try_acquire_slot; /* int  tryAcquireSlot()      (non-blocking; returns -1 on exhaustion) */
    jmethodID mid_release_slot;     /* void releaseSlot(int)       */
    jmethodID mid_slot_address;     /* long slotAddress(int)       */
    jmethodID mid_trim;             /* void trim()                 (idle-time slot cleanup) */

    /* NOTE: there is no cached `mid_slice_view` — the body callback
     * constructs the ByteBuffer view in C via NewDirectByteBuffer,
     * avoiding a JNI->Java round-trip per delivery. */

    /* Per-slot size; mirrors S3DirectBufferPool.partSize(). */
    size_t part_size;

    /*
     * Pending reserve futures, FIFO. Each entry holds an acquired
     * ref on a not-yet-resolved aws_future_s3_buffer_ticket and the
     * original reserve_meta. Drained from s_java_ticket_destroy
     * when a slot becomes available.
     *
     * GUARDED BY pending_lock.
     *
     * Mirrors the aws-c-s3 default pool's pending_reserves pattern
     * — see s_default_pool_reserve in
     * aws-c-s3/source/s3_default_buffer_pool.c.
     */
    struct aws_linked_list pending_reserves;
    struct aws_mutex pending_lock;

    /* The polymorphic header that aws-c-s3 dispatches against.
     * MUST be embedded so &state->pool gives a valid
     * aws_s3_buffer_pool* and the vtable lookup works.   */
    struct aws_s3_buffer_pool pool;
};

/* Pending-reserve list node. One per outstanding-but-unresolved
 * reserve future. Built by s_java_pool_reserve when the Java pool
 * is exhausted; consumed by s_java_ticket_destroy when a slot
 * frees. */
struct java_pending_reserve {
    struct aws_linked_list_node node;
    struct aws_future_s3_buffer_ticket *future;
    struct aws_s3_buffer_pool_reserve_meta meta;
};

struct java_ticket_state {
    /* The pool that issued this ticket. Each ticket HOLDS A REFCOUNT
     * on the pool (acquired in s_build_java_ticket, released as the
     * final step of s_java_ticket_destroy).
     *
     * WHY: with the Phase 2 S3BorrowedBuffer API, the JAVA CUSTOMER
     * can hold a ticket reference past meta-request completion and
     * past client shutdown (close() at any time, or the phantom-ref
     * GC fallback at a time we don't control). The old assumption
     * — "the owning client outlives all tickets it issued" — no
     * longer holds. Without this ref, the client's destroy path
     * would free java_pool_state and delete the JNI global ref
     * while a live ticket still points at both; the customer's
     * eventual close() would then lock a freed mutex (UAF crash)
     * and the Java pool object (and its DirectByteBuffer slots)
     * could be GC'd out from under the customer's ByteBuffer view.
     *
     * With the ref, the pool state, the JNI global ref, the Java
     * S3DirectBufferPool object, and therefore the slot memory all
     * stay alive until the LAST outstanding ticket is released —
     * borrowed buffers may safely outlive the client. */
    struct java_pool_state *pool_state;

    /* The slot index this ticket has checked out of the Java pool.
     * Returned to the pool's free queue in s_java_ticket_destroy. */
    jint slot_index;

    /* Cached native address of the slot's DirectByteBuffer memory.
     * Stable from acquire to release (see invariant #2 above). */
    void *slot_addr;

    /* Capacity = part_size; cached for convenience. */
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
static void s_java_pool_destroy(void *user_data);

static struct aws_byte_buf s_java_ticket_claim(struct aws_s3_buffer_ticket *t);
static void s_java_ticket_destroy(void *user_data);

/* Helper forward declarations. */
static struct aws_s3_buffer_ticket *s_build_java_ticket(struct java_pool_state *ps, jint slot_index, void *slot_addr);
static void s_release_slot_via_jni(struct java_pool_state *ps, jint slot_index);

static struct aws_s3_buffer_pool_vtable s_java_pool_vtable = {
    .reserve = s_java_pool_reserve,
    .trim = s_java_pool_trim,
    /* acquire/release left NULL — we use the default ref_count behavior. */
};

static struct aws_s3_buffer_ticket_vtable s_java_ticket_vtable = {
    .claim = s_java_ticket_claim,
    /* acquire/release left NULL — default ref_count behavior. */
};

/* ------------------------------------------------------------------ */
/* TICKET vtable.                                                     */
/* ------------------------------------------------------------------ */

/*
 * Invoked by aws-c-s3 on the first body chunk of a part (lazy claim,
 * see s_s3_meta_request_incoming_body in
 * aws-c-s3/source/s3_meta_request.c).
 *
 * Returns an aws_byte_buf pointing at the slot's native memory. The
 * receive path will then append HTTP body bytes into this buffer via
 * aws_byte_buf_append. Because .allocator == NULL, no realloc will
 * be attempted — see invariant #3 in the file preamble.
 *
 * IMPORTANT: claim() may be called more than once per ticket if the
 * default-pool semantics are followed (the existing default pool
 * returns the same buffer on repeated claims). We mirror that by
 * always returning a buffer pointing at the same slot — slot_addr
 * does not move between claims.
 */
static struct aws_byte_buf s_java_ticket_claim(struct aws_s3_buffer_ticket *t) {
    struct java_ticket_state *ts = t->impl;
    return aws_byte_buf_from_empty_array(ts->slot_addr, ts->capacity);
    /* .buffer = ts->slot_addr
     * .capacity = part_size
     * .len = 0       (filled in by aws_byte_buf_append as chunks arrive)
     * .allocator = NULL  ← critical, see invariant #3 */
}

/*
 * Called via aws_ref_count when the ticket's refcount reaches zero.
 * This is the SAFE POINT to dispose of the slot — by the time
 * aws-c-s3 has dropped its last reference to the ticket, the body
 * callback has fired AND the SDK consumer signalled readiness for
 * the next buffer (see "Lifetime Implementation Mechanic" below
 * for the wiring).
 *
 * Two outcomes for the slot:
 *
 *   (a) pending_reserves is non-empty: hand this slot DIRECTLY to
 *       the next pending future without routing it back through the
 *       Java free queue. Avoids a needless take/poll round-trip
 *       under pressure, and removes a race window where another
 *       thread could grab the slot before the pending future does.
 *
 *   (b) pending_reserves is empty: release the slot back to the
 *       Java pool's free queue.
 *
 * Either path concludes with this ticket-state struct being freed.
 *
 * WARNING: if this is called while the SDK still holds a reference
 * to a slice of the slot's memory, the next request that gets this
 * slot will hand the SDK corrupted data. The L3 backpressure wiring
 * in S3CrtResponseHandlerAdapter prevents this — see below.
 */
static void s_java_ticket_destroy(void *user_data) {
    struct java_ticket_state *ts = user_data;
    struct java_pool_state *ps = ts->pool_state;

    aws_mutex_lock(&ps->pending_lock);

    if (!aws_linked_list_empty(&ps->pending_reserves)) {
        /* Pending future waiting for a slot — hand this slot to it
         * directly, skipping the Java free queue. */
        struct aws_linked_list_node *node = aws_linked_list_pop_front(&ps->pending_reserves);
        aws_mutex_unlock(&ps->pending_lock);

        struct java_pending_reserve *pending = AWS_CONTAINER_OF(node, struct java_pending_reserve, node);

        /* Build a new ticket bound to the same slot. Never returns
         * NULL — aws_mem_calloc aborts on OOM. */
        struct aws_s3_buffer_ticket *new_ticket = s_build_java_ticket(ps, ts->slot_index, ts->slot_addr);

        aws_future_s3_buffer_ticket_set_result_by_move(pending->future, &new_ticket);

        /* Release the future-acquire we did when we pended. */
        aws_future_s3_buffer_ticket_release(pending->future);
        aws_mem_release(ps->allocator, pending);
    } else {
        aws_mutex_unlock(&ps->pending_lock);

        /* No pending requests — return slot to Java's free queue. */
        s_release_slot_via_jni(ps, ts->slot_index);
    }

    aws_mem_release(ps->allocator, ts);

    /* LAST step: drop this ticket's refcount on the pool. If this is
     * the final outstanding ticket AND the owning client has already
     * been destroyed (released its own pool ref), this triggers
     * s_java_pool_destroy — which frees ps and deletes the JNI global
     * ref. That is why this MUST come after every use of ps above
     * (pending_lock, allocator, JNI release). See the java_ticket_state
     * comment for why tickets ref the pool at all. */
    aws_s3_buffer_pool_release(&ps->pool);
}

/* ------------------------------------------------------------------ */
/* POOL vtable.                                                       */
/* ------------------------------------------------------------------ */

/*
 * Invoked by aws-c-s3 when a request needs to reserve a buffer (see
 * aws_s3_buffer_pool_reserve in aws-c-s3/source/s3_buffer_pool.c).
 *
 * Returns a future that resolves with a ticket. The reserve path is
 * NON-BLOCKING — it runs on the aws-c-s3 client's event-loop thread
 * and blocking would stall socket I/O, TLS state machines, and task
 * scheduling for every other request sharing the same event loop.
 *
 * Two outcomes:
 *
 *   (a) Java pool has a slot available (or can grow into one): we
 *       build a ticket immediately and resolve the future
 *       synchronously. aws-c-s3 proceeds without ever awaiting.
 *
 *   (b) Java pool is exhausted (tryAcquireSlot returns -1): we append
 *       the unresolved future to pending_reserves and return it. The
 *       event-loop awaits the future; when a slot is released (in
 *       s_java_ticket_destroy), the next pending future is resolved
 *       with that slot. Mirrors the default pool's async behavior in
 *       aws-c-s3/source/s3_default_buffer_pool.c.
 *
 * The size requested is `meta.size`. We assume meta.size <= part_size
 * (configured to match between Java pool and aws-c-s3 client). If a
 * mismatched size is requested, we fail the future loudly rather than
 * silently truncating.
 */
static struct aws_future_s3_buffer_ticket *s_java_pool_reserve(
    struct aws_s3_buffer_pool *pool,
    struct aws_s3_buffer_pool_reserve_meta meta) {

    struct java_pool_state *ps = pool->impl;
    struct aws_future_s3_buffer_ticket *future = aws_future_s3_buffer_ticket_new(ps->allocator);

    /* Size sanity: our slots are exactly part_size. If the caller asks
     * for more, this pool cannot satisfy it. (The default pool falls
     * back to secondary storage for oversized requests; we deliberately
     * do NOT to keep the implementation simple and correct.) */
    if (meta.size > ps->part_size) {
        aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_S3_INVALID_MEMORY_LIMIT_CONFIG);
        AWS_LOGF_ERROR(
            AWS_LS_S3_CLIENT, "S3DirectBufferPool: reserve size %zu exceeds slot size %zu", meta.size, ps->part_size);
        return future;
    }

    /******** JNI ENV ACQUIRE ********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_INVALID_STATE);
        return future;
    }

    /* NON-BLOCKING acquire. Returns -1 if the pool is exhausted —
     * this MUST NOT block the calling thread. See
     * S3DirectBufferPool#tryAcquireSlot Javadoc for the contract. */
    jint slot_index = (*env)->CallIntMethod(env, ps->java_pool_global, ps->mid_try_acquire_slot);
    if (aws_jni_check_and_clear_exception(env)) {
        /* Most likely cause: OutOfMemoryError from ByteBuffer.allocateDirect
         * during lazy growth (MaxDirectMemorySize exhausted, or the JVM
         * could not satisfy the reservation). Also catches unexpected
         * IllegalStateException from a closed pool. */
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: tryAcquireSlot threw an exception "
            "(most likely OutOfMemoryError from allocateDirect during "
            "lazy growth); part_size=%zu — failing reserve future",
            ps->part_size);
        aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_S3_BUFFER_ALLOCATION_FAILED);
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
        return future;
    }

    if (slot_index < 0) {
        /* Java pool exhausted. */
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);

        /* can_block reservations (currently set only by aws-c-s3's
         * async-write upload path, aws_s3_meta_request_write) MUST NOT
         * be deferred indefinitely — see the can_block contract in
         * aws-c-s3/include/aws/s3/s3_buffer_pool.h. Our deferral is
         * only guaranteed to resolve when held slots are released
         * INDEPENDENTLY of the waiter's progress. That holds for
         * download parts (the network completes them) but NOT for
         * async-write buffered-data tickets, which are held until the
         * CUSTOMER writes more data: N concurrent async-write uploads
         * holding all N slots while the app awaits a pending write
         * future is a permanent deadlock. The default pool escapes
         * this by granting "forced" buffers beyond its memory limit;
         * we deliberately do NOT — a hard memory cap is a headline
         * guarantee of the DBZ pool (see createFixed /
         * validateDirectMemoryCapacity on the Java side).
         *
         * So we fail the reservation loudly instead of deadlocking.
         * NOTE: this path is unreachable today — aws-crt-java does not
         * expose the S3 async-write API, so nothing sets can_block.
         * This is a tripwire: if async write is ever bound, the binder
         * hits this clear error instead of a silent hang. Revisit with
         * a bounded-overflow design (explicitly sized overflow
         * allowance) if that day comes. */
        if (meta.can_block) {
            AWS_LOGF_ERROR(
                AWS_LS_S3_CLIENT,
                "S3DirectBufferPool: blocking reservation (can_block=true, async-write path) requested while the "
                "pool is exhausted. The Java DBZ pool does not grant over-limit forced buffers and cannot safely "
                "defer blocking reservations (deadlock risk). Failing the reservation. Use the default native "
                "buffer pool for async-write uploads, or size the DBZ pool for the expected concurrency.");
            aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_S3_BUFFER_ALLOCATION_FAILED);
            return future;
        }

        /* Non-blocking reservation: pend the future on our pending
         * list; it will be resolved later from s_java_ticket_destroy
         * when a slot frees. The event-loop returns immediately. */
        struct java_pending_reserve *pending = aws_mem_calloc(ps->allocator, 1, sizeof(struct java_pending_reserve));
        pending->meta = meta;
        pending->future = future;
        /* Acquire a ref on the future for the time it sits on the
         * pending list. Released when we resolve it (or when the
         * pool is destroyed with pending entries — error path). */
        aws_future_s3_buffer_ticket_acquire(pending->future);

        aws_mutex_lock(&ps->pending_lock);
        aws_linked_list_push_back(&ps->pending_reserves, &pending->node);
        aws_mutex_unlock(&ps->pending_lock);

        return future;
    }

    /* Slot acquired. Get its cached native address and build a
     * ticket synchronously. */
    jlong slot_addr_jl = (*env)->CallLongMethod(env, ps->java_pool_global, ps->mid_slot_address, slot_index);
    if (aws_jni_check_and_clear_exception(env)) {
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: slotAddress(%d) threw an exception "
            "(likely IllegalStateException from defensive slot-not-allocated check); "
            "returning slot and failing reserve future",
            (int)slot_index);
        /* return the slot we just acquired before failing */
        (*env)->CallVoidMethod(env, ps->java_pool_global, ps->mid_release_slot, slot_index);
        aws_jni_check_and_clear_exception(env);
        aws_future_s3_buffer_ticket_set_error(future, AWS_ERROR_INVALID_STATE);
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
        return future;
    }
    aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
    /******** JNI ENV RELEASE ********/

    /* Build ticket state. Helper extracted for reuse from the pending
     * drain path in s_java_ticket_destroy. Never returns NULL —
     * aws_mem_calloc aborts on OOM. */
    struct aws_s3_buffer_ticket *new_ticket = s_build_java_ticket(ps, slot_index, (void *)(uintptr_t)slot_addr_jl);

    aws_future_s3_buffer_ticket_set_result_by_move(future, &new_ticket);
    /* future holds a ref via set_result; ticket is now owned by the
     * future, then by aws-c-s3 once it pops it. */

    return future;
}

/*
 * Helper: build a ticket bound to the given slot. Used by reserve
 * (synchronous-success path) and by ticket-destroy (pending-drain
 * path). Returns NULL on allocation failure.
 */
static struct aws_s3_buffer_ticket *s_build_java_ticket(struct java_pool_state *ps, jint slot_index, void *slot_addr) {

    /* aws_mem_calloc aborts on OOM; no NULL check needed. */
    struct java_ticket_state *ts = aws_mem_calloc(ps->allocator, 1, sizeof(struct java_ticket_state));

    /* Each ticket holds a refcount on the pool so the pool state (and
     * the Java pool object it pins via the JNI global ref) outlives
     * every outstanding ticket — including S3BorrowedBuffer tickets
     * the Java customer holds past client shutdown. Released as the
     * final step of s_java_ticket_destroy. */
    aws_s3_buffer_pool_acquire(&ps->pool);

    ts->pool_state = ps;
    ts->slot_index = slot_index;
    ts->slot_addr = slot_addr;
    ts->capacity = ps->part_size;

    ts->ticket.vtable = &s_java_ticket_vtable;
    ts->ticket.impl = ts;
    aws_ref_count_init(&ts->ticket.ref_count, ts, s_java_ticket_destroy);

    return &ts->ticket;
}

/*
 * Helper: release a slot back to the Java free queue. Called only
 * when there is no pending future to hand the slot to directly.
 */
static void s_release_slot_via_jni(struct java_pool_state *ps, jint slot_index) {
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env != NULL) {
        (*env)->CallVoidMethod(env, ps->java_pool_global, ps->mid_release_slot, slot_index);
        if (aws_jni_check_and_clear_exception(env)) {
            AWS_LOGF_WARN(
                AWS_LS_S3_CLIENT,
                "S3DirectBufferPool: releaseSlot(%d) threw an exception "
                "(likely IllegalStateException from defensive slot-not-allocated check); "
                "slot may leak from Java-side tracking",
                (int)slot_index);
        }
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
    } else {
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT, "S3DirectBufferPool: could not release slot %d — JVM shutting down", (int)slot_index);
    }
}

/*
 * Called by aws-c-s3's client on the periodic idleness-gated trim
 * schedule (see s_s3_client_schedule_buffer_pool_trim_synced in
 * aws-c-s3/source/s3_client.c). The client only invokes trim when
 * num_requests_in_flight == 0 at both schedule and execution time,
 * so we can safely reclaim slots that sit in the pool's free list
 * without racing an in-flight reader.
 *
 * We delegate to S3DirectBufferPool.trim() on the Java side, which
 * nulls slots[i]/slotAddresses[i] for every free slot with index
 * >= initialSlots and then forces synchronous native-memory
 * release via DirectBufferCleaner. This mirrors the native default
 * pool's aws_mem_release timing — direct memory returns to the OS
 * in microseconds rather than waiting for the JVM's Cleaner to
 * process phantom references during some future GC pass.
 *
 * Best-effort: any JNI exception is caught and logged; trim is
 * non-critical and must never bring down the client.
 */
static void s_java_pool_trim(struct aws_s3_buffer_pool *pool) {
    struct java_pool_state *ps = pool->impl;

    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env == NULL) {
        /* JVM shutting down — nothing to do. Slots will be reclaimed
         * as part of VM teardown. */
        return;
    }

    (*env)->CallVoidMethod(env, ps->java_pool_global, ps->mid_trim);
    if (aws_jni_check_and_clear_exception(env)) {
        /* trim() is defensive against nulls / closed pool, so an
         * exception here is unexpected. Log for diagnosis but do
         * not propagate — trim is fire-and-forget. */
        AWS_LOGF_WARN(
            AWS_LS_S3_CLIENT,
            "S3DirectBufferPool: trim() threw an exception; some direct memory "
            "may remain pinned until the next GC pass");
    }

    aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
}

/*
 * Invoked via aws_ref_count when the pool's refcount reaches zero,
 * which happens when the owning aws_s3_client is destroyed. Fails
 * any remaining pending-reserve futures, releases our global JNI
 * ref, and frees state. The Java pool itself remains valid (the
 * application may still hold a reference and create a new client)
 * — we only release OUR claim on it.
 *
 * Under normal teardown, pending_reserves should be empty — the
 * client's meta-request shutdown is supposed to cancel outstanding
 * reserves. We handle non-empty defensively to avoid leaking
 * unresolved futures.
 */
static void s_java_pool_destroy(void *user_data) {
    struct java_pool_state *ps = user_data;

    /* Fail any leftover pending reserves before tearing down the
     * mutex. Each entry holds a ref we acquired in s_java_pool_reserve. */
    aws_mutex_lock(&ps->pending_lock);
    while (!aws_linked_list_empty(&ps->pending_reserves)) {
        struct aws_linked_list_node *node = aws_linked_list_pop_front(&ps->pending_reserves);
        struct java_pending_reserve *pending = AWS_CONTAINER_OF(node, struct java_pending_reserve, node);

        aws_future_s3_buffer_ticket_set_error(pending->future, AWS_ERROR_S3_CANCELED);
        aws_future_s3_buffer_ticket_release(pending->future);
        aws_mem_release(ps->allocator, pending);
    }
    aws_mutex_unlock(&ps->pending_lock);
    aws_mutex_clean_up(&ps->pending_lock);

    /******** JNI ENV ACQUIRE ********/
    struct aws_jvm_env_context jvm_env_context = aws_jni_acquire_thread_env(ps->jvm);
    JNIEnv *env = jvm_env_context.env;
    if (env != NULL) {
        (*env)->DeleteGlobalRef(env, ps->java_pool_global);
        aws_jni_release_thread_env(ps->jvm, &jvm_env_context);
    }
    /* If env is NULL, JVM is shutting down — global refs are released
     * automatically as part of VM teardown. */
    /******** JNI ENV RELEASE ********/

    aws_mem_release(ps->allocator, ps);
}

/* ------------------------------------------------------------------ */
/* FACTORY (registered into client config).                           */
/* ------------------------------------------------------------------ */

/*
 * Wired into aws_s3_client_config_options.buffer_pool_factory_fn
 * by Layer 3 (s3_client.c) when the Java caller attaches a pool.
 *
 * `user_data` is a JNI global ref to the S3DirectBufferPool Java
 * object. This factory function takes ownership of that ref. From
 * here, the pool state holds the ref until the pool is destroyed.
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
        return NULL;
    }

    jobject java_pool_global = factory_data->java_pool_global;
    JavaVM *jvm = factory_data->jvm;

    /* Take ownership of the JNI global ref: clear the caller's
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

    /* STEP 4: Resolve method IDs once. The method IDs live in
     * java_class_ids.c (Layer 4) — we cache copies here for
     * predictable cache behavior. */
    ps->mid_try_acquire_slot = s3_direct_buffer_pool_properties.tryAcquireSlot;
    ps->mid_release_slot = s3_direct_buffer_pool_properties.releaseSlot;
    ps->mid_slot_address = s3_direct_buffer_pool_properties.slotAddress;
    ps->mid_trim = s3_direct_buffer_pool_properties.trim;

    /* STEP 5: Wire vtable and ref_count. Pool is now valid; the
     * global ref is owned by ps and will be released in
     * s_java_pool_destroy. */
    ps->pool.vtable = &s_java_pool_vtable;
    ps->pool.impl = ps;
    aws_ref_count_init(&ps->pool.ref_count, ps, s_java_pool_destroy);

    AWS_LOGF_INFO(
        AWS_LS_S3_CLIENT, "S3DirectBufferPool factory: pool=%p part_size=%zu", (void *)&ps->pool, ps->part_size);

    return &ps->pool;

error_release_global_ref:
    /* Failure path: ps either was never allocated or has been freed.
     * The global ref is still owned by us — delete it before
     * returning NULL. */
    {
        struct aws_jvm_env_context cleanup_env = aws_jni_acquire_thread_env(jvm);
        if (cleanup_env.env != NULL) {
            (*cleanup_env.env)->DeleteGlobalRef(cleanup_env.env, java_pool_global);
            aws_jni_release_thread_env(jvm, &cleanup_env);
        }
        /* If env is NULL here, JVM is shutting down — the global ref
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

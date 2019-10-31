/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

#include <aws/common/atomics.h>
#include <aws/common/common.h>
#include <aws/common/string.h>
#include <aws/common/thread.h>
#include <aws/http/connection.h>
#include <aws/http/http.h>
#include <aws/io/io.h>
#include <aws/io/logging.h>
#include <aws/io/tls_channel_handler.h>
#include <aws/mqtt/mqtt.h>

#include <stdio.h>

#include "async_callback.h"
#include "crt.h"
#include "logging.h"

#if defined(AWS_HAVE_EXECINFO)
#    define ALLOC_TRACE_AVAILABLE
#    include <execinfo.h>
#    include <limits.h>
#endif

#include <aws/common/hash_table.h>
#include <aws/common/mutex.h>
#include <aws/common/priority_queue.h>
#include <aws/common/system_info.h>
#include <aws/common/time.h>

/* 0 = off, 1 = bytes, 2 = stack traces */
static int s_memory_tracing = 0;

/* number of stack frames to collect per stack */
#define ALLOC_TRACING_FRAMES 8

/* describes a single live allocation */
struct alloc_t {
    size_t size;
    time_t time;
    uint64_t stack; /* hash of stack frame pointers */
};

/* one of these is stored per unique stack */
struct stacktrace_t {
    void *const frames[ALLOC_TRACING_FRAMES];
};

/* Tracking structure, used as the allocator impl */
static struct alloc_tracker {
    struct aws_allocator *allocator; /* underlying allocator */
    struct aws_atomic_var allocated; /* bytes currently allocated */
    struct aws_mutex mutex;          /* protects everything below */
    struct aws_hash_table allocs;    /* live allocations, maps address -> alloc_t */
    struct aws_hash_table stacks;    /* unique stack traces, maps hash -> stacktrace_t */
} s_alloc_tracker;

static void *s_jni_mem_acquire(struct aws_allocator *allocator, size_t size);
static void s_jni_mem_release(struct aws_allocator *allocator, void *ptr);
static void *s_jni_mem_realloc(struct aws_allocator *allocator, void *ptr, size_t old_size, size_t new_size);
static void *s_jni_mem_calloc(struct aws_allocator *allocator, size_t num, size_t size);

static struct aws_allocator s_jni_allocator = {
    .mem_acquire = s_jni_mem_acquire,
    .mem_release = s_jni_mem_release,
    .mem_realloc = s_jni_mem_realloc,
    .mem_calloc = s_jni_mem_calloc,
    .impl = &s_alloc_tracker,
};

/* for the hash table, to destroy elements */
static void s_destroy_alloc(void *data) {
    struct aws_allocator *allocator = ((struct alloc_tracker *)s_jni_allocator.impl)->allocator;
    struct alloc_t *alloc = data;
    aws_mem_release(allocator, alloc);
}

static void s_destroy_stacktrace(void *data) {
    struct aws_allocator *allocator = ((struct alloc_tracker *)s_jni_allocator.impl)->allocator;
    struct stacktrace_t *stack = data;
    aws_mem_release(allocator, stack);
}

static uint64_t s_stack_hash(const void *item) {
    /* yes, this truncates on 32-bit, no it doesn't matter, it's a hash */
    size_t value = (size_t)item;
    return aws_hash_ptr((void *)value);
}

static bool s_stack_eq(const void *a, const void *b) {
    uint64_t va = (uint64_t)a;
    uint64_t vb = (uint64_t)b;
    return va == vb;
}

static void s_alloc_tracker_init(struct alloc_tracker *tracker, struct aws_allocator *allocator) {
    tracker->allocator = allocator;
    aws_atomic_init_int(&tracker->allocated, 0);
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_mutex_init(&tracker->mutex));
    AWS_FATAL_ASSERT(
        AWS_OP_SUCCESS ==
        aws_hash_table_init(
            &tracker->allocs, tracker->allocator, 1024, aws_hash_ptr, aws_ptr_eq, NULL, s_destroy_alloc));
    if (s_memory_tracing == 2) {
        AWS_FATAL_ASSERT(
            AWS_OP_SUCCESS ==
            aws_hash_table_init(
                &tracker->stacks, tracker->allocator, 1024, s_stack_hash, s_stack_eq, NULL, s_destroy_stacktrace));
    }
}

static void s_alloc_tracker_track(struct alloc_tracker *tracker, void *ptr, size_t size) {
    struct alloc_t *alloc = aws_mem_calloc(tracker->allocator, 1, sizeof(struct alloc_t));
    alloc->size = size;
    alloc->time = time(NULL);

#if defined(ALLOC_TRACE_AVAILABLE)
    if (s_memory_tracing == 2) {
        /* capture stack frames */
        void *stack_frames[2 + ALLOC_TRACING_FRAMES];
        int stack_depth = backtrace(stack_frames, AWS_ARRAY_SIZE(stack_frames));
        struct aws_byte_cursor stack_cursor = aws_byte_cursor_from_array(stack_frames, stack_depth * sizeof(void *));
        /* hash the stack pointers */
        uint64_t stack_id = aws_hash_byte_cursor_ptr(&stack_cursor);
        alloc->stack = stack_id; /* associate the stack with the alloc */
        struct aws_hash_element *item = NULL;
        int was_created = 0;
        AWS_FATAL_ASSERT(
            AWS_OP_SUCCESS == aws_hash_table_create(&tracker->stacks, (void *)stack_id, &item, &was_created));
        /* If this is a new stack, save it to the hash */
        if (was_created) {
            struct stacktrace_t *stack = aws_mem_calloc(tracker->allocator, 1, sizeof(struct stacktrace_t));
            memcpy((void **)&stack->frames[0], &stack_frames[2], (stack_depth - 2) * sizeof(void *));
            item->value = stack;
        }
    }
#endif

    aws_mutex_lock(&tracker->mutex);
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_put(&tracker->allocs, ptr, alloc, NULL));
    aws_atomic_fetch_add(&tracker->allocated, size);
    aws_mutex_unlock(&tracker->mutex);
}

static void s_alloc_tracker_untrack(struct alloc_tracker *tracker, void *ptr) {
    aws_mutex_lock(&tracker->mutex);
    struct aws_hash_element item;
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_remove(&tracker->allocs, ptr, &item, NULL));
    AWS_FATAL_ASSERT(item.key && item.value);
    struct alloc_t *alloc = item.value;
    aws_atomic_fetch_sub(&tracker->allocated, alloc->size);
    aws_mutex_unlock(&tracker->mutex);
    s_destroy_alloc(item.value);
}

#if defined(ALLOC_TRACE_AVAILABLE)
/* used only to resolve stacks -> trace, count, size at dump time */
struct stack_info_t {
    struct aws_string *trace;
    size_t count;
    size_t size;
};

static int s_collect_stack_trace(void *context, struct aws_hash_element *item) {
    struct aws_hash_table *all_stacks = context;
    struct aws_allocator *allocator = ((struct alloc_tracker *)s_jni_allocator.impl)->allocator;
    struct stack_info_t *stack_info = item->value;
    struct aws_hash_element *stack_item = NULL;
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_find(all_stacks, item->key, &stack_item));
    AWS_FATAL_ASSERT(stack_item);
    struct stacktrace_t *stack = stack_item->value;
    void *const *stack_frames = &stack->frames[0];
    size_t num_frames = 0;
    while (stack_frames[num_frames] != NULL && num_frames < ALLOC_TRACING_FRAMES) {
        ++num_frames;
    }

    /* convert the frame pointers to symbols, and concat into a buffer */
    char buf[4096] = {0};
    struct aws_byte_buf stacktrace = aws_byte_buf_from_empty_array(buf, AWS_ARRAY_SIZE(buf));
    struct aws_byte_cursor newline = aws_byte_cursor_from_c_str("\n");
    char **symbols = backtrace_symbols(stack_frames, num_frames);
    for (int idx = 0; idx < num_frames; ++idx) {
        if (idx > 0) {
            aws_byte_buf_append(&stacktrace, &newline);
        }
        const char *caller = symbols[idx];
        if (!caller || !caller[0]) {
            break;
        }
        struct aws_byte_cursor cursor = aws_byte_cursor_from_c_str(caller);
        aws_byte_buf_append(&stacktrace, &cursor);
    }
    free(symbols);
    /* record the resultant buffer as a string */
    stack_info->trace = aws_string_new_from_array(allocator, stacktrace.buffer, stacktrace.len);
    aws_byte_buf_clean_up(&stacktrace);
    return AWS_COMMON_HASH_TABLE_ITER_CONTINUE;
}

static int s_stack_info_compare_size(const void *a, const void *b) {
    const struct stack_info_t *stack_a = *(const struct stack_info_t **)a;
    const struct stack_info_t *stack_b = *(const struct stack_info_t **)b;
    return stack_b->size > stack_a->size;
}

static int s_stack_info_compare_count(const void *a, const void *b) {
    const struct stack_info_t *stack_a = *(const struct stack_info_t **)a;
    const struct stack_info_t *stack_b = *(const struct stack_info_t **)b;
    return stack_b->count > stack_a->count;
}

static void s_stack_info_destroy(void *data) {
    struct aws_allocator *allocator = ((struct alloc_tracker *)s_jni_allocator.impl)->allocator;
    struct stack_info_t *stack = data;
    aws_string_destroy(stack->trace);
    aws_mem_release(allocator, stack);
}

/* tally up count/size per stack from all allocs */
static int s_collect_stack_stats(void *context, struct aws_hash_element *item) {
    struct aws_hash_table *stacks = context;
    struct alloc_t *alloc = item->value;
    struct aws_hash_element *stack_item = NULL;
    int was_created = 0;
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_create(stacks, (void *)alloc->stack, &stack_item, &was_created));
    if (was_created) {
        struct aws_allocator *allocator = ((struct alloc_tracker *)s_jni_allocator.impl)->allocator;
        stack_item->value = aws_mem_calloc(allocator, 1, sizeof(struct stack_info_t));
    }
    struct stack_info_t *stack = stack_item->value;
    stack->count++;
    stack->size += alloc->size;
    return AWS_COMMON_HASH_TABLE_ITER_CONTINUE;
}

static int s_insert_stacks(void *context, struct aws_hash_element *item) {
    struct aws_priority_queue *pq = context;
    struct stack_info_t *stack = item->value;
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_priority_queue_push(pq, &stack));
    return AWS_COMMON_HASH_TABLE_ITER_CONTINUE;
}
#endif

static int s_insert_allocs(void *context, struct aws_hash_element *item) {
    struct aws_priority_queue *allocs = context;
    struct alloc_t *alloc = item->value;
    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_priority_queue_push(allocs, &alloc));
    return AWS_COMMON_HASH_TABLE_ITER_CONTINUE;
}

static int s_alloc_compare(const void *a, const void *b) {
    const struct alloc_t *alloc_a = *(const struct alloc_t **)a;
    const struct alloc_t *alloc_b = *(const struct alloc_t **)b;
    return alloc_a->time > alloc_b->time;
}

static void s_alloc_tracker_dump(struct alloc_tracker *tracker) {
    if (aws_atomic_load_int(&tracker->allocated) == 0) {
        return;
    }

    size_t num_allocs = aws_hash_table_get_entry_count(&tracker->allocs);
    fprintf(
        stderr,
        "TRACKER: %zu bytes still allocated in %zu allocations\n",
        aws_atomic_load_int(&tracker->allocated),
        num_allocs);
#if defined(ALLOC_TRACE_AVAILABLE)
    /* convert stacks from pointers -> symbols */
    struct aws_hash_table stacks; /* maps stack hash/id -> stack_info_t */
    AWS_FATAL_ASSERT(
        AWS_OP_SUCCESS ==
        aws_hash_table_init(&stacks, tracker->allocator, 64, s_stack_hash, s_stack_eq, NULL, s_stack_info_destroy));
    /* collect active stacks, tally up sizes and counts */
    aws_hash_table_foreach(&tracker->allocs, s_collect_stack_stats, &stacks);
    /* collect stack traces for active stacks */
    aws_hash_table_foreach(&stacks, s_collect_stack_trace, &tracker->stacks);
#endif
    /* sort allocs by time */
    struct aws_priority_queue allocs;
    aws_priority_queue_init_dynamic(&allocs, tracker->allocator, num_allocs, sizeof(struct alloc_t *), s_alloc_compare);
    aws_hash_table_foreach(&tracker->allocs, s_insert_allocs, &allocs);
    /* dump allocs by time */
    fprintf(stderr, "################################################################################\n");
    fprintf(stderr, "Leaks in order of allocation:\n");
    fprintf(stderr, "################################################################################\n");
    while (aws_priority_queue_size(&allocs)) {
        struct alloc_t *alloc = NULL;
        aws_priority_queue_pop(&allocs, &alloc);
        fprintf(stderr, "ALLOC %zu bytes\n", alloc->size);
#if defined(ALLOC_TRACE_AVAILABLE)
        if (alloc->stack) {
            struct aws_hash_element *item = NULL;
            AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_hash_table_find(&stacks, (void *)alloc->stack, &item));
            struct stack_info_t *stack = item->value;
            fprintf(stderr, "  stacktrace:\n%s\n", (const char *)aws_string_bytes(stack->trace));
        }
#endif
    }

    aws_priority_queue_clean_up(&allocs);
#if defined(ALLOC_TRACE_AVAILABLE)
    size_t num_stacks = aws_hash_table_get_entry_count(&stacks);
    /* sort stacks by total size leaked */
    struct aws_priority_queue stacks_by_size;
    AWS_FATAL_ASSERT(
        AWS_OP_SUCCESS ==
        aws_priority_queue_init_dynamic(
            &stacks_by_size, tracker->allocator, num_stacks, sizeof(struct stack_info_t *), s_stack_info_compare_size));
    aws_hash_table_foreach(&stacks, s_insert_stacks, &stacks_by_size);
    fprintf(stderr, "################################################################################\n");
    fprintf(stderr, "Stacks by bytes leaked:\n");
    fprintf(stderr, "################################################################################\n");
    while (aws_priority_queue_size(&stacks_by_size) > 0) {
        struct stack_info_t *stack = NULL;
        aws_priority_queue_pop(&stacks_by_size, &stack);
        fprintf(stderr, "%zu bytes in %zu allocations:\n", stack->size, stack->count);
        fprintf(stderr, "%s\n", (const char *)aws_string_bytes(stack->trace));
    }
    aws_priority_queue_clean_up(&stacks_by_size);

    /* sort stacks by number of leaks */
    struct aws_priority_queue stacks_by_count;
    AWS_FATAL_ASSERT(
        AWS_OP_SUCCESS == aws_priority_queue_init_dynamic(
                              &stacks_by_count,
                              tracker->allocator,
                              num_stacks,
                              sizeof(struct stack_info_t *),
                              s_stack_info_compare_count));
    fprintf(stderr, "################################################################################\n");
    fprintf(stderr, "Stacks by number of leaks:\n");
    fprintf(stderr, "################################################################################\n");
    aws_hash_table_foreach(&stacks, s_insert_stacks, &stacks_by_count);
    while (aws_priority_queue_size(&stacks_by_count) > 0) {
        struct stack_info_t *stack = NULL;
        aws_priority_queue_pop(&stacks_by_count, &stack);
        fprintf(stderr, "%zu allocations leaking %zu bytes:\n", stack->count, stack->size);
        fprintf(stderr, "%s\n", (const char *)aws_string_bytes(stack->trace));
    }
    aws_priority_queue_clean_up(&stacks_by_count);
    aws_hash_table_clean_up(&stacks);
#endif
    fflush(stderr);
    // abort();
}

static void *s_jni_mem_acquire(struct aws_allocator *allocator, size_t size) {
    struct alloc_tracker *tracker = allocator->impl;
    void *ptr = aws_mem_acquire(tracker->allocator, size);
    s_alloc_tracker_track(tracker, ptr, size);
    return ptr;
}

static void s_jni_mem_release(struct aws_allocator *allocator, void *ptr) {
    struct alloc_tracker *tracker = allocator->impl;
    s_alloc_tracker_untrack(tracker, ptr);
    aws_mem_release(tracker->allocator, ptr);
}

static void *s_jni_mem_realloc(struct aws_allocator *allocator, void *ptr, size_t old_size, size_t new_size) {
    struct alloc_tracker *tracker = allocator->impl;
    void *new_ptr = ptr;

    AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_mem_realloc(tracker->allocator, &new_ptr, old_size, new_size));

    s_alloc_tracker_untrack(tracker, ptr);
    s_alloc_tracker_track(tracker, new_ptr, new_size);

    return new_ptr;
}

static void *s_jni_mem_calloc(struct aws_allocator *allocator, size_t num, size_t size) {
    struct alloc_tracker *tracker = allocator->impl;
    void *ptr = aws_mem_calloc(tracker->allocator, num, size);
    s_alloc_tracker_track(tracker, ptr, num * size);
    return ptr;
}

static struct aws_allocator *s_init_allocator() {
    if (s_memory_tracing) {
        struct aws_allocator *allocator = aws_default_allocator();
        s_alloc_tracker_init(&s_alloc_tracker, allocator);
        return &s_jni_allocator;
    }
    return aws_default_allocator();
}
static struct aws_allocator *s_allocator = NULL;
struct aws_allocator *aws_jni_get_allocator() {
    if (AWS_UNLIKELY(s_allocator == NULL)) {
        s_allocator = s_init_allocator();
    }
    return s_allocator;
}

void aws_jni_throw_runtime_exception(JNIEnv *env, const char *msg, ...) {
    va_list args;
    va_start(args, msg);
    char buf[1024];
    vsnprintf(buf, sizeof(buf), msg, args);
    va_end(args);

    char exception[1280];
    snprintf(exception, sizeof(exception), "%s (aws_last_error: %s)", buf, aws_error_str(aws_last_error()));
    jclass runtime_exception = (*env)->FindClass(env, "software/amazon/awssdk/crt/CrtRuntimeException");
    (*env)->ThrowNew(env, runtime_exception, exception);
}

/* methods of Java's ByteBuffer Class */
static struct {
    jclass cls;
    jmethodID get_capacity; /* The total number of bytes in the internal byte array. Stays constant. */
    jmethodID get_limit;    /* The max allowed read/write position of the Buffer. limit must be <= capacity. */
    jmethodID set_limit;
    jmethodID get_position; /* The current read/write position of the Buffer. position must be <= limit */
    jmethodID set_position;
    jmethodID get_remaining; /* Remaining number of bytes before the limit is reached. Equal to (limit - position). */
    jmethodID wrap;          /* Creates a new ByteBuffer Object from a Java byte[]. */
} s_java_byte_buffer = {0};

void s_cache_java_byte_buffer(JNIEnv *env) {
    jclass cls = (*env)->FindClass(env, "java/nio/ByteBuffer");
    AWS_FATAL_ASSERT(cls);

    // FindClass() returns local JNI references that become eligible for GC once this native method returns to Java.
    // Call NewGlobalRef() so that this class reference doesn't get Garbage collected.
    s_java_byte_buffer.cls = (*env)->NewGlobalRef(env, cls);

    s_java_byte_buffer.get_capacity = (*env)->GetMethodID(env, cls, "capacity", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_capacity);

    s_java_byte_buffer.get_limit = (*env)->GetMethodID(env, cls, "limit", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_limit);

    s_java_byte_buffer.set_limit = (*env)->GetMethodID(env, cls, "limit", "(I)Ljava/nio/Buffer;");
    AWS_FATAL_ASSERT(s_java_byte_buffer.set_limit);

    s_java_byte_buffer.get_position = (*env)->GetMethodID(env, cls, "position", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_position);

    s_java_byte_buffer.set_position = (*env)->GetMethodID(env, cls, "position", "(I)Ljava/nio/Buffer;");
    AWS_FATAL_ASSERT(s_java_byte_buffer.set_position);

    s_java_byte_buffer.get_remaining = (*env)->GetMethodID(env, cls, "remaining", "()I");
    AWS_FATAL_ASSERT(s_java_byte_buffer.get_remaining);

    s_java_byte_buffer.wrap = (*env)->GetStaticMethodID(env, cls, "wrap", "([B)Ljava/nio/ByteBuffer;");
    AWS_FATAL_ASSERT(s_java_byte_buffer.wrap);
}

jbyteArray aws_java_byte_array_new(JNIEnv *env, size_t size) {
    jbyteArray jArray = (*env)->NewByteArray(env, (jsize)size);
    return jArray;
}

bool aws_copy_native_array_to_java_byte_array(JNIEnv *env, jbyteArray dst, uint8_t *src, size_t amount) {
    (*env)->SetByteArrayRegion(env, dst, 0, (jsize)amount, (jbyte *)src);
    return (*env)->ExceptionCheck(env);
}

jobject aws_java_byte_array_to_java_byte_buffer(JNIEnv *env, jbyteArray jArray) {
    jobject jByteBuffer = (*env)->CallStaticObjectMethod(env, s_java_byte_buffer.cls, s_java_byte_buffer.wrap, jArray);
    return ((*env)->ExceptionCheck(env)) ? NULL: jByteBuffer;
}

/**
 * Converts a Native aws_byte_cursor to a Java byte[]
 */
jbyteArray aws_jni_byte_array_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data) {
    jbyteArray jArray = aws_java_byte_array_new(env, native_data->len);
    if (jArray) {
        if (!aws_copy_native_array_to_java_byte_array(env, jArray, native_data->ptr, native_data->len)) {
            return jArray;
        }
    }
    return NULL;
}

/**
 * Converts a Native aws_byte_cursor to a Java ByteBuffer Object
 */
jobject aws_jni_byte_buffer_copy_from_cursor(JNIEnv *env, const struct aws_byte_cursor *native_data) {
    AWS_FATAL_ASSERT(env);
    jbyteArray jArray = aws_jni_byte_array_from_cursor(env, native_data);
    jobject jByteBuffer = aws_java_byte_array_to_java_byte_buffer(env, jArray);

    return jByteBuffer;
}

/**
 * Get the Buffer Position (the next element to read/write)
 */
int aws_jni_byte_buffer_get_position(JNIEnv *env, jobject java_byte_buffer) {
    jint position = (*env)->CallIntMethod(env, java_byte_buffer, s_java_byte_buffer.get_position);
    return ((*env)->ExceptionCheck(env)) ? -1 : (int)position;
}

/**
 * Set the Buffer Position (the next element to read/write)
 */
void aws_jni_byte_buffer_set_position(JNIEnv *env, jobject jByteBuf, jint position) {
    (*env)->CallObjectMethod(env, jByteBuf, s_java_byte_buffer.set_position, position);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
}

/**
 * Set the Buffer Limit (the max allowed element to read/write)
 */
void aws_jni_byte_buffer_set_limit(JNIEnv *env, jobject jByteBuf, jint limit) {
    (*env)->CallObjectMethod(env, jByteBuf, s_java_byte_buffer.set_limit, limit);
    AWS_FATAL_ASSERT(!(*env)->ExceptionCheck(env));
}

/**
 * Populates a aws_byte_buf struct from a Java DirectByteBuffer Object
 */
void aws_jni_native_byte_buf_from_java_direct_byte_buf(JNIEnv *env, jobject directBuf, struct aws_byte_buf *dst) {
    dst->buffer = (*env)->GetDirectBufferAddress(env, directBuf);
    dst->capacity = (size_t)(*env)->GetDirectBufferCapacity(env, directBuf);
    dst->len = aws_jni_byte_buffer_get_position(env, directBuf);
}

jobject aws_jni_direct_byte_buffer_from_raw_ptr(JNIEnv *env, const void *dst, size_t capacity) {

    jobject jByteBuf = (*env)->NewDirectByteBuffer(env, (void *)dst, (jlong)capacity);
    if (jByteBuf) {
        aws_jni_byte_buffer_set_limit(env, jByteBuf, (jint)capacity);
        aws_jni_byte_buffer_set_position(env, jByteBuf, 0);
    }

    return jByteBuf;
}

/**
 * Converts a Native aws_byte_cursor to a Java DirectByteBuffer
 */
jobject aws_jni_direct_byte_buffer_from_byte_buf(JNIEnv *env, const struct aws_byte_buf *dst) {
    return aws_jni_direct_byte_buffer_from_raw_ptr(env, (void *)dst->buffer, (jlong)dst->capacity);
}

struct aws_byte_cursor aws_jni_byte_cursor_from_jstring(JNIEnv *env, jstring str) {
    return aws_byte_cursor_from_array(
        (*env)->GetStringUTFChars(env, str, NULL), (size_t)(*env)->GetStringUTFLength(env, str));
}

struct aws_byte_cursor aws_jni_byte_cursor_from_direct_byte_buffer(JNIEnv *env, jobject byte_buffer) {
    jlong payload_size = (*env)->GetDirectBufferCapacity(env, byte_buffer);
    if (payload_size == -1) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_publish: Unable to get capacity of payload ByteBuffer");
        return aws_byte_cursor_from_array(NULL, 0);
    }
    jbyte *payload_data = (*env)->GetDirectBufferAddress(env, byte_buffer);
    if (!payload_data) {
        aws_jni_throw_runtime_exception(
            env, "MqttClientConnection.mqtt_publish: Unable to get buffer from payload ByteBuffer");
        return aws_byte_cursor_from_array(NULL, 0);
    }
    return aws_byte_cursor_from_array((const uint8_t *)payload_data, (size_t)payload_size);
}

struct aws_string *aws_jni_new_string_from_jstring(JNIEnv *env, jstring str) {
    struct aws_allocator *allocator = aws_jni_get_allocator();
    return aws_string_new_from_c_str(allocator, (*env)->GetStringUTFChars(env, str, NULL));
}

void s_detach_jvm_from_thread(void *user_data) {
    JavaVM *jvm = user_data;
    (*jvm)->DetachCurrentThread(jvm);
}

JNIEnv *aws_jni_get_thread_env(JavaVM *jvm) {
    JNIEnv *env = NULL;
    if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) == JNI_EDETACHED) {
        jint result = (*jvm)->AttachCurrentThreadAsDaemon(jvm, (void **)&env, NULL);
        (void)result;
        AWS_FATAL_ASSERT(result == JNI_OK);
        /* This should only happen in event loop threads, the JVM main thread attachment is
         * managed by the JVM, so we only need to clean up event loop thread attachments */
        AWS_FATAL_ASSERT(AWS_OP_SUCCESS == aws_thread_current_at_exit(s_detach_jvm_from_thread, (void *)jvm));
    }

    return env;
}

#if defined(_MSC_VER)
#    pragma warning(push)
#    pragma warning(disable : 4210) /* non-standard extension used: function given file scope */
#endif
static void s_cache_jni_classes(JNIEnv *env) {
    extern void s_cache_mqtt_connection(JNIEnv *);
    extern void s_cache_message_handler(JNIEnv *);
    extern void s_cache_mqtt_exception(JNIEnv *);
    extern void s_cache_http_conn_manager(JNIEnv *);
    extern void s_cache_crt_http_stream_handler(JNIEnv *);
    extern void s_cache_http_header(JNIEnv *);
    extern void s_cache_http_stream(JNIEnv *);
    extern void s_cache_event_loop_group(JNIEnv *);
    extern void s_cache_crt_byte_buffer(JNIEnv * env);

    s_cache_java_byte_buffer(env);
    s_cache_mqtt_connection(env);
    s_cache_async_callback(env);
    s_cache_message_handler(env);
    s_cache_mqtt_exception(env);
    s_cache_http_conn_manager(env);
    s_cache_crt_http_stream_handler(env);
    s_cache_http_header(env);
    s_cache_http_stream(env);
    s_cache_event_loop_group(env);
    s_cache_crt_byte_buffer(env);
}
#if defined(_MSC_VER)
#    pragma warning(pop)
#endif

static void s_jni_atexit(void) {
    aws_http_library_clean_up();
    aws_mqtt_library_clean_up();
    aws_jni_cleanup_logging();

    if (s_memory_tracing) {
        s_alloc_tracker_dump((struct alloc_tracker *)s_jni_allocator.impl);
    }
}

/* Called as the entry point, immediately after the shared lib is loaded the first time by JNI */
JNIEXPORT
void JNICALL Java_software_amazon_awssdk_crt_CRT_awsCrtInit(JNIEnv *env, jclass jni_crt_class, jint jni_memtrace) {
    (void)jni_crt_class;

    s_memory_tracing = jni_memtrace;
#if !defined(ALLOC_TRACE_AVAILABLE)
    s_memory_tracing = (s_memory_tracing > 1) ? 1 : s_memory_tracing;
#endif

    struct aws_allocator *allocator = aws_jni_get_allocator();
    aws_mqtt_library_init(allocator);
    aws_http_library_init(allocator);

    s_cache_jni_classes(env);

    atexit(s_jni_atexit);
}

JNIEXPORT
jstring JNICALL Java_software_amazon_awssdk_crt_CRT_awsErrorString(JNIEnv *env, jclass jni_crt_class, jint error_code) {
    (void)jni_crt_class;
    const char *error_msg = aws_error_str(error_code);
    return (*env)->NewStringUTF(env, error_msg);
}

JNIEXPORT
jlong JNICALL Java_software_amazon_awssdk_crt_CRT_awsNativeMemory(JNIEnv *env, jclass jni_crt_class) {
    (void)env;
    (void)jni_crt_class;
    jlong allocated = 0;
    if (s_memory_tracing) {
        struct alloc_tracker *tracker = s_jni_allocator.impl;
        allocated = (jlong)aws_atomic_load_int(&tracker->allocated);
    }
    return allocated;
}

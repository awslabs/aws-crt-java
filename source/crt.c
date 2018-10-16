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

#include "aws/jni/com_amazon_aws_CrtResource.h"
#include <aws/common/common.h>
#include <aws/common/mutex.h>
#include <aws/common/atomics.h>
#include <stdio.h>

void JNICALL Java_com_amazon_aws_CrtResource_doIt(JNIEnv* env, jobject obj) {
    (void)env;
    (void)obj;
    printf("I DID THE THING\n");
}

struct jni_allocator {
    struct aws_atomic_var allocated;
};

struct jni_memory_header {
    size_t size;
};

static void *s_mem_acquire_malloc(struct aws_allocator *allocator, size_t size) {
    struct jni_allocator *jnialloc = (struct jni_allocator *)allocator->impl;

    struct jni_memory_header *memory =
        (struct jni_memory_header *)malloc(size + sizeof(struct jni_memory_header));

    if (!memory) {
        return NULL;
    }

    aws_atomic_fetch_add(&jnialloc->allocated, size);
    memory->size = size;
    return (uint8_t *)memory + sizeof(struct jni_memory_header);
}

static void s_mem_release_free(struct aws_allocator *allocator, void *ptr) {
    struct jni_allocator *jnialloc = (struct jni_allocator *)allocator->impl;
    struct jni_memory_header *memory =
        (struct jni_memory_header *)((uint8_t *)ptr - sizeof(struct jni_memory_header));
    
    aws_atomic_fetch_sub(&jnialloc->allocated, memory->size);
    free(memory);
}

struct aws_allocator *aws_jni_get_allocator() {
    static struct jni_allocator jnialloc = {
        .allocated = 0,
    };

    static struct aws_allocator aws_jni_alloc = {
        .mem_acquire = &s_mem_acquire_malloc,
        .mem_release = &s_mem_release_free,
        .mem_realloc = NULL,
        .impl = &jnialloc,
    };

    return &aws_jni_alloc;
}

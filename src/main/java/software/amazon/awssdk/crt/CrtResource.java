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
package software.amazon.awssdk.crt;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This wraps a native pointer to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 */
public abstract class CrtResource implements AutoCloseable {
    private static final ConcurrentHashMap<Long, String> NATIVE_RESOURCES = new ConcurrentHashMap<>();
    private static final long NULL = 0;
    private final LinkedList<CrtResource> referencedResources = new LinkedList<>();
    private long ptr;
    private AtomicInteger refCount = new AtomicInteger(1);

    static {
        /* This will cause the JNI lib to be loaded the first time a CRT is created */
        new CRT();
    }

    public static int getAllocatedNativeResourceCount() {
        return NATIVE_RESOURCES.size();
    }

    public static Collection<String> getAllocatedNativeResources() {
        return Collections.unmodifiableCollection(NATIVE_RESOURCES.values());
    }

    public CrtResource() {
    }

    /**
     * Marks a resource as referenced by this resource.
     * @param resource The referenced subresource
     * @return The original resource.
     */
    public <T extends CrtResource> T addReference(T resource) {
        CrtResource baseResource = resource;
        baseResource.refCount.incrementAndGet();
        referencedResources.push(resource);
        return resource;
    }

    protected void acquire(long _ptr) {
        if (!isNull()) {
            throw new IllegalStateException("Can't acquire >1 Native Pointer");
        }

        String canonicalName = this.getClass().getCanonicalName();

        if (_ptr == NULL) {
            throw new IllegalStateException("Can't acquire NULL Pointer: " + canonicalName);
        }

        String lastValue = NATIVE_RESOURCES.put(_ptr, canonicalName);

        if (lastValue != null) {
            throw new IllegalStateException("Acquired two CrtResources to the same Native Resource! Class: " + lastValue);
        }

        ptr = _ptr;
    }

    protected abstract void releaseNativeHandle();

    protected abstract boolean canReleaseReferencesImmediately();

    private void release() {
        if (isNull()) {
            throw new IllegalStateException("Already Released Resource!");
        }
        releaseNativeHandle();
        NATIVE_RESOURCES.remove(ptr);
        ptr = 0;
    }

    public long native_ptr() {
        return ptr;
    }

    public boolean isNull() {
        return (ptr == NULL);
    }

    @Override
    public void close() {
        if (!isNull()) {
            throw new IllegalStateException("Closing sub-resources before releasing parent Resource may cause use-after-free bugs!");
        }

        int remainingRefs = refCount.decrementAndGet();
        if (remainingRefs > 0) {
            return;
        }

        release();

        if (canReleaseReferencesImmediately()) {
            releaseReferences();
        }
    }

    protected void releaseReferences() {
        while(referencedResources.size() > 0) {
            CrtResource r = referencedResources.pop();
            r.close();
        }
    }
}

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
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import software.amazon.awssdk.crt.Log;

/**
 * This wraps a native pointer to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 */
public abstract class CrtResource implements AutoCloseable {
    private static final String NATIVE_DEBUG_PROPERTY_NAME = "aws.iot.sdk.debugnative";
    private static final long NULL = 0;

    private static final ConcurrentHashMap<Long, String> NATIVE_RESOURCES = new ConcurrentHashMap<>();

    private static boolean debugNativeObjects = System.getProperty(NATIVE_DEBUG_PROPERTY_NAME) != null;
    private static int resourceCount = 0;
    private static final Lock lock = new ReentrantLock();
    private static final Condition emptyResources  = lock.newCondition();

    private final LinkedList<CrtResource> referencedResources = new LinkedList<>();
    private long ptr;
    private AtomicInteger refCount = new AtomicInteger(1);

    static {
        /* This will cause the JNI lib to be loaded the first time a CRT is created */
        new CRT();
    }

    public CrtResource() {
    }

    /**
     * Marks a resource as referenced by this resource.
     * @param resource The referenced subresource
     * @return The original resource.
     */
    public <T extends CrtResource> T addReferenceTo(T resource) {
        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, String.format("Instance of class %s is adding a reference to instance of class %s", this.getClass().getCanonicalName(), resource.getClass().getCanonicalName()));
        }
        resource.addRef();
        referencedResources.push(resource);

        return resource;
    }

    public void addRef() {
        refCount.incrementAndGet();
    }

    protected void acquire(long _ptr) {
        if (!isNull()) {
            throw new IllegalStateException("Can't acquire >1 Native Pointer");
        }

        if (isAlwaysNullResource()) {
            throw new IllegalStateException("Always-null resources cannot acquire a native pointer");
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
        incrementNativeObjectCount();
    }

    protected abstract void releaseNativeHandle();

    protected abstract boolean canReleaseReferencesImmediately();

    private void release() {
        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, String.format("Releasing class %s", this.getClass().getCanonicalName()));
        }

        if (!isAlwaysNullResource()) {
            if (isNull()) {
                throw new IllegalStateException("Already Released Resource!");
            }

            /*
             * Recyclable resources (like http connections) may be given to another Java object during the call to releaseNativeHandle.
             * By removing from the map first, we prevent a double-bind exception from getting thrown when the second Java object
             * calls acquire on the native handle.
             *
             * "Totally reasonable approach" - Mike
             */
            NATIVE_RESOURCES.remove(ptr);
        }

        releaseNativeHandle();

        if (!isAlwaysNullResource()) {
            decrementNativeObjectCount();

            ptr = 0;
        }
    }

    public long native_ptr() {
        return ptr;
    }

    public boolean isNull() {
        return (ptr == NULL);
    }

    protected boolean isAlwaysNullResource() { return false; }

    @Override
    public void close() {
        int remainingRefs = refCount.decrementAndGet();

        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, String.format("Closing instance of class %s with %d remaining refs", this.getClass().getCanonicalName(), remainingRefs));
        }

        if (remainingRefs > 0) {
            return;
        }

        release();

        if (canReleaseReferencesImmediately()) {
            releaseReferences();
        }
    }

    protected void releaseReferences() {
        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, String.format("Instance of class %s closing referenced objects", this.getClass().getCanonicalName()));
        }

        while(referencedResources.size() > 0) {
            CrtResource r = referencedResources.pop();
            r.close();
        }
    }

    public static void logNativeResources() {
        Log.log(Log.LogLevel.Trace, "Dumping native object set:");
        for (Map.Entry<Long, String> entry : NATIVE_RESOURCES.entrySet()) {
            Log.log(Log.LogLevel.Trace, String.format(" * %s class instance using native pointer %d", entry.getValue(), entry.getKey().longValue()));
        }
    }

    private static void incrementNativeObjectCount() {
        if (!debugNativeObjects) {
            return;
        }

        lock.lock();
        try {
            ++resourceCount;
        } finally {
            lock.unlock();
        }
    }

    private static void decrementNativeObjectCount() {
        if (!debugNativeObjects) {
            return;
        }

        lock.lock();
        try {
            --resourceCount;
            if (resourceCount == 0) {
                emptyResources.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void waitForNoResources() {
        if (!debugNativeObjects) {
            return;
        }

        lock.lock();
        try {
            long timeout = System.currentTimeMillis() + 30*1000;
            while (resourceCount != 0 && System.currentTimeMillis() < timeout) {
                emptyResources.await(1, TimeUnit.SECONDS);
            }
            if (resourceCount != 0) {
                throw new InterruptedException();
            }
        } catch (InterruptedException e) {
            /* Cause tests to fail without having to go add checked exceptions to every instance */
            throw new RuntimeException("Timeout waiting for resource count to drop to zero");
        } finally {
            lock.unlock();
        }
    }
}

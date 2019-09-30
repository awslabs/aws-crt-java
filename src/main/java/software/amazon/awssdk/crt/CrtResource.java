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
    private static final String NATIVE_DEBUG_PROPERTY_NAME = "aws.crt.debugnative";
    private static final long DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS = 30;
    private static final long NULL = 0;

    private static final ConcurrentHashMap<Long, String> NATIVE_RESOURCES = new ConcurrentHashMap<>();

    /*
     * Primarily intended for testing only.  Tracks the number of non-closed resources and signals
     * whenever a zero count is reached.
     */
    private static boolean debugNativeObjects = System.getProperty(NATIVE_DEBUG_PROPERTY_NAME) != null;
    private static int resourceCount = 0;
    private static final Lock lock = new ReentrantLock();
    private static final Condition emptyResources  = lock.newCondition();

    private final LinkedList<CrtResource> referencedResources = new LinkedList<>();
    private long nativeHandle;
    private AtomicInteger refCount = new AtomicInteger(1);

    static {
        /* This will cause the JNI lib to be loaded the first time a CRT is created */
        new CRT();
    }

    public CrtResource() {
    }

    /**
     * Marks a resource as referenced by this resource.  It is not safe to change the resource dependency graph after resource construction.
     * @param resource The referenced subresource
     * @return The original resource.
     */
    public <T extends CrtResource> T addReferenceTo(T resource) {
        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("Instance of class %s is adding a reference to instance of class %s", this.getClass().getCanonicalName(), resource.getClass().getCanonicalName()));
        }
        resource.addRef();
        referencedResources.push(resource);

        return resource;
    }

    /**
     * Takes ownership of a native object where the native pointer is tracked as a long.
     * @param handle pointer to the native object being acquired
     */
    protected void acquireNativeHandle(long handle) {
        if (!isNull()) {
            throw new IllegalStateException("Can't acquire >1 Native Pointer");
        }

        if (!isNativeResource()) {
            throw new IllegalStateException("Non-native resources cannot acquire a native pointer");
        }

        String canonicalName = this.getClass().getCanonicalName();

        if (handle == NULL) {
            throw new IllegalStateException("Can't acquire NULL Pointer: " + canonicalName);
        }

        String lastValue = NATIVE_RESOURCES.put(handle, canonicalName);

        if (lastValue != null) {
            throw new IllegalStateException("Acquired two CrtResources to the same Native Resource! Class: " + lastValue);
        }

        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("acquireNativeHandle - %s acquired native pointer %d", canonicalName, handle));
        }

        nativeHandle = handle;
        incrementNativeObjectCount();
    }

    /**
     * Begins the cleanup process associated with this native object and performs various debug-level bookkeeping operations.
     */
    private void release() {
        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("Releasing class %s", this.getClass().getCanonicalName()));
        }

        if (isNativeResource()) {
            if (isNull()) {
                throw new IllegalStateException("Already Released Resource!");
            }

            /*
             * Recyclable resources (like http connections) may be given to another Java object during the call to releaseNativeHandle.
             * By removing from the map first, we prevent a double-acquire exception from getting thrown when the second Java object
             * calls acquire on the native handle.
             */
            NATIVE_RESOURCES.remove(nativeHandle);
        }

        releaseNativeHandle();

        if (isNativeResource()) {
            decrementNativeObjectCount();

            nativeHandle = 0;
        }
    }

    /**
     * returns the native handle associated with this CRTResource.
     */
    public long getNativeHandle() {
        return nativeHandle;
    }

    /**
     * Increments the reference count to this resource.
     */
    void addRef() {
        refCount.incrementAndGet();
    }

    /**
     * Required override method that must begin the release process of the acquired native handle
     */
    protected abstract void releaseNativeHandle();

    /**
     * Override that determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources with asynchronous shutdown processes should override this with false, and establish a callback from native code that
     * invokes releaseReferences() when the asynchronous shutdown process has completed.  See HttpConnectionPoolManager for an example.
     */
    protected abstract boolean canReleaseReferencesImmediately();

    /**
     * Checks if this resource's native handle is NULL.  For always-null resources this is always true.  For all other
     * resources it means it has already been cleaned up or was not properly constructed.
     */
    public boolean isNull() {
        return (nativeHandle == NULL);
    }

    /*
     * An ugly and unfortunate necessity.  The CRTResource currently entangles two loosely-coupled concepts:
     *  (1) management of a native resource
     *  (2) referencing of other resources and the resulting implied cleanup process
     *
     * Some classes don't represent an actual native resource.  Instead, they just want to use
     * the reference and cleanup framework.  See CrtBufferPool for an example.
     *
     * A solution that avoids this complication while not changing the buffer/buffer pool properties dramatically would
     * be welcomed.
     */

    /**
     * Is this an actual native resource (true) or does it just track native resources and use the close/shutdown/referencing
     * aspects (false)?
     */
    protected boolean isNativeResource() { return true; }

    /**
     * Decrements the reference count to this resource.  If zero is reached, begins (and possibly completes) the resource's
     * cleanup process.
     */
    @Override
    public void close() {
        int remainingRefs = refCount.decrementAndGet();

        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("Closing instance of class %s with %d remaining refs", this.getClass().getCanonicalName(), remainingRefs));
        }

        if (remainingRefs > 0) {
            return;
        }

        release();

        if (canReleaseReferencesImmediately()) {
            releaseReferences();
        }
    }

    /**
     * Decrements the ref counts for all resources referenced by this resource.  Most resources will have this called
     * from their close() function, but resources with asynchronous shutdown processes must have it called from a
     * shutdown completion callback.
     */
    protected void releaseReferences() {
        if (debugNativeObjects) {
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("Instance of class %s closing referenced objects", this.getClass().getCanonicalName()));
        }

        while(referencedResources.size() > 0) {
            CrtResource r = referencedResources.pop();
            r.close();
        }
    }

    /**
     * Debug method to log all of the currently un-closed CRTResource objects.
     */
    public static void logNativeResources() {
        Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, "Dumping native object set:");
        for (Map.Entry<Long, String> entry : NATIVE_RESOURCES.entrySet()) {
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format(" * %s class instance using native pointer %d", entry.getValue(), entry.getKey().longValue()));
        }
    }

    /**
     * Debug method to increment the current native object count.
     */
    private static void incrementNativeObjectCount() {
        if (!debugNativeObjects) {
            return;
        }

        lock.lock();
        try {
            ++resourceCount;
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("incrementNativeObjectCount - count = %d", resourceCount));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Debug method to decrement the current native object count.
     */
    private static void decrementNativeObjectCount() {
        if (!debugNativeObjects) {
            return;
        }

        lock.lock();
        try {
            --resourceCount;
            Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("decrementNativeObjectCount - count = %d", resourceCount));
            if (resourceCount == 0) {
                emptyResources.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Debug/test method to wait for the CRTResource count to drop to zero.  Times out with an exception after
     * a period of waiting.
     */
    public static void waitForNoResources() {
        if (!debugNativeObjects) {
            return;
        }

        lock.lock();

        try {
            long timeout = System.currentTimeMillis() + DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS*1000;
            while (resourceCount != 0 && System.currentTimeMillis() < timeout) {
                emptyResources.await(1, TimeUnit.SECONDS);
            }

            if (resourceCount != 0) {
                Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtResource, "waitForNoResources - timeOut");
                logNativeResources();
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

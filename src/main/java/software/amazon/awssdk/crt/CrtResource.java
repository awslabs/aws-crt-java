/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;



/**
 * This wraps a native pointer and/or one or more references to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 */
public abstract class CrtResource implements AutoCloseable {
    private static final String NATIVE_DEBUG_PROPERTY_NAME = "aws.crt.debugnative";
    private static final int DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS = 60;
    private static final long NULL = 0;

    private static final Log.LogLevel ResourceLogLevel = Log.LogLevel.Debug;

    /**
     * Debug/diagnostic data about a CrtResource object
     */
    public class ResourceInstance {
        public long nativeHandle;
        public final String canonicalName;
        private Throwable instantiation;
        private CrtResource wrapper;

        public ResourceInstance(CrtResource wrapper, String name) {
            canonicalName = name;
            this.wrapper = wrapper;
            if (debugNativeObjects) {
                try {
                    throw new RuntimeException();
                } catch (RuntimeException ex) {
                    instantiation = ex;
                }
            }
        }

        public String location() {
            String str = "";
            if (debugNativeObjects) {
                StackTraceElement[] stack = instantiation.getStackTrace();

                // skip ctor and acquireNativeHandle()
                for (int frameIdx = 2; frameIdx < stack.length; ++frameIdx) {
                    StackTraceElement frame = stack[frameIdx];
                    str += frame.toString() + "\n";
                }
            }
            return str;
        }

        @Override
        public String toString() {
            String str = canonicalName + " allocated at:\n";
            str += location();
            return str;
        }

        public CrtResource getWrapper() { return wrapper; }

        public void setNativeHandle(long handle) { nativeHandle = handle; }
    }

    private static final HashMap<Long, ResourceInstance> CRT_RESOURCES = new HashMap<>();

    /*
     * Primarily intended for testing only.  Tracks the number of non-closed resources and signals
     * whenever a zero count is reached.
     */
    private static boolean debugNativeObjects = System.getProperty(NATIVE_DEBUG_PROPERTY_NAME) != null;
    private static int resourceCount = 0;
    private static final Lock lock = new ReentrantLock();
    private static final Condition emptyResources  = lock.newCondition();
    private static final AtomicLong nextId = new AtomicLong(0);

    private final ArrayList<CrtResource> referencedResources = new ArrayList<>();
    private long nativeHandle;
    private AtomicInteger refCount = new AtomicInteger(1);
    private long id = nextId.getAndAdd(1);
    private Instant creationTime = Instant.now();
    private String description;

    static {
        /* This will cause the JNI lib to be loaded the first time a CRT is created */
        new CRT();
    }

    /**
     * Default constructor
     */
    public CrtResource() {
        if (debugNativeObjects) {
            String canonicalName = this.getClass().getCanonicalName();

            synchronized(CrtResource.class) {
                CRT_RESOURCES.put(id, new ResourceInstance(this, canonicalName));
            }

            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("CrtResource of class %s(%d) created", this.getClass().getCanonicalName(), id));
        }
    }

    /**
     * Marks a resource as referenced by this resource.
     * @param resource The resource to add a reference to
     */
    public void addReferenceTo(CrtResource resource) {
        resource.addRef();
        synchronized(this) {
            referencedResources.add(resource);
        }

        if (debugNativeObjects) {
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("Instance of class %s(%d) is adding a reference to instance of class %s(%d)", this.getClass().getCanonicalName(), id, resource.getClass().getCanonicalName(), resource.id));
        }
    }

    /**
     * Removes a reference from this resource to another.
     * @param resource The resource to remove a reference to
     */
    public void removeReferenceTo(CrtResource resource) {
        boolean removed = false;
        synchronized(this) {
            removed = referencedResources.remove(resource);
        }

        if (debugNativeObjects) {
            if (removed) {
                Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("Instance of class %s(%d) is removing a reference to instance of class %s(%d)", this.getClass().getCanonicalName(), id, resource.getClass().getCanonicalName(), resource.id));
            } else {
                Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("Instance of class %s(%d) erroneously tried to remove a reference to instance of class %s(%d) that it was not referencing", this.getClass().getCanonicalName(), id, resource.getClass().getCanonicalName(), resource.id));
            }
        }

        if (!removed) {
            return;
        }

        resource.decRef();
    }

    /**
     * Swaps a reference from one resource to another
     * @param oldReference resource to stop referencing
     * @param newReference resource to start referencing
     */
    protected void swapReferenceTo(CrtResource oldReference, CrtResource newReference) {
        if (oldReference != newReference) {
            if (newReference != null) {
                addReferenceTo(newReference);
            }
            if (oldReference != null) {
                removeReferenceTo(oldReference);
            }
        }
    }

    /**
     * Takes ownership of a native object where the native pointer is tracked as a long.
     * @param handle pointer to the native object being acquired
     */
    protected void acquireNativeHandle(long handle) {
        if (!isNull()) {
            throw new IllegalStateException("Can't acquire >1 Native Pointer");
        }

        String canonicalName = this.getClass().getCanonicalName();

        if (handle == NULL) {
            throw new IllegalStateException("Can't acquire NULL Pointer: " + canonicalName);
        }

        if (debugNativeObjects) {
            synchronized(CrtResource.class) {
                ResourceInstance instance = CRT_RESOURCES.get(id);
                if (instance != null) {
                    instance.setNativeHandle(handle);
                }
            }
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("acquireNativeHandle - %s(%d) acquired native pointer %d", canonicalName, id, handle));
        }

        nativeHandle = handle;
        incrementNativeObjectCount();
    }

    /**
     * Begins the cleanup process associated with this native object and performs various debug-level bookkeeping operations.
     */
    private void release() {
        if (debugNativeObjects) {
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("Releasing class %s(%d)", this.getClass().getCanonicalName(), id));

            synchronized(CrtResource.class) {
                CRT_RESOURCES.remove(id);
            }
        }

        releaseNativeHandle();

        if (nativeHandle != 0) {
            decrementNativeObjectCount();

            nativeHandle = 0;
        }
    }

    /**
     * returns the native handle associated with this CRTResource.
     * @return native address
     */
    public long getNativeHandle() {
        if(nativeHandle == NULL) {
            throw new IllegalStateException("Native handle is NULL");
        }
        return nativeHandle;
    }

    /**
     * Increments the reference count to this resource.
     */
    public void addRef() {
        refCount.incrementAndGet();
    }

    /**
     * Required override method that must begin the release process of the acquired native handle
     */
    protected abstract void releaseNativeHandle();

    /**
     * Override that determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources with asynchronous shutdown processes should override this with false, and establish a callback from native code that
     * invokes releaseReferences() when the asynchronous shutdown process has completed.  See HttpClientConnectionManager for an example.
     * @return true if this resource releases synchronously, false if this resource performs async shutdown
     */
    protected abstract boolean canReleaseReferencesImmediately();

    /**
     * Checks if this resource's native handle is NULL.  For always-null resources this is always true.  For all other
     * resources it means it has already been cleaned up or was not properly constructed.
     * @return true if no native resource is bound, false otherwise
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
     * the reference and cleanup framework.  See AwsIotMqttConnectionBuilder.java for example.
     *
     */

    @Override
    public void close() {
        decRef();
    }

    /**
     * Decrements the reference count to this resource.  If zero is reached, begins (and possibly completes) the resource's
     * cleanup process.
     */
    public void decRef() {
        int remainingRefs = refCount.decrementAndGet();

        if (debugNativeObjects) {
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("Closing instance of class %s(%d) with %d remaining refs", this.getClass().getCanonicalName(), id, remainingRefs));
        }

        if (remainingRefs != 0) {
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
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("Instance of class %s(%d) closing all referenced objects", this.getClass().getCanonicalName(), id));
        }

        synchronized(this) {
            for (CrtResource resource : referencedResources) {
                resource.decRef();
            }

            referencedResources.clear();
        }
    }

    /**
     * Sets a custom logging description for this resource
     * @param description custom resource description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets a debug/diagnostic string describing this resource and its reference state
     * @return resource diagnostic string
     */
    public String getResourceLogDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("[Id %d, Class %s, Refs %d](%s) - %s", id, getClass().getSimpleName(), refCount.get(), creationTime.toString(), description != null ? description : "<null>"));
        synchronized(this) {
            if (referencedResources.size() > 0) {
                builder.append("\n   Forward references by Id: ");
                for (CrtResource reference : referencedResources) {
                    builder.append(String.format("%d ", reference.id));
                }
            }
        }

        return builder.toString();
    }

    /**
     * Applies a resource description consuming functor to all CRTResource objects
     * @param fn function to apply to each resource description
     */
    public static void collectNativeResources(Consumer<String> fn) {
        collectNativeResource((ResourceInstance resource) -> {
            String str = String.format(" * Address: %d: %s", resource.nativeHandle,
                    resource.toString());
            fn.accept(str);
        });
    }

    /**
     * Applies a generic diagnostic-gathering functor to all CRTResource objects
     * @param fn function to apply to each outstanding Crt resource
     */
    public static void collectNativeResource(Consumer<ResourceInstance> fn) {
        synchronized(CrtResource.class) {
            for (Map.Entry<Long, ResourceInstance> entry : CRT_RESOURCES.entrySet()) {
                fn.accept(entry.getValue());
            }
        }
    }

    /**
     * Debug method to log all of the currently un-closed CRTResource objects.
     */
    public static void logNativeResources() {
        Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, "Dumping native object set:");
        collectNativeResource((resource) -> {
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, resource.getWrapper().getResourceLogDescription());
        });
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
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("incrementNativeObjectCount - count = %d", resourceCount));
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
            Log.log(ResourceLogLevel, Log.LogSubject.JavaCrtResource, String.format("decrementNativeObjectCount - count = %d", resourceCount));
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
        ClientBootstrap.closeStaticDefault();
        EventLoopGroup.closeStaticDefault();
        HostResolver.closeStaticDefault();

        if (debugNativeObjects) {
            lock.lock();

            try {
                long timeout = System.currentTimeMillis() + DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS * 1000;
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

        waitForGlobalResourceDestruction(DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS);
    }

    private static native void waitForGlobalResourceDestruction(int timeoutInSeconds);
}

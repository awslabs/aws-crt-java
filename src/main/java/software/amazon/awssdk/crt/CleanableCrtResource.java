/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * This wraps a native pointer to an AWS Common Runtime resource.
 */
public abstract class CleanableCrtResource implements AutoCloseable {
    private static final String NATIVE_DEBUG_PROPERTY_NAME = "aws.crt.debugnative";
    private static final int DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS = 60;

    private static Janitor RESOURCE_JANITOR = new Janitor();

    private static final HashMap<Long, NativeHandleWrapper> CRT_RESOURCES = new HashMap<>();

    /*
     * Primarily intended for testing only.  Tracks the number of non-closed resources and signals
     * whenever a zero count is reached.
     */
    private static boolean debugNativeObjects = System.getProperty(NATIVE_DEBUG_PROPERTY_NAME) != null;
    private static int resourceCount = 0;
    private static final Lock lock = new ReentrantLock();
    private static final Condition emptyResources  = lock.newCondition();
    private static final AtomicLong nextId = new AtomicLong(0);

    public static class NativeHandleWrapper {
        private boolean released = false;
        private long handle;
        private Consumer<Long> releaser;
        private long id;
        private String canonicalName;
        private Throwable instantiation;

        public NativeHandleWrapper(long handle, String canonicalName, long id, Consumer<Long> releaser) {
            this.id = id;
            this.releaser = releaser;
            this.handle = handle;
            this.canonicalName = canonicalName;

            if (debugNativeObjects) {
                try {
                    throw new RuntimeException();
                } catch (RuntimeException ex) {
                    instantiation = ex;
                }
            }
        }

        public long getNativeHandle() { return handle; }

        public void release() {
            synchronized(this) {
                if (released) {
                    return;
                }

                released = true;
            }

            releaser.accept(handle);

            CleanableCrtResource.removeResource(id);
        }

        private String getCreationCallstack() {
            StringBuilder creationCallstackDescription = new StringBuilder();
            if (debugNativeObjects) {
                StackTraceElement[] stack = instantiation.getStackTrace();

                // skip ctor and acquireNativeHandle()
                for (int frameIdx = 2; frameIdx < stack.length; ++frameIdx) {
                    StackTraceElement frame = stack[frameIdx];
                    creationCallstackDescription.append(frame.toString());
                    creationCallstackDescription.append("\n");
                }
            } else {
                creationCallstackDescription.append("<Unknown>(debug native not enabled)");
            }

            return creationCallstackDescription.toString();
        }

        @Override
        public String toString() {
            return String.format("Instance of class %s with id %d allocated at:\n%s", canonicalName, id, getCreationCallstack());
        }
    }

    private NativeHandleWrapper nativeHandle;
    private Janitor.Mess mess;

    public CleanableCrtResource() {
    }

    /**
     * Takes ownership of a native object where the native pointer is tracked as a long.
     * @param handle pointer to the native object being acquired
     */
    protected void acquireNativeHandle(long handle, Consumer<Long> releaser) {
        if (!isNull()) {
            throw new IllegalStateException("Can't acquire >1 Native Pointer");
        }

        String canonicalName = this.getClass().getCanonicalName();

        if (handle == 0) {
            throw new IllegalStateException("Can't acquire NULL Pointer: " + canonicalName);
        }

        Log.log(Log.LogLevel.Trace, Log.LogSubject.JavaCrtResource, String.format("acquireNativeHandle - %s acquired native pointer %d", canonicalName, handle));

        long id = nextId.getAndAdd(1);
        nativeHandle = new NativeHandleWrapper(handle, canonicalName, id, releaser);

        mess = RESOURCE_JANITOR.register(this, nativeHandle::release);

        addResource(id, nativeHandle);
    }

    /**
     * returns the native handle associated with this CRTResource.
     */
    public long getNativeHandle() {
        return nativeHandle.getNativeHandle();
    }

    /**
     * Checks if this resource's native handle is NULL.
     */
    public boolean isNull() {
        return (nativeHandle == null);
    }

    @Override
    public void close() {
        ;
    }

    static void removeResource(long id) {
        if (!debugNativeObjects) {
            return;
        }

        NativeHandleWrapper wrapper = null;

        lock.lock();
        try {
            wrapper = CRT_RESOURCES.remove(id);
            --resourceCount;
            if (resourceCount == 0) {
                emptyResources.signal();
            }
        } finally {
            lock.unlock();
        }

        if (wrapper != null) {
            Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtResource, String.format("CleanableCrtResource removed:\n%s", wrapper.toString()));
        } else {
            Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtResource, String.format("CleanableCrtResource of with id %d removed", id));
        }
    }

    /**
     * Debug method to increment the current native object count.
     */
    private static void addResource(long id, NativeHandleWrapper wrapper) {
        if (!debugNativeObjects) {
            return;
        }

        String logRecord = String.format("CleanableCrtResource instance created:\n%s", wrapper.toString());
        Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtResource, logRecord);

        lock.lock();
        try {
            CRT_RESOURCES.put(id, wrapper);
            ++resourceCount;
            Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtResource, String.format("CleanableCrtResource count now %d", resourceCount));
        } finally {
            lock.unlock();
        }
    }

    private static void debugApplyGCPressure() {
        for(int i = 0; i < 1000; ++i) {
            int[] anArray = new int[100000];
            for(int j = 0; j < 100000; ++j) {
                anArray[j] = j;
            }
        }
        System.gc();
    }

    private static void logNativeResources() {
        Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtResource, "Dumping native object set:");
        for (HashMap.Entry<Long, NativeHandleWrapper> entry : CRT_RESOURCES.entrySet()) {
            Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtResource, entry.getValue().toString());
        }
    }

    public static void debugWaitForNoResources() {
        /* TODO: change these to assign nulls internally */
        ClientBootstrap.closeStaticDefault();
        EventLoopGroup.closeStaticDefault();
        HostResolver.closeStaticDefault();

        debugApplyGCPressure();

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

        RESOURCE_JANITOR = null;

        waitForGlobalResourceDestruction(DEBUG_CLEANUP_WAIT_TIME_IN_SECONDS);
    }

    private static native void waitForGlobalResourceDestruction(int timeoutInSeconds);
}
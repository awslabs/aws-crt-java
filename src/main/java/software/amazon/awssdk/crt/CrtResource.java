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

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import software.amazon.awssdk.crt.Log;

/**
 * This wraps a native pointer to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 */
public abstract class CrtResource implements AutoCloseable {
    private static final long CLEANUP_TIMEOUT_MILLIS = 300 * 1000;

    private static final Janitor RESOURCE_JANITOR = new Janitor();

    public static class NativeHandleWrapper {
        private boolean released = false;
        private long handle;
        private Consumer<Long> releaser;

        public NativeHandleWrapper(long handle, Consumer<Long> releaser) {
            this.releaser = releaser;
            this.handle = handle;
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
        }
    }

    private NativeHandleWrapper nativeHandle;
    private Janitor.Mess mess;

    static {
        /* This will cause the JNI lib to be loaded the first time a CRT is created */
        new CRT();
    }

    public CrtResource() {
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

        NativeHandleWrapper wrapper = new NativeHandleWrapper(handle, releaser);

        mess = RESOURCE_JANITOR.register(this, wrapper::release);

        this.nativeHandle = wrapper;
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

    public static void waitForNoResources() {
        try {
            RESOURCE_JANITOR.waitForNoReferences(CLEANUP_TIMEOUT_MILLIS);
        } catch (InterruptedException ex) {
            throw new CrtRuntimeException("Cleanup timeout");
        }
    }

}

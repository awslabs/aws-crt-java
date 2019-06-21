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
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This wraps a native pointer to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 */
public class CrtResource implements AutoCloseable {
    private static final ConcurrentHashMap<Long, String> NATIVE_RESOURCES = new ConcurrentHashMap<>();
    private static final long NULL = 0;
    private final LinkedList<CrtResource> ownedSubResources = new LinkedList<>();
    private long ptr;

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
     * Marks a SubResource as owned by this Resource. This will cause the SubResource to be closed after this Resource
     * is closed.
     * @param subresource The owned subresource
     * @return The original subresource.
     */
    public <T extends CrtResource> T own(T subresource) {
        if (!isNull()) {
            throw new IllegalStateException("Parent Resource already created and acquired, can't add sub-resources after the fact.");
        }
        ownedSubResources.push(subresource);
        return subresource;
    }

    protected void acquire(long _ptr) {
        if (!isNull()) {
            throw new IllegalStateException("Can't acquire >1 Native Pointer");
        }

        if (_ptr == NULL) {
            throw new IllegalStateException("Can't acquire NULL Pointer");
        }

        String lastValue = NATIVE_RESOURCES.put(_ptr, this.getClass().getCanonicalName());

        if (lastValue != null) {
            throw new IllegalStateException("Acquired two CrtResources to the same Native Resource! Class: " + lastValue);
        }

        ptr = _ptr;
    }
    
    protected long release() {
        if (isNull()) {
            throw new IllegalStateException("Already Released Resource!");
        }
        NATIVE_RESOURCES.remove(ptr);
        long addr = ptr;
        ptr = 0;
        return addr;
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

        while(ownedSubResources.size() > 0) {
            CrtResource r = ownedSubResources.pop();
            r.close();
        }
    }
}

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
import java.util.concurrent.ConcurrentHashMap;

/**
 * This wraps a native pointer to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 */
public class CrtResource {
    private static final ConcurrentHashMap<Long, String> NATIVE_RESOURCES = new ConcurrentHashMap<>();
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

    protected void acquire(long _ptr) {
        NATIVE_RESOURCES.put(_ptr, this.getClass().getCanonicalName());
        ptr = _ptr;
    }
    
    protected long release() {
        NATIVE_RESOURCES.remove(ptr);
        long addr = ptr;
        ptr = 0;
        return addr;
    }

    public long native_ptr() {
        return ptr;
    }
}

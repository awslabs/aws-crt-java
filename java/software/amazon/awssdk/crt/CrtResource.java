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

/*******************************************************************************
 * This wraps a native pointer to an AWS Common Runtime resource. It also ensures
 * that the first time a resource is referenced, the CRT will be loaded and bound.
 ******************************************************************************/
public class CrtResource {
    private long ptr;

    static {
        /* This will cause the JNI lib to be loaded the first time a CRT is created */
        new CRT();
    }

    public CrtResource() {
    }

    protected void acquire(long _ptr) {
        ptr = _ptr;
    }
    
    protected void release() {
        ptr = 0;
    }

    public long native_ptr() {
        return ptr;
    }
}
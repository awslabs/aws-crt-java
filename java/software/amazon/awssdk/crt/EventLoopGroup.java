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

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;

public final class EventLoopGroup implements AutoCloseable {
    private long elg = 0;

    static {
        new CRT();
    }

    public EventLoopGroup(int numThreads) throws CrtRuntimeException {
        elg = event_loop_group_new(numThreads);
    }

    @Override
    public void close() {
        if (elg != 0) {
            event_loop_group_clean_up(elg);
            elg = 0;
        }
    }

    public long native_ptr() { 
        return elg;
    }

    private static native long event_loop_group_new(int numThreads) throws CrtRuntimeException;
    private static native void event_loop_group_clean_up(long elg);
};

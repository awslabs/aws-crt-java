/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

/**
 * The data returned when OnStopped is invoked in the LifecycleEvents callback.
 * Currently empty, but may be used in the future for passing additional data.
 */
public class OnStoppedReturn {
    /**
     * This is only called in JNI to make a new OnStoppedReturn.
     */
    private OnStoppedReturn() {}
}

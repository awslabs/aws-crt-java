/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

/**
 * Functor interface for receiving message flush events.
 */
public interface MessageFlushCallback {
    /**
     * Invoked when a message has been flushed to the underlying transport mechanism.
     * @param errorCode If this is 0, the message was successfully written. Otherwise,
     *                  errorCode represents the reason the message flush failed.
     */
    void onCallbackInvoked(int errorCode);
}

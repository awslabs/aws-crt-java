/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

/**
 * Interface for handling private key operations during the TLS handshake.
 */
public interface TlsKeyOperationHandler {

    /**
     * Invoked each time a private key operation needs to be performed.
     *
     * You MUST call either operation.complete(output) or
     * operation.completeExceptionally(exception) or the TLS connection will hang
     * forever.
     *
     * You may complete the operation synchronously, or async. You may complete the
     * operation on any thread.
     *
     * The function is always invoked from an IO event-loop thread. Therefore you
     * MUST NOT perform an async call and wait for it in a blocking way from within
     * this function. Such behavior is likely to deadlock your program.
     */
    void performOperation(TlsKeyOperation operation);
}

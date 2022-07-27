/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;

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
    *
    * Additionally, this may be called from multiple times from multiple threads
    * at once, so keep this in mind if using a private key operation that has to
    * be single-threaded and synchronously called.
    *
    * @param operation The operation to be acted on
    */
    void performOperation(TlsKeyOperation operation);
}

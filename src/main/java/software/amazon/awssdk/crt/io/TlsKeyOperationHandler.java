/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;

// TODO: Document
public class TlsKeyOperationHandler {

    // The interface to call when the TLS context handler gets an operation event. Will call the corresponding function
    // in the interface based on the data received. See TlsKeyOperationHandlerEvents for details.
    private TlsKeyOperationHandlerEvents operationHandlerEvents;

    // A pointer to the native C object associated with this class. If set to null, then no C object is associated.
    // Is used internally.
    private Long nativeHandle;

    /**
     * Returns the operation handler events associated with this TlsKeyOperationHandler.
     */
    public TlsKeyOperationHandlerEvents getOperationHandlerEvents() {
        return this.operationHandlerEvents;
    }

    public TlsKeyOperationHandler(TlsKeyOperationHandlerEvents operationHandlerEvents) {
        this.operationHandlerEvents = operationHandlerEvents;
    }

    /**
     * Calls operationHandlerEvents.performOperation, passing hte TlsKeyOperation from the tls context handler.
     * This function is called in JNI native code.
     *
     * If there is no operationHandlerEvents assigned, it will complete the operation with an exception.
     *
     * It will also catch any exceptions in the TlsKeyOperationHandlerEvents.performOperation function and complete
     * the TlsKeyOperation with the caught exception.
     */
    protected void invokePerformOperation(TlsKeyOperation operation)
    {
        // If an exception occurs for any reason, catch it and complete the operation with an exception.
        try {
            this.operationHandlerEvents.performOperation(operation);
        } catch (Exception ex) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "Exception occured while performing TlsKeyOperation: " + ex.toString());
            operation.completeExceptionally(ex);
        }
    }

    /**
    * Interface for handling private key operations during the TLS handshake.
    */
    public interface TlsKeyOperationHandlerEvents {

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

        /**
         * TODO - add a function that is called when the entire thing is destroyed, so
         * the customer can tear down whatever as needed.
         */
    }

}

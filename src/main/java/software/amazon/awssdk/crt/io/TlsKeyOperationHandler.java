/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;

/**
 * This class wraps the aws_custom_key_op_handler from aws-c-io to provide
 * the ability to perform custom private key operations during the MQTT TLS handshake.
 *
 * This is necessary if you require an external library to handle private operations
 * such as signing and decrypting.
 */
public class TlsKeyOperationHandler {

    /**
     * The interface to call when the TLS context handler gets an operation event. Will call the corresponding function
     * in the interface based on the data received. See TlsKeyOperationHandlerEvents for details.
     */
    private TlsKeyOperationHandlerEvents operationHandlerEvents;

    /**
     * A pointer to the native C object associated with this class. If set to null, then no C object is associated.
     * Is used internally.
     */
    private long nativeHandle;

    /**
     * Returns the operation handler events associated with this TlsKeyOperationHandler.
     *
     * @return The operation handler events associated with this TlsKeyOperationHandler
     */
    public TlsKeyOperationHandlerEvents getOperationHandlerEvents() {
        return this.operationHandlerEvents;
    }

    /**
     * Creates a new TlsKeyOperationHandler with the given TlsKeyOperationHandlerEvents
     *
     * @param operationHandlerEvents The TlsKeyOperationHandlerEvents to use
     */
    public TlsKeyOperationHandler(TlsKeyOperationHandlerEvents operationHandlerEvents) {
        this.operationHandlerEvents = operationHandlerEvents;
    }

    /**
     * Calls operationHandlerEvents.performOperation, passing the TlsKeyOperation from the tls context handler.
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
     * Calls operationHandlerEvents.onCleanup, allowing the interface to clean up.
     * This function is called in JNI native code right before cleaning up the native class.
     */
    protected void invokeOnCleanup()
    {
        this.operationHandlerEvents.onCleanup();
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
        *
        * Additionally, this may be called from multiple times from multiple threads
        * at once, so keep this in mind if using a private key operation that has to
        * be single-threaded and synchronously called.
        *
        * @param operation The operation to be acted on
        */
        void performOperation(TlsKeyOperation operation);

        /**
         * Invoked when the TlsKeyOperationHandler is no longer used in native code
         * and is about to be cleaned up. If you have anything to clean up, close, or
         * otherwise tear down for the interface, this is the place to do it.
         *
         * This will only be called once right before destroying the
         * TlsKeyOperationHandler fully.
         */
        void onCleanup();
    }

}

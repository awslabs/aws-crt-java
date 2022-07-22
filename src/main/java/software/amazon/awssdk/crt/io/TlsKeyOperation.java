/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;

import software.amazon.awssdk.crt.CrtResource;

/**
 * A class containing a TLS Private Key operation that needs to be performed.
 * This class is passed to TlsKeyOperationHandler if a custom key operation is set
 * in the MQTT client.
 *
 * You MUST call either complete(output) or completeExceptionally(exception)
 * or the TLS connection will hang forever!
 *
 * You do NOT need to call close on this CrtResource - it will be called automatically
 * when you call either complete(output) or completeExceptionally(exception)
 */
public final class TlsKeyOperation extends CrtResource {

    /**
     * The type of TlsKeyOperation that needs to be performed by the TlsKeyOperationHandlerEvents
     * interface in the TlsKeyOperationHandler.
     */
    public enum Type {
        SIGN(1), DECRYPT(2);

        static Map<Integer, Type> buildEnumMapping() {
            Map<Integer, Type> enumMapping = new HashMap<Integer, Type>();
            for (Type i : Type.values()) {
                enumMapping.put(i.nativeValue, i);
            }
            return enumMapping;
        }

        public static Type getEnumValueFromInteger(int value) {
            Type enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal TlsKeyOperation.Type");
        }

        Type(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() {
            return nativeValue;
        }

        int nativeValue;
        static Map<Integer, Type> enumMapping = buildEnumMapping();
    }

    private boolean closeCalled;
    private boolean clearCalled;
    private byte[] inputData;
    private Type operationType;
    private TlsSignatureAlgorithm signatureAlgorithm;
    private TlsHashAlgorithm digestAlgorithm;

    /* Called from native when there's a new operation to be performed. Creates a new TlsKeyOperation */
    protected TlsKeyOperation(long nativeHandle, byte[] inputData, int operationType, int signatureAlgorithm,
            int digestAlgorithm) {

        acquireNativeHandle(nativeHandle);

        this.closeCalled = false;
        this.clearCalled = false;
        this.inputData = inputData;
        this.operationType = Type.getEnumValueFromInteger(operationType);

        /*
         * signatureAlgorithm and digestAlgorithm may not be used by the operation. In
         * native we use enum value UNKNOWN(0) to indicate this, but in Java we'll use
         * null ot indicate this.
         */
        if (signatureAlgorithm != 0) {
            this.signatureAlgorithm = TlsSignatureAlgorithm.getEnumValueFromInteger(signatureAlgorithm);
        }

        if (digestAlgorithm != 0) {
            this.digestAlgorithm = TlsHashAlgorithm.getEnumValueFromInteger(digestAlgorithm);
        }
    }

    /**
     * Returns the input data from native that needs to be operated on using the private key.
     * You can determine the operation that needs to be performed on the data using the getType function.
     *
     * @return The input data from native that needs to be operated on
     */
    public byte[] getInput() {
        return inputData;
    }

    /**
     * Returns the operation that needs to be performed.
     *
     * @return The operation that needs to be performed.
     */
    public Type getType() {
        return operationType;
    }

    /**
     * Returns the TLS algorithm used in the signature.
     *
     * @return The TLS algorithm used in the signature
     */
    public TlsSignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * Returns the TLS Hash algorithm used in the digest.
     *
     * @return The TLS Hash algorithm used in the digest
     */
    public TlsHashAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * The function to call when you have modified the input data using the private key and are ready to
     * return it for use in the MQTT TLS Handshake.
     *
     * @param output The modified input data that has been modified by the custom key operation
     */
    public void complete(byte[] output) {
        if (getNativeHandle() == 0 || this.clearCalled == true) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "No native handle set in TlsKeyOperation! Cannot complete operation");
            return;
        }

        tlsKeyOperationComplete(getNativeHandle(), output);
        clear();
    }

    /**
     * The function to call when you either have an exception and want to complete the operation with an
     * exception or you cannot complete the operation. This will mark the operation as complete with an
     * exception so it can be reacted to accordingly.
     *
     * @param ex The exeception to complete with
     */
    public void completeExceptionally(Throwable ex) {
        if (getNativeHandle() == 0 || this.clearCalled == true) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "No native handle set in TlsKeyOperation! Cannot complete operation exceptionally");
            return;
        }

        tlsKeyOperationCompleteExceptionally(getNativeHandle(), ex);
        clear();
    }

    /**
     * Clears the data and makes the object no longer valid for use
     */
    private void clear() {
        if (this.closeCalled == false) {
            /* Indicates to the code that clear has been called but close has not yet, which will allow
               the code to call close once, and only once. */
            this.clearCalled = true;

            this.inputData = null; // the DirectByteBuffer backing this is no longer valid
            close(); // the operation backing this is no longer valid

            /* Indicates that close has now been called, so any further attempts to call it are unncessary */
            this.closeCalled = true;
        }
        else {
            Log.log(LogLevel.Warn, LogSubject.CommonGeneral,
                "TlsKeyOperation cannot be cleared: clear() already called!");
        }
    }

    @Override
    public void close() {
        // This should only occur if clear is called for the first time
        if (this.closeCalled == false && this.clearCalled == true) {
            decRef();
            return;
        }
        else if (this.closeCalled == true) {
            // Trying to close after calling complete
            Log.log(LogLevel.Warn, LogSubject.CommonGeneral,
                "TlsKeyOperation cannot be closed: TlsKeyOperation already closed as part of calling complete() or completeExceptionally()");
        }
        else {
            // Trying to close before calling complete
            Log.log(LogLevel.Warn, LogSubject.CommonGeneral,
                "TlsKeyOperation cannot be closed: complete() or completeExceptionally() has to be called!");
        }
    }

    /**
     * Frees all native resources associated with the context. This object is unusable after close is called.
     */
    protected void releaseNativeHandle() {
        if (this.clearCalled == false) {
            completeExceptionally(new RuntimeException("releaseNativeHandle called!"));
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    protected boolean canReleaseReferencesImmediately() {
        return false;
    };

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void tlsKeyOperationComplete(long nativeHandle, byte[] output);
    private static native void tlsKeyOperationCompleteExceptionally(long nativeHandle, Throwable ex);
}


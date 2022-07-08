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

/* TODO: should this be a CrtResource? (how to handle close() vs complete())
 *       if we do make it a CrtResource close() needs to become idempotent,
 *       currently it can make the refcount go negative and crazy things happen */

/**
 * A class containing a TLS Private Key operation that needs to be performed.
 * This class is passed to TlsKeyOperationHandler if a custom key operation is set
 * in the MQTT client.
 *
 * You MUST call either complete(output) or completeExceptionally(exception)
 * or the TLS connection will hang forever.
 */
public final class TlsKeyOperation {

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

    private long nativeHandle;
    private byte[] inputData;
    private Type operationType;
    private TlsSignatureAlgorithm signatureAlgorithm;
    private TlsHashAlgorithm digestAlgorithm;

    /* Called from native when there's a new operation to be performed. Creates a new TlsKeyOperation */
    protected TlsKeyOperation(long nativeHandle, byte[] inputData, int operationType, int signatureAlgorithm,
            int digestAlgorithm) {

        this.nativeHandle = nativeHandle;
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
     */
    public byte[] getInput() {
        return inputData;
    }

    /**
     * Returns the operation that needs to be performed.
     */
    public Type getType() {
        return operationType;
    }

    /**
     * Returns the TLS algorithm used in the signature.
     */
    public TlsSignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * Returns the TLS Hash algorithm used in the digest.
     */
    public TlsHashAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    /**
     * The function to call when you have modified the input data using the private key and are ready to
     * return it for use in the MQTT TLS Handshake.
     */
    public void complete(byte[] output) {
        if (nativeHandle == 0) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "No native handle set in TlsKeyOperation! Cannot complete operation");
            return;
        }

        tlsKeyOperationComplete(nativeHandle, output);
        clear();
    }

    /**
     * The function to call when you either have an exception and want to complete the operation with an
     * exception or you cannot complete the operation. This will mark the operation as complete with an
     * exception so it can be reacted to accordingly.
     */
    public void completeExceptionally(Throwable ex) {
        if (nativeHandle == 0) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "No native handle set in TlsKeyOperation! Cannot complete operation exceptionally");
            return;
        }

        tlsKeyOperationCompleteExceptionally(nativeHandle, ex);
        clear();
    }

    /**
     * Clears the data and makes the object no longer valid for use
     */
    private void clear() {
        this.nativeHandle = 0; // the operation backing this is no longer valid
        this.inputData = null; // the DirectByteBuffer backing this is no longer valid
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void tlsKeyOperationComplete(long nativeHandle, byte[] output);
    private static native void tlsKeyOperationCompleteExceptionally(long nativeHandle, Throwable ex);
}


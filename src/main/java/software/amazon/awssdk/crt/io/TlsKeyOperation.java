/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.crt.CrtResource;

/* TODO: should this be a CrtResource? (how to handle close() vs complete())
 *       if we do make it a CrtResource close() needs to become idempotent,
 *       currently it can make the refcount go negative and crazy things happen */

public final class TlsKeyOperation {

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

    long nativeHandle;
    byte[] inputData;
    Type operationType;
    TlsSignatureAlgorithm signatureAlgorithm;
    TlsHashAlgorithm digestAlgorithm;

    /* called from native when there's a new operation to be performed */
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

    public byte[] getInput() {
        return inputData;
    }

    public Type getType() {
        return operationType;
    }

    public TlsSignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public TlsHashAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void complete(byte[] output) {
        if (nativeHandle == 0) {
            return; /* TODO: log? */
        }

        tlsKeyOperationComplete(nativeHandle, output);
        clear();
    }

    public void completeExceptionally(Throwable ex) {
        if (nativeHandle == 0) {
            return; /* TODO: log? */
        }

        tlsKeyOperationCompleteExceptionally(nativeHandle, ex);
        clear();
    }

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


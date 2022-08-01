/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.HashMap;
import java.util.Map;
import java.io.StringWriter;
import java.io.PrintWriter;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;

/**
 * A class containing a mutual TLS (mTLS) Private Key operation that needs to be performed.
 * This class is passed to TlsKeyOperationHandler if a custom key operation is set.
 *
 * You MUST call either complete(output) or completeExceptionally(exception)
 * or the TLS connection will hang forever!
 */
public final class TlsKeyOperation {

    /**
     * The type of TlsKeyOperation that needs to be performed by the TlsKeyOperationHandler interface.
     */
    public enum Type {
        UNKNOWN(0), SIGN(1), DECRYPT(2);

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

    private CrtResourceInternal nativeResource;
    private byte[] inputData;
    private Type operationType;
    private TlsSignatureAlgorithm signatureAlgorithm;
    private TlsHashAlgorithm digestAlgorithm;

    /* Called from native when there's a new operation to be performed. Creates a new TlsKeyOperation */
    protected TlsKeyOperation(long nativeHandle, byte[] inputData, int operationType, int signatureAlgorithm,
            int digestAlgorithm) {

        nativeResource = new CrtResourceInternal(nativeHandle);
        this.inputData = inputData;
        this.operationType = Type.getEnumValueFromInteger(operationType);
        this.signatureAlgorithm = TlsSignatureAlgorithm.getEnumValueFromInteger(signatureAlgorithm);
        this.digestAlgorithm = TlsHashAlgorithm.getEnumValueFromInteger(digestAlgorithm);
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
     * return it for use in the mutual TLS Handshake.
     *
     * @param output The modified input data that has been modified by the custom key operation
     */
    public synchronized void complete(byte[] output) {
        if (nativeResource.isNull()) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "No native handle set in TlsKeyOperation! Cannot complete operation");
            return;
        }

        tlsKeyOperationComplete(nativeResource.getNativeHandle(), output);
        nativeResource.close();
    }

    /**
     * The function to call when you either have an exception and want to complete the operation with an
     * exception or you cannot complete the operation. This will mark the operation as complete with an
     * exception so it can be reacted to accordingly.
     *
     * @param ex The exeception to complete with
     */
    public synchronized void completeExceptionally(Throwable ex) {
        if (nativeResource.isNull()) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "No native handle set in TlsKeyOperation! Cannot complete operation exceptionally");
            return;
        }

        tlsKeyOperationCompleteExceptionally(nativeResource.getNativeHandle(), ex);
        nativeResource.close();
    }

    /**
     * The TlsKeyOperation has special lifetime rules, where you have to call one of the complete functions, and
     * by using this private, internal-only CRT resource, we can still get the benefits of using a CRT resource
     * for detecting memory leaks, while not exposing functionality that would conflict with the lifetime rules.
     */
    private class CrtResourceInternal extends CrtResource {
        CrtResourceInternal(long nativeHandle) {
            acquireNativeHandle(nativeHandle);
        }

        protected void releaseNativeHandle() {}

        protected boolean canReleaseReferencesImmediately() {
            return true;
        };
    }

    static private void invokePerformOperation(TlsKeyOperationHandler handler, TlsKeyOperation operation) {
        try {
            handler.performOperation(operation);
        } catch (Exception ex) {
            /**
             * printStackTrace gives a nice, full picture of the exception
             * but to use it, we have to use a StringWriter and a PrintWriter
             */
            StringWriter stringWriter = new StringWriter();
            ex.printStackTrace(new PrintWriter(stringWriter));
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "Exception occured!\n" + stringWriter.toString());

            operation.completeExceptionally(ex);
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void tlsKeyOperationComplete(long nativeHandle, byte[] output);
    private static native void tlsKeyOperationCompleteExceptionally(long nativeHandle, Throwable ex);

}


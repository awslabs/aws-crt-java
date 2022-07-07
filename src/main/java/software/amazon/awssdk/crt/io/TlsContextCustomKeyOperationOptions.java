/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;

/**
 * TODO: document
 */
public class TlsContextCustomKeyOperationOptions extends CrtResource {
    TlsKeyOperationHandler operationHandler;
    String certificateFilePath;
    String certificateFileContents;

    /**
     * TODO: document
     */
    public TlsContextCustomKeyOperationOptions(TlsKeyOperationHandler operationHandler) {
        this.operationHandler = operationHandler;
        acquireNativeHandle(tlsContextCustomKeyOperationOptionsNew(this));
    }

    /**
     * Frees the native resources associated with this instance
     */
    @Override
    protected void releaseNativeHandle() {
        tlsContextCustomKeyOperationOptionsDestroy(getNativeHandle());
    }

    /**
     * Use this X.509 certificate (file on disk). The certificate may be specified
     * by other means instead (ex: {@link withCertificateFileContents})
     *
     * @param path path to PEM-formatted certificate file on disk.
     * @return this
     */
    public TlsContextCustomKeyOperationOptions withCertificateFilePath(String path) {
        this.certificateFilePath = path;
        return this;
    }

    /**
     * Use this X.509 certificate (contents in memory). The certificate may be
     * specified by other means instead (ex: {@link withCertificateFilePath})
     *
     * @param contents contents of PEM-formatted certificate file.
     * @return this
     */
    public TlsContextCustomKeyOperationOptions withCertificateFileContents(String contents) {
        this.certificateFileContents = contents;
        return this;
    }

    public String getCertificateFilePath() {
        return certificateFilePath;
    }

    public String getCertificateFileContents() {
        return certificateFileContents;
    }

    public TlsKeyOperationHandler getOperationHandler() {
        return operationHandler;
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        // TODO determine if this needs to be true or false
        return true;
    }

    protected void invokePerformOperation(TlsKeyOperation operation)
    {
        // If an exception occurs for any reason, catch it and complete the operation with an exception.
        try {
            this.operationHandler.performOperation(operation);
        } catch (Exception ex) {
            Log.log(LogLevel.Error, LogSubject.CommonGeneral,
                "Exception occured while performing TlsKeyOperation: " + ex.toString());
            operation.completeExceptionally(ex);
        }
    }

    private static native long tlsContextCustomKeyOperationOptionsNew(
        TlsContextCustomKeyOperationOptions keyOperationOptions) throws CrtRuntimeException;
    private static native long tlsContextCustomKeyOperationOptionsDestroy(long context);

}

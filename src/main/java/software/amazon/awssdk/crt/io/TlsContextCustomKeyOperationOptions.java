/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

/**
 * Top level configuration for the custom TLS key operations.
 */
public class TlsContextCustomKeyOperationOptions {
    private TlsKeyOperationHandler operationHandler;
    private String certificateFilePath;
    private String certificateFileContents;

    /**
     * Creates a new TlsContextCustomKeyOperationOptions and sets the TlsKeyOperationHandler that
     * will be invoked when there is a TLS key operation that needs to be performed.
     *
     * Through the TlsKeyOperationHandler you can add your own private key operations during the
     * mutual TLS handshake.
     *
     * @param operationHandler The operation handler to use when performing a TLS key operation.
     */
    public TlsContextCustomKeyOperationOptions(TlsKeyOperationHandler operationHandler) {
        this.operationHandler = operationHandler;
    }

    /**
     * Use this X.509 certificate (file on disk). The certificate may be specified
     * by other means instead (ex: {@link withCertificateFileContents})
     *
     * @param path path to PEM-formatted certificate file on disk.
     * @return The TlsContextCustomKeyOperationOptions after setting the path
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
     * @return The TlsContextCustomKeyOperationOptions after certificate contents
     */
    public TlsContextCustomKeyOperationOptions withCertificateFileContents(String contents) {
        this.certificateFileContents = contents;
        return this;
    }

    /**
     * Returns the path to the X.509 certificate file on desk if it has been set.
     *
     * @return The path to the certificate file
     */
    public String getCertificateFilePath() {
        return certificateFilePath;
    }

    /**
     * Returns the contents of the X.509 certificate if it has been set.
     *
     * @return The contents of the certificate
     */
    public String getCertificateFileContents() {
        return certificateFileContents;
    }

    /**
     * Returns the TlsKeyOperationHandler assigned to this class.
     *
     * @return The operation handler that will be used
     */
    public TlsKeyOperationHandler getOperationHandler() {
        return operationHandler;
    }

}

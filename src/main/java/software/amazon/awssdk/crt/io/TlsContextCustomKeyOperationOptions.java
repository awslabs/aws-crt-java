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
     * MQTT TLS handshake.
     */
    public TlsContextCustomKeyOperationOptions(TlsKeyOperationHandler operationHandler) {
        this.operationHandler = operationHandler;
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

    /**
     * Returns the path to the X.509 certificate file on desk if it has been set.
     */
    public String getCertificateFilePath() {
        return certificateFilePath;
    }

    /**
     * Returns the contents of the X.509 certificate if it has been set.
     */
    public String getCertificateFileContents() {
        return certificateFileContents;
    }

    /**
     * Returns the TlsKeyOperationHandler assigned to this class.
     */
    public TlsKeyOperationHandler getOperationHandler() {
        return operationHandler;
    }

}

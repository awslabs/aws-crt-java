/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

/**
 * TODO: document
 */
public class TlsContextCustomKeyOperationOptions {
    TlsKeyOperationHandler operationHandler;
    String certificateFilePath;
    String certificateFileContents;

    /**
     * TODO: document
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

    public String getCertificateFilePath() {
        return certificateFilePath;
    }

    public String getCertificateFileContents() {
        return certificateFileContents;
    }

    public TlsKeyOperationHandler getOperationHandler() {
        return operationHandler;
    }

}

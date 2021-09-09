/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

/**
 * Options for TLS using a PKCS#11 library for private key operations.
 *
 * @see TlsContextOptions#withMtlsPkcs11(TlsContextPkcs11Options)
 */
public class TlsContextPkcs11Options {
    Pkcs11Lib pkcs11Lib;
    String userPin;
    Integer slotId;
    String tokenLabel;
    String privateKeyObjectLabel;
    String certificateFilePath;
    String certificateFileContents;

    /**
     * Constructor
     *
     * @param pkcs11Lib use this PKCS#11 library
     */
    public TlsContextPkcs11Options(Pkcs11Lib pkcs11Lib) {
        this.pkcs11Lib = pkcs11Lib;
    }

    /**
     * Use this PIN to log the user into the PKCS#11 token. Leave unspecified to log
     * into a token with a "protected authentication path".
     *
     * @param pin PIN
     * @return this
     */
    public TlsContextPkcs11Options withUserPin(String pin) {
        this.userPin = pin;
        return this;
    }

    /**
     * Specify the slot ID containing a PKCS#11 token. If not specified, the token
     * will be chosen based on other criteria (such as token label).
     *
     * @param slotId slot ID
     * @return this
     */
    public TlsContextPkcs11Options withSlotId(int slotId) {
        this.slotId = slotId;
        return this;
    }

    /**
     * Specify the label of the PKCS#11 token to use. If not specified, the token
     * will be chosen based on other criteria (such as slot ID).
     *
     * @param label label of token
     * @return this
     */
    public TlsContextPkcs11Options withTokenLabel(String label) {
        this.tokenLabel = label;
        return this;
    }

    /**
     * Specify the label of the private key object on the PKCS#11 token. If not
     * specified, the key will be chosen based on other criteria (such as being the
     * only available private key on the token).
     *
     * @param label label of private key object
     * @return this
     */
    public TlsContextPkcs11Options withPrivateKeyObjectLabel(String label) {
        this.privateKeyObjectLabel = label;
        return this;
    }

    /**
     * Use this X.509 certificate (file on disk). The certificate may be specified
     * by other means instead (ex: {@link withCertificateFileContents})
     *
     * @param path path to PEM-formatted certificate file on disk.
     * @return this
     */
    public TlsContextPkcs11Options withCertificateFilePath(String path) {
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
    public TlsContextPkcs11Options withCertificateFileContents(String contents) {
        this.certificateFileContents = contents;
        return this;
    }
}

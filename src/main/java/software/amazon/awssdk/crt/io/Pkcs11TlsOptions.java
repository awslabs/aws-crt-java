/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

/**
 * Options for TLS using a PKCS#11 library for private key operations.
 *
 * @see TlsContextOptions#withMtlsPkcs11(Pkcs11TlsOptions)
 */
public class Pkcs11TlsOptions {
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
    public Pkcs11TlsOptions(Pkcs11Lib pkcs11Lib) {
        this.pkcs11Lib = pkcs11Lib;
    }

    /**
     * Use this PIN to log the user into the token. Leave unspecified to log into a
     * token with a "protected authentication path".
     *
     * @param pin PIN
     * @return this
     */
    public Pkcs11TlsOptions withUserPin(String pin) {
        this.userPin = pin;
        return this;
    }

    /**
     * Use the token in this slot ID. If not specified, the token will be chosen
     * based on other criteria (such as token label).
     *
     * @param slotId slot ID
     * @return this
     */
    public Pkcs11TlsOptions withSlotId(int slotId) {
        this.slotId = slotId;
        return this;
    }

    /**
     * Use the token with this label. If not specified, the token will be chosen
     * based on other criteria (such as slot ID).
     *
     * @param label label of token
     * @return this
     */
    public Pkcs11TlsOptions withTokenLabel(String label) {
        this.tokenLabel = label;
        return this;
    }

    /**
     * Use the private key object with this label. If not specified, the key will be
     * chosen based on other criteria (such as being the only available key).
     *
     * @param label label of private key object
     * @return this
     */
    public Pkcs11TlsOptions withPrivateKeyObjectLabel(String label) {
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
    public Pkcs11TlsOptions withCertificateFilePath(String path) {
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
    public Pkcs11TlsOptions withCertificateFileContents(String contents) {
        this.certificateFileContents = contents;
        return this;
    }
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.utils.PemUtils;
import software.amazon.awssdk.crt.utils.StringUtils;

/**
 * This class wraps the aws_tls_connection_options from aws-c-io to provide
 * access to TLS configuration contexts in the AWS Common Runtime.
 */
public final class TlsContextOptions extends CrtResource {

    public enum TlsVersions {
        /**
         * SSL v3. This should almost never be used.
         */
        SSLv3(0),
        TLSv1(1),
        /**
         * TLS 1.1
         */
        TLSv1_1(2),
        /**
         * TLS 1.2
         */
        TLSv1_2(3),
        /**
         * TLS 1.3
         */
        TLSv1_3(4),
        /**
         * Use whatever the system default is. This is usually the best option, as it will be automatically updated
         * as the underlying OS or platform changes.
         */
        TLS_VER_SYS_DEFAULTS(128);

        private int version;
        TlsVersions(int val) {
            version = val;
        }

        int getValue() { return version; }
    }

    /**
     * Sets the minimum acceptable TLS version that the {@link TlsContext} will
     * allow. Not compatible with setCipherPreference() API.
     *
     * Select from TlsVersions, a good default is TlsVersions.TLS_VER_SYS_DEFAULTS
     * as this will update if the OS TLS is updated
     */
    public TlsVersions minTlsVersion = TlsVersions.TLS_VER_SYS_DEFAULTS;
    /**
     * Sets the TLS Cipher Preferences that can be negotiated and used during the
     * TLS Connection. Not compatible with setMinimumTlsVersion() API.
     *
     */
    public TlsCipherPreference tlsCipherPreference = TlsCipherPreference.TLS_CIPHER_SYSTEM_DEFAULT;
    /**
     * Sets the ALPN protocol list that will be provided when a TLS connection
     * starts e.g. "x-amzn-mqtt-ca"
     */
    public List<String> alpnList = new ArrayList<>();
    /**
     * Set whether or not the peer should be verified. Default is true for clients,
     * and false for servers. If you are in a development or debugging environment,
     * you can disable this to avoid or diagnose trust store issues. This should
     * always be true on clients in the wild. If you set this to true on a server,
     * it will validate every client connection.
     */
    public boolean verifyPeer = false;

    private String certificate;
    private String privateKey;
    private String certificatePath;
    private String privateKeyPath;
    private String caRoot;
    private String caFile;
    private String caDir;
    private String pkcs12Path;
    private String pkcs12Password;
    private Pkcs11Lib pkcs11Lib;
    private Integer pkcs11SlotId;
    private String pkcs11TokenLabel;
    private String pkcs11Pin;
    private String pkcs11PrivateKeyLabel;

    /**
     * Creates a new set of options that can be used to create a {@link TlsContext}
     */
    private TlsContextOptions() {

    }

    @Override
    public long getNativeHandle() {
        if (super.getNativeHandle() == 0) {
            if (tlsCipherPreference != TlsCipherPreference.TLS_CIPHER_SYSTEM_DEFAULT
                    && minTlsVersion != TlsVersions.TLS_VER_SYS_DEFAULTS) {
                throw new IllegalStateException("tlsCipherPreference and minTlsVersion are mutually exclusive");
            }
            acquireNativeHandle(tlsContextOptionsNew(
                minTlsVersion.getValue(),
                tlsCipherPreference.getValue(),
                alpnList.size() > 0 ? StringUtils.join(";", alpnList) : null,
                certificate,
                privateKey,
                certificatePath,
                privateKeyPath,
                caRoot,
                caFile,
                caDir,
                verifyPeer,
                pkcs12Path,
                pkcs12Password
            ));
        }
        return super.getNativeHandle();
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Frees the native resources associated with this instance
     */
    @Override
    protected void releaseNativeHandle() {
        // It is perfectly acceptable for this to have never created a native resource
        if (!isNull()) {
            tlsContextOptionsDestroy(getNativeHandle());
        }
    }

    public void setCipherPreference(TlsCipherPreference cipherPref) {
        if(!isCipherPreferenceSupported(cipherPref)) {
            throw new IllegalArgumentException("TlsCipherPreference is not supported on this platform: " + cipherPref.name());
        }

        if (this.minTlsVersion != TlsVersions.TLS_VER_SYS_DEFAULTS && cipherPref != TlsCipherPreference.TLS_CIPHER_SYSTEM_DEFAULT) {
            throw new IllegalArgumentException("Currently only setMinimumTlsVersion() or setCipherPreference() may be used, not both.");
        }

        this.tlsCipherPreference = cipherPref;
    }

    /**
     * Sets the path to the certificate that identifies this TLS host. Must be in PEM format.
     * @param certificatePath Path to PEM format certificate
     * @param privateKeyPath Path to PEM format private key
     */
    public void initMtlsFromPath(String certificatePath, String privateKeyPath) {
        this.certificatePath = certificatePath;
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * Sets the certificate/key pair that identifies this TLS host. Must be in
     * PEM format.
     *
     * @param certificate PEM armored certificate
     * @param privateKey  PEM armored private key
     * @throws IllegalArgumentException If the certificate or privateKey are not in PEM format or if they contain chains
     */
    public void initMtls(String certificate, String privateKey) throws IllegalArgumentException {
        this.certificate = PemUtils.cleanUpPem(certificate);
        PemUtils.sanityCheck(certificate, 1, "CERTIFICATE");

        this.privateKey = PemUtils.cleanUpPem(privateKey);
        PemUtils.sanityCheck(privateKey, 1, "PRIVATE KEY");
    }

    /**
     * OSX only - Initializes MTLS with PKCS12 file and password
     * @param pkcs12Path Path to PKCS12 file
     * @param pkcs12Password PKCS12 password
     */
    public void initMtlsPkcs12(String pkcs12Path, String pkcs12Password) {
        if (this.certificate != null || this.privateKey != null || this.certificatePath != null
                || this.privateKeyPath != null) {
            throw new IllegalArgumentException(
                    "PKCS#12 and MTLS via certificate/private key pair are mutually exclusive");
        }
        this.pkcs12Path = pkcs12Path;
        this.pkcs12Password = pkcs12Password;
    }

    /**
     * Returns whether or not ALPN is supported on the current platform
     * @return true if ALPN is supported, false otherwise
     */
    public static boolean isAlpnSupported() {
        return tlsContextOptionsIsAlpnAvailable();
    }

    /**
     * Returns whether or not the current platform can be configured to a specific TlsCipherPreference.
     * @param cipherPref The TlsCipherPreference to check
     * @return True if the current platform does support this TlsCipherPreference, false otherwise
     */
    public static boolean isCipherPreferenceSupported(TlsCipherPreference cipherPref) {
        return tlsContextOptionsIsCipherPreferenceSupported(cipherPref.getValue());
    }

    /**
     * Helper function to provide a TlsContext-local trust store
     * @param caPath Path to the local trust store. Can be null.
     * @param caFile Path to the root certificate. Must be in PEM format.
     */
    public void overrideDefaultTrustStoreFromPath(String caPath, String caFile) {
        if (this.caRoot != null) {
            throw new IllegalArgumentException("Certificate authority is already specified via PEM buffer");
        }
        this.caDir = caPath;
        this.caFile = caFile;
    }

    /**
     * Helper function to provide a TlsContext-local trust store
     *
     * @param caRoot Buffer containing the root certificate chain. Must be in PEM format.
     * @throws IllegalArgumentException if the CA Root PEM file is malformed
     */
    public void overrideDefaultTrustStore(String caRoot) throws IllegalArgumentException {
        if (this.caFile != null || this.caDir != null) {
            throw new IllegalArgumentException("Certificate authority is already specified via path(s)");
        }
        this.caRoot = PemUtils.cleanUpPem(caRoot);
        // 1024 certs in the chain is the default supported by s2n:
        PemUtils.sanityCheck(this.caRoot, 1024, "CERTIFICATE");
    }

    /**
     * Helper which creates a default set of TLS options for the current platform
     * @return A default configured set of options for a TLS client connection
     */
    public static TlsContextOptions createDefaultClient() {
        TlsContextOptions options = new TlsContextOptions();
        options.verifyPeer = true;
        return options;
    }

    /**
     * Helper which creates a default set of TLS options for the current platform
     *
     * @return A default configured set of options for a TLS server connection
     */
    public static TlsContextOptions createDefaultServer() {
        TlsContextOptions options = new TlsContextOptions();
        options.verifyPeer = false;
        return options;
    }

    /**
     * Helper which creates TLS options using a certificate and private key
     * @param certificatePath Path to a PEM format certificate
     * @param privateKeyPath Path to a PEM format private key
     * @return A set of options for setting up an MTLS connection
     */
    public static TlsContextOptions createWithMtlsFromPath(String certificatePath, String privateKeyPath) {
        TlsContextOptions options = new TlsContextOptions();
        options.initMtlsFromPath(certificatePath, privateKeyPath);
        options.verifyPeer = true;
        return options;
    }

    /**
     * Helper which creates TLS options using a certificate and private key
     *
     * @param certificate String containing a PEM format certificate
     * @param privateKey  String containing a PEM format private key
     * @return A set of options for setting up an MTLS connection
     * @throws IllegalArgumentException If either PEM fails to parse
     */
    public static TlsContextOptions createWithMtls(String certificate, String privateKey)
            throws IllegalArgumentException {
        TlsContextOptions options = new TlsContextOptions();
        options.initMtls(certificate, privateKey);
        options.verifyPeer = true;
        return options;
    }

    /**
     * OSX only - Helper which creates TLS options using PKCS12
     * @param pkcs12Path The path to a PKCS12 file @see #setPkcs12Path(String)
     * @param pkcs12Password The PKCS12 password @see #setPkcs12Password(String)
     * @return A set of options for creating a PKCS12 TLS connection
     */
    public static TlsContextOptions createWithMtlsPkcs12(String pkcs12Path, String pkcs12Password) {
        TlsContextOptions options = new TlsContextOptions();
        options.initMtlsPkcs12(pkcs12Path, pkcs12Password);
        options.verifyPeer = true;
        return options;
    }

    /*******************************************************************************
     * .with() methods
     ******************************************************************************/

    /**
     * Sets the ciphers that the TlsContext will be able to use
     * @param cipherPref The preference set of ciphers to use
     * @return this
     */
    public TlsContextOptions withCipherPreference(TlsCipherPreference cipherPref) {
        setCipherPreference(cipherPref);
        return this;
    }

    /**
     * Sets the minimum TLS version that the TlsContext will allow. Defaults to
     * OS defaults.
     * @param version Minimum acceptable TLS version
     * @return this
     */
    public TlsContextOptions withMinimumTlsVersion(TlsVersions version) {
        minTlsVersion = version;
        return this;
    }

    /**
     * Sets the ALPN protocols list for any connections using this TlsContext
     * @param alpnList Semi-colon delimited list of supported ALPN protocols
     * @return this
     */
    public TlsContextOptions withAlpnList(String alpnList) {
        String[] parts = alpnList.split(";");
        for (String part : parts) {
            this.alpnList.add(part);
        }
        return this;
    }

    /**
     * Enables mutual TLS (mTLS) on this TlsContext
     * @param certificate mTLS certificate, in PEM format
     * @param privateKey mTLS private key, in PEM format
     * @return this
     */
    public TlsContextOptions withMtls(String certificate, String privateKey) {
        this.initMtls(certificate, privateKey);
        return this;
    }

    /**
     * Enables mutual TLS (mTLS) on this TlsContext
     * @param certificatePath path to mTLS certificate, in PEM format
     * @param privateKeyPath path to mTLS private key, in PEM format
     * @return this
     */
    public TlsContextOptions withMtlsFromPath(String certificatePath, String privateKeyPath) {
        this.initMtlsFromPath(certificatePath, privateKeyPath);
        return this;
    }

    /**
     * Set the certificate for mutual TLS (mTLS).
     * A private key must also be set.
     * @param contents contents of PEM-formatted certificate
     * @return this
     */
    public TlsContextOptions withMtlsCertificate(String contents) {
        this.certificate = contents;
        return this;
    }

    /**
     * Set the certificate for mutual TLS (mTLS).
     * A private key must also be set.
     * @param path path to PEM-formatted certificate
     * @return this
     */
    public TlsContextOptions withMtlsCertificatePath(String path) {
        this.certificatePath = path;
        return this;
    }

    /**
     * Specifies the certificate authority to use. By default, the OS CA repository will be used.
     * @param caRoot Certificate Authority, in PEM format
     * @return this
     */
    public TlsContextOptions withCertificateAuthority(String caRoot) {
        this.overrideDefaultTrustStore(caRoot);
        return this;
    }

    /**
     * Specifies the certificate authority to use.
     * @param caDirPath Path to certificate directory, e.g. /etc/ssl/certs
     * @param caFilePath Path to ceritificate authority, in PEM format
     * @return this
     */
    public TlsContextOptions withCertificateAuthorityFromPath(String caDirPath, String caFilePath) {
        this.overrideDefaultTrustStoreFromPath(caDirPath, caFilePath);
        return this;
    }

    /**
     * Apple platforms only, specifies mTLS using PKCS#12
     * @param pkcs12Path Path to PKCS#12 certificate, in PEM format
     * @param pkcs12Password PKCS#12 password
     * @return this
     */
    public TlsContextOptions withMtlsPkcs12(String pkcs12Path, String pkcs12Password) {
        this.initMtlsPkcs12(pkcs12Path, pkcs12Password);
        return this;
    }

    /**
     * Unix platforms only, specifies mTLS using a PKCS#11 library for private key operations.
     * Use other methods to specify the mTLS certificate and private key
     * (ex: {@link withMtlsCertificate}, {@link withPkcs11PrivateKeyLabel}).
     * Consult the "withPkcs11" methods for further options.
     * @param pkcs11Lib PKCS#11 library handle to use.
     * @return this
     */
    public TlsContextOptions withMtlsPkcs11(Pkcs11Lib pkcs11Lib) {
        this.pkcs11Lib = pkcs11Lib;
        return this;
    }

    /**
     * (PKCS#11 only) Use the token in this slot ID.
     * If not specified, the token will be chosen based on other criteria (such as token label).
     * @return this
     * @see withMtlsPkcs11
     */
    public TlsContextOptions withPkcs11SlotId(int slotId) {
        this.pkcs11SlotId = slotId;
        return this;
    }

    /**
     * (PKCS#11 only) Use the token with this label.
     * If not specified, the token will be chosen based on other criteria (such as slot ID).
     * @return this
     * @see withMtlsPkcs11
     */
    public TlsContextOptions withPkcs11TokenLabel(String label) {
        this.pkcs11TokenLabel = label;
        return this;
    }

    /**
     * (PKCS#11 only) Use this PIN to log the user into the token.
     * Leave unspecified to log into a token with a "protected authentication path".
     * @return this
     * @see withMtlsPkcs11
     */
    public TlsContextOptions withPkcs11Pin(String pin) {
        this.pkcs11Pin = pin;
        return this;
    }

    /**
     * (PKCS#11 only) Use the private key with this label.
     * If not specified, the key will be chosen based on other criteria (such as being the only available key).
     * @return this
     * @see withMtlsPkcs11
     */
    public TlsContextOptions withPkcs11PrivateKeyLabel(String label) {
        this.pkcs11PrivateKeyLabel = label;
        return this;
    }

    /**
     * Sets whether or not TLS will validate the certificate from the peer. On clients,
     * this is enabled by default. On servers, this is disabled by default.
     * @param verify true to verify peers, false to ignore certs
     * @return this
     */
    public TlsContextOptions withVerifyPeer(boolean verify) {
        this.verifyPeer = verify;
        return this;
    }

    /**
     * Enables TLS peer verification of certificates
     * @see TlsContextOptions#withVerifyPeer(boolean)
     * @return this
     */
    public TlsContextOptions withVerifyPeer() {
        return this.withVerifyPeer(true);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tlsContextOptionsNew(
                int minTlsVersion,
                int cipherPref,
                String alpn,
                String certificate,
                String privateKey,
                String certificatePath,
                String privateKeyPath,
                String caRoot,
                String caFile,
                String caDir,
                boolean verifyPeer,
                String pkcs12Path,
                String pkcs12Password
            );

    private static native void tlsContextOptionsDestroy(long elg);

    private static native boolean tlsContextOptionsIsAlpnAvailable();

    private static native boolean tlsContextOptionsIsCipherPreferenceSupported(int cipherPref);

};

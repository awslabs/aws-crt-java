/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.awssdk.crt.io;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.utils.PemUtils;

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

    /**
     * Creates a new set of options that can be used to create a {@link TlsContext}
     * @throws CrtRuntimeException If the system is not able to allocate space for a native tls context options structure
     */
    private TlsContextOptions() throws CrtRuntimeException {
        
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
                alpnList.size() > 0 ? String.join(";", alpnList) : null,
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
    public void initMTLSFromPath(String certificatePath, String privateKeyPath) {
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
    public void initMTLS(String certificate, String privateKey) throws IllegalArgumentException {
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
    public void initMTLSPkcs12(String pkcs12Path, String pkcs12Password) {
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
     */
    public void overrideDefaultTrustStore(String caRoot) throws IllegalArgumentException {
        if (this.caFile != null || this.caDir != null) {
            throw new IllegalArgumentException("Certificate authority is already specified via path(s)");
        }
        this.caRoot = PemUtils.cleanUpPem(caRoot);
        // 7 certs in the chain is the default supported by s2n:
        // https://github.com/awslabs/s2n/blob/master/tls/s2n_x509_validator.c#L53
        PemUtils.sanityCheck(this.caRoot, 7, "CERTIFICATE");
    }

    /**
     * Helper which creates a default set of TLS options for the current platform
     * @return A default configured set of options for a TLS client connection
     */
    public static TlsContextOptions createDefaultClient() {
        return new TlsContextOptions();
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
     * @throws CrtRuntimeException @see #constructor()
     */
    public static TlsContextOptions createWithMTLSFromPath(String certificatePath, String privateKeyPath) throws CrtRuntimeException {
        TlsContextOptions options = new TlsContextOptions();
        options.initMTLSFromPath(certificatePath, privateKeyPath);
        return options;
    }

    /**
     * Helper which creates TLS options using a certificate and private key
     * 
     * @param certificate String containing a PEM format certificate
     * @param privateKey  String containing a PEM format private key
     * @return A set of options for setting up an MTLS connection
     * @throws CrtRuntimeException      @see #constructor()
     * @throws IllegalArgumentException If either PEM fails to parse
     */
    public static TlsContextOptions createWithMTLS(String certificate, String privateKey)
            throws CrtRuntimeException, IllegalArgumentException {
        TlsContextOptions options = new TlsContextOptions();
        options.initMTLS(certificate, privateKey);
        return options;
    }

    /**
     * OSX only - Helper which creates TLS options using PKCS12
     * @param pkcs12Path The path to a PKCS12 file @see #setPkcs12Path(String)
     * @param pkcs12Password The PKCS12 password @see #setPkcs12Password(String)
     * @return A set of options for creating a PKCS12 TLS connection
     * @throws CrtRuntimeException @see #constructor()
     */
    public static TlsContextOptions createWithMTLSPkcs12(String pkcs12Path, String pkcs12Password)
            throws CrtRuntimeException {
        TlsContextOptions options = new TlsContextOptions();
        options.initMTLSPkcs12(pkcs12Path, pkcs12Password);
        return options;
    }

    /*******************************************************************************
     * .with() methods
     ******************************************************************************/

    public TlsContextOptions withCipherPreference(TlsCipherPreference cipherPref) {
        setCipherPreference(cipherPref);
        return this;
    }

    public TlsContextOptions withMinimumTlsVersion(TlsVersions version) {
        minTlsVersion = version;
        return this;
    }

    public TlsContextOptions withAlpnList(String alpnList) {
        String[] parts = alpnList.split(";");
        for (String part : parts) {
            this.alpnList.add(part);
        }
        return this;
    }

    public TlsContextOptions withMtls(String certificate, String privateKey) {
        this.initMTLS(certificate, privateKey);
        return this;
    }

    public TlsContextOptions withMtlsFromPath(String certificatePath, String privateKeyPath) {
        this.initMTLSFromPath(certificatePath, privateKeyPath);
        return this;
    }

    public TlsContextOptions withCertificateAuthority(String caRoot) {
        this.overrideDefaultTrustStore(caRoot);
        return this;
    }

    public TlsContextOptions withCertificateAuthorityFromPath(String caDirPath, String caFilePath) {
        this.overrideDefaultTrustStoreFromPath(caDirPath, caFilePath);
        return this;
    }

    public TlsContextOptions withPkcs12(String pkcs12Path, String pkcs12Password) {
        this.initMTLSPkcs12(pkcs12Path, pkcs12Password);
        return this;
    }

    public TlsContextOptions withVerifyPeer(boolean verify) {
        this.verifyPeer = verify;
        return this;
    }

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

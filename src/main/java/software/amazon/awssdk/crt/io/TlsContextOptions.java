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

import java.nio.ByteBuffer;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;

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

    private TlsVersions tlsVersion = TlsVersions.TLS_VER_SYS_DEFAULTS;
    private TlsCipherPreference tlsCipherPreference = TlsCipherPreference.TLS_CIPHER_SYSTEM_DEFAULT;

    /**
     * Creates a new set of options that can be used to create a {@link TlsContext}
     * @throws CrtRuntimeException If the system is not able to allocate space for a native tls context options structure
     */
    public TlsContextOptions() throws CrtRuntimeException {
        acquireNativeHandle(tlsContextOptionsNew());
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

    /**
     * Sets the minimum acceptable TLS version that the {@link TlsContext} will allow. Not compatible with
     * setCipherPreference() API.
     *
     * @param version Select from TlsVersions, a good default is TlsVersions.TLS_VER_SYS_DEFAULTS
     * as this will update if the OS TLS is updated
     */
    public void setMinimumTlsVersion(TlsVersions version) {
        if (this.tlsCipherPreference != TlsCipherPreference.TLS_CIPHER_SYSTEM_DEFAULT && version != TlsVersions.TLS_VER_SYS_DEFAULTS) {
            throw new IllegalArgumentException("Currently only setMinimumTlsVersion() or setCipherPreference() may be used, not both.");
        }

        this.tlsVersion = version;
        tlsContextOptionsSetMinimumTlsVersion(getNativeHandle(), version.getValue());
    }

    /**
     * Sets the ALPN protocol list that will be provided when a TLS connection
     * starts
     * @param alpn The ALPN protocol to use, e.g. "x-amzn-mqtt-ca"
     */
    public void setAlpnList(String alpn) {
        tlsContextOptionsSetAlpn(getNativeHandle(), alpn);
    }

    /**
     * Sets the TLS Cipher Preferences that can be negotiated and used during the TLS Connection. Not compatible with
     * setMinimumTlsVersion() API.
     *
     * @param cipherPref The Cipher Preference to use
     */
    public void setCipherPreference(TlsCipherPreference cipherPref) {
        if(!isCipherPreferenceSupported(cipherPref)) {
            throw new IllegalArgumentException("TlsCipherPreference is not supported on this platform: " + cipherPref.name());
        }

        if (this.tlsVersion != TlsVersions.TLS_VER_SYS_DEFAULTS && cipherPref != TlsCipherPreference.TLS_CIPHER_SYSTEM_DEFAULT) {
            throw new IllegalArgumentException("Currently only setMinimumTlsVersion() or setCipherPreference() may be used, not both.");
        }

        this.tlsCipherPreference = cipherPref;
        tlsContextOptionsSetCipherPreference(getNativeHandle(), cipherPref.getValue());
    }

    /**
     * Sets the path to the certificate that identifies this TLS host. Must be in PEM format.
     * @param certificatePath Path to PEM format certificate
     * @param privateKeyPath Path to PEM format private key
     */
    public void initMTLSFromPath(String certificatePath, String privateKeyPath) {
        tlsContextOptionsInitMTLSFromPath(getNativeHandle(), certificatePath, privateKeyPath);
    }

    /**
     * Sets the path to the certificate that identifies this TLS host. Must be in
     * PEM format.
     * 
     * @param certificate PEM format certificate
     * @param privateKey  PEM format private key
     */
    public void initMTLS(ByteBuffer certificate, ByteBuffer privateKey) {
        tlsContextOptionsInitMTLS(getNativeHandle(), certificate, privateKey);
    }

    /**
     * OSX only - Initializes MTLS with PKCS12 file and password
     * @param pkcs12Path Path to PKCS12 file
     * @param pkcs12Password PKCS12 password
     */
    public void initMTLSPkcs12(String pkcs12Path, String pkcs12Password) {
        tlsContextOptionsInitMTLSPkcs12FromPath(getNativeHandle(), pkcs12Path, pkcs12Password);
    }

    /**
     * Set whether or not the peer should be verified. Default is true for clients, and false for servers.
     * If you are in a development or debugging environment, you can disable this to avoid or diagnose trust
     * store issues. This should always be true on clients in the wild.
     * If you set this to true on a server, it will validate every client connection.
     * @param verify true to verify peers, false to skip verification
     */
    public void setVerifyPeer(boolean verify) {
        tlsContextOptionsSetVerifyPeer(getNativeHandle(), verify);
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
        tlsContextOptionsOverrideDefaultTrustStoreFromPath(getNativeHandle(), caFile, caPath);
    }

    /**
     * Helper function to provide a TlsContext-local trust store
     * 
     * @param caRoot Buffer containing the root certificate chain. Must be in PEM format.
     */
    public void overrideDefaultTrustStore(ByteBuffer caRoot) {
        tlsContextOptionsOverrideDefaultTrustStore(getNativeHandle(), ByteBufferUtils.toDirectBuffer(caRoot));
    }

    /**
     * Helper which creates a default set of TLS options for the current platform
     * @return A default configured set of options for a TLS client connection
     * @throws CrtRuntimeException @see TlsContextOptions.TlsContextOptions()
     */
    public static TlsContextOptions createDefaultClient() throws CrtRuntimeException {
        return new TlsContextOptions();
    }

    /**
     * Helper which creates TLS options using a certificate and private key
     * @param certificatePath Path to a PEM format certificate
     * @param privateKeyPath Path to a PEM format private key
     * @return A set of options for setting up an MTLS connection
     * @throws CrtRuntimeException @see #constructor()
     */
    public static TlsContextOptions createWithMTLS(String certificatePath, String privateKeyPath) throws CrtRuntimeException {
        TlsContextOptions options = new TlsContextOptions();
        options.initMTLSFromPath(certificatePath, privateKeyPath);
        return options;
    }

    /**
     * OSX only - Helper which creates TLS options using PKCS12
     * @param pkcs12Path The path to a PKCS12 file @see #setPkcs12Path(String)
     * @param pkcs12Password The PKCS12 password @see #setPkcs12Password(String)
     * @return A set of options for creating a PKCS12 TLS connection
     * @throws CrtRuntimeException @see #constructor()
     */
    public static TlsContextOptions createWithMTLSPkcs12(String pkcs12Path, String pkcs12Password) throws CrtRuntimeException {
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
        setMinimumTlsVersion(version);
        return this;
    }

    public TlsContextOptions withAlpnList(String alpnList) {
        setAlpnList(alpnList);
        return this;
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tlsContextOptionsNew() throws CrtRuntimeException;

    private static native void tlsContextOptionsDestroy(long elg);
    
    private static native void tlsContextOptionsSetMinimumTlsVersion(long tls, int version);

    private static native void tlsContextOptionsOverrideDefaultTrustStoreFromPath(long tls, String ca_file,
            String ca_path);
    
    private static native void tlsContextOptionsOverrideDefaultTrustStore(long tls, ByteBuffer caFile);

    private static native void tlsContextOptionsSetAlpn(long tls, String alpn);

    private static native void tlsContextOptionsSetCipherPreference(long tls, int cipherPref);

    private static native void tlsContextOptionsInitMTLSFromPath(long tls, String cert_path, String key_path);

    private static native void tlsContextOptionsInitMTLS(long tls, ByteBuffer cert, ByteBuffer key);
    
    private static native void tlsContextOptionsInitMTLSPkcs12FromPath(long tls, String pkcs12_path, String pkcs12_password);

    private static native void tlsContextOptionsSetVerifyPeer(long tls, boolean verify);

    private static native boolean tlsContextOptionsIsAlpnAvailable();

    private static native boolean tlsContextOptionsIsCipherPreferenceSupported(int cipherPref);

};

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

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.CrtResource;

import java.io.Closeable;

/**
 * This class wraps the aws_tls_connection_options from aws-c-io to provide
 * access to TLS configuration contexts in the AWS Common Runtime.
 */
public final class TlsContextOptions extends CrtResource implements Closeable {

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
     * Creates a new set of options that can be used to create a {@link TlsContext}
     * @throws CrtRuntimeException
     */
    public TlsContextOptions() throws CrtRuntimeException {
        acquire(tlsContextOptionsNew());
    }

    /**
     * Frees the native resources associated with this instance
     */
    @Override
    public void close() {
        if (native_ptr() != 0) {
            tlsContextOptionsDestroy(release());
        }
    }

    /**
     * Sets the minimum acceptable TLS version that the {@link TlsContext} will allow
     * @param version
     */
    public void setMinimumTlsVersion(TlsVersions version) {
        tlsContextOptionsSetMinimumTlsVersion(native_ptr(), version.getValue());
    }

    /**
     * Sets the root certificate to validate certificates against.
     * @param caFile Path to PEM format root certificate
     */
    public void setCaFile(String caFile) {
        tlsContextOptionsSetCaFile(native_ptr(), caFile);
    }

    /**
     * Sets the path to a local trust store to use for validation
     * @param caPath
     */
    public void setCaPath(String caPath) {
        tlsContextOptionsSetCaPath(native_ptr(), caPath);
    }

    /**
     * Sets the ALPN protocol list that will be provided when a TLS connection starts
     * @param alpn
     */
    public void setAlpnList(String alpn) {
        tlsContextOptionsSetAlpn(native_ptr(), alpn);
    }

    /**
     * Sets the path to the certificate that identifies this TLS host. Must be in PEM format.
     * @param certificatePath Path to PEM format certificate
     */
    public void setCertificatePath(String certificatePath) {
        tlsContextOptionsSetCertificatePath(native_ptr(), certificatePath);
    }

    /**
     * Sets the path to the private key for the certificate provided via {@link setCertificatePath}
     * @param privateKeyPath
     */
    public void setPrivateKeyPath(String privateKeyPath) {
        tlsContextOptionsSetPrivateKeyPath(native_ptr(), privateKeyPath);
    }

    /**
     * OSX only - Sets the path to the PKCS12 file
     * @param pkcs12Path
     */
    public void setPkcs12Path(String pkcs12Path) {
        tlsContextOptionsSetPkcs12Path(native_ptr(), pkcs12Path);
    }

    /**
     * OSX only - Sets the password for PKCS12
     * @param pkcs12Password
     */
    public void setPkcs12Password(String pkcs12Password) {
        tlsContextOptionsSetPkcs12Password(native_ptr(), pkcs12Password);
    }

    /**
     * Set whether or not the peer should be verified. Default is true for clients, and false for servers.
     * If you are in a development or debugging environment, you can disable this to avoid or diagnose trust
     * store issues. This should always be true on clients in the wild.
     * If you set this to true on a server, it will validate every client connection.
     * @param verify
     */
    public void setVerifyPeer(boolean verify) {
        tlsContextOptionsSetVerifyPeer(native_ptr(), verify);
    }

    /**
     * Returns whether or not ALPN is supported on the current platform
     * @return
     */
    public static boolean isAlpnSupported() {
        return tlsContextOptionsIsAlpnAvailable();
    }

    /**
     * Helper function to provide a TlsContext-local trust store
     * @param caPath Path to the local trust store. Can be null.
     * @param caFile Path to the root certificate. Must be in PEM format.
     */
    public void overrideDefaultTrustStore(String caPath, String caFile) {
        setCaPath(caPath);
        setCaFile(caFile);
    }

    /**
     * Helper which creates a default set of TLS options for the current platform
     * @return
     * @throws CrtRuntimeException
     */
    public static TlsContextOptions createDefaultClient() throws CrtRuntimeException {
        return new TlsContextOptions();
    }

    /**
     * Helper which creates TLS options using a certificate and private key
     * @param certificatePath Path to a PEM format certificate
     * @param privateKeyPath Path to a PEM format private key
     * @return
     * @throws CrtRuntimeException
     */
    public static TlsContextOptions createWithMTLS(String certificatePath, String privateKeyPath) throws CrtRuntimeException {
        TlsContextOptions options = new TlsContextOptions();
        options.setCertificatePath(certificatePath);
        options.setPrivateKeyPath(privateKeyPath);
        return options;
    }

    /**
     * OSX only - Helper which creates TLS options using PKCS12
     * @param pkcs12Path {@see setPkcs12Path}
     * @param pkcs12Password {@see setPkcs12Password}
     * @return
     * @throws CrtRuntimeException
     */
    public static TlsContextOptions createWithMTLSPkcs12(String pkcs12Path, String pkcs12Password) throws CrtRuntimeException {
        TlsContextOptions options = new TlsContextOptions();
        options.setPkcs12Path(pkcs12Path);
        options.setPkcs12Password(pkcs12Password);
        return options;
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tlsContextOptionsNew() throws CrtRuntimeException;

    private static native void tlsContextOptionsDestroy(long elg);
    
    private static native void tlsContextOptionsSetMinimumTlsVersion(long tls, int version);

    private static native void tlsContextOptionsSetCaFile(long tls, String ca_file);

    private static native void tlsContextOptionsSetCaPath(long tls, String ca_path);

    private static native void tlsContextOptionsSetAlpn(long tls, String alpn);

    private static native void tlsContextOptionsSetCertificatePath(long tls, String cert_path);

    private static native void tlsContextOptionsSetPrivateKeyPath(long tls, String key_path);

    private static native void tlsContextOptionsSetPkcs12Path(long tls, String pkcs12_path);

    private static native void tlsContextOptionsSetPkcs12Password(long tls, String pkcs12_password);

    private static native void tlsContextOptionsSetVerifyPeer(long tls, boolean verify);

    private static native boolean tlsContextOptionsIsAlpnAvailable();
};

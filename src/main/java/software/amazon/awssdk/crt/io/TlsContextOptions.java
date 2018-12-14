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
        SSLv3(0),
        TLSv1(1),
        TLSv1_1(2),
        TLSv1_2(3),
        TLSv1_3(4),
        TLS_VER_SYS_DEFAULTS(128);

        private int version;
        TlsVersions(int val) {
            version = val;
        }

        int getValue() { return version; }
    }

    public TlsContextOptions() throws CrtRuntimeException {
        acquire(tlsContextOptionsNew());
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            tlsContextOptionsDestroy(release());
        }
    }

    public void setMinimumTlsVersion(TlsVersions version) {
        tlsContextOptionsSetMinimumTlsVersion(native_ptr(), version.getValue());
    }

    public void setCaFile(String caFile) {
        tlsContextOptionsSetCaFile(native_ptr(), caFile);
    }

    public void setCaPath(String caPath) {
        tlsContextOptionsSetCaPath(native_ptr(), caPath);
    }

    public void setAlpnList(String alpn) {
        tlsContextOptionsSetAlpn(native_ptr(), alpn);
    }

    public void setCertificatePath(String certificatePath) {
        tlsContextOptionsSetCertificatePath(native_ptr(), certificatePath);
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        tlsContextOptionsSetPrivateKeyPath(native_ptr(), privateKeyPath);
    }

    public void setPkcs12Path(String pkcs12Path) {
        tlsContextOptionsSetPkcs12Path(native_ptr(), pkcs12Path);
    }

    public void setPkcs12Password(String pkcs12Password) {
        tlsContextOptionsSetPkcs12Password(native_ptr(), pkcs12Password);
    }

    public void setVerifyPeer(boolean verify) {
        tlsContextOptionsSetVerifyPeer(native_ptr(), verify);
    }

    public boolean isAlpnSupported() {
        return tlsContextOptionsIsAlpnAvailable();
    }

    public void overrideDefaultTrustStore(String caPath, String caFile) {
        setCaPath(caPath);
        setCaFile(caFile);
    }

    public static TlsContextOptions createDefaultClient() throws CrtRuntimeException {
        return new TlsContextOptions();
    }

    public static TlsContextOptions createWithMTLS(String certificatePath, String privateKeyPath) throws CrtRuntimeException {
        TlsContextOptions options = new TlsContextOptions();
        options.setCertificatePath(certificatePath);
        options.setPrivateKeyPath(privateKeyPath);
        return options;
    }

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

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
package software.amazon.awssdk.crt;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.CrtResource;

import java.io.Closeable;

/**
 * This class wraps the aws_tls_ctx_options from aws-c-io to provide
 * access to TLS configuration contexts in the AWS Common Runtime.
 */
public final class TLSCtxOptions extends CrtResource implements Closeable {

    public enum TlsVersions {
        SSLv3(0),
        TLSv1(1),
        TLSv1_1(2),
        TLSv1_2(3),
        TLSv1_3(4);

        private int version;
        TlsVersions(int val) {
            version = val;
        }

        int getValue() { return version; }
    }

    public TLSCtxOptions() throws CrtRuntimeException {
        acquire(tls_ctx_options_new());
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            tls_ctx_options_clean_up(release());
        }
    }

    void setMinimumTlsVersion(TlsVersions version) {
        tls_ctx_options_set_minimum_tls_version(native_ptr(), version.getValue());
    }

    void setCaFile(String caFile) {
        tls_ctx_options_set_ca_file(native_ptr(), caFile);
    }

    void setCaPath(String caPath) {
        tls_ctx_options_set_ca_path(native_ptr(), caPath);
    }

    void setAlpnList(String alpn) {
        tls_ctx_options_set_alpn(native_ptr(), alpn);
    }

    void setCertificatePath(String certificatePath) {
        tls_ctx_options_set_certificate_path(native_ptr(), certificatePath);
    }

    void setPrivateKeyPath(String privateKeyPath) {
        tls_ctx_options_set_private_key_path(native_ptr(), privateKeyPath);
    }

    void setPkcs12Path(String pkcs12Path) {
        tls_ctx_options_set_pkcs12_path(native_ptr(), pkcs12Path);
    }

    void setPkcs12Password(String pkcs12Password) {
        tls_ctx_options_set_pkcs12_password(native_ptr(), pkcs12Password);
    }

    void setVerifyPeer(boolean verify) {
        tls_ctx_options_set_verify_peer(native_ptr(), verify);
    }

    boolean isAlpnSupported() {
        return tls_ctx_options_is_alpn_available();
    }

    void overrideDefaultTrustStore(String caPath, String caFile) {
        setCaPath(caPath);
        setCaFile(caFile);
    }

    static TLSCtxOptions createWithMTLS(String certificatePath, String privateKeyPath) throws CrtRuntimeException {
        TLSCtxOptions options = new TLSCtxOptions();
        options.setCertificatePath(certificatePath);
        options.setPrivateKeyPath(privateKeyPath);
        return options;
    }

    static TLSCtxOptions createWithMTLSPkcs12(String pkcs12Path, String pkcs12Password) throws CrtRuntimeException {
        TLSCtxOptions options = new TLSCtxOptions();
        options.setPkcs12Path(pkcs12Path);
        options.setPkcs12Password(pkcs12Password);
        return options;
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tls_ctx_options_new() throws CrtRuntimeException;

    private static native void tls_ctx_options_clean_up(long elg);
    
    private static native void tls_ctx_options_set_minimum_tls_version(long tls, int version);

    private static native void tls_ctx_options_set_ca_file(long tls, String ca_file);

    private static native void tls_ctx_options_set_ca_path(long tls, String ca_path);

    private static native void tls_ctx_options_set_alpn(long tls, String alpn);

    private static native void tls_ctx_options_set_certificate_path(long tls, String cert_path);

    private static native void tls_ctx_options_set_private_key_path(long tls, String key_path);

    private static native void tls_ctx_options_set_pkcs12_path(long tls, String pkcs12_path);

    private static native void tls_ctx_options_set_pkcs12_password(long tls, String pkcs12_password);

    private static native void tls_ctx_options_set_verify_peer(long tls, boolean verify);

    private static native boolean tls_ctx_options_is_alpn_available();
};

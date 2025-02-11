/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

/**
 * A TlsCipherPreference represents a hardcoded ordered list of TLS Ciphers to use when negotiating a TLS Connection.
 *
 * At present, the ability to configure arbitrary orderings of TLS Ciphers is not allowed, and only a curated list of
 * vetted TlsCipherPreference's are exposed.
 */
public enum TlsCipherPreference {
    /**
     * Use whatever the System Default Preference is. This is usually the best option, as it will be automatically
     * updated as the underlying OS or platform changes, and will always be supported on all Platforms.
     */
    TLS_CIPHER_SYSTEM_DEFAULT(0),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_2_2023 instead.
     */
    @Deprecated
    TLS_CIPHER_KMS_PQ_TLSv1_0_2019_06(1),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_2_2023 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_SIKE_TLSv1_0_2019_11(2),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_2_2023 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_TLSv1_0_2020_02(3),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_2_2023 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_SIKE_TLSv1_0_2020_02(4),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_2_2023 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_TLSv1_0_2020_07(5),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_2_2023 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05(6),

    /**
     * This  TlsCipherPreference is a clone of the "AWS-CRT-SDK-TLSv1.2-2023" s2n TLS Policy, but with the following PQ SupportedGroups added:
     *  - X25519MLKEM768, P256MLKEM768, SecP256r1Kyber768Draft00, X25519Kyber768Draft00, secp384r1_kyber-768-r3, secp521r1_kyber-1024-r3, secp256r1_kyber-512-r3, x25519_kyber-512-r3
     *
     *  Both X25519MLKEM768 and P256MLKEM768 will be standardized by the IETF soon. Hybrid Groups that contain Kyber may be removed in the future.
     */
    TLS_CIPHER_PREF_PQ_TLSv1_2_2023(7),

    /**
     * The latest recommended Post-quantum enabled TLS Policy. This policy may change over time.
     */
    TLS_CIPHER_PQ_DEFAULT(8);

    private int val;

    TlsCipherPreference(int val) {
        this.val = val;
    }

    int getValue() { return val; }

    /**
     * Not all Cipher Preferences are supported on all Platforms due to differences in the underlying TLS Libraries.
     *
     * @return True if this TlsCipherPreference is currently supported on the current platform.
     */
    public boolean isSupported() {
        return TlsContextOptions.isCipherPreferenceSupported(this);
    }
}

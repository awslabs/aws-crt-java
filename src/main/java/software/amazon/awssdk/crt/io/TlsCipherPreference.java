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
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05 instead.
     */
    @Deprecated
    TLS_CIPHER_KMS_PQ_TLSv1_0_2019_06(1),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_SIKE_TLSv1_0_2019_11(2),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_TLSv1_0_2020_02(3),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_SIKE_TLSv1_0_2020_02(4),

    /**
     * @deprecated This TlsCipherPreference is no longer supported. Use TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05 instead.
     */
    @Deprecated
    TLS_CIPHER_PREF_KMS_PQ_TLSv1_0_2020_07(5),

    /**
     * This TlsCipherPreference supports TLS 1.0 through TLS 1.3, and contains Kyber Round 3 as its highest priority
     * PQ algorithm. PQ algorithms in this preference list will be used in hybrid mode, and will be combined with a
     * classical ECDHE key exchange.
     *
     * NIST has announced that Kyber will be first post-quantum key-agreement algorithm that it will standardize.
     * However, the NIST standardization process might introduce minor changes that may cause the final Kyber standard
     * to differ from the Kyber Round 3 implementation available in this preference list.
     *
     * Since this TlsCipherPreference contains algorithms that have not yet been officially standardized by NIST, this
     * preference list, and any of the PQ algorithms in it, may stop being supported at any time.
     *
     * For more info see:
     *   - https://tools.ietf.org/html/draft-campagna-tls-bike-sike-hybrid
     *   - https://datatracker.ietf.org/doc/html/draft-ietf-tls-hybrid-design
     *   - https://aws.amazon.com/blogs/security/how-to-tune-tls-for-hybrid-post-quantum-cryptography-with-kyber/
     *   - https://nvlpubs.nist.gov/nistpubs/ir/2022/NIST.IR.8413.pdf
     */
    TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05(6);

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

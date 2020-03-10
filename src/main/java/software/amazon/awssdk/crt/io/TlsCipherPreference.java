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
     * This TlsCipherPreference contains BIKE Round 1 and SIKE Round 1 Draft Hybrid TLS Ciphers at the top of the
     * preference list.
     *
     * For more info see:
     *   - https://tools.ietf.org/html/draft-campagna-tls-bike-sike-hybrid
     *   - https://aws.amazon.com/blogs/security/post-quantum-tls-now-supported-in-aws-kms/
     *
     * These Hybrid TLS ciphers perform two Key Exchanges (1 ECDHE + 1 Post-Quantum) during the TLS Handshake in order
     * to combine the security of Classical ECDHE Key Exchange with the conjectured quantum-resistance of newly
     * proposed key exchanges.
     *
     * The algorithms these new Post-Quantum ciphers are based on have been submitted to NIST's Post-Quantum Crypto
     * Standardization Process, and are still under review.
     *
     * While these Post Quantum Hybrid TLS Ciphers are the most preferred ciphers in the preference list, classical
     * ciphers are still present and can be negotiated if the TLS peer does not support these Hybrid TLS Ciphers.
     *
     * This Cipher Preference may stop being supported at any time.
     */
    TLS_CIPHER_KMS_PQ_TLSv1_0_2019_06(1),

    /**
     * This TlsCipherPreference contains SIKE Round 1 Draft Hybrid TLS Ciphers at the top of the preference list.
     *
     * For more info see:
     *   - https://tools.ietf.org/html/draft-campagna-tls-bike-sike-hybrid
     *   - https://aws.amazon.com/blogs/security/post-quantum-tls-now-supported-in-aws-kms/
     *
     * This Cipher Preference may stop being supported at any time.
     */
    TLS_CIPHER_PREF_KMS_PQ_SIKE_TLSv1_0_2019_11(2),

    /**
     * This TlsCipherPreference contains BIKE Round 2, SIKE Round 2, BIKE Round 1, and SIKE Round 1 Draft Hybrid TLS
     * Ciphers at the top of the preference list.
     *
     * For more info see:
     *   - https://tools.ietf.org/html/draft-campagna-tls-bike-sike-hybrid
     *   - https://aws.amazon.com/blogs/security/post-quantum-tls-now-supported-in-aws-kms/
     *
     * This Cipher Preference may stop being supported at any time.
     */
    TLS_CIPHER_PREF_KMS_PQ_TLSv1_0_2020_02(3),

    /**
     * This TlsCipherPreference contains SIKE Round 2 and SIKE Round 1 Draft Hybrid TLS Ciphers at the top of the
     * preference list.
     *
     * For more info see:
     *   - https://tools.ietf.org/html/draft-campagna-tls-bike-sike-hybrid
     *   - https://aws.amazon.com/blogs/security/post-quantum-tls-now-supported-in-aws-kms/
     *
     * This Cipher Preference may stop being supported at any time.
     */
    TLS_CIPHER_PREF_KMS_PQ_SIKE_TLSv1_0_2020_02(4);

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

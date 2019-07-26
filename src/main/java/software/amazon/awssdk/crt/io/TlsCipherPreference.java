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

public enum TlsCipherPreference {
    /**
     * Use whatever the System Default Preference is. This is usually the best option, as it will be automatically
     * updated as the underlying OS or platform changes.
     */
    TLS_CIPHER_SYSTEM_DEFAULT(0),

    /**
     * Contains Draft Hybrid TLS Ciphers: https://tools.ietf.org/html/draft-campagna-tls-bike-sike-hybrid
     *
     * These ciphers perform two Key Exchanges (1 ECDHE + 1 Post-Quantum) during the TLS Handshake in order to
     * combine the security of Classical ECDHE Key Exchange with the conjectured quantum-resistance of newly
     * proposed key exchanges.
     *
     * The algorithms these new Post-Quantum ciphers are based on have been submitted to NIST's Post-Quantum Crypto
     * Standardization Process, and are still under review.
     *
     * This Cipher Preference may stop being supported at any time.
     */
    TLS_CIPHER_KMS_PQ_TLSv1_0_2019_06(1);

    private int val;

    TlsCipherPreference(int val) {
        this.val = val;
    }

    int getValue() { return val; }
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

public class HttpClientTestFixture extends CrtTestFixture {
    public TlsContext createHttpClientTlsContext() {
        return createTlsContextOptions(getContext().trustStore);
    }

    public TlsContext createHttpClientTlsContext(TlsContextOptions tlsOpts) {
        return new TlsContext(configureTlsContextOptions(tlsOpts, getContext().trustStore));
    }
}


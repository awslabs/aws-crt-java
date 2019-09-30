package software.amazon.awssdk.crt.test;

import static software.amazon.awssdk.crt.io.TlsContextOptions.TlsVersions;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsCipherPreference;
import software.amazon.awssdk.crt.io.TlsContextOptions;

public class TlsContextOptionsTest {

    @Test
    public void testTlsContextOptionsAPI() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        CrtResource.waitForNoResources();

        try (TlsContextOptions options = new TlsContextOptions()) {
            for (TlsVersions tlsVersion: TlsContextOptions.TlsVersions.values()) {
                options.setMinimumTlsVersion(tlsVersion);
            }

            options.setMinimumTlsVersion(TlsVersions.TLS_VER_SYS_DEFAULTS);

            for (TlsCipherPreference pref: TlsCipherPreference.values()) {
                if (TlsContextOptions.isCipherPreferenceSupported(pref)) {
                    options.setCipherPreference(pref);
                }
            }

            boolean exceptionThrown = false;

            try {
                options.setCipherPreference(TlsCipherPreference.TLS_CIPHER_KMS_PQ_TLSv1_0_2019_06);
                options.setMinimumTlsVersion(TlsVersions.TLSv1_2);
                Assert.fail();
            } catch (IllegalArgumentException e) {
                exceptionThrown = true;
            }

            Assert.assertTrue(exceptionThrown);
        }

        CrtResource.waitForNoResources();
    }
}

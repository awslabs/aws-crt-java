package software.amazon.awssdk.crt.test;

import static software.amazon.awssdk.crt.io.TlsContextOptions.TlsVersions;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsCipherPreference;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.utils.PemUtils;

public class TlsContextOptionsTest extends CrtTestFixture {

    static final String BAD_CERT = "--- THIS IS NOT A CERT ---";
    static final String ROOT_CA1 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF\n"
            + "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n"
            + "b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL\n"
            + "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n"
            + "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n"
            + "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n"
            + "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n"
            + "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n"
            + "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n"
            + "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n"
            + "jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\n"
            + "AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA\n"
            + "A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI\n"
            + "U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs\n"
            + "N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv\n"
            + "o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU\n"
            + "5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy\n"
            + "rqXRfboQnoZsG4q5WTP468SQvvG5\n"
            + "-----END CERTIFICATE-----";
    static final String TEST_CERT = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDzjCCArYCCQCoztOER4pOkzANBgkqhkiG9w0BAQsFADCBqDELMAkGA1UEBhMC\n"
            + "VVMxEzARBgNVBAgMCldhc2hpbmd0b24xEDAOBgNVBAcMB1NlYXR0bGUxIDAeBgNV\n"
            + "BAoMF0FtYXpvbiBXZWIgU2VydmljZXMgSW5jMRswGQYDVQQLDBJBV1MgU0RLcyBh\n"
            + "bmQgVG9vbHMxEjAQBgNVBAMMCWxvY2FsaG9zdDEfMB0GCSqGSIb3DQEJARYQaGVu\n"
            + "c29AYW1hem9uLmNvbTAeFw0xNzA5MDEwMjE2MThaFw00NTAxMTcwMjE2MThaMIGo\n"
            + "MQswCQYDVQQGEwJVUzETMBEGA1UECAwKV2FzaGluZ3RvbjEQMA4GA1UEBwwHU2Vh\n"
            + "dHRsZTEgMB4GA1UECgwXQW1hem9uIFdlYiBTZXJ2aWNlcyBJbmMxGzAZBgNVBAsM\n"
            + "EkFXUyBTREtzIGFuZCBUb29sczESMBAGA1UEAwwJbG9jYWxob3N0MR8wHQYJKoZI\n"
            + "hvcNAQkBFhBoZW5zb0BhbWF6b24uY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n"
            + "MIIBCgKCAQEA12pXSPgORAMlQtYRbxuz/Ocaoran3C2Fjyjhu0vucSEZSwxDJp75\n"
            + "TBQEMafSpSEKAQLeDt7xuDRDYn52V4UE6cF+xTWhtzsf7mhN/lHaDPcvR2ASPAEk\n"
            + "zkil8KCLY4e6tTxSwQ97splNuEZ099HoJYTTLFaReIfd1D3zZ1EYcSw8w+GZ2SxE\n"
            + "UfYUSL2CFmIYSkQjnlsJCIpCoGgDiBAPbIUJO3KWBDX0JgGDbx3Wf3jXG/Y6T63L\n"
            + "PsO+AS20RCvcEF0F/rlDINzI5EAHO1TOEd9fKOu+JAK06Pw1m77BgOrE7FtvIG7k\n"
            + "YNVuOEPeInOHkOuqryDisB1PwiyPNIbqdQIDAQABMA0GCSqGSIb3DQEBCwUAA4IB\n"
            + "AQDL3vA0QeYb+XE8pUm3lxwso4zf0lwYi8Fni23ThqlvTNrP0glaWNu28aa03F5r\n"
            + "Jc80acRjySG8q/gqwMMLOE+xqLgTzAHLYDnX2BZdaeIJWdgQP/YrWACrnYlVJ4kZ\n"
            + "fi3QiBU0b5OgQdwX0csr6NQ7fv5i9EiNdPf+Ll1gxQj0Q0AaJzb4+TUL4dHZV3L6\n"
            + "RRRK5KpTI3I+5A3vLSYSgwlVT+qB4J6+Z7O9SZX8s0xnm569tECbRnDDYv3E90SU\n"
            + "QMN6Rzsr2crUzQSMq2hQTnrpFvRX52Yw7Dkz4SgkP3Q4xzvITPgA8REgHd4eDgrz\n"
            + "36J362qmeHxjl/+KLxv/Vr4b\n"
            + "-----END CERTIFICATE-----";

    static final String TEST_KEY = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEpAIBAAKCAQEA12pXSPgORAMlQtYRbxuz/Ocaoran3C2Fjyjhu0vucSEZSwxD\n"
            + "Jp75TBQEMafSpSEKAQLeDt7xuDRDYn52V4UE6cF+xTWhtzsf7mhN/lHaDPcvR2AS\n"
            + "PAEkzkil8KCLY4e6tTxSwQ97splNuEZ099HoJYTTLFaReIfd1D3zZ1EYcSw8w+GZ\n"
            + "2SxEUfYUSL2CFmIYSkQjnlsJCIpCoGgDiBAPbIUJO3KWBDX0JgGDbx3Wf3jXG/Y6\n"
            + "T63LPsO+AS20RCvcEF0F/rlDINzI5EAHO1TOEd9fKOu+JAK06Pw1m77BgOrE7Ftv\n"
            + "IG7kYNVuOEPeInOHkOuqryDisB1PwiyPNIbqdQIDAQABAoIBAESQuI+lRQUo6ydG\n"
            + "8+2lp7iL5tJ7yRov8x8KKC9xj8e6fU6B7K3SVA9/H4aeoFGnHoQL4ZpiJBY5rGkh\n"
            + "T5Gz6UhuKmejFoI384Xy9UBJ1VnjI81YKvWmd4yhWxAoSbW4chlVxhFlWD4UxcQt\n"
            + "yPVIftfSW1T1iQAQXu87eMod6eW7VWlyMKicYkBGB2ohI0hW8chx361z96QcpxhA\n"
            + "yBAfnhxuTgKFYSRVfwYSOjHYPOvozmU7Wj0iURT+1MM4iO8YlBDuZEJArs3WAdIe\n"
            + "pmCq6snzOAJ6Y9iE0EGti9QGiAo6na/nWAfVlRSMyS/C1GC0oM0MnpRKSLW0tvLV\n"
            + "vtJG81ECgYEA7lzGpdlAKwWNKPc2YIbtUNomD/eOr7TzYedYxJ88SG52THjgE3Pu\n"
            + "poF3wZFjdtlwx1u4nsxlVe50FBTCN5s2FV4/8YP980zis+HtUC5pWCO3Oy6+DjSj\n"
            + "K9st+mGyzYjl3opVqcQZkHj1LPqNxBmvFpDgAtVZfdKSdyuzZpj8s5sCgYEA51rj\n"
            + "EFa/ijILp1P5vKn8b3pIfQFSsUsX5NXTy31f/2UwVV491djMyNyhtaRcrXP9CYpq\n"
            + "38o1xvUaxe2hlND/jiBjBHfsC13oUOVz8TrAzxDKAzbGLcOT2trgxMFbR8Ez+jur\n"
            + "1yQbPnoKZrB7SopAkcVqZv4ks0LLu+BLfEFXYy8CgYEApN8xXDgoRVnCqQpN53iM\n"
            + "n/c0iqjOXkTIb/jIksAdv3AAjaayP2JaOXul7RL2fJeshYiw684vbb/RNK6jJDlM\n"
            + "sH0Pt6t3tZmB2bC1KFfh7+BMdjg/p63LC6PAasa3GanObh67YADPOfoghCsOcgzd\n"
            + "6brt56fRDdHgE2P75ER/zm8CgYEArAxx6bepT3syIWiYww3itYBJofS26zP9++Zs\n"
            + "T9rX5hT5IbMo5vwIJqO0+mDVrwQfu9Wc7vnwjhm+pEy4qfPW6Hn7SNppxnY6itZo\n"
            + "J4/azOIeaM92B5h3Pv0gxBFK8YyjO8beXurx+79ENuOtfFxd8knOe/Mplcnpurjt\n"
            + "SeVJuG8CgYBxEYouOM9UuZlblXQXfudTWWf+x5CEWxyJgKaktHEh3iees1gB7ZPb\n"
            + "OewLa8AYVjqbNgS/r/aUFjpBbCov8ICxcy86SuGda10LDFX83sbyMm8XhktfyC3L\n"
            + "54irVW5mNUDcA8s9+DloeTlUlJIr8J/RADC9rpqHLaZzcdvpIMhVsw==\n"
            + "-----END RSA PRIVATE KEY-----";

    static final String TEST_CERT_PATH_PROPERTY = "certificate";
    static final String TEST_KEY_PATH_PROPERTY = "privatekey";

    // Skip test if system property, or the file it describes, cannot be found
    private String getPathStringFromSystemProperty(String property) {
        try {
            String pathStr = System.getProperty(property);
            Assume.assumeTrue("system property not set", pathStr != null && !pathStr.equals(""));
            Path path = Paths.get(pathStr);
            Assume.assumeTrue("file not found at " + pathStr, path.toFile().exists());
            return path.toString();
        } catch (Exception ex) {
            Assume.assumeNoException("cannot use '" + property + "' file", ex);
        }
        return null; // unreachable
    }

    @Test
    public void testTlsContextOptionsAPI() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            for (TlsCipherPreference pref : TlsCipherPreference.values()) {
                if (TlsContextOptions.isCipherPreferenceSupported(pref)) {
                    options.setCipherPreference(pref);
                }
            }
            Assert.assertNotEquals(0, options.getNativeHandle());
        }

        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            boolean exceptionThrown = false;

            try {
                options.setCipherPreference(TlsCipherPreference.TLS_CIPHER_KMS_PQ_TLSv1_0_2019_06);
                options.minTlsVersion = TlsVersions.TLSv1_2;
                Assert.assertEquals(0, options.getNativeHandle()); // Will never get here
            } catch (IllegalArgumentException | IllegalStateException e) {
                exceptionThrown = true;
            }

            Assert.assertTrue(exceptionThrown);
        }
    }

    @Test
    public void testPEMBadParse() {
        assertFalse(PemUtils.safeSanityCheck(BAD_CERT, 1, "CERTIFICATE"));
        Object exception = null;
        try {
            PemUtils.sanityCheck(BAD_CERT, 1, "CERTIFICATE");
        } catch (IllegalArgumentException ex) {
            exception = ex;
        }
        assertNotNull(exception);
    }

    @Test
    public void testPEMGoodParse() {
        assertTrue(PemUtils.safeSanityCheck(ROOT_CA1, 1, "CERTIFICATE"));
        try {
            PemUtils.sanityCheck(ROOT_CA1, 1, "CERTIFICATE");
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testMtls() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            options.initMtls(TEST_CERT, TEST_KEY);
            try (TlsContext tls = new TlsContext(options)) {
                assertNotNull(tls);
            } catch (Exception ex) {
                fail(ex.toString());
            }
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testMtlsFromPath() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        String certPath = getPathStringFromSystemProperty(TEST_CERT_PATH_PROPERTY);
        String keyPath = getPathStringFromSystemProperty(TEST_KEY_PATH_PROPERTY);

        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            options.initMtlsFromPath(certPath, keyPath);
            try (TlsContext tls = new TlsContext(options)) {
                assertNotNull(tls);
            } catch (Exception ex) {
                fail(ex.toString());
            }
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

    // Test should fail to create TlsContext because the file paths are not valid
    @Test
    public void testMtlsFromBadPath() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        String certPath = getPathStringFromSystemProperty(TEST_CERT_PATH_PROPERTY);
        String keyPath = getPathStringFromSystemProperty(TEST_KEY_PATH_PROPERTY);

        certPath = certPath + ".not.valid.path";
        keyPath = keyPath + ".not.valid.path";

        boolean successfullyCreatedTlsContext = false;

        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            options.initMtlsFromPath(certPath, keyPath);
            try (TlsContext tls = new TlsContext(options)) {
                successfullyCreatedTlsContext = true;
            }
        } catch (Exception ex) {
            // exceptions are expected
        }

        assertFalse(successfullyCreatedTlsContext);
    }

    @Test
    public void testOverridingTrustStore() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            options.overrideDefaultTrustStore(ROOT_CA1);
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }
}

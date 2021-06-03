package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.cal.EccKeyPair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EccKeyPairTest extends CrtTestFixture {
    public EccKeyPairTest() {}

    private static byte[] TEST_ACCESS_KEY_ID = "AKISORANDOMAASORANDOM".getBytes();
    private static byte[] TEST_SECRET_ACCESS_KEY = "q+jcrXGc+0zWN6uzclKVhvMmUsIfRPa4rlRandom".getBytes();

    static Credentials credentials = new Credentials(TEST_ACCESS_KEY_ID, TEST_SECRET_ACCESS_KEY, null);

    @Test
    public void testCreateDestroy() {
        try (EccKeyPair keyPair = EccKeyPair.newDeriveFromCredentials(credentials, EccKeyPair.AwsEccCurve.AWS_ECDSA_P256)) {
            assertNotNull(keyPair);
        }
    }

    @Test
    public void testSignMessage() {
        try (EccKeyPair keyPair = EccKeyPair.newDeriveFromCredentials(credentials, EccKeyPair.AwsEccCurve.AWS_ECDSA_P256)) {
            byte[] signatureBytes = keyPair.signMessage("".getBytes());
            assertTrue(signatureBytes.length > 0);
        }
    }
}

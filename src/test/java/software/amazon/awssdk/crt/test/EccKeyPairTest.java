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
    private static String EXPECTED_PRIVATE_KEY_BYTES_AS_HEX = "7fd3bd010c0d9c292141c2b77bfbde1042c92e6836fff749d1269ec890fca1bd";

    static Credentials credentials = new Credentials(TEST_ACCESS_KEY_ID, TEST_SECRET_ACCESS_KEY, null);

    private char toHexDigit(int value) {
        if (value < 10) {
            return (char)('0' + value);
        } else if (value < 16) {
            return (char)('a' + (value - 10));
        } else {
            throw new RuntimeException("UhOh");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < bytes.length; ++i) {
            byte value = bytes[i];
            char upper = toHexDigit((value >> 4) & 0x0F);
            builder.append(upper);
            char lower = toHexDigit(value & 0x0F);
            builder.append(lower);
        }

        return builder.toString();
    }

    @Test
    public void testCreateDestroy() {
        try (EccKeyPair keyPair = EccKeyPair.newDeriveFromCredentials(credentials, EccKeyPair.AwsEccCurve.AWS_ECDSA_P256)) {
            assertNotNull(keyPair);
        }
    }

    @Test
    public void testGetPrivateKeyBytes() {
        try (EccKeyPair keyPair = EccKeyPair.newDeriveFromCredentials(credentials, EccKeyPair.AwsEccCurve.AWS_ECDSA_P256)) {
            byte[] privateKeyBytes = keyPair.getPrivateKeyS();

            assertTrue(toHex(privateKeyBytes).equals(EXPECTED_PRIVATE_KEY_BYTES_AS_HEX));
        }
    }
}

package software.amazon.awssdk.crt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.Pkcs11Lib;

public class Pkcs11LibTest extends CrtTestFixture {

    // The PKCS#11 tests are skipped unless the following env variables are set:
    static String TEST_PKCS11_LIB = System.getenv("TEST_PKCS11_LIB");
    static String TEST_PKCS11_TOKEN_LABEL = System.getenv("TEST_PKCS11_TOKEN_LABEL");
    static String TEST_PKCS11_PIN = System.getenv("TEST_PKCS11_PIN");
    static String TEST_PKCS11_PKEY_LABEL = System.getenv("TEST_PKCS11_PKEY_LABEL");
    static String TEST_PKCS11_CERT_FILE = System.getenv("TEST_PKCS11_CERT_FILE");
    static String TEST_PKCS11_CA_FILE = System.getenv("TEST_PKCS11_CA_FILE");

    static void assumeEnvironmentSetUpForPkcs11Tests() {
        Assume.assumeNotNull(TEST_PKCS11_LIB);
        Assume.assumeNotNull(TEST_PKCS11_TOKEN_LABEL);
        Assume.assumeNotNull(TEST_PKCS11_PIN);
        Assume.assumeNotNull(TEST_PKCS11_PKEY_LABEL);
        Assume.assumeNotNull(TEST_PKCS11_CERT_FILE);
        Assume.assumeNotNull(TEST_PKCS11_CA_FILE);
    }

    public Pkcs11LibTest() {
    }

    @Test
    public void testPkcs11Lib() {
        assumeEnvironmentSetUpForPkcs11Tests();

        try (Pkcs11Lib pkcs11Lib = new Pkcs11Lib(TEST_PKCS11_LIB)) {
        }
    }

    @Test
    public void testPkcs11LibException() {
        assumeEnvironmentSetUpForPkcs11Tests();

        // check that errors during initialization bubble up as Exceptions
        assertThrows(Exception.class, () -> new Pkcs11Lib(null));
        assertThrows(Exception.class, () -> new Pkcs11Lib("obviously-invalid-path.so"));
        assertThrows(Exception.class, () -> new Pkcs11Lib(""));
    }

    @Test
    public void testPkcs11LibInitializeFinalizeBehavior() {
        assumeEnvironmentSetUpForPkcs11Tests();

        // check that the behavior enum is passed to native.
        // we expect OMIT behavior to cause failure here because no one else
        // has called C_Initialize.
        CrtRuntimeException crtException = null;
        try (Pkcs11Lib pkcs11Lib = new Pkcs11Lib(TEST_PKCS11_LIB, Pkcs11Lib.InitializeFinalizeBehavior.OMIT)) {
        } catch (Exception ex) {
            crtException = (CrtRuntimeException) ex;
        }
        assertNotNull(crtException);
        assertTrue(crtException.errorName.contains("CKR_CRYPTOKI_NOT_INITIALIZED"));
    }

}

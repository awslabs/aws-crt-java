package software.amazon.awssdk.crt.test;

import static org.junit.Assert.assertThrows;

import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.io.Pkcs11Lib;

public class Pkcs11LibTest extends CrtTestFixture {

    // The PKCS#11 tests are skipped unless the following env variables are set:
    String TEST_PKCS11_LIB = System.getenv("TEST_PKCS11_LIB");

    void assumeEnvironmentSetUpForPkcs11Tests() {
        Assume.assumeNotNull(TEST_PKCS11_LIB);
    }

    public Pkcs11LibTest() {
    }

    @Test
    public void testPkcs11LibWithInitialize() {
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
    public void testPkcs11LibWithOmitInitialize() {
        assumeEnvironmentSetUpForPkcs11Tests();

        // Check that omitInitialize constructor param can be used to skip C_Initialize.
        // To test this, we need to create 2 Pkcs11Lib instances,
        // where the 1st calls C_Initialize and the 2nd omits C_Initialize
        try (Pkcs11Lib firstLibInstance = new Pkcs11Lib(TEST_PKCS11_LIB, false /* omitInitialize */);
                Pkcs11Lib secondLibInstance = new Pkcs11Lib(TEST_PKCS11_LIB, true /* omitInitialize */)) {
        }
    }

}

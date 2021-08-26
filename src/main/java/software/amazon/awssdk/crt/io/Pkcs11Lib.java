package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * Handle to a loaded PKCS#11 library.
 * <p>
 * <b>Notes on initialization:</b> By default, {@code C_Initialize()} and
 * {@code C_Finalize()} are called when the PKCS#11 library is loaded and
 * unloaded. Call a constructor with {@link Pkcs11Lib(String, boolean)
 * omitInitialization} set to skip these calls if your application has already
 * initialized the PKCS#11 library (ex: by using the {@code SunPKCS11}
 * cryptographic provider)
 */
public class Pkcs11Lib extends CrtResource {

    /**
     * Load and initialize a PKCS#11 library.
     *
     * @param path path to PKCS#11 library.
     */
    public Pkcs11Lib(String path) throws CrtRuntimeException {
        this(path, false);
    }

    /**
     * Load a PKCS#11 library, with explicit control over initialization.
     *
     * @param path           path to PKCS#11 library.
     * @param omitInitialize if true, {@code C_Initialize()} and
     *                       {@code C_Finalize()} will not be called on the PKCS#11
     *                       library. See {@link Pkcs11Lib Notes on initialization}
     */
    public Pkcs11Lib(String path, boolean omitInitialize) throws CrtRuntimeException {
        acquireNativeHandle(pkcs11LibNew(path, omitInitialize));
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            pkcs11LibRelease(getNativeHandle());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long pkcs11LibNew(String path, boolean omitInitialize);

    private static native void pkcs11LibRelease(long nativeHandle);
}

/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;

/**
 * Handle to a loaded PKCS#11 library.
 *
 * For most use cases, a single instance of Pkcs11Lib should be used for the
 * lifetime of your application.
 */
public class Pkcs11Lib extends CrtResource {

    /**
     * Controls how Pkcs11Lib calls {@code C_Initialize()} and {@code C_Finalize()}
     * on the PKCS#11 library.
     */
    public enum InitializeFinalizeBehavior {
        /**
         * Default behavior that accommodates most use cases.
         *
         * {@code C_Initialize()} is called on creation, and "already-initialized"
         * errors are ignored. {@code C_Finalize()} is never called, just in case
         * another part of your application is still using the PKCS#11 library.
         */
        DEFAULT(0),

        /**
         * Skip calling {@code C_Initialize()} and {@code C_Finalize()}.
         *
         * Use this if your application has already initialized the PKCS#11 library, and
         * you do not want {@code C_Initialize()} called again.
         */
        OMIT(1),

        /**
         * {@code C_Initialize()} is called on creation and {@code C_Finalize()} is
         * called on cleanup.
         *
         * If {@code C_Initialize()} reports that's it's already initialized, this is
         * treated as an error. Use this if you need perfect cleanup (ex: running
         * valgrind with --leak-check).
         */
        STRICT(2);

        InitializeFinalizeBehavior(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        int nativeValue;
    }

    /**
     * Load and initialize a PKCS#11 library.
     *
     * {@code C_Initialize()} and {@code C_Finalize()} are called on the PKCS#11
     * library in the {@link InitializeFinalizeBehavior#DEFAULT DEFAULT} way.
     *
     * @param path path to PKCS#11 library.
     */
    public Pkcs11Lib(String path) {
        this(path, InitializeFinalizeBehavior.DEFAULT);
    }

    /**
     * Load a PKCS#11 library, specifying how {@code C_Initialize()} and
     * {@code C_Finalize()} will be called.
     *
     * @param path                       path to PKCS#11 library.
     * @param initializeFinalizeBehavior specifies how {@code C_Initialize()} and
     *                                   {@code C_Finalize()} will be called on the
     *                                   PKCS#11 library.
     */
    public Pkcs11Lib(String path, InitializeFinalizeBehavior initializeFinalizeBehavior) {
        acquireNativeHandle(pkcs11LibNew(path, initializeFinalizeBehavior.nativeValue));
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
    private static native long pkcs11LibNew(String path, int initializeFinalizeBehavior);

    private static native void pkcs11LibRelease(long nativeHandle);
}

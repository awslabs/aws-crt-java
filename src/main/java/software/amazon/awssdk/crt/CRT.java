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
package software.amazon.awssdk.crt;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
/**
 * This class is responsible for loading the aws-crt-jni shared lib for the current
 * platform out of aws-crt-java.jar. One instance of this class has to be created
 * somewhere to invoke the static initialization block which will load the shared lib
 */
public final class CRT {
    private static final String CRT_LIB_NAME = "aws-crt-jni";
    public static final int AWS_CRT_SUCCESS = 0;
    private static final CrtPlatform s_platform;

    static {
        // Scan for and invoke any platform specific initialization
        s_platform = findPlatformImpl();
        jvmInit();
        try {
            // If the lib is already present/loaded or is in java.library.path, just use it
            System.loadLibrary(CRT_LIB_NAME);
        } catch (UnsatisfiedLinkError e) {
            // otherwise, load from the jar this class is in
            loadLibraryFromJar();
        }

        // Initialize the CRT
        int memoryTracingLevel = 0;
        try {
            memoryTracingLevel = Integer.parseInt(System.getProperty("aws.crt.memory.tracing"));
        } catch (Exception ex) {}
        awsCrtInit(memoryTracingLevel, System.getProperty("aws.crt.debugwait") != null);

        try {
            Log.initLoggingFromSystemProperties();
        } catch (IllegalArgumentException e) {
            ;
        }
    }

    public static class UnknownPlatformException extends RuntimeException {
        UnknownPlatformException(String message) {
            super(message);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    private static boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static String getOSIdentifier() throws UnknownPlatformException {
        if (isAndroid()) {
            return "android";
        }

        CrtPlatform platform = getPlatformImpl();
        String name = normalize(platform != null ? platform.getOSIdentifier() : System.getProperty("os.name"));

        if (name.contains("windows")) {
            return "windows";
        } else if (name.contains("linux")) {
            return "linux";
        } else if (name.contains("freebsd")) {
            return "freebsd";
        } else if (name.contains("macosx")) {
            return "osx";
        } else if (name.contains("sun os") || name.contains("sunos") || name.contains("solaris")) {
            return "solaris";
        }

        throw new UnknownPlatformException("AWS CRT: OS not supported: " + name);
    }

    public static String getArchIdentifier() throws UnknownPlatformException {
        CrtPlatform platform = getPlatformImpl();
        String arch = normalize(platform != null ? platform.getArchIdentifier() : System.getProperty("os.arch"));

        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64|x86_64)$")) {
            return "x86_64";
        } else if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return (getOSIdentifier() == "android") ? "x86": "x86_32";
        } else if (arch.startsWith("armeabi")) {
            if (getOSIdentifier() == "android") {
                return "armeabi-v7a";
            }
            if (arch.contains("v7")) {
                return "armv7";
            } else {
                return "armv6";
            }
        } else if (arch.startsWith("arm64") || arch.startsWith("aarch64")) {
            return (getOSIdentifier() == "android") ? "arm64-v8a": "armv8";
        } else if (arch.equals("arm")) {
           return "armv6";
        }

        throw new UnknownPlatformException("AWS CRT: architecture not supported: " + arch);
    }

    private static void loadLibraryFromJar() {
        try {
            // Check java.io.tmpdir
            String tmpdir = System.getProperty("java.io.tmpdir");
            Path tmpdirPath;
            try {
                tmpdirPath = Paths.get(tmpdir).toAbsolutePath().normalize();
                File tmpdirFile = tmpdirPath.toFile();
                if (tmpdirFile.exists()) {
                    if (!tmpdirFile.isDirectory()) {
                        throw new NotDirectoryException(tmpdirPath.toString());
                    }
                } else {
                    Files.createDirectories(tmpdirPath);
                }

                if (!tmpdirFile.canRead() || !tmpdirFile.canWrite()) {
                    throw new AccessDeniedException(tmpdirPath.toString());
                }
            }
            catch (Exception ex) {
                String msg = "java.io.tmpdir=\"" + tmpdir + "\": Invalid directory";
                throw new IOException(msg, ex);
            }

            // Prefix the lib
            String prefix = "AWSCRT_" + new Date().getTime();
            String libraryName = System.mapLibraryName(CRT_LIB_NAME);
            String libraryPath = "/" + getOSIdentifier() + "/" + getArchIdentifier() + "/" + libraryName;
            Path libTempPath = Files.createTempFile(tmpdirPath, prefix, libraryName);

            // open a stream to read the shared lib contents from this JAR
            try (InputStream in = CRT.class.getResourceAsStream(libraryPath)) {
                if (in == null) {
                    throw new IOException("Unable to open library in jar for AWS CRT: " + libraryPath);
                }
                // Copy from jar stream to temp file
                Files.deleteIfExists(libTempPath);
                Files.copy(in, libTempPath);
            }

            File tempSharedLib = libTempPath.toFile();
            if (!tempSharedLib.setExecutable(true)) {
                throw new CrtRuntimeException("Unable to make shared library executable");
            }
            if (!tempSharedLib.setWritable(false)) {
                throw new CrtRuntimeException("Unable to make shared library read-only");
            }
            if (!tempSharedLib.setReadable(true)) {
                throw new CrtRuntimeException("Unable to make shared library readable");
            }

            // Ensure that the shared lib will be destroyed when java exits
            tempSharedLib.deleteOnExit();

            // load the shared lib from the temp path
            System.load(libTempPath.toString());
        }
        catch (CrtRuntimeException crtex) {
            System.err.println("Unable to initialize AWS CRT: " + crtex.toString());
            crtex.printStackTrace();
        }
        catch (UnknownPlatformException upe) {
            System.err.println("Unable to determine platform for AWS CRT: " + upe.toString());
            upe.printStackTrace();
        }
        catch (Exception ex) {
            System.err.println("Unable to unpack AWS CRT lib: " + ex.toString());
            ex.printStackTrace();
        }
    }


    private static CrtPlatform findPlatformImpl() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String[] platforms = new String[] {
            // Search for test impl first, fall back to crt
            String.format("software.amazon.awssdk.crt.test.%s.CrtPlatformImpl", getOSIdentifier()),
            String.format("software.amazon.awssdk.crt.%s.CrtPlatformImpl", getOSIdentifier()),
        };
        for (String platformImpl : platforms) {
            try {
                Class<?> platformClass = classLoader.loadClass(platformImpl);
                CrtPlatform instance = (CrtPlatform) platformClass.newInstance();
                return instance;
            } catch (ClassNotFoundException ex) {
                // IGNORED
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new CrtRuntimeException(ex.toString());
            }
        }
        return null;
    }


    public static CrtPlatform getPlatformImpl() {
        return s_platform;
    }

    private static void jvmInit() {
        CrtPlatform platform = getPlatformImpl();
        if (platform != null) {
            platform.jvmInit();
        }
    }

    // Called internally when bootstrapping the CRT, allows native code to do any static initialization it needs
    private static native void awsCrtInit(int memoryTracingLevel, boolean debugWait) throws CrtRuntimeException;

    /**
     * Returns the last error on the current thread.
     * @return Last error code recorded in this thread
     */
    public static native int awsLastError();

    /**
     * Given an integer error code from an internal operation
     * @param errorCode An error code returned from an exception or other native function call
     * @return A user-friendly description of the error
     */
    public static native String awsErrorString(int errorCode);

    /**
     * Given an integer error code from an internal operation
     *
     * @param errorCode An error code returned from an exception or other native
     *                  function call
     * @return A string identifier for the error
     */
    public static native String awsErrorName(int errorCode);

    /**
     * @return The number of bytes allocated in native resources. If aws.crt.memory.tracing is 1 or 2, this will
     *         be a non-zero value. Otherwise, no tracing will be done, and the value will always be 0
     */
    public static long nativeMemory() {
        return awsNativeMemory();
    }

    private static native long awsNativeMemory();


};

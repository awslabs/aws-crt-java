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
import java.nio.file.*;
import java.util.*;

import software.amazon.awssdk.crt.Log;

/**
 * This class is responsible for loading the aws-crt-jni shared lib for the current
 * platform out of aws-crt-java.jar. One instance of this class has to be created
 * somewhere to invoke the static initialization block which will load the shared lib
 */
public final class CRT {
    private static final String CRT_LIB_NAME = "aws-crt-jni";
    public static final int AWS_CRT_SUCCESS = 0;

    static {
        try {
            // If the lib is already present/loaded or is in java.library.path, just use it
            System.loadLibrary(CRT_LIB_NAME);
        } catch (UnsatisfiedLinkError e) {
            // otherwise, load from the jar this class is in
            loadLibraryFromJar();
        }
    }

    public static class UnknownPlatformException extends Exception {
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

    private static String getOSIdentifier() throws UnknownPlatformException {
        String name = normalize(System.getProperty("os.name"));

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
        } else if (name.contains("android")) {
            return "android";
        }

        throw new UnknownPlatformException("AWS CRT: OS not supported: " + name);
    }

    private static String getArchIdentifier() throws UnknownPlatformException {
        String arch = normalize(System.getProperty("os.arch"));
        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        } else if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        } else if (arch.startsWith("armeabi")) {
            if (arch.contains("v7")) {
                return "armv7";
            } else {
                return "armv6";
            }
        } else if (arch.startsWith("arm64")) {
            if (arch.contains("v8")) {
                return "armv8";
            }
        } else if (arch.startsWith("aarch64")) {
           return "armv8";
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

    // Called internally when bootstrapping the CRT, allows native code to do any static initialization it needs
    private static native void awsCrtInit(int memoryTracingLevel, boolean debugWait) throws CrtRuntimeException;

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
     * @return The number of bytes allocated in native resources. If aws.crt.memory.tracing > 0, this will
     *         be a non-zero value. Otherwise, no tracing will be done, and the value will always be 0
     */
    public static long nativeMemory() {
        return awsNativeMemory();
    }

    private static native long awsNativeMemory();


};

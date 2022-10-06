/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * This class is responsible for loading the aws-crt-jni shared lib for the
 * current platform out of aws-crt-java.jar. One instance of this class has to
 * be created somewhere to invoke the static initialization block which will
 * load the shared lib
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
        } catch (Exception ex) {
        }
        boolean debugWait = System.getProperty("aws.crt.debugwait") != null;
        boolean strictShutdown = System.getProperty("aws.crt.strictshutdown") != null;
        awsCrtInit(memoryTracingLevel, debugWait, strictShutdown);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                CRT.onJvmShutdown();
            }
        });

        try {
            Log.initLoggingFromSystemProperties();
        } catch (IllegalArgumentException e) {
            ;
        }
    }

    /**
     * Exception thrown when we can't detect what platform we're running on and thus can't figure out
     * the native library name/path to load.
     */
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

    /**
     * @return a string describing the detected platform the CRT is executing on
     */
    public static String getOSIdentifier() throws UnknownPlatformException {

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

    /**
     * @return a string describing the detected architecture the CRT is executing on
     */
    public static String getArchIdentifier() throws UnknownPlatformException {
        CrtPlatform platform = getPlatformImpl();
        String arch = normalize(platform != null ? platform.getArchIdentifier() : System.getProperty("os.arch"));

        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64|x86_64)$")) {
            return "x86_64";
        } else if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        } else if (arch.startsWith("armeabi")) {
            if (arch.contains("v7")) {
                return "armv7";
            } else {
                return "armv6";
            }
        } else if (arch.startsWith("arm64") || arch.startsWith("aarch64")) {
            return "armv8";
        } else if (arch.equals("arm")) {
            return "armv6";
        }

        throw new UnknownPlatformException("AWS CRT: architecture not supported: " + arch);
    }

    private static final String NON_LINUX_RUNTIME_TAG = "sys";
    private static final String MUSL_RUNTIME_TAG = "musl";
    private static final String GLIBC_RUNTIME_TAG = "glibc";

    public static String getCRuntime(String osIdentifier) {
        if (!osIdentifier.equals("linux")) {
            return NON_LINUX_RUNTIME_TAG;
        }

        Runtime rt = Runtime.getRuntime();
        String[] commands = {"ldd", "--version"};
        try {
            java.lang.Process proc = rt.exec(commands);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                outputBuilder.append(line);
            }

            String lddOutput = outputBuilder.toString();
            if (lddOutput.contains("musl")) {
                return MUSL_RUNTIME_TAG;
            } else {
                return GLIBC_RUNTIME_TAG;
            }
        } catch (IOException io) {
            return GLIBC_RUNTIME_TAG;
        }
    }

    private static void extractAndLoadLibrary(String path) {
        try {
            // Check java.io.tmpdir
            String tmpdirPath;
            File tmpdirFile;
            try {
                tmpdirFile = new File(path).getAbsoluteFile();
                tmpdirPath = tmpdirFile.getAbsolutePath();
                if (tmpdirFile.exists()) {
                    if (!tmpdirFile.isDirectory()) {
                        throw new IOException("not a directory: " + tmpdirPath);
                    }
                } else {
                    tmpdirFile.mkdirs();
                }

                if (!tmpdirFile.canRead() || !tmpdirFile.canWrite()) {
                    throw new IOException("access denied: " + tmpdirPath);
                }
            } catch (Exception ex) {
                String msg = "Invalid directory: " + path;
                throw new IOException(msg, ex);
            }

            String libraryName = System.mapLibraryName(CRT_LIB_NAME);

            // Prefix the lib we'll extract to disk
            String tempSharedLibPrefix = "AWSCRT_";

            File tempSharedLib = File.createTempFile(tempSharedLibPrefix, libraryName, tmpdirFile);
            if (!tempSharedLib.setExecutable(true, true)) {
                throw new CrtRuntimeException("Unable to make shared library executable by owner only");
            }
            if (!tempSharedLib.setWritable(true, true)) {
                throw new CrtRuntimeException("Unable to make shared library writeable by owner only");
            }
            if (!tempSharedLib.setReadable(true, true)) {
                throw new CrtRuntimeException("Unable to make shared library readable by owner only");
            }

			// The temp lib file should be deleted when we're done with it.
			// Ask Java to try and delete it on exit. We call this immediately
			// so that if anything goes wrong writing the file to disk, or
			// loading it as a shared lib, it will still get cleaned up.
			tempSharedLib.deleteOnExit();

            // Unfortunately File.deleteOnExit() won't work on Windows, where
            // files cannot be deleted while they're in use. On Windows, once
            // our .dll is loaded, it can't be deleted by this process.
            //
            // The Windows-only solution to this problem is to scan on startup
            // for old instances of the .dll and try to delete them. If another
            // process is still using the .dll, the delete will fail, which is fine.
            String os = getOSIdentifier();
            if (os.equals("windows")) {
                tryDeleteOldLibrariesFromTempDir(tmpdirFile, tempSharedLibPrefix, libraryName);
            }

            // open a stream to read the shared lib contents from this JAR
            String libResourcePath = "/" + os + "/" + getArchIdentifier() + "/" +  getCRuntime(os) + "/" + libraryName;
            try (InputStream in = CRT.class.getResourceAsStream(libResourcePath)) {
                if (in == null) {
                    throw new IOException("Unable to open library in jar for AWS CRT: " + libResourcePath);
                }

                // Copy from jar stream to temp file
                try (FileOutputStream out = new FileOutputStream(tempSharedLib)) {
                    int read;
                    byte [] bytes = new byte[1024];
                    while ((read = in.read(bytes)) != -1){
                        out.write(bytes, 0, read);
                    }
                }
            }

            if (!tempSharedLib.setWritable(false)) {
                throw new CrtRuntimeException("Unable to make shared library read-only");
            }

            // load the shared lib from the temp path
            System.load(tempSharedLib.getAbsolutePath());
        } catch (CrtRuntimeException crtex) {
            System.err.println("Unable to initialize AWS CRT: " + crtex);
            crtex.printStackTrace();
            throw crtex;
        } catch (UnknownPlatformException upe) {
            System.err.println("Unable to determine platform for AWS CRT: " + upe);
            upe.printStackTrace();
            CrtRuntimeException rex = new CrtRuntimeException("Unable to determine platform for AWS CRT");
            rex.initCause(upe);
            throw rex;
        } catch (Exception ex) {
            System.err.println("Unable to unpack AWS CRT lib: " + ex);
            ex.printStackTrace();
            CrtRuntimeException rex = new CrtRuntimeException("Unable to unpack AWS CRT library");
            rex.initCause(ex);
            throw rex;
        }
    }

    private static void loadLibraryFromJar() {
        // By default, just try java.io.tmpdir
        List<String> pathsToTry = new LinkedList<>();
        pathsToTry.add(System.getProperty("java.io.tmpdir"));

        // If aws.crt.lib.dir is specified, try that first
        String overrideLibDir = System.getProperty("aws.crt.lib.dir");
        if (overrideLibDir != null) {
            pathsToTry.add(0, overrideLibDir);
        }

        List<Exception> exceptions = new LinkedList<>();
        for (String path : pathsToTry) {
            try {
                extractAndLoadLibrary(path);
                return;
            } catch (CrtRuntimeException ex) {
                exceptions.add(ex);
            }
        }

        // Aggregate the exceptions in order and throw a single failure exception
        StringBuilder failureMessage = new StringBuilder();
        exceptions.stream().map(Exception::toString).forEach(failureMessage::append);
        throw new CrtRuntimeException(failureMessage.toString());
    }

    // Try to delete old CRT libraries that were extracted to the temp dir by previous runs.
    private static void tryDeleteOldLibrariesFromTempDir(File tmpDir, String libNamePrefix, String libNameSuffix) {
        try {
            File[] oldLibs = tmpDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(libNamePrefix) && name.endsWith(libNameSuffix);
                }
            });

            // Don't delete files that are too new.
            // We don't want to delete another process's lib in the
            // millisecond between the file being written to disk,
            // and the file being loaded as a shared lib.
            long aFewMomentsAgo = System.currentTimeMillis() - 10_000; // 10sec
            for (File oldLib : oldLibs) {
                try {
                    if (oldLib.lastModified() < aFewMomentsAgo) {
                        oldLib.delete();
                    }
                } catch (Exception e) {}
            }
        } catch (Exception e) {}
    }

    private static CrtPlatform findPlatformImpl() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String[] platforms = new String[] {
                // Search for test impl first, fall back to crt
                String.format("software.amazon.awssdk.crt.test.%s.CrtPlatformImpl", getOSIdentifier()),
                String.format("software.amazon.awssdk.crt.%s.CrtPlatformImpl", getOSIdentifier()), };
        for (String platformImpl : platforms) {
            try {
                Class<?> platformClass = classLoader.loadClass(platformImpl);
                CrtPlatform instance = (CrtPlatform) platformClass.getDeclaredConstructor().newInstance();
                return instance;
            } catch (ClassNotFoundException ex) {
                // IGNORED
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
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

    // Called internally when bootstrapping the CRT, allows native code to do any
    // static initialization it needs
    private static native void awsCrtInit(int memoryTracingLevel, boolean debugWait, boolean strictShutdown)
            throws CrtRuntimeException;

    /**
     * Returns the last error on the current thread.
     *
     * @return Last error code recorded in this thread
     */
    public static native int awsLastError();

    /**
     * Given an integer error code from an internal operation
     *
     * @param errorCode An error code returned from an exception or other native
     *                  function call
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
     * @return The number of bytes allocated in native resources. If
     *         aws.crt.memory.tracing is 1 or 2, this will be a non-zero value.
     *         Otherwise, no tracing will be done, and the value will always be 0
     */
    public static long nativeMemory() {
        return awsNativeMemory();
    }

    /**
     * Dump info to logs about all memory currently allocated by native resources.
     * The following system properties must be set to see a dump:
     * aws.crt.memory.tracing must be 1 or 2
     * aws.crt.log.level must be "Trace"
     */
    public static native void dumpNativeMemory();

    private static native long awsNativeMemory();

    static void testJniException(boolean throwException) {
        if (throwException) {
            throw new RuntimeException("Testing");
        }
    }

    public static void checkJniExceptionContract(boolean clearException) {
        nativeCheckJniExceptionContract(clearException);
    }

    private static native void nativeCheckJniExceptionContract(boolean clearException);

    private static native void onJvmShutdown();
};

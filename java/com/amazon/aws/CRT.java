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
package com.amazon.aws;

import java.io.*;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.*;


public class CRT {
    private static final String CRT_LIB_NAME = "aws-crt-jni";

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

    // Utility for dumping the contents of jars, debugging only
    private static void enumerateResources() throws URISyntaxException, IOException {
        Enumeration<URL> en = CRT.class.getClassLoader().getResources("META-INF");
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            System.out.println(url.toString());
            JarURLConnection urlcon = (JarURLConnection) (url.openConnection());
            try (JarFile jar = urlcon.getJarFile();) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement().getName();
                    System.out.println("  " + entry);
                }
            }
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
        } else if (name.contains("mac os")) {
            return "osx";
        } else if (name.contains("sun os") || name.contains("sunos") || name.contains("solaris")) {
            return "solaris";
        } else if (name.contains("android")) {
            return "android";
        }
            
        throw new UnknownPlatformException("AWS CRT: OS not supported");
    }

    private static String getArchIdentifier() throws UnknownPlatformException {
        String arch = normalize(System.getProperty("os.arch"));
        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        } else if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        } else if (arch.matches("^(arm|arm32)$")) {
            return "arm_32";
        } 
        
        throw new UnknownPlatformException("AWS CRT: architecture not supported");
    }

    private static void loadLibraryFromJar() {
        try {
            // Prefix the lib
            String prefix = "AWSCRT_" + new Date().getTime();
            String platformName = System.mapLibraryName(CRT_LIB_NAME);
            String extension = platformName.substring(platformName.lastIndexOf('.'));
            String libraryName = CRT_LIB_NAME + extension;
            String libraryPath = "/" + getOSIdentifier() + "/" + getArchIdentifier();
            Path libTempPath = Paths.get(System.getProperty("java.io.tmpdir"), prefix + libraryName);

            // open a stream to read the shared lib contents from this JAR
            InputStream in = CRT.class.getResourceAsStream(libraryPath);
            // Copy from jar stream to file stream
            Files.copy(in, libTempPath);
            // load the shared lib from the temp path
            System.load(libTempPath.toString());
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
};

class CrtResource implements AutoCloseable {
    public CrtResource()
    {
    }

    @Override
    public void close() {
        System.out.println("CrtResource CLOSED");
    }

    public native void doIt();
};

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
    private static final String CRT_LIB_NAME = "aws-crt-java";

    static {
        try {
            // If the lib is already present/loaded or is in java.library.path, just use it
            System.loadLibrary(CRT_LIB_NAME);
        } catch (UnsatisfiedLinkError e) {
            loadLibraryFromJar();
        }
    }

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

    private static void loadLibraryFromJar() {
        // Prefix the lib
        String prefix = "AWSCRT_" + new Date().getTime();
        String platformName = System.mapLibraryName(CRT_LIB_NAME);
        String extension = platformName.substring(platformName.lastIndexOf('.'));
        String libraryName = CRT_LIB_NAME + extension;
        Path libTempPath = Paths.get(System.getProperty("java.io.tmpdir"), prefix + libraryName);
        
        // open a stream to read the shared lib contents from this JAR
        System.err.println("READING FROM " + libraryName);
        InputStream in = CRT.class.getResourceAsStream("/" + libraryName);
        if (in == null) {
            System.err.println("Couldn't read from " + libraryName);
        }
        // Copy from jar stream to file stream
        try {
            enumerateResources();
            System.err.println("ATTEMPTING TO WRITE TO " + libTempPath);
            Files.copy(in, libTempPath);
        }
        catch (Exception ex) {
            System.err.println("Unable to unpack AWS CRT lib: " + ex.toString());
            ex.printStackTrace();
        }

        System.load(libTempPath.toString());
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

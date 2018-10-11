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

import java.util.StringTokenizer;

class BasicResource implements AutoCloseable {
    public BasicResource()
    {
        String libraryName = "aws-crt-java-test";
        String cwd = System.getProperty("user.dir");
        try {
            System.loadLibrary(libraryName);
        }
        catch (java.lang.UnsatisfiedLinkError ex) {
            System.err.println(ex.toString());
            String path = System.getProperty("java.library.path");
            System.err.println("Could not load " + libraryName + " from:");
            StringTokenizer parser = new StringTokenizer(path, ":");
            while (parser.hasMoreTokens()) {
                System.err.println("    " + parser.nextToken());
            }
            System.err.println("Current directory: " + cwd);
        }
    }

    @Override
    public void close() {
        System.out.println("CLOSED");
    }

    public native void doIt();
};

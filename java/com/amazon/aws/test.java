
package com.amazon.aws;

import java.util.StringTokenizer;

class Test implements AutoCloseable {
    public Test()
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

    public static native void doIt();

    public static void main(String[] args) {
        System.out.println("I LIVE");
        try (Test test = new Test()) {
            doIt();
        }
    }
};

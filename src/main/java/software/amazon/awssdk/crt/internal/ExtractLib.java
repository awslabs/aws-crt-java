package software.amazon.awssdk.crt.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CRT.UnknownPlatformException;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * Helper to extract JNI shared lib from Jar.
 * Internal API, not for external usage.
 */
public class ExtractLib {
    private static final String CRT_LIB_NAME = "aws-crt-jni";

    public static void extractLibrary(File extractFile) {

        try {
            if (!extractFile.setExecutable(true, true)) {
                throw new CrtRuntimeException("Unable to make shared library executable by owner only");
            }
            if (!extractFile.setWritable(true, true)) {
                throw new CrtRuntimeException("Unable to make shared library writeable by owner only");
            }
            if (!extractFile.setReadable(true, true)) {
                throw new CrtRuntimeException("Unable to make shared library readable by owner only");
            }
            String libraryName = System.mapLibraryName(CRT_LIB_NAME);
            String os = CRT.getOSIdentifier();
            // open a stream to read the shared lib contents from this JAR
            String libResourcePath = "/" + os + "/" + CRT.getArchIdentifier() + "/" +  CRT.getCRuntime(os) + "/" + libraryName;
            // Check whether there is a platform specific resource path to use
            CrtPlatform platform = CRT.getPlatformImpl();
            if (platform != null){
                String platformLibResourcePath = platform.getResourcePath(CRT.getCRuntime(os), libraryName);
                if (platformLibResourcePath != null){
                    libResourcePath = platformLibResourcePath;
                }
            }
            try (InputStream in = CRT.class.getResourceAsStream(libResourcePath)) {
                if (in == null) {
                    throw new IOException("Unable to open library in jar for AWS CRT: " + libResourcePath);
                }

                // Copy from jar stream to temp file
                try (FileOutputStream out = new FileOutputStream(extractFile)) {
                    int read;
                    byte [] bytes = new byte[1024];
                    while ((read = in.read(bytes)) != -1){
                        out.write(bytes, 0, read);
                    }
                }
            }
            if (!extractFile.setWritable(false)) {
                throw new CrtRuntimeException("Unable to make shared library read-only");
            }
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

    /**
     * Extract the CRT JNI library on current platform to a specific path.
     */
    public static void extractLibrary(String path) {
        String libraryName = System.mapLibraryName(CRT_LIB_NAME);
        File extractFile = new File(path, libraryName);
        try {
            extractFile.createNewFile();
        } catch (Exception ex) {
            CrtRuntimeException rex = new CrtRuntimeException("Unable to create file on path:" + extractFile.getAbsolutePath());
            rex.initCause(ex);
            throw rex;
        }
        extractLibrary(extractFile);
    }
}

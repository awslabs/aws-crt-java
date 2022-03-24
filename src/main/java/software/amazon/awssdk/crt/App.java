package software.amazon.awssdk.crt;

public class App {
    public static void main(String[] args) {
        // Print a test message so we know that the application started
        System.out.println("Hello, World!");

        // Print a message from CRT that guarantees CRT's static block was executed and that a valid value was produced
        //   for the OS identifier
        // Without META-INF/native-lib/jni-config.json and META-INF/native-lib/resource-config.json this will fail with
        //   an error like this:
        //
        // Unable to unpack AWS CRT lib: java.io.IOException: Unable to open library in jar for AWS CRT: /osx/x86_64/libaws-crt-jni.dylib
        // java.io.IOException: Unable to open library in jar for AWS CRT: /osx/x86_64/libaws-crt-jni.dylib
        System.out.println(CRT.getOSIdentifier());
    }
}

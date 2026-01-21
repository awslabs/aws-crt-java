# Prevent shrinking and minification of aws-crt-android library
-keep class software.amazon.awssdk.crt.** { *; }
-keepclassmembers class software.amazon.awssdk.crt.** { *; }

# Keep CrtResource subclasses (native resource management)
-keep class * extends software.amazon.awssdk.crt.CrtResource

# Keep all interfaces (callback interfaces used by native code)
-keep interface software.amazon.awssdk.crt.** { *; }

# Keep enum methods (used by JNI)
-keepclassmembers enum software.amazon.awssdk.crt.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}
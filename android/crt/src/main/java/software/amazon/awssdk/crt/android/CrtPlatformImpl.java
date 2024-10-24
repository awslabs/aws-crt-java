
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.android;

import software.amazon.awssdk.crt.BuildConfig;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.utils.PackageInfo;
import java.util.Locale;
import android.os.Build;

public class CrtPlatformImpl extends CrtPlatform {
    public String getOSIdentifier() {
        return "android";
    }

    public String getArchIdentifier() {
        return System.getProperty("os.arch");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }


    public String getArch() {
        // Internal folder structure of Android aar libraries are different from jar libraries and arch must
        // be retrieved using Build class instead of a system property.
        for (String arch: Build.SUPPORTED_ABIS) {
            if (arch.matches("^(x8664|amd64|ia32e|em64t|x64|x86_64)$")) {
                return "x86_64";
            } else if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
                return "x86";
            } else if (arch.startsWith("armeabi")) {
                if (arch.contains("v7")) {
                    return "armeabi-v7a";
                } 
            } else if (arch.startsWith("arm64") || arch.startsWith("aarch64") || arch.equals("armv8a")) {
                 return "arm64-v8a";
            } else if (arch.equals("armv7l")) {
                return "armeabi-v7a";
            } 
        }
        throw new RuntimeException("AWS CRT: architecture not supported on Android: " + arch);
    }

    public String getResourcePath(String cRuntime, String libraryName) {
        return "/lib/" + getArch() + "/" + libraryName;
    }

    public PackageInfo.Version getVersion() {
        return new PackageInfo.Version(BuildConfig.VERSION_NAME);
    }
}

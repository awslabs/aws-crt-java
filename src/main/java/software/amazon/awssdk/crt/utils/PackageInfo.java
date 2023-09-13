
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.utils;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtPlatform;

/**
 * Class that wraps version and package introspection
 */
public final class PackageInfo {

    /**
     * Class representing the introspected semantic version of the CRT library
     */
    public static class Version {
        private final String version;
        public final int major;
        public final int minor;
        public final int patch;
        public final String tag;

        public Version(String v) {
            version = v != null ? v : "UNKNOWN";
            
            int dashIdx = version.indexOf('-');
            if (dashIdx != -1) {
                tag = version.substring(dashIdx + 1);
            } else {
                tag = "";
            }

            v = version.replace("-.+$", ""); // remove -SNAPSHOT or any other suffix
            String[] parts = v.split("\\.", 3);

            int len = parts.length;
            patch = len > 2 ? maybeParse(parts[2]) : 0;
            minor = len > 1 ? maybeParse(parts[1]) : 0;
            major = len > 0 ? maybeParse(parts[0]) : 0;
        }

        @Override
        public String toString() {
            return version;
        }
    }

    private static int maybeParse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * the introspected semantic version of the CRT library instance
     */
    public Version version;

    /**
     * Default constructor
     */
    public PackageInfo() {
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            version = platform.getVersion();
            return;
        }

        Package pkg = CRT.class.getPackage();
        String pkgVersion = pkg.getSpecificationVersion();
        if (pkgVersion == null) {
            pkgVersion = pkg.getImplementationVersion();
        }
        // There is no JAR/manifest during internal tests
        if (pkgVersion == null) {
            pkgVersion = "0.0.0-UNITTEST";
        }
        version = new Version(pkgVersion);
    }
    
}

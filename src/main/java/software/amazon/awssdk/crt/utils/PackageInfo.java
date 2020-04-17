
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
package software.amazon.awssdk.crt.utils;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtPlatform;

public final class PackageInfo {
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
    
    public Version version;

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

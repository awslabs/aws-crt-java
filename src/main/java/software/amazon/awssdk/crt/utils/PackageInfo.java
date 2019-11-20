
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

public final class PackageInfo {
    public class Version {
        private final String version;
        public final int major;
        public final int minor;
        public final int patch;

        public Version(String v) {
            version = v != null ? v : "UNKNOWN";
            
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
        version = new Version(PackageInfo.class.getPackage().getImplementationVersion());
    }
    
}


/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.awssdk.crt.android;

import software.amazon.awssdk.crt.BuildConfig;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.utils.PackageInfo;

public class CrtPlatformImpl extends CrtPlatform {
    public String getOSIdentifier() {
        return "android";
    }

    public PackageInfo.Version getVersion() {
        return new PackageInfo.Version(BuildConfig.VERSION_NAME);
    }
}
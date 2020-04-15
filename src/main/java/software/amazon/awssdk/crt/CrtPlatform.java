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

package software.amazon.awssdk.crt;

public abstract class CrtPlatform {
    // Called before any native code is loaded, just to configure the JVM
    public void jvmInit() {}
    
    // Called before every JUnit test
    public void testSetup(Object context) {}
    
    // Called after every JUnit test
    public void testTearDown(Object context) {}
}

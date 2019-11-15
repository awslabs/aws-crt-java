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

package software.amazon.awssdk.crt.test;


import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.io.ByteArrayAwsInputStream;

public class AwsInputStreamTest {
    static private String STREAM_VALUE = "It's a little late to be binding this";

    public AwsInputStreamTest() {}

    @Test
    public void testCreateDestroyByteArray() {
        try (ByteArrayAwsInputStream stream = new ByteArrayAwsInputStream(STREAM_VALUE.getBytes())) {
            assertNotNull(stream);
            assertTrue(stream.getNativeHandle() != 0);
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

};

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
import software.amazon.awssdk.crt.http.HttpRequest2;

public class HttpRequestResponse2Test {

    private static final String METHOD = "GET";
    private static final String PATH = "/index.html";

    public HttpRequestResponse2Test() {}

    @Test
    public void testCreateDestroyEmpty() {
        try (HttpRequest2 request = new HttpRequest2()) {
            assertNotNull(request);
            assertTrue(request.getNativeHandle() != 0);

            request.setMethod(METHOD);
            request.setPath(PATH);


            String method = request.getMethod();
            assertTrue(method.equals(METHOD));

            String path = request.getPath();
            assertTrue(path.equals(PATH));

        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

};

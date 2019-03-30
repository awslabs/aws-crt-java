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

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.Logger;

public class LoggingTest {
    public LoggingTest() {
    }

    @Test
    public void testLogging() {
        try {
            File tempFile = File.createTempFile("AWSCRT-LoggingTest", ".log");
           //tempFile.deleteOnExit();
            Logger.configure(tempFile, Logger.Level.TRACE);
            //FileOutputStream logStream = new FileOutputStream(tempFile);
            //Logger.configure(logStream.getFD(), Logger.Level.TRACE);
            EventLoopGroup elg = new EventLoopGroup(1);
            elg.close();

            //logStream.flush();
            //logStream.close();
            Logger.flush();

            String fileContents = new String(Files.readAllBytes(tempFile.toPath()));
            assertTrue(fileContents.length() > 0);
        } catch (CrtRuntimeException | IOException ex) {
            fail(ex.getMessage());
        }
    }
};

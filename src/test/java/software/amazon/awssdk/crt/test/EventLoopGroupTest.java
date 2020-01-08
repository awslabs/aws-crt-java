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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.lang.Thread;
import java.util.function.Function;
import java.nio.file.Paths;

public class EventLoopGroupTest {
    public EventLoopGroupTest() {}

    @Test
    public void testCreateDestroy() {
        try (EventLoopGroup elg = new EventLoopGroup(1)) {
            assertNotNull(elg);
            assertTrue(!elg.isNull());
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSloppyExit() {
        int numTries = 100;
        for (int i = 0; i < numTries; ++i) {
            String javaPath = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                javaPath = javaPath + ".exe";
            }

            try {
                String classPath = System.getProperty("java.class.path");
                String mainClass = EventLoopGroupTest.class.getName();
                ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-cp", classPath, mainClass)
                    .redirectErrorStream(true);
                Process process = processBuilder.start();

                process.waitFor();

                // If process failed, print its stderr and stdout
                if (process.exitValue() != 0) {
                    System.out.println(String.format("--- Error running:%s exit-code:%d try:%d/%d ---",
                        processBuilder.command().toString(), process.exitValue(), i+1, numTries));

                    BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        System.out.println(line);
                    }

                    System.out.println("--- End Program Output ---" + System.lineSeparator());
                }
                assertTrue(process.exitValue() == 0);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static class MyFunction implements Function<byte[], byte[]> {
        private EventLoopGroup elg;
        private int id;
        private int run;
        public MyFunction(EventLoopGroup elg, int id) {
            this.elg = elg;
            this.id = id;
        }

        public byte[] apply(byte[] bytesIn) {
            //System.out.println(String.format("task:%d run:%d", id, run));
            bytesIn[1] = 4;
            run += 1;
            elg.scheduleTask(this);
            return new byte[500];
        }

    }

    public static void main(String[] args) {
        EventLoopGroup elg = new EventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);

        for (int i = 0; i < 1000; ++i) {
            elg.scheduleTask(new MyFunction(elg, i));
        }
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException ex) {
        }
    }
};

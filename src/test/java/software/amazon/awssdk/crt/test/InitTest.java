package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Arrays;

/*
This test exercises a case that used to deadlock the CRT.  To properly exercise init, the test must be run by
itself; a successful run when other tests have run doesn't mean anything.  Because the test uses a class loader
to reference the CRT types indirectly, this test will fail when run normally under maven.  Instead, it must
be run by invoking java directly with junit as the jar.

For CI, we explicitly run this test by itself as part of `.builder/actions/aws_crt_java_test.py`
 */
public class InitTest {

    private static String INIT_TEST_ENABLED = System.getenv("AWS_CRT_INIT_TESTING");

    private static boolean doInitTest() {
        return INIT_TEST_ENABLED != null;
    }

    @Test
    public void testConcurrentInitForDeadlock() throws Exception {
        Assume.assumeTrue(doInitTest());

        URL[] classPath = Arrays.stream(System.getProperty("java.class.path").split(":")).map(file -> {
            try {
                return new File(file).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);

        for (int i = 0; i < 100; i++) {
            try(URLClassLoader classLoader = new URLClassLoader(classPath)) {
                new Thread(() -> {
                    try {
                        Class.forName("software.amazon.awssdk.crt.CRT", true, classLoader);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                Class.forName("software.amazon.awssdk.crt.CrtResource", true, classLoader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.printf("step: %d%n", i);
        }
    }
}

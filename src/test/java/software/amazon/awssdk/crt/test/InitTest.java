package software.amazon.awssdk.crt.test;

import org.junit.Test;

/*
This test exercises a case that used to deadlock the CRT.  To properly exercise init, the test must be run by
itself; a successful run when other tests have run doesn't mean anything.

For CI, we explicitly run this test by itself as part of `.builder/actions/aws_crt_java_test.py`
 */
public class InitTest {

    @Test
    public void testConcurrentInitForDeadlock() {

        for (int i = 0; i < 100; i++) {
            try {
                new Thread(() -> {
                    try {
                        Class.forName("software.amazon.awssdk.crt.CRT");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                Class.forName("software.amazon.awssdk.crt.CrtResource");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.CRT;

public class JniExceptionTest extends CrtTestFixture {
    public JniExceptionTest() {}

    @Test(expected = RuntimeException.class)
    public void testExceptionCheck() {
        CRT.checkJniExceptionContract(false);
    }

    @Test
    public void testExceptionClear() {
        CRT.checkJniExceptionContract(true);
    }

}

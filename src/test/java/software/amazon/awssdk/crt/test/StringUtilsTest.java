package software.amazon.awssdk.crt.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.crt.utils.StringUtils;


public class StringUtilsTest {

    @Test
    public void testJoin() {
        List<String> alpns = new ArrayList<>();
        alpns.add("one");
        assertEquals("one", StringUtils.join(";", alpns));
        alpns.add("two");
        assertEquals("one;two", StringUtils.join(";", alpns));
    }
}

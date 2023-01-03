package software.amazon.awssdk.crt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.crt.utils.StringUtils;


public class StringUtilsTest extends CrtTestFixture {

    @Test
    public void testJoin() {
        List<String> alpns = new ArrayList<>();
        alpns.add("one");
        assertEquals("one", StringUtils.join(";", alpns));
        alpns.add("two");
        assertEquals("one;two", StringUtils.join(";", alpns));
    }

    @Test
    public void testBase64EncodeEmpty() {
        assertEquals("", new String(StringUtils.base64Encode("".getBytes())));
    }

    @Test
    public void testBase64EncodeNull() {
        ThrowingRunnable test_runnable = new ThrowingRunnable() {
            public void run() {
                StringUtils.base64Encode(null);
            }
        };
        assertThrows(NullPointerException.class, test_runnable);
    }

    @Test
    public void testBase64EncodeCaseFoobar() {
        assertEquals("Zm9vYmFy", new String(StringUtils.base64Encode("foobar".getBytes())));
    }

    @Test
    public void testBase64EncodeExtremelyLargeString() {
        StringBuilder test_input = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            test_input.append('A');
        }
        byte[] output = StringUtils.base64Encode(test_input.toString().getBytes());
        assertTrue(output != null);
    }

    @Test
    public void testBase64EncodeCaseAllValues() {
        byte[] data = new byte[255];
        for (int i = 0; i < 255; i++) {
            data[i] = (byte)(i);
        }

        String expected = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERU";
        expected += "ZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouM";
        expected += "jY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0t";
        expected += "PU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+";

        assertEquals(expected, new String(StringUtils.base64Encode(data)));
    }

    @Test
    public void testBase64DecodeEmpty() {
        assertEquals("", new String(StringUtils.base64Decode("".getBytes())));
    }

    @Test
    public void testBase64DecodeNull() {
        ThrowingRunnable test_runnable = new ThrowingRunnable() {
            public void run() {
                StringUtils.base64Decode(null);
            }
        };
        assertThrows(NullPointerException.class, test_runnable);
    }

    @Test
    public void testBase64DecodeCaseFoobar() {
        assertEquals("foobar", new String(StringUtils.base64Decode("Zm9vYmFy".getBytes())));
    }

    @Test
    public void testBase64DecodeExtremelyLargeString() {
        StringBuilder test_input = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            test_input.append('A');
        }
        byte[] output = StringUtils.base64Decode(test_input.toString().getBytes());
        assertTrue(output != null);
    }

    @Test
    public void testBase64DecodeCaseAllValues() {
        byte[] data = new byte[255];
        for (int i = 0; i < 255; i++) {
            data[i] = (byte)(i);
        }

        String input = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+P0BBQkNERU";
        input += "ZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouM";
        input += "jY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0t";
        input += "PU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+";

        String expected = new String(data);

        assertEquals(expected, new String(StringUtils.base64Decode(input.getBytes())));
    }

    @Test
    public void testBase64CaseFoobarRoundTrop() {
        String data = "foobar";
        data = new String(StringUtils.base64Encode(data.getBytes()));
        assertEquals("Zm9vYmFy", data);
        data = new String(StringUtils.base64Decode(data.getBytes()));
        assertEquals("foobar", data);
    }
}

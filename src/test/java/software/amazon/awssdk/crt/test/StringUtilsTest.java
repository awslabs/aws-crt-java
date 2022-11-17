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

    @Test
    public void testBase64EncodeEmpty() {
        assertEquals("", StringUtils.simpleBase64Encode("".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseF() {
        assertEquals("Zg==", StringUtils.simpleBase64Encode("f".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseFo() {
        assertEquals("Zm8=", StringUtils.simpleBase64Encode("fo".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseFoo() {
        assertEquals("Zm9v", StringUtils.simpleBase64Encode("foo".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseFoob() {
        assertEquals("Zm9vYg==", StringUtils.simpleBase64Encode("foob".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseFooba() {
        assertEquals("Zm9vYmE=", StringUtils.simpleBase64Encode("fooba".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseFoobar() {
        assertEquals("Zm9vYmFy", StringUtils.simpleBase64Encode("foobar".getBytes()));
    }

    @Test
    public void testBase64EncodeCase32bytes() {
        assertEquals("dGhpcyBpcyBhIDMyIGJ5dGUgbG9uZyBzdHJpbmchISE=", StringUtils.simpleBase64Encode("this is a 32 byte long string!!!".getBytes()));
    }

    @Test
    public void testBase64EncodeCaseZeros() {
        byte[] data = new byte[6];
        assertEquals("AAAAAAAA", StringUtils.simpleBase64Encode(data));
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

        assertEquals(expected, StringUtils.simpleBase64Encode(data));
    }
}

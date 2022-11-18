package software.amazon.awssdk.crt.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
    public void testBase64EncodeCaseF() {
        assertEquals("Zg==", new String(StringUtils.base64Encode("f".getBytes())));
    }

    @Test
    public void testBase64EncodeCaseFo() {
        assertEquals("Zm8=", new String(StringUtils.base64Encode("fo".getBytes())));
    }

    @Test
    public void testBase64EncodeCaseFoo() {
        assertEquals("Zm9v", new String(StringUtils.base64Encode("foo".getBytes())));
    }

    @Test
    public void testBase64EncodeCaseFoob() {
        assertEquals("Zm9vYg==", new String(StringUtils.base64Encode("foob".getBytes())));
    }

    @Test
    public void testBase64EncodeCaseFooba() {
        assertEquals("Zm9vYmE=", new String(StringUtils.base64Encode("fooba".getBytes())));
    }

    @Test
    public void testBase64EncodeCaseFoobar() {
        assertEquals("Zm9vYmFy", new String(StringUtils.base64Encode("foobar".getBytes())));
    }

    @Test
    public void testBase64EncodeCase32bytes() {
        assertEquals(
            "dGhpcyBpcyBhIDMyIGJ5dGUgbG9uZyBzdHJpbmchISE=",
            new String(StringUtils.base64Encode("this is a 32 byte long string!!!".getBytes())));
    }

    @Test
    public void testBase64EncodeCaseZeros() {
        byte[] data = new byte[6];
        assertEquals("AAAAAAAA", new String(StringUtils.base64Encode(data)));
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
    public void testBase64DecodeCaseF() {
        assertEquals("f", new String(StringUtils.base64Decode("Zg==".getBytes())));
    }

    @Test
    public void testBase64DecodeCaseFo() {
        assertEquals("fo", new String(StringUtils.base64Decode("Zm8=".getBytes())));
    }

    @Test
    public void testBase64DecodeCaseFoo() {
        assertEquals("foo", new String(StringUtils.base64Decode("Zm9v".getBytes())));
    }

    @Test
    public void testBase64DecodeCaseFoob() {
        assertEquals("foob", new String(StringUtils.base64Decode("Zm9vYg==".getBytes())));
    }

    @Test
    public void testBase64DecodeCaseFooba() {
        assertEquals("fooba", new String(StringUtils.base64Decode("Zm9vYmE=".getBytes())));
    }

    @Test
    public void testBase64DecodeCaseFoobar() {
        assertEquals("foobar", new String(StringUtils.base64Decode("Zm9vYmFy".getBytes())));
    }

    @Test
    public void testBase64DecodeCase32bytes() {
        assertEquals(
            "this is a 32 byte long string!!!",
            new String(StringUtils.base64Decode("dGhpcyBpcyBhIDMyIGJ5dGUgbG9uZyBzdHJpbmchISE=".getBytes())));
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

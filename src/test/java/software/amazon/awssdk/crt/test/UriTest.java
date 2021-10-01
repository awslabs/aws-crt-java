package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.io.Uri;
import static org.junit.Assert.*;
import org.junit.Test;

public class UriTest {

    @Test
    public void testUriEncodePath() {
        assertEquals("/path/1234/", Uri.encodeUriPath("/path/1234/"));
        assertEquals("/abcdefghijklmnopqrstuvwxyz/1234567890/",
                Uri.encodeUriPath("/abcdefghijklmnopqrstuvwxyz/1234567890/"));
        assertEquals("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/1234567890/",
                Uri.encodeUriPath("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/1234567890/"));
        assertEquals("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/_-~./%24%40%26%2C%3A%3B%3D/",
                Uri.encodeUriPath("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/_-~./$@&,:;=/"));
        assertEquals("/path/%25%5E%23%21%20/", Uri.encodeUriPath("/path/%^#! /"));
        assertEquals("/path/%E1%88%B4", Uri.encodeUriPath("/path/ሴ"));
        assertEquals("/path/%22%27%28%29%2A%2B%3C%3E%5B%5C%5D%60%7B%7C%7D/",
                Uri.encodeUriPath("/path/\"'()*+<>[\\]`{|}/"));
    }

    @Test
    public void testUriEncodeParam() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
                Uri.encodeUriParam("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));
        assertEquals(Uri.encodeUriParam("1234567890"), "1234567890");
        assertEquals("_~.-", Uri.encodeUriParam("_~.-"));
        assertEquals("%25%5E%23%21%20", Uri.encodeUriParam("%^#! "));
        assertEquals("%2F%24%40%26%2C%3A%3B%3D", Uri.encodeUriParam("/$@&,:;="));
        assertEquals("%E1%88%B4", Uri.encodeUriParam("ሴ"));
        assertEquals("%22%27%28%29%2A%2B%3C%3E%5B%5C%5D%60%7B%7C%7D", Uri.encodeUriParam("\"'()*+<>[\\]`{|}"));
    }

    @Test
    public void testUriDecode() {
        assertEquals("", Uri.decodeUri(""));
        assertEquals("abc123", Uri.decodeUri("abc123"));
        assertEquals(" ", Uri.decodeUri("%20"));
        assertEquals("ሴ", Uri.decodeUri("%E1%88%B4"));
        assertEquals("ሴ", Uri.decodeUri("%e1%88%b4"));
        assertEquals("%20", Uri.decodeUri("%2520"));
        assertEquals("ሴ", Uri.decodeUri("ሴ")); // odd input should just pass through
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
                Uri.decodeUri("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));// long enough to resize
                                                                                       // output buffer
    }

    private static String roundTripPath(String uri) {
        return Uri.decodeUri(Uri.encodeUriPath(uri));
    }

    private static String roundTripParam(String uri) {
        return Uri.decodeUri(Uri.encodeUriParam(uri));
    }

    @Test
    public void testUriRoundTrip() {
        assertEquals("", roundTripPath(""));
        assertEquals("abc123", roundTripPath("abc123"));
        assertEquals("a + b", roundTripPath("a + b"));
        assertEquals("ሴ", roundTripPath("ሴ"));
        assertEquals("", roundTripParam(""));
        assertEquals("abc123", roundTripParam("abc123"));
        assertEquals("a + b", roundTripParam("a + b"));
        assertEquals("ሴ", roundTripParam("ሴ"));
    }
}

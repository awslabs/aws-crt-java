package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.io.Uri;
import static org.junit.Assert.*;
import org.junit.Test;

public class UriTest {

    @Test
    public void testUriEncodePath() {
        assertEquals("/path/1234/", Uri.appendEncodingUriPath("/path/1234/"));
        assertEquals("/abcdefghijklmnopqrstuvwxyz/1234567890/",
                Uri.appendEncodingUriPath("/abcdefghijklmnopqrstuvwxyz/1234567890/"));
        assertEquals("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/1234567890/",
                Uri.appendEncodingUriPath("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/1234567890/"));
        assertEquals("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/_-~./%24%40%26%2C%3A%3B%3D/",
                Uri.appendEncodingUriPath("/ABCDEFGHIJKLMNOPQRSTUVWXYZ/_-~./$@&,:;=/"));
        assertEquals("/path/%25%5E%23%21%20/", Uri.appendEncodingUriPath("/path/%^#! /"));
        // assertEquals("/path/%E1%88%B4", Uri.appendEncodingUriPath("/path/ሴ"));
        assertEquals("/path/%22%27%28%29%2A%2B%3C%3E%5B%5C%5D%60%7B%7C%7D/",
                Uri.appendEncodingUriPath("/path/\"'()*+<>[\\]`{|}/"));
    }

    @Test
    public void testUriEncodeParam() {
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
                Uri.appendEncodingUriParam("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));
        assertEquals(Uri.appendEncodingUriParam("1234567890"), "1234567890");
        assertEquals("_~.-", Uri.appendEncodingUriParam("_~.-"));
        assertEquals("%25%5E%23%21%20", Uri.appendEncodingUriParam("%^#! "));
        assertEquals("%2F%24%40%26%2C%3A%3B%3D", Uri.appendEncodingUriParam("/$@&,:;="));
        // assertEquals("%E1%88%B4", Uri.appendEncodingUriParam("ሴ"));
        assertEquals("%22%27%28%29%2A%2B%3C%3E%5B%5C%5D%60%7B%7C%7D", Uri.appendEncodingUriParam("\"'()*+<>[\\]`{|}"));
    }

    @Test
    public void testUriDecode() {
        assertEquals("", Uri.appendDecodingUri(""));
        assertEquals("abc123", Uri.appendDecodingUri("abc123"));
        assertEquals(" ", Uri.appendDecodingUri("%20"));
        // assertEquals("ሴ", Uri.appendDecodingUri("%E1%88%B4"));
        // assertEquals("ሴ", Uri.appendDecodingUri("%e1%88%b4"));
        assertEquals("%20", Uri.appendDecodingUri("%2520"));
        // // assertEquals("ሴ", Uri.appendDecodingUri("ሴ")); // odd input should just
        // pass through
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
                Uri.appendDecodingUri("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));// long enough to resize
                                                                                               // output buffer
    }

    private static String roundTripPath(String uri) {
        return Uri.appendDecodingUri(Uri.appendEncodingUriPath(uri));
    }

    private static String roundTripParam(String uri) {
        return Uri.appendDecodingUri(Uri.appendEncodingUriParam(uri));
    }

    @Test
    public void testUriRoundTrip() {
        assertEquals("", roundTripPath(""));
        assertEquals("abc123", roundTripPath("abc123"));
        assertEquals("a + b", roundTripPath("a + b"));
        // // assertEquals("ሴ", roundTripPath("ሴ"));
        assertEquals("", roundTripParam(""));
        assertEquals("abc123", roundTripParam("abc123"));
        assertEquals("a + b", roundTripParam("a + b"));
        // // assertEquals("ሴ", roundTripParam("ሴ"));
    }
}

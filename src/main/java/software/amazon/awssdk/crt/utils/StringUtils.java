package software.amazon.awssdk.crt.utils;

public class StringUtils {
    /**
     * Returns a new String composed of copies of the CharSequence elements joined together with a copy of the specified delimiter.
     * Like `Strings.join()` but works on Android before API 26.
     *
     * @param delimiter a sequence of characters that is used to separate each of the elements in the resulting String
     * @param elements an Iterable that will have its elements joined together
     * @return a new String that is composed from the elements argument
     */
    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        if (delimiter == null || elements == null) throw new NullPointerException("delimiter and elements must not be null");
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for(CharSequence cs : elements) {
            if (!first) {
                sb.append(delimiter);
            }
            sb.append(cs);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Decode Base64 byte[] array into a String in pure Java for Android and Java 7+ support.
     * Based on: https://stackoverflow.com/a/4265472
    */
    private final static char[] BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static String simpleBase64ToString(byte[] buf){
        int size = buf.length;
        char[] ar = new char[((size + 2) / 3) * 4];
        int a = 0;
        int i=0;
        while(i < size){
            byte b0 = buf[i++];
            byte b1 = (i < size) ? buf[i++] : 0;
            byte b2 = (i < size) ? buf[i++] : 0;

            int mask = 0x3F;
            ar[a++] = BASE64_ALPHABET[(b0 >> 2) & mask];
            ar[a++] = BASE64_ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
            ar[a++] = BASE64_ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
            ar[a++] = BASE64_ALPHABET[b2 & mask];
        }
        switch(size % 3){
            case 1: ar[--a]  = '=';
            case 2: ar[--a]  = '=';
        }
        return new String(ar);
    }
}

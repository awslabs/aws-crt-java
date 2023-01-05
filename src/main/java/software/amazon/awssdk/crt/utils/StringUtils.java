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
      * Encode a byte array into a Base64 byte array.
      * @param data The byte array to encode
      * @return The byte array encoded as Byte64
      */
    public static byte[] base64Encode(byte[] data) {
        return stringUtilsBase64Encode(data);
    }

     /**
      * Decode a Base64 byte array into a non-Base64 byte array.
      * @param data The byte array to decode.
      * @return Byte array decoded from Base64.
      */
    public static byte[] base64Decode(byte[] data) {
        return stringUtilsBase64Decode(data);
    }

    private static native byte[] stringUtilsBase64Encode(byte[] data_to_encode);
    private static native byte[] stringUtilsBase64Decode(byte[] data_to_decode);
}

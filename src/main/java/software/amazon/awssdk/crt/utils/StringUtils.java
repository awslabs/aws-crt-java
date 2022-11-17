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
     * Encode a byte[] array into a Base64 String in pure Java
     */
    private final static char[] BASE64_VALID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static String simpleBase64Encode(byte[] data) {

        // Empty array? Return an empty string
        if (data.length == 0) {
            return new String();
        }

        String result = "";
        // Base64 REQUIRES each data block be 24 bits in length. This means that if the data we want to encode
        // is too short, we need to add some special padding to it.
        String padding_string = "";
        int length_remainder = data.length % 3;
        byte[] data_padded;

        // Add the proper amount of padding to the padding string and set the data in the data_padded array.
        if (length_remainder > 0) {
            // Make extra room for padding. Not ideal to do a copy, but we cannot just append to an already made array
            data_padded = new byte[data.length + (3 - length_remainder)];
            System.arraycopy(data, 0, data_padded, 0, data.length);

            for (; length_remainder < 3; length_remainder++) {
                padding_string += "=";
                data_padded[(data.length-1) + padding_string.length()] = '\0';
            }
        } else {
            data_padded = data;
        }

        // Variables we will use and override as we go through the data in the byte[] array
        int character = 0;
        int mask_offset = 63;
        int byte_offset = 255;
        byte byte_1 = 0;
        byte byte_2 = 0;
        byte byte_3 = 0;

        for (character = 0; character < data_padded.length; character += 3) {
            byte_1 = data_padded[character];
            byte_2 = data_padded[character+1];
            byte_3 = data_padded[character+2];
            result += BASE64_VALID_CHARACTERS[(byte_1 >> 2) & mask_offset];
            result += BASE64_VALID_CHARACTERS[((byte_1 << 4) | ((byte_2 & byte_offset) >> 4)) & mask_offset];
            result += BASE64_VALID_CHARACTERS[((byte_2 << 2) | ((byte_3 & byte_offset) >> 6)) & mask_offset];
            result += BASE64_VALID_CHARACTERS[byte_3 & mask_offset];
        }
        // The data we added for padding purposes ('\0') is not part of the data we were given and so we need to remove it.
        // We can replace it with '=', which is a special character in Base64 that tells Base64 decoders this is simply padding.
        return result.substring(0, result.length() - padding_string.length()) + padding_string;
    }
}

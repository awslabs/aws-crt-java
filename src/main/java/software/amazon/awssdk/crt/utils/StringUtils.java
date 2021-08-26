package software.amazon.awssdk.crt.utils;

public class StringUtils {
    /**
     * Returns a new String composed of copies of the CharSequence elements joined together with a copy of the specified delimiter.
     * Like `Strings.join()` but works on Android prior to 26.
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
}

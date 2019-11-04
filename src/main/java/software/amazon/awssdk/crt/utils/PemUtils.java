
/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR connectionS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility Class used for Cleaning Up and Sanity Checking PEM formatted Strings
 * for Validity.
 */
public class PemUtils {
    private static final int ALLOWED_CHARS_PER_LINE = 64;
    private static final String BASE_64_ENCODING_CHARS = "a-zA-Z0-9=+\\/";
    private static final String ALLOWED_WHITESPACE_CHARS = " \\r\\n";
    private static final String DELIMITER_CHARS = "-";
    private static final String MINUMUM_ALLOWED_PEM_CHARACTER_SET = BASE_64_ENCODING_CHARS + ALLOWED_WHITESPACE_CHARS
            + DELIMITER_CHARS;
    private static final String NON_BASE64_CHARACTER_SET = "[^" + BASE_64_ENCODING_CHARS + "]+";
    private static final String UNNECESSARY_PEM_CHARACTER_SET = "[^" + MINUMUM_ALLOWED_PEM_CHARACTER_SET + "]+";

    // These Regex's roughly follows the formal RFC Grammar Definition here:
    // https://tools.ietf.org/html/rfc7468#section-3
    private static final String PEM_OBJECT_TYPE = "([A-Z0-9 ]+)";
    private static final String PEM_DASHES = "[-]+";
    private static final String PEM_NON_DASHES = "([^-]+)";
    private static final String PEM_BEGIN_LINE_REGEX = PEM_DASHES + "BEGIN " + PEM_OBJECT_TYPE + PEM_DASHES;

    // Be fairly lenient on PEM Contents regex since formatAccordingToRFC() will
    // filter out non-Base64 Characters
    private static final String PEM_CONTENTS_REGEX = PEM_NON_DASHES;

    private static final String PEM_END_LINE_REGEX = PEM_DASHES + "END " + PEM_OBJECT_TYPE + PEM_DASHES;

    private static final Pattern INVALID_PEM_CHARACTER_PATTERN = Pattern.compile(UNNECESSARY_PEM_CHARACTER_SET);
    private static final Pattern PEM_BEGIN_PATTERN = Pattern.compile(PEM_BEGIN_LINE_REGEX);
    private static final Pattern PEM_END_PATTERN = Pattern.compile(PEM_END_LINE_REGEX);

    private static final Pattern PEM_OBJECT_PATTERN = Pattern
            .compile(PEM_BEGIN_LINE_REGEX + PEM_CONTENTS_REGEX + PEM_END_LINE_REGEX);

    // Pattern for catching when END and BEGIN being on the same line and they need
    // to be separated.
    private static final Pattern COMBINED_PEM_HEADERS = Pattern.compile(PEM_END_LINE_REGEX + "BEGIN");

    private PemUtils() {
    }

    /**
     * Removes characters that are not valid in PEM format (non-base64 chars). No other cleanup is
     * done.
     * 
     * @param pem The input "dirty" PEM
     * @return The output "clean" PEM
     */
    public static String removeInvalidPemChars(String pem) {
        if (pem == null || pem.length() == 0) {
            return pem;
        }
        return pem.replaceAll(UNNECESSARY_PEM_CHARACTER_SET, "");
    }

    /**
     * Removes characters that are not valid in base64. No other cleanup is
     * done.
     * 
     * @param base64Contents The input "dirty" PEM
     * @return The output "clean" PEM
     */
    private static String removeNonBase64Chars(String base64Contents) {
        if (base64Contents == null || base64Contents.length() == 0) {
            return base64Contents;
        }
        return base64Contents.replaceAll(NON_BASE64_CHARACTER_SET, "");
    }

    /**
     * Parses the full PEM Chain one object at a time and rewrites it following the
     * RFC formatting rules.
     *
     * Performs the following operations and fixes: - Base 64 Encoded PEM Content
     * Formatting: - All lines exactly 64 Characters long except for the last line.
     * - Only whitespace is a single newline every 64 chars - The number of dashes
     * "-" on the BEGIN and END lines are exactly 5 dashes - Garbage characters
     * in-between PEM objects (characters after an END and before the next BEGIN)
     * are removed
     *
     * For more info, see: https://tools.ietf.org/html/rfc1421#section-4.3.2.4
     * 
     * @param pem The input "dirty" PEM
     * @return The output "clean" PEM
     */
    private static String formatAccordingToRFC(String pem) {
        if (pem == null || pem.length() == 0) {
            return pem;
        }
        Matcher matcher = PEM_OBJECT_PATTERN.matcher(pem);
        StringBuffer outBuffer = new StringBuffer();

        int count = 0;
        while (matcher.find()) {
            if (count > 0) {
                outBuffer.append('\n');
            }
            String beginType = matcher.group(1);
            String base64Contents = removeNonBase64Chars(matcher.group(2));
            String endType = matcher.group(3);

            outBuffer.append("-----BEGIN " + beginType + "-----\n");

            int index = 0;
            for (char c : base64Contents.toCharArray()) {

                outBuffer.append(c);
                index++;
                if (index % ALLOWED_CHARS_PER_LINE == 0) {
                    outBuffer.append('\n');
                }
            }
            if (outBuffer.charAt(outBuffer.length() - 1) != '\n') {
                outBuffer.append('\n');
            }

            outBuffer.append("-----END " + endType + "-----");
            count++;
        }

        return outBuffer.toString();
    }

    /**
     * Inserts newlines in combined PEM Headers (Eg "-----END
     * CERTIFICATE----------BEGIN CERTIFICATE-----")
     * 
     * @param pem The input "dirty" PEM
     * @return The output "clean" PEM
     */
    private static String splitCombinedPemHeaders(String pem) {
        if (pem == null || pem.length() == 0) {
            return pem;
        }
        Matcher m = COMBINED_PEM_HEADERS.matcher(pem);
        if (m.find()) {
            // The Parenthesis in "([A-Z0-9 ]+)" in PEM_OBJECT_TYPE is a capturing group, it
            // allows us to reference
            // Group 1 with "$1" in the replaceAll() call here.
            // For more info see: https://stackoverflow.com/a/27328750/7565918
            return m.replaceAll("-----END $1-----\n-----BEGIN");
        }
        return pem;
    }

    /**
     * Merge consecutive spaces into a single space (Eg "BEGIN     CERTIFICATE", will
     * become "BEGIN CERTIFICATE")
     * 
     * @param pem The input "dirty" PEM
     * @return The output "clean" PEM
     */
    private static String mergeSpaces(String pem) {
        if (pem == null || pem.length() == 0) {
            return pem;
        }
        return pem.replaceAll("[ ]+", " ");
    }

    /**
     * Remove any spaces next to dashes (Eg "----- BEGIN" will become "-----BEGIN")
     * 
     * @param pem The input "dirty" PEM
     * @return The output "clean" PEM
     */
    private static String removeSpacesNextToDashes(String pem) {
        if (pem == null || pem.length() == 0) {
            return pem;
        }
        return pem.replaceAll("( -)|(- )", "-");
    }

    /**
     * Cleanup Function that removes most formatting and copy/paste mistakes from
     * PEM formatted Strings.
     * 
     * @param pem The input "dirty" PEM
     * @return The output "clean" PEM
     */
    public static String cleanUpPem(String pem) {
        if (pem == null || pem.length() == 0) {
            return pem;
        }

        String cleanPem = removeInvalidPemChars(pem);

        cleanPem = mergeSpaces(cleanPem);

        cleanPem = removeSpacesNextToDashes(cleanPem);

        cleanPem = splitCombinedPemHeaders(cleanPem);

        cleanPem = formatAccordingToRFC(cleanPem);

        return cleanPem;
    }

    /**
     * Checks for invalid characters in the PEM (Eg non-ASCII Chars).
     * 
     * @param pem The input PEM formatted String
     * @throws IllegalArgumentException If any character in the PEM is outside the
     *                                  valid Character Set.
     */
    private static void validateCharacterSet(String pem) {
        // If there are any invalid characters, throw an exception with detailed info
        // about their indexes
        if (INVALID_PEM_CHARACTER_PATTERN.matcher(pem).matches()) {
            StringBuilder debugStr = new StringBuilder();
            int index = 0;
            for (char c : pem.toCharArray()) {
                if (INVALID_PEM_CHARACTER_PATTERN.matcher(String.valueOf(c)).matches()) {
                    if (debugStr.length() > 0) {
                        debugStr.append(", ");
                    }
                    debugStr.append("\\u" + (int) c + " at index " + index);
                }
                index++;
            }
            throw new IllegalArgumentException("Illegal Characters found in PEM file. Chars: " + debugStr);
        }
    }

    /**
     * Checks that the number of "BEGIN" statements matches the number of "END"
     * statements, and that END's come after BEGIN's.
     *
     * @param pem           The input PEM formatted String
     * @param maxChainDepth The max number of PEM Formatted Objects in the String.
     * @return The number of PEM encoded objects found by Regex
     */
    private static int validatePemByRegexParser(String pem, String expectedPemTypeSubString, int maxChainDepth) {
        int beginCount = 0;
        int endCount = 0;
        int objCount = 0;
        Matcher beginMatcher = PEM_BEGIN_PATTERN.matcher(pem);
        Matcher endMatcher = PEM_END_PATTERN.matcher(pem);
        Matcher objMatcher = PEM_OBJECT_PATTERN.matcher(pem);

        while (beginMatcher.find()) {
            beginCount++;
        }
        while (endMatcher.find()) {
            endCount++;
        }
        while (objMatcher.find()) {
            String beginType = objMatcher.group(1);
            String base64Contents = objMatcher.group(2);
            String endType = objMatcher.group(3);

            if (!beginType.contains(expectedPemTypeSubString) || !endType.contains(expectedPemTypeSubString)) {
                throw new IllegalArgumentException(
                        "PEM Object does not have expected type. " + "Expected Type: " + expectedPemTypeSubString
                                + ", Actual BEGIN Type: " + beginType + ", Actual END Type: " + endType);
            }
            if (base64Contents.length() == 0) {
                throw new IllegalArgumentException("PEM Objet does not have any contents");
            }
            objCount++;
        }

        if (objCount == 0) {
            throw new IllegalArgumentException("PEM contains no objects, or is not a PEM");
        }

        if (beginCount != endCount || beginCount != objCount) {
            throw new IllegalArgumentException("PEM has mismatching BEGIN and END Delimiters. BeginCount: " + beginCount
                    + ", EndCount: " + endCount + ", ObjCount: " + objCount);
        }

        if (beginCount > maxChainDepth) {
            throw new IllegalArgumentException(
                    "PEM has greater than expected depth, ExpectedMax: " + maxChainDepth + ", Actual: " + beginCount);
        }

        return beginCount;
    }

    /**
     * Performs various sanity checks on a PEM Formatted String, and should be
     * tolerant of common minor mistakes in formatting.
     * 
     * @param pem                      The PEM or PEM Chain to validate.
     * @param maxChainLength           The max number of PEM encoded objects in the
     *                                 String.
     * @param expectedPemTypeSubString A Substring that is expected to be present in
     *                                 the PEM Type.
     * @throws IllegalArgumentException if there is a problem with the PEM formatted
     *                                  String.
     */
    public static void sanityCheck(String pem, int maxChainLength, String expectedPemTypeSubString) {
        if (pem == null || pem.length() == 0) {
            return;
        }
        validateCharacterSet(pem);
        validatePemByRegexParser(pem, expectedPemTypeSubString, maxChainLength);
    }

    /**
     * Returns false if there is a problem with a PEM instead of throwing an
     * Exception.
     * 
     * @param pem                      The PEM to sanity check.
     * @param maxChainLength           The Max number of PEM Objects in the PEM
     *                                 String
     * @param expectedPemTypeSubString A Substring that is expected to be present in
     *                                 the PEM Type.
     * @return True if the PEM passes all sanity Checks, false otherwise.
     */
    public static boolean safeSanityCheck(String pem, int maxChainLength, String expectedPemTypeSubString) {
        try {
            sanityCheck(pem, maxChainLength, expectedPemTypeSubString);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}

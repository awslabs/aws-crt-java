package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrtS3RuntimeException extends CrtRuntimeException {

    private final int statusCode;
    private final String awsErrorCode;
    private final String awsErrorMessage;
    private final String errorPayload;

    private final static String codeBeginBlock = new String("<Code>");
    private final static String codeEndBlock = new String("</Code>");
    private final static String messageBeginBlock = new String("<Message>");
    private final static String messageEndBlock = new String("</Message>");

    public CrtS3RuntimeException(int errorCode, int responseStatus, String errorPayload) {
        super(errorCode);
        this.statusCode = responseStatus;
        this.errorPayload = errorPayload;
        this.awsErrorCode = GetElementFromPyload(errorPayload, codeBeginBlock, codeEndBlock);
        this.awsErrorMessage = GetElementFromPyload(errorPayload, messageBeginBlock, messageEndBlock);
    }

    public CrtS3RuntimeException(S3FinishedResponseContext context) {
        super(context.getErrorCode());
        String errorString = new String(context.getErrorPayload(), java.nio.charset.StandardCharsets.UTF_8);
        this.statusCode = context.getResponseStatus();
        this.errorPayload = errorString;
        this.awsErrorCode = GetElementFromPyload(this.errorPayload, codeBeginBlock, codeEndBlock);
        this.awsErrorMessage = GetElementFromPyload(this.errorPayload, messageBeginBlock, messageEndBlock);
    }

    public CrtS3RuntimeException(int errorCode, int responseStatus, byte[] errorPayload) {
        super(errorCode);
        String errorString = new String(errorPayload, java.nio.charset.StandardCharsets.UTF_8);
        this.statusCode = responseStatus;
        this.errorPayload = errorString;
        this.awsErrorCode = GetElementFromPyload(this.errorPayload, codeBeginBlock, codeEndBlock);
        this.awsErrorMessage = GetElementFromPyload(this.errorPayload, messageBeginBlock, messageEndBlock);
    }

    /**
     * Helper function to get the detail of an element from xml payload. If not
     * found, empty string will be returned.
     */
    private String GetElementFromPyload(String errorPayload, String beginBlock, String endBlock) {
        Pattern regexFormat = Pattern.compile(beginBlock + ".*" + endBlock);
        Matcher matcher = regexFormat.matcher(errorPayload);
        String result = "";
        if (matcher.find()) {
            result = errorPayload.substring(matcher.start() + beginBlock.length(), matcher.end() - endBlock.length());
        }
        return result;
    }

    /**
     * Returns the aws error code from S3 response. The {@code Code} element in xml
     * response.
     *
     * @return errorCode, if no {@code Code} element in the response, empty string will be
     *         returned
     */
    public String getAwsErrorCode() {
        return awsErrorCode;
    }

    /**
     * Returns the error message from S3 response. The detail among {@code Message}
     * element in xml response.
     *
     * @return error message, if no {@code Message} element in the response, empty string
     *         will be returned
     */
    public String getAwsErrorMessage() {
        return awsErrorMessage;
    }

    /**
     * Returns the status code in S3 response.
     *
     * @return status code in int
     */
    public int getStatusCode() {
        return statusCode;

    }
    @Override
    public String toString() {
        return String.format("%s: response status code(%d). aws error code(%s), aws error message(%s)", super.toString(), statusCode, awsErrorCode, awsErrorMessage);
    }
}

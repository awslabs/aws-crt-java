package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrtS3RuntimeException extends CrtRuntimeException {

    public final int statusCode;
    public final String awsErrorCode;
    public final String errorPayload;

    private final static String beginBlock = new String("<Code>");
    private final static String endBlock = new String("</Code>");
    private final static Pattern codeFormat = Pattern.compile(beginBlock + ".*" + endBlock);

    public CrtS3RuntimeException(int errorCode, int responseStatus, String errorPayload) {
        super(errorCode);
        this.statusCode = responseStatus;
        this.errorPayload = errorPayload;
        this.awsErrorCode = GetAwsErrorCode(errorPayload);
    }

    public CrtS3RuntimeException(int errorCode, int responseStatus, byte[] errorPayload) {
        super(errorCode);
        String errorString = new String(errorPayload, java.nio.charset.StandardCharsets.UTF_8);
        this.statusCode = responseStatus;
        this.errorPayload = errorString;
        this.awsErrorCode = GetAwsErrorCode(this.errorPayload);
    }

    private String GetAwsErrorCode(String errorPayload) {
        Matcher matcher = codeFormat.matcher(errorPayload);
        String awsErrorCode = "";
        if (matcher.find()) {
            awsErrorCode = errorPayload.substring(matcher.start() + beginBlock.length(),
                    matcher.end() - endBlock.length());
        }
        return awsErrorCode;
    }

}

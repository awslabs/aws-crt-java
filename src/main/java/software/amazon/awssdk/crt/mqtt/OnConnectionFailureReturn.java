package software.amazon.awssdk.crt.mqtt;

 /**
 * The data returned when the connection failure callback is invoked in a connection.
 * @see software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents
 */
public class OnConnectionFailureReturn {
    /**
     * AWS CRT error code for the connection failure.
     * Pass to {@link software.amazon.awssdk.crt.CRT#awsErrorString(int)} for a human readable error
     */
    private int errorCode;

    /**
     * Gets the AWS CRT error code for the connection failure.
     * Pass to {@link software.amazon.awssdk.crt.CRT#awsErrorString(int)} for a human readable error
     * @return The AWS CRT error code for the connection failure.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Constructs a new OnConnectionFailureReturn with an error code
     * @param errorCode The AWS CRT error code
     */
    protected OnConnectionFailureReturn(int errorCode) {
        this.errorCode = errorCode;
    }
}

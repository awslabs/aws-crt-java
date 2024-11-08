package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;


public class MqttRequestResponseClient extends CrtResource {

    public MqttRequestResponseClient(Mqtt5Client client, MqttRequestResponseClientBuilder.MqttRequestResponseClientOptions options) throws CrtRuntimeException  {
        acquireNativeHandle(mqttRequestResponseClientNewFrom5(
                this,
                client.getNativeHandle(),
                options.getMaxRequestResponseSubscriptions(),
                options.getMaxStreamingSubscriptions(),
                options.getOperationTimeoutSeconds()
        ));
    }

    public MqttRequestResponseClient(MqttClientConnection client, MqttRequestResponseClientBuilder.MqttRequestResponseClientOptions options) throws CrtRuntimeException {
        acquireNativeHandle(mqttRequestResponseClientNewFrom311(
                this,
                client.getNativeHandle(),
                options.getMaxRequestResponseSubscriptions(),
                options.getMaxStreamingSubscriptions(),
                options.getOperationTimeoutSeconds()
        ));
    }

    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            mqttRequestResponseClientDestroy(getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqttRequestResponseClientNewFrom5(
            MqttRequestResponseClient client,
            long protocolClientHandle,
            int maxRequestResponseSubscriptions,
            int maxStreamingSubscriptions,
            int operationTimeoutSeconds
    ) throws CrtRuntimeException;

    private static native long mqttRequestResponseClientNewFrom311(
            MqttRequestResponseClient client,
            long protocolClientHandle,
            int maxRequestResponseSubscriptions,
            int maxStreamingSubscriptions,
            int operationTimeoutSeconds
    ) throws CrtRuntimeException;

    private static native void mqttRequestResponseClientDestroy(long client);
}

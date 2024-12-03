package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;

/**
 * A helper class for AWS service clients that use MQTT as the transport protocol.
 *
 * The class supports orchestrating request-response operations and creating streaming operations.  Used by the
 * IoT SDKs to implement higher-level service clients that provide a good user experience.
 *
 * Not intended to be constructed or used directly; the service client will create one during its construction.
 */
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

    public void submitRequest(MqttRequest request) {

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

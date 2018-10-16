
import com.amazon.aws.CRT;

public final class AWSIoTMQTTClient implements AutoCloseable {
    private CRT _crt;

    AWSIoTMQTTClient() {
        // This will cause the JNI lib to be loaded the first time a CRT is created
        _crt = new CRT();
        assert (_crt != null);
        
    }
    
    @Override
    public void close() {
        _crt = null;
    }

    void connect() {

    }
};

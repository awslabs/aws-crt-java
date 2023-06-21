package software.amazon.awssdk.crt.s3;

/**
 * This class provides access to setting Tcp Keep Alive Options.
 * If interval or timeout are zero, then default values are used.
 */
public class S3TcpKeepAliveOptions {

    private short keepAliveIntervalSec;

    private short keepAliveTimeoutSec;

    /* If set, sets the number of keep alive probes allowed to fail before the connection is considered
     * lost. If zero OS defaults are used. On Windows, this option is meaningless until Windows 10 1703.*/
    private short keepAliveMaxFailedProbes;

    public short getKeepAliveIntervalSec() {
        return keepAliveIntervalSec;
    }

    public void setKeepAliveIntervalSec(short keepAliveIntervalSec) {
        this.keepAliveIntervalSec = keepAliveIntervalSec;
    }

    public short getKeepAliveTimeoutSec() {
        return keepAliveTimeoutSec;
    }

    public void setKeepAliveTimeoutSec(short keepAliveTimeoutSec) {
        this.keepAliveTimeoutSec = keepAliveTimeoutSec;
    }

    public short getKeepAliveMaxFailedProbes() {
        return keepAliveMaxFailedProbes;
    }

    public void setKeepAliveMaxFailedProbes(short keepAliveMaxFailedProbes) {
        this.keepAliveMaxFailedProbes = keepAliveMaxFailedProbes;
    }
}

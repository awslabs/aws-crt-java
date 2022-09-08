package software.amazon.awssdk.crt.s3;

/**
 * This class provides access to setting Tcp Keep Alive Options.
 * If interval or timeout are zero, then default values are used.
 */
public class S3TcpKeepAliveOptions {

    private int keepAliveIntervalSec;

    private int keepAliveTimeoutSec;

    /* If set, sets the number of keep alive probes allowed to fail before the connection is considered
     * lost. If zero OS defaults are used. On Windows, this option is meaningless until Windows 10 1703.*/
    private int keepAliveMaxFailedProbes;

    public int getKeepAliveIntervalSec() {
        return keepAliveIntervalSec;
    }

    public void setKeepAliveIntervalSec(int keepAliveIntervalSec) {
        this.keepAliveIntervalSec = keepAliveIntervalSec;
    }

    public int getKeepAliveTimeoutSec() {
        return keepAliveTimeoutSec;
    }

    public void setKeepAliveTimeoutSec(int keepAliveTimeoutSec) {
        this.keepAliveTimeoutSec = keepAliveTimeoutSec;
    }

    public int getKeepAliveMaxFailedProbes() {
        return keepAliveMaxFailedProbes;
    }

    public void setKeepAliveMaxFailedProbes(int keepAliveMaxFailedProbes) {
        this.keepAliveMaxFailedProbes = keepAliveMaxFailedProbes;
    }
}

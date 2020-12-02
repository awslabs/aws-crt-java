package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;

public class S3ClientOptions {

    private String region;
    private String endpoint;
    private ClientBootstrap clientBootstrap;
    private CredentialsProvider credentialsProvider;
    private long partSize;
    private double throughputTargetGbps;
    private double throughputPerVIP;
    private int numConnectionsPerVIP;

    public S3ClientOptions() {
    
    }

    public S3ClientOptions withRegion(String region) {
        this.region = region;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public S3ClientOptions withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public S3ClientOptions withClientBootstrap(ClientBootstrap clientBootstrap) {
        this.clientBootstrap = clientBootstrap;
        return this;
    }

    public ClientBootstrap getClientBootstrap() {
        return clientBootstrap;
    }

    public S3ClientOptions withCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public S3ClientOptions withPartSize(long partSize) {
        this.partSize = partSize;
        return this;
    }

    public long getPartSize() {
        return partSize;
    }

    public S3ClientOptions withThroughputTargetGbps(double throughputTargetGbps) {
        this.throughputTargetGbps = throughputTargetGbps;
        return this;
    }

    public double getThroughputTargetGbps() {
        return throughputTargetGbps;
    }

    public S3ClientOptions withThroughputPerVIP(double throughputPerVIP) {
        this.throughputPerVIP = throughputPerVIP;
        return this;
    }

    public double getThroughputPerVIP() {
        return throughputPerVIP;
    }

    public S3ClientOptions withNumConnectionsPerVIP(int numConnectionsPerVIP) {
        this.numConnectionsPerVIP = numConnectionsPerVIP;
        return this;
    }

    public int getNumConnectionsPerVIP() {
        return numConnectionsPerVIP;
    }
}

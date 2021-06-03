package software.amazon.awssdk.crt.auth.signing;

import software.amazon.awssdk.crt.http.HttpRequest;

/**
 * Wrapper that holds signing-related output.  Depending on the signing configuration, not all members may be
 * assigned and some members, like signature, may have a variable format.
 */
public class AwsSigningResult {
    private HttpRequest signedRequest;
    private byte[] signature;

    public AwsSigningResult() {}

    /**
     * Gets the signed http request from the result
     * @return the signed http request, or NULL if an http request was not signed
     */
    public HttpRequest getSignedRequest() { return signedRequest; }

    /**
     * Gets the signature value from the result.  Depending on the requested signature type and algorithm, this value
     * will be in one of the following formats:
     *
     *   (1) HTTP_REQUEST_VIA_HEADERS - hex encoding of the binary signature value
     *   (2) HTTP_REQUEST_VIA_QUERY_PARAMS - hex encoding of the binary signature value
     *   (3) HTTP_REQUEST_CHUNK/SIGV4 - hex encoding of the binary signature value
     *   (4) HTTP_REQUEST_CHUNK/SIGV4_ASYMMETRIC - '*'-padded hex encoding of the binary signature value
     *   (5) HTTP_REQUEST_EVENT - binary signature value (NYI)
     *
     * @return the signature value from the signing process
     */
    public byte[] getSignature() { return signature; }
}

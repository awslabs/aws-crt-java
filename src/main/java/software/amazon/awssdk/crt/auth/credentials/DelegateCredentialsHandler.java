package software.amazon.awssdk.crt.auth.credentials;

/**
 * Interface that synchronously provides custom credentials.
 */
public interface DelegateCredentialsHandler {

    /**
     * Called from Native when delegate credential provider needs to fetch a
     * credential.
     */
    Credentials getCredentials();
}

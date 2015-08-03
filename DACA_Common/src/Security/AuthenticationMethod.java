package Security;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public enum AuthenticationMethod {

    /**
     * A hash of the password is sent over an non encrypted data stream. Use only if security is not a concern and data encryption is not required. Authenticates only the client.
     */
    PLAIN,
    /**
     * Challenge Response authentication with no encryption of the data streams. Use only if MITM attacks are not a concern and data encryption is not required. Authenticates both the client and the
     * server.
     */
    ChallengeResponse,
    /**
     * Uses anonymous SSL to encrypt the data and encrypts the DH key for authentication. Recommended method, use when security and data encryption is important. Authenticates both client and server
     * and protects against MITM attacks by digesting the agreed DH key with the hash of the user secret. Unlike the SSL authentication method, it does not require the user to provide the server's
     * certificate, preventing server impersonation attacks given that the shared secret is kept private and/or the second authentication step is used.
     */
    PSKSSL,
    /**
     * Uses SSL to encrypt the data and the ChallengeResponse to authenticate. User must provide the server's public key certificate and make sure it isn't swapped to prevent server impersonation
     * attacks. Uses the CR mechanism for mutual authentication.
     */
    SSL
}

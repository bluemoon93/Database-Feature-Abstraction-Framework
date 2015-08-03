package Security;

import LocalTools.BTC_Exception;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Implements the mutual challenge response authentication mechanism.
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class MutualChallengeResponse {
    /**
     * Size of the challenges, in bytes.
     */
    private static final int CHALL_SIZE = 64;

    /**
     * The source of randomness.
     */
    private final SecureRandom random;

    /**
     * The challenge to send to the remote location.
     */
    private final byte[] selfChallenge;

    /**
     * This entity's secret.
     */
    private final byte[] secret;
    
    /**
     * Indicates whether the application is the client or not.
     */
    private final boolean isClient;

    /**
     * Instantiates a new MutualChallengeResponse object. It should be used for only one authentication session.
     *
     * @param secret This entity's secret
     * @param isClient Indicates if the callee is the client or not. This changes the challenge so that reflection attacks are not possible.
     */
    public MutualChallengeResponse(byte[] secret, boolean isClient) {
        try {
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new BTC_Exception(ex);
        }

        this.isClient = isClient;
        
        // Force the random to seed itself securely.
        random.nextBytes(new byte[32]);

        // Calculate a challenge
        selfChallenge = new byte[CHALL_SIZE];
        random.nextBytes(selfChallenge);
        
        // Change the challenge, so that a server challenge cannot be used as a client challenge.
        int value = selfChallenge[CHALL_SIZE - 1];
        if(isClient) {
            // If the application is the client, set the last bit to 0.
            selfChallenge[CHALL_SIZE - 1] = (byte)(value & 0xFE);
        } else {
            // If the application is the server, set the last bit to 1.
            selfChallenge[CHALL_SIZE - 1] = (byte)(value | 0x01);
        }

        this.secret = secret;
    }

    /**
     * Retrieves the challenge to send.
     *
     * @return The challenge.
     */
    public byte[] getSelfChallenge() {
        return selfChallenge;
    }

    /**
     * Calculates the response to send to a challenge.
     *
     * @param remoteChallenge The remote challenge.
     * @return The response to send.
     */
    public byte[] getResponse(byte[] remoteChallenge) {
        if(remoteChallenge.length != CHALL_SIZE) {
            throw new BTC_Exception("Incorrect challenge size.");
        }
        
        if((remoteChallenge[CHALL_SIZE - 1] & 0x01) == (this.isClient ? 0 : 1)) {
            throw new BTC_Exception("Invalid challenge type received.");
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(selfChallenge);
            md.update(remoteChallenge);
            md.update(secret);
            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Authenticates the remote entity by comparing the expected response with the one received.
     *
     * @param remoteChallenge The challenge received from the remote entity.
     * @param remoteResponse The challenge received from the remote entity.
     * @return True if the remote entity provides the expected response and can be authenticated. False otherwise.
     */
    public boolean authenticate(byte[] remoteChallenge, byte[] remoteResponse) {
        if(remoteChallenge.length != CHALL_SIZE) {
            throw new BTC_Exception("Incorrect challenge size.");
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(remoteChallenge);
            md.update(selfChallenge);
            md.update(secret);
            byte[] digest = md.digest();
            return Arrays.equals(digest, remoteResponse);
        } catch (NoSuchAlgorithmException ex) {
            throw new BTC_Exception(ex);
        }
    }
}

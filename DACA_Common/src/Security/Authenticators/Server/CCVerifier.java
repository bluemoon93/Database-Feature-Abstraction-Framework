package Security.Authenticators.Server;

import LocalTools.BTC_Exception;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a Portuguese citizen card signature verification mechanism.
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class CCVerifier {

    /**
     * The source of randomness.
     */
    private final SecureRandom random;

    /**
     * The challenge to send to the remote location.
     */
    private final byte[] challenge;

    /**
     * The client's public certificate.
     */
    private final X509Certificate clientCert;

    /**
     * Instantiates a new MutualChallengeResponse object. It should be used for only one authentication session.
     *
     * @param userCert The user's certificate.
     */
    public CCVerifier(X509Certificate userCert) {
        assert userCert != null : "User certificate was null.";

        try {
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            throw new BTC_Exception(ex);
        }
        
        try {
            userCert.checkValidity();
        } catch (CertificateExpiredException | CertificateNotYetValidException ex) {
            throw new BTC_Exception(ex);
        }

        // Force the random to seed itself securely.
        random.nextBytes(new byte[32]);

        // Calculate a challenge
        challenge = new byte[64];
        random.nextBytes(challenge);

        this.clientCert = userCert;
    }

    /**
     * Retrieves the challenge to send.
     *
     * @return The challenge.
     */
    public byte[] getChallenge() {
        return challenge;
    }

    /**
     * Authenticates the remote entity by validating the signature using the provided certificate.
     *
     * @param signature The signature of the challenge.
     * @return True if the remote entity provides the expected signature and can be authenticated. False otherwise.
     */
    public boolean validate(byte[] signature) {
        try {
            Signature verSign = Signature.getInstance("SHA1withRSA", "BC");
            verSign.initVerify(clientCert);
            verSign.update(challenge);
            return verSign.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException ex) {
            Logger.getLogger(CCVerifier.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}

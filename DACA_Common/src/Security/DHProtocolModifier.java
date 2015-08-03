package Security;

import LocalTools.BTC_Exception;
import Security.ReflectionUtils;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DHProtocolModifier {

    public static void setup_SPEKE(byte[] secretHash, int minKeySize, int maxKeySize) {
        for (int keysize = minKeySize; keysize <= maxKeySize; keysize += 64) {
            try {
                setup_SPEKE(secretHash, keysize);
            } catch (InvalidParameterException ex) {
            }
        }
    }

    public static void setup_SPEKE(byte[] secretHash, int keySize) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
            kpg.initialize(keySize);
            kpg.generateKeyPair();

            BigInteger p = (BigInteger) ReflectionUtils.getFieldValue(
                    ReflectionUtils.getFieldValue(
                            ReflectionUtils.getFieldValue(kpg, "spi"),
                            "params"),
                    "p"
            );

            System.out.println(keySize + " [P]: " + p.toString(16));
            byte[] newG = new BigInteger(secretHash).pow(2).mod(p).toByteArray();

            ReflectionUtils.setFieldValue(
                    ReflectionUtils.getFieldValue(
                            ReflectionUtils.getFieldValue(kpg, "spi"),
                            "params"),
                    new BigInteger(newG),
                    "g"
            );

            System.out.println(keySize + " [G]: " + new BigInteger(newG).toString(16));
        } catch (NoSuchAlgorithmException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public static void main(String[] args) {
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-512");
            setup_SPEKE(m.digest("mysecret".getBytes()), 512, 2048);

            System.out.print("\nInitialising the KeyPairGenerator... ");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
            kpg.initialize(2048);
            System.out.println("OK!");

            System.out.print("Generating DH key pair... ");
            KeyPair kp = kpg.generateKeyPair();
            System.out.println("OK!");

            System.out.print("Getting keys... ");
            PrivateKey privateKey = kp.getPrivate();
            DHPublicKeySpec spec = getDHPublicKeySpec(kp.getPublic());
            BigInteger publicValue = spec.getY();
            BigInteger modulus = spec.getP();
            BigInteger base = spec.getG();
            System.out.println("OK!");

            System.out.println("\n***** Keys *****");
            System.out.println("Private: " + new BigInteger(privateKey.getEncoded()).toString(16));
            System.out.println("Public [Y]: " + publicValue.toString(16));
            System.out.println("Public [P]: " + modulus.toString(16));
            System.out.println("Public [G]: " + base.toString(16));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Could not generate DH keypair", ex);
        }
    }

    static DHPublicKeySpec getDHPublicKeySpec(PublicKey key) {
        if (key instanceof DHPublicKey) {
            DHPublicKey dhKey = (DHPublicKey) key;
            DHParameterSpec params = dhKey.getParams();
            return new DHPublicKeySpec(dhKey.getY(), params.getP(), params.getG());
        }
        try {
            KeyFactory factory = KeyFactory.getInstance("DH");
            return (DHPublicKeySpec) factory.getKeySpec(key, DHPublicKeySpec.class);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}

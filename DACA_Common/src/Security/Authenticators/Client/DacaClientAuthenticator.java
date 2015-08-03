package Security.Authenticators.Client;

import LocalTools.BTC_Exception;
import Security.CCHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public abstract class DacaClientAuthenticator {

    protected Socket socket;

    protected final int myListeningPort;

    public DacaClientAuthenticator(String dest, int port, int myListeningPort) {
        try {
            this.socket = new Socket(dest, port);
            this.myListeningPort = myListeningPort;
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public DacaClientAuthenticator(Socket socket, int myListeningPort) {
        this.socket = socket;
        this.myListeningPort = myListeningPort;
    }

    protected final String getSalt(Socket socket, String username) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(ClientAuthMsgs.GetSalt(username));
            out.flush();
            return in.readLine();
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    protected String hashPasswordToString(String password, String salt) {
        return new BigInteger(hashPasswordBytes(password, salt)).toString(16);
    }

    protected byte[] hashPasswordBytes(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update((password + salt).getBytes("UTF-8"));
            return md.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new BTC_Exception(ex);
        }
    }

    protected final String getHostIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            throw new BTC_Exception(ex);
        }
    }

    protected final int getHostListeningPort() {
        return myListeningPort;
    }

    /**
     * Handles the TFA (Two factor authentication) protocol with the server.
     *
     * @return True if the user was authenticated, false otherwise.
     */
    protected boolean secondStepAuthentication() {
        String autenticacaoCertifLabel = "CITIZEN AUTHENTICATION CERTIFICATE";

        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String authResult = in.readLine();
            if (authResult.equalsIgnoreCase("TFA")) {
                KeyStore ks = CCHelper.loadPkcs11();

                Certificate cert = ks.getCertificate(autenticacaoCertifLabel);
                out.println(ClientAuthMsgs.Certificate(new BigInteger(cert.getEncoded()).toString(16)));
                out.flush();

                String TFACertResp = in.readLine();
                if (TFACertResp.equalsIgnoreCase("AUTH_DENIED")) {
                    throw new BTC_Exception("Authentication denied.");
                }

                Key key = ks.getKey(autenticacaoCertifLabel, null);
                Signature sig = Signature.getInstance("SHA1withRSA", CCHelper.getProvider());
                sig.initSign((PrivateKey) key);

                byte[] challenge = new BigInteger(TFACertResp, 16).toByteArray();
                sig.update(challenge);
                byte[] signedHash = sig.sign();
                out.println(ClientAuthMsgs.Signature(new BigInteger(signedHash).toString(16)));
                out.flush();

                authResult = in.readLine();
            }
            System.out.println("\n\nRESULT GOT:\n"+authResult+"=AUTH_ACCEPTED\n");
            System.out.println("Returning "+(authResult.equalsIgnoreCase("AUTH_ACCEPTED")));
            return authResult.equalsIgnoreCase("AUTH_ACCEPTED");
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | InvalidKeyException | SignatureException | IOException | CertificateEncodingException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public abstract Socket authenticate(String appName, String username, String password);
}

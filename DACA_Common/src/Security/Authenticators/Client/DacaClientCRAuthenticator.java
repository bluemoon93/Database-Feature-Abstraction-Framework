package Security.Authenticators.Client;

import LocalTools.BTC_Exception;
import Security.MutualChallengeResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DacaClientCRAuthenticator extends DacaClientAuthenticator {

    public DacaClientCRAuthenticator(String dest, int port, int myListeningPort) {
        super(dest, port, myListeningPort);
    }

    public DacaClientCRAuthenticator(Socket socket, int myListeningPort) {
        super(socket, myListeningPort);
    }
    
    public DacaClientCRAuthenticator(String dest, int port) {
        super(dest, port, 0);
    }

    public DacaClientCRAuthenticator(Socket socket) {
        super(socket, 0);
    }

    @Override
    public Socket authenticate(String appName, String username, String password) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String salt = getSalt(socket, username);
            byte[] pwdbytes = hashPasswordBytes(password, salt);

            MutualChallengeResponse mcr = new MutualChallengeResponse(pwdbytes, true);
            out.println(ClientAuthMsgs.AuthChallenge(username, (new BigInteger(mcr.getSelfChallenge())).toString(16)));
            out.flush();
            
            String authResp = in.readLine();
            if(authResp.equalsIgnoreCase("NOT_ALLOWED")) {
                throw new BTC_Exception("Authentication using Challenge Response is not allowed by the server.");
            }

            byte[] remoteChallenge = (new BigInteger(in.readLine(), 16)).toByteArray();
            byte[] remoteResponse = (new BigInteger(in.readLine(), 16)).toByteArray();

            if (!mcr.authenticate(remoteChallenge, remoteResponse)) {
                out.println("NOK");
                out.flush();
                throw new BTC_Exception("Could not authenticate the server.");
            }

            out.println(ClientAuthMsgs.AuthChallengeResponse(appName, username, (new BigInteger(mcr.getResponse(remoteChallenge)).toString(16)), getHostIP(), getHostListeningPort()));
            out.flush();

            if (!secondStepAuthentication()) {
                throw new BTC_Exception("Second step authentication failed.");
            }

            if (in.readLine().equalsIgnoreCase("NOK")) {
                throw new BTC_Exception("DacaClientCRAuthenticator: Could not authenticate.");
            }

            return socket;
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }

    }
}

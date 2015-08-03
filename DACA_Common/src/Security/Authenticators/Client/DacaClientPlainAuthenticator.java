package Security.Authenticators.Client;

import LocalTools.BTC_Exception;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DacaClientPlainAuthenticator extends DacaClientAuthenticator {

    public DacaClientPlainAuthenticator(String dest, int port, int myListeningPort) {
        super(dest, port, myListeningPort);
    }

    public DacaClientPlainAuthenticator(Socket socket, int myListeningPort) {
        super(socket, myListeningPort);
    }
    
    public DacaClientPlainAuthenticator(String dest, int port) {
        super(dest, port, 0);
    }

    public DacaClientPlainAuthenticator(Socket socket) {
        super(socket, 0);
    }

    @Override
    public Socket authenticate(String appName, String username, String password) {
        System.out.println("at auth");
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("at auth1");
            String salt = getSalt(socket, username);
            password = hashPasswordToString(password, salt);
            System.out.println("at auth2");
            out.println(ClientAuthMsgs.AuthPlain(appName, username, password, getHostIP(), getHostListeningPort()));
            out.flush();
            System.out.println("at auth3");
            String authResp = in.readLine();
            if(authResp.equalsIgnoreCase("NOT_ALLOWED")) {
                throw new BTC_Exception("Plain authentication is not allowed by the server.");
            }
            System.out.println("at auth4");
            if (!secondStepAuthentication()) {
                throw new BTC_Exception("Second stepo authentication failed.");
            }
            System.out.println("at auth5");
            String nextRead=in.readLine();
            System.out.println("NextRead: "+nextRead);
            if (nextRead.equalsIgnoreCase("NOK")) {
                throw new BTC_Exception("DacaClientPlainAuthenticator: Could not authenticate.");
            }
            System.out.println("at end of auth");
            return socket;
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }
}

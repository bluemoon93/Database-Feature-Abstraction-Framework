package Security.Authenticators.Client;

import LocalTools.BTC_Exception;
import Security.PSKSSLAuthSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DacaClientPSKSSLAuthenticator extends DacaClientAuthenticator {

    public DacaClientPSKSSLAuthenticator(String dest, int port, int myListeningPort) {
        super(dest, port, myListeningPort);
    }

    public DacaClientPSKSSLAuthenticator(Socket socket, int myListeningPort) {
        super(socket, myListeningPort);
    }
    
    public DacaClientPSKSSLAuthenticator(String dest, int port) {
        super(dest, port, 0);
    }

    public DacaClientPSKSSLAuthenticator(Socket socket) {
        super(socket, 0);
    }

    @Override
    public Socket authenticate(String appName, String username, String password) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String salt = getSalt(socket, username);
            byte[] pwdbytes = hashPasswordBytes(password, salt);

            out.println(ClientAuthMsgs.UP_PSKSSL(username));
            out.flush();

            String authResp = in.readLine();
            if(authResp.equalsIgnoreCase("NOT_ALLOWED")) {
                throw new BTC_Exception("Authentication using PSKSSL is not allowed by the server.");
            }
            
            int sslPort = Integer.parseInt(in.readLine());
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            socket.close();
            socket = PSKSSLAuthSocketFactory.createSocket(sa.getHostString(), sslPort, pwdbytes);

            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(ClientAuthMsgs.AuthPSKSSL(appName, username, getHostIP(), getHostListeningPort()));
            out.flush();

            if (!secondStepAuthentication()) {
                throw new BTC_Exception("Second step authentication failed.");
            }

            if (in.readLine().equalsIgnoreCase("NOK")) {
                throw new BTC_Exception("DacaClientPSKSSLAuthenticator: Could not authenticate.");
            }

            return socket;
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }
}

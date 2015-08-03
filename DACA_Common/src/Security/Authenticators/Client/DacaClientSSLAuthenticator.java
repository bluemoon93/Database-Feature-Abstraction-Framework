package Security.Authenticators.Client;

import LocalTools.BTC_Exception;
import Security.DacaSSLSocketFactoryGenerator;
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
public class DacaClientSSLAuthenticator extends DacaClientAuthenticator {

    private final String certPath;
    private final String storePass;

    public DacaClientSSLAuthenticator(String dest, int port, int myListeningPort, String certPath, String storePass) {
        super(dest, port, myListeningPort);
        this.certPath = certPath;
        this.storePass = storePass;
    }

    public DacaClientSSLAuthenticator(Socket socket, int myListeningPort, String certPath, String storePass) {
        super(socket, myListeningPort);
        this.certPath = certPath;
        this.storePass = storePass;
    }
    
    public DacaClientSSLAuthenticator(String dest, int port, String certPath, String storePass) {
        super(dest, port, 0);
        this.certPath = certPath;
        this.storePass = storePass;
    }

    public DacaClientSSLAuthenticator(Socket socket, String certPath, String storePass) {
        super(socket, 0);
        this.certPath = certPath;
        this.storePass = storePass;
    }

    @Override
    public Socket authenticate(String appName, String username, String password) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(ClientAuthMsgs.UP_SSL());
            out.flush();

            String authResp = in.readLine();
            if(authResp.equalsIgnoreCase("NOT_ALLOWED")) {
                throw new BTC_Exception("Authentication using SSL is not allowed by the server.");
            }
            
            int sslPort = Integer.parseInt(in.readLine());
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            socket.close();
            socket = DacaSSLSocketFactoryGenerator.getSSLSocketFactory(certPath, storePass).createSocket(sa.getHostString(), sslPort);

            return new DacaClientCRAuthenticator(socket, getHostListeningPort()).authenticate(appName, username, password);
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }
}

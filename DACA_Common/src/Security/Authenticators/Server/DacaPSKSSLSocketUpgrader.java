package Security.Authenticators.Server;

import LocalTools.BTC_Exception;
import Security.PSKSSLAuthSocketFactory;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Receives a connection from a client and upgrades the socket to use SSL.
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DacaPSKSSLSocketUpgrader implements IDacaSocketUpgrader {
    
    private final byte[] secret;

    public DacaPSKSSLSocketUpgrader(byte[] secret) {
        this.secret = secret;
    }

    @Override
    public Socket upgradeSocket(Socket socket) {
        try {
            int port;
            try (ServerSocket server = new ServerSocket(0)) {
                port = server.getLocalPort();
            }

            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            out.println(String.format("%d", port));
            out.flush();
            socket.close();

            return PSKSSLAuthSocketFactory.createServerSocketAndAccept(port, secret);
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }
}

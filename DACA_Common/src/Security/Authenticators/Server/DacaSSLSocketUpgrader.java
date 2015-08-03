package Security.Authenticators.Server;

import LocalTools.BTC_Exception;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Receives a connection from a client and upgrades the socket to use SSL.
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DacaSSLSocketUpgrader implements IDacaSocketUpgrader {

    public DacaSSLSocketUpgrader() {
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

            return SSLServerSocketFactory.getDefault().createServerSocket(port).accept();
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }
}

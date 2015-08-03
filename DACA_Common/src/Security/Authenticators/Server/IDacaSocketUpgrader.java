package Security.Authenticators.Server;

import java.net.Socket;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public interface IDacaSocketUpgrader {

    public Socket upgradeSocket(Socket socket);
}

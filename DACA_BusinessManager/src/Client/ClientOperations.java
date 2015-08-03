/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * @author DIOGO
 */
public class ClientOperations {

    /**
     * Finds a free port for a socket.
     *
     * @return A free port number.
     * @throws IOException In case an IO problem raises.
     */
    public static int findFreePort() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) {
            return server.getLocalPort();
        }
    }

    /**
     * Finds the IP address of the machine.
     * @return The IP address of the machine.
     * @throws UnknownHostException If the host is unknown.
     */
    public static String getIp() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
}

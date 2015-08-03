package Server;

import LocalTools.BTC_Exception;
import Security.AuthenticationMethod;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

/**
 * A server that can handle multiple requests at once.
 */
public class MultiThreadedServer implements Runnable {

    /**
     * The listening socket.
     */
    private final ServerSocket serverSocket;

    /**
     * Flag that indicates if the server as been requested to stop.
     */
    private boolean isStopped = false;

    /**
     * Object responsible for executing the server operations.
     */
    //private final ServerOperations OP;
    
    /**
     * The authentication method to use when connecting to the client
     */
    private final AuthenticationMethod authMethod;

    /**
     * Instantiates a new multithreaded server.
     *
     * @param username The server access' username.
     * @param password The server access' password.
     * @param url The database url.
     * @param database The catalog name.
     * @param port The server port to use.
     * @param authMethod
     * @throws LocalTools.BTC_Exception If an error occurs.
     */
    public MultiThreadedServer(int port, AuthenticationMethod authMethod) throws BTC_Exception { // String username, String password, String url, String database, 
        try {
            //this.OP = new ServerOperations(username, password, url, database);
            this.serverSocket = new ServerSocket(port);
            //this.OP.addServerInfo(getIp(), port);
            //this.OP.ClearSessionTable();
            this.authMethod = authMethod;
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    @Override
    public void run() {
        try {
            while (!isStopped()) {
                try {
                    // Wait for a client to connect
                    System.out.println("Accepting connections...");
                    Socket clientSocket = this.serverSocket.accept();

                    // Instantiate a worker to handle the client.
                    System.out.println("Serving client request...");
                    (new Worker(clientSocket, authMethod)).start(); //OP.getUsername(), OP.getPassword(), OP.getUrl(), OP.getDatabase(), 
                } catch (IOException e) {
                    // Check if the server was stopped
                    if (isStopped) {
                        // if so send the exception to the outer try catch
                        throw e;
                    }
                    System.err.println("Error accepting client connection");
                }
            }
        } catch (IOException e) {
            // Stop the server
        }

        System.out.println("Server Stopped.");
    }

    /**
     * Checks if the server has been stopped.
     *
     * @return True it it has been stopped, false otherwise.
     */
    private boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Determines the IP address of this machine.
     *
     * @return The IP address as a String representation.
     * @throws BTC_Exception If an error occurs.
     */
    private static String getIp() throws BTC_Exception {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new BTC_Exception(e);
        }
    }

    /**
     * Stops the server.
     */
    public synchronized void stop() {
        this.isStopped = true;
        try {
            if (!this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing the listening socket.");
        }
    }
}

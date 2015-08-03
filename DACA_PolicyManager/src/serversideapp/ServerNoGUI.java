/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serversideapp;

import Configs.Reader;
import R4N.FT.FailureRecovery;
import R4N.FT.FaultTolerance_Disk;
import Security.AuthenticationMethod;
import Server.MultiThreadedServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;
import java.sql.SQLException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Starts the Policy Manager without a GUI.
 *
 * @author DIOGO
 */
public class ServerNoGUI {

    /**
     * Starts the Policy Manager without a GUI.
     *
     * @param args the command line arguments run:
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Incorrect number of parameters: <Username> <Password> <database url:port> <catalog> <port to use>");
            System.exit(1);
        }

        try {
            Reader.processConfigs();
            FailureRecovery.revertAll(Reader.getConn(), new FaultTolerance_Disk());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        Security.addProvider(new BouncyCastleProvider());
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        //Regateiro 123456 192.168.57.101:1433 PolicyServer2 9001
        //args[0], args[1], String.format("sqlserver://%s", args[2]), args[3],
        
        //Regateiro 123456 192.168.57.101:1433 PolicyServer2 
        
        // Instantiate the server
        MultiThreadedServer server = new MultiThreadedServer(
                Integer.parseInt(args[0]),
                AuthenticationMethod.PSKSSL);

        // Start the server
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Wait for user input to stop the server
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input;
        do {
            input = in.readLine();
        } while (!input.equalsIgnoreCase("q"));

        // Stop the server and wait for it to stop
        server.stop();
        serverThread.interrupt();
        serverThread.join();
        System.exit(0);
    }
}

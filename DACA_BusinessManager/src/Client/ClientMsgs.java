/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.math.BigInteger;

/**
 * Creates the different messages of communication protocol with the PolicyManager.
 *
 * @author DIOGO
 */
public abstract class ClientMsgs {
    /**
     * Creates an authentication message.
     *
     * @param appName  The name of the application to authenticate.
     * @param username The user name.
     * @param response The response to the challenge.
     * @param myip     The IP address of the client machine.
     * @param myport   The port of the client machine.
     * @return The authentication message.
     */
    public static String Auth(String appName, String username, String response, String myip, int myport) {
        return "AuthPlain " + appName + " " + username + " " + response + " " + myip + " " + myport;
    }

    /**
     * Creates a salt request message.
     *
     * @param username The user name.
     * @return The salt request message.
     */
    public static String GetSalt(String username) {
        return "GetSalt " + username;
    }

    /**
     * Creates a message to retrieve the Business Entities from the Policy BusinessManager.
     *
     * @return The message.
     */
    public static String GetBus() {
        return "GetBus";
    }

    /**
     * Creates a message to retrieve the Jar from the Policy BusinessManager.
     *
     * @return The message.
     */
    public static String GetJar() {
        return "GetJar";
    }

    static String Challenge(String username, String challenge) {
        return "AuthChallenge " + username + " " + challenge;
    }
}

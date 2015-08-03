/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Security.Authenticators.Client;

/**
 * Creates the different messages of communication protocol with the PolicyManager.
 *
 * @author DIOGO
 */
abstract class ClientAuthMsgs {
    /**
     * Creates an authentication message.
     *
     * @param appName  The name of the application to authenticate.
     * @param username The user name.
     * @param password The user password.
     * @param myip     The IP address of the client machine.
     * @param myport   The port of the client machine.
     * @return The authentication message.
     */
    public static String AuthPlain(String appName, String username, String password, String myip, int myport) {
        return "AuthPlain " + appName + " " + username + " " + password + " " + myip + " " + myport;
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
    
    static String AuthChallenge(String username, String challenge) {
        return "AuthChallenge " + username + " " + challenge;
    }
    
    /**
     * Creates an authentication message.
     *
     * @param appName  The name of the application to authenticate.
     * @param username The user name.
     * @param chalResp The challenge response.
     * @param myip     The IP address of the client machine.
     * @param myport   The port of the client machine.
     * @return The authentication message.
     */
    public static String AuthChallengeResponse(String appName, String username, String chalResp, String myip, int myport) {
        return "AuthChallengeResponse " + appName + " " + username + " " + chalResp + " " + myip + " " + myport;
    }
    
    public static String UP_PSKSSL(String username) {
        return "UP_PSKSSL " + username;
    }
    
    public static String UP_SSL() {
        return "UP_SSL";
    }
    
    /**
     * Creates an authentication message.
     *
     * @param appName  The name of the application to authenticate.
     * @param username The user name.
     * @param myip     The IP address of the client machine.
     * @param myport   The port of the client machine.
     * @return The authentication message.
     */
    public static String AuthPSKSSL(String appName, String username, String myip, int myport) {
        return "AuthPSKSSL " + appName + " " + username + " " + myip + " " + myport;
    }
    
    /**
     * Creates a certificate message.
     *
     * @param cert  The certificate.
     * @return The certificate message.
     */
    public static String Certificate(String cert) {
        return "TFACert " + cert;
    }
    
    /**
     * Creates a signature message.
     *
     * @param signature  The signature.
     * @return The signature message.
     */
    public static String Signature(String signature) {
        return "TFASign " + signature;
    }
}

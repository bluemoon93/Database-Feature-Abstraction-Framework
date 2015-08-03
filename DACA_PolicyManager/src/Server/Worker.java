package Server;

import JavaTools.BeInfo;
import JavaTools.Clients;
import LocalTools.BTC_Exception;
import Security.AuthenticationMethod;
import Security.Authenticators.Server.CCVerifier;
import Security.Authenticators.Server.DacaPSKSSLSocketUpgrader;
import Security.Authenticators.Server.DacaSSLSocketUpgrader;
import Security.MutualChallengeResponse;
import Security.CCHelper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the communication with a client.
 */
public class Worker extends Thread {

    private boolean allowPlainAuth = true;
    private boolean allowCRAuth = true;
    private final boolean allowPSKSSLAuth = true;
    private final boolean allowSSLAuth = true;
    private final boolean requireTwoStepAuthentication = false;

    /**
     * Socket stream reader.
     */
    private BufferedReader in;

    /**
     * Socket stream writer.
     */
    private PrintWriter out;

    /**
     * The authentication method to use when connecting to clients.
     */
    private final AuthenticationMethod authMethod;

    /**
     * The communication socket.
     */
    private Socket clientSocket;

    /**
     * Database connection handler.
     */
    private final DbHandler dbHandler;

    /**
     * Determines if the user is authorized.
     */
    private boolean isAuthorized;

    /**
     * Determines if the user is authorized.
     */
    private final boolean justForAuthentication;

    /**
     * The database relayer.
     */
    private final DatabaseRelayer dbr;

    /**
     * Instantiates an object of the type Worker.
     *
     * @param clientSocket The client's communication socket.
     * @param username The database access' username.
     * @param authMethod The authentication method to use when connecting to a client.
     * @throws LocalTools.BTC_Exception If an error occurs.
     */
    private Worker(Socket clientSocket, DbHandler dbHandler, AuthenticationMethod authMethod) throws BTC_Exception {
        assert clientSocket != null : "Client's socket reference was null.";
        assert !clientSocket.isClosed() : "Client's socket was closed.";
        assert dbHandler != null : "The dbHandler was null.";

        this.authMethod = authMethod;
        this.isAuthorized = false;
        this.clientSocket = clientSocket;
        this.dbHandler = dbHandler;
        this.justForAuthentication = true;
        this.dbr = new DatabaseRelayer(dbHandler.getSessionID()+"");
    }

    /**
     * Instantiates an object of the type Worker.
     *
     * @param clientSocket The client's communication socket.
     * @param username The database access' username.
     * @param password The database access' password.
     * @param url The database url.
     * @param database The catalog name.
     * @param authMethod The authentication method to use when connecting to a client.
     * @throws LocalTools.BTC_Exception If an error occurs.
     */
    public Worker(Socket clientSocket, AuthenticationMethod authMethod) throws BTC_Exception { //String username, String password, String url, String database, 
        assert clientSocket != null : "Client's socket reference was null.";
        assert !clientSocket.isClosed() : "Client's socket was closed.";
        //assert username != null : "The username was null.";
        //assert password != null : "The password was null.";
        //assert url != null : "The url was null.";
        //assert database != null : "The database was null.";

        this.authMethod = authMethod;
        this.isAuthorized = false;
        this.clientSocket = clientSocket;
        this.dbHandler = new DbHandler(); //username, password, url, database);
        this.justForAuthentication = false;
        this.dbr = new DatabaseRelayer(dbHandler.getSessionID()+"");
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            out = new PrintWriter(this.clientSocket.getOutputStream(), true);

            // Handle the inputs.
            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("Server got input: " + input);

                if (input.startsWith("UP_SSL")) {
                    handleUP_SSL();
                    // Since SSL is being used, update allowed authentication methods
                    allowPlainAuth = true;
                    allowCRAuth = true;
                } else if (input.startsWith("UP_PSKSSL")) {
                    handleUP_PSKSSL(input);
                    // Since SSL is being used, update allowed authentication methods
                    allowPlainAuth = true;
                    allowCRAuth = true;
                } else if (input.startsWith("AuthPlain")) {
                    handleAuthPlain(input);
                    if (justForAuthentication) {
                        System.out.println("thread returned");
                        return;
                    }
                } else if (input.startsWith("AuthChallengeServer")) {
                    handleAuthChallengeResponseServer(input);
                    if (justForAuthentication) {
                        return;
                    }
                } else if (input.startsWith("AuthChallenge")) {
                    handleAuthChallengeResponse(input);
                    if (justForAuthentication) {
                        return;
                    }
                } else if (input.startsWith("AuthPSKSSL")) {
                    handleAuthPSKSSL(input);
                    if (justForAuthentication) {
                        return;
                    }
                } else if (input.startsWith("GetSalt")) {
                    handleGetSalt(input);
                } else if (!isAuthorized) {
                    System.out.println("Not authenticated access attempt.");
                    out.println("NOT_AUTH");
                    out.flush();
                } else if (input.startsWith("insert")) {
                    handleInsert(input);
                } else if (input.startsWith("delete")) {
                    handleDelete(input);
                } else if (input.startsWith("delegation")) {
                    handleDelegation(input);
                } else if (input.startsWith("check_orchestration_drol")) {
                    handleCheckOrchestration_drol(input);
                } else if (input.startsWith("check_orchestration")) {
                    handleCheckOrchestration(input);
                } else if (input.startsWith("toggle_orchestration")) {
                    handleToogleOrchestration();
                } else if (input.startsWith("GetBus")) {
                    handleGetBus();
                } else if (input.startsWith("getJar")) {
                    handleGetJar(input);
                } else if (input.startsWith("getRoles")) {
                    handleGetRoles(input);
                } else if (input.startsWith("getCRUDs")) {
                    handleGetCRUDs(input);
                } else if (input.startsWith("getBEUrl")) {
                    handleGetBEUrl(input);
                } else if (input.startsWith("getBEsIDs")) {
                    handleGetBEsIDs(input);
                } else if (input.startsWith("getSeqInfo")) {
                    handleGetSeqInfo(input);
                } else if (input.startsWith("getSeqStatus")) {
                    handleGetSeqStatus();
                } else if (input.startsWith("getJarForInfo")) {
                    handleGetJarForInfo(input);
                } else if (input.startsWith("End")) {
                    handleEnd(input);
                } else if (input.startsWith("Data")) {
                    System.out.println("Starting data mode");
                    dbr.relay(clientSocket);
                } else {
                    System.err.println("Unknown input [" + input + "] received!");
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new BTC_Exception(e);
        } finally {
            // Clean up
            if (!justForAuthentication) {
                try {
                    dbHandler.close();
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("There was a problem cleaning up the worker.");
                }
            }
        }
        System.out.println("Thread finished");
    }

    /**
     * Handles the get sequence status input.
     *
     * @throws java.io.IOException If an IO error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleGetSeqStatus() throws IOException, SQLException {
        out.println(dbHandler.getSeqStatus());
        out.flush();
    }

    /**
     * Handles the get sequence info input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleGetSeqInfo(String input) throws IOException, SQLException {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

        Map<Integer, List<Map.Entry<String, List<String>>>> seqInfo = dbHandler.getSequenceInfoForRole(inputFields[1]);
        for (int seqID : seqInfo.keySet()) {
            List<Map.Entry<String, List<String>>> sequence = seqInfo.get(seqID);
            for (int pos = 0; pos < sequence.size(); pos++) {
                out.println(String.format("%d %d %s", seqID, pos, sequence.get(pos).getKey()));
                out.flush();
            }
        }
        out.println("END");
        out.flush();
    }

    /**
     * Handles the get bes ids input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleGetBEsIDs(String input) throws IOException, SQLException {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

        List<Integer> bEsIDs = dbHandler.getBEsIDsForRole(inputFields[1]);
        for (int beid : bEsIDs) {
            out.println(beid);
        }
        out.println("END");
        out.flush();
    }

    /**
     * Handles the get be url input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleGetBEUrl(String input) throws IOException, SQLException {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

        out.println(dbHandler.getBusinessURLByID(Integer.parseInt(inputFields[1])));
        out.flush();
    }

    /**
     * Handles the check orchestration input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleGetCRUDs(String input) throws IOException, SQLException {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

        Map<Integer, String> cruds = dbHandler.getCRUDsByBE(Integer.parseInt(inputFields[1]));
        for (int crudid : cruds.keySet()) {
            String crudref = cruds.get(crudid);
            out.println(String.format("%d %s", crudid, crudref));
            out.flush();
        }
        out.println("END");
        out.flush();
    }

    /**
     * Handles the check orchestration input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleGetRoles(String input) throws IOException, SQLException {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

        Map<String, String> roles = dbHandler.getRolesByAppRef(inputFields[1]);
        for (String role : roles.keySet()) {
            out.println(String.format("%s %s", role, roles.get(role)));
            out.flush();
        }
        out.println("END");
        out.flush();
    }

    /**
     * Handles the toogle orchestration input.
     *
     * @throws java.io.IOException If an IO error occurs.
     * @throws java.sql.SQLException If an SQL error occurs.
     */
    private void handleToogleOrchestration() throws IOException, SQLException {
        ArrayList<Clients> clients = dbHandler.getAllClients();

        // Check to avoid getting the orchestration status if no clients exist.
        if (!clients.isEmpty()) {
            boolean orqStatus = dbHandler.getSeqStatus();

            // For each client
            for (Clients client : clients) {
                try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                    try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                        // Send the new orchestration status
                        outclient.println("toggle_orchestration");
                        outclient.println(orqStatus);
                        outclient.flush();
                    }
                } catch (ConnectException ex) {
                    dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                }
            }

        }
    }

    /**
     * Handles the check orchestration input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     */
    private void handleCheckOrchestration(String input) throws BTC_Exception {
        assert input != null : "Input reference was null.";

        try {
            String[] inputFields = input.split(" ");
            assert inputFields.length == 2 : "Invalid input format, expected [check_orchestratrion <seqId>], received [" + input + "].";

            // Obtain the sequence identifier to be checked for changes.
            int seq = Integer.parseInt(inputFields[1]);

            // Obtain the list of active clients.
            ArrayList<Clients> clients = dbHandler.getClientsFromSequence(seq);

            if (!clients.isEmpty()) {
                // Obtain the sequence metadata ( list of beurl to revoke list)
                List<Map.Entry<String, List<String>>> seqinfo = dbHandler.getSequenceInfo(seq);

                // For each client
                for (Clients client : clients) {
                    try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                        try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                            // Send the type of message
                            outclient.println("check_orchestration");

                            // Send the sequence identifier and size
                            outclient.println(seq);
                            outclient.println(seqinfo.size());

                            // For each beurl and revoke list
                            for (Map.Entry<String, List<String>> seqInfoEntry : seqinfo) {
                                String beurl = seqInfoEntry.getKey();
                                List<String> revokelist = seqInfoEntry.getValue();

                                // send the be url and the revoke list size
                                outclient.println(beurl);
                                outclient.println(revokelist.size());

                                // send the revoke list contents
                                for (String revokebe : revokelist) {
                                    outclient.println(revokebe);
                                }
                            }

                            outclient.flush();
                        }
                    } catch (ConnectException ex) {
                        dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                    }
                }

            }
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the check orchestration input.
     *
     * @param input The input from the client / database.
     * @throws LocalTools.BTC_Exception If an error occurs.
     */
    private void handleCheckOrchestration_drol(String input) throws BTC_Exception {
        assert input != null : "Input reference was null.";

        try {
            String[] inputFields = input.split(" ");
            assert inputFields.length == 3 : "Invalid input format, expected [check_orchestratrion_drol <seqId> <roleid>], received [" + input + "].";

            // Obtain the sequence identifier to be checked for changes.
            int seq = Integer.parseInt(inputFields[1]);
            int roleid = Integer.parseInt(inputFields[2]);

            // Obtain the list of active clients.
            ArrayList<Clients> clients = dbHandler.getClientsForRole(roleid);

            if (!clients.isEmpty()) {
                // For each client
                for (Clients client : clients) {
                    try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                        try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                            // Send the type of message
                            outclient.println("delete_orchestration");
                            // Send the sequence identifier
                            outclient.println(seq);
                            outclient.flush();
                        }
                    } catch (ConnectException ex) {
                        dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                    }
                }

            }
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the get jar for info input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleGetJarForInfo(String input) throws BTC_Exception {
        assert input != null : "Input reference was null.";

        try {
            String[] inputFields = input.split(" ");
            assert inputFields.length == 2 : "Invalid input format, expected [getJarForInfo <AppName>], received [" + input + "].";

            // get the jar from the policy server
            byte[] jarbytes = dbHandler.GetJar(inputFields[1]);
            if (jarbytes != null) {
                // If the bytes are retrieved, send "OK" to the client.
                out.println("OK");
                out.flush();

                // Send the length of the jar
                out.println(jarbytes.length);
                out.flush();

                // Send the jar itself
                IOUtils.write(jarbytes, this.clientSocket.getOutputStream());
            } else {
                // If the bytes are null, send a "NOK"
                out.println("NOK");
                out.flush();
            }
        } catch (IOException e) {
            throw new BTC_Exception(e);
        }
    }

    /**
     * Handles the get jar input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleGetJar(String input) throws BTC_Exception {
        try {
            byte[] jarbytes = dbHandler.GetJar(input.split(" ")[1]);

            // Send Jar size
            System.out.println("Sending size "+jarbytes.length);
            out.println(jarbytes.length);
            out.flush();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Send the Jar bytes
            System.out.print("Sending jar.... ");
            OutputStream os = this.clientSocket.getOutputStream();
            os.write(jarbytes);
            os.flush();
            System.out.println("Done!");
        } catch (IOException e) {
            throw new BTC_Exception(e);
        }
    }

    /**
     * Handles the delegation input.
     *
     * @param input The input from the client / database.
     */
    private void handleDelegation(String input) throws BTC_Exception {
        assert input != null : "Input was null.";

        String[] inputFields = input.split(" ");
        assert inputFields.length == 4 : "Input was in an incorrect format. Received [" + input + "].";

        if (inputFields[1].compareTo("insert") == 0) {
            handleDelegationInsert(inputFields);
        } else if (inputFields[1].compareTo("delete") == 0) {
            handleDelegationDelete(inputFields);
        } else {
            throw new BTC_Exception("Invalid delegation operand [" + inputFields[1] + "].");
        }
    }

    /**
     * Handles the delegation delete input.
     *
     * @param inputFields The input fields.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleDelegationDelete(String[] inputFields) throws BTC_Exception {
        try {
            // Get the list of clients
            ArrayList<Clients> clients = dbHandler.getClientsFromSubApp(Integer.parseInt(inputFields[2]));
            if (!clients.isEmpty()) {
                // Get the unavailable Schemas for these clients.
                Map<String, ArrayList<BeInfo>> info = dbHandler.getDeleteDelegationInfo(
                        Integer.parseInt(inputFields[2]),
                        Integer.parseInt(inputFields[3])
                );

                // Get the updated orchestration info.
                Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo = dbHandler.getControlInfo();

                // For each client
                for (Clients client : clients) {
                    try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                        try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                            // Send the message type
                            outclient.println("delete");
                            // Send the available business schemas size
                            outclient.println(info.size());

                            // For each schema
                            for (Map.Entry<String, ArrayList<BeInfo>> entry : info.entrySet()) {
                                // Send the schema url
                                outclient.println(entry.getKey());
                                // Send the number of cruds associated
                                outclient.println(entry.getValue().size());

                                // For each crud
                                for (int j = 0; j < entry.getValue().size(); j++) {
                                    // Send the crud identifier
                                    outclient.println(entry.getValue().get(j).getCrudid());
                                }
                            }

                            // Send the size of the orchestration info
                            outclient.println(controlinfo.size());

                            // For each sequence
                            for (Integer seq : controlinfo.keySet()) {
                                // Send the sequence identifier
                                outclient.println(seq);
                                // Send the size of the sequence
                                outclient.println(controlinfo.get(seq).size());

                                // For each Business Schema in the sequence
                                for (int j = 0; j < controlinfo.get(seq).size(); j++) {
                                    String beurl = controlinfo.get(seq).get(j).getKey();
                                    List<String> revokelist = controlinfo.get(seq).get(j).getValue();

                                    // Send the beurl of the Business Schema
                                    outclient.println(beurl);
                                    // Send the size of the revocation list
                                    outclient.println(revokelist.size());

                                    // For each Business Schema url in the list
                                    for (String revokebe : revokelist) {
                                        // Send the Business Schema url
                                        outclient.println(revokebe);
                                    }
                                }
                            }

                            outclient.flush();
                        }
                    } catch (ConnectException ex) {
                        dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                    }
                }
            }
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the delegation insert input.
     *
     * @param inputFields The input fields.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleDelegationInsert(String[] inputFields) throws BTC_Exception {
        try {
            // Get the list of active clients
            ArrayList<Clients> clients = dbHandler.getClientsFromSubApp(Integer.parseInt(inputFields[2]));

            if (!clients.isEmpty()) {

                // Get the available Schemas for these clients.
                Map<String, ArrayList<BeInfo>> info = dbHandler.getInsertDelegationInfo(
                        Integer.parseInt(inputFields[2]),
                        Integer.parseInt(inputFields[3])
                );

                // Get the updated orchestration info.
                Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo = dbHandler.getControlInfo();

                // For each client
                for (Clients client : clients) {
                    try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                        try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                            // Send the message type
                            outclient.println("add");

                            // Orchestration info needs to be send first because it is needed to generate the code for the following Business Schemas
                            // Send the size of the orchestration info
                            outclient.println(controlinfo.size());

                            // For each sequence
                            for (Integer seq : controlinfo.keySet()) {
                                // Send the sequence identifier
                                outclient.println(seq);
                                // Send the size of the sequence
                                outclient.println(controlinfo.get(seq).size());

                                // For each Business Schema in the sequence
                                for (int j = 0; j < controlinfo.get(seq).size(); j++) {
                                    String beurl = controlinfo.get(seq).get(j).getKey();
                                    List<String> revokelist = controlinfo.get(seq).get(j).getValue();

                                    // Send the beurl of the Business Schema
                                    outclient.println(beurl);
                                    // Send the size of the revocation list
                                    outclient.println(revokelist.size());

                                    // For each Business Schema url in the list
                                    for (String revokebe : revokelist) {
                                        // Send the Business Schema url
                                        outclient.println(revokebe);
                                    }
                                }
                            }

                            // Send the available business schemas size
                            outclient.println(info.size());

                            // For each schema
                            for (Map.Entry<String, ArrayList<BeInfo>> entry : info.entrySet()) {
                                // Send the schema url
                                outclient.println(entry.getKey());
                                // Send the number of cruds associated
                                outclient.println(entry.getValue().size());

                                // For each crud
                                for (int j = 0; j < entry.getValue().size(); j++) {
                                    // Send the crud identifier
                                    outclient.println(entry.getValue().get(j).getCrudid());
                                    // Send the crud itself
                                    outclient.println(entry.getValue().get(j).getCrud_str());
                                }
                            }

                            outclient.flush();
                        }
                    } catch (ConnectException ex) {
                        dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                    }
                }

            }
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the delete input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleDelete(String input) throws BTC_Exception {
        try {
            String[] inputFields = input.split(" ");
            assert inputFields.length == 4 : "Input was in an incorrect format. Received [" + input + "].";
            // Get the list of active clients
            ArrayList<Clients> clients = dbHandler.getClientsFromSubApp(Integer.parseInt(inputFields[2]));

            if (clients.size() > 0) {
                // Get the list of deleted Business Schemas for the clients.
                Map<String, ArrayList<BeInfo>> info = dbHandler.getAut_ChangesDeletion(
                        Integer.parseInt(inputFields[2]),
                        Integer.parseInt(inputFields[3])
                );

                // Get the updated orchestration info.
                Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo = dbHandler.getControlInfo();

                for (Clients client : clients) {
                    try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                        try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                            if (info != null) {
                                // Send the message type
                                outclient.println("delete");
                                // Send the unavailable business schemas size
                                outclient.println(info.size());

                                // For each entry in the unavailable business schema info
                                for (Map.Entry<String, ArrayList<BeInfo>> entry : info.entrySet()) {
                                    // Send the business schema url
                                    outclient.println(entry.getKey());
                                    // Send the number of associated cruds
                                    outclient.println(entry.getValue().size());

                                    // For each crud
                                    for (int j = 0; j < entry.getValue().size(); j++) {
                                        // Send the crud identifier
                                        outclient.println(entry.getValue().get(j).getCrudid());
                                    }

                                }

                                // Send the size of the orchestration info
                                outclient.println(controlinfo.size());

                                // For each sequence
                                for (Integer seq : controlinfo.keySet()) {
                                    // Send the sequence identifier
                                    outclient.println(seq);
                                    // Send the size of the sequence
                                    outclient.println(controlinfo.get(seq).size());

                                    // For each Business Schema in the sequence
                                    for (int j = 0; j < controlinfo.get(seq).size(); j++) {
                                        String beurl = controlinfo.get(seq).get(j).getKey();
                                        List<String> revokelist = controlinfo.get(seq).get(j).getValue();

                                        // Send the beurl of the Business Schema
                                        outclient.println(beurl);
                                        // Send the size of the revocation list
                                        outclient.println(revokelist.size());

                                        // For each Business Schema url in the list
                                        for (String revokebe : revokelist) {
                                            // Send the Business Schema url
                                            outclient.println(revokebe);
                                        }
                                    }
                                }

                                outclient.flush();
                            } else {
                                outclient.println("no_change_role_deleted");
                                outclient.flush();
                            }
                        } catch (ConnectException ex) {
                            dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                        }
                    }

                }
            }
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the insert input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleInsert(String input) throws BTC_Exception {
        try {
            String[] inputFields = input.split(" ");
            assert inputFields.length == 4 : "Input was in an incorrect format. Received [" + input + "].";
            // Get the list of active clients
            ArrayList<Clients> clients = dbHandler.getClientsFromSubApp(Integer.parseInt(inputFields[2]));

            if (!clients.isEmpty()) {
                // Get the list of inserted Business Schemas for the clients.
                Map<String, ArrayList<BeInfo>> info = dbHandler.getAut_ChangesInsertion(
                        Integer.parseInt(inputFields[2]),
                        Integer.parseInt(inputFields[3])
                );

                // Get the updated orchestration info.
                Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo = dbHandler.getControlInfo();

                // For each client
                for (Clients client : clients) {
                    try (Socket s = authenticate(new Socket(client.getIp(), client.getPort()))) {
                        try (PrintWriter outclient = new PrintWriter(s.getOutputStream(), true)) {
                            // Send the message type
                            outclient.println("add");

                            // Orchestration info needs to be send first because it is needed to generate the code for the following Business Schemas
                            // Send the size of the orchestration info
                            outclient.println(controlinfo.size());

                            // For each sequence
                            for (Integer seq : controlinfo.keySet()) {
                                // Send the sequence identifier
                                outclient.println(seq);
                                // Send the size of the sequence
                                outclient.println(controlinfo.get(seq).size());

                                // For each Business Schema in the sequence
                                for (int j = 0; j < controlinfo.get(seq).size(); j++) {
                                    String beurl = controlinfo.get(seq).get(j).getKey();
                                    List<String> revokelist = controlinfo.get(seq).get(j).getValue();

                                    // Send the beurl of the Business Schema
                                    outclient.println(beurl);
                                    // Send the size of the revocation list
                                    outclient.println(revokelist.size());

                                    // For each Business Schema url in the list
                                    for (String revokebe : revokelist) {
                                        // Send the Business Schema url
                                        outclient.println(revokebe);
                                    }
                                }
                            }

                            // Send the unavailable business schemas size
                            outclient.println(info.size());

                            // For each entry in the available business schema info
                            for (Map.Entry<String, ArrayList<BeInfo>> entry : info.entrySet()) {
                                // Send the business schema url
                                outclient.println(entry.getKey());
                                // Send the number of associated cruds
                                outclient.println(entry.getValue().size());

                                // For each crud
                                for (int j = 0; j < entry.getValue().size(); j++) {
                                    // Send the crud identifier
                                    outclient.println(entry.getValue().get(j).getCrudid());
                                    // Send the crud itself
                                    outclient.println(entry.getValue().get(j).getCrud_str());
                                }
                            }

                            outclient.flush();
                        }
                    } catch (ConnectException ex) {
                        dbHandler.EndSession(client.getIp(), Integer.toString(client.getPort()));
                    }
                }
            }
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the get bus input.
     */
    private void handleGetBus() {
        Map<String, ArrayList<BeInfo>> info = new HashMap<>();

        // Send the session identifier
        out.println(dbHandler.getSessionID());

        //Obtencao de roles
        Map<String, String> roles = dbHandler.getRoles();
        roles.putAll(dbHandler.getDelegationsInfo());
        for (String key : roles.keySet()) {
            dbHandler.getRoleInfo(key, info);
        }

        // Send the size of the list of roles
        out.println(info.size());

        for (Map.Entry<String, ArrayList<BeInfo>> entry : info.entrySet()) {

            // Send the BE url
            out.println(entry.getKey());
            // Send the size of the list of cruds
            out.println(entry.getValue().size());

            // Send the list of cruds
            for (int i = 0; i < entry.getValue().size(); i++) {
                // Send the crud identifier
                out.println(entry.getValue().get(i).getCrudid());
                // Send the crud remote call identifier.
                out.println(dbHandler.createSessionQuery(entry.getValue().get(i).getCrud_ref()));
                // Send the crud itself
                out.println(entry.getValue().get(i).getCrud_str());
            }
        }

        // Send whether orquestration is active or not
        out.println(dbHandler.getSeqStatus());

        // Get the orchestration info for the roles
        Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo = dbHandler.getControlInfoForRoles(roles);

        // Send the size of the orchestration info
        out.println(controlinfo.size());

        // For each sequence
        for (Integer seq : controlinfo.keySet()) {
            // Send the sequence identifier
            out.println(seq);
            // Send the size of the sequence
            out.println(controlinfo.get(seq).size());

            // Send each BE reference in the sequence
            for (int i = 0; i < controlinfo.get(seq).size(); i++) {
                String beurl = controlinfo.get(seq).get(i).getKey();
                List<String> revokelist = controlinfo.get(seq).get(i).getValue();

                // Send the BE url
                out.println(beurl);
                // Send the size of the revocation list
                out.println(revokelist.size());

                // Send the revocation list
                for (String revokebe : revokelist) {
                    // Send the url of the BE in the revocation list
                    out.println(revokebe);
                }
            }
        }

        out.flush();
    }

    /**
     * Handles the get jar input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleAuthChallengeResponse(String input) throws BTC_Exception {
        if (!allowCRAuth) {
            out.println("NOT_ALLOWED");
            out.flush();
            return;
        } else {
            out.println("AUTHENTICATING");
            out.flush();
        }

        try {
            // Handle the first part of the challenge response
            String[] inputFields = input.split(" ");
            assert inputFields.length == 3 : "Input was in an incorrect format. Received [" + input + "].";

            byte[] secret = dbHandler.getSecret(inputFields[1]);
            byte[] remoteChallenge = (new BigInteger(inputFields[2], 16)).toByteArray();
            MutualChallengeResponse mcr = new MutualChallengeResponse(secret, false);
            out.println((new BigInteger(mcr.getSelfChallenge())).toString(16));
            out.println((new BigInteger(mcr.getResponse(remoteChallenge))).toString(16));
            out.flush();

            input = in.readLine();
            if (input.equalsIgnoreCase("NOK")) {
                throw new BTC_Exception("Client refused server authentication.");
            }

            System.out.println("input: " + input);
            inputFields = input.split(" ");
            assert inputFields.length == 6 : "Input was in an incorrect format. Received [" + input + "].";
            if (!inputFields[0].equalsIgnoreCase("AuthChallengeResponse")) {
                out.println("NOK");
                out.flush();
                throw new BTC_Exception("Unexpected Message: " + input);
            }

            if (!mcr.authenticate(remoteChallenge, (new BigInteger(inputFields[3], 16)).toByteArray())) {
                out.println("NOK");
                out.flush();
                throw new BTC_Exception("Could not authenticate client.");
            }

            handleSecondStepAuthentication(inputFields[2]);

            // Try to login to the policy server
            System.out.println("trying to login");
            if (dbHandler.TryToLogin(inputFields[1], inputFields[2], inputFields[4], inputFields[5])) {
                // If successful, send "OK"
                out.println("OK");
                isAuthorized = true;
            } else {
                // If unsucessful, send "NOK"
                out.println("NOK");
            }
            System.out.println("done trying to login");

            out.flush();
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the get jar input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleAuthChallengeResponseServer(String input) throws BTC_Exception {
        try {
            // Handle the first part of the challenge response
            String[] inputFields = input.split(" ");
            assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

            byte[] secret = "O,!{<p&)L#@*|]{4].~^x:1)p}>VU\"91:'TpD*&[@v+#m%X|;{#8_;8X%ib[oH1/g\"..| ^h({(t^C8}_7%}/-w#Z;{;8XP^%H#D".getBytes("UTF-8");
            byte[] remoteChallenge = new byte[64];
            for (int i = 0; i < 64; i++) {
                remoteChallenge[i] = (byte) Integer.parseInt("" + inputFields[1].charAt(i * 2) + inputFields[1].charAt((i * 2) + 1), 16);
            }

            MutualChallengeResponse mcr = new MutualChallengeResponse(secret, false);
            for (byte b : mcr.getSelfChallenge()) {
                out.print(String.format("%02x", b));
            }
            for (byte b : mcr.getResponse(remoteChallenge)) {
                out.print(String.format("%02x", b));
            }
            out.flush();

            input = in.readLine();
            if (input.equalsIgnoreCase("NOK")) {
                throw new BTC_Exception("Client refused server authentication.");
            }

            System.out.println("input: " + input);
            inputFields = input.split(" ");
            assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";
            if (!inputFields[0].equalsIgnoreCase("AuthChallengeResponseServer")) {
                out.print("NOK.");
                out.flush();
                throw new BTC_Exception("Unexpected Message: " + input);
            }

            byte[] remoteResponse = new byte[64];
            for (int i = 0; i < 64; i++) {
                remoteResponse[i] = (byte) Integer.parseInt("" + inputFields[1].charAt(i * 2) + inputFields[1].charAt((i * 2) + 1), 16);
            }

            if (!mcr.authenticate(remoteChallenge, remoteResponse)) {
                out.print("NOK.");
                out.flush();
                throw new BTC_Exception("Could not authenticate client.");
            }

            out.print("OK.");
            out.flush();
            isAuthorized = true;
        } catch (IOException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Handles the get jar input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleAuthPlain(String input) throws BTC_Exception {
        System.out.println("HEREEEEEEEEE");
        if (!allowPlainAuth) {
            out.println("NOT_ALLOWED");
            out.flush();
            return;
        } else {
            out.println("AUTHENTICATING");
            out.flush();
        }
        System.out.println("HEREEEEEEEEE");
        // Handle the first part of the challenge response
        String[] inputFields = input.split(" ");
        assert inputFields.length == 6 : "Input was in an incorrect format. Received [" + input + "].";
        System.out.println("HEREEEEEEEEE");
        handleSecondStepAuthentication(inputFields[2]);
        System.out.println("HEREEEEEEEEE");
        // Try to login to the policy server
        System.out.println("Authing with "+inputFields[1]+" "+inputFields[2]+" "+inputFields[3]+" "+inputFields[4]+" "+inputFields[5]);
        if (dbHandler.TryToLogin(inputFields[1], inputFields[2], inputFields[3], inputFields[4], inputFields[5])) {
            // If successful, send "OK"
            out.println("OK");
            isAuthorized = true;
        } else {
            // If unsucessful, send "NOK"
            out.println("NOK");
        }
        System.out.println("authed");
        out.flush();
        System.out.println("authed and flushed");
    }

    /**
     * Handles the auth with PSKSSL.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleAuthPSKSSL(String input) throws BTC_Exception {
        // Handle the first part of the challenge response
        String[] inputFields = input.split(" ");
        assert inputFields.length == 5 : "Input was in an incorrect format. Received [" + input + "].";

        handleSecondStepAuthentication(inputFields[2]);

        // Try to login to the policy server
        if (dbHandler.TryToLogin(inputFields[1], inputFields[2], inputFields[3], inputFields[4])) {
            // If successful, send "OK"
            out.println("OK");
            isAuthorized = true;
        } else {
            // If unsucessful, send "NOK"
            out.println("NOK");
        }

        out.flush();
    }

    /**
     * Handles the salt input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleGetSalt(String input) throws BTC_Exception {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";

        out.println(dbHandler.getSalt(inputFields[1]));
        out.flush();
    }

    /**
     * Handles the end input.
     *
     * @param input The input from the client / database.
     * @throws BTC_Exception If an error occurs.
     */
    private void handleEnd(String input) throws BTC_Exception {
        String[] inputFields = input.split(" ");
        assert inputFields.length == 3 : "Input was in an incorrect format. Received [" + input + "].";

        dbHandler.EndSession(inputFields[1], inputFields[2]);
    }

    private void handleUP_SSL() throws IOException {
        if (!allowSSLAuth) {
            out.println("NOT_ALLOWED");
            out.flush();
            return;
        } else {
            out.println("AUTHENTICATING");
            out.flush();
        }

        clientSocket = new DacaSSLSocketUpgrader().upgradeSocket(clientSocket);
        in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
        out = new PrintWriter(this.clientSocket.getOutputStream(), true);
    }

    private void handleUP_PSKSSL(String input) throws IOException {
        if (!allowPSKSSLAuth) {
            out.println("NOT_ALLOWED");
            out.flush();
            return;
        } else {
            out.println("AUTHENTICATING");
            out.flush();
        }

        String[] inputFields = input.split(" ");
        assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";
        byte[] clientSecret = dbHandler.getSecret(inputFields[1]);
        clientSocket = new DacaPSKSSLSocketUpgrader(clientSecret).upgradeSocket(clientSocket);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @SuppressWarnings("CallToThreadRun")
    private Socket authenticate(Socket s) throws IOException {
        PrintWriter sout = new PrintWriter(s.getOutputStream(), true);

        switch (authMethod) {
            case PLAIN:
                sout.println("Plain");
                break;
            case ChallengeResponse:
                sout.println("Challenge");
                break;
            case PSKSSL:
                sout.println("PSKSSL");
                break;
            case SSL:
            default:
                sout.println("SSL");
        }
        sout.flush();
        Worker tempWorker = new Worker(s, dbHandler, authMethod);
        tempWorker.run(); // blocking call to run method
        return tempWorker.clientSocket;
    }

    private void handleSecondStepAuthentication(String username) {
        System.out.println("\n\nGoing for 2step!");
        if (requireTwoStepAuthentication) {
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                out.println("TFA"); // Tell the client that the two factor authentication is required.
                out.flush();

                // Receive the public certificate from the client.
                String input = in.readLine();
                System.out.println("input: " + input);
                String[] inputFields = input.split(" ");
                assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";
                if (!inputFields[0].equalsIgnoreCase("TFACert")) {
                    out.println("AUTH_DENIED");
                    out.flush();
                    throw new BTC_Exception("Unexpected Message: " + input);
                }

                // Convert the bytes received into a X509Certificate
                X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(new BigInteger(inputFields[1], 16).toByteArray()));
                cert.checkValidity();

                // Retrieve the BI number from the database for this user.
                String UserBI = dbHandler.getUserBI(username);

                // Retrieve the BI number from the certificate.
                String CertBI = cert.getSubjectX500Principal().toString();
                int fieldIdx = CertBI.indexOf("SERIALNUMBER=BI");
                if (fieldIdx == -1) {
                    out.println("AUTH_DENIED");
                    out.flush();
                    throw new BTC_Exception("Invalid card. No BI found.");
                }

                CertBI = CertBI.substring(fieldIdx + 15, CertBI.indexOf(",", fieldIdx));

                if (!CertBI.equalsIgnoreCase(UserBI)) {
                    out.println("AUTH_DENIED");
                    out.flush();
                    throw new BTC_Exception("Certificate BI does not match user BI");
                }

                if (!CCHelper.validate(cert)) {
                    out.println("AUTH_DENIED");
                    out.flush();
                    throw new BTC_Exception("User certificate not valid.");
                }

                // Initialize the verifier and send a challenge to the client.
                CCVerifier ccVer = new CCVerifier(cert);
                out.println(new BigInteger(ccVer.getChallenge()).toString(16));
                out.flush();

                // Get the signature of the challenge from the client.
                input = in.readLine();
                System.out.println("input: " + input);
                inputFields = input.split(" ");
                assert inputFields.length == 2 : "Input was in an incorrect format. Received [" + input + "].";
                if (!inputFields[0].equalsIgnoreCase("TFASign")) {
                    out.println("AUTH_DENIED");
                    out.flush();
                    throw new BTC_Exception("Unexpected Message: " + input);
                }

                // Verify if indeed the signature is valid for the received certificate and challenge sent.
                if (!ccVer.validate(new BigInteger(inputFields[1], 16).toByteArray())) {
                    out.println("AUTH_DENIED");
                    out.flush();
                    return;
                }

            } catch (IOException | CertificateException ex) {
                out.println("AUTH_DENIED");
                out.flush();
                throw new BTC_Exception(ex);
            }
        }
        System.out.println("if went through, returning acceppted!\n");
        // Client was successfully authenticated.
        out.println("AUTH_ACCEPTED");
        out.flush();
    }
}

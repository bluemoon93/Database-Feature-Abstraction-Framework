/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import BusinessManager.BusinessManager;
import JavaTools.BECruds;
import LocalTools.BTC_Exception;
import Security.AuthenticationMethod;
import Security.Authenticators.Client.DacaClientAuthenticator;
import Security.Authenticators.Client.DacaClientCRAuthenticator;
import Security.Authenticators.Client.DacaClientPlainAuthenticator;
import Security.Authenticators.Client.DacaClientPSKSSLAuthenticator;
import Security.Authenticators.Client.DacaClientSSLAuthenticator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author DIOGO
 */
public class ClientHandler extends Thread implements Closeable {

    private Socket ClientSocket;
    private ServerSocket ServerSocket = null;
    private ArrayList<BECruds> tmp = null;
    private String myip;
    private int myport;
    private BusinessManager mr;
    private Map<Integer, List<Map.Entry<String, List<String>>>> controlinfo;
    private boolean isControllerActive;
    private boolean stopped;
    private String certPath;
    private String storePass;
    private AuthenticationMethod authMethod;

    public ClientHandler(BusinessManager mr, AuthenticationMethod authMethod, String certPath, String certPwd) throws BTC_Exception {
        try {
            System.out.println("at ch");
            this.mr = mr;
            this.myip = mr.getMyIP();
            this.myport = mr.getMyport();
            this.stopped = false;
            this.tmp = new ArrayList<>();
            this.controlinfo = new HashMap<>();
            this.certPath = certPath;
            this.storePass = certPwd;
            this.authMethod = authMethod;
            System.out.println("at socket");
            ClientSocket = new Socket(mr.getServerip(), mr.getServerport());
            System.out.println("at login");
            ClientSocket = Login(ClientSocket, mr.getAppname(), mr.getUsername(), mr.getPassword(), authMethod);
            System.out.println("at getbus");
            GetBus(ClientSocket);
            System.out.println("at setbes");
            this.mr.setBes(tmp);
            this.mr.setControlInfo(controlinfo);
            this.mr.setControlStatus(isControllerActive);
            System.out.println("at end of ch");
        } catch (IOException ex) {
            throw new BTC_Exception("Could not Connect to Server!");
        }
    }

    private Socket Login(Socket ClientSocket, String app, String username, String password, AuthenticationMethod authMethod) throws BTC_Exception {
        DacaClientAuthenticator auth;

        switch (authMethod) {
            case PLAIN:
                auth = new DacaClientPlainAuthenticator(ClientSocket, mr.getMyport());
                break;
            case ChallengeResponse:
                auth = new DacaClientCRAuthenticator(ClientSocket, mr.getMyport());
                break;
            case PSKSSL:
                auth = new DacaClientPSKSSLAuthenticator(ClientSocket, mr.getMyport());
                break;
            case SSL:
            default:
                auth = new DacaClientSSLAuthenticator(ClientSocket, mr.getMyport(), certPath, storePass);
        }

        return auth.authenticate(app, username, password);
    }

    private void GetBus(Socket ClientSocket) throws IOException {
        PrintWriter out = new PrintWriter(ClientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));

        out.println(ClientMsgs.GetBus());
        mr.setSessionID(Integer.parseInt(in.readLine()));
        int nbus = Integer.parseInt(in.readLine());
        for (int i = 0; i < nbus; i++) {
            String beurl = in.readLine();
            BECruds tmpbe = new BECruds(0, beurl, beurl);
            int ncruds = Integer.parseInt(in.readLine());
            for (int j = 0; j < ncruds; j++) {
                int id = Integer.parseInt(in.readLine());
                int srid = Integer.parseInt(in.readLine());
                mr.addCrudIdToSRId(id, srid);
                String crud = in.readLine();
                tmpbe.addCrud(id, crud);
            }
            tmp.add(tmpbe);
        }
        isControllerActive = Boolean.parseBoolean(in.readLine());
        controlinfo.clear();
        controlinfo = updateControllerInfo(in);
    }

    public File getJar() throws IOException {
        PrintWriter out = new PrintWriter(ClientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));

        // Send request
        out.println("getJar App");
        System.out.println("Requested");
        // get the reference to the file that will hold the jar
        File newFile = new File("./lib/serverjar.jar");
        if (newFile.exists()) {
            if (!newFile.delete()) {
                throw new BTC_Exception("Unable to delete old jar from " + newFile.getAbsolutePath());
            }
        }
        System.out.println("Deleted old file "+newFile.getAbsolutePath());
        // receive file size
        int filesize = Integer.parseInt(in.readLine());
        System.out.println("got file size "+filesize);
        // receive jar bytes
        byte[] jarbytes = IOUtils.toByteArray(ClientSocket.getInputStream(), filesize);
        System.out.println("got file");
        // write the bytes to the file
        FileUtils.writeByteArrayToFile(newFile, jarbytes);
        System.out.println("wrote file");
        return newFile;
    }

    @Override
    public void run() {
        try {
            ServerSocket = new ServerSocket(mr.getMyport());
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        do {
            Socket clientSocket;
            try {
                clientSocket = ServerSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                DacaClientAuthenticator auth;
                String method = in.readLine();
                if (method.equalsIgnoreCase("Plain")) {
                    auth = new DacaClientPlainAuthenticator(clientSocket);
                } else if (method.equalsIgnoreCase("Challenge")) {
                    auth = new DacaClientCRAuthenticator(clientSocket);
                } else if (method.equalsIgnoreCase("PSKSSL")) {
                    auth = new DacaClientPSKSSLAuthenticator(clientSocket);
                } else if (method.equalsIgnoreCase("SSL")) {
                    auth = new DacaClientSSLAuthenticator(clientSocket, certPath, storePass);
                } else {
                    throw new BTC_Exception("No authentication method received.");
                }

                clientSocket = auth.authenticate(mr.getAppname(), mr.getUsername(), mr.getPassword());

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String userInput;
                while ((userInput = in.readLine()) != null) {
                    try {
                        processMsg(clientSocket, userInput);
                    } catch (BTC_Exception ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
                return;
            }

        } while (!stopped);
    }

    public void processMsg(Socket ClientSocket, String msg) throws IOException, BTC_Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));

        Class bs = null;
        if (msg.equalsIgnoreCase("add")) {
            controlinfo.clear();
            controlinfo = updateControllerInfo(in);
            mr.setControlInfo(controlinfo);

            int size = Integer.parseInt(in.readLine());
            for (int i = 0; i < size; i++) {
                try {
                    bs = mr.loadClass(in.readLine());
                    mr.addBusinessSchema(bs);
                } catch (ClassNotFoundException ignored) {
                }
                int ncruds = Integer.parseInt(in.readLine());
                for (int j = 0; j < ncruds; j++) {
                    int crudid = Integer.parseInt(in.readLine());
                    String crud = in.readLine();
                    mr.addCRUD(crudid, crud, bs);
                }
            }

            mr.compileAddedBusinessSchemas();
        } else if (msg.equalsIgnoreCase("delete")) {
            int size = Integer.parseInt(in.readLine());
            for (int i = 0; i < size; i++) {
                try {
                    bs = mr.loadClass(in.readLine());
                } catch (ClassNotFoundException ignored) {
                }
                int ncruds = Integer.parseInt(in.readLine());
                for (int j = 0; j < ncruds; j++) {
                    int crudid = Integer.parseInt(in.readLine());
                    mr.removeCRUD(crudid, bs);

                }
                mr.removeBusinessSchema(bs);
            }
            controlinfo.clear();
            controlinfo = updateControllerInfo(in);
            mr.setControlInfo(controlinfo);
        } else if (msg.equalsIgnoreCase("delete_orchestration")) {
            int seq = Integer.parseInt(in.readLine());
            mr.removeSequence(seq);
        } else if (msg.equalsIgnoreCase("check_orchestration")) {
            int seq = Integer.parseInt(in.readLine());
            mr.removeSequence(seq);

            int size = Integer.parseInt(in.readLine());
            for (int i = 0; i < size; i++) {
                String beurl = in.readLine();
                List<String> revlist = new ArrayList<>();

                int revlistsize = Integer.parseInt(in.readLine());
                for (int j = 0; j < revlistsize; j++) {
                    revlist.add(in.readLine());
                }

                mr.addBEtoSequence(seq, beurl, revlist);
                this.mr.updateControlInfo();
            }
        } else if (msg.equalsIgnoreCase("toggle_orchestration")) {
            mr.setControlStatus(Boolean.parseBoolean(in.readLine()));
        } else if (msg.equalsIgnoreCase("no_change_role_deleted")) {
            this.mr.policiesChanged();
        }

    }

    private Map<Integer, List<Map.Entry<String, List<String>>>> updateControllerInfo(BufferedReader in) throws IOException {
        Map<Integer, List<Map.Entry<String, List<String>>>> ret = new HashMap<>();

        int nseqs = Integer.parseInt(in.readLine());
        for (int i = 0; i < nseqs; i++) {
            int seq = Integer.parseInt(in.readLine());
            int seqsize = Integer.parseInt(in.readLine());

            List<Map.Entry<String, List<String>>> seqdata = new ArrayList<>();
            for (int j = 0; j < seqsize; j++) {
                String beurl = in.readLine();
                int revlsize = Integer.parseInt(in.readLine());

                List<String> revlist = new ArrayList<>();
                for (int k = 0; k < revlsize; k++) {
                    revlist.add(in.readLine());
                }
                seqdata.add(new AbstractMap.SimpleEntry<>(beurl, revlist));
            }
            ret.put(seq, seqdata);
        }

        return ret;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    /**
     * Closes this stream and releases any system resources associated with it. If the stream is already closed then invoking this method has no effect.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        Socket endSocket = new Socket(mr.getServerip(), mr.getServerport());
        endSocket = Login(endSocket, mr.getAppname(), mr.getUsername(), mr.getPassword(), authMethod);
        PrintWriter out = new PrintWriter(endSocket.getOutputStream(), true);
        out.println("End " + this.myip + " " + this.myport);
        endSocket.close();
        ClientSocket.close();

        this.stopped = true;
        if (this.ServerSocket != null) {
            this.ServerSocket.close();
        }
    }

    public Socket getSocket() {
        return ClientSocket;
    }

    public void GoToDataMode() throws IOException {
        PrintWriter out = new PrintWriter(ClientSocket.getOutputStream(), true);
        out.println("Data");
        out.flush();
    }
}

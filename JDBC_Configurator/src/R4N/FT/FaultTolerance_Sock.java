/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import static R4N.FT.FaultToleranceServer.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author bluemoon
 */
public class FaultTolerance_Sock implements FaultTolerance {
    InetAddress ip;
    int port;
    Socket s;
    PrintWriter out;
    BufferedReader in;

    public FaultTolerance_Sock(String ip, int port) throws UnknownHostException, IOException {
        this.ip = InetAddress.getByName(ip);
        this.port=port;
        this.s = new Socket(this.ip, this.port);
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public synchronized void createFile(String id) throws IOException {
        out.println(createFile + id);
        in.readLine();
    }

    @Override
    public synchronized void deleteFile(String id) throws IOException {
        out.println(deleteFile + id);
        in.readLine();
    }

    @Override
    public synchronized void clearStates(String id) throws IOException {
        out.println(clearStates + id);
        in.readLine();
    }

    @Override
    public synchronized void setNewState(String id, String state) throws IOException {
        out.println(setNewState + id + ";" + state);
        in.readLine();
    }

    @Override
    public synchronized void removeLastStateReverser(String id) throws FileNotFoundException, IOException {
        out.println(removeLastStateReverser + id);
        in.readLine();
    }

    @Override
    public synchronized void removeLastState(String id) throws FileNotFoundException, IOException {
        out.println(removeLastState + id);
        in.readLine();
    }

    @Override
    public synchronized boolean checkIfRecoveryIsNecessary() throws IOException {
        out.println(checkIfRecoveryIsNecessary);
        return Boolean.parseBoolean(in.readLine());
    }

    @Override
    public synchronized ArrayList<FailureRecovery.DBStatement> getStatements() throws IOException {
        out.println(getStatements);

        ArrayList<FailureRecovery.DBStatement> list = new ArrayList();
        String readLine, currentId="Unknown!";
        while (!(readLine = in.readLine()).equals("ok")) {
            if (readLine.startsWith("id:")) {
                currentId = readLine.substring(3);
            } else if (readLine.equals("Done;")) {
                list.get(list.size() - 1).done = true;
            } else {
                String a = readLine.substring(5).split(";;")[0];
                FailureRecovery.DBStatement temp = new FailureRecovery.DBStatement(a, currentId, readLine.substring(7 + a.length()).split(";;"));
                list.add(temp);
            }
        }

        return list;
    }

    @Override
    public void reconnect() throws Exception {
        this.s = new Socket("localhost", this.port);    //CHANGE this.ip
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }
}

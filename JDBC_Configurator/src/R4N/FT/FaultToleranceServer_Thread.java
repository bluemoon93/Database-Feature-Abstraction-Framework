/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Sock;
import static R4N.FT.FaultToleranceServer.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author bluemoon
 */
public class FaultToleranceServer_Thread extends Thread {

    private final PrintWriter out;
    private final BufferedReader in;
    private final Socket s;
    private final HashMap<String, ArrayList<String>> map;
    private final ArrayList<String> currentClientIds = new ArrayList();
    private final Connection conn;
    private final ArrayList<Object[]> slaves;
    public boolean thisIsAMasterThread;

    public FaultToleranceServer_Thread(Socket s, HashMap<String, ArrayList<String>> m, Connection c, ArrayList<Object[]> slaves) throws IOException {
        this.s = s;
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        map = m;
        conn = c;
        this.slaves = slaves;
        thisIsAMasterThread = false;
    }

    public void propagate(String command) {
        ArrayList<Object[]> toBeDeleted = new ArrayList();
        for (Object[] obj : slaves) {
            try {
                PrintWriter out2 = (PrintWriter) obj[0];
                BufferedReader in2 = (BufferedReader) obj[1];
                out2.println(command);
                in2.readLine();
            } catch (Exception ex) {
                toBeDeleted.add(obj);
            }
        }
        for (Object[] obj : toBeDeleted) {
            slaves.remove(obj);
        }
    }

    @Override
    public void run() {
        ArrayList<String> al;
        while (!s.isClosed()) {
            try {
                String reply = "ok";
                String command = in.readLine();
                if (command == null) {
                    break;
                }
                boolean sendReply = true;
                //System.out.println("Got: "+command);
                switch (command.charAt(0)) {
                    case createFile:
                        synchronized (map) {
                            //to dooooooooooooo
                            propagate(command);
                            command = command.substring(1);
                            map.put(command, new ArrayList());
                            currentClientIds.add(command);
                        }
                        break;
                    case deleteFile:
                        synchronized (map) {
                            propagate(command);
                            command = command.substring(1);
                            map.remove(command);
                            currentClientIds.remove(command);
                        }
                        break;
                    case clearStates:
                        synchronized (map) {
                            propagate(command);
                            command = command.substring(1);
                            map.get(command).clear();
                        }
                        break;
                    case setNewState:

                        String id = command.substring(1, command.indexOf(";"));
                        String state = command.substring(command.indexOf(";") + 1);
                        synchronized (map) {
                            propagate(command);
                            map.get(id).add(state);
                        }
                        break;
                    case removeLastStateReverser:
                        synchronized (map) {
                            propagate(command);
                            command = command.substring(1);

                            al = map.get(command);
                            String[] fields = al.remove(al.size() - 1).split(";;");
                            String newState = "";
                            for (int i = 0; i < fields.length - 1; i++) {
                                newState += (fields[i] + ";;");
                            }
                            al.add(newState.substring(0, newState.length() - 2));
                        }
                        break;
                    case removeLastState:
                        synchronized (map) {
                            propagate(command);
                            command = command.substring(1);

                            al = map.get(command);
                            al.remove(al.size() - 1);
                        }
                        break;
                    case checkIfRecoveryIsNecessary:
                        synchronized (map) {
                            reply = "" + !map.isEmpty();
                        }
                        break;
                    case getStatements:
                        synchronized (map) {
                            Set<String> keys = map.keySet();
                            for (String k : keys) {
                                out.println("id:" + k);
                                for (String s : map.get(k)) {
                                    out.println(s);
                                }
                            }
                        }
                        break;
                    case getStatementsFromSlave:
                        synchronized (map) {
                            slaves.add(new Object[]{out, in});
                            Set<String> keys2 = map.keySet();
                            for (String k : keys2) {
                                out.println("id:" + k);
                                for (String s : map.get(k)) {
                                    out.println(s);
                                }
                            }
                        }
                        break;
                    case endThread:
                        return;
                    default:
                        System.out.println("Unknown command");
                        break;
                }
                //if (sendReply) {
                    out.println(reply);
                //}
            } catch (IOException ex) {
                if(!thisIsAMasterThread){
                    ex.printStackTrace();
                } else{
                   System.out.println("\tMaster crashed!"); 
                }
                try {
                    s.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }

        if (!thisIsAMasterThread) {
            try {
                // by here the 'R4N server' / client has crashed!
                FailureRecovery.revertSome(conn, new FaultTolerance_Sock("localhost", 5921), currentClientIds,
                        new ConcurrencyHandler(new GraphClient_Sock("localhost", 5922)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

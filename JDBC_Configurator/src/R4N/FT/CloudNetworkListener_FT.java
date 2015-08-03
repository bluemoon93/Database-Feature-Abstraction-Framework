/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import R4N.CloudNetworkListener;
import static R4N.FT.FaultToleranceServer.endThread;
import static R4N.FT.FaultToleranceServer.getStatementsFromSlave;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bluemoon
 */
public class CloudNetworkListener_FT implements CloudNetworkListener {

    private final HashMap<String, ArrayList<String>> map;
    private final Connection conn;
    private Socket s;
    private PrintWriter out;
    private BufferedReader in;

    public CloudNetworkListener_FT(HashMap<String, ArrayList<String>> map, Connection conn) {
        this.map = map;
        this.conn = conn;
    }

    // when a master is found, ask him for the current graph
    @Override
    public void setMaster(InetAddress master) {
        map.clear();
        try {
            this.s = new Socket(master, 5921);
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            out.println(getStatementsFromSlave);

            ArrayList<String> reversers = new ArrayList();
            String readLine, currentId = "Unknown!";
            while (!(readLine = in.readLine()).equals("ok")) {
                if (readLine.startsWith("id:")) {
                    currentId = readLine.substring(3);
                    reversers=new ArrayList();
                } else {
                    map.put(currentId, reversers);
                }
            }

            out.println("OK");
            in.readLine();
            out.println(endThread);
            FaultToleranceServer_Thread ftt = new FaultToleranceServer_Thread(s, map, conn, new ArrayList());
            ftt.thisIsAMasterThread=true;
            ftt.start(); //does this work?
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void startup() {

    }

    @Override
    public void nomaster() {

    }

    @Override
    public void master() {

    }

    @Override
    public void slave() {

    }

    @Override
    public void candidate() {

    }

    @Override
    public void accept() {

    }

    @Override
    public void conflict() {

    }

    @Override
    public void consistency() {

    }
}

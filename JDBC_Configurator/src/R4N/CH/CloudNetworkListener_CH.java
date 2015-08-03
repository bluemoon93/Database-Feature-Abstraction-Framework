/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

import R4N.CloudNetworkListener;
import R4N.CH.GraphClient.Node;
import R4N.CH.GraphClient_Local.BoolWrapper;
import static R4N.CH.GraphClient_Local.locks;
import static R4N.CH.GraphServer.getGraph;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author bluemoon
 */
public class CloudNetworkListener_CH implements CloudNetworkListener {

    private GraphClient g;
    private Socket s;
    private PrintWriter out;
    private BufferedReader in;

    public CloudNetworkListener_CH(GraphClient g) {
        this.g = g;
    }

    // when a master is found, ask him for the current graph
    @Override
    public void setMaster(InetAddress master) {
        g.reset();
        try {
            this.s = new Socket(master, 5922);
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println(getGraph);
            int nodes = Integer.parseInt(in.readLine());
            for (int i = 0; i < nodes; i++) {
                String name = in.readLine();
                String type = in.readLine();
                g.addNode(name, type);
                locks.put(name, new BoolWrapper());
            }
            for (int i = 0; i < nodes; i++) {
                String name = in.readLine();
                Node n = g.getNode(name);
                int connections = Integer.parseInt(in.readLine());
                for (int j = 0; j < connections; j++) {
                    switch (n.type) {
                        case "sub":
                            n.connectedTo.add(g.getNode(in.readLine()));
                            break;
                        case "obj":
                            BoolWrapper bw = locks.get(n.name);
                            bw.status=false;
                            n.connectedFrom.add(g.getNode(in.readLine()));
                            break;
                    }

                }
            }
            out.println("OK");
            in.readLine();
            out.println(GraphServer.endThread);
            new GraphServer_Thread(s, g).start();
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

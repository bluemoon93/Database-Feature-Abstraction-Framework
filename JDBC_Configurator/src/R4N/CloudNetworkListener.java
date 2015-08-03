/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N;

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
public interface CloudNetworkListener {

    // when a master is found, ask him for the current graph
    public void setMaster(InetAddress master);

    public void startup();

    public void nomaster();

    public void master();

    public void slave();

    public void candidate();

    public void accept();

    public void conflict();

    public void consistency();
}

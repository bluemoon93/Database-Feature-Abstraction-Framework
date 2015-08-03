/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

import static R4N.CH.GraphServer.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author bluemoon
 */
public class GraphClient_Sock implements GraphClient {

    InetAddress ip;
    int port;
    Socket s;
    PrintWriter out;
    BufferedReader in;

    public GraphClient_Sock(String ip, int port) throws UnknownHostException, IOException {
        this.ip = InetAddress.getByName(ip);
        this.port = port;
        this.s = new Socket(this.ip, port);
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public String getName() {
        return "Remote @ " + s.getRemoteSocketAddress();
    }

    @Override
    public synchronized void addNode(String name, String type) throws Exception {
        out.println(addNode);
        out.println(name);
        out.println(type);
        in.readLine();
    }

    @Override
    public synchronized void addConnection(String name, String target) throws Exception {
        out.println(addConnection);
        out.println(name);
        out.println(target);
        in.readLine();
    }

    @Override
    public synchronized void removeConnection(String name, String target) throws Exception {
        out.println(removeConnection);
        out.println(name);
        out.println(target);
        in.readLine();
    }
    
    @Override
    public synchronized void removeConnectionFromClient(String name) throws Exception {
        out.println(removeConnectionFromClient);
        out.println(name);
        in.readLine();
    }

    @Override
    public synchronized boolean checkForCycles(String name, String target) throws Exception {
        out.println(checkForCycles);
        out.println(name);
        out.println(target);
        return Boolean.parseBoolean(in.readLine());
    }

    @Override
    public boolean getWriteLock(String name, String target) throws Exception {
        out.println(getWriteLock);
        out.println(name);
        out.println(target);
        return Boolean.parseBoolean(in.readLine());
    }

    @Override
    public void dropWriteLock(String name, String target) throws Exception {
        out.println(dropWriteLock);
        //System.out.println("sending "+name);
        out.println(name);
        out.println(target);
        in.readLine();
    }

    @Override
    public Node getNode(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getGraph(PrintWriter out, BufferedReader in) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reconnect() throws Exception {
        this.s = new Socket(this.ip, port);
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }
}

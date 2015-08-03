/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author bluemoon
 */
public interface GraphClient {

    public Node getNode(String name);
    public void addNode(String name, String type) throws Exception;
    public void addConnection(String name, String target) throws Exception;
    public void removeConnection(String name, String target) throws Exception;
    public void removeConnectionFromClient(String name) throws Exception;
    public boolean checkForCycles(String name, String target) throws Exception;
    
    public boolean getWriteLock(String name, String target) throws Exception;
    public void dropWriteLock(String name, String target) throws Exception;
    
    public void reset();
    public String getName();
    public void getGraph(PrintWriter out, BufferedReader in) throws Exception;
    public void reconnect() throws Exception;
    
    public class Node {

        String name, type;
        ArrayList<Node> connectedTo = null;
        ArrayList<Node> connectedFrom = null;

        public Node(String n, String t) {
            switch (t) {
                case "sub":
                    connectedTo = new ArrayList();
                    break;
                case "obj":
                    connectedFrom = new ArrayList();
                    break;
            }
            name = n;
            type = t;
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bluemoon
 */
public class GraphClient_Local implements GraphClient {

    ArrayList<Object[]> slaves = new ArrayList();
    ArrayList<Node> nodes = new ArrayList();
    public static HashMap<String, BoolWrapper> locks = new HashMap();

    @Override
    public synchronized void addNode(String name, String type) {
        for (Node n : nodes) {
            if (n.name.equals(name)) {
                return;
            }
        }
        nodes.add(new Node(name, type));
    }

    @Override
    public synchronized void addConnection(String name, String target) {
        Node curr = getNode(name), tar = getNode(target);

        curr.connectedTo.add(tar);
        tar.connectedFrom.add(curr);
    }

    @Override
    public synchronized void removeConnection(String name, String target) {
        Node curr = getNode(name), tar = getNode(target);

        curr.connectedTo.remove(tar);
        tar.connectedFrom.remove(curr);
    }

    @Override
    public synchronized void removeConnectionFromClient(String name) {
        System.out.println("Clearing connection from " + name);
        Node curr = getNode(name);

        if (curr == null) {
            return;
        }

        for (Node tar : curr.connectedTo) {
            tar.connectedFrom.remove(curr);

            BoolWrapper rwLock;
            synchronized (locks) {
                rwLock = locks.get(tar.name);
                if (rwLock == null) {
                    System.out.println("Trying to get a lock that doesn't exist!");
                    continue;
                }
            }
            synchronized (rwLock) {
                rwLock.status = true;
                rwLock.notify();
            }
        }
        curr.connectedTo.clear();

        //forward to slaves
        synchronized (slaves) {
            ArrayList<Object[]> toBeDeleted = new ArrayList();
            for (Object[] obj : slaves) {
                try {
                    PrintWriter out = (PrintWriter) obj[0];
                    BufferedReader in = (BufferedReader) obj[1];
                    out.println(GraphServer.removeConnectionFromClient);
                    out.println(name);
                    in.readLine();
                } catch (Exception ex) {
                    toBeDeleted.add(obj);
                }
            }
            for (Object[] obj : toBeDeleted) {
                slaves.remove(obj);
            }
            toBeDeleted.clear();
        }
    }

    public Node getNode(String name) {
        for (Node n : nodes) {
            if (n.name.equals(name)) {
                return n;
            }
        }

        return null;
    }

    @Override
    public synchronized boolean checkForCycles(String name, String target) {
        Node tar = getNode(target);

        ArrayList<Node> currSubjects = new ArrayList();
        ArrayList<Node> visitedSubjects = new ArrayList();

        ArrayList<Node> visitedObjects = new ArrayList();
        ArrayList<Node> currObjects = new ArrayList();

        // fill the list of current subjects
        for (Node n : tar.connectedFrom) {
            visitedSubjects.add(n);
            currSubjects.add(n);
        }

        // while we have subjects to explore
        while (!currSubjects.isEmpty()) {

            // for each subject
            for (int i = 0; i < currSubjects.size(); i++) {
                // and each object it connects to
                for (Node object : currSubjects.get(i).connectedTo) {
                    // add object to list
                    if (!contains(visitedObjects, object)) {
                        visitedObjects.add(object);
                        currObjects.add(object);
                    }
                }

                currSubjects.remove(i);
                i--;
            }

            // for each object
            for (int i = 0; i < currObjects.size(); i++) {
                // and each subject connected to it
                for (Node subject : currObjects.get(i).connectedFrom) {
                    if (subject.name.equals(name)) {
                        return true;
                    }
                    // add subject to list
                    if (!contains(visitedSubjects, subject)) {
                        visitedSubjects.add(subject);
                        currSubjects.add(subject);
                    }
                }

                currObjects.remove(i);
                i--;
            }
        }

        // if no loops were found, add this connection
        addConnection(name, target);
        return false;
    }

    private boolean contains(ArrayList<Node> l, Node a) {
        for (Node n : l) {
            if (a.name.equals(n.name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "Local";
    }

    @Override
    public synchronized void getGraph(PrintWriter out, BufferedReader in) throws IOException {
        System.out.println("Sending graph...");
        slaves.add(new Object[]{out, in});
        out.println(nodes.size());
        for (Node n : nodes) {
            out.println(n.name);
            out.println(n.type);
            System.out.println("\tNode " + n.name + " of type " + n.type);
        }

        for (Node n : nodes) {
            System.out.println("\tFor node " + n.name);
            out.println(n.name);
            if (n.connectedFrom != null) {
                System.out.println("\t\tConnections are " + n.connectedFrom.size());
                out.println(n.connectedFrom.size());
                for (Node n2 : n.connectedFrom) {
                    System.out.println("\t\tConnections from " + n2.name);
                    out.println(n2.name);
                }
            } else {
                System.out.println("\t\tConnections are " + n.connectedTo.size());
                out.println(n.connectedTo.size());
                for (Node n2 : n.connectedTo) {
                    System.out.println("\t\tConnections to " + n2.name);
                    out.println(n2.name);
                }
            }

        }
        System.out.println("\tGetting OK");
        in.readLine();
        System.out.println("Graph sent!");
    }

    @Override
    public boolean getWriteLock(String uri, String id) throws Exception {
        this.addNode(uri, "obj");
        this.addNode(id, "sub");

        synchronized (slaves) {
            ArrayList<Object[]> toBeDeleted = new ArrayList();
            for (Object[] obj : slaves) {
                try {
                    PrintWriter out = (PrintWriter) obj[0];
                    BufferedReader in = (BufferedReader) obj[1];
                    out.println(GraphServer.addNode);
                    out.println(uri);
                    out.println("obj");
                    in.readLine();
                    out.println(GraphServer.addNode);
                    out.println(id);
                    out.println("sub");
                    in.readLine();
                } catch (Exception ex) {
                    toBeDeleted.add(obj);
                }
            }
            for (Object[] obj : toBeDeleted) {
                slaves.remove(obj);
            }
            toBeDeleted.clear();
        }

        BoolWrapper rwLock;
        synchronized (locks) {
            rwLock = locks.get(uri);
            if (rwLock == null) {
                rwLock = new BoolWrapper();
                locks.put(uri, rwLock);
            }
        }

        if (this.checkForCycles(id, uri)) {
            System.out.println(id + " found cycles!");
            return true;
        }

        synchronized (slaves) {
            ArrayList<Object[]> toBeDeleted = new ArrayList();
            for (Object[] obj : slaves) {
                try {
                    PrintWriter out = (PrintWriter) obj[0];
                    BufferedReader in = (BufferedReader) obj[1];

                    out.println(GraphServer.addConnection);
                    out.println(id);
                    out.println(uri);
                    in.readLine();
                } catch (Exception ex) {
                    toBeDeleted.add(obj);
                }
            }
            for (Object[] obj : toBeDeleted) {
                slaves.remove(obj);
            }
        }

        synchronized (rwLock) {
            while (!rwLock.status) {
                rwLock.wait();
            }
            rwLock.status = false;
        }

        return false;
    }

    @Override
    public void dropWriteLock(String uri, String id) throws Exception {
        if (uri == null) {
            return;
        }

        BoolWrapper rwLock;
        synchronized (locks) {
            rwLock = locks.get(uri);
            if (rwLock == null) {
                System.out.println("Trying to get a lock that doesn't exist: "+uri+"!");
                return;
            }
        }

        this.removeConnection(id, uri);

        synchronized (slaves) {
            ArrayList<Object[]> toBeDeleted = new ArrayList();
            for (Object[] obj : slaves) {
                try {
                    PrintWriter out = (PrintWriter) obj[0];
                    BufferedReader in = (BufferedReader) obj[1];
                    out.println(GraphServer.removeConnection);
                    out.println(id);
                    out.println(uri);
                    in.readLine();
                } catch (Exception ex) {
                    toBeDeleted.add(obj);
                }
            }
            for (Object[] obj : toBeDeleted) {
                slaves.remove(obj);
            }
            toBeDeleted.clear();
        }

        synchronized (rwLock) {
            rwLock.status = true;
            rwLock.notify();
        }
    }

    @Override
    public void reset() {
        nodes = new ArrayList();
        locks = new HashMap();
    }

    @Override
    public void reconnect() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class BoolWrapper {

        boolean status = true;
    }

}

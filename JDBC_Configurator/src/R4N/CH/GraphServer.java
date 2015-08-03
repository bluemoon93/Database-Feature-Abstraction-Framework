/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

import R4N.CloudNetwork;
import R4N.CloudNetworkListener;
import java.io.IOException;
import java.net.ServerSocket;

public class GraphServer {

    public static final char addNode = 'c', addConnection = 'd', removeConnection = 's', checkForCycles = 'n',
            dropWriteLock = 'q', getWriteLock = 'w', getGraph = 'l', endThread = 'e', removeConnectionFromClient = 'm';

    public static String getOp(char c) {
        switch (c) {
            case addNode:
                return "addNode";
            case addConnection:
                return "addConnection";
            case removeConnection:
                return "removeConnection";
            case checkForCycles:
                return "checkForCycles";
            case dropWriteLock:
                return "dropWriteLock";
            case getWriteLock:
                return "getWriteLock";
            case getGraph:
                return "getGraph";
            case endThread:
                return "endThread";
            case removeConnectionFromClient:
                return "removeConnectionFromClient";
            default:
                return "Unknown: " + c;
        }
    }

    public void runServer() throws IOException {
        GraphClient g = new GraphClient_Local();
        CloudNetworkListener lcl = new CloudNetworkListener_CH(g);
        CloudNetwork lc = new CloudNetwork(4447, lcl);
        lc.start();

        ServerSocket a = new ServerSocket(5922);

        while (true) {
            System.out.println("Awaiting connection...");
            new GraphServer_Thread(a.accept(), g).start();
        }
    }

    public static void main(String[] args) throws IOException {
        GraphServer gs = new GraphServer();
        gs.runServer();
    }
}

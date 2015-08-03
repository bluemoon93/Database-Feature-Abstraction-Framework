/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.CH;

/**
 *
 * @author bluemoon
 */
public class ConcurrencyHandler {

    private final GraphClient graph;

    public ConcurrencyHandler(GraphClient g) {
        graph = g;
    }

    public boolean acquireWriteSemaphore(String uri, String id) {
        boolean done = false;
        while (!done) {
            try {
                boolean deadlock = graph.getWriteLock(uri, id);
                done=true;
                if(deadlock) return true;
            } catch (Exception ex) {
                System.out.println("Exception: "+ex.getMessage());
                try {
                    System.out.println("Graph server crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    graph.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: "+ex1.getMessage());
                }
            }
        }

        return false;
    }

    public void releaseWriteSemaphore(String uri, String id) {
        if(uri==null) return;
        
        boolean done = false;
        while (!done) {
            try {
                graph.dropWriteLock(uri, id);
                done=true;
            } catch (Exception ex) {
                System.out.println("Exception: "+ex.getMessage());
                try {
                    System.out.println("Graph server crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    graph.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: "+ex1.getMessage());
                }
            }
        }
    }
    
    public void releaseAllWriteSemaphore(String id) {
        while (true) {
            try {
                graph.removeConnectionFromClient(id);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: "+ex.getMessage());
                try {
                    System.out.println("Graph server crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    graph.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: "+ex1.getMessage());
                }
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import R4N.CH.ConcurrencyHandler;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author bluemoon
 */
public class FailureRecovery {

    public static void revertAll(Connection connection, FaultTolerance f) throws Exception {
        if(!f.checkIfRecoveryIsNecessary()){
            return;
        }
        System.out.println("Failure recovery initiated!");
        //R4N_Transaction trs = new R4N_TransactionSQL(connection, "FR");
        Statement stmt = connection.createStatement();
        ArrayList<DBStatement> list = f.getStatements();

        for (DBStatement a : list) {
            System.out.println("Action: " + a.action + ", [" + a.id + "]");
            for (int i = 0; i < a.reverserCheckers.length; i++) {
                System.out.println("\tVerify with: " + a.reverserCheckers[i] + ", which should match " + a.reverserRowsValues[i]);
                System.out.println("\t\tReverser: " + a.reversers[i]);
            }
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            DBStatement a = list.get(i);
            for (int j = a.reverserCount - 1; j >= 0; j--) {
                if (checkVal(stmt.executeQuery(a.reverserCheckers[j])) != a.reverserRowsValues[j]) {
                    stmt.executeUpdate(a.reversers[j]);
                }
                if (j != 0) {
                    f.removeLastStateReverser(a.id);
                }
            }
            f.removeLastState(a.id);
        }

        
        for (DBStatement fi : list) {
            f.deleteFile(fi.id);
        }

        //trs.close();
        System.out.println("Failure recovery terminated!");
    }
    
    public static boolean contains(ArrayList<String> ids, String id){
        for(String i: ids){
            if(i.equals(id))
                return true;
        }
        
        return false;
    }
    
    public static void revertSome(Connection connection, FaultTolerance f, ArrayList<String> ids, ConcurrencyHandler ch) throws Exception {
        
        System.out.println("Failure recovery for some IDs initiated!");

        Statement stmt = connection.createStatement();
        ArrayList<DBStatement> list = f.getStatements();

        // remove those that shouldn't be reverted
        for(int i=0; i<list.size(); i++){
            if(!contains(ids, list.get(i).id)){
                list.remove(i);
                i--;
            }
        }
        
        // print changes
        for (DBStatement a : list) {
            System.out.println("Action: " + a.action + ", [" + a.id + "]");
            for (int i = 0; i < a.reverserCheckers.length; i++) {
                System.out.println("\tVerify with: " + a.reverserCheckers[i] + ", which should match " + a.reverserRowsValues[i]);
                System.out.println("\t\tReverser: " + a.reversers[i]);
            }
        }

        // for each statement
        for (int i = list.size() - 1; i >= 0; i--) {
            DBStatement a = list.get(i);
            
            // for each reverser (backwards)
            for (int j = a.reverserCount - 1; j >= 0; j--) {
                //if it needs to be reversed
                if (checkVal(stmt.executeQuery(a.reverserCheckers[j])) != a.reverserRowsValues[j]) {
                    stmt.executeUpdate(a.reversers[j]);
                }
                
                //remove that reverser
                if (j != 0) {
                    f.removeLastStateReverser(a.id);
                }
            }
            // remove that statement
            ch.releaseAllWriteSemaphore(a.id);
            f.removeLastState(a.id);
        }

        // remove that client
        for (DBStatement fi : list) {
            f.deleteFile(fi.id);
        }

        System.out.println("Failure recovery terminated!");
    }

    private static int checkVal(ResultSet a) throws SQLException {
        int size = 0;
        while (a.next()) {
            size++;
        }
        return size;

    }

    protected static class DBStatement {

        public String action, id;
        public boolean done;
        public String[] reversers, reverserCheckers;
        public int[] reverserRowsValues;
        public int reverserCount;

        public DBStatement(String a, String b, String[] c) {
            action = a;
            id = b;
            done = false;

            reverserCount = c.length;

            reversers = new String[reverserCount];
            reverserCheckers = new String[reverserCount];
            reverserRowsValues = new int[reverserCount];

            for (int i = 0; i < reverserCount; i++) {
                String[] e = c[i].split(";");
                reverserCheckers[i] = e[0];
                reverserRowsValues[i] = Integer.parseInt(e[1]);
                reversers[i] = e[2];
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.DFM;

//import static R4N.ConcurrencyHandler.acquireWriteSemaphore;
//import static R4N.ConcurrencyHandler.releaseWriteSemaphore;
import R4N.CH.ConcurrencyHandler;
import R4N.FT.FTHandler;
import R4N.FT.FaultTolerance;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;


/**
 *
 * @author bluemoon
 */
public abstract class R4N_Transaction {

    private final String id;
    protected final Connection conn;
    protected final ArrayList<String[]> revertActions;
    protected final ArrayList<String> actions;
    protected final ArrayList<R4N_SavePoint> savepoints;
    protected Statement statement;
    protected FTHandler ftm;
    protected ConcurrencyHandler ch;

    public abstract String[][] getRevertAction(String action) throws SQLException;

    public abstract String getLockURI(String action) throws SQLException;

    public R4N_Transaction(Connection conn, String id, FTHandler f, ConcurrencyHandler ch) throws Exception {
        this.conn = conn;
        this.ch=ch;
        
        actions = new ArrayList();
        revertActions = new ArrayList();
        savepoints = new ArrayList();
        this.id = id;
        try {
            statement = conn.createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        ftm=f;
        ftm.createFile(id);
    }

    public void close() throws Exception {
        commit();
        ftm.deleteFile(id);
        //actions.clear();
        //revertActions.clear();
    }

    public int executeUpdate(String action) throws Exception {
        
        String semUri = getLockURI(action);
        
        // check whether we have locked this sem already
        boolean thisSemaphoreIsAlreadyMine = false;
        for (String[] revertAction : revertActions) {
            if (semUri.equals(revertAction[1])) {
                thisSemaphoreIsAlreadyMine = true;
            }
        }

        // we dont lock it twice
        if (!thisSemaphoreIsAlreadyMine) {
            if (ch.acquireWriteSemaphore(semUri, id)) {
                System.out.println("Deadlock detected! Restarting transaction");
                restartTransaction();
                return executeUpdate(action);
            }
        }

        String[][] revert=null;
        try {
            revert = getRevertAction(action);
        } catch (Exception ex) {
            if (!thisSemaphoreIsAlreadyMine) {
                ch.releaseWriteSemaphore(semUri, id);
            }
            //ex.printStackTrace();
            throw ex;
        }

        String text = "OnIt;"+action+";;";
        for(int i=0; i<revert[0].length; i++){
            text+=revert[0][i]+";"+revert[1][i]+";;";
        }
        ftm.setNewState(id, text);

        //System.out.println("About to execute");
        // execute action. if it crashes and we just locked, unlock
       
        int rows;
        try {
            rows = statement.executeUpdate(action);
        } catch (Exception ex) {
            ftm.removeLastState(id);
            if (!thisSemaphoreIsAlreadyMine) {
                ch.releaseWriteSemaphore(semUri, id);
            }
            //ex.printStackTrace();
            throw ex;
        }
        //System.out.println("Executed");
        ftm.setNewState(id, "Done;");

        // add the revert action
        for (String r : revert[1]) {
            if (!thisSemaphoreIsAlreadyMine) {
                revertActions.add(new String[]{r, semUri});
                thisSemaphoreIsAlreadyMine=true;
            } else {
                revertActions.add(new String[]{r, null});
            }
        }
        actions.add(action);
        return rows;
    }

    public ResultSet executeQuery(String action) throws SQLException {
        //Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(action);
 
        return rs;
    }

    private void restartTransaction() throws Exception {
        ArrayList<String> tempActions = new ArrayList();
        for (String action : actions) {
            tempActions.add(action);
        }

        rollback();
        try {
            conn.setAutoCommit(true);
            //conn.setAutoCommit(false);
        } catch (Exception ignore) {

        }

        for (String action : tempActions) {
            executeUpdate(action);
        }
    }

    public void commit() throws Exception {
        ftm.clearStates(id);
        
        for (int i = revertActions.size() - 1; i >= 0; i--) {
            //System.out.println("Lock: "+revertActions.get(i)[1]);
            ch.releaseWriteSemaphore(revertActions.get(i)[1], id);
        }

        revertActions.clear();
    }

    public void rollback() throws Exception {
        for (int i = revertActions.size() - 1; i >= 0; i--) {
            String[] temp = revertActions.get(i);
            ftm.removeLastState(id);  // Removes the done
            statement.executeUpdate(temp[0]);
            ftm.removeLastState(id);  // Removes the action
            ch.releaseWriteSemaphore(temp[1], id);
        }

        actions.clear();
        revertActions.clear();
    }

    public void setSavepoint(String lol) {
        R4N_SavePoint a = new R4N_SavePoint(1, lol);
        savepoints.add(a);
    }

    public void rollback(String lol) throws Exception {
        for (int j = 0; j < savepoints.size(); j++) {
            R4N_SavePoint currSP = savepoints.get(j);
            if (currSP.name.equals(lol)) {
                //Statement statement = conn.createStatement();

                for (int i = revertActions.size() - 1; i >= currSP.index; i--) {
                    String[] temp = revertActions.get(i);
                    ftm.removeLastState(id);  // Removes the done
                    statement.executeUpdate(temp[0]);
                    ftm.removeLastState(id);  // Removes the action
                    ch.releaseWriteSemaphore(temp[1], id);
                }

                while (revertActions.size() > currSP.index) {
                    revertActions.remove(currSP.index);
                }
                while (savepoints.size() > j) {
                    savepoints.remove(j);
                }

                return;
            }
        }

        System.out.println("No matching save points were found");
    }

    protected class R4N_SavePoint implements Savepoint {

        int id, index;
        String name;

        public R4N_SavePoint(int i, String n) {
            id = i;
            name = n;
            index = revertActions.size();
        }

        @Override
        public int getSavepointId() throws SQLException {
            return id;
        }

        @Override
        public String getSavepointName() throws SQLException {
            return name;
        }

    }
}

package Server;

import Configs.Reader;
import static LocalTools.MessageTypes.*;
import R4N.DFM.R4N_CallableStatement;
import R4N.DFM.R4N_Transaction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public class DatabaseRelayer {

    private Connection psConn;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    //private ResultSet rs;
    private Connection conn;
    private Object setArgument = null;
    private HashMap<Integer, ResultSet> RSs;
    private HashMap<Integer, HashMap<String, Integer>> columns;
    private int qRID;
    R4N_Transaction trans;
    R4N_CallableStatement[] StoredProcedures;

    private final String id;
    private boolean useR4NTransactions, useR4NIAMInteractions, useR4NSP, useR4NColIndexes;

    public DatabaseRelayer(String id) {
        this.id = id;
        columns = new HashMap();
        RSs = new HashMap();
        try {
            useR4NTransactions = Reader.getTrans();
            useR4NIAMInteractions = Reader.getIAM();
            useR4NSP = Reader.getSP();
            useR4NColIndexes = Reader.getColI();
            if (useR4NSP) {
                StoredProcedures = Reader.getSPs();
            } else {
                StoredProcedures = new R4N_CallableStatement[0];
            }
            conn = Reader.getConn();
            psConn = Reader.getPSConn();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // change the socket from the client to forward its data to the server
    public void relay(final Socket fromSocket) throws Exception {
        try {
            oos = new ObjectOutputStream(fromSocket.getOutputStream());
            ois = new ObjectInputStream(fromSocket.getInputStream());

            while (true) {
                int args = ois.readInt();
                if (args > 0) {
                    processQuery(args);
                } else if (args == -100) {
                    break;
                } else {
                    processRequest(args);
                }
                oos.flush();
            }
        } catch (IOException | SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Crashed here. Rollback!");
            /*if (useR4NTransactions) {
                trans.rollback();
            } else {
                conn.rollback();
            }*/
        }

        System.out.println("Relayer ended");
    }

    private String getQuery(int sID, int qSRID) {
        int QueryRID;

        String sel1 = "SELECT SQ.[QueryRID] FROM _remote.[SessionQueries] as SQ WHERE SQ.[SessionID] = " + sID + " AND SQ.[QuerySRID] = " + qSRID;

        try {
            PreparedStatement ps = psConn.prepareStatement(sel1, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs2 = ps.executeQuery();
            rs2.next();
            QueryRID = rs2.getInt(1);
            qRID = QueryRID;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }

        String sel2 = "SELECT Q.[Query], Q.[_ID] FROM _remote.[Queries] as Q WHERE Q.[RID] = " + QueryRID;

        try {
            PreparedStatement ps = psConn.prepareStatement(sel2, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs2 = ps.executeQuery();
            rs2.next();
            String query = rs2.getString(1).replaceAll("''", "'");
            return query;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    private void processRequest(int args) throws Exception {
        switch (args) {
            case NEXT:
                processNext();
                break;
            case PREVIOUS:
                processPrevious();
                break;
            case ABSOLUTE:
                processAbsolute();
                break;
            case RELATIVE:
                processRelative();
                break;
            case BEFORE_FIRST:
                processBF();
                break;
            case AFTER_LAST:
                processAL();
                break;
            case GET:
                processGet();
                break;
            case SET:
                processSet();
                break;
            case BEGIN_UPDATE:
                processBUpdate();
                break;
            case CANCEL_UPDATE:
                processCUpdate();
                break;
            case UPDATE_ROW:
                processUpdateRow();
                break;
            case UPDATE_VAL:
                processUpdateVal();
                break;
            case BEGIN_INSERT:
                processBInsert();
                break;
            case CANCEL_INSERT:
                processCInsert();
                break;
            case INSERT_ROW:
                processInsertRow();
                break;
            case INSERT_VAL:
                processInsertVal();
                break;
            case FIRST:
                processFirst();
                break;
            case LAST:
                processLast();
                break;
            case DELETE_ROW:
                processDeleteRow();
                break;
            case COMMIT:
                commit();
                break;
            case RB:
                rollback();
                break;
            case AUTOCOMMIT:
                autocommit();
                break;
            case SP:
                savepoint();
                break;
            case SPNAME:
                savepointName();
                break;
            case RELSP:
                releaseSavepoint();
                break;
            case RBSP:
                rollbackName();
                break;
            default:
                System.out.println("Unknown command: " + args);
                break;
        }
    }

    private void getColumns(int queryId) {
        //columns.clear();
        HashMap<String, Integer> cols = new HashMap();

        String sel1 = "SELECT * FROM _remote.[QueryColumns] as SQ WHERE SQ.[QueryRID] = " + qRID;

        try {
            PreparedStatement ps = psConn.prepareStatement(sel1, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                cols.put(rs2.getString("ColName"), rs2.getInt("position"));
                //System.out.println("inserting " + rs2.getString("ColName") + " on pos " + rs2.getInt("position"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        columns.put(queryId, cols);
    }

    private void processQuery(int args) throws Exception {
        boolean isQuery = ois.readBoolean();

        Object[] arguments = new Object[args];
        for (int i = 0; i < args; i++) {
            arguments[i] = ois.readObject();
        }
        Integer queryIdentifier = ois.readInt();
        System.out.println("qId=" + queryIdentifier);
        String[][] pairs = null;

        if (arguments.length == 2 && setArgument != null) {
            Object[] tempArgs = new Object[3];
            tempArgs[0] = arguments[0];
            tempArgs[1] = arguments[1];
            tempArgs[2] = setArgument;
            arguments = tempArgs;
        }

        if (arguments.length > 2) {
            String[] vars = ((String) arguments[2]).split(",");
            pairs = new String[vars.length][2];
            for (int i = 0; i < vars.length; i++) {
                while (vars[i].startsWith(" ")) {
                    vars[i] = vars[i].substring(1);
                }
                String[] temp = vars[i].split(" ");
                pairs[i][0] = temp[0];
                pairs[i][1] = "";
                for (int j = 2; j < temp.length; j++) {
                    pairs[i][1] += temp[j] + " ";
                }
            }
        } else {
            pairs = new String[0][2];
        }

        setArgument = null;

        String query = getQuery((int) arguments[0], (int) arguments[1]);

        if (query.startsWith("exec ")) {
            String spName = query.split(" ")[1];
            for (R4N_CallableStatement sp : StoredProcedures) {
                if (sp.name.equals(spName)) {
                    for (int i = 0; i < pairs.length; i++) {
                        sp.setObject(i + 1, pairs[i][1]);
                    }
                    sp.execute();
                    oos.writeInt((Integer) sp.getObject(pairs.length + 1));
                    break;
                }
            }
        } else {

            // replaces @args with uppercase values
            boolean turn = false;
            for (int i = 0; i < query.length(); i++) {
                char c = query.charAt(i);

                if (c == '@') {
                    turn = true;
                    continue;
                } else if (!Character.isLetter(c)) {
                    turn = false;
                    continue;
                }

                if (turn) {
                    query = query.substring(0, i) + Character.toUpperCase(c) + query.substring(i + 1, query.length());
                }
            }

            if (pairs != null) {
                for (String[] pair : pairs) {
                    query = query.replaceFirst(pair[0].toUpperCase(), pair[1].replaceAll("\"", "'"));
                }
            }

            System.out.println("Executing query: " + query);

            if (isQuery) {
                if (useR4NColIndexes) {
                    getColumns(queryIdentifier);
                }

                if (trans == null) {
                    if (useR4NIAMInteractions) {
                        RSs.put(queryIdentifier, Reader.getRS(query));
                    } else {
                        PreparedStatement ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        RSs.put(queryIdentifier, ps.executeQuery());
                    }
                } else {
                    RSs.put(queryIdentifier, trans.executeQuery(query));
                }
                oos.writeObject("ok");
            } else {
                int rows;
                if (trans == null) {
                    Statement ps = conn.createStatement();
                    rows = ps.executeUpdate(query);
                } else {
                    rows = trans.executeUpdate(query);
                }
                oos.writeInt(rows);
            }
        }
    }

    private void processNext() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        boolean val = RSs.get(queryIdentifier).next();

        oos.writeBoolean(val);
    }

    private void processPrevious() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).previous();
        oos.writeObject("ok");
    }

    private void processAbsolute() throws IOException, SQLException, ClassNotFoundException {

        int val = ois.readInt();
        int queryIdentifier = ois.readInt();
        oos.writeBoolean(RSs.get(queryIdentifier).absolute(val));
    }

    private void processRelative() throws IOException, SQLException, ClassNotFoundException {

        int val = ois.readInt();
        int queryIdentifier = ois.readInt();
        oos.writeBoolean(RSs.get(queryIdentifier).relative(val));
    }

    private void processFirst() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        oos.writeBoolean(RSs.get(queryIdentifier).first());
    }

    private void processLast() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        oos.writeBoolean(RSs.get(queryIdentifier).last());
    }

    private void processBF() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).beforeFirst();
        oos.writeObject("ok");
    }

    private void processAL() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).afterLast();
        oos.writeObject("ok");
    }

    private void processGet() throws IOException, SQLException, ClassNotFoundException {
        String colName = ois.readUTF();
        String objType = ois.readUTF();

        int queryIdentifier = ois.readInt();
        Object o;

        if (columns.containsKey(queryIdentifier)) {
            int index = columns.get(queryIdentifier).get(colName);
            switch (objType) {
                case "Date":
                    o = RSs.get(queryIdentifier).getDate(index);
                    break;
                case "int":
                    o = RSs.get(queryIdentifier).getInt(index);
                    break;
                case "double":
                    o = RSs.get(queryIdentifier).getDouble(index);
                    break;
                default:
                    o = RSs.get(queryIdentifier).getObject(index);
                    break;
            }
        } else {
            switch (objType) {
                case "Date":
                    o = RSs.get(queryIdentifier).getDate(colName);
                    break;
                case "int":
                    o = RSs.get(queryIdentifier).getInt(colName);
                    break;
                case "double":
                    o = RSs.get(queryIdentifier).getDouble(colName);
                    break;
                default:
                    o = RSs.get(queryIdentifier).getObject(colName);
                    break;
            }
        }
        oos.writeObject(o);
    }

    private void processSet() throws IOException, SQLException, ClassNotFoundException {
        setArgument = ois.readObject();
        int queryIdentifier = ois.readInt();
        oos.writeObject("ok");
    }

    private void processBUpdate() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        //not needed
        oos.writeObject("ok");
    }

    private void processCUpdate() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).cancelRowUpdates();
        oos.writeObject("ok");
    }

    private void processUpdateRow() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).updateRow();
        oos.writeObject("ok");
    }

    private void processUpdateVal() throws IOException, SQLException, ClassNotFoundException {
        String colName = ois.readUTF();

        Object obj = ois.readObject();

        int queryIdentifier = ois.readInt();
        if (columns.containsKey(queryIdentifier)) {
            int index = columns.get(queryIdentifier).get(colName);
            RSs.get(queryIdentifier).updateObject(index, obj);
        } else {
            RSs.get(queryIdentifier).updateObject(colName, obj);
        }
        oos.writeObject("ok");
    }

    private void processBInsert() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).moveToInsertRow();
        oos.writeObject("ok");
    }

    private void processCInsert() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).moveToCurrentRow();
        oos.writeObject("ok");
    }

    private void processInsertRow() throws IOException, SQLException, ClassNotFoundException {
        boolean mtcr = ois.readBoolean();
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).insertRow();
        if (mtcr) {
            RSs.get(queryIdentifier).moveToCurrentRow();
        }
        oos.writeObject("ok");
    }

    private void processInsertVal() throws IOException, SQLException, ClassNotFoundException {
        String colName = ois.readUTF();
        Object obj = ois.readObject();
        int queryIdentifier = ois.readInt();

        if (columns.containsKey(queryIdentifier)) {
            int index = columns.get(queryIdentifier).get(colName);
            RSs.get(queryIdentifier).updateObject(index, obj);
        } else {
            RSs.get(queryIdentifier).updateObject(colName, obj);
        }
        oos.writeObject("ok");
    }

    private void processDeleteRow() throws IOException, SQLException, ClassNotFoundException {
        int queryIdentifier = ois.readInt();
        RSs.get(queryIdentifier).deleteRow();
        oos.writeObject("ok");
    }

    private void commit() throws Exception {

        if (useR4NTransactions) {
            trans.commit();
        } else {
            conn.commit();
        }
        oos.writeObject("ok");
    }

    private void rollback() throws Exception {

        if (useR4NTransactions) {
            trans.rollback();
        } else {
            conn.rollback();
        }
        oos.writeObject("ok");
    }

    private void rollbackName() throws Exception {
        String sp = ois.readUTF();

        if (useR4NTransactions) {
            trans.rollback(sp);
        } else {
            conn.rollback(null);
        }
        oos.writeObject("ok");
    }

    private void savepoint() throws IOException, SQLException, ClassNotFoundException {
        if (useR4NTransactions) {
            trans.setSavepoint(new Random().nextDouble() + "");
        } else {
            conn.setSavepoint();
        }
        oos.writeObject("ok");
    }

    private void savepointName() throws IOException, SQLException, ClassNotFoundException {
        String sp = ois.readUTF();

        if (useR4NTransactions) {
            trans.setSavepoint(sp);
        } else {
            conn.setSavepoint(sp);
        }
        oos.writeObject("ok");
    }

    private void releaseSavepoint() throws IOException, SQLException, ClassNotFoundException {
        String sp = ois.readUTF();
        if (useR4NTransactions) {
            System.out.println("Not implemented");
        } else {
            System.out.println("Not implemented");
        }
        oos.writeObject("ok");
    }

    private void autocommit() throws Exception {
        boolean val = ois.readBoolean();
        if (useR4NTransactions) {
            if (!val) {
                trans = Reader.getTrans(id);

                for (R4N_CallableStatement cs : StoredProcedures) {
                    cs.setTransaction(trans);
                }
            } else if (trans != null) {
                trans.close();
                trans = null;
                for (R4N_CallableStatement cs : StoredProcedures) {
                    cs.setTransaction(null);
                }
            }
        } else {
            conn.setAutoCommit(val);
        }
        oos.writeObject("ok");
    }
}

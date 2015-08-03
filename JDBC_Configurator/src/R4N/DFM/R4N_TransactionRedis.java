/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.DFM;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bluemoon
 */
public class R4N_TransactionRedis extends R4N_Transaction {

    private String[][] decodeInsert(String action) throws SQLException {
        String[] fields = parseQuery(action);
        ResultSet rs = statement.executeQuery("get " + fields[1]);
        if (!rs.next()) {
            String[][] retVal = new String[2][1];
            retVal[0][0] = "get " + fields[1] + ";" + 0;
            retVal[1][0] = "del " + fields[1];
            return retVal;
        } else {
            String[][] retVal = new String[2][1];
            retVal[0][0] = "get " + fields[1] + ";" + 1;
            retVal[1][0] = "set " + fields[1]+" ";
            for(int i=1; i<=rs.getMetaData().getColumnCount(); i++){
                retVal[1][0]+=rs.getMetaData().getColumnName(i)+" "+rs.getObject(i)+" ";
            }
            return retVal;
        }
    }

    public String[][] decodeDelete(String action) throws SQLException {
        String[] fields = parseQuery(action);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("get " + fields[1]);
        if (rs.next()) {
            String[][] retVal = new String[2][1];
            retVal[0][0] = "get " + fields[1] + ";" + 1;
            retVal[1][0] = "set " + fields[1];
            ResultSetMetaData rsm = rs.getMetaData();
            for (int i = 1; i <= rsm.getColumnCount(); i++) {
                retVal[1][0]+=" "+rsm.getColumnName(1)+" "+rs.getObject(i);
            }
            return retVal;
        } else {
            String[][] retVal = new String[2][1];
            retVal[0][0] = "get " + fields[1] + ";" + 0;
            retVal[1][0] = "get do:nutin";
            return retVal;
        }
    }

    @Override
    public String[][] getRevertAction(String action) throws SQLException {
        String[][] revertAction = null;
        while (action.startsWith(" ")) {
            action = action.substring(1);
        }
        
        if (action.toLowerCase().startsWith("set")) {
            revertAction = decodeInsert(action);
        } else if (action.toLowerCase().startsWith("del")) {
            revertAction = decodeDelete(action);
        } else {
            throw new SQLException("Can't revert action: " + action);
        }

        return revertAction;
    }

    public R4N_TransactionRedis(Connection conn, String id, FTHandler f, ConcurrencyHandler ch) throws Exception {
        super(conn, id,f,ch);
    }

    private String[] parseQuery(String sql) {
        sql = sql.replaceAll("\t", " ");
        while (sql.contains("  ")) {
            sql = sql.replaceAll("  ", " ");
        }
        if (sql.endsWith(" ")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        if (sql.startsWith(" ")) {
            sql = sql.substring(1, sql.length());
        }
        return sql.split(" ");

    }

    @Override
    public String getLockURI(String action) throws SQLException {
        //return parseQuery(action)[1];
        return parseQuery(action)[1].split(":")[0];
    }
}

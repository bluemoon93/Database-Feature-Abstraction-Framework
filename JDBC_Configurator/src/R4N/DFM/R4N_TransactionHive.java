/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.DFM;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FTHandler;
import R4N.FT.FaultTolerance;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bluemoon
 */
public class R4N_TransactionHive extends R4N_Transaction {

    private String[][] decodeInsert(String action) throws SQLException {
        String[] fields = action.split("[()]");

        // Get table name
        while (fields[0].contains("  ")) {
            fields[0] = fields[0].replaceAll("  ", " ");
        }
        String tableName = fields[0].split(" ")[3];

        // Get table's columns
        Statement statement = conn.createStatement();
        ArrayList<String> cols = new ArrayList();
        ResultSet rs = statement.executeQuery("describe " + tableName);
        while (rs.next()) {
            cols.add(rs.getString(1));
        }

        String[][] retVal = new String[2][fields.length - 1];
        for (int j = 1; j < fields.length; j += 2) {
            String[] values = fields[j].split(",");
            for (String val : values) {
                while (val.startsWith(" ")) {
                    val = val.substring(1);
                }
                while (val.endsWith(" ")) {;
                    val = val.substring(0, val.length() - 1);
                }
            }

            if (values.length != cols.size()) {
                throw new SQLException("Column and value mismatch!");
            }

            statement = conn.createStatement();
            String query = "SELECT * FROM " + tableName;
            String whereClause = " where ";
            for (int i = 0; i < cols.size() - 1; i++) {
                whereClause += cols.get(i) + "=" + values[i] + " and ";
            }
            whereClause += cols.get(cols.size() - 1) + "=" + values[values.length - 1];

            //System.out.println("Executing " + query + whereClause);
            rs = statement.executeQuery(query + whereClause);
            if (!rs.next()) {
                retVal[0][j - 1] = query + whereClause + ";" + 0;
                retVal[1][j - 1] = "delete from " + tableName + whereClause;

            } else {
                throw new SQLException("Couldnt isolate target");
            }
        }
        return retVal;
        /*
         String[] values = fields[fields.length - 1].split(",");
         for (String val : values) {
         System.out.println("checking: " + val + ":");
         while (val.startsWith(" ")) {
         val = val.substring(1);
         }
         while (val.endsWith(" ")) {;
         val = val.substring(0, val.length() - 1);
         }
         }

         if (values.length != cols.size()) {
         throw new SQLException("Column and value mismatch!");
         }

         statement = conn.createStatement();
         String query = "SELECT * FROM " + tableName;
         String whereClause = " where ";
         for (int i = 0; i < cols.size() - 1; i++) {
         whereClause += cols.get(i) + "=" + values[i] + " and ";
         }
         whereClause += cols.get(cols.size() - 1) + "=" + values[values.length - 1];

         System.out.println("Executing " + query + whereClause);
         rs = statement.executeQuery(query + whereClause);
         if (!rs.next()) {
         String[][] retVal = new String[2][1];
         retVal[0][0] = query + whereClause + ";" + 0;
         retVal[1][0] = "delete from " + tableName + whereClause;
         return retVal;
         } else {
         throw new SQLException("Couldnt isolate target");
         }*/
    }

    public String[][] decodeDelete(String action) throws SQLException {
        // Sintax: DELETE FROM table_name WHERE some_column=some_value;
        // Delete * from table;
        // detele from table;

        String[] fields = action.split("where");

        // Get table name
        while (fields[0].contains("  ")) {
            fields[0] = fields[0].replaceAll("  ", " ");
        }
        while (fields[0].endsWith(" ")) {
            fields[0] = fields[0].substring(0, fields[0].length() - 1);
        }

        String[] t = fields[0].split(" ");
        String tableName = t[t.length - 1];

        String select;
        if (fields.length == 1) {
            // all records are being deleted
            select = "SELECT * FROM " + tableName;
        } else {
            // where clause
            select = "SELECT * FROM " + tableName + " where " + fields[1];
        }

        ResultSet rs = statement.executeQuery(select);
        int colNumber = rs.getMetaData().getColumnCount();

        String q = "";
        while (rs.next()) {
            q += "insert into table " + tableName + " values (";
            for (int i = 1; i <= colNumber - 1; i++) {
                q += "'" + rs.getObject(i) + "', ";
            }
            q += "'" + rs.getObject(colNumber) + "');;";

            
        }
        String [] reversers = q.split(";;");
        String[][] retVal = new String[2][reversers.length];
        
        for(int i=0; i<reversers.length; i++){
            retVal[0][i] = "select * from " + tableName + " where "+fields[1] + ";" + (reversers.length-i);
            retVal[1][i] = reversers[i];
        }

        return retVal;
    }

    private int contains(String[] a, String b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i].equals(b)) {
                return i;
            }
        }
        return -1;
    }

    private String[][] decodeUpdate(String action) throws SQLException {
        // UPDATE table_name SET column1=value1,column2=value2,... WHERE some_column=some_value;

        // Get table name
        while (action.contains("  ")) {
            action = action.replaceAll("  ", " ");
        }
        String tableName = action.split(" ")[1];

        // Parse string
        action = action.replace(" SET ", " set ").replace(" WHERE ", " where ");
        String setClause = action.substring(action.indexOf(" set ") + 5, action.indexOf(" where "));
        String whereClause = action.substring(action.indexOf(" where ") + 7);
        String[] setColumns = setClause.split(",");
        String[] setValues = new String[setColumns.length];
        for (int i = 0; i < setColumns.length; i++) {
            String[] f = setColumns[i].replaceAll(" ", "").split("=");
            setColumns[i] = f[0];
            setValues[i] = f[1];
        }

        // Get table's columns
        Statement statement = conn.createStatement();
        ArrayList<String> cols1 = new ArrayList();
        ResultSet rs = statement.executeQuery("describe " + tableName);
        while (rs.next()) {
            cols1.add(rs.getString(1));
        }
        String[] cols = new String[cols1.size()];
        for (int i = 0; i < cols.length; i++) {
            cols[i] = cols1.get(i);
        }

        // get reversers
        List<String[]> valuesAboutToChange = new ArrayList();
        List<String[]> valuesAfterChange = new ArrayList();
        rs = statement.executeQuery("SELECT * FROM " + tableName + " where " + whereClause);
        while (rs.next()) {
            String updateQuery = "update " + tableName + " set ";
            for (String t : setColumns) {
                updateQuery += t + "='" + rs.getObject(contains(cols, t) + 1) + "', ";
            }
            updateQuery = updateQuery.substring(0, updateQuery.length() - 2) + " where ";
            String whereClause2 = "", whereClause3 = "";
            for (int i = 0; i < cols.length - 1; i++) {
                int setIndex = contains(setColumns, cols[i]);
                if (setIndex == -1) {
                    whereClause2 += cols[i] + "='" + rs.getObject(i + 1) + "' and ";
                } else {
                    whereClause2 += cols[i] + "=" + setValues[setIndex] + " and ";
                }
                whereClause3 += cols[i] + "='" + rs.getObject(i + 1) + "' and ";
            }
            int setIndex = contains(setColumns, cols[cols.length - 1]);
            if (setIndex == -1) {
                whereClause2 += cols[cols.length - 1] + "='" + rs.getObject(cols.length) + "'";
            } else {
                whereClause2 += cols[cols.length - 1] + "=" + setValues[setIndex] + "";
            }
            whereClause3 += cols[cols.length - 1] + "='" + rs.getObject(cols.length) + "'";

            for (String[] t : valuesAfterChange) {
                if (whereClause2.equals(t[1])) {
                    throw new SQLException("Couldnt isolate target");
                }
            }

            for (String[] t : valuesAboutToChange) {
                if (whereClause3.equals(t[1])) {
                    throw new SQLException("Couldnt isolate target");
                }
            }

            valuesAboutToChange.add(new String[]{updateQuery, whereClause3});
            valuesAfterChange.add(new String[]{updateQuery, whereClause2});
        }

        String query = "SELECT * FROM " + tableName + " where ";
        String[][] retVal = new String[2][valuesAboutToChange.size()];
        for (int i = 0; i < valuesAboutToChange.size(); i++) {
            retVal[0][i] = query + valuesAboutToChange.get(i)[1] + ";1";
            retVal[1][i] = valuesAfterChange.get(i)[0] + valuesAfterChange.get(i)[1];
            //System.out.println("Checker: " + retVal[0][i]);
            //System.out.println("Reverser: " + retVal[1][i]);
        }
        return retVal;
    }

    @Override
    public String[][] getRevertAction(String action) throws SQLException {
        String[][] revertAction = null;
        while (action.startsWith(" ")) {
            action = action.substring(1);
        }

        if (action.toLowerCase().startsWith("insert")) {
            revertAction = decodeInsert(action);
        } else if (action.toLowerCase().startsWith("delete")) {
            revertAction = decodeDelete(action);
        } else if (action.toLowerCase().startsWith("update")) {
            revertAction = decodeUpdate(action);
        } else {
            throw new SQLException("Can't revert action: " + action);
        }
        return revertAction;
    }

    public R4N_TransactionHive(Connection conn, String id, FTHandler f, ConcurrencyHandler ch) throws Exception {
        super(conn, id, f,ch);
    }

    @Override
    public String getLockURI(String action) throws SQLException {
        String[] actions2 = action.replaceAll("  ", " ").split(" ");
        if (actions2[0].toLowerCase().equals("insert")) {
            return actions2[3];
        } else if (actions2[0].toLowerCase().equals("delete")) {
            return actions2[2];
        } else if (actions2[0].toLowerCase().equals("update")) {
            return actions2[1];
        } else {
            throw new SQLException("Cant fing URI");
        }
    }
}

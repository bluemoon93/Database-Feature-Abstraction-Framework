/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.DFM;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 *
 * @author bluemoon
 */
public class R4N_ResultSetRedis extends R4N_ResultSet {
    String key="";
    public R4N_ResultSetRedis(Connection conn, String query, int type) throws SQLException {
        super(conn, query, type);

        String[] fields = query.replace("  ", " ").split(" ");
        table = fields[1];
    }

    @Override
    public void insertRow() throws SQLException {
        if (!inserting) {
            throw new SQLException("Not on insert row");
        }

        String vals = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                vals += rs.getMetaData().getColumnName(i + 1) + " " + args[i] + " ";
            }
        }

        System.out.println("set " + key+ " " + vals);
        conn.createStatement().executeUpdate("set " + key+ " " + vals);
        maxRealRows++;
        map.put(maxRealRows, args);

        cancelRowUpdates();
        currentRealRow = maxRealRows;
        currentPretendRow = maxRealRows - deletedRows.size();
    }

    @Override
    public void updateRow() throws SQLException {
        if (inserting) {
            throw new SQLException("Inserting row");
        }

        Object[] vals = new Object[args.length];

        String valsUpdated = "";
        for (int i = 0; i < args.length; i++) {
            if (argsChanged[i]) {
                valsUpdated += rs.getMetaData().getColumnName(i + 1) + " " + args[i] + " ";
                vals[i] = args[i];
            } else {
                vals[i] = getObject(i + 1);
            }
        }

        System.out.println("set " + table + " " + valsUpdated);
        conn.createStatement().executeUpdate("set " + table + " " + valsUpdated);
        map.put(currentRealRow, vals);
    }

    @Override
    public void deleteRow() throws SQLException {
        if (inserting) {
            throw new SQLException("Inserting row");
        }

        System.out.println("del "+table);
        conn.createStatement().executeUpdate("del "+table);

        currentPretendRow--;
        deletedRows.add(currentRealRow);

        if (currentPretendRow != 0) {
            absolute(currentPretendRow);
        } else {
            beforeFirst();
        }
    }
    
    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        if(columnIndex>0){
            args[columnIndex - 1] = x;
            argsChanged[columnIndex - 1] = true;
        } else{
            key=(String)x;
        }
    }

}

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
public class R4N_ResultSetHive extends R4N_ResultSet {

    public R4N_ResultSetHive(Connection conn, String query, int type) throws SQLException {
        super(conn, query, type);

        String[] fields = query.replace("  ", " ").split(" ");
        int i;
        for (i = 0; i < fields.length; i++) {
            if (fields[i].toLowerCase().equals("from")) {
                break;
            }
        }
        table = fields[i + 1];
    }

    @Override
    public void insertRow() throws SQLException {
        if (!inserting) {
            throw new SQLException("Not on insert row");
        }

        String vals = "", cols = "";
        for (int i = 0; i < args.length - 1; i++) {
            vals += "'" + args[i] + "', "; //surroundWithPlica(args[i], rs.getMetaData().getColumnType(i + 1) == Types.VARCHAR) + ", ";
            cols += rs.getMetaData().getColumnName(i + 1) + ", ";
        }
        vals += "'" + args[args.length - 1] + "'";//surroundWithPlica(args[args.length - 1], rs.getMetaData().getColumnType(args.length) == Types.VARCHAR);
        cols += rs.getMetaData().getColumnName(args.length);

        System.out.println("insert into table " + table + "(" + cols + ") values (" + vals + ")");
        conn.createStatement().execute("insert into " + table + "(" + cols + ") values (" + vals + ")");
        maxRealRows++;
        map.put(maxRealRows, args);

        cancelRowUpdates();
        currentRealRow = maxRealRows;
        currentPretendRow = maxRealRows - deletedRows.size();
    }

    private String getCorrectSintax(Object a, int column) throws SQLException {
        String colName = rs.getMetaData().getColumnName(column);
        //System.out.println("Column name: " + colName);
        if (colName.contains(".")) {
            String[] fields = colName.split("\\.");
            colName = fields[fields.length - 1];
        }

        return colName + "='" + a + "'";// + surroundWithPlica(a, rs.getMetaData().getColumnType(column) == Types.VARCHAR);
    }

    /*private String surroundWithPlica(Object a, boolean var) throws SQLException {
     //if (var) {
     return "'" + a + "'";
     // } else {
     //    return a + "";
     //  }
     }*/
    @Override
    public void updateRow() throws SQLException {
        if (inserting) {
            throw new SQLException("Inserting row");
        }

        Object[] vals = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            if (argsChanged[i]) {
                vals[i] = args[i];
            } else {
                vals[i] = getObject(i + 1);
            }
        }

        String valsUpdated = "";
        for (int i = 0; i < vals.length - 1; i++) {
            valsUpdated += getCorrectSintax(vals[i], i + 1) + ", ";
        }
        valsUpdated += getCorrectSintax(vals[args.length - 1], args.length);

        String valsOriginal = "";
        for (int i = 0; i < args.length - 1; i++) {
            valsOriginal += getCorrectSintax(getObject(i + 1), i + 1) + " and ";
        }
        valsOriginal += getCorrectSintax(getObject(args.length), args.length);

        System.out.println("update " + table + " set " + valsUpdated + " where " + valsOriginal);
        conn.createStatement().execute("update " + table + " set " + valsUpdated + " where " + valsOriginal);
        map.put(currentRealRow, vals);
    }

    @Override
    public void deleteRow() throws SQLException {
        if (inserting) {
            throw new SQLException("Inserting row");
        }

        String valsOriginal = "";
        for (int i = 0; i < args.length - 1; i++) {
            valsOriginal += getCorrectSintax(getObject(i + 1), i + 1) + " and ";
        }
        valsOriginal += getCorrectSintax(getObject(args.length), args.length);

        System.out.println("delete from " + table + " where " + valsOriginal);
        conn.createStatement().execute("delete from " + table + " where " + valsOriginal);

        next();
        //currentPretendRow--;
        deletedRows.add(currentRealRow);
        currentRealRow++;

        /* if (currentPretendRow != 0) {
         absolute(currentPretendRow);
         } else {
         beforeFirst();
         }*/
    }

}

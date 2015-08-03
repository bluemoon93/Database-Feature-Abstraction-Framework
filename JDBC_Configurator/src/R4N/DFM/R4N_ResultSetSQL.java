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
public class R4N_ResultSetSQL extends R4N_ResultSet {

    public R4N_ResultSetSQL(Connection conn, String query, int type) throws SQLException {
        super(conn, query, type);
        
        String [] fields = query.replace("  ", " ").split(" ");
        int i;
        for(i=0; i<fields.length; i++){
            if(fields[i].toLowerCase().equals("from"))
                break;
        }
        table = fields[i+1];
    }

    

    @Override
    public void insertRow() throws SQLException {
        if (!inserting) {
            throw new SQLException("Not on insert row");
        }

        String vals = "", cols = "";
        for (int i = 0; i < args.length - 1; i++) {
            vals += surroundWithMeiaAspa(args[i], rs.getMetaData().getColumnType(i + 1) == Types.VARCHAR) + ", ";
            cols += rs.getMetaData().getColumnName(i + 1) + ", ";
        }
        vals += surroundWithMeiaAspa(args[args.length - 1], rs.getMetaData().getColumnType(args.length) == Types.VARCHAR);
        cols += rs.getMetaData().getColumnName(args.length);

        System.out.println("insert into " + table + "(" + cols + ") values (" + vals + ")");
        conn.createStatement().execute("insert into " + table + "(" + cols + ") values (" + vals + ")");
        maxRealRows++;
        map.put(maxRealRows, args);

        cancelRowUpdates();
        currentRealRow = maxRealRows;
        currentPretendRow = maxRealRows - deletedRows.size();
    }

    private String getCorrectSintax(Object a, int column) throws SQLException {
        return rs.getMetaData().getColumnName(column) + "=" + surroundWithMeiaAspa(a, rs.getMetaData().getColumnType(column) != Types.INTEGER && rs.getMetaData().getColumnType(column) != Types.DOUBLE);
    }

    private String surroundWithMeiaAspa(Object a, boolean var) throws SQLException {
        if (var) {
            return "'" + a + "'";
        } else {
            return a + "";
        }
    }

    @Override
    public void updateRow() throws SQLException {
        if (inserting) {
            throw new SQLException("Inserting row");
        }

        Object[] vals = new Object[args.length];
        int counter=0, counter2=0;
        for (int i = 0; i < args.length; i++) {
            if (argsChanged[i]) {
                counter++;
                vals[i] = args[i];
            } else {
                vals[i] = getObject(i + 1);
            }
        }

        String valsUpdated = "";
        for (int i = 0; i < args.length && counter2<counter; i++) {
            if(argsChanged[i]) {
                valsUpdated += getCorrectSintax(args[i], i + 1);
                counter2++;
            }
            if(counter2<counter-1){
                valsUpdated+=", ";
            }
        }
        //valsUpdated += getCorrectSintax(args[args.length - 1], args.length);

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

        currentPretendRow--;
        deletedRows.add(currentRealRow);

        if (currentPretendRow != 0) {
            absolute(currentPretendRow);
        } else {
            beforeFirst();
        }
    }

}

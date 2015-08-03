/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Demo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bluemoon
 */
public class ListDB {

    public static void main(String[] args) {
        String lol = "jdbc:sqlite:/home/bluemoon/Desktop/s-draca/test.db";
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(lol);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        List<String> tables = new ArrayList(), columns = new ArrayList();
        DatabaseMetaData meta;

        String query = "SELECT name FROM sqlite_master WHERE type='table';";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                // System.out.println("Table " + rs.getString("name"));
                String name = rs.getString("name");
                if(!name.equals("sqlite_sequence") && !name.equals("spt_fallback_db") && !name.equals("spt_fallback_dev") && 
                        !name.equals("spt_fallback_usg") && !name.equals("spt_monitor") && !name.equals("MSreplication_options"))
                tables.add(name);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tables.size(); i++) {
            query = "SELECT * FROM " + tables.get(i) + ";";
            System.out.println("Table " + tables.get(i));
            columns.clear();
            try {
                meta = conn.getMetaData();
                ResultSet rs = meta.getColumns(null, null, tables.get(i), null);
                System.out.print(" | ");
                while (rs.next()) {
                    System.out.print(rs.getString("COLUMN_NAME")+" | ");
                    columns.add(rs.getString("COLUMN_NAME"));
                }
                System.out.println();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                
                while (rs.next()) {
                    System.out.print(" | ");
                    for (int j = 0; j < columns.size(); j++) {
                        System.out.print(rs.getString(columns.get(j))+" | ");
                    }
                    System.out.println();
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }
}

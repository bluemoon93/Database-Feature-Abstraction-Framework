/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import R4N.DFM.R4N_ResultSet;
import R4N.DFM.R4N_ResultSetSQL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bluemoon
 */
public class MySQL_IAM {

    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("com.mysql.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/EMP", "rute", "1234lol")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("drop table if exists person2");
                statement.executeUpdate("create table person2 (id int primary key, name VARCHAR(20))");
                statement.executeUpdate("insert into person2 values (1, 'one')");
                statement.executeUpdate("insert into person2 values (2, 'two')");
                statement.executeUpdate("insert into person2 values (3, 'tres')");
                statement.executeUpdate("insert into person2 values (4, 'four')");
                statement.executeUpdate("insert into person2 values (5, 'five')");
            }

            //Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //ResultSet rs = stmt.executeQuery("SELECT * FROM person2");
            ResultSet rs = new R4N_ResultSetSQL(connection, "SELECT * FROM person2", ResultSet.TYPE_SCROLL_SENSITIVE);
            printRS(rs);

            System.out.println("Updating 3");
            rs.absolute(3);
            rs.updateObject(2, "three");
            rs.updateRow();

            rs.wasNull();

            rs.beforeFirst();
            printRS(rs);

            System.out.println("Inserting 6");
            rs.beforeFirst();
            rs.moveToInsertRow();       // moves cursor to the insert row
            rs.updateObject(1, 6);
            rs.updateObject(2, "six");
            rs.insertRow();
            rs.moveToCurrentRow();
            printRS(rs);

            rs.wasNull();

            rs.moveToInsertRow();       // moves cursor to the insert row
            rs.updateObject(1, 26);
            rs.updateObject(2, "twentysix");
            rs.insertRow();
            System.out.println("twentysix at (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");
            rs.wasNull();

            rs.absolute(3);     // moves cursor to the insert row
            System.out.println("Deleting (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");
            rs.deleteRow();     // deletes the row
            System.out.println("Cursor now at (" + rs.getObject(1) + ", " + rs.getObject(2) + ")\n");
            rs.beforeFirst();
            printRS(rs);

            rs.wasNull();

            rs.absolute(3);     // moves cursor to the insert row
            System.out.println("3 BD At (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");
            rs.deleteRow();
            System.out.println("3 AD At (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");
            rs.wasNull();

            printRS(rs);

            
            System.out.println("inserting 16");
            rs.moveToInsertRow();       // moves cursor to the insert row
            rs.updateObject(1, 16);
            rs.updateObject(2, "sixteen");
            rs.insertRow();
            System.out.println("sixteen At (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");
            rs.wasNull();

            System.out.println("killing spree");
            rs.absolute(4);    
            rs.deleteRow();
            rs.wasNull();
            
            rs.absolute(4);    
            rs.deleteRow();
            rs.wasNull();
            
            rs.absolute(1);    
            rs.deleteRow();
            rs.wasNull();
            
            rs.absolute(1);    
            rs.deleteRow();
            rs.wasNull();
            
            System.out.println("inserting");
            rs.moveToInsertRow();       // moves cursor to the insert row
            rs.updateObject(1, 17);
            rs.updateObject(2, "seventeen");
            rs.insertRow();

            rs.absolute(2);    
            System.out.println("2 At (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");

            rs.beforeFirst();
            printRS(rs);
            
            System.out.println("Query");
            
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM person2");
            printRS(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printRS(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println("client (" + rs.getObject(1) + ", " + rs.getObject(2) + ")");
        }
        System.out.println();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import S_Orders.IS_Orders;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bluemoon
 */
public class Derby {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        
        //Driver driverClass = (Driver) DriverManager.getDriver("jdbc:derby:metastore_db;create=true");
        //System.out.println("\nDriver for jdbc:derby://somehost/somedb");
        //System.out.println("   " + driverClass.getClass().getName());

        Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/metastore_db;create=true", "APP", "mine");
        Statement lol = conn.createStatement();
        lol.execute("drop table test");
        lol.execute("create table test (id integer)");
        lol.execute("insert into test values (19)");
        ResultSet rs = lol.executeQuery("select * from test");
        if(rs.next()) System.out.println("Val = "+rs.getInt(1));
        System.out.println("Done");
        
        IS_Orders S_Orders=null;
        S_Orders.uShipCity("lol");
        S_Orders.updateRow();
    }
}

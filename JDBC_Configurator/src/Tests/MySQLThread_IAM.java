/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bluemoon
 */
public class MySQLThread_IAM extends Thread {
    @Override
    public void run() {
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySQLThread_IAM.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/EMP", "rute", "1234lol")) {
            
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("insert into person2 values ('7', 'seven')");
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
            System.out.println("Done");
        }catch(SQLException ex){
            ex.printStackTrace();
        }
    }
}

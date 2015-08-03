/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FaultTolerance_Noop;
import R4N.CH.GraphClient_Sock;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bluemoon
 */
public class MySQLThread_Deadlock extends Thread {

    @Override
    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySQLThread_Deadlock.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/EMP", "root", "1234lol")) {

            /*connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                System.out.println("T - Inserting value");
                statement.executeUpdate("insert into person1 values (2, 'thread')");
                //connection.commit();
                
                System.out.println("T - Waiting");
                Thread.sleep(1000);
                
                System.out.println("T - Inserting another value");
                statement.executeUpdate("insert into person2 values(2, 'thread')");
                connection.commit();
            }
            connection.setAutoCommit(true);
            */
            
            R4N_Transaction trans = new R4N_TransactionSQL(connection, "T -", new FTHandler(new FaultTolerance_Noop()), 
                    new ConcurrencyHandler(new GraphClient_Sock("192.168.1.68", 5922)));
            
            System.out.println("T - Inserting value");
            trans.executeUpdate("insert into person1 values (1, 'thread')");
            
            System.out.println("T - Waiting");
            Thread.sleep(5000);
            
            System.out.println("T - Inserting another value");
            trans.executeUpdate("insert into person2 values (2, 'thread')");
            trans.commit();  
            
            trans.close();
            
            System.out.println("T - Done");
            /*
             R4N_Transaction trans = new R4N_TransactionSQL(connection, "thread1");

             System.out.println("\tStarting THR transaction!");
             System.out.println("here1");
             trans.executeUpdate("insert into person2 values(100, 'YOLO')");
             System.out.println("here2");
             trans.executeUpdate("insert into person2 values(101, 'ROFL')");
             System.out.println("here3");

             trans.executeUpdate("delete from person2 where id=101");
             System.out.println("here4");
             printRS(trans.executeQuery("select * from person2"));
             System.out.println("\tEnding THR transaction!");
             trans.commit();
            
             trans.close();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

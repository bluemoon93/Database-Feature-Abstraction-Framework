package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FaultTolerance_Noop;
import R4N.CH.GraphClient_Sock;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL_DeadLock {

    public static void main(String[] args) throws Exception {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("com.mysql.jdbc.Driver");

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/EMP", "rute", "1234lol")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("drop table if exists person2");
                statement.executeUpdate("drop table if exists person1");
                statement.executeUpdate("create table person2 (id int, name VARCHAR(20))");
                statement.executeUpdate("create table person1 (id int, name VARCHAR(20))");
            }
            
            //ConcurrencyHandler.addGraph(new GraphClient_Sock("localhost", 5922));
            //ConcurrencyHandler.addGraph(new GraphClient_Local());
            //ConcurrencyHandler_v2.setGraph(new GraphClient_Local());
            //ConcurrencyHandler_v2 ch = new ConcurrencyHandler_v2(new GraphClient_Sock("192.168.1.101", 5922));
            //ConcurrencyHandler_v2.setGraph(new GraphClient_Sock("192.168.1.101", 5922)); //68 pc grande, 101 o pequeno
            
            System.out.println("About to start");
            Thread.sleep(1000);
            
            Thread thr = new MySQLThread_Deadlock();
            thr.start();

            /*connection.setAutoCommit(false);
             try (Statement statement = connection.createStatement()) {
             System.out.println("M - Inserting value");
             statement.executeUpdate("insert into person2 values (1, 'main')");
             //connection.commit();
                
             System.out.println("M - Waiting");
             Thread.sleep(1000);
                
             System.out.println("M - Inserting another value");
             statement.executeUpdate("insert into person1 values(1, 'main')");
             connection.commit();
             }
             connection.setAutoCommit(true); */
            R4N_Transaction trans = new R4N_TransactionSQL(connection, "M -", new FTHandler(new FaultTolerance_Noop()), 
                    new ConcurrencyHandler(new GraphClient_Sock("192.168.1.68", 5922)));

            System.out.println("M - Inserting value");
            trans.executeUpdate("insert into person2 values (2, 'main')");

            System.out.println("M - Waiting");
            Thread.sleep(10000);

            System.out.println("M - Inserting another value");
            trans.executeUpdate("insert into person1 values (1, 'main')");
            trans.commit();
            trans.close();
            System.out.println("M - Done");

            Thread.sleep(1000);

            Statement statement = connection.createStatement();
            printRS(statement.executeQuery("select * from person2"));
            printRS(statement.executeQuery("select * from person1"));

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void printRS(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println("(" + rs.getInt(1) + ", " + rs.getString(2) + ")");
        }
        System.out.println();
    }
}

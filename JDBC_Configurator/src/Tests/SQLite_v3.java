package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Local;
import R4N.CH.GraphClient_Sock;
import R4N.FT.FailureRecovery;
import R4N.FT.FaultTolerance;
import R4N.FT.FaultTolerance_Noop;
import R4N.FT.FaultTolerance_Sock;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite_v3 {

    public static void main(String[] args) throws Exception {

        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db")) {
            try (Statement statement = conn.createStatement()) {
                printRS(statement.executeQuery("select * from Customers"));
                statement.executeUpdate("drop table Customers");
                statement.executeUpdate("create table Customers (CustomerID string, CompanyName string, ContactName string, ContactTitle string, Address string, "
                        + "City string, Region string, PostalCode string, Country string, Phone string, Fax string)");

                statement.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, "
                        + "City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('DEFAULT', 'Yahoo', 'Yiruma', 'Manager', "
                        + "'BakerBoulevard', 'NY', 'New York', '12345', 'USA', '555-2234', '2-234')");

                FTHandler ftm; // = new FaultToleranceManager_Noop();
                ftm = new FTHandler(new FaultTolerance_Sock("localhost", 5921));
                //FailureRecovery.revertAll(conn, ftm);
                ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Sock("localhost", 5922));
                
                R4N_Transaction trans = new R4N_TransactionSQL(conn, "sqlite", ftm,ch);
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Inserting");
                trans.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('NEW', 'Google', 'George', 'Manager', 'BakerBoulevard', 'LA', 'California', '1', 'USA', '555-2234', '2')");
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Rolling back");
                trans.rollback();
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Inserting");
                trans.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('NEW', 'Google', 'George', 'Manager', 'BakerBoulevard', 'LA', 'California', '1', 'USA', '555-2234', '2')");
                printRS(trans.executeQuery("select * from Customers"));
                
                System.out.println("waiting for a bit");
                Thread.sleep(10000);
                
                System.out.println("Updating");
                trans.executeUpdate("update Customers set City='LAX', Region='Californication' "
                        + "where PostalCode='1' and Fax='2'");
                printRS(trans.executeQuery("select * from Customers"));
                
                System.out.println("Rolling back");
                trans.rollback();
                
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Inserting");
                trans.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('NEW', 'Google', 'George', 'Manager', 'BakerBoulevard', 'LA', 'California', '1', 'USA', '555-2234', '2')");
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Committing");
                trans.commit();
                
                System.out.println("Updating");
                trans.executeUpdate("update Customers set City='LAX', Region='Californication' "
                        + "where PostalCode='1' and Fax='2'");
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Rolling back");
                trans.rollback();
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Deleting");
                trans.executeUpdate("DELETE FROM Customers where PostalCode='1' and Fax='2'");
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Rolling back");
                trans.rollback();
                printRS(trans.executeQuery("select * from Customers"));
                System.out.println("Deleting");
                trans.executeUpdate("DELETE FROM Customers where PostalCode='1' and Fax='2'");
                printRS(trans.executeQuery("select * from Customers"));
                trans.close();

            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void printRS(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println("(" + rs.getObject("CustomerID") + ", " + rs.getObject("CompanyName") + ", " + rs.getObject("ContactName")+ ", " + rs.getObject("City")+ ", " + rs.getObject("Region") + ")");
        }
    }
}

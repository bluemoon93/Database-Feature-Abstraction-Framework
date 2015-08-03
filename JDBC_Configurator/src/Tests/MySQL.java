package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Local;
import R4N.FT.FaultTolerance;
import R4N.FT.FaultTolerance_Noop;
import R4N.DFM.R4N_CallableStatement;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQL {

    private static R4N_CallableStatement[] SPs = null;

    private static R4N_CallableStatement getSP(String a) {
        if (SPs == null) {
            return null;
        }
        for (R4N_CallableStatement SP : SPs) {
            if (SP.name.equals(a)) {
                return SP;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("com.mysql.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/EMP", "rute", "1234lol")) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("drop table Orders");
                statement.executeUpdate("create table Orders (OrderID int, CustomerID varchar(50), EmployeeID int, OrderDate date, RequiredDate date, ShippedDate date,"
                        + "ShipVia int, Freight double,"
                        + "ShipName varchar(50), ShipAddress varchar(50), ShipCity varchar(50), ShipRegion varchar(50),  ShipPostalCode varchar(50), ShipCountry varchar(50))");

                statement.executeUpdate("INSERT INTO Orders"
                        + "(OrderID, CustomerID,EmployeeID,OrderDate,RequiredDate,ShippedDate,ShipVia,Freight,"
                        + "ShipName,ShipAddress,ShipCity,ShipRegion,ShipPostalCode,ShipCountry) "
                        + "VALUES (11110, 'GREAL', 11, '2013-02-04', '2013-06-04', '2013-04-04', 1, 2.0, "
                        + "'ShipName', 'Address', 'City', 'Region', 'Zip Code', 'Country')");

                statement.executeUpdate("drop table Customers");
                statement.executeUpdate("create table Customers (CustomerID varchar(50), CompanyName varchar(50), ContactName varchar(50), ContactTitle varchar(50), Address varchar(50), "
                        + "City varchar(50), Region varchar(50), PostalCode varchar(50), Country varchar(50), Phone varchar(50), Fax varchar(50))");

                statement.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, "
                        + "City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('GREAL', 'GreatLakesFM', 'Howard', 'Manager', "
                        + "'BakerBoulevard', 'LA', 'California', '12345', 'USA', '555-2234', '2-234')");
                statement.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, "
                        + "City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('JORGE', 'Broomy', 'John', 'Manager', "
                        + "'BakerBoulevard', 'LA', 'California', '12345', 'USA', '555-2234', '2-234')");
                statement.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, "
                        + "City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('JORGE', 'Broomy', 'Johny', 'Manager', "
                        + "'BakerBoulevard', 'LA', 'California', '12345', 'USA', '555-2234', '2-234')");
                FTHandler ftm = new FTHandler(new FaultTolerance_Noop());
                ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());
                R4N_Transaction trans2 = new R4N_TransactionSQL(conn, "mysql", ftm, ch);
                printRS(trans2.executeQuery("select * from Customers"));
                trans2.executeUpdate("update Customers set CustomerID='GROPE', CompanyName='NewComp' where Region='California' and City='LA'");
                System.out.println("Executing: update Customers set CustomerID='GROPE', CompanyName='NewComp' where Region='California' and City='LA'");
                printRS(trans2.executeQuery("select * from Customers"));
                trans2.rollback();
                printRS(trans2.executeQuery("select * from Customers"));
                trans2.close();

                int runs = 1001, skips = runs / 10;
                long[] times = new long[11], times2 = new long[11], times3 = new long[11];
                long t, t2;

                for (int i = 1; i <= runs; i += skips) {
                    t = System.nanoTime();

                    for (int j = 0; j < i; j++) {
                        statement.executeUpdate("INSERT INTO Customers"
                                + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                                + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
                    }
                    t2 = System.nanoTime();
                    times[i / skips] = t2 - t;
                    System.out.print(".");

                    t = System.nanoTime();

                    for (int j = 0; j < i; j++) {
                        statement.executeUpdate("update Customers "
                                + "set City='LAX', Region='Californication' "
                                + "where PostalCode='" + i + "' and Fax='" + j + "'");
                    }
                    t2 = System.nanoTime();
                    times2[i / skips] = t2 - t;

                    t = System.nanoTime();

                    for (int j = 0; j < i; j++) {
                        statement.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
                    }
                    t2 = System.nanoTime();
                    times3[i / skips] = t2 - t;
                    System.out.print(".");
                }
                System.out.println("");
                for (int i = 0; i <= 10; i += 1) {
                    System.out.print("(" + (i * skips + 1) + "," + times[i] / 1000000.0 + ")");
                }
                System.out.println();
                for (int i = 0; i <= 10; i += 1) {
                    System.out.print("(" + (i * skips + 1) + "," + times2[i] / 1000000.0 + ")");
                }
                System.out.println();
                for (int i = 0; i <= 10; i += 1) {
                    System.out.print("(" + (i * skips + 1) + "," + times3[i] / 1000000.0 + ")");
                }
                System.out.println();
                //------------------------

                for (int i = 1; i <= runs; i += skips) {
                    t = System.nanoTime();
                    R4N_Transaction trans = new R4N_TransactionSQL(conn, "mysql", ftm, ch);
                    for (int j = 0; j < i; j++) {
                        trans.executeUpdate("INSERT INTO Customers"
                                + "(CustomerID,CompanyName,ContactName,ContactTitle,Address,City,Region,PostalCode,Country,Phone,Fax) "
                                + "VALUES ('GREPL','GreatLakesFM','Howard','Manager','BakerBoulevard','LA','California','" + i + "','USA','555-2234','" + j + "')");
                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times[i / skips] = t2 - t;

                    t = System.nanoTime();
                    trans = new R4N_TransactionSQL(conn, "mysql", ftm, ch);
                    for (int j = 0; j < i; j++) {
                        trans.executeUpdate("update Customers "
                                + "set City='LAX', Region='Californication' "
                                + "where PostalCode='" + i + "' and Fax='" + j + "'");
                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times2[i / skips] = t2 - t;
                    System.out.print(".");

                    t = System.nanoTime();
                    trans = new R4N_TransactionSQL(conn, "mysql", ftm, ch);
                    for (int j = 0; j < i; j++) {
                        trans.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times3[i / skips] = t2 - t;
                }

                for (int i = 0; i <= 10; i += 1) {
                    System.out.print("(" + (i * skips + 1) + "," + times[i] / 1000000.0 + ")");
                }
                System.out.println();
                for (int i = 0; i <= 10; i += 1) {
                    System.out.print("(" + (i * skips + 1) + "," + times2[i] / 1000000.0 + ")");
                }
                System.out.println();
                for (int i = 0; i <= 10; i += 1) {
                    System.out.print("(" + (i * skips + 1) + "," + times3[i] / 1000000.0 + ")");
                }

                System.out.println();
                
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void printRS(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println("(" + rs.getObject("CustomerID") + ", " + rs.getObject("CompanyName") + ", " + rs.getObject("ContactName") + ")");
        }
        System.out.println("Done.\n");
    }
}

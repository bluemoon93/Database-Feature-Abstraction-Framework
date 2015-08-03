package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FaultTolerance;
import R4N.FT.FaultTolerance_Disk;
import R4N.FT.FaultTolerance_Noop;
import R4N.FT.FaultTolerance_Sock;
import R4N.CH.GraphClient_Local;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLite_v4 {

    public static void lalala() {

        String noop = "(100,0.154)(200,0.229)(300,0.299)(400,0.386)(500,0.478)(600,0.531)(700,0.622)(800,0.683)(900,0.811)(1000,0.823)";
        String rem = "(100,8.008)(200,16.010)(300,24.008)(400,32.014)(500,40.028)(600,48.031)(700,56.036)(800,64.097)(900,72.179)(1000,80.273)";
        String fs = "(100,8.003)(200,16.009)(300,24.016)(400,32.022)(500,40.031)(600,48.037)(700,56.057)(800,64.045)(900,72.042)(1000,80.085)";
      
        
        String[] fields1 = noop.split("[(,)]");
        String[] fields2 = rem.split("[(,)]");
        String[] fields3 = fs.split("[(,)]");
        for (int i = 1; i < fields1.length; i += 3) {
            System.out.print("(" + fields1[i] + "," + String.format("%2.2f", Double.parseDouble(fields1[i + 1]) - Double.parseDouble(fields1[i + 1])) + ")");
        }
        for (int i = 1; i < fields1.length; i += 3) {
            System.out.print("(" + fields1[i] + "," + String.format("%2.2f", Double.parseDouble(fields2[i + 1]) - Double.parseDouble(fields1[i + 1])) + ")");
        }
        System.out.println();
        for (int i = 1; i < fields1.length; i += 3) {
            System.out.print("(" + fields1[i] + "," + String.format("%2.2f", Double.parseDouble(fields3[i + 1]) - Double.parseDouble(fields1[i + 1])) + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db")) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("drop table Orders");
                statement.executeUpdate("create table Orders (OrderID int,CustomerID string, EmployeeID int, OrderDate date, RequiredDate date, ShippedDate date,"
                        + "ShipVia int, Freight double,"
                        + " ShipName string, ShipAddress string, ShipCity string , ShipRegion string,  ShipPostalCode string, ShipCountry string)");

                statement.executeUpdate("INSERT INTO Orders"
                        + "(OrderID, CustomerID,EmployeeID,OrderDate,RequiredDate,ShippedDate,ShipVia,Freight,"
                        + "ShipName,ShipAddress,ShipCity,ShipRegion,ShipPostalCode,ShipCountry) "
                        + "VALUES (11110, 'GREAL', 11, '2013-02-04', '2013-06-04', '2013-04-04', 1, 2.0, "
                        + "'ShipName', 'Address', 'City', 'Region', 'Zip Code', 'Country')");

                statement.executeUpdate("drop table Customers");
                statement.executeUpdate("create table Customers (CustomerID string, CompanyName string, ContactName string, ContactTitle string, Address string, "
                        + "City string, Region string, PostalCode string, Country string, Phone string, Fax string)");

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
                FTHandler ftm;
                ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());

                int howManyTests = 5;
                int[] records = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
                long[][] times = new long[howManyTests][records.length], times2 = new long[howManyTests][records.length],
                        times3 = new long[howManyTests][records.length];
                long t, t2;

                for (int ftmTypes = 0; ftmTypes < 3; ftmTypes++) {
                    if (ftmTypes == 0) {
                        ftm = new FTHandler(new FaultTolerance_Noop());
                    } else if (ftmTypes == 1) {
                        ftm = new FTHandler(new FaultTolerance_Sock("localhost", 5921));
                    } else {
                        ftm = new FTHandler(new FaultTolerance_Disk());
                    }
                    for (int k = 0; k < howManyTests; k++) {
                        for (int i = 0; i < records.length; i++) {
                            t = System.nanoTime();
                            R4N_Transaction trans = new R4N_TransactionSQL(conn, "sqlite", ftm, ch);
                            for (int j = 0; j < records[i]; j++) {
                                trans.executeUpdate("INSERT INTO Customers"
                                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                                        + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
                            }
                            trans.close();
                            t2 = System.nanoTime();
                            times[k][i] = t2 - t;
                            System.out.print(".");

                            t = System.nanoTime();
                            trans = new R4N_TransactionSQL(conn, "sqlite", ftm, ch);
                            for (int j = 0; j < records[i]; j++) {
                                trans.executeUpdate("update Customers "
                                        + "set City='LAX', Region='Californication' "
                                        + "where PostalCode='" + i + "' and Fax='" + j + "'");
                            }
                            trans.close();
                            t2 = System.nanoTime();
                            times2[k][i] = t2 - t;
                            System.out.print(".");

                            t = System.nanoTime();
                            trans = new R4N_TransactionSQL(conn, "sqlite", ftm, ch);
                            for (int j = 0; j < records[i]; j++) {
                                trans.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
                            }
                            trans.close();
                            t2 = System.nanoTime();
                            times3[k][i] = t2 - t;
                            System.out.println(".");
                        }
                    }
                    long[][] averages2 = new long[4][records.length];
                    for (int i = 0; i < howManyTests; i++) {
                        for (int j = 0; j < records.length; j++) {
                            averages2[0][j] += times[i][j];
                            averages2[1][j] += times2[i][j];
                            averages2[2][j] += times3[i][j];
                            //averages2[3][j] += times4[i][j];
                        }
                    }
                    System.out.println("Inserts:");
                    for (int j = 0; j < records.length; j++) {
                        averages2[0][j] /= howManyTests;
                        averages2[1][j] /= howManyTests;
                        averages2[2][j] /= howManyTests;
                        System.out.print("(" + records[j] + "," + averages2[0][j] / 1000000.0 + ")");
                    }
                    System.out.println("\nUpdates:");
                    for (int j = 0; j < records.length; j++) {
                        System.out.print("(" + records[j] + "," + averages2[1][j] / 1000000.0 + ")");
                    }
                    System.out.println("\nDeletes:");
                    for (int j = 0; j < records.length; j++) {
                        System.out.print("(" + records[j] + "," + averages2[2][j] / 1000000.0 + ")");
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void printRS(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println("(" + rs.getObject("CustomerID") + ", " + rs.getObject("CompanyName") + ", " + rs.getObject("ContactName") + ")");
        }
        System.out.println("Done.\n");
    }
}

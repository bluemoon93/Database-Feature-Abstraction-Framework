/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Local;
import R4N.FT.FaultTolerance;
import R4N.FT.FaultTolerance_Noop;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;

/**
 *
 * @author bluemoon
 */
public class MongoDB {

    static void print(String name, ResultSet res) throws SQLException {
        System.out.println(name);
        while (res.next()) {
            System.out.println("\t" + res.getString("name") + "\t" + res.getInt("age") + "\t" + res.getObject(0));
        }
    }

    static void printRS(ResultSet rst) throws SQLException {
        ResultSetMetaData meta = rst.getMetaData();
        int numColumns = meta.getColumnCount();

        System.out.print(meta.getColumnName(1));
        for (int j = 2;
                j
                <= meta.getColumnCount();
                j++) {
            System.out.print(", " + meta.getColumnName(j));
        }

        System.out.println();

        while (rst.next()) {
            System.out.print(rst.getObject(1));
            for (int j = 2; j <= numColumns; j++) {
                System.out.print(", " + rst.getObject(meta.getColumnName(j)));
            }
            System.out.println();
        }
        rst.close();
    }

    public static void main(String args[]) throws Exception {

        Class.forName("mongodb.jdbc.MongoDriver");
        String url = "jdbc:mongo://localhost/mydb?rebuildschema=true";
        Connection con = DriverManager.getConnection(url, "bluemoon", "1234lol");
        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        stmt.executeUpdate("drop table Orders");
        stmt.executeUpdate("INSERT INTO Orders"
                + "(OrderID, CustomerID,EmployeeID,OrderDate,RequiredDate,ShippedDate,ShipVia,Freight,"
                + "ShipName,ShipAddress,ShipCity,ShipRegion,ShipPostalCode,ShipCountry) "
                + "VALUES (11110, 'GREAL', 11, '2013-02-04', '2013-06-04', '2013-04-04', 1, 2.0, "
                + "'ShipName', 'Address', 'City', 'Region', 'Zip Code', 'Country')");

        stmt.executeUpdate("drop table Customers");
        stmt.executeUpdate("INSERT INTO Customers"
                + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, "
                + "City, Region, PostalCode, Country, Phone, Fax) "
                + "VALUES ('GREAL', 'Great Lakes FM', 'Howard', 'Manager', "
                + "'Baker Boulevard', 'LA', 'California', '12345', 'USA', '555-2234', '(2) 234')");

        printRS(stmt.executeQuery("SELECT * FROM Orders"));
        printRS(stmt.executeQuery("SELECT * FROM Customers"));

        int runs = 1001, skips = runs / 10;
        long[] times = new long[11], times2 = new long[11], times3 = new long[11];
        long t, t2;

        for (int i = 1; i <= runs; i += skips) {
            t = System.nanoTime();

            for (int j = 0; j < i; j++) {
                stmt.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                        + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
            }
            t2 = System.nanoTime();
            times[i / skips] = t2 - t;
            System.out.print(".");

            t = System.nanoTime();

            for (int j = 0; j < i; j++) {
                stmt.executeUpdate("update Customers "
                        + "set City='LAX', Region='Californication' "
                        + "where PostalCode='" + i + "' and Fax='" + j + "'");
            }
            t2 = System.nanoTime();
            times2[i / skips] = t2 - t;

            t = System.nanoTime();

            for (int j = 0; j < i; j++) {
                stmt.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
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
        FTHandler ftm = new FTHandler(new FaultTolerance_Noop());
        ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());
        for (int i = 1; i <= runs; i += skips) {
            t = System.nanoTime();
            R4N_Transaction trans = new R4N_TransactionSQL(con, "mongo", ftm, ch);
            for (int j = 0; j < i; j++) {
                trans.executeUpdate("INSERT INTO Customers"
                        + "(CustomerID,CompanyName,ContactName,ContactTitle,Address,City,Region,PostalCode,Country,Phone,Fax) "
                        + "VALUES ('GREPL','GreatLakesFM','Howard','Manager','BakerBoulevard','LA','California','" + i + "','USA','555-2234','" + j + "')");
            }
            trans.close();
            t2 = System.nanoTime();
            times[i / skips] = t2 - t;

            t = System.nanoTime();
            trans = new R4N_TransactionSQL(con, "mongo", ftm,ch);
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
            trans = new R4N_TransactionSQL(con, "mongo", ftm,ch);
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

        stmt.close();
        con.close();
    }
}

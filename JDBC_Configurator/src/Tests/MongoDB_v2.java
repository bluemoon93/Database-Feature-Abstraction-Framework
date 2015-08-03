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
import java.sql.*;

/**
 *
 * @author bluemoon
 */
public class MongoDB_v2 {

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
        Connection conn = DriverManager.getConnection(url, "bluemoon", "1234lol");
        Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        statement.executeUpdate("drop table Orders");
        statement.executeUpdate("INSERT INTO Orders"
                + "(OrderID, CustomerID,EmployeeID,OrderDate,RequiredDate,ShippedDate,ShipVia,Freight,"
                + "ShipName,ShipAddress,ShipCity,ShipRegion,ShipPostalCode,ShipCountry) "
                + "VALUES (11110, 'GREAL', 11, '2013-02-04', '2013-06-04', '2013-04-04', 1, 2.0, "
                + "'ShipName', 'Address', 'City', 'Region', 'Zip Code', 'Country')");

        statement.executeUpdate("drop table Customers");
        statement.executeUpdate("INSERT INTO Customers"
                + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, "
                + "City, Region, PostalCode, Country, Phone, Fax) "
                + "VALUES ('GREAL', 'Great Lakes FM', 'Howard', 'Manager', "
                + "'Baker Boulevard', 'LA', 'California', '12345', 'USA', '555-2234', '(2) 234')");

        printRS(statement.executeQuery("SELECT * FROM Orders"));
        printRS(statement.executeQuery("SELECT * FROM Customers"));
        FTHandler ftm = new FTHandler(new FaultTolerance_Noop());
        ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());
        int howManyTests = 5;
        int[] records = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        long[][] times = new long[howManyTests][records.length], times2 = new long[howManyTests][records.length],
                times3 = new long[howManyTests][records.length], times4 = new long[howManyTests][records.length];
        long t, t2;

        for (int k = 0; k < howManyTests; k++) {
            for (int i = 0; i < records.length; i++) {
                t = System.nanoTime();

                for (int j = 0; j < records[i]; j++) {
                    statement.executeUpdate("INSERT INTO Customers"
                            + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                            + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
                }
                t2 = System.nanoTime();
                times[k][i] = t2 - t;
                System.out.print(".");

                t = System.nanoTime();
                for (int j = 0; j < records[i]; j++) {
                    statement.executeUpdate("update Customers "
                            + "set City='LAX', Region='Californication' "
                            + "where PostalCode='" + i + "' and Fax='" + j + "'");
                }
                t2 = System.nanoTime();
                times2[k][i] = t2 - t;
                System.out.print(".");

                t = System.nanoTime();
                for (int j = 0; j < records[i]; j++) {
                    statement.executeQuery("select * from Customers "
                            + "where PostalCode='" + i + "' and Fax='" + j + "'");
                }
                t2 = System.nanoTime();
                times4[k][i] = t2 - t;
                System.out.print(".");

                t = System.nanoTime();
                for (int j = 0; j < records[i]; j++) {
                    statement.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
                }
                t2 = System.nanoTime();
                times3[k][i] = t2 - t;
                System.out.println(".");
            }
        }
        long[][] averages = new long[4][records.length];
        for (int i = 0; i < howManyTests; i++) {
            for (int j = 0; j < records.length; j++) {
                averages[0][j] += times[i][j];
                averages[1][j] += times2[i][j];
                averages[2][j] += times3[i][j];
                averages[3][j] += times4[i][j];
            }
        }
        for (int j = 0; j < records.length; j++) {
            averages[0][j] /= howManyTests;
            averages[1][j] /= howManyTests;
            averages[2][j] /= howManyTests;
            averages[3][j] /= howManyTests;
                    //System.out.println("For " + records[j] + " records inserted, avg was " + averages[0][j] / 1000000.0 + " ms");
            //System.out.println("For " + records[j] + " records updated,  avg was " + averages[1][j] / 1000000.0 + " ms");
            //System.out.println("For " + records[j] + " records deleted,  avg was " + averages[2][j] / 1000000.0 + " ms");
            //System.out.println("For " + records[j] + " records queried,  avg was " + averages[3][j] / 1000000.0 + " ms");
        }
        System.out.println("\nInserts:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages[0][j] / 1000000.0 + ")");
        }
        System.out.println("\nUpdates:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages[1][j] / 1000000.0 + ")");
        }
        System.out.println();
        System.out.println("\nDeletes:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages[2][j] / 1000000.0 + ")");
        }
        System.out.println("\nSelects:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages[3][j] / 1000000.0 + ")");
        }
        System.out.println();

        //------------------------
        for (int k = 0; k < howManyTests; k++) {
            for (int i = 0; i < records.length; i++) {
                t = System.nanoTime();
                R4N_Transaction trans = new R4N_TransactionSQL(conn, "mongo", ftm,ch);
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
                trans = new R4N_TransactionSQL(conn, "mongo", ftm,ch);
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
                trans = new R4N_TransactionSQL(conn, "mongo", ftm,ch);
                for (int j = 0; j < records[i]; j++) {
                    trans.executeQuery("select * from Customers "
                            + "where PostalCode='" + i + "' and Fax='" + j + "'");
                }
                trans.close();
                t2 = System.nanoTime();
                times4[k][i] = t2 - t;
                System.out.print(".");

                t = System.nanoTime();
                trans = new R4N_TransactionSQL(conn, "mongo", ftm,ch);
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
                averages2[3][j] += times4[i][j];
            }
        }
        for (int j = 0; j < records.length; j++) {
            averages2[0][j] /= howManyTests;
            averages2[1][j] /= howManyTests;
            averages2[2][j] /= howManyTests;
            averages2[3][j] /= howManyTests;
                    //System.out.println("For " + records[j] + " records inserted, avg was " + averages2[0][j] / 1000000.0 + " ms");
            //System.out.println("For " + records[j] + " records updated,  avg was " + averages2[1][j] / 1000000.0 + " ms");
            //System.out.println("For " + records[j] + " records deleted,  avg was " + averages2[2][j] / 1000000.0 + " ms");
            //System.out.println("For " + records[j] + " records queried,  avg was " + averages2[3][j] / 1000000.0 + " ms");
        }
        System.out.println("\nInserts:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages2[0][j] / 1000000.0 + ")");
        }
        System.out.println("\nUpdates:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages2[1][j] / 1000000.0 + ")");
        }
        System.out.println();
        System.out.println("\nDeletes:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages2[2][j] / 1000000.0 + ")");
        }
        System.out.println("\nSelects:");
        for (int j = 0; j < records.length; j++) {
            System.out.print("(" + records[j] + "," + averages2[3][j] / 1000000.0 + ")");
        }
        System.out.println();

        statement.close();
        conn.close();
    }
}

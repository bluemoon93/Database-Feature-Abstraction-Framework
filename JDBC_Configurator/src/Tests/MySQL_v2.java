package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Local;
import R4N.FT.FaultTolerance;
import R4N.FT.FaultTolerance_Noop;
import R4N.DFM.R4N_CallableStatement;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL_v2 {

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

    public static void main(String[] args) throws ClassNotFoundException, InterruptedException, Exception {
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

                for (int k = 0; k < howManyTests; k++) {
                    for (int i = 0; i < records.length; i++) {
                        
                        
                        t = System.nanoTime();
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            statement.executeUpdate("INSERT INTO Customers"
                                    + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                                    + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
                        }
                        conn.commit();
                        conn.setAutoCommit(true);
                        t2 = System.nanoTime();
                        times[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            statement.executeUpdate("update Customers "
                                    + "set City='LAX', Region='Californication' "
                                    + "where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        conn.commit();
                        conn.setAutoCommit(true);
                        t2 = System.nanoTime();
                        times2[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            statement.executeQuery("select * from Customers "
                                    + "where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        conn.commit();
                        conn.setAutoCommit(true);
                        t2 = System.nanoTime();
                        times4[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            statement.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        conn.commit();
                        conn.setAutoCommit(true);
                        t2 = System.nanoTime();
                        times3[k][i] = t2 - t;
                        System.out.println(".");
                    }
                }
                long[][] averages3 = new long[4][records.length];
                for (int i = 0; i < howManyTests; i++) {
                    for (int j = 0; j < records.length; j++) {
                        averages3[0][j] += times[i][j];
                        averages3[1][j] += times2[i][j];
                        averages3[2][j] += times3[i][j];
                        averages3[3][j] += times4[i][j];
                    }
                }
                for (int j = 0; j < records.length; j++) {
                    averages3[0][j] /= howManyTests;
                    averages3[1][j] /= howManyTests;
                    averages3[2][j] /= howManyTests;
                    averages3[3][j] /= howManyTests;
                    //System.out.println("For " + records[j] + " records inserted, avg was " + averages[0][j] / 1000000.0 + " ms");
                    //System.out.println("For " + records[j] + " records updated,  avg was " + averages[1][j] / 1000000.0 + " ms");
                    //System.out.println("For " + records[j] + " records deleted,  avg was " + averages[2][j] / 1000000.0 + " ms");
                    //System.out.println("For " + records[j] + " records queried,  avg was " + averages[3][j] / 1000000.0 + " ms");
                }
                System.out.println("\nInserts:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages3[0][j] / 1000000.0 + ")");
                }
                System.out.println("\nUpdates:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages3[1][j] / 1000000.0 + ")");
                }
                System.out.println();
                System.out.println("\nDeletes:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages3[2][j] / 1000000.0 + ")");
                }
                System.out.println("\nSelects:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages3[3][j] / 1000000.0 + ")");
                }
                System.out.println();


                //------------------------
                for (int k = 0; k < howManyTests; k++) {
                    for (int i = 0; i < records.length; i++) {
                        t = System.nanoTime();
                        R4N_Transaction trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
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
                        trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
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
                        trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
                        for (int j = 0; j < records[i]; j++) {
                            trans.executeQuery("select * from Customers "
                                    + "where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        trans.close();
                        t2 = System.nanoTime();
                        times4[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
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
                
                //------------------------
                for (int k = 0; k < howManyTests; k++) {
                    for (int i = 0; i < records.length; i++) {
                        t = System.nanoTime();
                        R4N_Transaction trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            trans.executeUpdate("INSERT INTO Customers"
                                    + "(CustomerID, CompanyName, ContactName, ContactTitle, Address, City, Region, PostalCode, Country, Phone, Fax) "
                                    + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
                        }
                        conn.setAutoCommit(true);
                        trans.close();
                        t2 = System.nanoTime();
                        times[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            trans.executeUpdate("update Customers "
                                    + "set City='LAX', Region='Californication' "
                                    + "where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        conn.setAutoCommit(true);
                        trans.close();
                        t2 = System.nanoTime();
                        times2[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            trans.executeQuery("select * from Customers "
                                    + "where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        conn.setAutoCommit(true);
                        trans.close();
                        t2 = System.nanoTime();
                        times4[k][i] = t2 - t;
                        System.out.print(".");

                        t = System.nanoTime();
                        trans = new R4N_TransactionSQL(conn, "mysql", ftm,ch);
                        conn.setAutoCommit(false);
                        for (int j = 0; j < records[i]; j++) {
                            trans.executeUpdate("DELETE FROM Customers where PostalCode='" + i + "' and Fax='" + j + "'");
                        }
                        conn.setAutoCommit(true);
                        trans.close();
                        t2 = System.nanoTime();
                        times3[k][i] = t2 - t;
                        System.out.println(".");
                    }
                }
                long[][] averages4 = new long[4][records.length];
                for (int i = 0; i < howManyTests; i++) {
                    for (int j = 0; j < records.length; j++) {
                        averages4[0][j] += times[i][j];
                        averages4[1][j] += times2[i][j];
                        averages4[2][j] += times3[i][j];
                        averages4[3][j] += times4[i][j];
                    }
                }
                for (int j = 0; j < records.length; j++) {
                    averages4[0][j] /= howManyTests;
                    averages4[1][j] /= howManyTests;
                    averages4[2][j] /= howManyTests;
                    averages4[3][j] /= howManyTests;
                    //System.out.println("For " + records[j] + " records inserted, avg was " + averages2[0][j] / 1000000.0 + " ms");
                    //System.out.println("For " + records[j] + " records updated,  avg was " + averages2[1][j] / 1000000.0 + " ms");
                    //System.out.println("For " + records[j] + " records deleted,  avg was " + averages2[2][j] / 1000000.0 + " ms");
                    //System.out.println("For " + records[j] + " records queried,  avg was " + averages2[3][j] / 1000000.0 + " ms");
                }
                System.out.println("\nInserts:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages4[0][j] / 1000000.0 + ")");
                }
                System.out.println("\nUpdates:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages4[1][j] / 1000000.0 + ")");
                }
                System.out.println();
                System.out.println("\nDeletes:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages4[2][j] / 1000000.0 + ")");
                }
                System.out.println("\nSelects:");
                for (int j = 0; j < records.length; j++) {
                    System.out.print("(" + records[j] + "," + averages4[3][j] / 1000000.0 + ")");
                }
                System.out.println();
            }
        } catch (SQLException e) {
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

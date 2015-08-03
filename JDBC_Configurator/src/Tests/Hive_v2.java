package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FaultTolerance_Noop;
import R4N.CH.GraphClient_Local;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionHive;
import R4N.FT.FTHandler;
import java.sql.*;

public class Hive_v2 {

    static String JDBCDriverCloudera = "com.cloudera.hive.jdbc4.HS2Driver";
    static String JDBCDriver = "org.apache.hive.jdbc.HiveDriver";
    static String ConnectionURL = "jdbc:hive2://localhost:10000";

    public static void printRS(ResultSet rs) throws SQLException {
        while (rs.next()) {
            System.out.println("(" + rs.getObject(1) + ", " + rs.getObject(2) + ", " + rs.getObject(3) + ")");
        }
        System.out.println("Done.\n");
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            Class.forName(JDBCDriver);
            conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
            statement = conn.createStatement();

            statement.executeUpdate("drop table Customers");
            statement.executeUpdate("create table Customers (CustomerID string, CompanyName string, ContactName string, ContactTitle string, Address string, "
                    + "City string, Region string, PostalCode string, Country string, Phone string, Fax string) clustered by (CustomerID) into 2 buckets stored as orc TBLPROPERTIES('transactional'='true')");

            statement.executeUpdate("INSERT INTO table Customers "
                    + "VALUES ('GREAL', 'GreatLakesFM', 'Howard', 'Manager', "
                    + "'BakerBoulevard', 'LAX', 'California', '12345', 'USA', '555-2234', '2-234'),"
                    + "('JORGE', 'Broomy', 'John', 'Manager', "
                    + "'BakerBoulevard', 'LAX', 'California', '12345', 'USA', '555-2234', '2-234'),"
                    + "('JORGE', 'Broomy', 'Johny', 'Manager', "
                    + "'BakerBoulevard', 'NY', 'California', '12345', 'USA', '555-2234', '2-234')");

            FTHandler ftm = new FTHandler(new FaultTolerance_Noop());
            ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());
            int howManyTests = 5;
            int[] records = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
            long[][] times = new long[howManyTests][records.length], times2 = new long[howManyTests][records.length],
                    times3 = new long[howManyTests][records.length], times4 = new long[howManyTests][records.length];
            long t, t2;

            //------------------------
            for (int k = 0; k < howManyTests; k++) {
                for (int i = 0; i < records.length; i++) {
                    String omg = "insert into table Customers values ";
                    for (int j = 0; j < records[i]; j++) {
                        omg += "('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "'), ";
                    }
                    omg = omg.substring(0, omg.length() - 2);

                    t = System.nanoTime();
                    R4N_Transaction trans = new R4N_TransactionHive(conn, "hive", ftm, ch);
                    trans.executeUpdate(omg);
                    trans.close();
                    t2 = System.nanoTime();
                    times[k][i] = t2 - t;
                    System.out.print(".");// + times[k][i] / 1000000000.0 + ".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();

                    t = System.nanoTime();
                    trans = new R4N_TransactionHive(conn, "hive", ftm, ch);
                    trans.executeUpdate("update customers "
                            + "set city='NY', region='Californication' "
                            + "where city='LA'");
//                    for (int j = 0; j < records[i]; j++) {
//                        trans.executeUpdate("update customers "
//                                + "set city='LAX', region='Californication' "
//                                + "where postalcode='" + i + "' and fax='" + j + "'");
//                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times2[k][i] = t2 - t;
                    System.out.print(".");// + times2[k][i] / 1000000000.0 + ".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();

                    t = System.nanoTime();
                    trans = new R4N_TransactionHive(conn, "hive", ftm, ch);
                    trans.executeQuery("select * from customers where city='NY'");
                    t2 = System.nanoTime();
                    times4[k][i] = t2 - t;
                    System.out.print(".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();

                    t = System.nanoTime();
                    trans = new R4N_TransactionHive(conn, "hive", ftm, ch);
                    trans.executeUpdate("DELETE FROM Customers where city='NY'");
//                    for (int j = 0; j < records[i]; j++) {
//                        trans.executeUpdate("DELETE FROM Customers where postalcode='" + i + "' and fax='" + j + "'");
//                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times3[k][i] = t2 - t;
                    System.out.print(".");// + times3[k][i] / 1000000000.0 + ".");
                }
                System.out.println(";");
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
//                System.out.println("For " + records[j] + " records inserted, avg was " + averages2[0][j] / 1000000.0 + " ms");
//                System.out.println("For " + records[j] + " records updated,  avg was " + averages2[1][j] / 1000000.0 + " ms");
//                System.out.println("For " + records[j] + " records deleted,  avg was " + averages2[2][j] / 1000000.0 + " ms");
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
            
            
            for (int k = 0; k < howManyTests; k++) {
                for (int i = 0; i < records.length; i++) {
                    String omg = "insert into table Customers values ";
                    for (int j = 0; j < records[i]; j++) {
                        omg += "('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "'), ";
                    }
                    omg = omg.substring(0, omg.length() - 2);

                    t = System.nanoTime();
                    statement.executeUpdate(omg);
//                    for (int j = 0; j < records[i]; j++) {
//                        statement.executeUpdate("insert into table Customers "
//                                + "VALUES ('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "')");
//                    }
                    t2 = System.nanoTime();
                    times[k][i] = t2 - t;
                    System.out.print(".");// + times[k][i] / 1000000000.0 + ".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();

                    t = System.nanoTime();
//                    for (int j = 0; j < records[i]; j++) {
//                        statement.executeUpdate("update customers "
//                                + "set city='LAX', region='Californication' "
//                                + "where postalcode='" + i + "' and fax='" + j + "'");
//                    }
                    statement.executeUpdate("update customers "
                            + "set city='NY', region='Californication' "
                            + "where city='LA'");
                    t2 = System.nanoTime();
                    times2[k][i] = t2 - t;
                    System.out.print(".");// + times2[k][i] / 1000000000.0 + ".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();

                    t = System.nanoTime();
                    statement.executeQuery("select * from customers where city='NY'");
                    t2 = System.nanoTime();
                    times4[k][i] = t2 - t;
                    System.out.print(".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();

                    t = System.nanoTime();
                    statement.executeUpdate("DELETE FROM Customers where city='NY'");
//                    for (int j = 0; j < records[i]; j++) {
//                        statement.executeUpdate("DELETE FROM Customers where postalcode='" + i + "' and fax='" + j + "'");
//                    }
                    t2 = System.nanoTime();
                    times3[k][i] = t2 - t;
                    System.out.print(".");//) + times3[k][i] / 1000000000.0 + ".");

                    conn.close();
                    conn = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
                    statement = conn.createStatement();
                }
                System.out.println(";");
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
                System.out.println("For " + records[j] + " records inserted, avg was " + averages[0][j] / 1000000000.0 + " s");
                System.out.println("For " + records[j] + " records updated,  avg was " + averages[1][j] / 1000000000.0 + " s");
                System.out.println("For " + records[j] + " records selected,  avg was " + averages[3][j] / 1000000000.0 + " s");
                System.out.println("For " + records[j] + " records deleted,  avg was " + averages[2][j] / 1000000000.0 + " s");
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
                    
            

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se1) {
                System.out.println("Couldnt close result set");
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException se2) {
                System.out.println("Couldnt close statement");
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se3) {
                se3.printStackTrace();
            }
        } // End try
    } // End main

}

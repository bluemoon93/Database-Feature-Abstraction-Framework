package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FaultTolerance;
import R4N.FT.FaultTolerance_Noop;
import R4N.CH.GraphClient_Local;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionHive;
import R4N.FT.FTHandler;
import java.sql.*;

public class Hive {

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
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            Class.forName(JDBCDriver);
            connection = DriverManager.getConnection(ConnectionURL, "bluemoon", "1234lol");
            statement = connection.createStatement();

            statement.executeUpdate("drop table Customers");
            statement.executeUpdate("create table Customers (CustomerID string, CompanyName string, ContactName string, ContactTitle string, Address string, "
                    + "City string, Region string, PostalCode string, Country string, Phone string, Fax string) clustered by (CustomerID) into 2 buckets stored as orc TBLPROPERTIES('transactional'='true')");

            statement.executeUpdate("INSERT INTO table Customers "
                    + "VALUES ('GREAL', 'GreatLakesFM', 'Howard', 'Manager', "
                    + "'BakerBoulevard', 'LA', 'California', '12345', 'USA', '555-2234', '2-234'),"
                    + "('JORGE', 'Broomy', 'John', 'Manager', "
                    + "'BakerBoulevard', 'LA', 'California', '12345', 'USA', '555-2234', '2-234'),"
                    + "('JORGE', 'Broomy', 'Johny', 'Manager', "
                    + "'BakerBoulevard', 'NY', 'California', '12345', 'USA', '555-2234', '2-234')");

            /*R4N_Transaction trans2 = new R4N_TransactionHive(connection, "sqlite");
             printRS(trans2.executeQuery("select * from Customers"));
             trans2.executeUpdate("update customers set customerid='GROPE', companyname='NewComp' where region='California' and city='LA'");
             System.out.println("Executing: update customers set customerid='GROPE', companyname='NewComp' where region='California' and city='LA'");
             printRS(trans2.executeQuery("select * from Customers"));
             trans2.rollback();
             printRS(trans2.executeQuery("select * from Customers"));
             trans2.close();*/
            /*R4N_Transaction trans2 = new R4N_TransactionHive(connection, "hive");
             printRS(trans2.executeQuery("select * from Customers"));
             trans2.executeUpdate("delete from customers where region='California' and city='LA'");
             System.out.println("Executing: delete from customers where region='California' and city='LA'");
             printRS(trans2.executeQuery("select * from Customers"));
             trans2.rollback();
             printRS(trans2.executeQuery("select * from Customers"));
             trans2.close();*/
            int runs = 11, skips = runs / 10;
            long[] times = new long[11];
            long t, t2;

            /*for (int i = 1; i <= runs; i += skips) {
                t = System.currentTimeMillis();
               
                for (int j = 0; j < i; j++) {
                    statement.executeUpdate("INSERT INTO table Customers VALUES "
                            + "('GREPL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', "
                            + "'" + i + "', 'USA', '555-2234', '" + j + "')");
                }
               
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }

            for (int i = 1; i <= runs; i += skips) {
                t = System.currentTimeMillis();
                
                for (int j = 0; j < i; j++) {
                    statement.executeUpdate("update customers set city='LAX', region='Californication' where postalcode='" + i + "' and fax='" + j + "'");
                }
                
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }

            for (int i = 1; i <= runs; i += skips) {
                t = System.currentTimeMillis();
                
                for (int j = 0; j < i; j++) {
                    statement.executeUpdate("DELETE FROM Customers where postalcode='" + i + "' and fax='" + j + "'");
                }
                
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }
           */
            System.out.println("and now with r4n");
            FTHandler ftm = new FTHandler(new FaultTolerance_Noop());
            ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());
            for (int i = 1; i <= runs; i += skips) {
                t = System.currentTimeMillis();
                R4N_Transaction trans = new R4N_TransactionHive(connection, "hive", ftm, ch);
                for (int j = 0; j < i; j++) {
                    trans.executeUpdate("INSERT INTO table Customers VALUES "
                            + "('GREPL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', "
                            + "'" + i + "', 'USA', '555-2234', '" + j + "')");
                }
                trans.close();
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }
            
            for (int i = 1; i <= runs; i += skips) {
                t = System.currentTimeMillis();
                R4N_Transaction trans = new R4N_TransactionHive(connection, "hive", ftm, ch);
                for (int j = 0; j < i; j++) {
                    trans.executeUpdate("update customers set city='LAX', region='Californication' where postalcode='" + i + "' and fax='" + j + "'");
                }
                trans.close();
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }

            for (int i = 1; i <= runs; i += skips) {
                t = System.currentTimeMillis();
                R4N_Transaction trans = new R4N_TransactionHive(connection, "hive", ftm, ch);
                for (int j = 0; j < i; j++) {
                    trans.executeUpdate("DELETE FROM Customers where postalcode='" + i + "' and fax='" + j + "'");
                }
                trans.close();
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }
            System.out.println("as a side note...");
      
            for (int i = 1; i <= runs; i += skips) {
                String stuff = "INSERT INTO table Customers VALUES ";
                for (int j = 0; j < i - 1; j++) {
                    stuff += "('GREPL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "'), ";
                }
                stuff += "('GREPL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + (i - 1) + "')";
                //System.out.println(stuff);

                t = System.currentTimeMillis();
                statement.executeUpdate(stuff);
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }
            System.out.println("and now with r4n");
            for (int i = 1; i <= runs; i += skips) {
                String stuff = "INSERT INTO table Customers VALUES ";
                for (int j = 0; j < i - 1; j++) {
                    stuff += "('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + j + "'), ";
                }
                stuff += "('GREAL', 'GreatLakesFM', 'Holly', 'Manager', 'BakerBoulevard', 'LA', 'California', '" + i + "', 'USA', '555-2234', '" + (i - 1) + "')";
                //System.out.println(stuff);

                t = System.currentTimeMillis();
                R4N_Transaction trans = new R4N_TransactionHive(connection, "hive", ftm, ch);
                trans.executeUpdate(stuff);
                trans.close();
                t2 = System.currentTimeMillis();
                times[(i - 1) / skips] = t2 - t;
            }
            for (int i = 0; i <= 10; i += 1) {
                System.out.println(times[i] / 1000.0 + " s for " + (i * skips + 1) + " records (" + (times[i] / 1000.0 / (i * (double) skips + 1)) + " s/rec)");
            }

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
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException se3) {
                se3.printStackTrace();
            }
        } // End try
    } // End main

}

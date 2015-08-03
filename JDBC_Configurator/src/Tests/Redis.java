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
import R4N.DFM.R4N_TransactionRedis;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bluemoon
 */
public class Redis {

    public static void main(String[] a) throws Exception {

        try {
            Class.forName("RedisJDBC.HBDriver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        Connection conn;
        Statement st;
        try {
            conn = DriverManager.getConnection("jdbc:HBDriver:localhost");
            st = conn.createStatement();

            int runs = 1001, skips = runs / 10;

            for (int i = 1; i <= runs; i += skips) {
                for (int j = 0; j < i; j++) {
                    st.executeUpdate("del Customers:" + i + "-" + j);
                    st.executeUpdate("del Customers:" + i + "+" + j);

                }
            }
            st.executeUpdate("del Customers");
            st.executeUpdate("set Customers:a "
                    + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                    + "City LA Region California PostalCode USA Country 555-2234 Phone 123 Fax 45345");

            long[] times = new long[11], times2 = new long[11], times3 = new long[11];
            long t, t2;

            for (int i = 1; i <= runs; i += skips) {
                t = System.nanoTime();

                for (int j = 0; j < i; j++) {
                    st.executeUpdate("set Customers:" + i + "-" + j + " "
                            + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                            + "City LA Region California PostalCode USA Country 555-2234 Phone 123 Fax 45345");
                }
                t2 = System.nanoTime();
                times[i / skips] = t2 - t;
                System.out.print(".");

                t = System.nanoTime();

                for (int j = 0; j < i; j++) {
                    st.executeUpdate("set Customers:" + i + "-" + j + " "
                            + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                            + "City LAX Region Californication PostalCode USA Country 555-2234 Phone 123 Fax 45345");
                }
                t2 = System.nanoTime();
                times2[i / skips] = t2 - t;

                t = System.nanoTime();

                for (int j = 0; j < i; j++) {
                    st.executeUpdate("del Customers:" + i + "+" + j);
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
                R4N_Transaction trans = new R4N_TransactionRedis(conn, "redis", ftm,ch);
                for (int j = 0; j < i; j++) {
                    trans.executeUpdate("set Customers:" + i + "+" + j + " "
                            + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                            + "City LA Region California PostalCode USA Country 555-2234 Phone 123 Fax 45345");
                }
                trans.close();
                t2 = System.nanoTime();
                times[i / skips] = t2 - t;

                t = System.nanoTime();
                trans = new R4N_TransactionRedis(conn, "redis", ftm,ch);
                for (int j = 0; j < i; j++) {
                    trans.executeUpdate("set Customers:" + i + "+" + j + " "
                            + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                            + "City LAX Region Californication PostalCode USA Country 555-2234 Phone 123 Fax 45345");
                }
                trans.close();
                t2 = System.nanoTime();
                times2[i / skips] = t2 - t;
                System.out.print(".");

                t = System.nanoTime();
                trans = new R4N_TransactionRedis(conn, "redis", ftm,ch);
                for (int j = 0; j < i; j++) {
                    trans.executeUpdate("del Customers:" + i + "+" + j);
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

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void printRS(ResultSet rs) throws SQLException {
        ResultSetMetaData rsm = rs.getMetaData();
        while (rs.next()) {
            for (int i = 1; i <= rsm.getColumnCount(); i++) {
                System.out.print(rs.getObject(i) + " ");
            }
        }
        System.out.println();
    }
}

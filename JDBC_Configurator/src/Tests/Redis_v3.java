/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Local;
import R4N.CH.GraphClient_Sock;
import R4N.FT.FaultTolerance_Noop;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionRedis;
import R4N.DFM.R4N_TransactionRedis1;
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
public class Redis_v3 {

    public static void main(String[] a) throws Exception {

        try {
            Class.forName("RedisJDBC.HBDriver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        Connection conn;
        Statement statement;
        try {
            conn = DriverManager.getConnection("jdbc:HBDriver:localhost");
            statement = conn.createStatement();

            int runs = 1001, skips = runs / 10;

            for (int i = 1; i <= runs; i += skips) {
                for (int j = 0; j < i; j++) {
                    statement.executeUpdate("del Customers:" + i + "-" + j);
                    statement.executeUpdate("del Customers:" + i + "+" + j);

                }
            }
            statement.executeUpdate("del Customers");
            statement.executeUpdate("set Customers:a "
                    + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                    + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");

            FTHandler ftm = new FTHandler(new FaultTolerance_Noop());
            ConcurrencyHandler ch = new ConcurrencyHandler(new GraphClient_Local());
            int howManyTests = 5;
            int[] records = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
            long[][] averages = new long[4][records.length];
            long[][] times = new long[howManyTests][records.length], times2 = new long[howManyTests][records.length],
                    times3 = new long[howManyTests][records.length], times4 = new long[howManyTests][records.length];
            long t, t2;
            /*
             for (int k = 0; k < howManyTests; k++) {
             for (int i = 0; i < records.length; i++) {
             t = System.nanoTime();
             R4N_Transaction trans = new R4N_TransactionRedis(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("set Customers:" + i + "+" + j + " "
             + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
             + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("set Customers:" + i + "+" + j + " "
             + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
             + "City LAX Region Californication PostalCode 555-2234 Country USA Phone 123 Fax 45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times2[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeQuery("get Customers:" + i + "+" + j + " where CustomerID=GREAL CompanyName=GreatLakesFM "
             + "ContactName=Holly ContactTitle=Manager Address=BakerBoulevard City=LAX "
             + "Region=Californication PostalCode=555-2234 Country=USA Phone=123 Fax=45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times4[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("del Customers:" + i + "+" + j);
             }
             trans.close();
             t2 = System.nanoTime();
             times3[k][i] = t2 - t;
             System.out.println(".");
             }
             }

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
             R4N_Transaction trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("set Customers:" + i + "+" + j + " "
             + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
             + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("set Customers:" + i + "+" + j + " "
             + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
             + "City LAX Region Californication PostalCode 555-2234 Country USA Phone 123 Fax 45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times2[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeQuery("get Customers:" + i + "+" + j + " where CustomerID=GREAL CompanyName=GreatLakesFM "
             + "ContactName=Holly ContactTitle=Manager Address=BakerBoulevard City=LAX "
             + "Region=Californication PostalCode=555-2234 Country=USA Phone=123 Fax=45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times4[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("del Customers:" + i + "+" + j);
             }
             trans.close();
             t2 = System.nanoTime();
             times3[k][i] = t2 - t;
             System.out.println(".");
             }
             }

             for (int i = 0; i < howManyTests; i++) {
             for (int j = 0; j < records.length; j++) {
             averages[0][j] = 0;
             averages[1][j] = 0;
             averages[2][j] = 0;
             averages[3][j] = 0;
             }
             }
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
             R4N_Transaction trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("set Customers:" + i + "+" + j + " "
             + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
             + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("set Customers:" + i + "+" + j + " "
             + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
             + "City LAX Region Californication PostalCode 555-2234 Country USA Phone 123 Fax 45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times2[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeQuery("get Customers:" + i + "+" + j + " where CustomerID=GREAL CompanyName=GreatLakesFM "
             + "ContactName=Holly ContactTitle=Manager Address=BakerBoulevard City=LAX "
             + "Region=Californication PostalCode=555-2234 Country=USA Phone=123 Fax=45345");
             }
             trans.close();
             t2 = System.nanoTime();
             times4[k][i] = t2 - t;
             System.out.print(".");

             t = System.nanoTime();
             trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
             for (int j = 0; j < records[i]; j++) {
             trans.executeUpdate("del Customers:" + i + "+" + j);
             }
             trans.close();
             t2 = System.nanoTime();
             times3[k][i] = t2 - t;
             System.out.println(".");
             }
             }
             for (int i = 0; i < howManyTests; i++) {
             for (int j = 0; j < records.length; j++) {
             averages[0][j] = 0;
             averages[1][j] = 0;
             averages[2][j] = 0;
             averages[3][j] = 0;
             }
             }
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
             */
            ch = new ConcurrencyHandler(new GraphClient_Sock("localhost", 5922)); //192.168.1.68
            System.out.println("Same thing, Sock GC");

            for (int k = 0; k < howManyTests; k++) {
                for (int i = 0; i < records.length; i++) {
                    t = System.nanoTime();
                    R4N_Transaction trans = new R4N_TransactionRedis(conn, "redis", ftm, ch);
                    for (int j = 0; j < records[i]; j++) {
                        trans.executeUpdate("set Customers:" + i + "+" + j + " "
                                + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                                + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");
                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times[k][i] = t2 - t;
                    System.out.print(".");

                    for (int j = 0; j < records[i]; j++) {
                        statement.executeUpdate("del Customers:" + i + "+" + j);
                    }
                }
            }

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
                    R4N_Transaction trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
                    for (int j = 0; j < records[i]; j++) {
                        trans.executeUpdate("set Customers:" + i + "+" + j + " "
                                + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                                + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");
                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times[k][i] = t2 - t;
                    System.out.print(".");
                    
                    for (int j = 0; j < records[i]; j++) {
                        statement.executeUpdate("del Customers:" + i + "+" + j);
                    }
                }
            }

            for (int i = 0; i < howManyTests; i++) {
                for (int j = 0; j < records.length; j++) {
                    averages[0][j] = 0;
                    averages[1][j] = 0;
                    averages[2][j] = 0;
                    averages[3][j] = 0;
                }
            }
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
                    R4N_Transaction trans = new R4N_TransactionRedis1(conn, "redis", ftm, ch);
                    for (int j = 0; j < records[i]; j++) {
                        trans.executeUpdate("set Customers:" + i + "+" + j + " "
                                + "CustomerID GREAL CompanyName GreatLakesFM ContactName Holly ContactTitle Manager Address BakerBoulevard "
                                + "City LA Region California PostalCode 555-2234 Country USA Phone 123 Fax 45345");
                    }
                    trans.close();
                    t2 = System.nanoTime();
                    times[k][i] = t2 - t;
                    System.out.print(".");
                    
                    for (int j = 0; j < records[i]; j++) {
                        statement.executeUpdate("del Customers:" + i + "+" + j);
                    }
                }
            }
            for (int i = 0; i < howManyTests; i++) {
                for (int j = 0; j < records.length; j++) {
                    averages[0][j] = 0;
                    averages[1][j] = 0;
                    averages[2][j] = 0;
                    averages[3][j] = 0;
                }
            }
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

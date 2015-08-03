/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Configs;

import Features.SPHive_insertRandomOrder;
import Features.SPSQL_insertRandomOrder;
import R4N.CH.ConcurrencyHandler;
import R4N.CH.GraphClient_Local;
import R4N.CH.GraphClient_Sock;
import R4N.DFM.R4N_CallableStatement;
import R4N.DFM.R4N_ResultSetHive;
import R4N.DFM.R4N_ResultSetRedis;
import R4N.DFM.R4N_ResultSetSQL;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionHive;
import R4N.DFM.R4N_TransactionRedis;
import R4N.DFM.R4N_TransactionSQL;
import R4N.FT.FTHandler;
import R4N.FT.FaultTolerance_Disk;
import R4N.FT.FaultTolerance_Noop;
import R4N.FT.FaultTolerance_Sock;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author bluemoon
 */
public class Reader {

    private static boolean useR4NTransactions, useR4NIAMInteractions, useR4NSP, useR4NColIndexes;
    private static boolean sqlserver, sqlite, hive, redis, mongo;
    private static Connection conn, psConn;
    private static SQLServerDataSource policyServer;
    private static R4N_CallableStatement[] StoredProcedures;
    private static FTHandler ft;
    private static ConcurrencyHandler ch;

    public static void processConfigs() throws Exception {
        String connType = null, url = null, forName = null, user = null, pw = null, dbName = null, urlPS = null, 
                dbNamePS = null, userPS = null, pwPS = null, ft2 = null, ftLoc = null, ch2 = null, chLoc = null;
        try (Scanner fin = new Scanner(new File("config"))) {
            String line;
            while (fin.hasNextLine()) {
                line = fin.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split("=");
                switch (fields[0]) {
                    case "dbType":
                        switch (fields[1]) {
                            case "Hive":
                                hive = true;
                                break;
                            case "SQLServer":
                                sqlserver = true;
                                break;
                            case "SQlite":
                                sqlite = true;
                                break;
                            case "Redis":
                                redis = true;
                                break;
                            case "Mongo":
                                mongo = true;
                                break;
                            default:
                                throw new ClassNotFoundException("Unknown db type!");
                        }
                        break;
                    case "connectionType":
                        connType = fields[1];
                        break;
                    case "url":
                        url = fields[1];
                        break;
                    case "classForName":
                        forName = fields[1];
                        break;
                    case "user":
                        user = fields[1];
                        break;
                    case "password":
                        pw = fields[1];
                        break;
                    case "dbName":
                        dbName = fields[1];
                        break;
                    case "urlPS":
                        urlPS = fields[1];
                        break;
                    case "dbNamePS":
                        dbNamePS = fields[1];
                        break;
                    case "userPS":
                        userPS = fields[1];
                        break;
                    case "passwordPS":
                        pwPS = fields[1];
                        break;
                    case "r4nTrans":
                        useR4NTransactions = Boolean.parseBoolean(fields[1]);
                        break;
                    case "r4nIAM":
                        useR4NIAMInteractions = Boolean.parseBoolean(fields[1]);
                        break;
                    case "r4nSP":
                        useR4NSP = Boolean.parseBoolean(fields[1]);
                        break;
                    case "r4nColI":
                        useR4NColIndexes = Boolean.parseBoolean(fields[1]);
                        break;
                    case "ft":
                        ft2 = fields[1];
                        break;
                    case "ftLocation":
                        ftLoc = fields[1];
                        break;
                    case "ch":
                        ch2 = fields[1];
                        break;
                    case "chLocation":
                        chLoc = fields[1];
                        break;
                    default:
                        if (!fields[0].isEmpty()) {
                            System.out.println("Unknown config: " + fields[0]);
                        }
                        break;
                }
            }
        }
        
        switch (ft2) {
            case "NoOp":
                ft=new FTHandler(new FaultTolerance_Noop());
                break;
            case "FS":
                ft=new FTHandler(new FaultTolerance_Disk(ftLoc));
                break;
            case "Remote":
                String [] location = ftLoc.split(":");
                ft=new FTHandler(new FaultTolerance_Sock(location[0], Integer.parseInt(location[1])));
                break;
            default:
                throw new ClassNotFoundException("Unknown FT!");
        }
        
        switch (ch2) {
            case "Local":
                ch=new ConcurrencyHandler(new GraphClient_Local());
                break;
            case "Remote":
                String [] location = ftLoc.split(":");
                ch=new ConcurrencyHandler(new GraphClient_Sock(location[0], Integer.parseInt(location[1])));
                break;
            default:
                throw new ClassNotFoundException("Unknown FT!");
        }

        switch (connType) {
            case "SQLDataSource":
                SQLServerDataSource ds = new SQLServerDataSource();
                ds.setServerName(url);
                ds.setDatabaseName(dbName);
                ds.setUser(user);
                ds.setPassword(pw);
                conn = ds.getConnection();
                break;
            case "UrlAuth":
                Class.forName(forName);
                conn = DriverManager.getConnection(url, user, pw);
                break;
            case "Url":
                Class.forName(forName);
                conn = DriverManager.getConnection(url);
                break;
            default:
                throw new ClassNotFoundException("Unknown db!");
        }

        policyServer = new SQLServerDataSource();
        policyServer.setServerName(urlPS);
        policyServer.setDatabaseName(dbNamePS);
        policyServer.setUser(userPS);
        policyServer.setPassword(pwPS);
        psConn = policyServer.getConnection();

        if (useR4NSP) {
            StoredProcedures = new R4N_CallableStatement[2];
            StoredProcedures[0] = new SPHive_insertRandomOrder(conn);
            StoredProcedures[1] = new SPSQL_insertRandomOrder(conn);
        }
    }

    public static Connection getConn() {
        return conn;
    }

    public static Connection getPSConn() {
        return psConn;
    }

    public static Connection getPSConnNew() throws SQLServerException {
        return policyServer.getConnection();
    }

    public static ResultSet getRS(String query) throws SQLException {
        if (hive) {
            return new R4N_ResultSetHive(conn, query, ResultSet.TYPE_FORWARD_ONLY);
        } else if (sqlite || sqlserver || mongo) {
            return new R4N_ResultSetSQL(conn, query, ResultSet.TYPE_SCROLL_INSENSITIVE);
        } else if (redis) {
            return new R4N_ResultSetRedis(conn, query, ResultSet.TYPE_SCROLL_INSENSITIVE);
        }
        return null;
    }
    
    public static FTHandler getFTHandler() {
        return ft;
    }

    public static R4N_Transaction getTrans(String id) throws Exception {
        if (hive) {
            return new R4N_TransactionHive(conn, id, ft, ch);
        } else if (sqlserver || sqlite || mongo) {
            return new R4N_TransactionSQL(conn, id, ft, ch);
        } else if (redis) {
            return new R4N_TransactionRedis(conn, id, ft, ch);
        }
        return null;
    }

    public static boolean getTrans() {
        return useR4NTransactions;
    }

    public static boolean getIAM() {
        return useR4NIAMInteractions;
    }

    public static boolean getSP() {
        return useR4NSP;
    }

    public static boolean getColI() {
        return useR4NColIndexes;
    }

    public static R4N_CallableStatement[] getSPs() {
        return StoredProcedures;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RedisJDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

/**
 * The driver's client tier, which provides the standard JDBC interface to the
 * client programs, consists of a Driver class that implements the
 * java.sql.Driver interface. It also consists of the implementations of JDBC's
 * Connection, Statement and ResultSet interfaces.
 *
 * @author bluemoon
 */
public class HBDriver implements java.sql.Driver {

    //Remote driver
    //static IRemoteDriver remoteDriver = null;
    //Driver URL prefix.
    private static final String URL_PREFIX = "jdbc:HBDriver:";

    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;

    static {
        try {
            // Register the JWDriver with DriverManager
            HBDriver driverInst = new HBDriver();
            DriverManager.registerDriver(driverInst);
            //System.setSecurityManager(new RMISecurityManager());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (acceptsURL(url)) {
            Jedis jedis = new Jedis(url.substring(URL_PREFIX.length()));
            jedis.connect();
            return new HBConn(jedis);
        }
        return null;
    }

    public static String getURLPrefix() {
        return URL_PREFIX;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RedisJDBC;

import R4N.CH.ConcurrencyHandler;
import R4N.FT.FaultTolerance_Noop;
import R4N.CH.GraphClient_Local;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionRedis;
import R4N.FT.FTHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import redis.clients.jedis.Jedis;

/**
 *
 * @author bluemoon
 */
public class Redis {

    public static void main(String[] a) throws Exception {
        Jedis jedis = new Jedis("localhost");
        jedis.connect();
        jedis.set("foo", "bar");
        System.out.println(jedis.get("foo"));
        jedis.append("foo", "2");
        System.out.println(jedis.get("foo"));
        jedis.set("foo", "bar");
        System.out.println(jedis.get("foo"));
        //jedis.del("foo");
        System.out.println(jedis.get("foo"));

        jedis.del("list");
        jedis.rpush("list", "first");
        jedis.rpush("list", "second");
        jedis.rpush("list", "third");
        List<String> c = jedis.lrange("list", 0, -1);
        for (String b : c) {
            System.out.println(b);
        }
        //System.out.println(jedis.llen("foo"));

        //jedis.hmset(null, null);
        jedis.hset("user1", "name", "dave");
        jedis.hset("user1", "password", "1234lol");
        System.out.println(jedis.hget("user1", "name"));
        //System.out.println(jedis.get("user1"));
        //System.out.println(jedis.llen("user1"));
        System.out.println(jedis.hget("user3", "name"));
        System.out.println(jedis.get("user3"));
//        System.out.println(jedis.get("user1"));

        System.out.println("---------------------");
        try {
            Class.forName("RedisJDBC.HBDriver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        Connection conn;
        Statement st;
        ResultSet rs;
        try {
            conn = DriverManager.getConnection("jdbc:HBDriver:localhost");
            st = conn.createStatement();
            /*st.executeUpdate("del user");
             st.executeUpdate("del user:100");
             st.executeUpdate("del user:101");
             st.executeUpdate("set user:100 name Dave pw 1234lol age 25 id GREAL");
             st.executeUpdate("set user:101 name John pw lol1234 job IT id REDIT");
             st.executeUpdate("set user:100 job PR");

             printRS(st.executeQuery("get user:100"));
             printRS(st.executeQuery("get user:101"));
             printRS(st.executeQuery("get user:100 where id=GREAL"));
             printRS(st.executeQuery("get user:100 where id=lol"));
             */

            printRS(st.executeQuery("get orders:thisOrder"));
            printRS(st.executeQuery("get orders:thisOrder where CustomerID=GREAL ShipCountry=Country"));
            printRS(st.executeQuery("get users:usa"));
            st.executeUpdate("del orders:thisOrder");

            System.out.println("Transaction started! Output: null, Dave, null");
            R4N_Transaction trans = new R4N_TransactionRedis(conn, "redis", new FTHandler(new FaultTolerance_Noop()), new ConcurrencyHandler(new GraphClient_Local()));

            trans.close();
            /*
             st.executeUpdate("set user:100 name Dave pw 1234lol age 25 id GREAL");
             st.executeUpdate("set orders:thisOrder CustomerID GREAL EmployeeID 1 OrderDate 2014-02-02 RequiredDate 2014-04-02 ShippedDate 2014-06-02 ShipVia 1 Freight 2.0 ShipName Shippie ShipAddress Addr ShipCity Chity ShipRegion Reegion ShipPostalCode 2048-526 ShipCountry Country");
             st.executeUpdate("set users:usa CustomerID GREAL CompanyName GreatlakesFM ContactName Howard ContactTitle Manager Address BakerBlvrd City LA Region Cali PostalCode 12345 Country USA Phone 555-1234 Fax (2)555-1123");
          
             rs = new R4N_ResultSetRedis(conn, "get user:100", ResultSet.TYPE_SCROLL_SENSITIVE);
             rs.moveToInsertRow();
             rs.updateObject(0, "user:101");
             rs.updateObject("name", "Dave2");
             rs.updateObject("pw", "lol1234");
             rs.updateObject("job", "IT2");
             rs.updateObject("id", "LOLIS");
             rs.insertRow();
             rs.beforeFirst();
             rs.next();
             rs.updateObject("name", "Alice");
             rs.updateRow();
             rs.beforeFirst();
             printRS(rs);
            
             printRS(st.executeQuery("get user:100"));
             printRS(st.executeQuery("get user:101"));
             */

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void printRS(ResultSet rs) throws SQLException {
        ResultSetMetaData rsm = rs.getMetaData();
        System.out.println("printing: ");
        while (rs.next()) {
            for (int i = 1; i <= rsm.getColumnCount(); i++) {
                System.out.print(rs.getObject(i) + " ");
            }
            System.out.println();
        }

    }
}

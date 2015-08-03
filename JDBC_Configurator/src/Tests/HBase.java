/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

/**
 *
 * @author bluemoon
 */
import java.io.IOException;
/*
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
*/
// Class that has nothing but a main.
// Does a Put, Get and a Scan against an hbase table.
// The API described here is since HBase 1.0.
public class HBase {

    public static void main(String[] args) throws IOException {
        // You need a configuration object to tell the client where to connect.
        // When you create a HBaseConfiguration, it reads in whatever you've set
        // into your hbase-site.xml and in hbase-default.xml, as long as these can
        // be found on the CLASSPATH
       /* Configuration config = HBaseConfiguration.create();
 
        String hbaseZookeeperQuorum="192.168.56.101";
        String hbaseZookeeperClientPort="2181"; // 10000
        //String tableName="HBaseTableName";

        Configuration hConf = HBaseConfiguration.create();
        hConf.set(HConstants.ZOOKEEPER_QUORUM, hbaseZookeeperQuorum);
        hConf.set(HConstants.ZOOKEEPER_CLIENT_PORT, hbaseZookeeperClientPort);
        System.out.println(HConstants.ZOOKEEPER_QUORUM+" "+HConstants.ZOOKEEPER_CLIENT_PORT);
        //HTable hTable = new HTable(hConf, tableName);

        // Connections are heavyweight.  Create one once and keep it around. From a Connection
        // you get a Table instance to access Tables, an Admin instance to administer the cluster,
        // and RegionLocator to find where regions are out on the cluster. As opposed to Connections,
        // Table, Admin and RegionLocator instances are lightweight; create as you need them and then
        // close when done.
        System.out.println("Connecting...");
        try (Connection connection = ConnectionFactory.createConnection(config)) {
            System.out.println("Connected! About to get a table");
            // The below instantiates a Table object that connects you to the "myLittleHBaseTable" table
            // (TableName.valueOf turns String into a TableName instance).
            // When done with it, close it (Should start a try/finally after this creation so it gets
            // closed for sure the jdk7 idiom, try-with-resources: see
            // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
            Table table = connection.getTable(TableName.valueOf("analytics_demo"));
            try {
                System.out.println("Got the table");
                // To add to a row, use Put.  A Put constructor takes the name of the row
                // you want to insert into as a byte array.  In HBase, the Bytes class has
                // utility for converting all kinds of java types to byte arrays.  In the
                // below, we are converting the String "myLittleRow" into a byte array to
                // use as a row key for our update. Once you have a Put instance, you can
                // adorn it by setting the names of columns you want to update on the row,
                // the timestamp to use in your update, etc. If no timestamp, the server
                // applies current time to the edits.
                //Put p = new Put(Bytes.toBytes("myLittleRow"));

                // To set the value you'd like to update in the row 'myLittleRow', specify
                // the column family, column qualifier, and value of the table cell you'd
                // like to update.  The column family must already exist in your table
                // schema.  The qualifier can be anything.  All must be specified as byte
                // arrays as hbase is all about byte arrays.  Lets pretend the table
                // 'myLittleHBaseTable' was created with a family 'myLittleFamily'.
                //p.add(Bytes.toBytes("myLittleFamily"), Bytes.toBytes("someQualifier"), Bytes.toBytes("Some Value"));

                // Once you've adorned your Put instance with all the updates you want to
                // make, to commit it do the following (The HTable#put method takes the
                // Put instance you've been building and pushes the changes you made into
                // hbase)
                //table.put(p);
                //System.out.println("Inserted row");
                // Now, to retrieve the data we just wrote. The values that come back are
                // Result instances. Generally, a Result is an object that will package up
                // the hbase return into the form you find most palatable.
                //Get g = new Get(Bytes.toBytes("domain.0"));
               // Result r = table.get(g);
                //byte[] value = r.getValue(Bytes.toBytes("myLittleFamily"), Bytes.toBytes("someQualifier"));

                // If we convert the value bytes, we should get back 'Some Value', the
                // value we inserted at this location.
               // String valueStr = Bytes.toString(value);
                //System.out.println("GET: " + valueStr);

                // Sometimes, you won't know the row you're looking for. In this case, you
                // use a Scanner. This will give you cursor-like interface to the contents
                // of the table.  To set up a Scanner, do like you did above making a Put
                // and a Get, create a Scan.  Adorn it with column names, etc.
                Scan s = new Scan();
                s.addColumn(Bytes.toBytes("day"), Bytes.toBytes("someQualifier"));
                ResultScanner scanner = table.getScanner(s);
                System.out.println("Scanned table");
                try {
                    for (Result rr : scanner) {
                       System.out.println("Found row: " + rr);
                    }
                } finally {
                    scanner.close();
                }

                // Close your table and cluster connection.
            } finally {
                if (table != null) {
                    table.close();
                }
            }
        }*/
    }
}

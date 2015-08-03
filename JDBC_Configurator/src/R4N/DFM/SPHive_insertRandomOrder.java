/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.DFM;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 *
 * @author bluemoon
 */
public class SPHive_insertRandomOrder extends R4N_CallableStatement {

    public SPHive_insertRandomOrder(Connection conn) throws SQLException {
        super(conn, 14);
        this.name = "insertRandomOrder";
    }

    @Override
    public boolean execute() throws SQLException {
        try {
            Statement statement = conn.createStatement();
            String query = "INSERT INTO table Orders VALUES "
                    + "(" + new Random().nextInt() + "," + getObject(1) + "," + getObject(2) + "," + getObject(3) + "," + getObject(4) + ","
                    + getObject(5) + "," + getObject(6) + "," + getObject(7) + "," + getObject(8) + "," + getObject(9) + ","
                    + getObject(10) + "," + getObject(11) + "," + getObject(12) + "," + getObject(13) + ")";
            System.out.println("SP: " + query);
            int rows;
            if (trans == null) {
                rows = statement.executeUpdate(query);
            } else {
                rows = trans.executeUpdate(query);
            }
            setObject(14, rows);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}

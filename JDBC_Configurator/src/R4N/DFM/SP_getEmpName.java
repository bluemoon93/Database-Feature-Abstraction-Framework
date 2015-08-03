/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.DFM;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author bluemoon
 */
public class SP_getEmpName extends R4N_CallableStatement {

    public SP_getEmpName(Connection conn) throws SQLException {
        super(conn, 2);
        this.name = "getEmpName";
    }

    @Override
    public boolean execute() throws SQLException {
        try {
            Statement statement = conn.createStatement();
            rs = statement.executeQuery("select * from person2 where id="+getObject(1));
            if (rs.next()) {
                setObject(2, rs.getString(2));
            } else {
                setObject(2, "");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

}

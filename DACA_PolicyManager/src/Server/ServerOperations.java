/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import LocalTools.BTC_Exception;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ServerOperations {

    private Connection conn;
    private String username;
    private String password;
    private String policyServerURL;
    private String policyServerName;

    public ServerOperations(String username, String password, String url, String database) throws BTC_Exception {
        try {
            this.username = username;
            this.password = password;
            this.policyServerURL = url;
            this.policyServerName = database;
            this.conn = getConnection(username, password, this.policyServerURL);
            this.conn.setCatalog(policyServerName);
        } catch (SQLException e) {
            throw new BTC_Exception(e);
        }
    }

    private Connection getConnection(String un, String pw, String url) throws BTC_Exception {
        try {
            String connectionUrl = "jdbc:" + url + ";"
                    + "user=" + un + ";" + "password=" + pw + ";";
            return DriverManager.getConnection(connectionUrl);//Obtém conecção á base de dados
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public String getUsername2() {
        return username;
    }

    public String getPassword2() {
        return password;
    }

    public String getUrl2() {
        return policyServerURL;
    }

    public String getDatabase2() {
        return policyServerName;
    }

    public void ClearSessionTable() throws SQLException {
        Statement st = conn.createStatement();
        st.execute("DELETE FROM Ses_Session");
        st.execute("DELETE FROM _remote.SessionQueries");
    }

    public ArrayList<Integer> checkSessionOpen(int userid, int beid) throws BTC_Exception {
        try {
            ArrayList<Integer> tmp = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT App_Id FROM Sessions S INNER JOIN Usr_App UA ON UA.Usr_App_Usr_id=S.Usr_Id INNER JOIN Jar_JarPackage JP ON JP.Jar_App_id=S.App_Id INNER JOIN Be_Jar BJ ON BJ.Be_Jar_Jar_id=JP.Jar_id INNER JOIN Be_Business_Entities BE ON BE.Be_Id=BJ.Be_Jar_Be_id WHERE BE.Be_Id=? AND S.Usr_Id=?");
            ps.setInt(1, beid);
            ps.setInt(2, userid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmp.add(rs.getInt("App_Id"));
            }
            return tmp;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public String getBeUrl(int beid) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT BE_Name,Url FROM Be_Business_Entities WHERE Be_Id=?");
            ps.setInt(1, beid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString("Url") + "." + rs.getString("Be_Name");
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public String getCRUD(int sqlid) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT Sql_Crud FROM Sql_Cruds WHERE Sql_Id=?");
            ps.setInt(1, sqlid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString("Sql_Crud");
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public void DeleteSession(int user_id, int app_id) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Sessions Where Usr_Id=? and App_Id=?");
            ps.setInt(1, user_id);
            ps.setInt(2, app_id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public void addServerInfo(String ip, int port) throws BTC_Exception {
        PreparedStatement ps;
        try {
            ps = conn.prepareStatement("DELETE FROM ServerInfo");
            ps.executeUpdate();
            ps = conn.prepareStatement("INSERT INTO [ServerInfo]([ServerIP],[ServerPort])VALUES(?,?)");
            ps.setString(1, ip);
            ps.setInt(2, port);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BTC_Exception(e);
        }
    }

}

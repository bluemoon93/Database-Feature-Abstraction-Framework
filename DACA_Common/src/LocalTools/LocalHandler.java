/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LocalTools;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalHandler {

    private Connection conn;

    void GetConnection(String un, String pw, String url) {
        conn = null;
        try {
            String connectionUrl = "jdbc:" + url + ";"
                    + "user=" + un + ";" + "password=" + pw + ";";
            conn = DriverManager.getConnection(connectionUrl);//Obtém conecção á base de dados
        } catch (SQLException ex) {
            System.err.println("ERRO DE EXCEPÇÂO SQL: " + ex);

        }
    }

    public void ConfigFile(File configfile) throws IOException, SQLException {
        // Open the file that is the first
        // command line parameter
        FileInputStream fstream = new FileInputStream(configfile);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String un = br.readLine();
        String pw = br.readLine();
        String url = br.readLine();
        String db = br.readLine();
        GetConnection(un, pw, url);
        this.conn.setCatalog(db);
        fstream.close();
    }

    public ArrayList<String> GetDatabases() throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet res = meta.getCatalogs();
        ArrayList<String> str = new ArrayList<>();
        while (res.next()) {
            str.add(res.getString("TABLE_CAT"));
        }
        res.close();
        return str;
    }

    public void SetDatabase(String db) throws SQLException {
        this.conn.setCatalog(db);
    }

    boolean checkTablesExist(String tableName) throws SQLException {
        DatabaseMetaData dbm = conn.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        return tables.next();
    }

    public void CreateTables() throws SQLException {
        String createUsers_table = "create table Users ("
                + "User_ID INTEGER PRIMARY KEY IDENTITY, "
                + "Username VARCHAR(30),"
                + "Password VARCHAR(30))";
        String createInterfaces_table = "create table Interfaces ("
                + "InterfaceName nvarchar(max) NOT NULL, "
                + "jarfile VARBINARY(max),"
                + "jarname nvarchar(max))";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers_table);
            stmt.executeUpdate(createInterfaces_table);
        }
    }

    public void DropTable(String tablename) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("DROP TABLE " + tablename);
        }
    }

    public String getCatalog() throws SQLException {
        if (conn.getCatalog() == null) {
            return "";
        } else {
            return conn.getCatalog();
        }
    }

    public boolean AlreadyConnected() {
        return conn != null;
    }

    public boolean CheckUserExists(String username) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM USERS WHERE username=?")) {
            st.setString(1, username);
            ResultSet rst = st.executeQuery();
            return rst.next();
        } catch (SQLException ex) {
            System.err.println("ERROR VERIFYING USER! MSG:" + ex);
            System.exit(0);
        }
        return false;
    }

    public boolean CheckJarExists(String interfacename) {
        try (PreparedStatement st = conn.prepareStatement("SELECT * FROM Jar_JarPackage WHERE Jar_Name=?")) {
            st.setString(1, interfacename);
            ResultSet rst = st.executeQuery();
            return rst.next();
        } catch (SQLException ex) {
            System.err.println("ERROR VERIFYING INTERFACES! MSG:" + ex);
            System.exit(0);
        }
        return false;
    }

    public void AddJarFile(int id, File jarfile, String appName) {
        try (FileInputStream fis = new FileInputStream(jarfile);
             PreparedStatement pstmt = conn.prepareStatement("insert into App_Application(App_id,App_reference,App_description,App_BusinessSchemas) values (?,?,?,?)")) {
            pstmt.setInt(1, id);
            pstmt.setString(2, appName);
            pstmt.setString(3, "");
            pstmt.setBinaryStream(4, fis, (int) jarfile.length());
            pstmt.executeUpdate();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(LocalHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void AlterJarFile(int id, File jarfile, int app_id) {
        try {
            FileInputStream fis = new FileInputStream(jarfile);
            PreparedStatement pstmt = conn.prepareStatement("UPDATE Jar_JarPackage SET Jar_Data=? WHERE Jar_id=?");
            pstmt.setBinaryStream(1, fis, (int) jarfile.length());
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(LocalHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void AddApplication(int id, String app_name) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into App_Application(App_id,App_Name) values (?,?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, app_name);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(LocalHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void AddBusiness_Entity(int id, String be_name, String url) {
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into Be_Business_Entities(Be_id,Be_Name,Url) values (?,?,?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, be_name);
            pstmt.setString(3, url);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(LocalHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void AddUser(String username, String password) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO USERS(username,password) values(?,?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.execute();
        } catch (SQLException ex) {
            System.err.println("COULD NOT ADD USER USER! MSG:" + ex);
            System.exit(0);
        }
    }

    public void CloseConnection() throws SQLException {
        conn.close();
    }

    public ArrayList<String> getUsers() {
        ArrayList<String> userlist = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet rst = st.executeQuery("SELECT username FROM USERS");
            while (rst.next()) {
                userlist.add(rst.getString("username"));
            }
        } catch (SQLException ex) {
            System.err.println("ERROR VERIFYING USER! MSG:" + ex);
            System.exit(0);
        }
        return userlist;
    }

    public void getInterfaceFile(String interfaceName, String path) {
        {
            int BUFFER_SIZE = 10240;
            byte buffer[] = new byte[BUFFER_SIZE];
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM interfaces where interfacename=?")) {
                ps.setString(1, interfaceName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        File tmp = new File(path + "testingTH.jar");
                        try (InputStream binaryStream = rs.getBinaryStream("jarfile");
                             FileOutputStream fout = new FileOutputStream(tmp)) {
                            while (true) {
                                int nRead = binaryStream.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                fout.write(buffer, 0, nRead);
                            }
                            binaryStream.close();
                        }
                    }
                }
            } catch (IOException | SQLException ex) {
                Logger.getLogger(LocalHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public ArrayList<String> getInterfaces() {
        ArrayList<String> userlist = new ArrayList<>();
        try (PreparedStatement st = conn.prepareStatement("SELECT interfacename FROM interfaces");
             ResultSet rst = st.executeQuery()) {
            while (rst.next()) {
                userlist.add(rst.getString("interfacename"));
            }
        } catch (SQLException ex) {
            System.err.println("ERROR VERIFYING INTERFACES! MSG:" + ex);
            System.exit(0);
        }
        return userlist;
    }

    public boolean TableExist() throws SQLException {
        return checkTablesExist("Users");
    }
}

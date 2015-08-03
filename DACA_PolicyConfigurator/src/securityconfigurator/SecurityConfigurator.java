/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securityconfigurator;

//import Security.CCHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
//import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import securityconfigurator.Utils.BTC_Exception;
import securityconfigurator.Utils.Generator;
import securityconfigurator.Utils.GenericTreeNode;

public class SecurityConfigurator {

    private String BI = null;
    private String un = null;
    private String pw = null;
    private String url = null;
    private Connection conn;
    private String app = null;
    private Class[] roles = null;
    private final Map<Class, HashMap<Class, ArrayList<CrudInfo>>> info = new HashMap<>();
    private File tmpjar;
    private int appid;
    private GenericTreeNode<Class>[] trees;
    private Class ICrud;

    public void ConfigureServer(String un, String pw, String url) throws SCException {
        this.un = un;
        this.pw = pw;
        this.url = url;

        conn = null;
        String connectionUrl = "jdbc:" + this.url + ";"
                + "user=" + this.un + ";" + "password=" + this.pw + ";";
        try {
            conn = DriverManager.getConnection(connectionUrl); //Obtém conecção á base de dados
        } catch (SQLException ex) {
            throw new SCException("Could not connect to db! " + ex);
        }

    }

    /**
     * Updates only the App_Application table
     *
     * @param IApp
     * @param ICrud
     * @throws SCException
     * @throws BTC_Exception
     */
    public void ConfigureApplication(Class IApp, Class ICrud) throws SCException, BTC_Exception {
        this.ICrud = ICrud;
        getInfo(IApp);
        for (Class role : this.roles) {
            getAllInterfaces(role);
        }
        //DeployJar();
        UpdateApplication();
    }

    public void Configure(Class IApp, Class ICrud) throws SCException, BTC_Exception {
//        X509Certificate xcert = CCHelper.getCCAuthenticationCert();
//        BI = xcert.getSubjectX500Principal().toString();
//        System.out.println(xcert.getIssuerX500Principal().getName());
//        int fieldIdx = BI.indexOf("SERIALNUMBER=BI");
//        if (fieldIdx == -1) {
//            System.err.println("Invalid card. No BI found.");
//        }
//
//        BI = BI.substring(fieldIdx + 15, BI.indexOf(",", fieldIdx));
        BI = "12345678";
        this.ICrud = ICrud;
        getInfo(IApp);
        DeleteInfo();
        InsertInfo();
    }

    public void DeleteInfo() {
        try {
            ArrayList<Integer> Aut = new ArrayList<>();
            ArrayList<Integer> Del = new ArrayList<>();
            ArrayList<Integer> SubApp = new ArrayList<>();
            ArrayList<Integer> AppRol = new ArrayList<>();
            ArrayList<Integer> Roles = new ArrayList<>();
            ArrayList<Integer> RolBus = new ArrayList<>();
            ArrayList<Integer> Bus = new ArrayList<>();
            ArrayList<Integer> BusCrd = new ArrayList<>();
            ArrayList<Integer> Cruds = new ArrayList<>();
            int app_id;
            app_id = getApplicationId();
            if (app_id != -1) {
                getInfoForDeletion(app_id, Aut, Del, SubApp, AppRol, Roles, RolBus, Bus, BusCrd, Cruds);
                DeleteSequenceControl();
                DeleteAuthorizations();
                DeleteDelegations();
                DeleteSessions();
                DeleteSubApp(SubApp);
                DeleteAppRole(AppRol);
                DeleteRolBus(RolBus);
                DeleteBusCrd(BusCrd);
                DeleteCruds(Cruds);
                DeleteBus(Bus);
                DeleteRoles(Roles);
                DeleteApplication(app_id);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ArrayList RemoveDuplicates(ArrayList<Object> tmp) {
        ArrayList<Object> al = new ArrayList<>();
        HashSet<Object> hs = new HashSet<>();
        hs.addAll(tmp);
        al.clear();
        al.addAll(hs);
        return al;
    }

    public void DeleteBusCrd(ArrayList<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM BusCrd WHERE BusCrud_id=?");
            ps.setInt(1, ids.get(i));
            ps.executeUpdate();
        }
    }

    public void DeleteRolBus(ArrayList<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM RolBus WHERE RolBus_id=?");
            ps.setInt(1, ids.get(i));
            ps.executeUpdate();
        }
    }

    public void DeleteAppRole(ArrayList<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM AppRol WHERE AppRol_id=?");
            ps.setInt(1, ids.get(i));
            ps.executeUpdate();
        }
    }

    public void DeleteSubApp(ArrayList<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM SubApp WHERE SubApp_id=?");
            ps.setInt(1, ids.get(i));
            ps.executeUpdate();
        }
    }

    public void DeleteAuthorizations() throws SQLException {
        PreparedStatement ps = conn.prepareStatement("EXEC sp_rename 'Aut_Authorization', 'Aut_Authorization_tmp'");
        ps.executeUpdate();
        ps = conn.prepareStatement("SELECT * into Aut_Authorization FROM Aut_Authorization_tmp where 0=1");
        ps.executeUpdate();
        ps = conn.prepareStatement("DROP TABLE Aut_Authorization_tmp");
        ps.executeUpdate();
    }

    public void DeleteDelegations() throws SQLException {
//        for (int i = 0; i < ids.size(); i++) {
//            PreparedStatement ps = conn.prepareStatement("DELETE FROM Del_Delegation WHERE Del_id=?");
//            ps.setInt(1, ids.get(i));
//            ps.executeUpdate();
//        }

        PreparedStatement ps = conn.prepareStatement("EXEC sp_rename 'Del_Delegation', 'Del_Delegation_tmp'");
        ps.executeUpdate();
        ps = conn.prepareStatement("SELECT * into Del_Delegation FROM Del_Delegation_tmp where 0=1");
        ps.executeUpdate();
        ps = conn.prepareStatement("DROP TABLE Del_Delegation_tmp");
        ps.executeUpdate();
    }

    public void DeleteSessions() throws SQLException {
//        for (int i = 0; i < ids.size(); i++) {
//            PreparedStatement ps = conn.prepareStatement("DELETE FROM Del_Delegation WHERE Del_id=?");
//            ps.setInt(1, ids.get(i));
//            ps.executeUpdate();
//        }
        if (CheckSessionTableExists()) {
            PreparedStatement ps = conn.prepareStatement("EXEC sp_rename 'Ses_Session', 'Ses_Session_tmp'");
            ps.executeUpdate();
            ps = conn.prepareStatement("SELECT * into Ses_Session FROM Ses_Session_tmp where 0=1");
            ps.executeUpdate();
            ps = conn.prepareStatement("DROP TABLE Ses_Session_tmp");
            ps.executeUpdate();
        }
    }

    public boolean CheckSessionTableExists() throws SQLException {
        DatabaseMetaData dbm = this.conn.getMetaData();
        ResultSet tables = dbm.getTables(null, null, "Ses_Session", null);
        return tables.next();
    }

    public void getInfoForDeletion(int app_id, ArrayList<Integer> Aut, ArrayList<Integer> Del, ArrayList<Integer> SubApp, ArrayList<Integer> AppRol, ArrayList<Integer> Roles, ArrayList<Integer> RolBus, ArrayList<Integer> Bus, ArrayList<Integer> BusCrd, ArrayList<Integer> Cruds) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT AA.Aut_id,D.Del_id,SA.SubApp_id,AR.AppRol_id,R.Rol_id,RB.RolBus_id,B.Bus_id,BC.BusCrud_id,CD.Crd_id FROM App_Application A "
                + "LEFT OUTER JOIN SubApp SA ON SA.SubAppApp_id=A.App_id "
                + "LEFT OUTER JOIN Del_Delegation D ON D.DelSubApp_id=A.App_id "
                + "LEFT OUTER JOIN Aut_Authorization AA ON AA.AutSubApp_id=SA.SubApp_id "
                + "LEFT OUTER JOIN AppRol AR ON AR.AppRolApp_id=A.App_id "
                + "LEFT OUTER JOIN Rol_Role R ON R.Rol_id=AR.AppRolRol_id "
                + "LEFT OUTER JOIN RolBus RB ON RB.RolBusRol_id=R.Rol_id "
                + "LEFT OUTER JOIN Bus_BusinessSchema B ON B.Bus_id=RB.RolBusBus_id "
                + "LEFT OUTER JOIN BusCrd BC ON BC.BusCrdBus_id=B.Bus_id "
                + "LEFT OUTER JOIN Crd_Crud CD ON CD.Crd_id=BC.BusCrdCrd_id "
                + "WHERE App_id=?");
        ps.setInt(1, app_id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int _SubApp = rs.getInt("SubApp_id");
            if (!rs.wasNull()) {
                SubApp.add(_SubApp);
            }
            int _AppRol = rs.getInt("AppRol_id");
            if (!rs.wasNull()) {
                AppRol.add(_AppRol);
            }
            int _Roles = rs.getInt("Rol_id");
            if (!rs.wasNull()) {
                Roles.add(_Roles);
            }
            int _RolBus = rs.getInt("RolBus_id");
            if (!rs.wasNull()) {
                RolBus.add(_RolBus);
            }
            int _Bus = rs.getInt("Bus_id");
            if (!rs.wasNull()) {
                Bus.add(_Bus);
            }
            int _BusCrd = rs.getInt("BusCrud_id");
            if (!rs.wasNull()) {
                BusCrd.add(_BusCrd);
            }
            int _Cruds = rs.getInt("Crd_id");
            if (!rs.wasNull()) {
                Cruds.add(_Cruds);
            }

        }
    }

    public void DeleteApplication(int app_id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM App_Application WHERE App_id=?");
        ps.setInt(1, app_id);
        ps.executeUpdate();
    }

    public void DeleteRoles(ArrayList<Integer> ids) throws SQLException {
        ArrayList<Integer> tmp = new ArrayList<>();
        while (ids.size() > 0) {
            for (int i = 0; i < ids.size(); i++) {
                PreparedStatement ps = conn.prepareStatement("SELECT R.Rol_id FROM Rol_Role R WHERE R.RolRol_id=?");
                ps.setInt(1, ids.get(i));
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    ps = conn.prepareStatement("DELETE FROM Rol_Role WHERE Rol_id=?");
                    ps.setInt(1, ids.get(i));
                    ps.executeUpdate();
                    tmp.add(ids.get(i));
                }

            }
            for (int j = 0; j < tmp.size(); j++) {
                ids.remove(tmp.get(j));
            }
            tmp = new ArrayList<>();
        }
    }

    public void DeleteBus(ArrayList<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
//            PreparedStatement ps = conn.prepareStatement("Delete from Bus_Business_Schema_Alias where Bus_id=?");
//            ps.setInt(1, ids.get(i));
//            System.out.println(ps.executeUpdate());
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Bus_BusinessSchema WHERE Bus_id=?");
            ps.setInt(1, ids.get(i));
            ps.executeUpdate();

        }
    }

    public void DeleteCruds(ArrayList<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Crd_Crud WHERE Crd_id=?");
            ps.setInt(1, ids.get(i));
            ps.executeUpdate();

        }
    }

    public ArrayList<Integer> getCrudsOfBus(ArrayList<Integer> bus) throws SQLException {
        ArrayList<Integer> tmplist = new ArrayList<>();
        for (int i = 0; i < bus.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("SELECT BusCrdCrd_id FROM BusCrd WHERE BusCrdBus_id=?");
            ps.setInt(1, bus.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmplist.add(rs.getInt("BusCrdCrd_id"));
            }
        }

        return tmplist;
    }

    public ArrayList<Integer> getBusOfRoles(ArrayList<Integer> roles) throws SQLException {
        ArrayList<Integer> tmplist = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            PreparedStatement ps = conn.prepareStatement("SELECT RolBusBus_id FROM RolBus WHERE RolBusRol_id=?");
            ps.setInt(1, roles.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmplist.add(rs.getInt("RolBusBus_id"));
            }
        }

        return tmplist;
    }

    public ArrayList<Integer> getApplicationRoles(int appid) throws SQLException {
        ArrayList<Integer> tmplist = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement("SELECT Rol_id FROM Rol_Role R INNER JOIN AppRol AR ON AR.AppRolRol_id=R.Rol_id WHERE AR.AppRolApp_id=?");
        ps.setInt(1, appid);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            tmplist.add(rs.getInt("Rol_id"));
        }
        return tmplist;
    }

    public int getApplicationId() throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT App_id FROM App_Application Where App_reference=?");
        ps.setString(1, this.app);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("App_id");
        } else {
            return -1;
        }
    }

    public void getInfo(Class IApp) throws SCException {
        if (conn == null) {
            throw new SCException("Must connect to db first!");
        } else {
            Field[] fields = IApp.getFields();
            for (Field field : fields) {
                if (field.getName().compareTo("app") == 0) {
                    try {
                        this.app = (String) field.get(String.class);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (field.getName().compareTo("roles") == 0) {
                    try {
                        this.roles = (Class[]) field.get(null);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            if (this.app == null || this.roles == null) {
                throw new SCException("Invalid Application Interface!");
            }

        }
    }

    public void InsertInfo() throws SCException, BTC_Exception {
        try {
//            classes = new ArrayList<>();
            for (Class role : this.roles) {
                getAllInterfaces(role);
            }
            //123
            DeployJar();
            DeployDBInformation();
        } catch (SQLException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void DeployDBInformation() throws SQLException {
        Savepoint sp = null;
        try {
            conn.setAutoCommit(false);
            sp = conn.setSavepoint("start");
            DeployApplication();
            DeployRolesCruds();
            DeployBusCruds();
            DeployExampleUser();
            DeploySequenceControl();
            //pause();
            conn.commit();

        } catch (FileNotFoundException ex) {
            conn.rollback(sp);
        } catch (SQLException ex) {
            System.err.println("Sql Exception: " + ex);
            conn.rollback(sp);
        }
    }

    private void DeployExampleUser() throws SQLException {
        Savepoint sp = null;
        try {
            conn.setAutoCommit(false);
            sp = conn.setSavepoint("start");

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO [dbo].[Sub_Subject] ([Sub_username], [Sub_password], [Sub_salt], [Sub_BI]) "
                    + "VALUES ('user1', "
                    + "'-132d09d2b0c69d6438e9537561b11cf6eae2903d64d70ab374a7c0e8f06e4e8ea90841353c9a4b70b774fa69ea2cd5419cf9167c6dc2d8e026362973f9bd14e7', "
                    + "'-3d6b6bbae8ea688126c6653c86ea91f8c5bb82146e4fb2074503695febc5cc5c', ?)");

            if (BI != null) {
                stmt.setString(1, BI);
            } else {
                stmt.setNull(1, java.sql.Types.NVARCHAR);
            }

            stmt.executeUpdate();
            
            conn.commit();
        } catch (SQLException e) {
            System.err.println("user1 already existed!");
            conn.rollback(sp);
        }
        
        sp = null;
        try {
            conn.setAutoCommit(false);
            sp = conn.setSavepoint("start");

            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO [dbo].[SubApp] ([SubAppSub_id], [SubAppApp_id]) "
                    + "VALUES ((SELECT Sub_id FROM Sub_Subject WHERE Sub_username='user1'), "
                    + "(SELECT App_id FROM App_Application WHERE App_reference='app'))");

            try {
                stmt.executeUpdate();
            } catch (Exception e) {
            }

            stmt = conn.prepareStatement(
                    "INSERT INTO [dbo].[Aut_Authorization] ([Aut_code],[AutSubApp_id],[AutRol_id]) "
                    + "VALUES (1, "
                    + "(SELECT SubApp_id FROM SubApp A INNER JOIN Sub_Subject S ON S.Sub_id=A.SubAppSub_id where S.Sub_username='user1'), "
                    + "(SELECT Rol_id FROM Rol_Role WHERE Rol_reference='IRole_B1'))");

            try {
                stmt.executeUpdate();
            } catch (Exception e) {
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println(e);
            conn.rollback(sp);
        }
    }

    public void DeployBusCruds() throws SQLException {
        for (Map.Entry<Class, HashMap<Class, ArrayList<CrudInfo>>> entry : info.entrySet()) {
            Class key = entry.getKey();
            Map<Class, ArrayList<CrudInfo>> value = entry.getValue();
            int roleid = findRoleId(key);
            for (Map.Entry<Class, ArrayList<CrudInfo>> entry2 : value.entrySet()) {
                PreparedStatement ps = conn.prepareStatement("Insert into Bus_BusinessSchema(Bus_url) values(?)", PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, entry2.getKey().getName());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                int busid = rs.getInt(1);
//                String name = entry2.getKey().getName();
//                conn.setAutoCommit(false);
//                ps = conn.prepareStatement("Insert into Bus_Business_Schema_Alias(Alias_Name,Bus_id) values(?,?)");
//
//                ps.setString(1, name + "_seq_1");
//                ps.setInt(2, busid);
//                ps.addBatch();
//                ps.setString(1, name + "_seq_2");
//                ps.setInt(2, busid);
//                ps.addBatch();
//                ps.executeBatch();
//                conn.commit();
//
//                conn.setAutoCommit(true);
                ps = conn.prepareStatement("Insert into RolBus(RolBusRol_id,RolBusBus_id) values(?,?)");
                ps.setInt(1, roleid);
                ps.setInt(2, busid);
                ps.executeUpdate();
                for (int i = 0; i < entry2.getValue().size(); i++) {
                    ps = conn.prepareStatement("Insert into Crd_Crud(Crd_crud,Crd_reference) values(?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, entry2.getValue().get(i).getCrud());
                    ps.setString(2, entry2.getValue().get(i).getCrudref());
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys();
                    rs.next();
                    int crudid = rs.getInt(1);
                    ps = conn.prepareStatement("Insert into BusCrd(BusCrdBus_id,BusCrdCrd_id) values(?,?)");
                    ps.setInt(1, busid);
                    ps.setInt(2, crudid);
                    ps.executeUpdate();
                }
            }
        }

    }

    public void DeployApplication() throws SQLException, FileNotFoundException {
        PreparedStatement ps = conn.prepareStatement("Insert into App_Application(App_reference,App_description,App_BusinessSchemas) values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        FileInputStream fis = new FileInputStream(this.tmpjar);
        ps.setString(1, this.app);
        ps.setString(2, "");
        ps.setBinaryStream(3, fis, (int) this.tmpjar.length());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        this.appid = rs.getInt(1);
        System.out.println("deleting "+this.tmpjar.getAbsolutePath()+" "+this.tmpjar.exists());
        //this.tmpjar.delete();

    }

    public void DeployRolesCruds() throws SQLException, FileNotFoundException {

        ConstructTrees();
        for (GenericTreeNode<Class> tree : this.trees) {
            DeployTree(tree.getChildAt(0));
        }

    }

    public void DeployTree(GenericTreeNode<Class> tree) {
        try {
            InsertAppRol(InsertRole(tree.data));
            List<GenericTreeNode<Class>> children = tree.getChildren();
            for (GenericTreeNode<Class> aChildren : children) {
                DeployTree(aChildren);
            }
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int InsertRole(Class cls) throws SQLException, FileNotFoundException {
        String roleref = cls.getSimpleName();
        Class parent = findRoleParent(cls);
        PreparedStatement ps;
        if (parent == null) {
            ps = conn.prepareStatement("Insert into Rol_Role(Rol_reference,Rol_description,RolRol_id) values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, roleref);
            ps.setString(2, "");
            ps.setNull(3, java.sql.Types.INTEGER);
        } else {
            ps = conn.prepareStatement("Insert into Rol_Role(Rol_reference,Rol_description,RolRol_id) values(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, roleref);
            ps.setString(2, "");
            ps.setInt(3, findRoleId(parent));
        }
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    public void InsertAppRol(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("Insert into AppRol(AppRolApp_id,AppRolRol_id) values(?,?)");
        ps.setInt(1, this.appid);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    public int findRoleId(Class cls) throws SQLException {
        PreparedStatement ps;
        ps = conn.prepareStatement("SELECT Rol_id FROM Rol_Role Where Rol_reference=?");
        ps.setString(1, cls.getSimpleName());
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt("Rol_id");
    }

    public Class findRoleParent(Class cls) {
        for (Map.Entry<Class, HashMap<Class, ArrayList<CrudInfo>>> entry : info.entrySet()) {
            Class key = entry.getKey();
            Class[] itfs = key.getInterfaces();
            for (Class itf : itfs) {
                if (itf == cls) {
                    return key;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void ConstructTrees() {
        this.trees = (GenericTreeNode<Class>[]) new GenericTreeNode[this.roles.length];
        for (int i = 0; i < roles.length; i++) {
            trees[i] = new GenericTreeNode<>();
            ConstructTree(roles[i], trees[i]);
        }

    }

    public void ConstructTree(Class cls, GenericTreeNode<Class> tree) {
        GenericTreeNode<Class> newtree = new GenericTreeNode<>(cls);
        tree.addChild(newtree);
        Class[] itfs = cls.getInterfaces();
        for (Class itf : itfs) {
            ConstructTree(itf, newtree);
        }
    }

    public void DeployJar() throws SCException, BTC_Exception {
        ArrayList<Class> tmpclasses = new ArrayList<>();
        for (Map.Entry<Class, HashMap<Class, ArrayList<CrudInfo>>> entry : info.entrySet()) {
            Map<Class, ArrayList<CrudInfo>> value = entry.getValue();
            for (Map.Entry<Class, ArrayList<CrudInfo>> entry2 : value.entrySet()) {
                tmpclasses.add(entry2.getKey());

            }
        }

        // add elements to al, including duplicates
        this.tmpjar = new File("./tmpjar.jar");
        HashSet<Class> hs = new HashSet<>();
        hs.addAll(tmpclasses);
        tmpclasses.clear();
        tmpclasses.addAll(hs);
        if (tmpjar.exists()) {
            System.out.println("deleting (deployJar) "+tmpjar.getAbsolutePath());
            if (!tmpjar.delete()) {
                throw new SCException("ERROR CANT DELETE TEMPORARY JAR");
            }
        }

        for (Class tmpclass : tmpclasses) {
            Generator.AddClassBe("./", "tmpjar.jar", tmpclass);
        }

    }

    public void getAllInterfaces(Class cls) {
        info.put(cls, new HashMap<Class, ArrayList<CrudInfo>>());

        Field[] fltmp = cls.getDeclaredFields();
        for (int i = 0; i < fltmp.length; i += 2) {
            if (fltmp[i].getType() == Class.class) {

                try {
                    info.get(cls).put((Class) fltmp[i].get(null), new ArrayList<CrudInfo>());
                    try {
                        String[] cruds = (String[]) fltmp[i + 1].get(null);
                        for (String crud : cruds) {
                            info.get(cls).get(fltmp[i].get(null)).add(new CrudInfo(getCrudRef(crud), crud));
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        Class[] tmp = cls.getInterfaces();
        for (Class tmp1 : tmp) {
            getAllInterfaces(tmp1);
        }
    }

    public String getCrudRef(String crud) {
        try {
            Class cls = this.ICrud;
            Field[] fldtmp = cls.getDeclaredFields();
            for (Field fldtmp1 : fldtmp) {
                String tmp = (String) fldtmp1.get(null);
                if (tmp.compareTo(crud) == 0) {
                    return fldtmp1.getName();
                }
            }
            return null;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void DeleteSequenceControl() {
        try {
            ClearFromDatabase("RevokeList");
            ClearFromDatabase("BSSeqRol");
            ClearFromDatabase("BSSeqPos");
            //ClearFromDatabase("BSCrude_Seq_Pos");
            ClearFromDatabase("ControlInfo");
        } catch (SQLException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ClearFromDatabase(String table) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM " + table, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            rs.deleteRow();
        }
    }

    private void DeploySequenceControl() {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqPos (SeqID, SeqPos, RefBusID) "
                    + "VALUES (1, 1, (SELECT Bus_id FROM Bus_BusinessSchema WHERE Bus_url = 'S_Customers.IS_Customers'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqPos (SeqID, SeqPos, RefBusID) "
                    + "VALUES (1, 2, (SELECT Bus_id FROM Bus_BusinessSchema WHERE Bus_url = 'S_Orders.IS_Orders'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqPos (SeqID, SeqPos, RefBusID) "
                    + "VALUES (1, 3, (SELECT Bus_id FROM Bus_BusinessSchema WHERE Bus_url = 'U_Orders.IU_Orders'))");
            pstmt.executeUpdate();
            
            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqPos (SeqID, SeqPos, RefBusID) "
                    + "VALUES (1, 4, (SELECT Bus_id FROM Bus_BusinessSchema WHERE Bus_url = 'US_Customers_byName.IS_Customers2'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqPos (SeqID, SeqPos, RefBusID) "
                    + "VALUES (2, 1, (SELECT Bus_id FROM Bus_BusinessSchema WHERE Bus_url = 'S_Customers.IS_Customers'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqPos (SeqID, SeqPos, RefBusID) "
                    + "VALUES (2, 2, (SELECT Bus_id FROM Bus_BusinessSchema WHERE Bus_url = 'I_Orders.II_Orders'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqRol (SeqID, RefRolID) "
                    + "VALUES (1, (SELECT Rol_id FROM Rol_Role WHERE Rol_reference = 'IRole_B1'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO BSSeqRol (SeqID, RefRolID) "
                    + "VALUES (2, (SELECT Rol_id FROM Rol_Role WHERE Rol_reference = 'IRole_B1'))");
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement(
                    "INSERT INTO ControlInfo ([key], value) "
                    + "VALUES (?, ?)");

            pstmt.setString(1, "active");
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void UpdateApplication() {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE App_Application SET App_BusinessSchemas = ?");
            System.out.println("opening "+this.tmpjar.getAbsolutePath());
            try (FileInputStream fis = new FileInputStream(this.tmpjar)) {
                ps.setBinaryStream(1, fis, (int) this.tmpjar.length());
                ps.executeUpdate();
                this.tmpjar.delete();
            }
        } catch (SQLException | IOException ex) {
            Logger.getLogger(SecurityConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

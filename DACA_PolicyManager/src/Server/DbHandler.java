/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import JavaTools.BeInfo;
import JavaTools.Clients;
import LocalTools.BTC_Exception;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.*;
import java.util.*;

import Configs.Reader;

import static Server.DBQueries.*;
import java.math.BigInteger;

/**
 * Handles the database interaction
 */
public class DbHandler implements Closeable {

    /**
     * Database connection.
     */
    private final Connection conn;

    /**
     * The reference to the subject and application
     */
    private int subApp_id;

    /**
     * The reference to the session id
     */
    private int sessionID;

    /**
     * Instantiate a new object of the type DbHandler.
     *
     * @param username The database access' username.
     * @param password The database access' password.
     * @param url The database url.
     * @param database The catalog name.
     * @throws BTC_Exception If an error occurs.
     */
    public DbHandler() throws BTC_Exception { //String username, String password, String url, String database
        try {
            conn = Reader.getPSConnNew();
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
        /*try {
         String connectionUrl = "jdbc:" + url + ";user=" + username + ";password=" + password + ";";
         conn = DriverManager.getConnection(connectionUrl);
         conn.setCatalog(database);
         } catch (SQLException ex) {
         throw new BTC_Exception(ex);
         }*/
    }

    public boolean TryToLogin(String App_Name, String Username, String Ip, String Port) throws BTC_Exception {
        return TryToLogin(App_Name, Username, null, Ip, Port);
    }

    /**
     * Tries to login using the given credentials and application.
     *
     * @param App_Name The name of the application.
     * @param Username The username of the subject.
     * @param Password The user's password.
     * @param Ip The IP address of the client.
     * @param Port The port where the client is running.
     * @return True if the login process was successful, false otherwise.
     * @throws BTC_Exception If an error occurs.
     */
    public boolean TryToLogin(String App_Name, String Username, String Password, String Ip, String Port) throws BTC_Exception {

        try {
            // Get the subject ID of the client from the IP address and port
            PreparedStatement ps;
            if (Password == null) {
                ps = conn.prepareStatement(Select_SujectID_By_Username);
                ps.setString(1, Username);
            } else {
                ps = conn.prepareStatement(Select_SujectID_By_Username_Password);
                ps.setString(1, Username);
                ps.setString(2, Password);
            }
            ResultSet rs = ps.executeQuery();

            int usr_id;
            if (rs.next()) {
                usr_id = rs.getInt("Sub_id");
                ps.close();
            } else {
                return false;
            }

            if (Integer.parseInt(Port) != 0) {
                // Select the subject-application relation ID from the subjectID and the application name.
                ps = conn.prepareStatement(Select_SubjectAplicationRelationID_By_SubjectID_ApplicationName);
                System.out.println("Getting values for UserID=" + usr_id + " and APP_NAME=" + App_Name);
                ps.setInt(1, usr_id);
                ps.setString(2, App_Name);
                rs = ps.executeQuery();

                if (rs.next()) {
                    // Login successful.
                    subApp_id = rs.getInt("SubApp_id");

                    // Insert a reference to the active client in the database.
                    CreateSession(subApp_id, Ip, Port);
                    System.out.println("Login was successful");
                    return true;
                }
                System.out.println("Login was not successful");
                return false;
            } else {
                return true;
            }

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Inserts a reference to the client associated with the subject-application
     * relation in the database.
     *
     * @param subapp_id The identifier of the relation of subject-application.
     * @param ip The IP address of the client.
     * @param port The port being used by the client.
     * @throws BTC_Exception If an error occurs.
     */
    private void CreateSession(int subapp_id, String ip, String port) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_SessionID_By_SubAppID_IP_Port);
            ps.setInt(1, subapp_id);
            ps.setString(2, ip);
            ps.setString(3, port);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sessionID = rs.getInt(1);
            } else {
                ps = conn.prepareStatement(Insert_Session_SubAppID_ClientIP_ClientPort);
                ps.setInt(2, subapp_id);
                ps.setString(3, ip);
                ps.setInt(4, Integer.parseInt(port));

                int count = 0;
                while (count < 3) {
                    try {
                        sessionID = (int) (Math.random() * Integer.MAX_VALUE);
                        ps.setInt(1, sessionID);
                        if (ps.executeUpdate() > 0) {
                            break;
                        }
                    } catch (SQLException ex) {
                    }

                    System.out.println("Try " + (count + 1) + " failed.");
                    count++;
                }

                if (count == 3) {
                    throw new BTC_Exception("Could not create the session.");
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Deletes a reference to the client associated with the subject-application
     * relation in the database.
     *
     * @param ip The IP address of the client.
     * @param port The port being used by the client.
     * @throws BTC_Exception If an error occurs.
     */
    public void EndSession(String ip, String port) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Delete_Session_SubAppID_ClientIP_ClientPort);
            ps.setString(1, ip);
            ps.setInt(2, Integer.parseInt(port));
            ps.executeUpdate();

            // Determine the existing session remote IDs.
            ArrayList<Integer> existingSRIDs = new ArrayList<>();
            ps = conn.prepareStatement(Select_QuerySRIDs_By_SessionID);
            ps.setInt(1, getSessionID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingSRIDs.add(rs.getInt(1));
                }
            }

            ps = conn.prepareStatement(Delete_Session_RemoteIDs);
            for (int querySRID : existingSRIDs) {
                ps.setInt(1, getSessionID());
                ps.setInt(2, querySRID);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Selects the Jar with the access control model from the database.
     *
     * @param appName The client's application name.
     * @return The jar bytes.
     * @throws BTC_Exception If an error occurs.
     */
    public byte[] GetJar(String appName) throws BTC_Exception {
        PreparedStatement ps;
        try {
            // Request the jar from the database.
            ps = conn.prepareStatement(Select_BusinessSchemas_By_Application);

            ps.setString(1, appName);
            ResultSet rs = ps.executeQuery();

            byte[] ret = null;
            if (rs.next()) {
                // read the jar from the stream to an array of bytes
                try (InputStream binaryStream = rs.getBinaryStream("App_BusinessSchemas")) {
                    ret = IOUtils.toByteArray(binaryStream);
                }
            }

            System.out.println("DONE");

            assert ret != null : "GetJar was going to return a null reference.";
            return ret;
        } catch (SQLException | IOException e) {
            throw new BTC_Exception(e);
        }
    }

    /**
     * Retrieves the salt from the database.
     *
     * @param username The username of the user whose salt is requested.
     * @return The salt.
     * @throws BTC_Exception If an error occurs.
     */
    public String getSalt(String username) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT Sub_salt FROM Sub_Subject WHERE Sub_username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return (rs.next() ? rs.getString(1) : null);
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Retrieves the list of roles and parent roles associated with the
     * subject-application relation.
     *
     * @return The list of roles to parent roles.
     * @throws BTC_Exception If an error occurs.
     */
    public Map<String, String> getRoles() throws BTC_Exception {
        Map<String, String> roles = new HashMap<>();
        try {
            // Select roles and the parent roles from the database.
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_By_SubjectAplicationRelationID);
            ps.setInt(1, this.subApp_id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // If the subject-application relation is associated with the role
                if (haveAuthorization(rs.getString("role"), this.subApp_id)) {
                    // put it in the return list
                    roles.put(rs.getString("role"), rs.getString("parent"));

                    // Add any child roles related to this role.
                    ArrayList<String> childs = getChildsFromRole(rs.getString("role"));
                    for (String child : childs) {
                        putOtherRoles(child, roles, this.subApp_id);
                    }
                }
            }

            return roles;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Retrieves the list of roles and parent roles associated with an app
     * reference.
     *
     * @param appRef The application reference.
     * @return The list of roles to parent roles.
     * @throws BTC_Exception If an error occurs.
     */
    public Map<String, String> getRolesByAppRef(String appRef) throws BTC_Exception {
        Map<String, String> roles = new HashMap<>();
        try {
            // Select roles and the parent roles from the database.
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_By_App);
            ps.setString(1, appRef);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // put it in the return list
                roles.put(rs.getString("role"), rs.getString("parent"));
            }

            return roles;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Recursive insertion of child roles into the map roles for the authorized
     * subject-application relation.
     *
     * @param role The role to insert.
     * @param roles The map to insert to.
     * @param autsubapp The identifier of the subject-application relation
     * authorized.
     */
    private void putOtherRoles(String role, Map<String, String> roles, int autsubapp) {
        // Add child roles only if they are not directly authorized.
        if (!haveAuthorization(role, autsubapp)) {
            roles.put(role, getParentRole(role));
            ArrayList<String> childs = getChildsFromRole(role);
            for (String child : childs) {
                putOtherRoles(child, roles, autsubapp);
            }
        }
    }

    /**
     * Recursive insertion of child roles into the map roles for the delegated
     * subject-application relation.
     *
     * @param role The role to insert.
     * @param roles The map to insert to.
     * @param autsubapp The identifier of the delegated subject-application
     * relation.
     */
    private void putOtherRolesDelegation(String role, Map<String, String> roles, int autsubapp) {
        // Add child roles only if they are not directly authorized.
        if (!haveAuthorizationDelegation(role, autsubapp)) {
            roles.put(role, getParentRole(role));
            ArrayList<String> childs = getChildsFromRole(role);
            for (String child : childs) {
                putOtherRolesDelegation(child, roles, autsubapp);
            }
        }
    }

    /**
     * Selects the parent role of the given role.
     *
     * @param role The role.
     * @return The parent of the role.
     * @throws BTC_Exception If an error occurs.
     */
    private String getParentRole(String role) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_ParentRole_By_RoleReference);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Rol_reference");
            } else {
                return null;
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    //TODO: Continue documentation...
    private boolean haveAuthorization(String role, int autsubapp) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_With_Authorization_By_SubAppID_RoleReference);
            ps.setInt(1, autsubapp);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    private boolean haveAuthorizationDelegation(String role, int autsubapp) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_With_Delegated_Authorization_By_DelegatedSubAppID_RoleReference);
            ps.setInt(1, autsubapp);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    private boolean AnyParentPermission(String role, int autsubapp) throws BTC_Exception {
        String parent = getParentRole(role);
        return haveAuthorization(role, autsubapp) || parent != null && AnyParentPermission(parent, autsubapp);
    }

    private boolean AnyParentPermissionDelegation(String role, int autsubapp) throws BTC_Exception {
        String parent = getParentRole(role);
        return haveAuthorizationDelegation(role, autsubapp) || parent != null && AnyParentPermissionDelegation(parent, autsubapp);
    }

    public void getRoleInfo(String role, Map<String, ArrayList<BeInfo>> info) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_BusUrl_CrudReference_CrudID_Crud_By_RoleReference);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!info.containsKey(rs.getString("Bus_url"))) {
                    info.put(rs.getString("Bus_url"), new ArrayList<BeInfo>());
                    info.get(rs.getString("Bus_url")).add(new BeInfo(rs.getInt("Crd_id"), rs.getString("Crd_reference"), rs.getString("Crd_Crud")));
                    System.out.println("adding " + rs.getString("Crd_Crud") + " to the crud list");
                } else {
                    info.get(rs.getString("Bus_url")).add(new BeInfo(rs.getInt("Crd_id"), rs.getString("Crd_reference"), rs.getString("Crd_Crud")));
                }
            }

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

    }

    private ArrayList<String> getChildsFromRole(String role) throws BTC_Exception {
        ArrayList<String> childs = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(Select_ChildRoles_By_RoleReference);
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                childs.add(rs.getString("Rol_reference"));
            }

            return childs;

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public ArrayList<Clients> getClientsFromSubApp(int id) throws BTC_Exception {
        ArrayList<Clients> clients = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Clients_IP_Port_By_SessionSubjectApplicationID);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clients.add(new Clients(rs.getString("Ses_clientIP"), rs.getInt("Ses_clientPort")));

            }

            return clients;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Retrieves the clients associated with a orchestration sequence.
     *
     * @param seq The sequence identifier
     * @return The associated clients information.
     * @throws LocalTools.BTC_Exception If something goes wrong.
     */
    public ArrayList<Clients> getClientsFromSequence(int seq) throws BTC_Exception {
        ArrayList<Clients> clients = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Clients_IP_Port_By_OrchestrationSequenceID);
            ps.setInt(1, seq);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clients.add(new Clients(rs.getString("Ses_clientIP"), rs.getInt("Ses_clientPort")));

            }

            return clients;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Retrieves all clients information.
     *
     * @return The clients information.
     * @throws LocalTools.BTC_Exception If something goes wrong.
     */
    public ArrayList<Clients> getAllClients() throws BTC_Exception {
        ArrayList<Clients> clients = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(Select_All_Clients_IP_Port);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clients.add(new Clients(rs.getString("Ses_clientIP"), rs.getInt("Ses_clientPort")));

            }

            return clients;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public Map<String, ArrayList<BeInfo>> getAut_ChangesInsertion(int autsubapp, int rol_id) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_By_RoleID);
            ps.setInt(1, rol_id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String role = rs.getString("role");
            this.subApp_id = autsubapp;

            HashMap<String, String> lroles = new HashMap<>();
            if (haveAuthorization(role, autsubapp)) {
                lroles.put(rs.getString("role"), rs.getString("parent"));
                ArrayList<String> childs = getChildsFromRole(role);
                for (String child : childs) {
                    putOtherRoles(child, lroles, autsubapp);
                }
            }
            Map<String, ArrayList<BeInfo>> info = new HashMap<>();
            for (Map.Entry<String, String> entry : lroles.entrySet()) {
                getRoleInfo(entry.getKey(), info);
            }

            return info;

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public Map<String, ArrayList<BeInfo>> getAut_ChangesDeletion(int autsubapp, int rol_id) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_By_RoleID);
            ps.setInt(1, rol_id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String role = rs.getString("role");

            HashMap<String, String> lroles = new HashMap<>();
            if (haveAuthorization(role, autsubapp) || AnyParentPermission(getParentRole(role), autsubapp)) {
                return null;
            } else {

                lroles.put(rs.getString("role"), rs.getString("parent"));
                ArrayList<String> childs = getChildsFromRole(role);
                for (String child : childs) {
                    putOtherRoles(child, lroles, autsubapp);
                }
                Map<String, ArrayList<BeInfo>> info = new HashMap<>();
                for (Map.Entry<String, String> entry : lroles.entrySet()) {
                    getRoleInfo(entry.getKey(), info);
                }
                this.subApp_id = autsubapp;
                Map<String, String> rolestmp = getDelegationsInfo();
                Map<String, ArrayList<BeInfo>> infotmp = new HashMap<>();
                for (Map.Entry<String, String> entry : rolestmp.entrySet()) {
                    String key = entry.getKey();
                    getRoleInfo(key, infotmp);
                }

                for (Map.Entry<String, ArrayList<BeInfo>> entrytmp : infotmp.entrySet()) {
                    if (info.containsKey(entrytmp.getKey())) {

                        info.remove(entrytmp.getKey());

                    }
                }

                return info;

            }

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public Map<String, String> getDelegationsInfo() throws BTC_Exception {

        HashMap<String, String> roles = new HashMap<>();
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_Delegations_By_DelegatedSubAppID);
            ps.setInt(1, this.subApp_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (haveAuthorizationDelegation(rs.getString("role"), this.subApp_id)) {
                    roles.put(rs.getString("role"), rs.getString("parent"));
                    ArrayList<String> childs = getChildsFromRole(rs.getString("role"));
                    for (String child : childs) {
                        putOtherRolesDelegation(child, roles, this.subApp_id);
                    }

                }
            }
            return roles;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public Map<String, ArrayList<BeInfo>> getInsertDelegationInfo(int subappid, int rolid) throws BTC_Exception {
        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_By_RoleID);
            ps.setInt(1, rolid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String role = rs.getString("role");
            this.subApp_id = subappid;

            HashMap<String, String> lroles = new HashMap<>();
            if (haveAuthorizationDelegation(role, subappid)) {
                lroles.put(rs.getString("role"), rs.getString("parent"));
                ArrayList<String> childs = getChildsFromRole(role);
                for (String child : childs) {
                    putOtherRolesDelegation(child, lroles, subappid);
                }
            }
            Map<String, ArrayList<BeInfo>> info = new HashMap<>();
            for (Map.Entry<String, String> entry : lroles.entrySet()) {
                getRoleInfo(entry.getKey(), info);
            }

            return info;

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

    }

    public Map<String, ArrayList<BeInfo>> getDeleteDelegationInfo(int subappid, int rolid) throws BTC_Exception {

        try {
            PreparedStatement ps = conn.prepareStatement(Select_Role_ParentRole_By_RoleID);
            ps.setInt(1, rolid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String role = rs.getString("role");

            HashMap<String, String> lroles = new HashMap<>();
            if (haveAuthorizationDelegation(role, subappid) || AnyParentPermissionDelegation(getParentRole(role), subappid)) {
                return null;
            } else {

                lroles.put(rs.getString("role"), rs.getString("parent"));
                ArrayList<String> childs = getChildsFromRole(role);
                for (String child : childs) {
                    putOtherRolesDelegation(child, lroles, subappid);
                }
                Map<String, ArrayList<BeInfo>> info = new HashMap<>();
                for (Map.Entry<String, String> entry : lroles.entrySet()) {
                    getRoleInfo(entry.getKey(), info);
                }
                this.subApp_id = subappid;
                Map<String, String> rolestmp = getRoles();
                Map<String, ArrayList<BeInfo>> infotmp = new HashMap<>();
                for (Map.Entry<String, String> entry : rolestmp.entrySet()) {
                    String key = entry.getKey();
                    getRoleInfo(key, infotmp);
                }

                for (Map.Entry<String, ArrayList<BeInfo>> entrytmp : infotmp.entrySet()) {
                    if (info.containsKey(entrytmp.getKey())) {
                        info.remove(entrytmp.getKey());

                    }
                }

                return info;

            }

        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

    }

    public Map<Integer, List<Map.Entry<String, List<String>>>> getControlInfoForRoles(Map<String, String> roles) throws BTC_Exception {
        try {
            Map<Integer, List<Map.Entry<String, List<String>>>> ret = new HashMap<>();

            for (String role : roles.keySet()) {
                PreparedStatement pstmt = conn.prepareStatement(Select_Sequences_ID_Position_BusUrl_By_RoleReference);

                pstmt.setString(1, role);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int seq = rs.getInt(1);
                    int pos = rs.getInt(2);
                    String beurl = rs.getString(3);

                    List<String> revokelist = getRevokeList(seq, pos);

                    if (!ret.containsKey(seq)) {
                        ret.put(seq, new ArrayList<Map.Entry<String, List<String>>>());
                    }

                    List<Map.Entry<String, List<String>>> seqdata = ret.get(seq);

                    while (seqdata.size() < pos) {
                        seqdata.add(null);
                    }
                    seqdata.remove(pos - 1);
                    seqdata.add(pos - 1, new AbstractMap.SimpleEntry<>(beurl, revokelist));
                }
            }
            return ret;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public Map<Integer, List<Map.Entry<String, List<String>>>> getSequenceInfoForRole(String role) throws BTC_Exception {
        try {
            Map<Integer, List<Map.Entry<String, List<String>>>> ret = new HashMap<>();

            PreparedStatement pstmt = conn.prepareStatement(Select_Sequences_ID_Position_BusUrl_By_RoleReference);

            pstmt.setString(1, role);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int seq = rs.getInt(1);
                int pos = rs.getInt(2);
                String beurl = rs.getString(3);

                List<String> revokelist = getRevokeList(seq, pos);

                if (!ret.containsKey(seq)) {
                    ret.put(seq, new ArrayList<Map.Entry<String, List<String>>>());
                }

                List<Map.Entry<String, List<String>>> seqdata = ret.get(seq);

                while (seqdata.size() < pos) {
                    seqdata.add(null);
                }
                seqdata.remove(pos - 1);
                seqdata.add(pos - 1, new AbstractMap.SimpleEntry<>(beurl, revokelist));
            }
            return ret;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public List<Integer> getBEsIDsForRole(String role) throws BTC_Exception {
        try {
            List<Integer> ret = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(Select_BusinessID_By_Role);

            pstmt.setString(1, role);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ret.add(rs.getInt(1));
            }
            return ret;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public String getBusinessURLByID(int busid) throws BTC_Exception {
        try {
            PreparedStatement pstmt = conn.prepareStatement(Select_BusUrl_By_BusID);

            pstmt.setInt(1, busid);

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    private List<String> getRevokeList(int seq, int pos) throws BTC_Exception {
        List<String> revokelist = new ArrayList<>();

        try {
            PreparedStatement pstmt;
            pstmt = conn.prepareStatement(Select_BusUrl_By_SequenceID_Position);

            pstmt.setInt(1, seq);
            pstmt.setInt(2, pos);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                revokelist.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new BTC_Exception(e);
        }

        return revokelist;
    }

    public boolean getSeqStatus() throws BTC_Exception {
        try {
            PreparedStatement pstmt = conn.prepareStatement(Select_Control_Status);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

        throw new BTC_Exception("Couldn't get orquestration status.");
    }

    public Map<Integer, String> getCRUDsByBE(int beid) {
        try {
            Map<Integer, String> ret = new HashMap<>();

            PreparedStatement ps = conn.prepareStatement(Select_CRUDs_By_beid);
            ps.setInt(1, beid);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ret.put(rs.getInt("Crd_id"), rs.getString("Crd_reference"));
            }

            return ret;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public List<Map.Entry<String, List<String>>> getSequenceInfo(int seq) throws BTC_Exception {
        List<Map.Entry<String, List<String>>> ret = new ArrayList<>();
        try {
            PreparedStatement pstmt = conn.prepareStatement(Select_Sequence_Positions_BusUrl_By_SequenceID);

            pstmt.setInt(1, seq);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int pos = rs.getInt(1);
                String beurl = rs.getString(2);

                List<String> revokelist = getRevokeList(seq, pos);

                while (ret.size() < pos) {
                    ret.add(null);
                }
                ret.remove(pos - 1);
                ret.add(pos - 1, new AbstractMap.SimpleEntry<>(beurl, revokelist));
            }

            return ret;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public Map<Integer, List<Map.Entry<String, List<String>>>> getControlInfo() throws BTC_Exception {
        Map<Integer, List<Map.Entry<String, List<String>>>> ret = new HashMap<>();
        try {
            PreparedStatement pstmt = conn.prepareStatement(Select_Role_ParentRole);

            ResultSet rs = pstmt.executeQuery();
            Map<Integer, Integer> roles = new HashMap<>();
            while (rs.next()) {
                roles.put(rs.getInt(1), rs.getInt(2));
            }

            pstmt.close();
            pstmt = conn.prepareStatement(Select_Authorizations_By_SubAppID);
            pstmt.setInt(1, this.subApp_id);
            rs = pstmt.executeQuery();

            List<Integer> relevantRoles = new ArrayList<>();
            while (rs.next()) {
                relevantRoles.add(rs.getInt(1));
            }

            boolean changed;
            do {
                changed = false;
                for (int child : roles.keySet()) {
                    int parent = roles.get(child);
                    if (relevantRoles.contains(parent) && !relevantRoles.contains(child)) {
                        relevantRoles.add(child);
                        changed = true;
                    }
                }
            } while (changed);

            pstmt.close();
            pstmt = conn.prepareStatement(Select_Sequences_ID_Positions_BusUrl_By_RoleID);

            for (int relevantRole : relevantRoles) {
                pstmt.setInt(1, relevantRole);

                rs = pstmt.executeQuery();
                while (rs.next()) {
                    int seq = rs.getInt(1);
                    int pos = rs.getInt(2);
                    String beurl = rs.getString(3);

                    List<String> revokelist = getRevokeList(seq, pos);

                    if (!ret.containsKey(seq)) {
                        ret.put(seq, new ArrayList<Map.Entry<String, List<String>>>());
                    }

                    while (ret.get(seq).size() < pos) {
                        ret.get(seq).add(null);
                    }
                    ret.get(seq).remove(pos - 1);
                    ret.get(seq).add(pos - 1, new AbstractMap.SimpleEntry<>(beurl, revokelist));
                }
            }

            return ret;
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    /**
     * Closes this stream and releases any system resources associated with it.
     * If the stream is already closed then invoking this method has no effect.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {

        System.out.println("closing..........:");
        try {
            this.conn.close();
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Retrieves the user's password.
     *
     * @param username the username.
     * @return the password.
     */
    public byte[] getSecret(String username) {
        try (PreparedStatement ps = conn.prepareStatement(Select_Password_By_Username)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (new BigInteger(rs.getString(1), 16)).toByteArray();
                } else {
                    throw new BTC_Exception("Could not find the username");
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public String getUserBI(String username) {
        try (PreparedStatement ps = conn.prepareStatement(Select_BI_By_Username)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    throw new BTC_Exception("Could not find the user's BI");
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public byte[] getRootCert(String alias) {
        try (PreparedStatement ps = conn.prepareStatement(Select_Certificate_By_Alias)) {
            ps.setString(1, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes(1);
                } else {
                    throw new BTC_Exception("Could not find the user's BI");
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }
    }

    public int getSessionID() {
        return sessionID;
    }

    public int createSessionQuery(String crud_ref) {
        // Select the Query Remote ID given the crud reference
        int queryRID;
        try (PreparedStatement ps = conn.prepareStatement(Select_QueryRID)) {
            ps.setString(1, crud_ref);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    queryRID = rs.getInt(1);
                } else {
                    throw new BTC_Exception("Could not find the query remote ID");
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

        // Determine the existing session remote IDs.
        ArrayList<Integer> existingSRIDs = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(Select_QuerySRIDs_By_SessionID)) {
            ps.setInt(1, getSessionID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingSRIDs.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

        // Choose a new random session query remote ID.
        int newQuerySRID;
        do {
            newQuerySRID = (int) (Math.random() * Integer.MAX_VALUE);
        } while (existingSRIDs.contains(newQuerySRID));

        // Add the query session remote ID.
        try (PreparedStatement ps = conn.prepareStatement(Insert_Session_Query)) {
            ps.setInt(1, getSessionID());
            ps.setInt(2, queryRID);
            ps.setInt(3, newQuerySRID);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

        return newQuerySRID;
    }

    public ArrayList<Clients> getClientsForRole(int roleid) {
        ArrayList<Clients> clients = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(Select_Clients_IP_Port_By_RoleID)) {
            ps.setInt(1, roleid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clients.add(new Clients(rs.getString(1), rs.getInt(2)));
                }
            }
        } catch (SQLException ex) {
            throw new BTC_Exception(ex);
        }

        return clients;
    }
}

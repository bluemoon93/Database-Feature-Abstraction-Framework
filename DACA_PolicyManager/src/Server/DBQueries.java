package Server;

/**
 * Contains the required SQL Queries.
 * 
 * Created by Regateiro on 11-02-2014.
 */
public final class DBQueries {

    /**
     * Selects the subject identifier given its username and password.
     */
    public static final String Select_SujectID_By_Username_Password
            = "SELECT Sub_id "
            + "FROM Sub_Subject "
            + "WHERE Sub_username=? and Sub_password=?";

    /**
     * Selects the subject identifier given its username.
     */
    public static final String Select_Password_By_Username
            = "SELECT Sub_password "
            + "FROM Sub_Subject "
            + "WHERE Sub_username=?";

    /**
     * Selects the subject identifier given its username.
     */
    public static final String Select_SujectID_By_Username
            = "SELECT Sub_id "
            + "FROM Sub_Subject "
            + "WHERE Sub_username=?";

    /**
     * Selects the subject certificate given its username.
     */
    public static final String Select_BI_By_Username
            = "SELECT Sub_BI "
            + "FROM Sub_Subject "
            + "WHERE Sub_username=?";

    /**
     * Selects the identifier of the subject - application relation given the subject identifier and the application name.
     */
    public static final String Select_SubjectAplicationRelationID_By_SubjectID_ApplicationName
            = "SELECT SA.SubApp_id "
            + "FROM App_Application A "
            + "INNER JOIN SubApp SA ON SA.SubAppApp_id=A.App_id "
            + "INNER JOIN Sub_Subject S ON S.Sub_id=SA.SubAppSub_id "
            + "WHERE Sub_id=? and App_reference=?";

    /**
     * Inserts a new sessions given the identifier of the subject - application relation, the client IP address and the port used.
     */
    public static final String Select_SessionID_By_SubAppID_IP_Port
            = "SELECT Ses_id FROM Ses_Session WHERE SesSubApp_id = ? AND Ses_clientIP = ? AND Ses_clientPort = ?";

    /**
     * Inserts a new sessions given the identifier of the subject - application relation, the client IP address and the port used.
     */
    public static final String Insert_Session_SubAppID_ClientIP_ClientPort
            = "INSERT INTO Ses_Session(Ses_id,SesSubApp_id,Ses_clientIP,Ses_clientPort)"
            + "VALUES(?,?,?,?)";

    /**
     * Inserts a new sessions given the identifier of the subject - application relation, the client IP address and the port used.
     */
    public static final String Delete_Session_SubAppID_ClientIP_ClientPort
            = "DELETE FROM Ses_Session WHERE Ses_clientIP = ? AND Ses_clientPort = ?";

    /**
     * Selects the name of the business schemas given an application name.
     */
    public static final String Select_BusinessSchemas_By_Application
            = "SELECT App_BusinessSchemas "
            + "FROM App_Application "
            + "WHERE App_reference=?";

    /**
     * Selects the roles and parent roles names given an identifier of the subject - application relation.
     */
    public static final String Select_Role_ParentRole_By_SubjectAplicationRelationID
            = "SELECT R.Rol_reference as role,R2.Rol_reference as parent "
            + "FROM Rol_Role R "
            + "LEFT OUTER JOIN Aut_Authorization AA ON AA.AutRol_id=R.Rol_id "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE AutSubApp_id=?";

    /**
     * Selects the roles and parent roles names given an app reference.
     */
    public static final String Select_Role_ParentRole_By_App
            = "SELECT R.Rol_reference as role,R2.Rol_reference as parent "
            + "FROM Rol_Role R LEFT "
            + "OUTER JOIN AppRol AP ON AP.AppRolRol_id=R.Rol_id "
            + "LEFT OUTER JOIN App_Application A ON A.App_id=AP.AppRolApp_id "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE A.App_reference=?";

    /**
     * Selects the parent role of a given role reference.
     */
    public static final String Select_ParentRole_By_RoleReference
            = "SELECT R2.Rol_reference "
            + "FROM Rol_Role R "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE  R.Rol_reference=?";

    /**
     * Selects the roles a subject - application pair can use from a given role.
     */
    public static final String Select_Role_ParentRole_With_Authorization_By_SubAppID_RoleReference
            = "SELECT R.Rol_reference as role,R2.Rol_reference as parent "
            + "FROM Rol_Role R "
            + "LEFT OUTER JOIN Aut_Authorization AA ON AA.AutRol_id=R.Rol_id "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE AutSubApp_id=? AND R.Rol_reference=?";

    /**
     * Selects the roles from delegation that a subject - application pair can use from a given role.
     */
    public static final String Select_Role_ParentRole_With_Delegated_Authorization_By_DelegatedSubAppID_RoleReference
            = "SELECT R.Rol_reference as role,R2.Rol_reference as parent "
            + "FROM Rol_Role R "
            + "LEFT OUTER JOIN AppRol AR ON AR.AppRolRol_id=R.Rol_id "
            + "LEFT OUTER JOIN Del_Delegation D ON D.DelAppRol_id=AR.AppRol_id "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE D.DelSubApp_id=? AND R.Rol_Reference=?";

    /**
     * Selects the Business Schemas URL, Cruds name, id and statement given a role.
     */
    public static final String Select_BusUrl_CrudReference_CrudID_Crud_By_RoleReference
            = "SELECT B.Bus_url,C.Crd_reference,C.Crd_id,C.Crd_Crud "
            + "FROM Bus_BusinessSchema B "
            + "INNER JOIN RolBus RB ON RB.RolBusBus_id=B.Bus_id "
            + "INNER JOIN Rol_Role R ON R.Rol_id=RB.RolBusRol_id "
            + "INNER JOIN BusCrd BC ON BC.BusCrdBus_id=B.Bus_id "
            + "INNER JOIN Crd_Crud C ON C.Crd_id=BC.BusCrdCrd_id "
            + "WHERE R.Rol_reference=?";

    /**
     * Selects the child roles given a role reference.
     */
    public static final String Select_ChildRoles_By_RoleReference
            = "SELECT R2.Rol_reference "
            + "FROM Rol_Role R1 "
            + "INNER JOIN Rol_Role R2 ON R2.RolRol_id=R1.Rol_id "
            + "WHERE R1.Rol_reference=? AND R1.Rol_id!=R2.Rol_id";

    /**
     * Selects the clients IP and Port given the identifier of the subject - application relation
     */
    public static final String Select_Clients_IP_Port_By_SessionSubjectApplicationID
            = "SELECT S.Ses_clientIP,S.Ses_clientPort "
            + "FROM Ses_Session S "
            + "WHERE S.SesSubApp_id=?";

    public static final String Select_Clients_IP_Port_By_RoleID
            = "SELECT S.Ses_clientIP,S.Ses_clientPort "
            + "FROM Ses_Session S "
            + "INNER JOIN SubApp SA ON S.SesSubApp_id = SA.SubApp_id "
            + "INNER JOIN App_Application A ON A.App_id = SA.SubAppApp_id "
            + "INNER JOIN AppRol AR ON A.App_id = AR.AppRolApp_id "
            + "WHERE AR.AppRolRol_id = ?";

    /**
     * Selects the clients IP and Port that use a given orchestration sequence identifier.
     */
    public static final String Select_Clients_IP_Port_By_OrchestrationSequenceID
            = "SELECT S.Ses_clientIP,S.Ses_clientPort "
            + "FROM BSSeqRol AS SR "
            + "INNER JOIN AppRol AS AR ON SR.RefRolID = AR.AppRolRol_id "
            + "INNER JOIN SubApp AS SA ON SA.SubAppApp_id = AR.AppRolApp_id "
            + "INNER JOIN Ses_Session AS S ON S.SesSubApp_id = SA.SubApp_id "
            + "WHERE SR.SeqID = ?";

    /**
     * Selects all clients' IPs and Ports
     */
    public static final String Select_All_Clients_IP_Port
            = "SELECT S.Ses_clientIP,S.Ses_clientPort "
            + "FROM Ses_Session AS S";

    /**
     * Selects roles and parent roles given a role identifier.
     */
    public static final String Select_Role_ParentRole_By_RoleID
            = "SELECT R.Rol_reference as role,R2.Rol_reference as parent "
            + "FROM Rol_Role R "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE R.Rol_id=?";

    /**
     * Selects the roles and parent roles for a given identifier of a subject - application pair.
     */
    public static final String Select_Role_ParentRole_Delegations_By_DelegatedSubAppID
            = "SELECT R.Rol_reference as role,R2.Rol_reference as parent "
            + "FROM Rol_Role R "
            + "LEFT OUTER JOIN AppRol AR ON AR.AppRolRol_id=R.Rol_id "
            + "LEFT OUTER JOIN Del_Delegation D ON D.DelAppRol_id=AR.AppRol_id "
            + "LEFT OUTER JOIN Rol_Role R2 ON R2.Rol_id=R.RolRol_id "
            + "WHERE D.DelSubApp_id=?";

    /**
     * Selects the orchestration information given a role reference.
     */
    public static final String Select_Sequences_ID_Position_BusUrl_By_RoleReference
            = "SELECT T1.SeqID, T1.SeqPos, T2.Bus_url "
            + "FROM BSSeqPos as T1 "
            + "INNER JOIN BSSeqRol as T4 ON T1.SeqID = T4.SeqID "
            + "INNER JOIN Bus_BusinessSchema as T2 ON T1.RefBusID = T2.Bus_id "
            + "INNER JOIN Rol_Role as T3 ON T3.Rol_id = T4.RefRolID "
            + "WHERE T3.Rol_reference = ?";

    /**
     * Selects the Bus URL given a sequence identifier and the relative position.
     */
    public static final String Select_BusUrl_By_SequenceID_Position
            = "SELECT T2.Bus_url "
            + "FROM RevokeList as T1 "
            + "INNER JOIN Bus_BusinessSchema as T2 ON T1.RefBusID = T2.Bus_id "
            + "WHERE T1.RefSeqID = ? AND T1.RefSeqPos = ?";

    /**
     * Selects the Bus URL given its identifier.
     */
    public static final String Select_BusUrl_By_BusID
            = "SELECT Bus_url "
            + "FROM Bus_BusinessSchema "
            + "WHERE Bus_id = ?";

    /**
     * Selects the orchestration status.
     */
    public static final String Select_Control_Status
            = "SELECT T1.value "
            + "FROM ControlInfo as T1 "
            + "WHERE T1.[key] = 'active'";

    /**
     * Selects a certificate by the alias.
     */
    public static final String Select_Certificate_By_Alias
            = "SELECT T1.bytes "
            + "FROM ControlInfo as T1 "
            + "WHERE T1.[key] = ?";

    /**
     * Selects a sequence information.
     */
    public static final String Select_Sequence_Positions_BusUrl_By_SequenceID
            = "SELECT T1.SeqPos, T3.Bus_url "
            + "FROM BSSeqPos as T1 "
            + "INNER JOIN BSSeqRol as T2 ON T1.SeqID = T2.SeqID "
            + "INNER JOIN Bus_BusinessSchema as T3 ON T1.RefBusID = T3.Bus_id "
            + "WHERE T1.SeqID = ?";

    /**
     * Selects the sequences available to a given RoleID.
     */
    public static final String Select_Sequences_ID_Positions_BusUrl_By_RoleID
            = "SELECT T1.SeqID, T1.SeqPos, T3.Bus_url "
            + "FROM BSSeqPos as T1 "
            + "INNER JOIN BSSeqRol as T2 ON T1.SeqID = T2.SeqID "
            + "INNER JOIN Bus_BusinessSchema as T3 ON T1.RefBusID = T3.Bus_id "
            + "WHERE T2.RefRolID = ?";

    /**
     * Selects the hierarchy of roles.
     */
    public static final String Select_Role_ParentRole
            = "SELECT T1.Rol_id, T1.RolRol_id "
            + "FROM Rol_Role as T1";

    /**
     * Selects the authorizations.
     */
    public static final String Select_Authorizations_By_SubAppID
            = "SELECT T1.Rol_id "
            + "FROM ( "
            + "SELECT T2.AutRol_id as Rol_id, T2.AutSubApp_id as SubApp_id "
            + "FROM Aut_Authorization as T2 "
            + "UNION "
            + "SELECT T2.DelAppRol_id as Rol_id, T2.DelSubApp_id as SubApp_id "
            + "FROM Del_Delegation as T2 "
            + ") as T1 "
            + "WHERE T1.SubApp_id = ?";

    /**
     * Selects the busid by role
     */
    public static final String Select_BusinessID_By_Role
            = "SELECT B.Bus_id "
            + "FROM Bus_BusinessSchema B "
            + "INNER JOIN RolBus RB ON RB.RolBusBus_id=B.Bus_id "
            + "INNER JOIN Rol_Role R ON R.Rol_id=RB.RolBusRol_id "
            + "WHERE Rol_reference=?";

    /**
     * Selects the cruds associated with a be.
     */
    public static final String Select_CRUDs_By_beid
            = "SELECT C.Crd_id,C.Crd_reference "
            + "FROM Crd_Crud C "
            + "INNER JOIN BusCrd BC ON BC.BusCrdCrd_id=C.Crd_id "
            + "INNER JOIN Bus_BusinessSchema B ON B.Bus_id=BC.BusCrdBus_id "
            + "WHERE Bus_id=?";

    public static final String Delete_Session_RemoteIDs
            = "DELETE FROM _remote.SessionQueries WHERE SessionID = ? AND QuerySRID = ?";

    public static final String Select_QueryRID
            = "SELECT RID FROM _remote.Queries WHERE QueryReference = ?";

    public static final String Select_QuerySRIDs_By_SessionID
            = "SELECT QuerySRID "
            + "FROM _remote.SessionQueries "
            + "WHERE SessionID = ?";

    public static final String Insert_Session_Query
            = "INSERT INTO _remote.SessionQueries (SessionID,QueryRID,QuerySRID) "
            + "VALUES (?,?,?)";

    /**
     * Deny instantiation.
     */
    private DBQueries() {
    }
}

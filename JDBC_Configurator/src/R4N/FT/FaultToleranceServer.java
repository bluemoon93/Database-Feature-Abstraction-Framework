package R4N.FT;

import R4N.CloudNetwork;
import R4N.CloudNetworkListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class FaultToleranceServer {

    public static final char createFile = 'c', deleteFile = 'd', clearStates = 's', setNewState = 'n',
            removeLastStateReverser = 'r', removeLastState = 'l', checkIfRecoveryIsNecessary = 'q', getStatements = 't',
            endThread='p', getStatementsFromSlave='m', imTheMaster='b';

    public void runServer(Connection conn) throws IOException {
        HashMap<String, ArrayList<String>> map = new HashMap();
        
        CloudNetworkListener lcl = new CloudNetworkListener_FT(map, conn);
        CloudNetwork lc = new CloudNetwork(4448, lcl);
        lc.start();
        
        ServerSocket a = new ServerSocket(5921);
        ArrayList<Object[]> slaves = new ArrayList();
        
        while (true) {
            System.out.println("Awaiting connection...");
            new FaultToleranceServer_Thread(a.accept(), map, conn, slaves).start();
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db");
        
        new FaultToleranceServer().runServer(conn);
    }
}

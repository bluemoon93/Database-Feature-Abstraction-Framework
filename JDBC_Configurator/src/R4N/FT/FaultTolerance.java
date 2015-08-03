/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import R4N.FT.FailureRecovery.DBStatement;
import java.util.ArrayList;

/**
 *
 * @author bluemoon
 */
public interface FaultTolerance {
    public void createFile(String id) throws Exception;
    
    public void deleteFile(String id) throws Exception;

    public void clearStates(String id) throws Exception;

    public void setNewState(String id, String state) throws Exception;
    
    public void removeLastStateReverser(String id) throws Exception;

    public void removeLastState(String id) throws Exception;
    
    public boolean checkIfRecoveryIsNecessary() throws Exception;
    
    public ArrayList<DBStatement> getStatements() throws Exception;
    
    public void reconnect() throws Exception;
}

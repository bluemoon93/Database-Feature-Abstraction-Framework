/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package R4N.FT;

import R4N.FT.FailureRecovery.DBStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author bluemoon
 */
public class FaultTolerance_Noop implements FaultTolerance {

    @Override
    public void createFile(String id) {

    }

    @Override
    public void deleteFile(String id){

    }

    @Override
    public void clearStates(String id) {

    }

    @Override
    public void setNewState(String id, String state) {

    }

    @Override
    public void removeLastStateReverser(String id){

    }

    @Override
    public void removeLastState(String id) {

    }

    @Override
    public boolean checkIfRecoveryIsNecessary() {
        return false;
    }

    @Override
    public ArrayList<DBStatement> getStatements() {
        return new ArrayList();
    }

    @Override
    public void reconnect() throws Exception {
        }
}

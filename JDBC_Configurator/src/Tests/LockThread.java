/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tests;

import R4N.FT.FaultTolerance_Noop;
import R4N.DFM.R4N_Transaction;
import R4N.DFM.R4N_TransactionSQL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static Tests.MySQL.printRS;
import Tests.ReadWriteLocks.Test2;

/**
 *
 * @author bluemoon
 */
public class LockThread extends Thread {
    Test2 b;
    public LockThread(Test2 a ){
        b=a;
    }
    
    @Override
    public void run() {
        try {
            b.testM();
        } catch (InterruptedException ex) {
            Logger.getLogger(LockThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

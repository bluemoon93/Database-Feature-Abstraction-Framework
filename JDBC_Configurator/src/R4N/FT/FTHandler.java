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
public class FTHandler {

    private final FaultTolerance ftm;

    public FTHandler(FaultTolerance g) {
        ftm = g;
    }

    public void createFile(String id) {
        while (true) {
            try {
                ftm.createFile(id);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }

    public void deleteFile(String id) {
        while (true) {
            try {
                ftm.deleteFile(id);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }

    public void clearStates(String id) {
        while (true) {
            try {
                ftm.clearStates(id);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }

    public void setNewState(String id, String state) {
            while (true) {
            try {
                ftm.setNewState(id, state);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }

    public void removeLastStateReverser(String id) {
        while (true) {
            try {
                ftm.removeLastStateReverser(id);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }

    public void removeLastState(String id) {
        while (true) {
            try {
                ftm.removeLastState(id);
                return;
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
    }

    public boolean checkIfRecoveryIsNecessary() {
        while (true) {
            try {
                return ftm.checkIfRecoveryIsNecessary();
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }

    public ArrayList<DBStatement> getStatements() {
        while (true) {
            try {
                return ftm.getStatements();
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                try {
                    System.out.println("FT system crashed! Retrying in 3");
                    Thread.sleep(1000);
                    System.out.println("2");
                    Thread.sleep(1000);
                    System.out.println("1");
                    Thread.sleep(1000);
                    ftm.reconnect();
                } catch (Exception ex1) {
                    System.out.println("Exception: " + ex1.getMessage());
                }
            }
        }
        
    }
}

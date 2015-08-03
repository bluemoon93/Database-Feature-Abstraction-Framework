/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author DIOGO
 */
interface ITransaction {

    void commit() throws SQLException;

    //int getTransactionIsolationLevel() throws SQLException;

    void releaseSavepoint(String savepoint) throws SQLException;

    void rollBack() throws SQLException;

    void rollBack(String savepoint) throws SQLException;

    void setAutoCommit(boolean autoCommit) throws SQLException;

    void setSavepoint() throws SQLException;

    void setSaveSavepoint(String name) throws SQLException;

    //void setTransactionIsolation(int level) throws SQLException;
}

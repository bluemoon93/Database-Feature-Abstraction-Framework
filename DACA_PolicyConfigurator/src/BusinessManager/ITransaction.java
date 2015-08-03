/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import java.sql.*;

/**
 * @author DIOGO
 */
public interface ITransaction {

    void commit() throws SQLException;

    int getTransactionIsolationLevel() throws SQLException;

    void releaseSavepoint(Savepoint savepoint) throws SQLException;

    void rollBack() throws SQLException;

    void rollBack(Savepoint savepoint) throws SQLException;

    void setAutoCommit(boolean autoCommit) throws SQLException;

    Savepoint setSavepoint() throws SQLException;

    Savepoint setSaveSavepoint(String name) throws SQLException;

    void setTransactionIsolation(int level) throws SQLException;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import java.sql.SQLException;

/**
 * @author OMP
 */
public interface ISession extends ITransaction {

    <T> T businessService(Class<T> BI, int crudId) throws BTC_Exception;

    <T> T businessService(Class<T> BI, int crudId, Integer activeSequence) throws BTC_Exception;

    void releaseBusinessSession() throws SQLException;
}

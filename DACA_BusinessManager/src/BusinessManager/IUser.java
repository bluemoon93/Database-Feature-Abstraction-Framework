/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessManager;

import LocalTools.BTC_Exception;

import java.sql.SQLException;

/**
 * @author DIOGO
 */
public interface IUser {
    /**
     * Adds a DACAChangeListener and calls its methods with the current values.
     * @param listener 
     */
    public void addAndCallDACAChangeListener(DACAChangeListener listener);

    /**
     * Adds a DACAChangeListener.
     * @param listener 
     */
    public void addDACAChangeListener(DACAChangeListener listener);

    /**
     * Removes a DACAChangeListener.
     * @param listener 
     */
    public void removeDACAChangeListener(DACAChangeListener listener);
    
    public <T> T instantiateBS(Class<T> bs, int crudId, ISession session) throws BTC_Exception;

    public <T> T instantiateBS(Class<T> bs, int crudId, ISession session, Integer activeSequence) throws BTC_Exception;

    public ISession getSession(String dburl) throws SQLException;
}

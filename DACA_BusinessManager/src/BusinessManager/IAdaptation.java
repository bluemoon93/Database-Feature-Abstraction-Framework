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
public interface IAdaptation {

    public void removeBusinessSchema(Class bs) throws BTC_Exception;

    public void addBusinessSchema(Class bs) throws BTC_Exception;

    public void repository(String jarFile, boolean Rebuild);

}

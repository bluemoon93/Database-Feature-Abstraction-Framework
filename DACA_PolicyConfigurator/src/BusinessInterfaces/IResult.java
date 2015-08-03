/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessInterfaces;

/**
 * Interface to get result information of an executeupdate method
 */
public interface IResult {
    /**
     * returns the number of affected rows by an executeupdate method
     *
     * @return number of affected rows by an executeupdate method
     */
    int getNAffectedRows();
}

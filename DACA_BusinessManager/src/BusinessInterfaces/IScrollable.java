/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BusinessInterfaces;

import java.sql.SQLException;

/**
 * @author OMP
 */
public interface IScrollable {

    /**
     * Method that moves the cursor, to the next row
     *
     * @return Boolean - True if command executed with success
     *
     */
    boolean moveNext();

    /**
     * Method, that moves the cursor, to the an absolute position
     *
     * @param pos int - absolute position
     * @return Boolean - True if command executed with success
     *
     */
    boolean moveAbsolute(int pos);

    /**
     * Method that moves the cursor to an position relative to actual position
     *
     * @param offset int - relative positiob
     * @return Boolean - True if command executed with success
     *
     */
    boolean moveRelative(int offset);

    /**
     * Method that moves the cursor, to the row before the first
     *
     *
     */
    void moveBeforeFirst();

    /**
     * Method that moves the cursor to the first row of the table
     *
     * @return Boolean - True if command executed with success
     *
     */
    boolean moveFirst();

    /**
     * Method that moves the cursor to the row after the last of the table
     *
     *
     */
    void moveAfterLst();

    /**
     * Method that moves the cursor to the last row of the table
     *
     * @return Boolean - True if command executed with success
     *
     */
    boolean moveLast();
}

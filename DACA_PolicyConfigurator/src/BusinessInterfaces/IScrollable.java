package BusinessInterfaces;

import java.sql.SQLException;

/**
 * Interface with the scrolls methods
 */
public interface IScrollable {
    /**
     * Method that moves the cursor, to the next row
     *
     * @return Boolean - True if command executed with success
     * @throws java.sql.SQLException
     */
    boolean moveNext() throws SQLException;

    /**
     * Method that moves the cursor, to the an absolute position
     *
     * @param pos int - absolute position
     * @return Boolean - True if command executed with success
     * @throws java.sql.SQLException
     */
    boolean moveAbsolute(int pos) throws SQLException;

    /**
     * Method that moves the cursor to an position relative to actual position
     *
     * @param offset int - relative positiob
     * @return Boolean - True if command executed with success
     * @throws java.sql.SQLException
     */
    boolean moveRelative(int offset) throws SQLException;

    /**
     * Method that moves the cursor, to the row before the first
     *
     * @throws java.sql.SQLException
     */
    void moveBeforeFirst() throws SQLException;

    /**
     * Method that moves the cursor to the first row of the table
     *
     * @return Boolean - True if command executed with success
     * @throws java.sql.SQLException
     */
    boolean moveFirst() throws SQLException;

    /**
     * Method that moves the cursor to the row after the last of the table
     *
     * @throws java.sql.SQLException
     */
    void moveAfterLst() throws SQLException;

    /**
     * Method that moves the cursor to the last row of the table
     *
     * @return Boolean - True if command executed with success
     * @throws java.sql.SQLException
     */
    boolean moveLast() throws SQLException;
}

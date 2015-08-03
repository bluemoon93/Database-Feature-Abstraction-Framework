package BusinessInterfaces;

import java.sql.SQLException;

/**
 * Interface with the deletion methods of a resultset
 */
public interface IDelete {

    /**
     * Method that deletes a row from the resultset
     *
     * @throws java.sql.SQLException
     */
    void deleteRow() throws SQLException;
}

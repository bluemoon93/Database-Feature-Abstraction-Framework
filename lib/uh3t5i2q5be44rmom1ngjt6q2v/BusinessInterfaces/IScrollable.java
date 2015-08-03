package BusinessInterfaces;
import java.math.*;
import java.sql.*;

public interface IScrollable {
 boolean moveNext() throws SQLException;
 boolean moveAbsolute(int pos) throws SQLException;
 boolean moveRelative(int offset) throws SQLException;
 void moveBeforeFirst() throws SQLException;
 boolean moveFirst() throws SQLException;
 void moveAfterLast() throws SQLException;
 boolean moveLast() throws SQLException;
}
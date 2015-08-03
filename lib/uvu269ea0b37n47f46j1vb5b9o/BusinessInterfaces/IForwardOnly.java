package BusinessInterfaces;
import java.math.*;
import java.sql.*;

public interface IForwardOnly {
  boolean moveNext() throws SQLException;
}
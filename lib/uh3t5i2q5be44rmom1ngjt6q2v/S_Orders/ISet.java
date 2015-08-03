package S_Orders;
import java.math.*;
import java.sql.*;

import S_Customers.IRCustomerID;
public interface ISet {
 void set(IRCustomerID args0,String args1) throws SQLException;
 void set() throws SQLException;
}
package S_Orders;
import java.math.*;
import java.sql.*;

import S_Customers.IRCustomerID;
public interface IExecute {
 void execute(IRCustomerID args0,String args1) throws SQLException;
 void execute() throws SQLException;
}
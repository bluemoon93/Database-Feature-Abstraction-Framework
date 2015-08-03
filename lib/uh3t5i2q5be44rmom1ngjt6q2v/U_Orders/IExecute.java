package U_Orders;
import java.math.*;
import java.sql.*;

import S_Orders.IROrderID;
public interface IExecute {
 void execute(IROrderID args0,String args1,String args2,String args3) throws SQLException;
}
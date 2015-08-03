package I_Orders;
import java.math.*;
import java.sql.*;

import S_Customers.IRCustomerID;
public interface IExecute {
 void execute(IRCustomerID args0,int args1,Date args2,Date args3,Date args4,int args5,double args6,String args7,String args8,String args9,String args10,String args11,String args12) throws SQLException;
}
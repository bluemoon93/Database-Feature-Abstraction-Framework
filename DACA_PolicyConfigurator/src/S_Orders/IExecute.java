package S_Orders;

import S_Customers.IRCustomerID;

import java.sql.SQLException;

public interface IExecute {
    void execute(IRCustomerID cid, String country) throws SQLException;

    void execute() throws SQLException;
}

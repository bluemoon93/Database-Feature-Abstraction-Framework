package S_Orders;

import S_Customers.*;

public abstract interface IExecute {

    public abstract void execute(IRCustomerID arg0, java.lang.String arg1) throws java.sql.SQLException;
    public abstract void execute() throws java.sql.SQLException;
}

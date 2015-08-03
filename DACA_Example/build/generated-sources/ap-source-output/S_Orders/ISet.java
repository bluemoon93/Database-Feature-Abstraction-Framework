package S_Orders;

import S_Customers.*;

public abstract interface ISet {

    public abstract void set(IRCustomerID arg0, java.lang.String arg1) throws java.sql.SQLException;
    public abstract void set() throws java.sql.SQLException;
}

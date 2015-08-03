package I_Orders;

import S_Customers.IRCustomerID;

import java.sql.Date;
import java.sql.SQLException;


public interface IExecute {
    void execute(IRCustomerID CustomerID, int EmployeeID, Date OrderDate, Date RequiredDate, Date ShippedDate, int ShipVia, double Freight, String ShipName, String ShipAddress, String ShipCity, String ShipRegion, String ShipPostalCode, String ShipCountry) throws SQLException;
}


package U_Orders;

import S_Orders.IROrderID;
import java.sql.SQLException;


public interface IExecute {
    void execute(IROrderID OrderID, String ShipAddress, String ShipCountry, String ShipCity) throws SQLException;
}


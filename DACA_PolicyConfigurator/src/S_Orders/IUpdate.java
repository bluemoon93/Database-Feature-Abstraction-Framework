package S_Orders;

import S_Customers.IRCustomerID;

import java.sql.Date;
import java.sql.SQLException;

public interface IUpdate {
    void beginUpdate() throws SQLException;

    void updateRow() throws SQLException;

    void cancelUpdate() throws SQLException;

    public void uOrderID(int OrderID) throws SQLException;

    public void uCustomerID(IRCustomerID CustomerID) throws SQLException;

    public void uEmployeeID(int EmployeeID) throws SQLException;

    public void uOrderDate(Date OrderDate) throws SQLException;

    public void uRequiredDate(Date RequiredDate) throws SQLException;

    public void uShippedDate(Date ShippedDate) throws SQLException;

    public void uShipVia(int ShipVia) throws SQLException;

    public void uFreight(double Freight) throws SQLException;

    public void uShipName(String ShipName) throws SQLException;

    public void uShipAddress(String ShipAddress) throws SQLException;

    public void uShipCity(String ShipCity) throws SQLException;

    public void uShipRegion(String ShipRegion) throws SQLException;

    public void uShipPostalCode(String ShipPostalCode) throws SQLException;

    public void uShipCountry(String ShipCountry) throws SQLException;
}

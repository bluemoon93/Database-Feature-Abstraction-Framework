package S_Orders;

import java.sql.Date;
import java.sql.SQLException;

public interface IInsert {
    void beginInsert() throws SQLException;

    void endInsert(boolean moveToPreviousRow) throws SQLException;

    void cancelInsert() throws SQLException;

    public void iOrderID(int OrderID) throws SQLException;

    public void iCustomerID(String CustomerID) throws SQLException;

    public void iEmployeeID(int EmployeeID) throws SQLException;

    public void iOrderDate(Date OrderDate) throws SQLException;

    public void iRequiredDate(Date RequiredDate) throws SQLException;

    public void iShippedDate(Date ShippedDate) throws SQLException;

    public void iShipVia(int ShipVia) throws SQLException;

    public void iFreight(double Freight) throws SQLException;

    public void iShipName(String ShipName) throws SQLException;

    public void iShipAddress(String ShipAddress) throws SQLException;

    public void iShipCity(String ShipCity) throws SQLException;

    public void iShipRegion(String ShipRegion) throws SQLException;

    public void iShipPostalCode(String ShipPostalCode) throws SQLException;

    public void iShipCountry(String ShipCountry) throws SQLException;
}

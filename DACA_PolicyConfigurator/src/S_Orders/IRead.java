package S_Orders;

import java.sql.Date;
import java.sql.SQLException;

public interface IRead extends IROrderID {
    public String CustomerID() throws SQLException;

    public int EmployeeID() throws SQLException;

    public Date OrderDate() throws SQLException;

    public Date RequiredDate() throws SQLException;

    public Date ShippedDate() throws SQLException;

    public int ShipVia() throws SQLException;

    public double Freight() throws SQLException;

    public String ShipName() throws SQLException;

    public String ShipAddress() throws SQLException;

    public String ShipCity() throws SQLException;

    public String ShipRegion() throws SQLException;

    public String ShipPostalCode() throws SQLException;

    public String ShipCountry() throws SQLException;
}

package S_Orders;


public abstract interface IRead extends IROrderID {

    public abstract java.sql.Date OrderDate() throws java.sql.SQLException;
    public abstract java.sql.Date RequiredDate() throws java.sql.SQLException;
    public abstract java.sql.Date ShippedDate() throws java.sql.SQLException;
    public abstract int ShipVia() throws java.sql.SQLException;
    public abstract double Freight() throws java.sql.SQLException;
    public abstract java.lang.String ShipName() throws java.sql.SQLException;
    public abstract java.lang.String CustomerID() throws java.sql.SQLException;
    public abstract java.lang.String ShipAddress() throws java.sql.SQLException;
    public abstract java.lang.String ShipCity() throws java.sql.SQLException;
    public abstract java.lang.String ShipRegion() throws java.sql.SQLException;
    public abstract java.lang.String ShipPostalCode() throws java.sql.SQLException;
    public abstract java.lang.String ShipCountry() throws java.sql.SQLException;
    public abstract int EmployeeID() throws java.sql.SQLException;
}

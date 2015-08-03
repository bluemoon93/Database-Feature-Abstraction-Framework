package S_Orders;


public abstract interface IInsert {

    public abstract void cancelInsert() throws java.sql.SQLException;
    public abstract void iOrderID(int arg0) throws java.sql.SQLException;
    public abstract void iCustomerID(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iEmployeeID(int arg0) throws java.sql.SQLException;
    public abstract void iShipVia(int arg0) throws java.sql.SQLException;
    public abstract void iFreight(double arg0) throws java.sql.SQLException;
    public abstract void iShipName(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iShipAddress(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iShipCity(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iShipRegion(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iShipPostalCode(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iShipCountry(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void beginInsert() throws java.sql.SQLException;
    public abstract void endInsert(boolean arg0) throws java.sql.SQLException;
    public abstract void iOrderDate(java.sql.Date arg0) throws java.sql.SQLException;
    public abstract void iRequiredDate(java.sql.Date arg0) throws java.sql.SQLException;
    public abstract void iShippedDate(java.sql.Date arg0) throws java.sql.SQLException;
}

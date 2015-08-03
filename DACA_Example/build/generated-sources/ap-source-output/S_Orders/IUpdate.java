package S_Orders;

import S_Customers.*;

public abstract interface IUpdate {

    public abstract void beginUpdate() throws java.sql.SQLException;
    public abstract void updateRow() throws java.sql.SQLException;
    public abstract void cancelUpdate() throws java.sql.SQLException;
    public abstract void uEmployeeID(int arg0) throws java.sql.SQLException;
    public abstract void uOrderDate(java.sql.Date arg0) throws java.sql.SQLException;
    public abstract void uRequiredDate(java.sql.Date arg0) throws java.sql.SQLException;
    public abstract void uShippedDate(java.sql.Date arg0) throws java.sql.SQLException;
    public abstract void uShipVia(int arg0) throws java.sql.SQLException;
    public abstract void uOrderID(int arg0) throws java.sql.SQLException;
    public abstract void uCustomerID(IRCustomerID arg0) throws java.sql.SQLException;
    public abstract void uFreight(double arg0) throws java.sql.SQLException;
    public abstract void uShipName(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uShipAddress(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uShipCity(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uShipRegion(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uShipPostalCode(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uShipCountry(java.lang.String arg0) throws java.sql.SQLException;
}

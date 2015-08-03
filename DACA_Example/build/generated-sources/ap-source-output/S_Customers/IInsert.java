package S_Customers;


public abstract interface IInsert {

    public abstract void cancelInsert() throws java.sql.SQLException;
    public abstract void beginInsert() throws java.sql.SQLException;
    public abstract void endInsert(boolean arg0) throws java.sql.SQLException;
    public abstract void iCompanyName(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iContactName(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iContactTitle(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iAddress(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iCity(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iRegion(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iPostalCode(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iCountry(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iPhone(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void iFax(java.lang.String arg0) throws java.sql.SQLException;
}

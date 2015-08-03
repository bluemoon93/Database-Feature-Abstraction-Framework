package S_Customers_byName;


public abstract interface IUpdate {

    public abstract void beginUpdate() throws java.sql.SQLException;
    public abstract void uAddress(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uRegion(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uPostalCode(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uCountry(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uCity(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void updateRow() throws java.sql.SQLException;
    public abstract void cancelUpdate() throws java.sql.SQLException;
    public abstract void uContactName(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uContactTitle(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uCompanyName(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uPhone(java.lang.String arg0) throws java.sql.SQLException;
    public abstract void uFax(java.lang.String arg0) throws java.sql.SQLException;
}

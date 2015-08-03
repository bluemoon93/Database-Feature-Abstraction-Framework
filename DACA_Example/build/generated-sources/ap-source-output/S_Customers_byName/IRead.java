package S_Customers_byName;


public abstract interface IRead extends IRCustomerID {

    public abstract java.lang.String ContactName() throws java.sql.SQLException;
    public abstract java.lang.String ContactTitle() throws java.sql.SQLException;
    public abstract java.lang.String Address() throws java.sql.SQLException;
    public abstract java.lang.String City() throws java.sql.SQLException;
    public abstract java.lang.String Region() throws java.sql.SQLException;
    public abstract java.lang.String PostalCode() throws java.sql.SQLException;
    public abstract java.lang.String Country() throws java.sql.SQLException;
    public abstract java.lang.String Phone() throws java.sql.SQLException;
    public abstract java.lang.String Fax() throws java.sql.SQLException;
    public abstract java.lang.String CompanyName() throws java.sql.SQLException;
}

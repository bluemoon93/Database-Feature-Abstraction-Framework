package S_Customers;

import java.sql.Date;
import java.sql.SQLException;

public interface IRead extends IRCustomerID {
    public String CompanyName() throws SQLException;

    public String ContactName() throws SQLException;

    public String ContactTitle() throws SQLException;

    public String Address() throws SQLException;

    public String City() throws SQLException;

    public String Region() throws SQLException;

    public String PostalCode() throws SQLException;

    public String Country() throws SQLException;

    public String Phone() throws SQLException;

    public String Fax() throws SQLException;


}
